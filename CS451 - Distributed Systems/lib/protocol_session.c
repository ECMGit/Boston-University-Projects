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
#include <string.h>
#include <stdlib.h>
#include <sys/types.h>
#include <strings.h>
#include <errno.h>
#include <pthread.h>

#include "net.h"
#include "protocol.h"
#include "protocol_utils.h"
#include "protocol_session.h"

#include "maze.h"

extern void
proto_session_dump(Proto_Session *s)
{
  fprintf(stderr, "Session s=%p:\n", s);
  fprintf(stderr, " fd=%d, extra=%p slen=%d, rlen=%d\n shdr:\n  ", 
	  s->fd, s->extra,
	  s->slen, s->rlen);
  proto_dump_msghdr(&(s->shdr));
  fprintf(stderr, " rhdr:\n  ");
  proto_dump_msghdr(&(s->rhdr));
}

extern void
proto_session_init(Proto_Session *s)
{
  if (s) bzero(s, sizeof(Proto_Session));
}

extern void
proto_session_reset_send(Proto_Session *s)
{
  bzero(&s->shdr, sizeof(s->shdr));
  s->slen = 0;
}

extern void
proto_session_reset_receive(Proto_Session *s)
{
  bzero(&s->rhdr, sizeof(s->rhdr));
  s->rlen = 0;
}

static void
proto_session_hdr_marshall_sver(Proto_Session *s, Proto_StateVersion v)
{
  s->shdr.sver.raw = htonll(v.raw);
}

static void
proto_session_hdr_unmarshall_sver(Proto_Session *s, Proto_StateVersion *v)
{
  v->raw = ntohll(s->rhdr.sver.raw);
}

static void
proto_session_hdr_marshall_pstate(Proto_Session *s, Proto_Player_State *ps)
{
    s->shdr.pstate.pid.pid.team  = htonl(ps->pid.pid.team);
    s->shdr.pstate.pid.pid.num  = htonl(ps->pid.pid.num);
    s->shdr.pstate.pid.raw  = htonl(ps->pid.raw);
    s->shdr.pstate.pos.pos.x  = htonl(ps->pos.pos.x); 
    s->shdr.pstate.pos.pos.y  = htonl(ps->pos.pos.y);
    s->shdr.pstate.pos.raw  = htonl(ps->pos.raw);
    s->shdr.pstate.state.inGame  = htonl(ps->state.inGame);
}

static void
proto_session_hdr_unmarshall_pstate(Proto_Session *s, Proto_Player_State *ps)
{
  ps->pid.pid.team = ntohl(s->rhdr.pstate.pid.pid.team);
  ps->pid.pid.num = ntohl(s->rhdr.pstate.pid.pid.num);
  ps->pid.raw = ntohl(s->rhdr.pstate.pid.raw);
  ps->pos.pos.x = ntohl(s->rhdr.pstate.pos.pos.x);
  ps->pos.pos.y = ntohl(s->rhdr.pstate.pos.pos.y);
  ps->pos.raw = ntohl(s->rhdr.pstate.pos.raw);
  ps->state.inGame = ntohl(s->rhdr.pstate.state.inGame);
}

static void
proto_session_hdr_marshall_gstate(Proto_Session *s, Proto_Game_State *gs)
{
  s->shdr.gstate.diff.raw = htonl(gs->diff.raw);
  s->shdr.gstate.state.raw = htonl(gs->state.raw);
  s->shdr.gstate.extra = htonl(gs->extra);
}

static void
proto_session_hdr_unmarshall_gstate(Proto_Session *s, Proto_Game_State *gs)
{
  gs->diff.raw = ntohl(s->rhdr.gstate.diff.raw);
  gs->state.raw = ntohl(s->rhdr.gstate.state.raw);
  gs->extra = ntohl(s->rhdr.gstate.extra);
}

static int
proto_session_hdr_unmarshall_blen(Proto_Session *s)
{
  return ntohl(s->rhdr.blen);
}

static void
proto_session_hdr_marshall_type(Proto_Session *s, Proto_Msg_Types t)
{
  s->shdr.type = htonl(t);
}

static int
proto_session_hdr_unmarshall_version(Proto_Session *s)
{
  return ntohl(s->rhdr.version);
}

extern Proto_Msg_Types
proto_session_hdr_unmarshall_type(Proto_Session *s)
{
  return ntohl(s->rhdr.type);
}

extern void
proto_session_hdr_unmarshall(Proto_Session *s, Proto_Msg_Hdr *h)
{
  
  h->version = proto_session_hdr_unmarshall_version(s);
  h->type = proto_session_hdr_unmarshall_type(s);
  proto_session_hdr_unmarshall_sver(s, &h->sver);
  proto_session_hdr_unmarshall_pstate(s, &h->pstate);
  proto_session_hdr_unmarshall_gstate(s, &h->gstate);
  h->blen = proto_session_hdr_unmarshall_blen(s);
}
   
extern void
proto_session_hdr_marshall(Proto_Session *s, Proto_Msg_Hdr *h)
{
  // ignore the version number and hard code to the version we support
  s->shdr.version = PROTOCOL_BASE_VERSION;
  proto_session_hdr_marshall_type(s, h->type);
  proto_session_hdr_marshall_sver(s, h->sver);
  proto_session_hdr_marshall_pstate(s, &h->pstate);
  proto_session_hdr_marshall_gstate(s, &h->gstate);
  // we ignore the body length as we will explicity set it
  // on the send path to the amount of body data that was
  // marshalled.
}

extern int 
proto_session_body_marshall_ll(Proto_Session *s, long long v)
{
  if (s && ((s->slen + sizeof(long long)) <  PROTO_SESSION_BUF_SIZE)) {
    *((long long *)(s->sbuf + s->slen)) = htonll(v);
    s->slen+=sizeof(long long);
    return 1;
  }
  return -1;
}

extern int 
proto_session_body_unmarshall_ll(Proto_Session *s, int offset, long long *v)
{
  if (s && ((s->rlen - (offset + sizeof(long long))) >=0 )) {
    *v = *((long long *)(s->rbuf + offset));
    *v = htonl(*v);
    return offset + sizeof(long long);
  }
  return -1;
}

extern int 
proto_session_body_marshall_int(Proto_Session *s, int v)
{
  if (s && ((s->slen + sizeof(int)) < PROTO_SESSION_BUF_SIZE)) {
    *((int *)(s->sbuf + s->slen)) = htonl(v);
    s->slen+=sizeof(int);
    return 1;
  }
  return -1;
}

extern int 
proto_session_body_unmarshall_int(Proto_Session *s, int offset, int *v)
{
  if (s && ((s->rlen  - (offset + sizeof(int))) >=0 )) {
    *v = *((int *)(s->rbuf + offset));
    *v = htonl(*v);
    return offset + sizeof(int);
  }
  return -1;
}

extern int 
proto_session_body_marshall_char(Proto_Session *s, char v)
{
  if (s && ((s->slen + sizeof(char)) < PROTO_SESSION_BUF_SIZE)) {
    s->sbuf[s->slen] = v;
    s->slen+=sizeof(char);
    return 1;
  }
  return -1;
}

extern int 
proto_session_body_unmarshall_char(Proto_Session *s, int offset, char *v)
{
  if (s && ((s->rlen - (offset + sizeof(char))) >= 0)) {
    *v = s->rbuf[offset];
    return offset + sizeof(char);
  }
  return -1;
}

extern int
proto_session_body_reserve_space(Proto_Session *s, int num, char **space)
{
  if (s && ((s->slen + num) < PROTO_SESSION_BUF_SIZE)) {
    *space = &(s->sbuf[s->slen]);
    s->slen += num;
    return 1;
  }
  *space = NULL;
  return -1;
}

extern int
proto_session_body_ptr(Proto_Session *s, int offset, char **ptr)
{
  if (s && ((s->rlen - offset) > 0)) {
    *ptr = &(s->rbuf[offset]);
    return 1;
  }
  return -1;
}
	    
extern int
proto_session_body_marshall_bytes(Proto_Session *s, int len, char *data)
{
  if (s && ((s->slen + len) < PROTO_SESSION_BUF_SIZE)) {
    memcpy(s->sbuf + s->slen, data, len);
    s->slen += len;
    return 1;
  }
  return -1;
}

extern int
proto_session_body_unmarshall_bytes(Proto_Session *s, int offset, int len, 
				     char *data)
{
  if (s && ((s->rlen - (offset + len)) >= 0)) {
    memcpy(data, s->rbuf + offset, len);
    return offset + len;
  }
  return -1;
}

/*
	Maze Marshalling
*/

extern void
proto_session_maze_marshall(Proto_Session *s, char* buf)
{ 
	s->withmaze = 1;
	proto_session_set_data(s, buf);
}

extern void
proto_session_maze_unmarshall(Proto_Session *s, char *buf)
{
 buf = (char*)proto_session_get_data(s);
}

// rc < 0 on comm failures
// rc == 1 indicates comm success
extern  int
proto_session_send_msg(Proto_Session *s, int reset)
{
  s->shdr.blen = htonl(s->slen);

  // write request
  if (net_writen(s->fd, &(s->shdr), sizeof(s->shdr)) != sizeof(s->shdr)) 
    return -1;

  if (s->slen) {
    if (net_writen(s->fd, s->sbuf, s->slen) != s->slen) {
      return -2;
    }
  }

	int jj = 0;	
	if(s->withmaze == 1) {
		if ((jj = net_writen(s->fd, s->extra, sizeof(s->extra))) != sizeof(s->extra)) {
			printf("sizes don't match\n");		
			printf("sizeofmaze: %d\n",sizeof(s->extra));
			printf("sizeofjj: %d\n",jj);
		}
	}		
		
  if (proto_debug()) {
    fprintf(stderr, "%p: proto_session_send_msg: SENT:\n", pthread_self());
    proto_session_dump(s);
  }

  // communication was successfull 
  if (reset) proto_session_reset_send(s);

  return 1;
}

extern int
proto_session_rcv_msg(Proto_Session *s)
{
  
  proto_session_reset_receive(s);

  // read reply
  if (net_readn(s->fd, &(s->rhdr), sizeof(s->rhdr)) != sizeof(s->rhdr)) 
    return -3;

  s->rlen = ntohl(s->rhdr.blen);
  if (s->rlen) {
    if (s->rlen > PROTO_SESSION_BUF_SIZE || 
	net_readn(s->fd, s->rbuf, s->rlen)!=s->rlen) {
      return -4;
    }
  }

	int jj = 0;
	if(s->withmaze == 0) {
		if ((jj = net_readn(s->fd, s->extra, sizeof(s->extra))) != sizeof(Maze)) {
			printf("read sizes don't match\n");		
			printf("sizeofmaze: %d\n",sizeof(s->extra));
			printf("sizeofjj: %d\n",jj);
		}
	}
		 
  if (proto_debug()) {
    fprintf(stderr, "%p: proto_session_rcv_msg: RCVED:\n", pthread_self());
    proto_session_dump(s);
  }
  return 1;
}

extern int
proto_session_rpc(Proto_Session *s)
{
  int rc;
  
  rc = proto_session_send_msg(s,1);
  if (rc==1) {
    rc = proto_session_rcv_msg(s);
  } else {
    fprintf(stderr, "Failed to send message to server.\n");
  }
  return rc;
}

