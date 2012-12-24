module Natural where
import Unify

data Natural = Zero | Succ Natural | Var String
	deriving (Eq, Show)

instance Substitutable Natural where
	 subst ((x, y):xs) (Var z) = if(z == x) then y else (subst xs (Var z))
 	 subst _ (Var x) = (Var x)

	 vars (Zero) = []
	 vars (Succ x) = vars x
	 vars (Var x) = [x]

instance (Eq Natural, Substitutable Natural) => Unifiable Natural where
	 unify (Var x) y = Just (sub x y)
	 unify y (Var x) = Just (sub x y)
	 unify (Zero) (Zero) = Just emp
	 unify (Succ x) (Succ y) = unify x y
	 unify _ _ = Nothing
