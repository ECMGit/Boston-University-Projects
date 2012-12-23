#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include "../lib/types.h"
#include "../lib/protocol_client.h"

void *PS;

prompt(void) 
{
  char c[2];
  printf("\n> ");
  scanf("%1s", c);
  return c[0];
}

int doConnect(void)
{
  char host[81];
  PortType p;
  
  printf("enter host port: ");
  scanf("%80s %d", host, &p);
  return proto_client_init(&PS, host, p);
}

int
doRPC(void)
{
  int rc;
  char c;

  printf("enter (h|m<u,d,l,r>|p|d|g): ");
  scanf("%c", &c);

  switch (c) {
  case 'h':
    rc = proto_client_hello(PS);
    if (rc == 0xdeadbeef) rc=1;
    break;
  case 'm':
    scanf("%c", &c);
    rc = proto_client_move(PS, c);
    if (rc == 0xdeadbeef) rc=1;
    break;
  case 'p':
    rc = proto_client_pickup(PS);
    if (rc == 0xdeadbeef) rc=1;
    break;
  case 'd':
    rc = proto_client_drop(PS);
    if (rc == 0xdeadbeef) rc=1;
    break;
  case 'g':
    rc = proto_client_goodbye(PS);
    if (rc == 0xdeadbeef) rc=1;
    break;
  default:
    printf("doRPC: unknown command %c\n", c);
  }

  printf("doRPC: rc=0x%x\n", rc);
  return rc;
}

int
doUI(void)
{
  return 1;
}

int 
docmd(char cmd)
{
  int rc = 1;

  switch (cmd) {
  case 'c':
    rc = doConnect();
    break;
  case 'r':
    rc = doRPC();
    break;
  case 'u':
    rc = doUI();
    break;
  default:
    printf("Unkown Command\n");
  }
  return rc;
}

int
shell(void)
{
  char c=prompt();
  int rc=docmd(c);
  return rc;
}


int
main(int argc, char **argv)
{  
  int rc;

  while (1) {
    rc = shell();
    if (rc!=1) break;
  }

  return 0;
}
