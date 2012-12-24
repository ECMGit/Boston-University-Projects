module Equation where
import Unify

data Equation a = a `Equals` a

solveEqn :: (Unifiable a) => Equation a -> Maybe (Subst a)
solveEqn (a1 `Equals` a2) = resolve (unify a1 a2)

solveSystem::(Unifiable a) => [Equation a] -> Maybe (Subst a)
solveSystem eqs = resolve (Just (concat [f eq | eq <- eqs]))
    where
    f (a `Equals` b) = shed (unify a b)
    shed (Just j) = j
