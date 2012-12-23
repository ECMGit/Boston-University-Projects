----------------------------------------------------------------
--
-- Ibis (light version: Haskell syntax, limited capability)
-- Copyright (C) 2008-2009
--
-- This software is made available under the GNU GPLv3.
-- 
-- Main.hs
--   Main module. Parses command-line arguments, calls
--   appropriate routines, and tracks time.

----------------------------------------------------------------
-- 

module Main (main) where

import System.Environment (getArgs)
import System.Time (getClockTime,diffClockTimes,tdSec,tdPicosec)

import Parse (parseFile)
import Stmt (run)
import Result (Result(..), showRs)

----------------------------------------------------------------
-- Takes a file path in the form of a string, try to parse the
-- file into an abstract syntax, and run it.

showTD td = "(completed in "++
  (show$floor(((toRational$tdSec td)*(toRational$10^12)+(toRational$tdPicosec td))/10^9))++"ms)\n"

mainParseConv :: Bool -> String -> IO ()
mainParseConv html fname =
  do { t0 <- getClockTime
     ; r <- parseFile fname
     ; putStr $ showRs html $ case r of
         Right ss ->  run ss
         Left err -> [err]
     ; t1 <- getClockTime
     ; putStr $ "\n"++showTD (diffClockTimes t1 t0)
     }

procCmd html lit [str] = mainParseConv html str
procCmd _ _ _ =
  putStr$showRs False$ [ErrSystem 
                 "usage:\n\n\tibis [-html] \"path/file.ext\"\n"]

----------------------------------------------------------------
-- The main function, useful if the interpreter is compiled.

main :: IO ()
main =
  do{ args <- getArgs
    ; procCmd False False args
    }

ibis fname =
  do{ procCmd False False [fname]
    }

--eof
