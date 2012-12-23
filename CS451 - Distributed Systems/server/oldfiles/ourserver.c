#include <stdio.h>
#include <stdlib.h>
#include "../lib/dummy.h"
#include "net.c"

int
main(int argc, char *argv[])
{
    if(argc < 2){
        printf("Please include the port number in the command line arguments!\n");
        return 0;
    }
    
    else if (argc > 2){
        printf("Woahhh.. to many numbers! Just put the port number please!\n");
        return 0;
    }
    

    PortType port = (PortType) atoi(argv[1]); // parse string to int
    FDType socket;
    
    printf("Listening on port %d\n", port);
    
    if (net_setup_listen_socket(&socket, &port))
	  	printf("Socket has been bound!\n");
        
    // Tell socket to listen
    if (net_listen(socket))
    	printf("listening for connections...\n");
    
		for (;;) {
			FDType newsocket;
			pid_t childpid;
			newsocket = net_accept(socket);// Blocks until request is found
			//Support for multiple client connections
			if ((childpid = fork()) == 0) {				
				close(socket);
        printf("connection established!\n");
				// do echo function
				char line[1024];
				while (read(newsocket, line, 1024) != 0) {
					printf("the client sent %s\n",line);
					write(newsocket, line, 1024);
					// if (strcmp(line, "quit") == 0)
					// 	break; 
					memset(&line[0], 0, sizeof(line));

										
				} 
				

				
				exit(0);
				printf("I have exited\n");
			}

	        // char input[1024];
	        // 
	        //     net_readn(socket, input, 1024);
	        //     net_writen(socket, input, 1024);
			close(newsocket);
	   
		}
        
    return 0;
}
