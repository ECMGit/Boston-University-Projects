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

#include "protocol.h"
#include "protocol_utils.h"


int PROTO_DEBUG=0;

extern void
proto_dump_mt(Proto_Msg_Types type)
{
  switch (type) {
  case PROTO_MT_REQ_BASE_RESERVED_FIRST: 
    fprintf(stderr, "PROTO_MT_REQ_BASE_RESERVED_FIRST");
    break;
  case PROTO_MT_REQ_BASE_HELLO: 
    fprintf(stderr, "PROTO_MT_REQ_BASE_HELLO");
    break;
  case PROTO_MT_REQ_BASE_MOVE: 
    fprintf(stderr, "PROTO_MT_REQ_BASE_MOVE");
    break;
  case PROTO_MT_REQ_BASE_PICKUP:
    fprintf(stderr, "PROTO_MT_REQ_BASE_PICKUP");
    break;
  case PROTO_MT_REQ_BASE_DROP:
    fprintf(stderr, "PROTO_MT_REQ_BASE_DROP");
    break;
  case PROTO_MT_REQ_BASE_GOODBYE: 
    fprintf(stderr, "PROTO_MT_REQ_BASE_GOODBYE");
    break;
  case PROTO_MT_REQ_BASE_RESERVED_LAST: 
    fprintf(stderr, "PROTO_MT_REQ_BASE_RESERVED_LAST");
    break;
  case PROTO_MT_REP_BASE_RESERVED_FIRST: 
    fprintf(stderr, "PROTO_MT_REP_BASE_RESERVED_FIRST");
    break;
  case PROTO_MT_REP_BASE_HELLO: 
    fprintf(stderr, "PROTO_MT_REP_BASE_HELLO");
    break;
  case PROTO_MT_REP_BASE_MOVE:
    fprintf(stderr, "PROTO_MT_REP_BASE_MOVE");
    break;
  case PROTO_MT_REP_BASE_PICKUP:
    fprintf(stderr, "PROTO_MT_REP_BASE_PICKUP");
    break;
  case PROTO_MT_REP_BASE_DROP:
    fprintf(stderr, "PROTO_MT_REP_BASE_DROP");
    break;
  case PROTO_MT_REP_BASE_GOODBYE:
    fprintf(stderr, "PROTO_MT_REP_BASE_GOODBYE");
    break;
  case PROTO_MT_REP_BASE_RESERVED_LAST: 
    fprintf(stderr, "PROTO_MT_REP_BASE_RESERVED_LAST");
    break;
  case PROTO_MT_EVENT_BASE_RESERVED_FIRST: 
    fprintf(stderr, "PROTO_MT_EVENT_BASE_RESERVED_FIRST");
    break;
  case PROTO_MT_EVENT_BASE_UPDATE: 
    fprintf(stderr, "PROTO_MT_EVENT_BASE_UPDATE");
    break;
  case PROTO_MT_EVENT_BASE_RESERVED_LAST: 
    fprintf(stderr, "PROTO_MT_EVENT_BASE_RESERVED_LAST");
    break;
  default:
    fprintf(stderr, "UNKNOWN=%d", type);
  }
}
 
extern void
proto_dump_pstate(Proto_Player_State *ps)
{
  int pid, team, posx, posy, state;
  
  pid = ntohl(ps->pid.raw);
  team = ntohl(ps->pid.pid.team);
  posx = ntohl(ps->pos.pos.x);
  posy = ntohl(ps->pos.pos.y);
  state = ntohl(ps->state.inGame);

  fprintf(stderr, "pid=0x%x team=0x%x posx=0x%x posy=0x%x state=0x%x\n",
	  pid, team, posx, posy, state);
}

extern void
proto_dump_gstate(Proto_Game_State *gs)
{
  int diff, state, extra;

  diff = ntohl(gs->diff.raw);
  state = ntohl(gs->state.raw);
  extra = ntohl(gs->extra);

  fprintf(stderr, "diff=0x%x state=0x%x extra=0x%x\n",
	  diff, state, extra);
}

extern void
proto_dump_msghdr(Proto_Msg_Hdr *hdr)
{
  fprintf(stderr, "ver=%d type=", ntohl(hdr->version));
  proto_dump_mt(ntohl(hdr->type));
  fprintf(stderr, " sver=%llx", ntohll(hdr->sver.raw));
  fprintf(stderr, " pstate:");
  proto_dump_pstate(&(hdr->pstate));
  fprintf(stderr, " gstate:"); 
  proto_dump_gstate(&(hdr->gstate));
  fprintf(stderr, " blen=%d\n", ntohl(hdr->blen));
}
