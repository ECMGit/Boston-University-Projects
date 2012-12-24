----------------------------------------------------------------
--
-- Ibis (light version: Haskell syntax, limited capability)
-- Copyright (C) 2008-2009
--
-- This software is made available under the GNU GPLv3.
--
-- Exp.hs
--   Representation of arithmetic and logical expressions.

----------------------------------------------------------------
-- 

module Exp where

import Data.Maybe (catMaybes)
import Data.List (partition, sort, sortBy)

import Set
import Const

-- Variable names consist of the user-defined string, and an
-- integer identifier unique to each binding.

type Name = (String, Integer)
data Exp =
    C Const
  | Var Name
  | App Exp Exp
  | T [Exp]
  | Forall [Name] Exp
  | Exists [Name] Exp
  | Bind Const [Name] Exp
    deriving (Show, Eq, Ord)

----------------------------------------------------------------
-- Convenient constructors to be used in parsers and elsewhere.

bOp op e1 e2 =  App (C op) (T [e1,e2])
listAnd es = if es==[] then C$B True else foldr (bOp And) (last es) (init es)

mkTup es = if length es == 1 then head es else T es
mkVect e = case e of T es -> T es; _ -> T [e]
mkBrack Round Round e = e
mkBrack b1 b2 e = App (C $ Brack b1 b2) e

----------------------------------------------------------------
-- Map and specialized search functions on expressions.

mapExp :: (Exp -> Exp) -> Exp -> Exp
mapExp f e = f (mpe f e)

mapExpPre :: (Exp -> Exp) -> Exp -> Exp
mapExpPre f e = mpe f (f e)

mpe :: (Exp -> Exp) -> Exp -> Exp
mpe f (C op)        = C op
mpe f (Var n)       = Var n
mpe f (App e1 e2)   = App (mapExp f e1) (mapExp f e2)
mpe f (T es)        = T (map (mapExp f) es)
mpe f (Forall ns e) = Forall ns (mapExp f e)
mpe f (Exists ns e) = Exists ns (mapExp f e)
mpe f (Bind c ns e) = Bind c ns (mapExp f e)

----------------------------------------------------------------
-- Equality on expressions, open form (non-recursive).

eqOpen :: (Exp -> Exp -> Bool) -> Exp -> Exp -> Bool
eqOpen eq (C c)         (C c')           = c == c'
eqOpen eq (Var n)       (Var n')         = n == n'
eqOpen eq (App e1 e2)   (App e1' e2')    = eqOpens eq [e1,e2] [e1',e2']
eqOpen eq (T es)        (T es')          = eqOpens eq es es'
eqOpen eq (Forall ns e) (Forall ns' e')  = (ns `eql` ns') && (e `eq` e')
eqOpen eq (Exists ns e) (Exists ns' e')  = (ns `eql` ns') && (e `eq` e')
eqOpen eq (Bind c ns e) (Bind c' ns' e') = c == c' && (ns `eql` ns') && eq e e'
eqOpen eq _             _                = False

eqOpens :: (Exp -> Exp -> Bool) -> [Exp] -> [Exp] -> Bool
eqOpens eq (e:es) (e':es') = e `eq` e' && eqOpens eq es es'
eqOpens eq []     []       = True
eqOpens eq _      _        = False

----------------------------------------------------------------
-- Free variable determination.

fv :: [Name] -> Exp -> [Name]
fv bound (C op)        = []
fv bound (Var n)       = [n] `diff` bound
fv bound (App e1 e2)   = foldl u [] $ map (fv bound) [e1,e2]
fv bound (T es)        = foldl u [] $ map (fv bound) es
fv bound (Forall ns e) = fv (bound `u` ns) e
fv bound (Exists ns e) = fv (bound `u` ns) e
fv bound (Bind c ns e) = fv (bound `u` ns) e

----------------------------------------------------------------
-- Generation of unique variable names.

updName :: Name -> Integer -> Name
updName (s,i) i' = (s,i')

uv :: [(Name,Integer)] -> Integer -> Exp -> (Exp, Integer)
uv fvs c (C op)  = (C op, c)
uv fvs c (Var n) = case lookup n fvs of
  Nothing -> (Var n, c)
  Just i' -> (Var $ updName n i', c)
uv fvs c (App e1 e2) = (App e1' e2', c')
  where ([e1',e2'], c') = uvs fvs c [e1,e2]
uv fvs c (T es) = (T es', c') where (es', c') = uvs fvs c es
uv fvs c (Forall ns e) = uvq fvs c Forall ns e
uv fvs c (Exists ns e) = uvq fvs c Exists ns e
uv fvs c (Bind o ns e) = uvq fvs c (Bind o) ns e

uvs fvs c [] = ([],c)
uvs fvs c (e:es) = (e':es',c'') where
  (e',c') = uv fvs c e
  (es',c'') = uvs fvs c' es

uvq fvs c q ns e = (q ns' e', c'') where
  c' = c + (toInteger $ length ns)
  fvs' = zip ns [c..c']
  (e',c'') = uv (fvs'++fvs) c' e
  ns' = map (\(v,n) -> updName v n) fvs'

----------------------------------------------------------------
-- Common decomposition operations for expressions.

splitAnd :: Exp -> [Exp]
splitAnd (App (C And) (T [e1,e2])) = splitAnd e1 ++ splitAnd e2
splitAnd e                         = [e]

splitInExps :: Exp -> ([Exp], [Exp])
splitInExps (e@(App (C Imp) (T [e1,e2]))) =
  let (es,rest) = partition expIn $ splitAnd e1
  in if length rest > 0 then (es, [App (C Imp) (T [listAnd rest,e2])])
     else (es, [e2])
splitInExps e = partition expIn $ splitAnd e

splitInExps' (e@(App (C Imp) (T [e1,e2]))) =
  let (es,rest) = partition expIn $ splitAnd e1
  in if length rest > 0 then (es, [App (C Imp) (T [listAnd rest,e2])])
     else (es, [e2])
splitInExps' e = partition expIn $ splitAnd e

expIn (App (C In) e) = True
expIn _              = False

isTrue (C (B True)) = True
isTrue _ = False
isFalse (C (B False)) = True
isFalse _ = False

cLTEeSort :: [Exp] -> [Exp]
cLTEeSort = sortBy f where
  f (C c) _ = LT
  f _ _ = GT

normOps = mapExp (\e->e)

----------------------------------------------------------------
-- We enforce representation invariants that make searching
-- and other operations more easy to define on expressions.

mkImplies e1 (App (C Imp) (T [e,e'])) = bOp Imp (bOp And e1 e) e'
mkImplies e1 e2                       = bOp Imp e1 e2

----------------------------------------------------------------
-- Quantifier construction and normalization.

mknts :: [Name] -> [(Name,Maybe Exp)]
mknts = map $ \n -> (n,Nothing)

mkForall = simplify (liftQ Forall) Imp
liftQ q ns = merge.merge.merge.merge.merge.(q ns) 

simplify q o ((n, Just t) :nts)  e = q [n] $ bOp o (bOp In (Var n) t) (simplify q o nts e)
simplify q o ((n, Nothing):nts)  e = q [n] $ simplify q o nts e
simplify q o []                  e = e

merge (Forall [n] (Forall ns e)) = Forall (n:ns) e
merge (Forall ns e) = reduce Forall ns e
merge e = e

reduce' q ns e = if ns==[] then e else q ns e
reduce q ns e = if ns'==[] then e else reduce' q ns' e
                where ns' = fv [] e `isect` ns

--eof
