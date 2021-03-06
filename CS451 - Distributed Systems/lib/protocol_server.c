/******************************************************************************
* Copyright (C) 2011 by Jonathan Appavoo, Boston University
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
* THE SOFTWARE.
*****************************************************************************/

#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <strings.h>
#include <errno.h>
#include <pthread.h>

#include "net.h"
#include "protocol.h"
#include "protocol_utils.h"
#include "protocol_server.h"
#include "types.h"
#include "maze.h"
#include "player.h"
#include "objects.h"

#define PROTO_SERVER_MAX_EVENT_SUBSCRIBERS 200

struct {
  FDType   RPCListenFD;
  PortType RPCPort;
  Maze *m;
  Players *pp;
  Object *o;

  FDType             EventListenFD;
  PortType           EventPort;
  pthread_t          EventListenTid;
  pthread_mutex_t    EventSubscribersLock;
  int                EventLastSubscriber;
  int                EventNumSubscribers;
  FDType             EventSubscribers[PROTO_SERVER_MAX_EVENT_SUBSCRIBERS];
  Proto_Session      EventSession; 
  pthread_t          RPCListenTid;
  Proto_MT_Handler   session_lost_handler;
  Proto_MT_Handler   base_req_handlers[PROTO_MT_REQ_BASE_RESERVED_LAST - 
				       PROTO_MT_REQ_BASE_RESERVED_FIRST-1];


} Proto_Server;

extern PortType proto_server_rpcport(void) { return Proto_Server.RPCPort; }
extern PortType proto_server_eventport(void) { return Proto_Server.EventPort; }

int NYI(void) { fprintf(stderr, "NYI\n"); return 1; }

extern Proto_Session *
proto_server_event_session(void) 
{ 
  return &Proto_Server.EventSession; 
}

extern int
proto_server_set_session_lost_handler(Proto_MT_Handler h)
{
  Proto_Server.session_lost_handler = h;
}

extern int
proto_server_set_req_handler(Proto_Msg_Types mt, Proto_MT_Handler h)
{
  int i;

  if (mt>PROTO_MT_REQ_BASE_RESERVED_FIRST &&
      mt<PROTO_MT_REQ_BASE_RESERVED_LAST) {
    i = mt - PROTO_MT_REQ_BASE_RESERVED_FIRST - 1;
    Proto_Server.base_req_handlers[i]=h;
    return 1;
  } else {
    return -1;
  }
}


static int
proto_server_record_event_subscriber(int fd, int *num)
{
  int rc=-1;

  pthread_mutex_lock(&Proto_Server.EventSubscribersLock);

  if (Proto_Server.EventLastSubscriber < PROTO_SERVER_MAX_EVENT_SUBSCRIBERS
      && Proto_Server.EventSubscribers[Proto_Server.EventLastSubscriber]
      ==-1) {
    Proto_Server.EventSubscribers[Proto_Server.EventLastSubscriber]=fd;
    *num=Proto_Server.EventLastSubscriber;
    Proto_Server.EventLastSubscriber++;
    Proto_Server.EventNumSubscribers++;
    rc = 1;
  } else {
    int i;
    for (i=0; i< PROTO_SERVER_MAX_EVENT_SUBSCRIBERS; i++) {
      if (Proto_Server.EventSubscribers[i]==-1) {
	Proto_Server.EventSubscribers[i]=fd;
	Proto_Server.EventNumSubscribers++;
	*num=i;
	rc=1;
      }
    }
  }

  pthread_mutex_unlock(&Proto_Server.EventSubscribersLock);

  return rc;
}

static
void *
proto_server_event_listen(void *arg)
{
  int fd = Proto_Server.EventListenFD;
  int connfd;

  if (net_listen(fd)<0) {
    exit(-1);
  }

  for (;;) {
    connfd = net_accept(fd);
    if (connfd < 0) {
      fprintf(stderr, "Error: EventListen accept failed (%d)\n", errno);
    } else {
      int i;
      fprintf(stderr, "EventListen: connfd=%d -> ", connfd);

      if (proto_server_record_event_subscriber(connfd,&i)<0) {
	fprintf(stderr, "oops no space for any more event subscribers\n");
	close(connfd);
      } else {
	fprintf(stderr, "subscriber num %d\n", i);
      }
    } 
  }
} 

void
proto_server_post_event(void) 
{
  int i;
  int num;

  pthread_mutex_lock(&Proto_Server.EventSubscribersLock);

  i = 0;
  num = Proto_Server.EventNumSubscribers;
  while (num) {
    Proto_Server.EventSession.fd = Proto_Server.EventSubscribers[i];
    if (Proto_Server.EventSession.fd != -1) {
      num--;
      if (proto_session_send_msg(&Proto_Server.EventSession,0)<0) {
	// must have lost an event connection
	close(Proto_Server.EventSession.fd);
	Proto_Server.EventSubscribers[i]=-1;
	Proto_Server.EventNumSubscribers--;
	Proto_Server.session_lost_handler(&Proto_Server.EventSession);
      } 
      // FIXME: add ack message here to ensure that game is updated 
      // correctly everywhere... at the risk of making server dependent
      // on client behaviour  (use time out to limit impact... drop
      // clients that misbehave but be carefull of introducing deadlocks
    }
    i++;
  }
  proto_session_reset_send(&Proto_Server.EventSession);
  pthread_mutex_unlock(&Proto_Server.EventSubscribersLock);
}


static void *
proto_server_req_dispatcher(void * arg)
{
  Proto_Session s;
  Proto_Msg_Types mt;
  Proto_MT_Handler hdlr;
  int i;
  unsigned long arg_value = (unsigned long) arg;
  
  pthread_detach(pthread_self());

  proto_session_init(&s);

  s.fd = (FDType) arg_value;
  // do this so server wont listen for maze
  s.withmaze = 1;
  //find better way to do the above

  fprintf(stderr, "proto_rpc_dispatcher: %p: Started: fd=%d\n", 
	  pthread_self(), s.fd);

  for (;;) {
    if (proto_session_rcv_msg(&s)==1) {
      mt = proto_session_hdr_unmarshall_type(&s);
      proto_session_dump(&s);
      if (mt > PROTO_MT_REQ_BASE_RESERVED_FIRST && mt < PROTO_MT_REQ_BASE_RESERVED_LAST) {
	i = mt - PROTO_MT_REQ_BASE_RESERVED_FIRST - 1;
	hdlr = Proto_Server.base_req_handlers[i];
	if (hdlr(&s)<0) goto leave;
      }
    } else {
      fprintf(stderr, "rcv_msg failed.\n");
      goto leave;
    }
  }
 leave:
  Proto_Server.session_lost_handler(&s);
  close(s.fd);
  return NULL;
}

static
void *
proto_server_rpc_listen(void *arg)
{
  int fd = Proto_Server.RPCListenFD;
  unsigned long connfd;
  pthread_t tid;
  
  if (net_listen(fd) < 0) {
    fprintf(stderr, "Error: proto_server_rpc_listen listen failed (%d)\n", errno);
    exit(-1);
  }

  for (;;) {
    connfd = net_accept(fd);
    if (connfd < 0) {
      fprintf(stderr, "Error: proto_server_rpc_listen accept failed (%d)\n", errno);
    } else {
      pthread_create(&tid, NULL, &proto_server_req_dispatcher,
		     (void *)connfd);
    }
  }
}

extern int
proto_server_start_rpc_loop(void)
{
  if (pthread_create(&(Proto_Server.RPCListenTid), NULL, 
		     &proto_server_rpc_listen, NULL) !=0) {
    fprintf(stderr, 
	    "proto_server_rpc_listen: pthread_create: create RPCListen thread failed\n");
    perror("pthread_create:");
    return -3;
  }
  return 1;
}

static int 
proto_session_lost_default_handler(Proto_Session *s)
{
  fprintf(stderr, "Session lost...:\n");
  proto_session_dump(s);
  return -1;
}

static int 
proto_server_mt_null_handler(Proto_Session *s)
{
  int rc=1;
  Proto_Msg_Hdr h;
  
  fprintf(stderr, "proto_server_mt_null_handler: invoked for session:\n");
  proto_session_dump(s);

  // setup dummy reply header : set correct reply message type and 
  // everything else empty
  bzero(&h, sizeof(s));
  h.type = proto_session_hdr_unmarshall_type(s);
  h.type += PROTO_MT_REP_BASE_RESERVED_FIRST;
  proto_session_hdr_marshall(s, &h);

  // setup a dummy body that just has a return code 
  proto_session_body_marshall_int(s, 0xdeadbeef);

  rc=proto_session_send_msg(s,1);

  return rc;
} 

static void
marshall_mtonly(Proto_Session *s, Proto_Msg_Types mt) {
  Proto_Msg_Hdr h;
  h = s->shdr;
  bzero(&h, sizeof(h));
  h.type = mt;
  proto_session_hdr_marshall(s, &h);
};

static int
proto_server_mt_hello_handler(Proto_Session *s)
{
  int rc;
  int i;
  Proto_Msg_Hdr h;
  Player *pHello;
  Maze_Cell *cell;

  fprintf(stderr, "proto_server_mt_hello_handler: invoked for session:\n");
  //proto_session_dump(s);

  bzero(&h, sizeof(s));
  h.type = proto_session_hdr_unmarshall_type(s);
  h.type += PROTO_MT_REP_BASE_RESERVED_FIRST;

  //assign to a team in players struct
  //initialize player struct
  //update header pstate 
    
    pHello = player_add(Proto_Server.pp);   
    
    //place player on map
    if (maze_player_add(Proto_Server.m, pHello) < 0) {
      fprintf(stderr, "ERROR: failed to add player to map");
      rc = proto_session_lost_default_handler(s);
      close(s);
      return rc;
    }
	Maze_Cell *c = maze_cell(Proto_Server.m, pHello->posx, pHello->posy);
        maze_dump_cell(c);

  player_dump(pHello);

  //update header to contain Player pid and pos, and that player is ingame
  h.pstate.state.inGame = pHello->state;
  h.pstate.pid.raw = pHello->pid;
  h.pstate.pid.pid.team = pHello->team;
  h.pstate.pid.pid.num =  pHello->teamIndex;
  h.pstate.pos.pos.x = pHello->posx;
  h.pstate.pos.pos.y = pHello->posy;

  fprintf(stderr, "Outgoing msg hdr =\n");
  proto_dump_msghdr(&h);
      
  // message for client who envoked this RPC
  proto_session_hdr_marshall(s, &h);
  proto_session_body_marshall_int(s, 1);

  if (proto_session_send_msg(s,1)<0) {
    fprintf(stderr, "RPC reply failed.");
    rc = proto_session_lost_default_handler(s);
    close(s);
    return rc;
  }

  char buf[40001];	
  maze_to_ascii(Proto_Server.m, buf);

  //do update client
  marshall_mtonly(&(Proto_Server.EventSession), PROTO_MT_EVENT_BASE_UPDATE);
  proto_session_maze_marshall(&(Proto_Server.EventSession), buf);
  proto_server_post_event();

  return 1;
}

static int
proto_server_mt_move_handler(Proto_Session *s)
{

  //NYI();
  
  int rc=1;
  int i;
  int winner;
  char *move;
  int moveIndex;
  Proto_Msg_Hdr h;
  Player *p;
  Proto_Session *eventSession = &Proto_Server.EventSession;
  
  fprintf(stderr, "proto_server_move: invoked for session:\n");
  proto_session_dump(s);
  
  bzero(&h, sizeof(s));
  h.type = proto_session_hdr_unmarshall_type(s);
  h.type += PROTO_MT_REP_BASE_RESERVED_FIRST;
  
  //unmarshall body to get move (char to use as direction)
  if (proto_session_body_unmarshall_char(s,0,move) < 0) {
     fprintf(stderr, "Failed to unmarshall body: Cannot process move.\n");
  }

  moveIndex = atoi(move);
  proto_session_hdr_unmarshall(s, &h);

  // extract player info
  int team = h.pstate.pid.pid.team;
  int num = h.pstate.pid.pid.num;
  int pid = h.pstate.pid.raw;
  int inGame = h.pstate.state.inGame;
  
  if(team == 1){
    p = Proto_Server.pp->team1[num];
  } else {
    p = Proto_Server.pp->team2[num];
  }

  if(moveIndex == 1){ rc = maze_player_move(Proto_Server.m, p, 0, -1); }
  else if(moveIndex == 2){ rc = maze_player_move(Proto_Server.m, p, 1, 0); }
  else if(moveIndex == 3){ rc = maze_player_move(Proto_Server.m, p, 0, 1); }
  else if(moveIndex == 4){ rc = maze_player_move(Proto_Server.m, p, -1, 0); }
  else{ rc = -1; }

  // update header
  h.pstate.pos.pos.x = p->posx;
  h.pstate.pos.pos.y = p->posy;

  // marshall
  proto_session_hdr_marshall(s, &h);
  proto_session_body_marshall_int(s, rc);

  //send back to client
  if (proto_session_send_msg(s,1)<0) {
    fprintf(stderr, "RPC reply failed.");
    rc = proto_session_lost_default_handler(s);
    close(s);
    return rc;
  }

  if(rc == 1){
    //do update client
    marshall_mtonly(&(Proto_Server.EventSession), PROTO_MT_EVENT_BASE_UPDATE);
    char buf[40001];	
    maze_to_ascii(Proto_Server.m, buf);
    proto_session_maze_marshall(&(Proto_Server.EventSession), &buf);
    proto_server_post_event();
  }

  return rc;
}

static int
proto_server_mt_pickup_handler(Proto_Session *s)
{
  int rc;

  //check if player already holds an object, cannot hold two at once
  NYI();
}

static int
proto_server_mt_drop_handler(Proto_Session *s)
{
  NYI();

}
  	
static int
proto_server_mt_goodbye_handler(Proto_Session *s)
{
  int rc=1;
  int i;
  Proto_Msg_Hdr h;
  
  fprintf(stderr, "proto_server_goodbye: invoked for session:\n");
  proto_session_dump(s);
  
  bzero(&h, sizeof(s));
  
  //need to clean up game structs to prepare for next game
  proto_session_reset_send(s);
  proto_session_reset_receive(s);
  //keep fd so clients are still connected, can start new game

  return rc;
  
}

extern int
proto_server_init(void)
{
  int i;
  int rc;

  proto_session_init(&Proto_Server.EventSession);

  proto_server_set_session_lost_handler(proto_session_lost_default_handler);
#if 0
  for (i=PROTO_MT_REQ_BASE_RESERVED_FIRST+1; 
       i<PROTO_MT_REQ_BASE_RESERVED_LAST; i++) {
    proto_server_set_req_handler(i, proto_server_mt_null_handler);
  }
#endif
  proto_server_set_req_handler(PROTO_MT_REQ_BASE_HELLO, proto_server_mt_hello_handler);


  for (i=0; i<PROTO_SERVER_MAX_EVENT_SUBSCRIBERS; i++) {
    Proto_Server.EventSubscribers[i]=-1;
  }
  Proto_Server.EventNumSubscribers=0;
  Proto_Server.EventLastSubscriber=0;
  pthread_mutex_init(&Proto_Server.EventSubscribersLock, 0);

  // initialize the maze subsystem

  if (maze_init(&(Proto_Server.m))<0) {
    fprintf(stderr, "ERROR: server: failed to allocate map\n");
    return -1;
  }

  if (maze_load_file("daGame.map", Proto_Server.m) <= 0) {
    fprintf(stderr, "ERROR: could not load map\n");
    return -1;
  }

  char buf[40001];	
  maze_to_ascii(Proto_Server.m, buf);

  //initialize players structs
  if (players_init(&(Proto_Server.pp))<0) {
    fprintf(stderr, "ERROR: could not initialize players\n");
    return -1;
  }

// do this so server wont listen for maze
 Proto_Server.EventSession.withmaze = 1;
//find better way to do the above

  rc=net_setup_listen_socket(&(Proto_Server.RPCListenFD),
			     &(Proto_Server.RPCPort));

  if (rc==0) { 
    fprintf(stderr, "prot_server_init: net_setup_listen_socket: FAILED for RPCPort\n");
    return -1;
  }

  Proto_Server.EventPort = Proto_Server.RPCPort + 1;

  rc=net_setup_listen_socket(&(Proto_Server.EventListenFD),
			     &(Proto_Server.EventPort));

  if (rc==0) { 
    fprintf(stderr, "proto_server_init: net_setup_listen_socket: FAILED for EventPort=%d\n", 
	    Proto_Server.EventPort);
    return -2;
  }

  if (pthread_create(&(Proto_Server.EventListenTid), NULL, 
		     &proto_server_event_listen, NULL) !=0) {
    fprintf(stderr, 
	    "proto_server_init: pthread_create: create EventListen thread failed\n");
    perror("pthread_createt:");
    return -3;
  }

  return 0;
}
