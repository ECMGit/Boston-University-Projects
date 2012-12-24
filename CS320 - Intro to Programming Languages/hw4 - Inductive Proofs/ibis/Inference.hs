----------------------------------------------------------------
--
-- Ibis (light version: Haskell syntax, limited capability)
-- Copyright (C) 2008-2009
--
-- This software is made available under the GNU GPLv3.

-- Inference.hs
--   Functions for making and verifiyig logical inferences
--   using the logical syntax defined in Exp.

----------------------------------------------------------------
-- 

module Inference (verify, updExps) where

import Data.Maybe (catMaybes)
import Data.List (sort)

import Set
import Const (Const(..))
import Exp
import Subst
import Verification
import State

updExps e s = updExps0 (\s e -> e) e s

verify s e = r$r$r$r$r$r$vfy s e where
  r (Potential vf) = vf ()
  r v = v

verify0 s e = r$r$r$r$r$r$vfy0 0 s e where
  r (Potential vf) = vf ()
  r v = v

vfy :: State -> Exp -> Verification
vfy = vfy0 1

vfy0 :: Int -> State -> Exp -> Verification
vfy0 d s e =
 let vfy = vfy0 d
     srchF = if d <= 0 then srchv' else srchv
     srch0 ow = case ow of Unknown -> chkC s e $ srchF s e; _ -> ow
 in case e of
  App (C And) (T[e1,e2]) -> vfy s e1 &&& vfy (updExps e1 s) e2
  App (C Or)  (T[e1,e2]) -> srch0 $ vfy s e1 ||| vfy s e2
  App (C Imp) (T[e1,e2]) -> srch0 $ notV (vfy s e1) ||| vfy (updExps e1 s) e2
  App (C Iff) (T[e1,e2]) -> srch0 $ vfy (updExps e1 s) e2 &&& vfy (updExps e2 s) e1

  App (C FalToUnknown) e -> isVTrue' $ vfy s e
  App (C SearchIff) (T[e',e]) -> if isVTrue (verify0 s e) then vfy s e' else Unknown

  Forall ns e0 -> srch0 $ vfy (updVars ns s) e0
  _ -> 
     expToVer e $
     (chkC s e)
     ( srchF s e
   ||| boolToV (chkStateRels s e)
   ||| boolToV (or [chkStateEq s a' e | a'<-getEvalContext s])
   )

chkC s e ow = case srchContext s e of
  Verifiable s (B b) -> Verifiable s (B b) 
  _ -> ow

srchContext s e = if e `elem` getAssumptions s then
                  Verifiable () (B True) else Unknown

-- Check whether some combination of assumptions implies the formula in
-- question. It is capable of handling assumptions that contain an
-- arbitrarily long sequence of alternating "for all"/"implies" expressions.

srchv s e = Potential (\()-> orV $ map (vfy s) $ concat [search [] [] e a | a<-getAssumptions s])
srchv' s e = orV $ map (\(e,a)-> vfy0 0 (remAssump s a) e) $ concat [map (\e->(e,a)) $ search [] [] e a | a<-getAssumptions s]

-- Still need to check that substitution actually captures all variables...

search :: [Name] -> [(Exp,Subst->Subst)] -> Exp -> Exp -> [Exp]
search ns gs e (App (C And) (T[e1,e2])) = search ns gs e e1 ++ search ns gs e e2
search [] gs e (App (C Or)  (T[e1,e2])) = map (App (C FalToUnknown)) $ [bOp And (listAnd [e | (e,_)<-gs]) (bOp And (bOp Imp e1 e) (bOp Imp e2 e))]
search ns gs e (App (C Iff) (T[e1,e2])) =
  let l1 = (case match ns e2 e of
               Nothing -> []
               Just sub -> 
                 if not $ ns `subset` boundInSubst sub then [] else [bOp SearchIff (subst sub e1) (listAnd [subst (subf sub) e | (e,subf)<-gs])])
        ++
           (case match ns e1 e of
               Nothing -> []
               Just sub -> 
                 if not $ ns `subset` boundInSubst sub then [] else [bOp SearchIff (subst sub e2) (listAnd [subst (subf sub) e | (e,subf)<-gs])])
  in if length l1 > 0 then l1 else search ns gs e (bOp Imp e1 e2) ++ search ns gs e (bOp Imp e2 e1)

search ns gs e (App (C Imp) (T[e1,e2])) = search ns ((e1,\s->s):gs) e e2
search ns gs e (Forall ns' e') = search (ns `u` ns') (map (\(e,s) -> (e,(limitSubst ns).s)) gs) e e'
search ns gs e e' = map (App (C FalToUnknown)) $ searchBase ns gs e e'

searchBase ns gs e e' = case match ns e' e of
  Nothing -> []
  Just sub -> if not $ ns `subset` boundInSubst sub then [] else [listAnd [subst (subf sub) e | (e,subf)<-gs]]

--eof
