################################################################
##
## Ibis (light version: Haskell syntax, limited capability)
## Copyright (C) 2008-2009
##
## This software is made available under the GNU GPLv3.
##
## Makefile
##   Basic makefile for compiling with GHC under Linux
##   environemnts.

################################################################
##

CC = ghc
CCFLAGS = -O2 --make -odir o -hidir hi
SRCS = *.hs
TARGET = Main
EXECUTABLE = ibis

all: ${SRCS}
	if [ ! -d "./o" ] ; then mkdir o; fi
	if [ ! -d "hi" ] ; then mkdir hi; fi
	${CC} ${CCFLAGS} ${TARGET} -o $(EXECUTABLE)

clean:
	rm -rf o
	rm -rf hi

#eof
