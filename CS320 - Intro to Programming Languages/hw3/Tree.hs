module Tree where

data Tree a = Leaf | Node a (Tree a) (Tree a)
              deriving (Show, Eq)

data Color = Red | Green | Blue | Yellow

tree1 :: Tree String
tree1 = Node "A" (Leaf) (Leaf)

mapT :: (a -> b) -> Tree a -> Tree b
mapT f Leaf = Leaf
mapT f (Node x t1 t2) = Node (f x) (mapT f t1) (mapT f t2)

foldT :: (a -> b -> b -> b) -> b -> (Tree a) -> b
foldT f g Leaf = g
foldT f g (Node x t1 t2) = f x (foldT f g t1) (foldT f g t2)

leafCount :: Tree a -> Integer
leafCount Leaf = 1
leafCount (Node a l r) = leafCount l + leafCount r
                                    
nodeCount :: Tree a -> Integer
nodeCount Leaf = 0
nodeCount (Node a l r) = 1 + leafCount l + leafCount r

height :: Tree a -> Integer
height Leaf = 0
height (Node _ l r) = 1 + max (height l) (height r)

perfect :: Tree a -> Bool
perfect t = if (nodeCount t == (2^(height t) -1)) then True else False

degenerate :: Tree a -> Bool
degenerate t = if ((nodeCount t) == (height t)) then True else False

list :: Tree a -> Maybe [a]
list t = if (degenerate t) then Just(help t) else Nothing
    where
      help Leaf = []
      help (Node x l r) = [x] ++ (help l) ++ (help r)