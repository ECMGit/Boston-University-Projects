module Tree where
import Unify
import Equation

data Tree = Leaf | Node Tree Tree | Var String
	deriving (Eq, Show)

instance Substitutable Tree where
	 subst ((x, y):xs) (Var z) = if(z == x) then y else (subst xs (Var z))
 	 subst _ (Var x) = (Var x)

	 vars (Leaf) = []
	 vars (Node t1 t2) = (vars t1) ++ (vars t2)
	 vars (Var x) = [x]

instance (Eq Tree, Substitutable Tree) => Unifiable Tree where
	 unify (Var x) y = Just (sub x y)
	 unify y (Var x) = Just (sub x y)
	 unify (Leaf) (Leaf) = Just emp
	 unify (Node t1 t2) (Node t3 t4) = combine((unify t1 t3)(unify t2 t4))
	 unify _ _  = Nothing

e0 = Node (Node (Node (Var "x") (Var "y")) (Node (Var "y") (Var "x"))) (Var "z")
         `Equals`
     Node (Node (Node Leaf (Var "z")) (Node Leaf (Var "y"))) (Var "x")
e1 = let f b 0 = b
         f b n = Node (f b (n-1)) (f b (n-1))
     in f (Var "x") 10 `Equals` f Leaf 13
e2 = [ (Var "z") `Equals` Leaf
     , Node (Var "y") Leaf `Equals` Node Leaf (Var "x")
     , (Var "x") `Equals` Node (Var "z") (Var "z")
     ]

s0 = solveEqn e0
s1 = solveEqn e1
s3 = solveSystem e2



