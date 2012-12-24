----------------------------------------------------------------
--
-- Ibis (light version: Haskell syntax, limited capability)
-- Copyright (C) 2008-2009
--
-- This software is made available under the GNU GPLv3.
--
-- State.hs
--   Representation of state for verifier.

----------------------------------------------------------------
-- 

module State where

import Set
import Exp (Name, Exp(..), uv, fv, splitAnd, isFalse)
import Relations

----------------------------------------------------------------
-- Representation of the collection of assumptions available
-- when verifiying.

type State = ([Name],[(Exp,())],Relations,(Integer,[Name]),[Exp],[()])

state0 :: State
state0 = ([],[],empRels,(0,[]),[],[])

getBound :: State -> [Name]
getBound (b,_,_,_,_,_) = b

getAssumptions :: State -> [Exp]
getAssumptions (_,as,_,_,_,_) = map fst as

getEvalContext :: State -> [Exp]
getEvalContext (_,_,_,_,ec,_) = ec

freshExpVars :: Exp -> State -> (Exp, State)
freshExpVars e (b,as,rs,(c,gs),ec,m) = (e',(b,as,rs,(c',gs),ec,m))
  where (e',c') = uv [] c e

chkStateRels :: State -> Exp -> Bool
chkStateRels (_,_,rs,_,_,_) e = chkRels rs e

chkStateEq :: State -> Exp -> Exp -> Bool
chkStateEq (_,_,rs,_,_,_) e1 e2 = chkEq rs e1 e2

-- Remove assumptions that no longer apply after bound
-- variables have been hidden my a new quantifier.

updVars :: [Name] -> State -> State
updVars vs (b,as,r,c,ec,m) =
  ( b `u` vs,
    [(a,s) | (a,s)<-as, f a],
    updVarRels f r,
    c,
    [e | e<-ec, f e],
    m
  )
  where f e = (fv (b `diff` vs) e) `isect` vs == []

updExps0 :: (State -> Exp -> Exp) -> Exp -> State -> State
updExps0 evl e s = foldl each s $ splitAnd e where
  each (s@(b,as,rs,c,ec,m)) e =
   (b, (e,()):as, updRels rs e, c, (evl s e):ec,m)

-- not semantic removal, just for performance-driven manipulations
remAssump (b,as,rs,c,ec,m) a' = (b, [a | a<-as, fst a /= a'] ,rs,c,ec `diff` [a'],m)

--eof
