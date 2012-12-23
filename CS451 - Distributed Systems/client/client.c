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
#include <string.h>
#include <pthread.h>
#include "../lib/types.h"
#include "../lib/net.h"
#include "../lib/maze.h"
#include "../lib/player.h"
#include "../lib/objects.h"
#include "../lib/ui.h"
#include "../lib/protocol_client.h"
#include "../lib/protocol_utils.h"

#define STRLEN 81

typedef struct {
  char host[STRLEN];
  PortType p;
  Maze *m;
  unsigned long long version;
  Proto_Client_Handle ph;
  Players *Teams;
  Player *self;
  Objects *o;
  int inGame;               //0 if no, 1 if yes
  pthread_mutex_t GS_lock;
  // probably want more stuff here ;-)
} GameState;

typedef struct ClientState  {
  GameState GS;
  UI *ui;
  struct ClientState *next; 
} Client;

struct Globals {
  char host[STRLEN];
  PortType port;
  enum UITYPES { UI_SHELL=1, UI_GUI=2 } ui;  //comment out these
  Client *clients;                           //lines if it won't
  pthread_mutex_t lock;                      //compile
} globals;

int NYI(void) { fprintf(stderr, "NYI\n"); return 1; }

static int
gameStateInit(GameState *GS)
{

  GS = (GameState *)malloc(sizeof(GameState));
  if (GS == NULL) return -1;
  bzero(GS, sizeof(GameState));
  pthread_mutex_init(&(GS->GS_lock), NULL);
  return 1;

}

static int
clientInit(Client *C)
{
  
  bzero(C, sizeof(Client));
  
  // initialize game state struct
  if (gameStateInit(&(C->GS)) < 0) {
    fprintf(stderr, "ERROR: failed to initialize client game state");
  }
  
  return 1;
}

static void
game_state_begin_transaction(GameState *GS)
{
  //add lock for game state
  pthread_mutex_lock(&(GS->GS_lock));
}

static void
game_state_end_transaction(GameState *GS)
{
   //unlock game state
   pthread_mutex_unlock(&(GS->GS_lock));
}

static int
ignore_event(GameState *GS)
{
  // we should ignore events until we have obtained
  // a valid base game state (eg. have processed hello response)
 
  return (GS->inGame);
}

static int
update_event_handler(Proto_Session *s)
{

  //if(!ignore_event()
  // Client *C = proto_session_get_data(s);
  Maze *m;
  proto_session_maze_unmarshall(s, &m);
  //*(C->GS.m) = *m;

  fprintf(stderr, "%s: called\n", __func__);
  return 1;
}

int 
game_hello(Client *C)
{
  int rc;
  Proto_Client_Handle ch = C->GS.ph;

  if (C->GS.inGame == 1) {
    fprintf(stderr, "Already said Hello. Try moving around!\n");
    return 1;
  }
  
  rc = proto_client_hello(ch);
  printf("hello: rc=%x\n", rc);
  if (rc > 0) {    
    rc = game_process_hello(C, rc);
  } else {
    fprintf(stderr, "Hello failed. Could not join game\n");
    //returns rc, will break connection from server I think, if it's not
    //already broken
  }
  return rc;
}

int
game_move(Client *C, char data)
{
  int rc;
  Proto_Session *s;
  Proto_Client_Handle ch = C->GS.ph;
  s = proto_client_rpc_session(ch);
  proto_session_dump(s);

  rc = proto_client_move(ch, data);
  printf("move: rc=%x\n", rc);
  if (rc > 0) {    
    rc = game_process_move(C, data);
  } else {
    fprintf(stderr, "Move failed!\n");
  }
  return rc;
}

int 
startConnection(Client *C, char *host, PortType port, Proto_MT_Handler h)
{
  if (globals.host[0]!=0 && globals.port!=0) {
    if (proto_client_connect(C->GS.ph, host, port)!=0) {
      fprintf(stderr, "failed to connect\n");
      return -1;
    }
    proto_session_set_data(proto_client_event_session(C->GS.ph), C);
#if 1
    if (h != NULL) {
      proto_client_set_event_handler(C->GS.ph, PROTO_MT_EVENT_BASE_UPDATE, 
				     h);
    }
#endif
    return 1;
  }
  return 0;
}

void
globalsAddClient(Client *c)
{
  Client *tmp=globals.clients;
  
  pthread_mutex_lock(&(globals.lock));

  if (tmp==NULL) globals.clients = c;
  else {
    while (tmp->next != NULL) tmp = tmp->next;
    tmp->next = c;
  }

  pthread_mutex_unlock(&(globals.lock));
  return;
}

static int
createClient(Client **c)
{ 
  Client *C;

  *c = NULL;

  C = (Client *)malloc(sizeof(Client));
  if (C == 0) { 
    fprintf(stderr, "ERROR: client: failed to allocate Client struct\n");
    return -1;
  }

  clientInit(C);

  // initialize the maze subsystem
  if (maze_init(&(C->GS.m))<0) {
    fprintf(stderr, "ERROR: client: failed to allocate map\n");
    return -1;
  }

  // initialize the player subsystem
  if (players_init(&(C->GS.Teams))<0) {
    fprintf(stderr, "client: main: ERROR initializing player system\n");
    return -1;
  }
#if 0
  // initialize the object subsystem
  if (objs_init(&(C->GS.objs))<0) {
    fprintf(stderr, "client: main: ERROR initializing object system\n");
    return -1;
  }
 
#endif
  // initialize the client protocol subsystem
  if (proto_client_init(&(C->GS.ph))<0) {
    fprintf(stderr, "client: main: ERROR initializing proto system\n");
    return -1;
  }

  globalsAddClient(C);
  *c = C;
  return 1;
}

int
prompt(int menu) 
{
  static char MenuString[] = "\nclient> ";
  int ret;
  int c=0;

  if (menu) printf("%s", MenuString);
  fflush(stdout);
  c = getchar();
  return c;
}


// FIXME:  this is ugly maybe the separation of the proto_client code and
//         the game code is dumb
int
game_process_reply(Client *C)
{
  Proto_Session *s;

  s = proto_client_rpc_session(C->GS.ph);

  fprintf(stderr, "%s: do something %p\n", __func__, s);

  return 1;
}

//double check that reply is unmarshalled already
int
game_process_hello(Client *C, int teamNum)
{
  Proto_Session *s;
  Proto_Msg_Hdr h;
  Player *p;

  game_state_begin_transaction(&(C->GS));

  s = proto_client_rpc_session(C->GS.ph);
  proto_session_hdr_unmarshall(s, &h);
  p = C->GS.self;
  proto_dump_msghdr(&h);

  // if Client is not in game, read in info. Otherwise, this is a new player joining the game.
  //initialize player
  player_init(&p);
  
  if (h.pstate.pid.pid.team==2) {
    fprintf(stderr, "You are on team GREEN.\n");
    p->team = 2;
    p->teamIndex = h.pstate.pid.pid.num;
    C->GS.Teams->team2[h.pstate.pid.pid.num] = p;
  } else {
    fprintf(stderr, "You are on team RED.\n");
    p->team = 1;
    p->teamIndex = h.pstate.pid.pid.num;
    C->GS.Teams->team1[h.pstate.pid.pid.num] = p;
  }
  p->pid = h.pstate.pid.raw;
  p->posx = h.pstate.pos.pos.x;
  p->posy = h.pstate.pos.pos.y;
  
  fprintf(stderr, "Your number is %i\n.", p->teamIndex);
  player_dump(p);

  maze_player_place(C->GS.m, p, (sval)p->posx, (sval)p->posy);
  Maze_Cell *c = maze_cell(C->GS.m, (sval)p->posx, (sval)p->posy);
  maze_dump_cell(c);

  fprintf(stderr, "\n\nUpdating UI...?");
  ui_update(C->ui);

  C->GS.inGame = 1;

  game_state_end_transaction(&(C->GS));

  return 1;
}

int
game_process_move(Client *C)
{
  NYI();
  return 1;
}


int 
doRPCCmd(Client *C, char c) 
{
  int rc=-1;

  switch (c) {
  case 'h':  
    rc = game_hello(C);
    break;
  case 'm':
    scanf("%c", &c);
    rc = proto_client_move(C->GS.ph, c);
    break;
  case 'g':
    rc = proto_client_goodbye(C->GS.ph);
    break;
  default:
    printf("%s: unknown command %c\n", __func__, c);
  }
  // NULL MT OVERRIDE ;-)
  printf("%s: rc=0x%x\n", __func__, rc);
  if (rc == 0xdeadbeef) rc=1;
  return rc;
}

int
doRPC(Client *C)
{
  int rc;
  char c;

  printf("enter (h|m<c>|g): ");
  scanf("%c", &c);
  rc=doRPCCmd(C,c);

  printf("doRPC: rc=0x%x\n", rc);

  return rc;
}


int 
docmd(Client *C, char cmd)
{
  int rc = 1;

  switch (cmd) {
  case 'd':
    proto_debug_on();
    break;
  case 'D':
    proto_debug_off();
    break;
  case 'r':
    rc = doRPC(C);
    break;
  case 'q':
    rc=-1;
    break;
  case '\n':
    rc=1;
    break;
  default:
    printf("Unkown Command\n");
  }
  return rc;
}

void *
shell(void *arg)
{
  Client *C = arg;
  char c;
  int rc;
  int menu=1;

  while (1) {
    if ((c=prompt(menu))!=0) rc=docmd(C, c);
    if (rc<0) break;
    if (rc==1) menu=1; else menu=0;
  }

  fprintf(stderr, "terminating\n");
  fflush(stdout);
  return NULL;
}

void 
usage(char *pgm)
{
  fprintf(stderr, "USAGE: %s <port|<<host port> [shell] [gui]>>\n"
           "  port     : rpc port of a game server if this is only argument\n"
           "             specified then host will default to localhost and\n"
	   "             only the graphical user interface will be started\n"
           "  host port: if both host and port are specifed then the game\n"
	   "examples:\n" 
           " %s 12345 : starts client connecting to localhost:12345\n"
	  " %s localhost 12345 : starts client connecting to locaalhost:12345\n",
	   pgm, pgm, pgm, pgm);
 
}

void
initGlobals(int argc, char **argv)
{
  bzero(&globals, sizeof(globals));

  pthread_mutex_init(&(globals.lock), NULL);

  globals.ui = UI_GUI;

  if (argc==1) {
    usage(argv[0]);
    exit(-1);
  }

  if (argc==2) {
    strncpy(globals.host, "localhost", STRLEN);
    globals.port = atoi(argv[1]);
  }

  if (argc>=3) {
    strncpy(globals.host, argv[1], STRLEN);
    globals.port = atoi(argv[2]);
  }

  if ((argc>=4 && atoi(argv[3])==1)) {
    // if you specify the shell ui then 
    // by default make it the only gui
    globals.ui = UI_SHELL;
  }

  if (argc>=5 && atoi(argv[4])==1) {
    // if you want to force shell and gui 
    // ui to come up at startup then you can
    // specify one more arg
    globals.ui |= UI_GUI;
  }

}

int 
main(int argc, char **argv)
{
  Client *c;
  pthread_t tid;
  int start_gui;

  initGlobals(argc, argv);

  if (createClient(&c) < 0) {
    fprintf(stderr, "ERROR: could not create client\n");
    return -1;
  }    

  fprintf(stderr,
	  "kludge: loading map from file rather than it having come from server\n");
  if (maze_load_file("../lib/daGame.map", c->GS.m) <= 0) {
    fprintf(stderr, "ERROR: could not load map\n");
    return -1;
  }

  // Initialize the gui subsystem so that we can start it up in the future
  // for this client:
  // no mater what get us ready to be able to start the UI
  // for the main client 

  ui_init(&(c->ui));
  Proto_Client_Handle ph;
  c->ui->extra = c;

  fprintf(stderr, "About to start connection to server\n");
  // ok startup our connection to the server
  if (startConnection(c, globals.host, globals.port, update_event_handler)<0) {
    fprintf(stderr, "ERROR: startConnection failed\n");
    return -1;
  }
  fprintf(stderr, "Started connection with server\n");
  fprintf(stderr, "Welcome to our game!\n");
  fprintf(stderr, "HOW TO PLAY: \n");
  fprintf(stderr, "When you are ready to play, press j to join the game.\n");
  fprintf(stderr, "Use the arrow keys to move.\n");
  fprintf(stderr, "pick up an object with p and drop it with d. \n"); 
  fprintf(stderr, "to zoom in, press z. To zoom out, press shift+z\n");
  fprintf(stderr, "to pan, use shift+arrow keys\n");
  fprintf(stderr, "HOW TO WIN: \n");
  fprintf(stderr, "To win, capture the opponents flag and bring it to your base.\n");
  fprintf(stderr, "Also, have all of your opponents in jail.\n");

  fprintf(stderr, "Enjoy and play fair!\n");
  //need to use ui to get the shell started
  //we can kludge it until then
  if (globals.ui | UI_SHELL) pthread_create(&tid, NULL, shell, c);

  // now determine if we should actually starting the gui
  // at startup
  start_gui = (globals.ui & UI_GUI) ? 1 : 0;

  // WITH OSX ITS IS EASIEST TO KEEP UI ON MAIN THREAD
  // SO JUMP THROW HOOPS :-(
  // You probably will need to replace the NULL reference
  ui_main_loop(c->ui, 800, 800, c->GS.m, NULL, start_gui);

  shell(&c);

  return 0;
}

extern sval
ui_keypress(UI *ui, Maze *m, Players *Teams, SDL_KeyboardEvent *e)
{
  Client *C = (Client *)ui->extra;
  SDLKey sym = e->keysym.sym;
  SDLMod mod = e->keysym.mod;

  if (e->type == SDL_KEYDOWN) {
    if (sym == SDLK_LEFT && mod == KMOD_NONE) 
      return game_move(C, 4);
    if (sym == SDLK_RIGHT && mod == KMOD_NONE) 
      return game_move(C, 2);
    if (sym == SDLK_UP && mod == KMOD_NONE)    
      return game_move(C, 1);
    if (sym == SDLK_DOWN && mod == KMOD_NONE)  
      return game_move(C, 3);
    if (sym == SDLK_p && mod == KMOD_NONE) //pickup 
      return NYI();
    if (sym == SDLK_d && mod == KMOD_NONE)     
      return NYI();
    if (sym == SDLK_j && mod == KMOD_NONE) return game_hello(C); //HELLO HANDLER FOR "j"
    if (sym == SDLK_z && mod == KMOD_NONE) return ui_zoom(ui, m, 1);
    if (sym == SDLK_z && mod & KMOD_SHIFT ) return ui_zoom(ui, m, -1);
    if (sym == SDLK_LEFT && mod & KMOD_SHIFT) return ui_pan(ui, m, -1,0);
    if (sym == SDLK_RIGHT && mod & KMOD_SHIFT) return ui_pan(ui, m, 1,0);
    if (sym == SDLK_UP && mod & KMOD_SHIFT) return ui_pan(ui, m, 0,-1);
    if (sym == SDLK_DOWN && mod & KMOD_SHIFT) return ui_pan(ui, m, 0,1);
   if (sym == SDLK_q) return -1;
    else {
      //      fprintf(stderr, "key pressed: %d\n", sym); 
    }
  } else {
    //    fprintf(stderr, "key released: %d\n", sym);
  }
  return 1;
}


