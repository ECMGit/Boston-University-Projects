UISYSTEM=$(shell uname)

ifeq ($(UISYSTEM),Darwin)
  UIINCDIR = -I/opt/local/include
  UILIBS =  -L/opt/local/lib -lSDLmain -lSDL -lSDL_image -framework Cocoa
else
  UINCDIR = 
  UILIBS = -lSDL
endif

CFLAGS := -g $(UIINCDIR)

MODULE := $(shell basename $CURDIR)

DAGAMELIBHDRS := types.h net.h protocol.h protocol_utils.h maze.h     \
	protocol_session.h protocol_client.h protocol_server.h \
        player.h objects.h ui.h
DAGAMELIBFILE := libdagame.a
DAGAMELIBARCHIVE := ../lib/$(DAGAMELIBFILE)
DAGAMELIB := -L../lib -ldagame


src  = $(wildcard *.c)
objs = $(patsubst %.c,%.o,$(src))

ifeq ($(MODULE),lib)
  DAGAMELIBINCS:=$(DAGAMELIBHDRS)
else
  DAGAMELIBINCS:=$(addprefix ../lib/,$(DAGAMELIBHDRS))
endif


all: $(targets)
.PHONY: all

$(objs) : $(src) $(DAGAMELIBINCS)

clean:
	rm $(objs) $(targets)

