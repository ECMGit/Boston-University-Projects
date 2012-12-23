----------------------------------------------------------------
-- Computer Science 320 (Fall, 2009)
-- Concepts of Programming Languages
--
-- Assignments 6, 7, and 8
--   Ty.hs

----------------------------------------------------------------
-- Syntax for Mini-Haskell types 

module Ty (typeCheck) 
  where

import Err
import Env
import Unify
import Exp (Exp(..), Oper(..), showTuple)
import Val

data Ty = TyVar String
        | TyBool
        | TyInt
        | TyTuple [Ty]
        | TyList Ty
        | Ty `TyArrow` Ty
  deriving Eq

----------------------------------------------------------------
-- This function is exported to the Main module.

typeCheck :: Exp -> Error Ty
typeCheck e =
  case ty emptyEnv freshTyVars e of
    Error msg -> Error msg
    S (t, s, fvs) ->
      case resolve (unifySubst (Just s)) of
        Just s' -> S (subst s' t)
        Nothing -> Error "type checking failed"

-- The following was the intended solution for Assignment 6.
--
-- Assignment 6, Problem 3, Part D
-- typeCheck :: Exp -> Error Ty
-- typeCheck e =
--   case ty1 emptyEnv e of
--     Error msg -> Error msg
--     S t -> S t

----------------------------------------------------------------
-- Type checking with no variables or bindings.

-- Assignment 6, Problem 3, Part A
tyOp :: Oper -> Ty
tyOp Plus =  TyArrow TyInt (TyArrow TyInt TyInt)
tyOp Times = TyArrow TyInt (TyArrow TyInt TyInt)
tyOp Equal = TyArrow TyInt (TyArrow TyInt TyBool)
tyOp And   = TyArrow TyBool (TyArrow TyBool TyBool)
tyOp Or    = TyArrow TyBool (TyArrow TyBool TyBool)
tyOp Not   = TyArrow TyBool TyBool
tyOp Head  = TyArrow (TyList TyInt) TyInt
tyOp Tail  = TyArrow (TyList TyInt) (TyList TyInt)
tyOp Cons  = TyArrow TyInt (TyArrow (TyList TyInt) (TyList TyInt))

-- Assignment 6, Problem 3, Part B
ty0 :: Exp -> Error Ty
ty0 Nil = S $ TyList TyInt
ty0 (N _) = S TyInt
ty0 (B _) = S TyBool
ty0 (Op op) = S (tyOp op)
ty0 (If e1 e2 e3) =
  case (ty0 e1, ty0 e2, ty0 e3) of
    (Error msg, _, _) -> Error msg
    (_, Error msg, _) -> Error msg
    (_, _, Error msg) -> Error msg
    (S t1, S t2, S t3) -> 
      if (t1 == TyBool) && (t2 == t3) then
        S t2
      else
        Error "'if' type mismatch"

-- Note that even if both subexpressions have types,
-- if the type of the function is not an arrow type, we
-- must return an error.

ty0 (App e1 e2) =
  case (ty0 e1, ty0 e2) of
    (Error msg, _) -> Error msg
    (_, Error msg) -> Error msg
    (S (TyArrow t1 t2), S t1') ->
      if (t1 == t1') then
        S t2
      else
        Error "function applied to argument of wrong type."
    (S _, _) -> Error "non-function applied to argument."

ty0 (Tuple es) =
  case mapError ty0 es of
    S ts' -> S $ TyTuple ts'
    Error msg -> Error msg

----------------------------------------------------------------
-- Basic type-checking algorithm for expressions with variables
-- and bindings.

-- Assignment 6, Problem 3, Part C
ty1 :: Env Ty -> Exp -> Error Ty
ty1 gamma Nil = S $ TyList TyInt
ty1 gamma (N _) = S TyInt
ty1 gamma (B _) = S TyBool
ty1 gamma (Op op) = S (tyOp op)

ty1 gamma (Var x) =
  case (findEnv x gamma) of
    Just x' -> S x'
    Nothing -> Error $ "unbound variable: " ++ x

ty1 gamma (If e1 e2 e3) =
  case (ty1 gamma e1, ty1 gamma e2, ty1 gamma e3) of
    (Error msg, _, _) -> Error msg
    (_, Error msg, _) -> Error msg
    (_, _, Error msg) -> Error msg
    (S t1, S t2, S t3) -> 
      if (t1 == TyBool) && (t2 == t3) then
        S t2
      else
        Error "'if' type mismatch"

-- Note that even if both subexpressions have types,
-- if the type of the function is not an arrow type, we
-- must return an error.

ty1 gamma (App e1 e2) =
  case (ty1 gamma e1, ty1 gamma e2) of
    (Error msg, _) -> Error msg
    (_, Error msg) -> Error msg
    (S (TyArrow t1 t2), S t1') ->
      if (t1 == t1') then
        S t2
      else
        Error "function applied to argument of wrong type."
    (S _, _) -> Error "non-function applied to argument."

ty1 gamma (Tuple es) =
  case mapError (ty1 gamma) es of
    S ts' -> S $ TyTuple ts'
    Error msg -> Error msg

ty1 gamma (Let [x] e be) =
    case (ty1 gamma e) of
      Error err -> Error err
      S t       -> ty1 (updEnv x t gamma) be

-- Assignment 6, Problem 4, Part E

-- We define a helper function, "ty1s", below that can
-- determine the types of a list of expressions.

ty1 gamma (Let xs e be) =
  case (ty1 gamma e) of
    Error err -> Error err
    S (TyTuple ts) ->
      if length xs /= length ts then
          Error "tuples of mismatched lengths"
      else
          ty1 (updEnvL (zip xs ts) gamma) be

----------------------------------------------------------------
-- Type substitution and unification

-- Assignment 6, Problem 4, Part F
instance Substitutable Ty where
  vars (TyVar x) = [x]
  vars (TyArrow t1 t2) = vars t1 ++ vars t2
  vars (TyList t) = vars t
  vars _ = []

  subst s (TyArrow t1 t2) = TyArrow (subst s t1) (subst s t2)
  subst s (TyTuple ts)    = TyTuple $ map (subst s) ts
  subst s (TyList t)      = TyList $ subst s t
  subst s (TyVar x)       = case get x s of Nothing -> TyVar x
                                            Just t -> t
  subst s t               = t -- base: TyBool, TyInt

-- Assignment 6, Problem 4, Part G
instance Unifiable Ty where
  unify (TyVar x)  t                      = Just $ sub x t
  unify t               (TyVar x)         = Just $ sub x t
  unify TyBool          TyBool            = Just emp
  unify TyInt           TyInt             = Just emp
  unify (TyList t)      (TyList t')       = unify t t'

  unify (TyArrow t1 t2) (TyArrow t1' t2') =
    combine (unify t1 t1') (unify t2 t2')

  unify (TyTuple ts)    (TyTuple ts') =
    foldr combine (Just emp) $ map (\(x,y) -> unify x y) (zip ts ts')

  unify t1              t2            =
    if t1 == t2 then Just emp else Nothing

----------------------------------------------------------------
-- Infinite List of Fresh Type Variables

-- Assignment 6, Problem 4, Part H
type FreshVars = [Ty]

-- We return an infinite list of type variables, where each
-- variable is of the form (TyVar "t0"), (TyVar "t1"), etc.
freshTyVars :: FreshVars
freshTyVars = fvs 0
  where fvs n = TyVar ("t" ++ (show n)):(fvs (n+1))

----------------------------------------------------------------
-- General Type-checking Algorithm

ty :: Env Ty -> FreshVars -> Exp -> Error (Ty, Subst Ty, FreshVars)

-- Assignment 7, Problem 3, Part A
-- Below, the types returned permit different types of lists.
-- It is okay to assume that all lists are lists of integers,
-- in which case no fresh variables need to be used (the "fv"
-- instances can just be "TyInt").

ty gamma (fv:fvs) Nil       = S (TyList fv, emp, fvs)
ty gamma (fv:fvs) (Op Cons) = S (TyArrow fv (TyArrow (TyList fv) (TyList fv)), emp, fvs)
ty gamma (fv:fvs) (Op Head) = S (TyArrow (TyList fv) fv, emp, fvs)
ty gamma (fv:fvs) (Op Tail) = S (TyArrow (TyList fv) (TyList fv), emp, fvs)
ty gamma fvs (N n)          = S (TyInt, emp, fvs)
ty gamma fvs (B b)          = S (TyBool, emp, fvs)
ty gamma fvs (Op Plus)      = S (TyArrow TyInt (TyArrow TyInt TyInt), emp, fvs)
ty gamma fvs (Op Times)     = S (TyArrow TyInt (TyArrow TyInt TyInt), emp, fvs)
ty gamma fvs (Op Equal)     = S (TyArrow TyInt (TyArrow TyInt TyBool), emp, fvs)
ty gamma fvs (Op And)       = S (TyArrow TyBool (TyArrow TyBool TyBool), emp, fvs)
ty gamma fvs (Op Or)        = S (TyArrow TyBool (TyArrow TyBool TyBool), emp, fvs)
ty gamma fvs (Op Not)       = S (TyArrow TyBool TyBool, emp, fvs)

-- Assignment 7, Problem 3, Part B

ty gamma fvs (If e1 e2 e3) =
  case (tys gamma fvs [e1,e2,e3]) of
    Error msg -> Error msg
    S ([t1, t2, t3], s, fvs') ->
      case (Just s) `combine` (unify t1 TyBool `combine` unify t2 t3) of
        Nothing -> Error "unification error"
        Just s'' -> S (t2, s'', fvs')

-- Assignment 7, Problem 3, Part C

ty gamma fvs (Tuple es) =
  case (tys gamma fvs es) of
    Error msg -> Error msg
    S (ts, s, fvs') -> S (TyTuple ts, s, fvs')

-- Assignment 7, Problem 3, Part D

ty gamma (fv:fvs') (Lam [x] e) =
  case (ty (updEnv x fv gamma) fvs' e) of
    Error msg -> Error msg
    S (t, s, fvs'') -> S (TyArrow fv t, s, fvs'')

-- We add a case for unit. This is not required.

ty gamma fvs (Lam [] e) =
  case (ty gamma fvs e) of
    Error msg -> Error msg
    S (t, s, fvs') -> S (TyArrow (TyTuple []) t, s, fvs')

-- We simply look up the variable's type in the environment.

ty gamma fvs (Var x) =
  case (findEnv x gamma) of
    Nothing -> Error $ "unbound var. "++x
    Just t  -> S (t, emp, fvs)

-- We first determine the type of the bound expression "e",
-- and then update the environment so that the bound variable
-- has its type. Then, we check the type of the body. If it
-- succeeds, we make sure the constraints returned by both
-- recursive calls unify, and return the type of the body as
-- the result.

ty gamma fvs (Let [x] e be) =
  case (ty gamma fvs e) of
    Error msg       -> Error msg
    S (t, s, fvs')  -> 
      case (ty (updEnv x t gamma) fvs' be) of
        Error msg -> Error msg
        S (t, s', fvs'') ->
          case (Just s) `combine` (Just s') of
            Nothing -> Error "unification error"
            Just s -> S (t, s, fvs'')

-- We first obtain the types of the two subexpressions. Next, 
-- we unify the type of the first expression with a function
-- type whose return type is represented by a fresh variable
-- (since we have no way of knowing the return type), and we
-- combine this solution with the substitution we got from the
-- recursive call to the type-checking algorithm. If this
-- succeeds, we return the type of the result (the fresh
-- variable) along with the new substitution.

ty gamma (fv:fvs) (App e1 e2) =
  case (tys gamma fvs [e1, e2]) of
    Error msg -> Error msg
    S ([t1, t2], s, fvs') ->
      case (unify t1 (TyArrow t2 fv) `combine` (Just s)) of
        Nothing -> Error "unification error"
        Just s' -> S (fv, s', fvs')

-- Helper function for determining multiple types
tys :: Env Ty -> FreshVars -> [Exp] -> Error ([Ty], Subst Ty, FreshVars)
tys gamma fvs []     = S ([], emp, fvs)
tys gamma fvs (e:es) =
  case tys gamma fvs es of
    Error msg -> Error msg
    S (ts, s, fvs') ->
      case ty gamma fvs' e of
        Error msg -> Error msg
        S (t, s', fvs'') ->
          case Just s `combine` Just s' of
            Nothing -> Error "unification failure"
            Just s'' -> S (t:ts, s'', fvs'')

----------------------------------------------------------------
-- Printing functions for Syntax of Types

instance Show Ty where
  show (TyVar s)   = s
  show TyBool      = "Bool"
  show TyInt       = "Int"
  show (TyList t)  = "[" ++ show t ++ "]"
  show (TyTuple ts) = showTuple (map show ts)

  -- If the left argument of an arrow is an arrow, we need to
  -- add parentheses to make sure the type is not ambiguous.
  show (TyArrow (TyArrow t1 t2) t3) = 
                "("++show (TyArrow t1 t2)++") -> "++show t3
  show (TyArrow t1 t2) = show t1++" -> "++show t2

--eof
