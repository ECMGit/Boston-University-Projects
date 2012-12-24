----------------------------------------------------------------
-- Computer Science 320 (Fall, 2009)
-- Concepts of Programming Languages
--
-- Assignments 6, 7, and 8
--   Eval.hs

----------------------------------------------------------------
-- Evaluation for Mini-Haskell

module Eval (evalExp,ev0,ev) where

import Env
import Err
import Exp
import Val
import Unify

----------------------------------------------------------------
-- This function is exported to the Main module.

-- Assignment 6, Problem 2, Part B

evalExp :: Exp -> Error Val
evalExp e =

-- Either of the two definitions below are fine in non-bonus
-- solutions.
  --ev emptyEnv e
  ev0 e

-- Assignment 7, Problem 6, Part B
-- This is one valid bonus solution. However, there are others
-- that may not require explicit modification to "evalExp".

  --ev0 $ fst (unique (fv 0) emptyEnv e)
  --where fv n = ("v"++show n):(fv $ n+1) --fresh variable list

----------------------------------------------------------------
-- Functions for evaluating operations applied to values.

-- Assignment 6, Problem 1, Part A
appOp :: Oper -> Val -> Error Val
appOp Not  (VB b)         = S $ VB $ not b
appOp Head (VList (v:vs)) = S $ v
appOp Tail (VList (v:vs)) = S $ VList vs
appOp Head _              = Error "head applied to empty list"
appOp Tail _              = Error "tail applied to empty list"
appOp Not  _              = Error "not applied to non-boolean"
appOp op v2               = S $ Partial op v2

-- Assignment 6, Problem 1, Part B
appBinOp :: Oper -> Val -> Val -> Error Val
appBinOp Plus  (VN n) (VN n') = S $ VN (n + n')
appBinOp Times (VN n) (VN n') = S $ VN (n * n')
appBinOp Equal (VN n) (VN n') = S $ VB (n == n')
appBinOp And   (VB b) (VB b') = S $ VB (b && b')
appBinOp Or    (VB b) (VB b') = S $ VB (b || b')
appBinOp Cons  v      (VList vs) = S $ VList (v:vs)
appBinOp Cons  v      VNil    = S $ VList (v:[])
appBinOp op v v' =
  Error $ "binary operator " ++ show op 
           ++ "not defined on arguments " 
           ++ (show v) ++ " and " ++ (show v')

----------------------------------------------------------------
-- Function for applying one value to another.

-- Assignment 6, Problem 1, Part C
appVals :: Val -> Val -> Error Val
appVals (VOp op)           v2     = appOp op v2
appVals (Partial op v1 )   v2     = appBinOp op v1 v2

-- Assignment 7, Problem 1, Part A
appVals (VLam [x] e env)   v2     = ev (updEnv x v2 env) e

-- Assignment 7, Problem 4
-- If the number of variables in the lambda abstraction is
-- not one, the value must be a tuple; otherwise, we have
-- an error, and this will be caught by the last case for
-- "appVals", further below.

appVals (VLam xs e env) (VTuple vs) =
      if length xs /= length vs then
          Error "tuples of mismatched lengths"
      else
          ev (updEnvL (zip xs vs) env) e

appVals v1 v2 = Error $ (show v1)
                        ++ " cannot be applied to " ++ show v2

-- Assignment 7, Problem 2, Part B
-- We are careful to avoid evaluating the second argument
-- wherever it is unnecessary to do so (as specified by the
-- call-by-name evaluation rules).

appValExp :: Val -> Exp -> Error Val
appValExp (VOp op) e2 =
  case ev0 e2 of
    S v2      -> appVals (VOp op) v2
    Error msg -> Error msg

appValExp (Partial Or  (VB True )) e2 = S (VB True)
appValExp (Partial And (VB False)) e2 = S (VB False)
appValExp (Partial op  v1)         e2 =
  case ev0 e2 of
    S v2      -> appVals (Partial op v1) v2
    Error msg -> Error msg

appValExp (VLam [x] e env) e2 = ev0 $ subst (sub x e2) e

-- Assignment 7, Problem 5, Part B
-- This extra credit problem should be graded strictly.

-- Here, for unit lambda abstractions, we don't evaluate
-- the second argument; for variable lambda abstractions,
-- we perform the substitution, then evaluate the result
-- of the substitution.

appValExp (VLam [] e env) e2 = ev0 e

-- For tuples of arbitrary length, this problem is very
-- difficult. One option is to take advantage of the lazy
-- evaluation of full Haskell, as in the example below.
-- Solutions that don't work on all examples (e.g. only on
-- those in which "e" is a tuple) should get at most half
-- credit.

-- For full credit solutions must TERMINATE on the 
-- following example code (evaluating to "0"):
--
--      (\(x,y) -> 0 ) ((\f -> f f) (\f -> f f))

appValExp (VLam xs e env) e2 =
  let vToExp VNil = Nil
      vToExp (VN i) = N i
      vToExp (VB b) = B b
      vToExp (VOp op) = Op op
      vToExp (Partial op v) = App (Op op) $ vToExp v
      vToExp (VTuple vs) = Tuple $ map vToExp vs
      -- All closure environments are empty under CBN.
      vToExp (VLam xs e []) = Lam xs e
      vToExp (VList vs) =
        let es = map vToExp vs
        in foldr (\e -> \e' -> App (App (Op Cons) e) e') 
                 (last es) (init es)

      -- A bit of a hack, but effective.
      nthVTup i (S (VTuple vs)) = vToExp $ (!!) vs i
      nthVTup i (Error msg)     = Var $ "tuple error: "++msg

      checkLength (S (VTuple vs)) =
        if length xs /= length vs then Error "tuple lengths"
        else S $ VTuple vs
      checkLength (Error s) = Error s
      checkLength _ = Error "tuple lambda applied to non-tuple"

      subAll i (x:xs) vs = subst (sub x (nthVTup i vs)) (subAll (i+1) xs vs)
      subAll i []     vs = e

  in ev0 $ subAll 0 xs $ checkLength $ ev0 e2

appValExp v1 e2 = Error $ show v1
                          ++ " cannot be applied to an argument"

----------------------------------------------------------------
-- Function for evaluating an expression with no bindings or
-- variables to a value.

-- Assignment 6, Problem 1, Part D
ev0 :: Exp -> Error Val
ev0 Nil     = S VNil
ev0 (N n)   = S (VN n)
ev0 (B b)   = S (VB b)
ev0 (Op op) = S (VOp op)

ev0 (If e1 e2 e3) =
  case (ev0 e1) of
    S (VB c)  -> if c then ev0 e2 else ev0 e3
    S _       -> Error "'if' condition not a boolean"
    Error err -> Error err

ev0 (Tuple es) = case (mapError ev0 es) of
                   Error msg -> Error msg
                   S vs -> S $ VTuple vs

-- Assignment 7, Problem 2, Part C

ev0 (App e1 e2) =
  case (ev0 e1) of
    Error err -> Error err
    S v1 -> appValExp v1 e2

-- We assume that the expression being bound to the
-- variables is not yet under the scope of those
-- variables (since we don't have recursive definitions
-- in Mini-Haskell). However, solutions that make such
-- an assumption (and thus, substitute variables
-- inside the expression being assigned) are also
-- acceptable.

-- Notice that we supply the empty environment to the
-- closure, as there is no need for environments.

ev0 (Lam xs e)   = S $ VLam xs e emptyEnv
ev0 (Let [x] e be) = ev0 (subst (sub x e) be)

-- Assignment 7, Problem 5, Part C
ev0 (Let xs e be) = ev0 $ App (Lam xs be) e

ev0 e = Error $ "ev0 error: "++show e

----------------------------------------------------------------
-- Function for evaluating an expression to a value. Note the
-- need for an environment to keep track of variables.

-- Assignment 6, Problem 2, Part A
ev :: Env Val -> Exp -> Error Val
ev env Nil     = S VNil
ev env (N n)   = S (VN n)
ev env (B b)   = S (VB b)
ev env (Op op) = S (VOp op)

ev env (Var x) =
  case (findEnv x env) of
    Just x' -> S x'
    Nothing -> Error $ "unbound variable: " ++ x

ev env (App e1 e2) =
  case (ev env e1) of
    Error err -> Error err
    S v1 -> case (ev env e2) of
        Error err -> Error err
        S v2 -> appVals v1 v2

ev env (Tuple es) = case mapError (ev env) es of
                          S vs -> S $ VTuple vs
                          Error msg -> Error msg

ev env (If e1 e2 e3) =
  case (ev env e1) of
    S (VB c)  -> if c then ev env e2 else ev env e3
    S _       -> Error "'if' condition not a boolean"
    Error err -> Error err

-- Assignment 7, Problem 1, Part B
-- Notice that we store the current environment inside the
-- closure. If the closure is applied to an argument at any
-- other point, this environment can then be used to evaluate
-- the body of the lambda abstraction.

ev env (Lam xs e) = S (VLam xs e env)

ev env (Let [x] e be) =
  case (ev env e) of
    Error err -> Error err
    S v       -> ev (updEnv x v env) be

-- Assignment 6, Problem 4, Part B
-- The parser will never produce a tuple of size
-- one, but this code will still work for tuples
-- of size 0,2,3,4,..., so it is sufficient.

ev env (Let xs e be) =
  case (ev env e) of
    Error err -> Error err
    S (VTuple vs) ->
      if length xs /= length vs then
          Error "tuples of mismatched lengths"
      else
          ev (updEnvL (zip xs vs) env) be
    S _ -> Error "cannot assign non-tuple value to tuple"

----------------------------------------------------------------
-- Helper functions for call-by-name evaluation of Mini-Haskell.

-- Assignment 7, Problem 2, Part A
instance Substitutable Exp where

  -- It is actually not important for the assignment how
  -- the "Let" and "Lam" cases are handled here -- any
  -- solution is fine. The one below happens to remove the
  -- bound variables.
  vars (Var x)       = [x]
  vars (If e1 e2 e3) = concat $ map vars [e1,e2,e3]
  vars (App e1 e2)   = vars e1 ++ vars e2
  vars (Lam xs e)    = [v | v <- vars e, not (v `elem` xs)]
  vars (Let xs e e') = vars e ++ [v | v <- vars e', not (v `elem` xs)]
  vars (Tuple es)    = concat $ map vars es
  vars _             = []

  subst s (Var x)     = case get x s of Nothing -> Var x
                                        Just t -> t
  subst s (If e1 e2 e3) = If (subst s e1) (subst s e2) (subst s e3)
  subst s (Tuple es)    = Tuple $ map (subst s) es
  subst s (App e1 e2)   = App (subst s e1) (subst s e2)

  -- Assignment 7, Problem 5, Part A
  -- In our solutions, we will assume that all substitutions
  -- will contain only a single variable. Otherwise, we
  -- would have needed to define in the "Unify" module
  -- a "remove" function. We would then call it here with,
  -- for example, "remove s xs", and then we'd have used
  -- this new substitution in our recursive call on the body
  -- of the lambda abstraction.

  subst s (Lam xs e)    =
    if allNothing (map (\x -> get x s) xs) then
      Lam xs (subst s e)
    else
      Lam xs e 
    where
      allNothing (Nothing:xs) = allNothing xs
      allNothing [] = True
      allNothing _ = False

  -- We assume that the expression being bound to the
  -- variables is not yet under the scope of those
  -- variables (since we don't have recursive definitions
  -- in Mini-Haskell). However, solutions that make such
  -- an assumption are also acceptable.

  subst s (Let xs e be)    =
    if allNothing (map (\x -> get x s) xs) then
      Let xs (subst s e) (subst s be)
    else
      Let xs (subst s e) be
    where
      allNothing (Nothing:xs) = allNothing xs
      allNothing []           = True
      allNothing _            = False

  subst s e             = e -- Base cases: Nil, N i, B b, Op op

-- Assignment 7, Problem 6, Part A
-- We generate a fresh variable at every *binding* point,
-- then propogate the variable down using the environment.
-- There is a way to do this without environments using
-- substitutions, and such solutions should get full credit.

unique :: [String] -> Env String -> Exp -> (Exp, [String])
unique fvs env (Var x) =
  case (findEnv x env) of
    Nothing -> (Var x, fvs)
    Just x' -> (Var x', fvs)

unique fvs env (App e1 e2) =
  let (e1', fvs') = unique fvs env e1
      (e2', fvs'') = unique fvs' env e2
  in 
     (App e1' e2', fvs'')

unique fvs env (Tuple es) =
  let (es',fvs') = uniques fvs env es
  in (Tuple es', fvs')

unique fvs env (If e1 e2 e3) =
  let (e1', fvs') = unique fvs env e1
      (e2', fvs'') = unique fvs' env e2
      (e3', fvs''') = unique fvs'' env e3
  in 
     (If e1' e2' e3', fvs''')

unique (fv:fvs') env (Let [x] e be) =
  let (e', fvs'') = unique fvs' env e
      (be', fvs''') = unique fvs'' (updEnv x fv env) be
  in
     (Let [fv] e' be', fvs''')
     
unique fvs env (Let xs e be) =
  let fvsNew = take (length xs) fvs
      fvsRest = drop (length xs) fvs
      (e', fvsRest') = unique fvsRest env e
      (be', fvsRest'') = unique fvsRest' (updEnvL (zip xs fvsNew) env) be
  in
     (Let fvsNew e' be', fvsRest'')

unique (fv:fvs') env (Lam [x] e) =
  let (e', fvs'') = unique fvs' (updEnv x fv env) e
  in
     (Lam [fv] e', fvs'')

unique fvs env (Lam xs e) =
  let fvsNew = take (length xs) fvs
      fvsRest = drop (length xs) fvs
      (e', fvsRest') = unique fvsRest (updEnvL (zip xs fvsNew) env) e
  in
     (Lam fvsNew e', fvsRest')

unique fvs env e = (e, fvs) -- base cases

uniques :: [String] -> Env String -> [Exp] -> ([Exp], [String])
uniques fvs env (e:es) =
  let (es',fvs') = uniques fvs env es
      (e', fvs'') = unique fvs' env e
  in (e':es', fvs'')
uniques fvs env [] = ([], fvs)

--eof
