-- Code by: Tim Duffy
-- Superstring.hs
-- 9/26/09

module SuperString where
import List

overlap :: (String, String) -> Int
overlap ("", _) = 0
overlap (s, s') = if(isPrefixOf s s') then (length s) else
		  overlap ((tail s), s')

contains :: String -> String -> Bool
contains "" _ = False
contains s s' = if(isPrefixOf s' s) then True else
		contains (tail s) s'

o :: String -> String -> String
xs `o` ys = if ((overlap (xs,ys)) == 0) then xs ++ ys else
                xs++ (drop (overlap (xs,ys)) ys)

naive :: [String] -> String
naive l = foldr o "" l

minimize :: (a -> Int) -> a -> a -> a
minimize obj x y = if obj x < obj y then x else y

allPairs :: [String] -> [(String, String)]
allPairs xs = [ (s, s') | s <- xs, s' <-xs, s /= s']

update :: [String] -> (String, String) -> [String]
update l (s,s') = s'' : filter (not. (contains (s''))) l
    where
      s'' = ( s `o` s')

-- superstring :: ([String] -> [(String, String)]) -> [String] -> String
-- superstring l = superstring [ fst x ++ snd x | x <- (allPairs l)]

-- optimal :: [String] -> String
