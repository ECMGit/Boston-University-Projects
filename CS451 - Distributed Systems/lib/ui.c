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
#include <stdlib.h> /* for exit() */
#include <assert.h>
#include "types.h"
#include "objects.h"
#include "maze.h"
#include "player.h"
#include "ui.h"

#include <SDL/SDL.h>

/* A lot of this code comes from http://www.libsdl.org/cgi/docwiki.cgi */

#define SPRITE_H 32
#define SPRITE_W 32

#define UI_FLOOR_BMP "floor.bmp"
#define UI_REDWALL_BMP "redwall.bmp"
#define UI_GREENWALL_BMP "greenwall.bmp"
#define UI_TEAMA_BMP "teama.bmp"
#define UI_TEAMB_BMP "teamb.bmp"
#define UI_LOGO_BMP "logo.bmp"
#define UI_REDFLAG_BMP "redflag.bmp"
#define UI_GREENFLAG_BMP "greenflag.bmp"
#define UI_JACKHAMMER_BMP "shovel.bmp"

typedef enum {UI_SDLEVENT_UPDATE, UI_SDLEVENT_QUIT} UI_SDL_Event;

static inline sval sy2my(UI *ui, sval sy) { 
  return sy/ui->tile_h; 
}

static inline sval sx2mx(UI *ui, sval sx) { 
  return sx/ui->tile_w; 
}

static inline sval my2sy(UI *ui, sval my) { 
  return my * ui->tile_h; 
}

static inline sval mx2sx(UI *ui, sval mx) { 
  return mx * ui->tile_w; 
}

static inline SDL_Surface *
ui_player_img(UI *ui, Player *p)
{
  UI_Player *uip = get_uip(p);
  SDL_Surface *img = uip->img;
  return img;
}

static inline sval 
pxSpriteOffSet(Player *p)
{
#if 0
  uval state = player_ui_state(p);
  if (state == UI_PLAYER_HAS_TEAMA_FLAG)
    return (p->team == 1) ? SPRITE_W*1 : SPRITE_W*2;
  if (state == UI_PLAYER_HAS_TEAMB_FLAG) 
    return (p->team == 2) ? SPRITE_W*2 : SPRITE_W*1;
  if (state == UI_PLAYER_HAS_JACKHAMMER) return SPRITE_W*3;
#endif
  
  uval state = player_ui_state(p);
  sval playerOffset = SPRITE_W * 4 * p->teamIndex;

  if (state == UI_PLAYER_HAS_TEAMA_FLAG)
    return (p->team == 1) ? (SPRITE_W*1) + playerOffset : (SPRITE_W*2) + playerOffset;
  if (state == UI_PLAYER_HAS_TEAMB_FLAG) 
    return (p->team == 2) ? (SPRITE_W*2) + playerOffset : (SPRITE_W * 1) + playerOffset;
  if (state == UI_PLAYER_HAS_JACKHAMMER) return (SPRITE_W*3) + playerOffset;

  return playerOffset;
}

static sval
ui_uip_init(UI *ui, Player *p)
{
  UI_Player *ui_p;
  
  ui_p = (UI_Player *)malloc(sizeof(UI_Player));
  if (!ui_p) return 0;

  ui_p->img = ui_player_img(ui, p);
  ui_p->clip.w = SPRITE_W; ui_p->clip.h = SPRITE_H; ui_p->clip.y = 0;

  //NYI();
  ui_p->base_clip_x = p->teamIndex * SPRITE_W * 4;
  put_uip(p, ui_p);

  return 1;
}

/*
 * Return the pixel value at (x, y)
 * NOTE: The surface must be locked before calling this!
 */
static uint32_t 
ui_getpixel(SDL_Surface *surface, int x, int y)
{
  int bpp = surface->format->BytesPerPixel;
  /* Here p is the address to the pixel we want to retrieve */
  uint8_t *p = (uint8_t *)surface->pixels + y * surface->pitch + x * bpp;
  
  switch (bpp) {
  case 1:
    return *p;
  case 2:
    return *(uint16_t *)p;
  case 3:
    if (SDL_BYTEORDER == SDL_BIG_ENDIAN)
      return p[0] << 16 | p[1] << 8 | p[2];
    else
      return p[0] | p[1] << 8 | p[2] << 16;
  case 4:
    return *(uint32_t *)p;
  default:
    return 0;       /* shouldn't happen, but avoids warnings */
  } // switch
}

/*
 * Set the pixel at (x, y) to the given value
 * NOTE: The surface must be locked before calling this!
 */
static void 
ui_putpixel(SDL_Surface *surface, int x, int y, uint32_t pixel)
 {
   int bpp = surface->format->BytesPerPixel;
   /* Here p is the address to the pixel we want to set */
   uint8_t *p = (uint8_t *)surface->pixels + y * surface->pitch + x * bpp;

   switch (bpp) {
   case 1:
	*p = pixel;
	break;
   case 2:
     *(uint16_t *)p = pixel;
     break;     
   case 3:
     if (SDL_BYTEORDER == SDL_BIG_ENDIAN) {
       p[0] = (pixel >> 16) & 0xff;
       p[1] = (pixel >> 8) & 0xff;
       p[2] = pixel & 0xff;
     }
     else {
       p[0] = pixel & 0xff;
       p[1] = (pixel >> 8) & 0xff;
       p[2] = (pixel >> 16) & 0xff;
     }
     break;
 
   case 4:
     *(uint32_t *)p = pixel;
     break;
 
   default:
     break;           /* shouldn't happen, but avoids warnings */
   } // switch
 }

static 
sval splash(UI *ui)
{
  SDL_Rect r;
  SDL_Surface *temp;


  temp = SDL_LoadBMP(UI_LOGO_BMP);
  
  if (temp != NULL) {
    ui->sprites[LOGO_S].img = SDL_DisplayFormat(temp);
    SDL_FreeSurface(temp);
    r.h = ui->sprites[LOGO_S].img->h;
    r.w = ui->sprites[LOGO_S].img->w;
    r.x = ui->screen->w/2 - r.w/2;
    r.y = ui->screen->h/2 - r.h/2;
    //    printf("r.h=%d r.w=%d r.x=%d r.y=%d\n", r.h, r.w, r.x, r.y);
    SDL_BlitSurface(ui->sprites[LOGO_S].img, NULL, ui->screen, &r);
  } else {
    /* Map the color yellow to this display (R=0xff, G=0xFF, B=0x00)
       Note:  If the display is palettized, you must set the palette first.
    */
    r.h = 40;
    r.w = 80;
    r.x = ui->screen->w/2 - r.w/2;
    r.y = ui->screen->h/2 - r.h/2;
 
    /* Lock the screen for direct access to the pixels */
    if ( SDL_MUSTLOCK(ui->screen) ) {
      if ( SDL_LockSurface(ui->screen) < 0 ) {
	fprintf(stderr, "Can't lock screen: %s\n", SDL_GetError());
	return;
      }
    }
    SDL_FillRect(ui->screen, &r, ui->yellow_c);

    if ( SDL_MUSTLOCK(ui->screen) ) {
      SDL_UnlockSurface(ui->screen);
    }
  }
  /* Update just the part of the display that we've changed */
  SDL_UpdateRect(ui->screen, r.x, r.y, r.w, r.h);

  SDL_Delay(3000);
  return 1;
}


static sval
load_sprites(UI *ui) 
{
  SDL_Surface *temp;
  sval colorkey;

  /* setup sprite colorkey and turn on RLE */
  // FIXME:  Don't know why colorkey = purple_c; does not work here???
  colorkey = SDL_MapRGB(ui->screen->format, 255, 0, 255);
  
  temp = SDL_LoadBMP(UI_TEAMA_BMP);
  if (temp == NULL) { 
    fprintf(stderr, "ERROR: loading teama.bmp: %s", SDL_GetError()); 
    return -1;
  }
  ui->sprites[TEAMA_S].img = SDL_DisplayFormat(temp);
  SDL_FreeSurface(temp);
  SDL_SetColorKey(ui->sprites[TEAMA_S].img, SDL_SRCCOLORKEY | SDL_RLEACCEL, 
		  colorkey);

  temp = SDL_LoadBMP(UI_TEAMB_BMP);
  if (temp == NULL) { 
    fprintf(stderr, "ERROR: loading teamb.bmp: %s\n", SDL_GetError()); 
    return -1;
  }
  ui->sprites[TEAMB_S].img = SDL_DisplayFormat(temp);
  SDL_FreeSurface(temp);

  SDL_SetColorKey(ui->sprites[TEAMB_S].img, SDL_SRCCOLORKEY | SDL_RLEACCEL, 
		  colorkey);
  temp = SDL_LoadBMP(UI_FLOOR_BMP);
  if (temp == NULL) {
    fprintf(stderr, "ERROR: loading floor.bmp %s\n", SDL_GetError()); 
    return -1;
  }
  ui->sprites[FLOOR_S].img = SDL_DisplayFormat(temp);
  SDL_FreeSurface(temp);
  SDL_SetColorKey(ui->sprites[FLOOR_S].img, SDL_SRCCOLORKEY | SDL_RLEACCEL, 
		  colorkey);

  temp = SDL_LoadBMP(UI_REDWALL_BMP);
  if (temp == NULL) { 
    fprintf(stderr, "ERROR: loading redwall.bmp: %s\n", SDL_GetError());
    return -1;
  }
  ui->sprites[REDWALL_S].img = SDL_DisplayFormat(temp);
  SDL_FreeSurface(temp);
  SDL_SetColorKey(ui->sprites[REDWALL_S].img, SDL_SRCCOLORKEY | SDL_RLEACCEL, 
		  colorkey);

  temp = SDL_LoadBMP(UI_GREENWALL_BMP);
  if (temp == NULL) {
    fprintf(stderr, "ERROR: loading greenwall.bmp: %s", SDL_GetError()); 
    return -1;
  }
  ui->sprites[GREENWALL_S].img = SDL_DisplayFormat(temp);
  SDL_FreeSurface(temp);
  SDL_SetColorKey(ui->sprites[GREENWALL_S].img, SDL_SRCCOLORKEY | SDL_RLEACCEL,
		  colorkey);

  temp = SDL_LoadBMP(UI_REDFLAG_BMP);
  if (temp == NULL) {
    fprintf(stderr, "ERROR: loading redflag.bmp: %s", SDL_GetError()); 
    return -1;
  }
  ui->sprites[REDFLAG_S].img = SDL_DisplayFormat(temp);
  SDL_FreeSurface(temp);
  SDL_SetColorKey(ui->sprites[REDFLAG_S].img, SDL_SRCCOLORKEY | SDL_RLEACCEL,
		  colorkey);

  temp = SDL_LoadBMP(UI_GREENFLAG_BMP);
  if (temp == NULL) {
    fprintf(stderr, "ERROR: loading redflag.bmp: %s", SDL_GetError()); 
    return -1;
  }
  ui->sprites[GREENFLAG_S].img = SDL_DisplayFormat(temp);
  SDL_FreeSurface(temp);
  SDL_SetColorKey(ui->sprites[GREENFLAG_S].img, SDL_SRCCOLORKEY | SDL_RLEACCEL,
		  colorkey);

  temp = SDL_LoadBMP(UI_JACKHAMMER_BMP);
  if (temp == NULL) {
    fprintf(stderr, "ERROR: loading %s: %s", UI_JACKHAMMER_BMP, SDL_GetError()); 
    return -1;
  }
  ui->sprites[JACKHAMMER_S].img = SDL_DisplayFormat(temp);
  SDL_FreeSurface(temp);
  SDL_SetColorKey(ui->sprites[JACKHAMMER_S].img, SDL_SRCCOLORKEY | SDL_RLEACCEL,
		  colorkey);
  
  return 1;
}

inline static sval
draw_cell_extra(UI *ui, Maze_Cell *cell, SDL_Rect *t, SDL_Surface *s)
{
  maze_dump_cell(cell);
  Player *p = (Player *)cell->player;
  Object *o = (Object *)cell->object;
  UI_Player *ui_p;

  if (t->h == SPRITE_H && t->w == SPRITE_W) {
    if (o) {

      if (o->type==OBJ_JACKHAMMER) 
	SDL_BlitSurface(ui->sprites[JACKHAMMER_S].img, NULL, s, t);
      if (o->type==OBJ_RFLAG) 
	SDL_BlitSurface(ui->sprites[REDFLAG_S].img, NULL, s, t);
      if (o->type==OBJ_GFLAG) 
	SDL_BlitSurface(ui->sprites[GREENFLAG_S].img, NULL, s, t);

      // NYI();
    }
    if (p) {
      ui_p = get_uip(p);
      if (ui_p == NULL) { 
	ui_uip_init(ui, p);
	ui_p = get_uip(p);
      }
      ui_p->clip.x = ui_p->base_clip_x + pxSpriteOffSet(p);
      SDL_BlitSurface(ui_p->img, &(ui_p->clip), s, t);
    }
  } else {
    uint32_t color;
    if (p) {
      color = (p->state & PLAYER_TEAM_A(p)) ? ui->player_teama_c 
                                 	 : ui->player_teamb_c;

      //NYI();
    } else {
      #if 0
      assert(o);
      if (o->type == OBJ_JACKHAMMER) color = ui->jackhammer_c;
      if (o->type == OBJ_RFLAG) color = ui->flag_teama_c;
      if (o->type == OBJ_GFLAG) color = ui->flag_teamb_c;
      #endif
      NYI();
    }
    SDL_FillRect(s, t, color);
  }
  return 1;
}

inline static void
draw_cell(UI *ui, Maze_Cell *cell, SDL_Rect *t, SDL_Surface *s)
{
  SDL_Surface *ts=NULL;
  uint32_t tc;

  if (MAZE_CELL_IS_FLOOR(cell)) {
    tc = ui->isle_c ; 
    ts = ui->sprites[FLOOR_S].img;
  }
  else  {
    if (MAZE_CELL_IS_TEAM_A(cell)) {
      tc = ui->wall_teama_c; 
      ts = ui->sprites[REDWALL_S].img;
    } else {
      tc = ui->wall_teamb_c; 
      ts = ui->sprites[GREENWALL_S].img;
    }
  }

#if 0
  fprintf(stderr, "t.x=%d t.y=%d t.w=%d t.h=%d: cell:", t->x, t->y, t->w, t->h);
  maze_dump_cell(cell);
#endif

  if ( ts && t->h == SPRITE_H && t->w == SPRITE_W) 
    SDL_BlitSurface(ts, NULL, s, t);
  else 
    SDL_FillRect(s, t, tc);

  if (MAZE_CELL_IS_OCCUPIED(cell) || MAZE_CELL_HAS_OBJECT(cell)) {
    draw_cell_extra(ui, cell,t,s);
  }
}

static sval
ui_paintmap(UI *ui, Maze *m) 
{
  SDL_Rect t;
  sval my, mx;
  Maze_Cell *mrow;
  
  assert(m);
  // mview.y1 = mview.y0 + sy2my(((screen->h+tile_h)/tile_h)*tile_h); 
  // mview.x1 = mview.x0 + sx2mx(((screen->w+tile_w)/tile_w)*tile_w); 

  ui->mview.y1 = ui->mview.y0 + sy2my(ui, 
				      ((ui->screen->h)/ui->tile_h)*ui->tile_h); 
  ui->mview.x1 = ui->mview.x0 + sx2mx(ui,
				      ((ui->screen->w)/ui->tile_w)*ui->tile_w); 

  for (my=ui->mview.y0, t.y=0; my<ui->mview.y1; my++, t.y+=ui->tile_h) {
    mrow = maze_row(m,my);
    for (mx = ui->mview.x0, t.x=0; mx<ui->mview.x1; mx++, t.x+=ui->tile_w) {
      t.w=ui->tile_w; t.h=ui->tile_h;
      	draw_cell(ui, mrow + mx, &t, ui->screen);
    }
  }  
  SDL_UpdateRect(ui->screen, 0, 0, ui->screen->w, ui->screen->h);
  return 1;
}


static sval
ui_init_sdl(UI *ui, int32_t h, int32_t w, int32_t d, Maze *m, Players *Teams)
{

  fprintf(stderr, "UI_init: Initializing SDL.\n");

#if 0
    assert(ui && m && Teams);  
#else 
    //NYI();
#endif
  /* Initialize defaults, Video and Audio subsystems */
  if((SDL_Init(SDL_INIT_VIDEO|SDL_INIT_TIMER)==-1)) { 
    fprintf(stderr, "Could not initialize SDL: %s.\n", SDL_GetError());
    return -1;
  }

  atexit(SDL_Quit);

  if (w<m->width) w=m->width; else w=(w/m->width) * m->width;
  if (h<m->height) h=m->height; else h=(h/m->height) * m->height;

  /* init tile h and w */
  ui->tile_h = h/m->height;
  ui->tile_w = w/m->width;

  //  w = ((w + SPRITE_W)/SPRITE_W) * SPRITE_W;
  // h = ((h + SPRITE_H)/SPRITE_H) * SPRITE_H;
 
  fprintf(stderr, "ui_init: h=%d w=%d d=%d\n", h, w, d);

  ui->depth = d;
  ui->screen = SDL_SetVideoMode(w, h, ui->depth, SDL_SWSURFACE);
  if ( ui->screen == NULL ) {
    fprintf(stderr, "Couldn't set %dx%dx%d video mode: %s\n", w, h, ui->depth, 
	    SDL_GetError());
    return -1;
  }
    
  fprintf(stderr, "UI_init: SDL initialized.\n");


  if (load_sprites(ui)<=0) return -1;

  ui->black_c      = SDL_MapRGB(ui->screen->format, 0x00, 0x00, 0x00);
  ui->white_c      = SDL_MapRGB(ui->screen->format, 0xff, 0xff, 0xff);
  ui->red_c        = SDL_MapRGB(ui->screen->format, 0xff, 0x00, 0x00);
  ui->green_c      = SDL_MapRGB(ui->screen->format, 0x00, 0xff, 0x00);
  ui->yellow_c     = SDL_MapRGB(ui->screen->format, 0xff, 0xff, 0x00);
  ui->purple_c     = SDL_MapRGB(ui->screen->format, 0xff, 0x00, 0xff);

  ui->isle_c         = ui->black_c;
  ui->wall_teama_c   = ui->red_c;
  ui->wall_teamb_c   = ui->green_c;
  ui->player_teama_c = ui->red_c;
  ui->player_teamb_c = ui->green_c;
  ui->flag_teama_c   = ui->white_c;
  ui->flag_teamb_c   = ui->white_c;
  ui->jackhammer_c   = ui->yellow_c;
  
 
  ui->mview.x0 = 0;
  ui->mview.y0 = 0;

  /* set keyboard repeat */
  SDL_EnableKeyRepeat(70, 70);  

  SDL_EventState(SDL_MOUSEMOTION, SDL_IGNORE);
  SDL_EventState(SDL_MOUSEBUTTONDOWN, SDL_IGNORE);
  SDL_EventState(SDL_MOUSEBUTTONUP, SDL_IGNORE);

  splash(ui);
  return 1;
}

static void
ui_shutdown_sdl(void)
{
  fprintf(stderr, "UI_shutdown: Quitting SDL.\n");
  SDL_Quit();
}

static void 
upperleft(UI *ui, Maze *m, sval x, sval y) 
{
  assert(m);
  //  fprintf(stderr, "upperleft: x=%ld y=%ld mview.x0=%ld mview.y0=%ld",
  //	  x, y, mview.x0, mview.y0);

  if (x<0) x=0;
  if (x>(m->width - sx2mx(ui, ui->screen->w)))
    x = m->width - sx2mx(ui, ui->screen->w);
  if (y<0) y=0;
  if (y>(m->height - sy2my(ui, ui->screen->h))) 
    y = m->height - sy2my(ui, ui->screen->h);
  
  ui->mview.x0=x;
  ui->mview.y0=y;
  
  //  fprintf(stderr, " -> mview.x0=%ld mview.y0=%ld\n",
  //	  mview.x0, mview.y0);
}

static void 
center(UI *ui, sval x, sval y)
{
}

static sval
ui_userevent(UI *ui, Maze *m, Players *Teams, SDL_UserEvent *e) {
  if (e->code == UI_SDLEVENT_UPDATE) return 2;
  if (e->code == UI_SDLEVENT_QUIT) return -1;
}

static sval
ui_process(UI *ui, Maze *m, Players *Teams)
{
  SDL_Event e;
  sval rc = 1;

  while(SDL_WaitEvent(&e)) {
    switch (e.type) {
    case SDL_QUIT:
      return -1;
    case SDL_KEYDOWN:
    case SDL_KEYUP:
      rc = ui_keypress(ui, m, Teams, &(e.key));
      break;
    case SDL_ACTIVEEVENT:
      break;
    case SDL_USEREVENT:
      rc = ui_userevent(ui, m, Teams, &(e.user));
      break;
    default:
      fprintf(stderr, "e.type=%d NOT Handled\n", e.type);
    }
    if (rc==2) { ui_paintmap(ui, m); }
    if (rc<0) break;
  }
  return rc;
}

extern sval
ui_zoom(UI *ui, Maze *m, sval fac)
{
  assert(ui && m);
  fprintf(stderr, "zoom: %ld tile_h=%d tile_w=%d m.h=%ld m.w=%ld s.h=%d"
	  " s.w=%d\n", fac, ui->tile_h, ui->tile_w, m->height, m->width, 
	  ui->screen->h, ui->screen->w);

  if (fac==1 && ui->tile_h < SPRITE_H && ui->tile_w < SPRITE_W) {
    ui->tile_h <<= 1;
    ui->tile_w <<= 1;
    upperleft(ui, m, ui->mview.x0, ui->mview.y0);
  } else if (fac == -1 && 
	     (((ui->tile_h>>1) * m->height) >= ui->screen->h) && 
	     (((ui->tile_w>>1) * m->width) >= (ui->screen->w))) {
    ui->tile_h >>= 1;
    ui->tile_w >>= 1;
    upperleft(ui, m, ui->mview.x0, ui->mview.y0);
  }

  //  fprintf(stderr, " -> tile_h=%d tile_w=%d\n", tile_h, tile_w);
  return 2;
}

extern sval
ui_pan(UI *ui, Maze *m, sval xdir, sval ydir)
{
  assert(ui && m);
  upperleft(ui, m, ui->mview.x0 + xdir, ui->mview.y0 + ydir);
  return 2;
}

extern sval
ui_move(UI *ui, Maze *m, Player *p, sval xdir, sval ydir)
{
  assert(ui && m);
  if (p && maze_player_move(m, p, xdir, ydir))  return 2;
  return 1;
}


extern void
ui_update(UI *ui)
{
  SDL_Event event;
  
  if (ui->running && ui->active) {
    event.type      = SDL_USEREVENT;
    event.user.code = UI_SDLEVENT_UPDATE;
    SDL_PushEvent(&event);
  }

}

extern void
ui_pause(UI *ui)
{
  ui->active = 0;
}

extern void
ui_resume(UI *ui)
{
  ui->active = 0;
  ui_update(ui);
}

extern void
ui_start(UI *ui)
{
  pthread_mutex_lock(&ui->lock);
  ui->start = 1;
  pthread_cond_signal(&ui->start_cond);
  pthread_mutex_unlock(&ui->lock);
}

extern void
ui_quit(UI *ui)
{
  if (ui->running) {
    SDL_Event event;
    fprintf(stderr, "ui_quit: stopping ui...\n");
    event.type      = SDL_USEREVENT;
    event.user.code = UI_SDLEVENT_QUIT;
    SDL_PushEvent(&event);
  } else {
    pthread_mutex_lock(&ui->lock);
    pthread_cond_signal(&ui->start_cond);
    pthread_mutex_unlock(&ui->lock);    
  }
}

extern void
ui_main_loop(UI *ui, uval h, uval w, Maze *map, Players *Teams, uval start_ui)
{
  sval rc;
  
  assert(ui);

  if (start_ui==0) {
    pthread_mutex_lock(&ui->lock);
    pthread_cond_wait(&ui->start_cond, &ui->lock);
  }

  if (start_ui || ui->start == 1) {
    ui_init_sdl(ui, h, w, 32, map, Teams);
    ui_paintmap(ui, map);
   
    ui->running = 1;
    while (1) {
      if (ui_process(ui, map, Teams)<0) break;
    }
    ui->running = 0;
    ui_shutdown_sdl();
  }
  pthread_mutex_unlock(&ui->lock);  
}


extern void
ui_init(UI **ui)
{
  *ui = (UI *)malloc(sizeof(UI));
  if (ui==NULL) return;

  bzero(*ui, sizeof(UI));
  
  (*ui)->tile_h = SPRITE_H;
  (*ui)->tile_h = SPRITE_W;

  (*ui)->start = 0;
  pthread_cond_init(&((*ui)->start_cond), NULL);
  pthread_mutex_init(&((*ui)->lock), NULL);
  (*ui)->running = 0;
  (*ui)->active = 1;
}


//int NYI(void) { fprintf(stderr, "NYI\n"); return 1; }

