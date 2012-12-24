----------------------------------------------------------------
--
-- Ibis (light version: Haskell syntax, limited capability)
-- Copyright (C) 2008-2009
--
-- This software is made available under the GNU GPLv3.
--
-- Relations.hs
--   Representation of relation collection for verification.

----------------------------------------------------------------
-- 

module Relations where
    
import Set
import Const (Const(..))
import Exp (Exp(..), eqOpen)

----------------------------------------------------------------
-- Equivalence classes.

-- This is the data structure used for maintaining a collection
-- of equivalence classes of expressions.

-- It would be preferable to have O(log n) access times to
-- this data structure, so instead of a list, it might be better
-- to have a sorted balanced tree or sorted random-access list.

type Index = Integer
type Equivalence = [(Exp,Index)]

empEquality :: Equivalence
empEquality = []

getEC :: Equivalence -> Exp -> Maybe Index
getEC eis e = lookup e eis

-- A fresh equivalence class index.
freshECIx :: Equivalence -> Index
freshECIx = (+1).(foldr max 0).(map snd)

mergeEC :: Index -> Index -> Equivalence -> Equivalence
mergeEC i1 i2 = map (\(e,i)->(e,if i==i1 then i2 else i))

insertEC :: Exp -> Index -> Equivalence -> Equivalence
insertEC e i ies = (e,i):ies

-- This function takes a list of expressions and determines
-- the corresponding list of equivalence class indices.
getIxs :: [Exp] -> Equivalence -> Maybe [Index]
getIxs [] ies = Just []
getIxs (e:es) ies =
  case getIxs es ies of
    Nothing -> Nothing
    Just is ->
      case getEC ies e of
        Just i -> Just $ i:is
        Nothing -> Nothing

-- This takes a list of expressions and returns the list of
-- indices, but adds any new expressions and generates fresh
-- indices for them.
getIxsWithPut :: [Exp] -> Equivalence -> ([Index], Equivalence)
getIxsWithPut [] ies = ([],ies)
getIxsWithPut (e:es) ies =
  let (is, ies') = getIxsWithPut es ies
  in case getEC ies' e of
    Just i -> (i:is, ies')
    Nothing -> let i' = freshECIx ies' in (i':is, insertEC e i' ies')

-- This adds a new equality relationship to the structure.
putEquivExps :: Exp -> Exp -> Equivalence -> Equivalence
putEquivExps e1 e2 ies =
  case (getEC ies e1, getEC ies e2) of
    (Just i1, Just i2) -> mergeEC i1 i2 ies
    (Just i1, Nothing) -> insertEC e2 i1 ies
    (Nothing, Just i2) -> insertEC e1 i2 ies
    (Nothing, Nothing) -> insertEC e1 i' (insertEC e2 i' ies)
                             where i' = freshECIx ies

-- This simply determines if two expressions are equal according
-- to the data structure.
chkEquality :: Equivalence -> Exp -> Exp -> Bool
chkEquality ies e1 e2 = case getIxs [e1,e2] ies of 
   Just [i1,i2] -> i1==i2
   _ -> False

----------------------------------------------------------------
-- External interface.

type FRel = (Const, [Integer])
type Relations = (Equivalence, [FRel])

empRels :: Relations
empRels = (empEquality, [])

-- This updates the entire data structure with a single
-- expression.
updRels :: Relations -> Exp -> Relations
updRels (eqvcls, rs) (App (C Eql) (T [e1,e2])) = (putEquivExps e1 e2 eqvcls, rs)
updRels rels _ = rels

-- This filters the data structure in case a new
-- variable blocks old expressions.
updVarRels :: (Exp -> Bool) -> Relations -> Relations
updVarRels f (eqvcls, rs) = (eqvcls', rs')
  where eqvcls' = filter (\(e,i) -> f e) eqvcls
        isToRmv = map snd $ filter (\(e,i) -> not (f e)) eqvcls
        rs' = filter (\(c, is) -> isToRmv `isect` is == []) rs

-- This checks the structure for a particular relation instance.
-- This currently takes linear time, this should be much faster.
chkRels :: Relations -> Exp -> Bool
chkRels (eis, rs) (App (C Eql) (T [e1,e2])) = eqChkZ 1 eis e1 e2
chkRels (eis, rs) _ = False

chkEq (eis, rs) e1 e2 = eqChkZ 0 eis e1 e2

----------------------------------------------------------------
-- Recursive equality check.

eqChk :: (Exp -> Exp -> Bool) ->  Exp -> Exp -> Bool
eqChk eq e e' = eqOpen (eqChk eq) e e' || eq e e'

eqChkZ :: Int -> Equivalence -> Exp -> Exp -> Bool
eqChkZ 0 er e1 e2 = eqChk (\e1-> \e2-> (chkEquality er e1 e2) || (e1==e2)) e1 e2
eqChkZ n er e1 e2 = eqChk (\e1-> \e2-> (eqChkZ (n-1) er e1 e2)) e1 e2

--eof
