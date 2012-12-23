----------------------------------------------------------------
-- Computer Science 320 (Fall, 2009)
-- Concepts of Programming Languages
--
-- Assignments 6, 7, and 8
--   Env.hs

----------------------------------------------------------------
-- Environments

module Env (Env, emptyEnv, updEnv, updEnvL, findEnv, mapEnv)
  where

-- We represent environments as lists of association pairs,
-- where each pair indicates that the first component is the
-- variable associated with the second component.
type Env a = [(String, a)]

emptyEnv :: Env a
emptyEnv = []

-- When a new association is added, the old association
-- still exists in the list.
updEnv :: String -> a -> Env a -> Env a
updEnv n x e = (n, x):e

updEnvL :: [(String, a)] -> Env a -> Env a
updEnvL nxs e = foldr (\(n,x)-> \env-> updEnv n x env) e nxs

-- We simply search the list and return the value for the
-- first matching string.
findEnv :: String -> Env a -> Maybe a
findEnv n' ((n,x):nxs) =
  if n' == n then
    Just x
  else
    findEnv n' nxs
findEnv n' [] = Nothing

-- Useful function for transforming environment (used for
-- applying substitutions to environment).
mapEnv :: (a -> b) -> Env a -> Env b
mapEnv s ((x, t):xts) = (x, s t):(mapEnv s xts)
mapEnv s [] = []

--eof 
