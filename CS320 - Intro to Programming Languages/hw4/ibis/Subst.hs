----------------------------------------------------------------
--
-- Ibis (light version: Haskell syntax, limited capability)
-- Copyright (C) 2008-2009
--
-- This software is made available under the GNU GPLv3.
--
-- Subst.hs
--   Substitution (capture-avoiding) on expressions.

----------------------------------------------------------------
-- 

module Subst (emptySubst, match, subst, unify, 
              boundInSubst, limitSubst, Subst) where

import Set
import Const
import Exp

type Subst = [(Name, Exp)]

emptySubst :: Subst
emptySubst = []

boundInSubst :: Subst -> [Name]
boundInSubst s = map fst s

limitSubst :: [Name] -> Subst -> Subst
limitSubst ns = filter $ \(n,_) -> n `elem` ns

----------------------------------------------------------------
-- Capture-avoiding substitution.
--
-- The maximum variable index of the replacement expressions 
-- is computed, and the target expression's variable indices
-- are incremented so that all variables are unique with
-- respect to each other. This update takes linear time in the
-- size of all the expressions involved in a substitution.

subst :: Subst -> Exp -> Exp
subst s e = subst' s (incVar [] (foldr max 0 (map (maxVar.snd) s)) e)
subst' [] e            = e
subst' s (C op)        = C op
subst' s (Var x)       = maybe (Var x) id $ lookup x s
subst' s (Forall ns e) = Forall ns $ subst' [(n,e) | (n,e)<-s, not $ n `elem` ns] e
subst' s (Exists ns e) = Exists ns $ subst' [(n,e) | (n,e)<-s, not $ n `elem` ns] e
subst' s (Bind c ns e) = Bind c ns $ subst' [(n,e) | (n,e)<-s, not $ n `elem` ns] e
subst' s (App e1 e2)   = App (subst' s e1) (subst' s e2)
subst' s (T es)        = T $ map (subst' s) es

maxVar :: Exp -> Integer
maxVar (C op)        = -1
maxVar (Var (n,i))   = i
maxVar (Forall ns e) = foldr max (maxVar e) (map snd ns)
maxVar (Exists ns e) = foldr max (maxVar e) (map snd ns)
maxVar (Bind c ns e) = foldr max (maxVar e) (map snd ns)
maxVar (App e1 e2)   = max (maxVar e1) (maxVar e2)
maxVar (T es)        = foldr max (-1) (map maxVar es)

incVar :: [String] -> Integer -> Exp -> Exp
incVar vs j (C op)        = C op
incVar vs j (Var (n,i))   = Var $ if n `elem` vs then (n,i+j) else (n,i)
incVar vs j (Forall ns e) = incVars Forall vs j ns e
incVar vs j (Exists ns e) = incVars Exists vs j ns e
incVar vs j (Bind c ns e) = incVars (Bind c) vs j ns e
incVar vs j (App e1 e2)   = App (incVar vs j e1) (incVar vs j e2)
incVar vs j (T es)        = T $ map (incVar vs j) es

incVars c vs j ns e = c (map (\(v,i)->(v,i+j)) ns) (incVar (vs `u` map fst ns) j e)

unify :: Subst -> Subst -> Maybe Subst
unify (ne:s1) s2 = case unify s1 s2 of
  Nothing  -> Nothing
  Just s2' -> if consistent s2 ne then Just (ne:s2') else Nothing
unify [] s2 = Just s2

unifyMaybe (Just s1) (Just s2) = unify s1 s2
unifyMaybe _         _         = Nothing

consistent :: Subst -> (Name, Exp) -> Bool
consistent s (n,e) = and [e==e' | (n',e')<-s, n'==n]

match :: [Name] -> Exp -> Exp -> Maybe Subst
match ns e1 e2 = match' [] ns e1 e2
match' ps ns (C op)        (C op')          = if op == op' then Just emptySubst else Nothing
match' ps ns (Forall xs e) (Forall xs' e')  = matchq' ps ns xs e xs' e'
match' ps ns (Exists xs e) (Exists xs' e')  = matchq' ps ns xs e xs' e'
match' ps ns (Bind c xs e) (Bind c' xs' e') = if c==c' then matchq' ps ns xs e xs' e' else Nothing
match' ps ns (App e1 e2)   (App e1' e2')    = matchs' ps ns [e1,e2] [e1',e2']
match' ps ns (T es)        (T es')          = matchs' ps ns es es'
match' ps ns (T es)        (C (TC cs))      = matchs' ps ns es (map C cs)
match' ps ns (Var x)       (Var x')         = if (x==x' && not (x `elem` ns)) || ((x,x') `elem` ps) then Just emptySubst
                                              else if (x `elem` ns) then Just [(x, Var x')] else Nothing
match' ps ns (Var x)       e2               = if Var x == e2 && not (x `elem` ns) then Just emptySubst
                                              else if (x `elem` ns) then Just [(x, e2)] else Nothing
match' ps ns _             _                = Nothing

matchq' ps ns xs e xs' e' =
  let ps' = [(x,x') | (x,x')<-ps, not (x `elem` xs || x' `elem` xs')]
  in if xs `eql` xs' then
       match' ps' (ns `diff` xs) e e'
     else if length xs == length xs' then 
       match' (ps'++(zip xs xs')) ((ns `diff` xs) `diff` xs') e e'
     else Nothing

matchs' ps ns es es' =
  if length es == length es' then
    foldl unifyMaybe (Just []) (map (uncurry $ match' ps ns) (zip es es'))
  else
    Nothing

--eof
