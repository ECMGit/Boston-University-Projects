#ifndef __PROTOCOL_H__
#define __PROTOCOL_H__

//  HDR : BODY
//  HDR ->  VERSION | TYPE | PSTATE | GSTATE | BLEN 
//     VERSION : int default version is 0
//     TYPE    : int see enum below
//     PSTATE  : <PID,POS,STATE,EXTRA>  
//                 PID:    int
//                 POS:    int 
//                 STATE:    int   
//                 EXTRA:    int
//     GSTATE   : <DIFF,STATE,EXTRA> 
//                 DIFF:  int
//                 STATE:  int
//                 EXTRA:  int
//     BLEN     : int length of body

#define PROTOCOL_BASE_VERSION 0

typedef enum  {

  // Requests
  PROTO_MT_REQ_BASE_RESERVED_FIRST,
  PROTO_MT_REQ_BASE_HELLO,
  PROTO_MT_REQ_BASE_MOVE,
  PROTO_MT_REQ_BASE_PICKUP,
  PROTO_MT_REQ_BASE_DROP,
  PROTO_MT_REQ_BASE_GOODBYE,
  // RESERVED LAST REQ MT PUT ALL NEW REQ MTS ABOVE
  PROTO_MT_REQ_BASE_RESERVED_LAST,
  
  // Replys
  PROTO_MT_REP_BASE_RESERVED_FIRST,
  PROTO_MT_REP_BASE_HELLO,
  PROTO_MT_REP_BASE_MOVE,
  PROTO_MT_REP_BASE_PICKUP,
  PROTO_MT_REP_BASE_DROP,
  PROTO_MT_REP_BASE_GOODBYE,
  // RESERVED LAST REP MT PUT ALL NEW REP MTS ABOVE
  PROTO_MT_REP_BASE_RESERVED_LAST,

  // Events  
  PROTO_MT_EVENT_BASE_RESERVED_FIRST,
  PROTO_MT_EVENT_BASE_UPDATE,
  PROTO_MT_EVENT_BASE_RESERVED_LAST

} Proto_Msg_Types;

typedef enum { PROTO_STATE_INVALID_VERSION=0, PROTO_STATE_INITIAL_VERSION=1} Proto_SVERS;

typedef union {
  unsigned long long raw;
} Proto_StateVersion;

typedef union {
  int raw;
  struct {
    int team;
    int num;
  } __attribute__((__packed__)) pid;
} Proto_PId;

typedef union {
  int raw;
  struct {
    unsigned short x;
    unsigned short y;
  } __attribute__((__packed__)) pos;
} Proto_PPos;

typedef union {
  int inGame; //0 if no, 1 if yes
  enum Proto_PState_Bits {
    PPS_JAIL=1,
    PPS_HOME=2,
    PPS_ONSIDE=4,
    PPS_HASFLAG=8,
    PPS_HASJACKHAMMER=16
  } state_bits;
} Proto_PState;


typedef struct {
  Proto_PId    pid;
  Proto_PPos   pos;
  Proto_PState state;
  int extra;
} __attribute__((__packed__)) Proto_Player_State;

typedef union {
  int raw;
}  Proto_GDiff;

typedef union {
  int raw;
  enum Proto_GState_Bits {
    PGS_TEAM1WON=1,
    PGS_TEAM2WON=2
  } state_bits;
} Proto_GState;


typedef struct {
  Proto_GDiff        diff;
  Proto_GState       state;
  int                extra;
} __attribute__((__packed__)) Proto_Game_State;

typedef struct {
  int                version;
  Proto_Msg_Types    type;
  Proto_StateVersion sver;
  Proto_Player_State pstate;
  Proto_Game_State   gstate;
  int                blen;
} __attribute__((__packed__)) Proto_Msg_Hdr;

// THE FOLLOWING IS TO MAKE SURE THAT YOU GET 
// 64bit ntohll and htonll defined from your
// host OS... only tested for OSX and LINUX
#ifdef __APPLE__
#  ifndef ntohll
#    include <libkern/OSByteOrder.h>
#    define ntohll(x) OSSwapBigToHostInt64(x)
#    define htonll(x) OSSwapHostToBigInt64(x)
#  endif
#else
#  ifndef ntohll
#    include <asm/byteorder.h>
#    define ntohll(x) __be64_to_cpu(x)
#    define htonll(x) __cpu_to_be64(x)
#  endif
#endif

#endif
