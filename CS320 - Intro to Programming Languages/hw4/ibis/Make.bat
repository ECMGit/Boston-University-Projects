::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
::
:: Ibis (light version: Haskell syntax, limited capability)
:: Copyright (C) 2008-2009
::
:: This software is made available under the GNU GPLv3.
::
:: Make.bat
::   Batch script for compiling with GHC under Windows
::   environments.

::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
::

:o
@IF EXIST o GOTO hi
@MD o
:hi
@IF EXIST hi GOTO make
@MD hi
:make
ghc -O2 --make -odir o -hidir hi Main -o ibis.exe

::eof
