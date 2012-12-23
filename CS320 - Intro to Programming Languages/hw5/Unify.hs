module Unify (emp,sub,get, Subst, Substitutable,Unifiable,subst,vars,unify,combine, resolve) where
import List

type Subst a = [(String, a)]

emp::Subst a
emp = []

sub::String -> a -> Subst a
sub str a = [(str, a)]

get::String -> Subst a -> Maybe a
get _ [] = Nothing
get var ((x, y):xs)  = if (var == x) then Just y
    	     	       else get var xs

class Substitutable a where
      subst::Subst a -> a -> a
      vars::a -> [String]
      
      solved::Subst a -> Bool
      solved [] = True
      solved ((_, x):xs) = if (vars x == []) then solved xs else False
      	       
      reduce::Subst a -> Subst a
      reduce ((x,y):xs) = (x, (subst (help1 ((x,y):xs)) y)):reduce xs
             where
	     help1 ((x,y):xs) = if (vars y == []) then (x,y):(help1 xs)
	     	  	       else help1 xs

class (Eq a, Substitutable a) => Unifiable a where
      unify::a -> a -> Maybe (Subst a)
      
      combine::Maybe (Subst a) -> Maybe (Subst a) -> Maybe (Subst a)
      combine Nothing _ = Nothing
      combine _ Nothing = Nothing
      combine (Just x) (Just y) = Just (x ++ y)

      resolve::Maybe (Subst a) -> Maybe (Subst a)
      resolve Nothing = Nothing
      resolve (Just x) = if (x == (reduce x)) then help2 x else resolve (Just (nub (reduce x)))
      	      where
	      help2 lst = if ([x | x <- lst, y <- lst, (fst x == fst y)] == []) then  Just lst else Nothing
