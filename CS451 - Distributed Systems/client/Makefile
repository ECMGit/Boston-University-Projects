targets = client

include ../config.mk

client_objs = $(objs)
libs = $(DAGAMELIB) $(UILIBS)

//bmps = logo.bmp

//logo.bmp:
         //convert -size 120x50 xc:orange -pointsize 24 -stroke purple -font URW-Gothic-Book -gravity center -draw 'text 0,0 "daGame"' logo.bmp

client: $(client_objs) $(DAGAMELIBARCHIVE)
	gcc $(CFLAGS) -o $@ $(client_objs) $(libs) -lpthread

