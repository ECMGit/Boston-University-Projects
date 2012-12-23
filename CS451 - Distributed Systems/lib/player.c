

#include <stdio.h>
#include <stdlib.h>
#include <strings.h>
#include <pthread.h>

#include "types.h"
#include "player.h"
#include "objects.h"
#include "maze.h"




extern void 
player_dump(Player *p)
{
 fprintf(stderr, "team=%d: pid=%d posx=%d posy=%d state=%d\n", p->team, p->pid, p->posx, p->posy, p->state);
}


extern int
players_init (Players **pp)
{
  int i;
  Players *cur;

  *pp = (Players *)malloc(sizeof(Players));
  if (*pp == NULL) return -1;
  bzero(*pp, sizeof(Players));
  cur = *pp;

  //initialize when server/client starts
  cur->nextTeam = 1;

  for (i=0; i<100; i++) {
    cur->team1[i]=NULL;
    cur->team2[i]=NULL;
  }  
 
  pthread_mutex_init(&cur->teamsLock, 0);

  return 1;
  
}

extern int
player_init (Player **p)
{
  *p = (Player *)malloc(sizeof(Player));
  if (*p == NULL) return -1;
  bzero(*p, sizeof(Player)); 
  return 1;
}

extern Player *
player_add(Players *pp)
{
  //check which team to add player to
  //create player pointer in said team
  //pass said pointer to player_init
  int i;
  Player *p;
  if (player_init(&p) < 0)
    return NULL;

  pthread_mutex_lock(&pp->teamsLock);

  
  //adjust so that it checks for team at maximum and for empty spaces
  if (pp->nextTeam % 2 == 1) {
    p->team = 1;
    for (i=0; i<100; i++) {
      if (pp->team1[i]==NULL) {
	pp->team1[i]=p;
        p->teamIndex = i;
	p->pid = i;
        break;
      }
    }
  } else {
    fprintf(stderr, "Does team get assigned correctly?\n");
    p->team = 2;
    for (i=0; i<100; i++) {
      if (pp->team2[i]==NULL) {
	pp->team2[i]=p;
        p->teamIndex = i;
	p->pid = i + 100;
        break;
      }
    }
  }
  p->state = 1;
  if (pp->nextTeam == 1){ pp->nextTeam = 2; }
  else { pp->nextTeam = 1; }

  fprintf(stderr, "Next Team = %i\n", pp->nextTeam);
  
  
  pthread_mutex_unlock(&pp->teamsLock);
  return p;
}

extern int
player_remove(Player *p, Maze *m, Players *pp)
{
  //NYI();
  // remove from team array
  int i;

  p->state = 0;

  if(p->team == 1){
    for (i=0; i<100; i++) {
      if (pp->team1[i]== p) { pp->team1[i] = NULL;}
    }
  }
  if(p->team == 2){
    for (i=0; i<100; i++) {
      if (pp->team2[i]== p) { pp->team2[i] = NULL;}
    }
  }
  
  /* Not sure if we need anything else in this funciton, but lets just be safe.*/

  // remove player from maze
  Maze_Cell *c = maze_cell(m, p->posx, p->posy);
  c-> player = NULL;

  // set player position to a location not on the map
  p->posx = -1;
  p->posy = -1;
 

  return 1;
}

extern uval
player_ui_state(Player *p)
{
  if (p->o != NULL) 
	return p->o->type;
  else
	return -1;

}


