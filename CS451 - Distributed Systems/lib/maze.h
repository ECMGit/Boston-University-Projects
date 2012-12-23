#ifndef __DA_GAME_MAZE_H__
#define __DA_GAME_MAZE_H__
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

//nikka added
#include "mazetypes.h"
#include "types.h"

#define MAZE_MAX_HOME_CELLS 4096
#define MAZE_MAX_JAIL_CELLS 4096

/* defines meaing of cell state bits */
#define MAZE_CELL_NONE     0x00
#define MAZE_CELL_A        0x02
#define MAZE_CELL_B_JAIL   0x04
#define MAZE_CELL_A_JAIL   0x08
#define MAZE_CELL_B_HOME   0x10
#define MAZE_CELL_A_HOME   0x20
#define MAZE_CELL_OCCUPIED 0x40
#define MAZE_CELL_FLOOR    0x80

struct Maze_Pos_Struct {
  sval x;
  sval y;
};

struct Maze_Cell_Struct {
  Maze_Pos   pos;
  uval       state;
  Player    *player;
  Object    *object;
};

struct Maze_Struct {
  sval       width;
  sval       height;
  sval       num_a_home;
  sval       num_b_home;
  sval       num_a_jail;
  sval       num_b_jail;
  Maze_Cell  *cells;
  Maze_Cell  *ahome[MAZE_MAX_HOME_CELLS];
  Maze_Cell  *bhome[MAZE_MAX_HOME_CELLS];
  Maze_Cell  *ajail[MAZE_MAX_JAIL_CELLS];
  Maze_Cell  *bjail[MAZE_MAX_JAIL_CELLS];
};

static inline 
Maze_Cell * maze_row(Maze *m, sval y)
{
  return m->cells + (y * m->width);
}

static inline 
Maze_Cell * maze_cell(Maze *m, sval x, sval y)
{
  return  (maze_row(m,y)) + x;
}

#define MAZE_CELL_IS_A_HOME(c) (c->state & MAZE_CELL_A_HOME)
#define MAZE_CELL_IS_B_HOME(c) (c->state & MAZE_CELL_B_HOME)
#define MAZE_CELL_IS_A_JAIL(c) (c->state & MAZE_CELL_A_JAIL)
#define MAZE_CELL_IS_B_JAIL(c) (c->state & MAZE_CELL_B_JAIL)
#define MAZE_CELL_SET_TEAM_A(c) (c->state |= MAZE_CELL_A)
#define MAZE_CELL_IS_TEAM_A(c) (c->state & MAZE_CELL_A)
#define MAZE_CELL_SET_TEAM_B(c) (c->state |= ~MAZE_CELL_A)
#define MAZE_CELL_IS_TEAM_B(c) (!MAZE_CELL_IS_TEAM_A(c))
#define MAZE_CELL_SET_FLOOR(c)  (c->state |= MAZE_CELL_FLOOR)
#define MAZE_CELL_IS_FLOOR(c)   (c->state & MAZE_CELL_FLOOR)
#define MAZE_CELL_SET_OCCUPIED(c) (c->state |= MAZE_CELL_OCCUPIED)
#define MAZE_CELL_CLR_OCCUPIED(c) (c->state &= ~MAZE_CELL_OCCUPIED)
#define MAZE_CELL_IS_OCCUPIED(c)  (c->state & MAZE_CELL_OCCUPIED)
#define MAZE_CELL_HAS_OBJECT(c)    (c->object != 0)
#define MAZE_CELL_SET_OBJECT(c, o) (c->object = o)

#define MAZE_IS_TEAM_A(m,x,y) (MAZE_CELL_IS_TEAM_A(maze_cell(m,x,y)))
#define MAZE_SET_FLOOR(m,x,y)  (MAZE_CELL_SET_FLOOR(maze_cell(m,x,y)))
#define MAZE_IS_FLOOR(m,x,y)   (MAZE_CELL_IS_FLOOR(maze_cell(m,x,y)))
#define MAZE_IS_EDGE(m,x,y) ( x==0 || x==(m->width-1) \
			      || y==0 || y==(m->height-1) )

sval maze_init(Maze **m);
sval maze_load_bytes(char *str, uval len, sval tlx, sval tly, sval brx, sval bry, Maze *m);
sval maze_load_file(char *file, Maze *m);
sval maze_dump_cell(Maze_Cell *cell);
sval maze_dump(Maze *maze, sval tlx, sval tly, sval brx, sval bry, char *buf);  

sval maze_player_add(Maze *m, Player *p);
sval maze_player_remove(Maze *m, Player *p);
sval maze_player_move(Maze *m, Player *p, sval dx, sval dy);
sval maze_player_place(Maze *m, Player *p, sval x, sval y);
sval maze_object_add(Maze *m, Object *o, uval team);

static inline sval
maze_cell_set_object(Maze_Cell *c, Object *o)
{
  if (o==NULL) return 1;
  if (c==NULL || MAZE_CELL_HAS_OBJECT(c)) return -1;
  MAZE_CELL_SET_OBJECT(c,o);
  return 1;
}

static inline 
Object * maze_cell_clear_object(Maze_Cell *c)
{
  Object *o = NULL;
  if (c) {
    o = c->object;
    c->object = NULL;
  }
  return o;
}

static inline
Object * maze_cell_get_object(Maze_Cell *c)
{
  return ((c) ? c->object : NULL);
}
static inline 
Player * maze_cell_get_player(Maze_Cell *c)
{
  return ((c) ? c->player : NULL);
}

#endif
