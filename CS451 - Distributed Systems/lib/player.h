#ifndef __DA_GAME_PLAYER__
#define __DA_GAME_PLAYER__
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

#include <stdlib.h>
#include <pthread.h>

#include "types.h"
#include "objects.h"

#define PLAYER_TEAM_A(p) (p->team == 1)

struct Player_Struct {
  uval extra;
  int team;
  int pid;  //event fd
  int state;
  int teamIndex;
  int posx;
  int posy;
  Object *o;
};

struct Players_Struct {
  //teams are linked lists of players
  //as sucky as arrays can be, it might be easier to organize teams as arrays instead
  Player *team1[100];
  Player *team2[100];
  int nextTeam;
  pthread_mutex_t    teamsLock;  
};

void player_dump(Player *p);
int players_init(Players **pp);
int player_init(Player **p);
Player * player_add(Players *pp);
int player_remove(Player *p, Maze *m, Players *pp);
uval player_ui_state(Player *p);



#endif

