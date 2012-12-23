----------------------------------------------------------------
-- Computer Science 320 (Fall, 2009)
-- Concepts of Programming Languages
--
-- Assignments 6, 7, and 8
--   Main.hs

----------------------------------------------------------------
-- Main module for the Mini-Haskell interpreter

module Main where

--import System.Environment   -- to obtain command-line arguments

import Err (Error(..))
import Eval (evalExp)
import Parser (parseFile)
import Ty (typeCheck)

----------------------------------------------------------------
-- Take a file path in the form of astring, and try to parse
-- the contents of the file into abstract syntax. If this
-- succeeds, type check the abstract syntax, and if this
-- succeeds, evaluate it and display the result.

parseTypeEval :: String -> IO ()
parseTypeEval fname =
  do { r <- parseFile fname
     ; case r of              
       Left err -> do {putStr "parse error: "; print err}
       Right e -> case (typeCheck e) of
           Error msg -> do {putStr "type error: "; print msg}
           S ty -> 
            case (evalExp e) of
                Error msg ->
                  do {putStr "evaluation error: "; print msg}
                S v -> putStr $ show v ++ " :: " ++ show ty
     }

-- Take a file path in the form of astring, and try to parse
-- the contents of the file into abstract syntax. If this
-- succeeds, evaluate the abstract syntax, and display the
-- result.

parseEval :: String -> IO ()
parseEval fname =
  do { r <- parseFile fname
     ; case r of              
       Left err -> do {putStr "parse error: "; print err}
       Right e ->
           case (evalExp e) of
                Error msg ->
                  do {putStr "evaluation error: "; print msg}
                S v -> putStr $ show v
     }

----------------------------------------------------------------
-- The main function, useful if the interpreter is compiled.

main :: IO ()
main = parseTypeEval "tests1.mhs"

-- If you wish to compile the interpreter, the following
-- code can be used to obtain a file name from the command
-- line. However, be sure to uncomment the
--
--      import System.Environment
--
-- at the beginning of this module's definition.

-- main takes the first command-line argument and treats it
-- as a file path, calling mainParseRun on that file; if
-- the number of command-line argument is not exactly one,
-- main returns an error message indicating this.

-- main :: IO ()
-- main =
--    do { args <- getArgs
--       ; case args of
--          [filename] -> mainParseEval (head args)
--          otherwise  -> 
--            putStr "error: specify a single file name.\n" }
