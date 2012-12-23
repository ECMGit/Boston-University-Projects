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
#include <strings.h>
#include <sys/types.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <assert.h>
#include "types.h"
#include "objects.h"
#include "maze.h"
#include "player.h"


struct Counts {
  sval cellcnt, ucnt, fcnt, wcnt, ahcnt, bhcnt, ajcnt, bjcnt;
};

static sval
maze_init_cell(Maze *mp, Maze_Cell *cell, char c, struct Counts *cnts, 
	       sval x, sval y, uval team, uval update)
{
  uval oldstate;

  if (!update) {
    cell->pos.x = x; cell->pos.y = y; 
    cell->state = (team==0) ? MAZE_CELL_A : 0; 
  } else {
    oldstate = cell->state;
  }

  switch (c) {
  case '#': 
    cnts->wcnt++;
    cell->state |= MAZE_CELL_NONE;
    cnts->cellcnt++;
    break;
  case 'h':
    cell->state |= MAZE_CELL_FLOOR|MAZE_CELL_A_HOME;
    if (cnts->ahcnt < MAZE_MAX_HOME_CELLS) {
      mp->ahome[cnts->ahcnt] = cell;
      cnts->ahcnt++;
    } else {
      fprintf(stderr, "ERROR: mapfile defines more team A home cells than"
	      "supported (%d)\n", MAZE_MAX_HOME_CELLS);
    }
    cnts->fcnt++;
    cnts->cellcnt++;
    break;
  case 'H':
    cell->state |= MAZE_CELL_FLOOR|MAZE_CELL_B_HOME;
    if (cnts->bhcnt < MAZE_MAX_HOME_CELLS) {
      mp->bhome[cnts->bhcnt] = cell;
      cnts->bhcnt++;
    } else {
      fprintf(stderr, "ERROR: mapfile defines more team B home cells than"
	      "supported (%d)\n", MAZE_MAX_HOME_CELLS);
    }
    cnts->fcnt++;
    cnts->cellcnt++;
    break;
  case 'j':
    cell->state |= MAZE_CELL_FLOOR|MAZE_CELL_A_JAIL;
    if (cnts->ajcnt < MAZE_MAX_JAIL_CELLS) {
      mp->ajail[cnts->ajcnt] = cell;
      cnts->ajcnt++;
    } else {
      fprintf(stderr, "ERROR: mapfile defines more team A jail cells than"
	      "supported (%d)\n", MAZE_MAX_JAIL_CELLS);
    }
    cnts->fcnt++;
    cnts->cellcnt++;
    break;
  case 'J':
    cell->state |= MAZE_CELL_FLOOR|MAZE_CELL_B_JAIL;
    if (cnts->bjcnt < MAZE_MAX_JAIL_CELLS) {
      mp->bjail[cnts->bjcnt] = cell;
      cnts->bjcnt++;
    } else {
      fprintf(stderr, "ERROR: mapfile defines more team B jail cells than"
	      "supported (%d)\n", MAZE_MAX_JAIL_CELLS);
    }
    cnts->fcnt++;
    cnts->cellcnt++;
    break;
  case ' ':
    cnts->fcnt++;
    cell->state |= MAZE_CELL_FLOOR;
    cnts->cellcnt++;
    break;
  default:
    cnts->ucnt++;
  }
}

extern sval
maze_init(Maze **m)
{  
  *m = (Maze *)malloc(sizeof(Maze));
  if (*m == NULL) return -1;
  bzero(*m, sizeof(Maze));  
  return 1;
}

static sval
maze_cells_alloc(sval w, sval h, Maze_Cell **c)
{
  *c = (Maze_Cell *)malloc(h * w * sizeof(Maze_Cell));
  if (*c == NULL) return 0;
  bzero(*c, h*w*sizeof(Maze_Cell));
  return 1;
}

extern sval
maze_load_bytes(char *str, uval len, sval tlx, sval tly, sval brx, sval bry, 
		Maze *m)
{
  sval x,y,i;
  uval update, team;
  Maze_Cell *c;
  sval mid;
  struct Counts cnts;
  
  if (!m) return -1;
  
  if (m->cells) {
    update = 1; 
    // if this is an update then make sure bbox is with current dimensions
    if (tlx < 0 || brx >= m->width) return -1;
    if (tly < 0 || bry >= m->height) return -1;
  } else {
    update = 0;
    m->width  = brx - tlx;
    m->height = bry - tly;
    maze_cells_alloc(m->width, m->height, &m->cells);
  }

  bzero(&cnts, sizeof(cnts));

  if (!m->cells) return -1;

  mid = m->width / 2;
  y=tly; x=tlx;

  for (i=0; i<len; i++) {
    if (str[i]!='\n') {
      c=maze_cell(m, x, y);
      team = (x < mid) ? 0 : 1;
      maze_init_cell(m, c, str[i], &cnts, x, y, team, update);
      x++;
      if (x==brx) { y++; x=tlx; }
    }
  }
  
  m->num_a_home = cnts.ahcnt; m->num_b_home = cnts.bhcnt;
  m->num_a_jail = cnts.ajcnt; m->num_b_jail = cnts.bjcnt;

  fprintf(stderr, "h*w=%ld cells=%ld wcnt=%ld fcnt=%ld ucnt=%ld\n"
	  "ahome=%ld, bhome=%ld, ajail=%ld bjail=%ld\n",
	  m->height*m->width, cnts.cellcnt, cnts.wcnt, cnts.fcnt, cnts.ucnt, 
	  m->num_a_home, m->num_b_home, m->num_a_jail, m->num_b_jail);

  return 1;
}

static sval 
maze_calc_dim(char *str, uval len, sval *w, sval *h)
{
  sval cols, rows;
  uval i;

  *w=0;
  *h=0;

  if (len==0) return 1;

  cols=0; rows=0;
  for (i=0; i<len; i++) {
    if (str[i]=='\n') {
      if (*w) { 
	if (*w != cols) goto error;
      } else *w = cols;
      rows++; cols=0;
    } else cols++;
  }
  if (str[len-1]!='\n' && cols!=*w) goto error;
  *h = rows;
  return 1;
 error:
  fprintf(stderr, "ERROR: maze_calc_dim: row=%ld: *w=%ld != %ld=cols\n", 
	  rows, *w, cols);
  return -1;
}

extern sval
maze_load_file(char *file, Maze *m)
{
  int fd;
  uval len;
  sval w, h;
  struct stat finfo;
  char *str;

  if (!m) return 0;

  fd = open(file, O_RDONLY);
  if (fd==-1) {
    perror("maze_load_file: open");
    return 0;
  }

  if (fstat(fd, &finfo)==-1) {
    perror("maze_load_file: fstat");
    return 0;
  }
  len = finfo.st_size;

  str = mmap(0, len, PROT_READ, MAP_FILE|MAP_PRIVATE, fd, 0);

  if (str == MAP_FAILED) {
    perror("ERROR: maze_load_file:");
    return 0;
  }

  if (maze_calc_dim(str, len, &w, &h)<0) {
    fprintf(stderr, "ERROR: maze_load_file: maze_calc_dim: failed\n");
    return 0;
  }

  fprintf(stderr, "maze_load_file: w=%ld h=%ld\n", w, h);

  return maze_load_bytes(str, len, 0, 0, w, h, m);
}

extern sval 
maze_dump_cell(Maze_Cell *c)
{
  if (c == NULL) { fprintf(stderr, "NULL\n"); return 1; }

  fprintf(stderr, "c.pos.x=%ld c.pos.y=%ld c.state=0x%lx c.player=0x%p"
	  " c.object=0x%p\n",
	  c->pos.x, c->pos.y, c->state, c->player, c->object);
  return 1;
}

static void
maze_player_remove_from_cell(Player *p, Maze_Cell *c)
{ 
    // remove player
    c->player = NULL;
    MAZE_CELL_CLR_OCCUPIED(c);

    // update player - not sure what to do here
    //msg from other tim: I think it won't matter what we do here, setting to -1 is fine. I don't honestly think we need to do anything at all thoguh
    //if this is ever called, the player had to have been dropped, a transfer would be used for people going to jail/getting otu
    //and move would be used for people moving
    p->posx = -1;
    p->posy = -1;

}

static void
maze_player_put_in_cell(Player *p, Maze_Cell *c)
{  
  // Get the object, if any, that is in the cell
  Object *o = maze_cell_get_object(c);

  // update player
  p->posx = c->pos.x;
  p->posy = c->pos.y;

  // update maze cell
  c->player = p;
  MAZE_CELL_SET_OCCUPIED(c);

 /* if(o){
    o->owner = p;
    //remove object from board
    c->object=NULL;
  } */ //in our implementation its not automatic, picking up an item needs to be called
  
}

static void
maze_player_transfer(Player *p, Maze_Cell *s, Maze_Cell *d)
{
  //NYI();

  maze_player_put_in_cell(p, d);	// Move player
  
  if( MAZE_CELL_HAS_OBJECT(s) ){	// If there is an object in the source
    if( s->object->owner = p ){		// And if the player is holding that object, move it to destination
					// This allows a player to drop an object, and move away from it
	// Update object position	
	s->object->posx = d->pos.x;
	s->object->posy = d->pos.y;

	// Update cells
	s->object = 0;
	d->object = s->object;
    } 
  }

  maze_player_remove_from_cell(p, s);
}

extern sval
maze_player_move(Maze *m, Player *p, sval dx, sval dy)
{
  //NYI();
  // Get players current position
  sval curx = p->posx;
  sval cury = p->posy;

  // Get source and destination cells
  Maze_Cell *s = maze_cell(m, curx, cury);
  Maze_Cell *d = maze_cell(m, curx+dx, cury+dy);

  //Check if move is legal
  if(!MAZE_CELL_IS_OCCUPIED(d)){
	// legal moves are on floor cells or home cells belonging to the players team 
	if(MAZE_CELL_IS_FLOOR(d) || (MAZE_CELL_IS_A_HOME(d) == (p->team % 2))){

		// transfer player from cell 's' to cell 'd'
		maze_player_transfer(p, s, d);
		return 1;
	}
  }

  // return -1 if not a legal move?
  return -1;
}

extern sval 
maze_player_place(Maze *m, Player *p, sval x, sval y)
{
  // NYI();

  Maze_Cell *c = maze_cell(m, x, y);
  if(c){
    maze_player_put_in_cell(p, c);
    return 1;
  }
  
  return -1;  
}

extern sval
maze_player_add(Maze *m, Player *p)
{
  int i;
  // Adds player to game

  // If player is on team 1 (A)
  if(p->team == 1){
	// Find position of first home cell
		
	for(i = 0; i< MAZE_MAX_HOME_CELLS; i++){
	  if( !MAZE_CELL_IS_OCCUPIED(m->ahome[i]) ){
		sval x = m->ahome[i]->pos.x;
		sval y = m->ahome[i]->pos.y;
		maze_player_place(m, p, x, y);
		return 1;
	  }
	}
  }
  // if player is on team 2 (B)
  if(p->team == 2){
	// Find empty home cell that belongs to team B
	for(i = 0; i< MAZE_MAX_HOME_CELLS; i++){
	  if( !MAZE_CELL_IS_OCCUPIED(m->bhome[i]) ){
		sval x = m->bhome[i]->pos.x;
		sval y = m->bhome[i]->pos.y;
		maze_player_place(m, p, x, y);
		return 1;
	  }
	}
  }

  return -1;
}

static sval
maze_jackhammer_add(Maze *m, Object *o, uval team)
{ 
  //adds shovel/jackhammer thing, implement later
  NYI();
  return 1;
}

static sval
maze_flag_add(Maze *m, Object *o, uval team)
{
  NYI();
  //places flag randomly

  if(team == 1){
    while(1){	// Possible danger?
      //choose x placement of flag 1 
      srand(time(NULL));
      int x1 = rand() % 50;
      //chose y placement 
      int y1= random() % 200;

      //check if this is a legal place
      Maze_Cell *c1 = maze_cell(m, x1, y1);
      if( MAZE_CELL_IS_FLOOR(c1) && !MAZE_CELL_IS_OCCUPIED(c1) && !MAZE_CELL_HAS_OBJECT(c1)){
	// Place the Red Flag
	c1-> object = o;
	o-> posx = x1;
	o-> posy = y1;
	break;
      }
    }
  }

  if(team == 2){
    while(1){ // Possible danger?
      //choose x placement of flag 2
      int x2 = (rand() % 50)+50;
      //chose y placement 
      int y2= random() % 200;
      //check if this is a legal place
      Maze_Cell *c2 = maze_cell(m, x2, y2);
      if ( MAZE_CELL_IS_FLOOR(c2) && !MAZE_CELL_IS_OCCUPIED(c2) && !MAZE_CELL_HAS_OBJECT(c2)){
  	// Place the Red Flag
	c2-> object = o;
	o-> posx = x2;
	o-> posy = y2;
	break;
      }
    }
  }

  return 1;
}

sval
maze_object_add(Maze *m, Object *o, uval team)
{
  //NYI();
}

extern sval
maze_dump(Maze *m, sval tlx, sval tly, sval brx, sval bry, char *buf)
{
  sval i=0;
  sval x, y;
  sval acnt=0, bcnt=0;
  Maze_Cell *cell;
  
  if (m->width == 0 || m->height == 0) return 1;
  
  for (y=tly; y<bry; y++) {
    //    printf("%02ld: ",y);
    for (x=tlx; x<brx; x++) {
      cell = maze_cell(m, x, y);
      if (MAZE_CELL_IS_TEAM_A(cell)) acnt++; else bcnt++;
      if (MAZE_CELL_IS_FLOOR(cell)) {
	if      (MAZE_CELL_IS_A_HOME(cell)) if (buf) buf[i]='h'; 
	                                    else printf("h");
	else if (MAZE_CELL_IS_B_HOME(cell)) if (buf) buf[i]='H'; 
                                            else printf("H");
	else if (MAZE_CELL_IS_A_JAIL(cell)) if (buf) buf[i]='j'; 
                                            else printf("j");
	else if (MAZE_CELL_IS_B_JAIL(cell)) if (buf) buf[i]='J'; 
                                            else printf("J");
	else if (MAZE_CELL_IS_TEAM_A(cell)) if (buf) buf[i]=' '; 
                                            else printf(" ");
	else if (MAZE_CELL_IS_TEAM_B(cell)) if (buf) buf[i]=' '; 
                                            else printf(" ");
      } else  {
	if      (MAZE_CELL_IS_TEAM_A(cell)) if (buf) buf[i]='#'; 
                                            else printf("#");
	else if (MAZE_CELL_IS_TEAM_B(cell)) if (buf) buf[i]='#'; 
                                            else printf("#");
      }
      i++;
    }
    if (!buf) printf("\n");
  }
  if (!buf) {
    fprintf(stderr, "m=%p: width=%ld height=%ld num_a_home=%ld num_b_home=%ld "
	    "num_a_jail=%ld num_b_jail=%ld cells:\n", m, 
	    m->width, m->height, m->num_a_home, m->num_b_home, m->num_a_jail,
	    m->num_b_jail);
    
    fprintf(stderr, "Team A cells: acnt=%ld, Team B cells: bcnt=%ld\n",
	    acnt, bcnt);
  }
  return 1;
}

void
maze_to_ascii(Maze *m, char *buf)
{
  int h = m->width;
  int w = m->height;

  int i,x,y;
  for (x=0; x<h; x++) {
    for (y=0; y<w; y++) {
      
      Maze_Cell *cell = maze_cell(m, x, y);

	if (MAZE_CELL_IS_FLOOR(cell)) {
	  if (!MAZE_CELL_IS_OCCUPIED(cell)) {
		if (MAZE_CELL_IS_A_HOME(cell)){ buf[i]='h'; }                       
		else if (MAZE_CELL_IS_B_HOME(cell)){ buf[i]='H'; }
		else if (MAZE_CELL_IS_A_JAIL(cell)){buf[i]='j'; }
		else if (MAZE_CELL_IS_B_JAIL(cell)){ buf[i]='J'; }
		else if (MAZE_CELL_IS_TEAM_A(cell)){ buf[i]=' '; }
		else if (MAZE_CELL_IS_TEAM_B(cell)){ buf[i]=' '; }
	  } else {
		if(cell->player->team == 1){ buf[i]='p'; }
		if(cell->player->team == 2){ buf[i]='P'; }
	  }
      } else  {
	if (MAZE_CELL_IS_TEAM_A(cell)){ buf[i]='#'; }
	else if (MAZE_CELL_IS_TEAM_B(cell)){ buf[i]='#'; }
      }
      i++;
    }
  }
	buf[i] = '\0';	

  fprintf(stderr, "buf = %s", buf);

}

//int NYI(void) { fprintf(stderr, "NYI\n"); return 1; }

