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
--import Unify
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

-- Assignment 6, Problem 3, Part D
typeCheck :: Exp -> Error Ty
typeCheck e =
  case ty0 e of
    Error msg -> Error msg
    S t -> S t

----------------------------------------------------------------
-- Type checking with no variables or bindings.

-- Assignment 6, Problem 3, Part A
tyOp :: Oper -> Ty
tyOp Plus = TyInt
tyOp Times = TyInt
tyOp Equal = TyInt
tyOp And = TyBool
tyOp Or = TyBool
tyOp Not = TyBool
tyOp Cons = TyInt
tyOp Head = TyInt
tyOp Tail = TyInt
tyOp _ =  TyVar "That operator isnt defined."

-- Assignment 6, Problem 3, Part B
ty0 :: Exp -> Error Ty
ty0 (Op x) = S (tyOp x)
ty0 (N x) = S (TyInt)
ty0 (B x) = S (TyBool)
ty0 (If x y z) = if (((g (ty0 x)) == TyBool) && ((g (ty0 y)) == (g (ty0 z)))) then (ty0 y) else Error "Unable to determine type"
ty0 (App x y) = if ((g (ty0 x)) == (g (ty0 y))) then (ty0 y) else Error "Unable to determine type"
ty0 _ = Error "Unable to determine type"

g :: Error a -> a
g (S z) = z
----------------------------------------------------------------
-- Basic type-checking algorithm for expressions with variables
-- and bindings.

-- Assignment 6, Problem 3, Part C
ty1 :: Env Ty -> Exp -> Error Ty
--ty1 e (
ty1 _ _ = Error "Assignment 6, Problem 3(c) Not Yet Implemented"

----------------------------------------------------------------
-- Type substitution and unification

-- Assignment 6, Problem 4, Part F
--instance Substitutable Ty where

-- Assignment 6, Problem 4, Part G
--instance Unifiable Ty where

----------------------------------------------------------------
-- Infinite List of Fresh Type Variables

-- Assignment 6, Problem 4, Part H
type FreshVars = [Ty]

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
