/*
 */
#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <unistd.h>

#include "mm.h"
#include "memlib.h"

/*********************************************************
 * NOTE TO STUDENTS: Before you do anything else, please
 * provide your team information in the following struct.
 ********************************************************/
team_t team = {
    /* Team name */
    "davarisg_timmahd",
    /* First member's full name */
    "Georgios Davaris",
    /* First member's email address */
    "davarisg",
    /* Second member's full name (leave blank if none) */
    "Tim Duffy",
    /* Second member's email address (leave blank if none) */
    "timmahd"
};

/* single word (4) or double word (8) alignment */
#define ALIGNMENT 8
/* rounds up to the nearest multiple of ALIGNMENT */
#define ALIGN(size) (((size) + (ALIGNMENT-1)) & ~0x7)

#define SIZE_T_SIZE (ALIGN(sizeof(size_t)))

#define ZONE_SIZE 16384

// Explict list
static void *free_list = NULL;
static void *heap_end = NULL;	// end of free list

static size_t mm_get_size(void *p) {
  size_t tmp = *((size_t *) p);
  return tmp & ~0x7;
}

static void mm_set_size(void *p, size_t n, int alloc) {
  size_t tmp = *((size_t *) p);
  tmp = (n | alloc) ; // header
  *(((size_t *) p) + (n-1)) = (n | alloc); // footer
}

static void *mm_get_next(void *p) {
  if ( (p = *((void **) ((size_t *) p + 1)))  ==  NULL){ return NULL; }
 return p;
}

static void *mm_get_prev(void *p) {
  return *((void **) ((size_t *) p + 2));
}

static void mm_set_next(void *p, void *next) {
  *((void **)((size_t *) p + 1)) = next;
}

static void mm_set_prev(void *p, void *prev) {
  *((void **)((size_t *) p + 2)) = prev;
}

static int is_alloc(void *p){
  size_t tmp = *((size_t *) p);
  return (tmp & 1);
}

/* mm_coalesce - merge any contiguous free blocks together
*/
static void  *mm_coalesce(void *fblock) {
  void *prev = (void *) ((size_t *)fblock - *((size_t *)fblock - 1));
  void *next = (void *) ((size_t *)fblock + *((size_t *)fblock));
  void *n_next = NULL;

  if (next != NULL) { n_next = mm_get_next(next); }
    
  size_t p_alloc = 1;
  size_t n_alloc = 1;

  if (prev != NULL) { p_alloc = is_alloc(prev); }
  if (next != NULL) { n_alloc = is_alloc(next); }
  size_t size = mm_get_size(fblock);
  
  // if prev and next are allocated
  if (p_alloc && n_alloc) { return fblock; }
  
  else if (p_alloc && !n_alloc){   // if only next is free
    size += mm_get_size(next);
    mm_set_size(fblock, size, 0);
    mm_set_next(fblock, n_next);
    
    if (n_next != NULL){ mm_set_prev(n_next, fblock); }
    return fblock;
  }
  
  else if (!p_alloc && n_alloc){  // if only prev is free
    size += mm_get_size(prev);
    mm_set_size(prev,size, 0);
    mm_set_next(prev, next);
    mm_set_prev(fblock, prev);
    return prev;
  }
  
  else{					// if both are free
    size += mm_get_size(prev) + mm_get_size(next);
    mm_set_size(prev, size , 0);
    mm_set_next(prev, n_next);
    if (n_next != NULL){ mm_set_prev(n_next, prev); }
    return prev;

  }
}

static void *extend_heap(size_t size){
  char *bp;
  
  size = ALIGN(size);

  if((bp = mem_sbrk(size)) != (void *) -1){
    
    mm_set_size(bp, size, 0);
    mm_set_next(heap_end, bp);
    mm_set_next(bp, NULL);
    mm_set_prev(bp, heap_end);  

    heap_end = mem_heap_hi();  // update free_list_end

    return mm_coalesce(bp);
  }

  else { return NULL; }
}

/* 
 * mm_init - initialize the malloc package.
 */
int mm_init(void)
{
  if ( (free_list = mem_sbrk(4 * sizeof(size_t))) == (void *) -1) { return -1; }
 
  mm_set_size(free_list, (4 * sizeof(size_t)) , 0);

  // make our list
  mm_set_next(free_list, NULL);
  mm_set_prev(free_list, NULL); 

  heap_end = (void *) mem_heap_hi();

  if (extend_heap(ZONE_SIZE) == NULL) { return -1; }

  return 0;
}


void mm_free (void *ptr) {

  size_t size = mm_get_size(ptr);
  mm_set_size(ptr, size, 0);
  
  mm_set_next(ptr, free_list);
  mm_set_prev(free_list, ptr);
  mm_set_prev(ptr, NULL);

  mm_coalesce(ptr);
}

/* 
 * mm_malloc - Allocate a block by incrementing the brk pointer.
 *     Always allocate a block whose size is a multiple of the alignment.
 */
void *mm_malloc(size_t size) {
  int n = ALIGN(size + SIZE_T_SIZE); // newsize is the aligned size
  
  void **curr = &free_list;
  
  while (*curr != NULL) {
    size_t m = mm_get_size(*curr);
    if (n > m) {			// if block is too small
      *curr = mm_get_next(*curr);           // move forward through list
      continue;
    }
    
    
    // When loop exits, we have a block with enough space
    
    /* Split current block here if needed.  Minimum memory used by a
       block is the size header plus the next pointer. */
    if (m - n > 4 * SIZE_T_SIZE) {
      
      void *orig_next = mm_get_next(*curr);  // remember the original next
      void *orig_prev = mm_get_prev(*curr);  // remember orig previous

      mm_set_size(*curr, n, 1); // update curr size
      
      // update size of new block
      size_t new_size = ALIGN(m - n - sizeof(size_t));  // size of new block
      
      // where new block begins
      void *new_free_block = (void *) (((size_t *) *curr) + n);
      mm_set_size(new_free_block, new_size, 0);

      // pop curr out of list
      mm_set_next(orig_prev, new_free_block);
      mm_set_prev(new_free_block, orig_prev);
      mm_set_next(new_free_block, orig_next);
      mm_set_prev(orig_next, new_free_block); 

      return mm_coalesce(new_free_block);
    }
  }

  if( extend_heap(ZONE_SIZE) == NULL) { return NULL; }
  return mm_malloc(n);

}
