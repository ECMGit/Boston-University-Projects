----------------------------------------------------------------
-- Computer Science 320 (Fall, 2009)
-- Concepts of Programming Languages
--
-- Assignment 5 Solutions
--   Unify.hs

----------------------------------------------------------------

-- Note: deductions should never cascade. If an implementation
-- of a function is correct, it should receive full credit
-- even if it does not work correctly because a function on
-- on which it depends is implemented incorrectly.

module Unify 

-- Problem 3, Part C and last comment in Problem 5
-- Partial credit is allowed. No credit should be given if
-- module exports any but the functions listed below.

  (emp, sub, get, Subst, 
   Substitutable, subst, vars,
   Unifiable, unify, combine, resolve, unifySubst)

  where

-- These imports are not required for full credit.
import Data.Maybe (catMaybes)
import Data.List (nub)

----------------------------------------------------------------
-- Problem 1

type Subst a = [(String, a)]

-- Part A

emp :: Subst a
emp = []

sub :: String -> a -> Subst a
sub s x = [(s, x)]

-- Part B
-- We use a library function that behaves the same way as "get".

get :: String -> Subst a -> Maybe a
get = lookup

----------------------------------------------------------------
-- Problem 2

-- Part A
-- These two functions are not defined within the class
-- declaration, so only their types are specified.

class Substitutable a where
  subst :: Subst a -> a -> a
  vars :: a -> [String]

-- Part B
-- We obtain the list of pairs in which the second component
-- contains some variables (as determined by "vars"). If this
-- list is empty, then there are no free variables and the
-- substitution is indeed solved.

  solved :: Subst a -> Bool
  solved s = null [(x,d) | (x,d) <- s, not (null (vars d))]

-- Part C
-- We obtain the list of pairs in which the second component
-- contains no variables, and build a new substitution from it.
-- We then apply this substitution to all the values in the
-- input substitution and return the result.

  reduce :: Subst a -> Subst a
  reduce s = [(x, subst s' d) | (x,d) <- s]
               where s' = [(x,d) | (x,d) <- s, null (vars d)]

----------------------------------------------------------------
-- Problem 3

-- Part A
-- No credit should be given if "Substitutable a"
-- is not one of the constraints for class membership.

class (Eq a, Substitutable a) => Unifiable a where
  unify :: a -> a -> Maybe (Subst a)

-- Part B
-- We concatenate the two lists inside the two substitutions
-- if they exist; otherwise, we return "Nothing".

  combine :: Maybe (Subst a) -> Maybe (Subst a) -> Maybe (Subst a)
  combine (Just s) (Just s') = Just (s ++ s')
  combine _        _         = Nothing

-- Part C
-- See "Unify" module declaration at the top of this file.

----------------------------------------------------------------
-- Problem 5

-- Repetition is accomplished using recursion. At each step,
-- we call "reduce", get rid of any duplicates using "nub",
-- and check if anything has changed. If it has, we repeat
-- the process again. If not, we stop.

-- We use a list comprehension to generate the list of pairs
-- that conflict for a given element. If the variables are
-- equivalent, the value to which they map must also be
-- equivalent.

-- Partial credit should be given for functions that only work
-- in some or most cases.

-- Partial credit should be given if only some of the steps
-- are missing (for example, if the conflicting pairs were
-- not first removed, or the recursive call is not made).

  resolve :: Maybe (Subst a) -> Maybe (Subst a)
  resolve Nothing = Nothing
  resolve (Just s) = if noConflict s' && solved s' then Just s' 
                     else Nothing
    where
      s' = repeat s
      repeat s =
        let s' = nub (reduce s)
        in if s' == s then s else repeat s'
      noConflict s = 
        and [x /= x' || d == d' | (x,d) <- s, (x',d') <- s]

----------------------------------------------------------------
-- Optional fix for unification algorithm (not required).

unifySubst :: Unifiable a => Maybe (Subst a) -> Maybe (Subst a)
unifySubst Nothing = Nothing
unifySubst (Just s) =
  let u = [unify v w | (x,v)<-s, (y,w)<-s, x==y]
      s' = nub (s ++ concat (catMaybes u))
  in if Nothing `elem` u then Nothing 
     else (if s==s' then id else unifySubst) (Just s')
----------------------------------------------------------------

--eof
