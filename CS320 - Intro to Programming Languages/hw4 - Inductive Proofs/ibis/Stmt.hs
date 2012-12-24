----------------------------------------------------------------
--
-- Ibis (light version: Haskell syntax, limited capability)
-- Copyright (C) 2008-2009
--
-- This software is made available under the GNU GPLv3.
--
-- Stmt.hs
--   Representation of statements and functions for turning a
--   list of statements into verification results.

----------------------------------------------------------------
-- 

module Stmt (run, Stmt(..)) where

import Const (Const(..))
import Exp
import State
import Verification
import Inference(verify, updExps)
import Result

data Stmt =
    Intro [Name]
  | Assume Exp
  | Assert String Exp
  | Include String [Stmt]
  deriving Show

run :: [Stmt] -> [Result]
run ss = fst (execs state0 ss)

exec :: State -> Stmt -> ([Result], State)
exec state (Intro vs) = ([OkIntro (map fst vs)], updVars vs state)
exec state (Assume e) = noFVs e' state' $ ([OkAssume ()], updExps e' state')
  where (e',state') = preProc e state
exec state (Assert str e) =
  let (e',state') = preProc e state in
  noFVs e' state' $ case verify state' e' of
    Verifiable s (B True) -> ([R str $ Verifiable s (B True)], updExps e state')
    r                     -> ([R (str) r], state') -- ++":"++(show (evalExp state e))
exec state (Include n ss) = (map (mkRInclude n) rs, state')
  where (rs, state') = execs state ss

execs :: State -> [Stmt] -> ([Result], State)
execs state []     = ([], state)
execs state (s:ss) =
  let (vs , state' ) = exec  state  s
      (vs', state'') = execs state' ss 
  in (vs++vs', state'')

-- Checks that all the variables in an expression are bound
-- given a state.
noFVs e s ow = if length fvs > 0 then ([ErrUnbound $ map fst fvs], s) else ow
  where fvs = fv (getBound s) e

preProc e state = freshExpVars (normOps e) state

-- eof
