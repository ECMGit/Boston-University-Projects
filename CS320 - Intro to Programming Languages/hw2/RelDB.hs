-- Code by: Tim Duffy
-- Superstring.hs
-- 9/26/09

module RelDB where

data Value = I Int | S String | Null
     deriving Show --displays values

data OpArg = C Column | V Value

type Column = String
type Row = [Value]
type Table = ([Column], [Row])
type Op = Value -> Value -> Value
type LogicOp = Value -> Value -> Bool
type AggregateOp = [Value] -> Value

plus :: Op
plus (I x) (I y) = I (x + y)
plus  _ _ = Null

ccat :: Op
ccat (S x)(S y) = S (x ++ y)
ccat _ _ = Null

eqls :: LogicOp
eqls (I x) (I y) = (x==y)
eqls (S x) (S y) = (x==y)
eqls Null Null = True
eqls _ _ = False

agg :: Op -> Value -> AggregateOp
agg op base [] = base
agg op base (x:xs) = op x (agg op base xs)

v :: Column -> [(Column, Value)] -> Value
v name ((x, y):xs) = if(name==x) then y else
		    v name xs
v name [] = Null

select :: [Column] -> Table -> Table
select cs tbl  = ([c | c <- fst tbl, c' <- cs, c == c'], [[v | (c,v) <- r, c' <- cs,c==c'] | r <- map (zip (fst tbl)) (snd tbl)])

aggregate :: AggregateOp -> Column -> Table -> Value
aggregate op c tbl = op (concat (snd (select [c] tbl)))

join :: Table -> Table -> Table
join t1 t2 = ([ x | x <- (fst t1) ++ (fst t2)], [x ++ y | x <- (snd t1), y <-(snd t2)])

only :: OpArg -> LogicOp -> OpArg -> Table -> Table
only x op y tbl = ((fst tbl) , [r | r <- snd tbl, op (extract x) y == True])

extract :: OpArg -> [(Column,Value)] -> Value
extract (C op) ((x,y) : xs) = if (op == x) then y else extract (C op) xs
extract (V op) _ = op

tbl1 = (
     ["Metro area",	"Population"], [
     [S "Tokyo",	I 32450000],
     [S "Seoul",	I 20550000],
     [S "Mexico City",	I 20450000],
     [S "New York City",I 19750000],
     [S "Mumbai",	I 19200000],
     [S "Jakarta",	I 18900000],
     [S "San Paulo",    I 18600000],
     [S "Delhi",	I 18600000] ] )

tbl2 = (
     ["City",		"Country"], [
     [S "Tokyo",	S "Japan"],
     [S "Seoul",	S "South Korea"],
     [S "Mexico City",	S "Mexico"],
     [S "New York City",S "United States"],
     [S "Mumbai",	S "India"],
     [S "Jakarta",	S "Indonesia"],
     [S "San Paulo",    S "Brazil"],
     [S "Delhi",	S "India"] ] )

tbl3 :: Table
tbl3 = join (select ["Country"] tbl2) (select ["Population"] tbl1)

--population :: Value
