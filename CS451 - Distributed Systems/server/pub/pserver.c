#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include "../lib/types.h"
#include "../lib/protocol_server.h"

int
main(int argc, char **argv)
{  

  if (proto_server_init()<0) {
    fprintf(stderr, "ERROR: failed to initialize proto_server subsystem\n");
    exit(-1);
  }

  fprintf(stderr, "RPC Port: %d, Event Port: %d\n", proto_server_rpcport(), 
	  proto_server_eventport());

  proto_server_rpc_loop();

}
