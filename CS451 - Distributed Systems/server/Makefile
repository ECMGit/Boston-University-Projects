targets=server

include ../config.mk


server_objs = $(objs)
libs = $(DAGAMELIBDIR) $(DAGAMELIB) $(UILIBS)

server: $(server_objs) $(DAGAMELIBARCHIVE)
	gcc $(CFLAGS) -o $@ $(server_objs) $(libs) -lpthread 
