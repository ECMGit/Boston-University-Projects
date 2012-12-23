	---------------------------------------------------------------- 
-- Computer Science 320 (Fall, 2009) 
-- Concepts of Programming Languages 
-- 
-- Assignments 6, 7, and 8 
--   Compile.hs 
 
---------------------------------------------------------------- 
-- Compiler for Mini-Haskell. 
 
module Compile  where --(compile) 
 
import Monad 
import Env 
import Err 
import Exp 
import Machine

memlocs :: [MemLoc] 
memlocs = locs 0 where locs n = n:locs (n+1) 
 
labels :: [Label] 
labels = labs 0 where labs n = ("L"++show n):labs (n+1) 
 
compile e = (ml,mc) 
  where (_,(ml,mc)) = (\(Fresh f) ->  
                      f (emptyEnv, memlocs, labels)) $ (comp e) 
 
data Fresh a =  
  Fresh ((Env MemLoc, [MemLoc], [Label])  
          -> ((Env MemLoc, [MemLoc], [Label]), a))

-- Assignment 8, Problem 1, Part A
instance Monad Fresh where
  return x = Fresh (\y -> (y,x))
  (Fresh m) >>= f = Fresh(\x -> let (y, x') = m x in
				let Fresh f' = f x' in
				f' y)

-- Assignment 8, Problem 1, Part B
freshMemLoc :: Fresh MemLoc
freshMemLoc = Fresh (\(e,(m:ms), ls) -> ( (e, ms, ls), m ))

-- Assignment 8, Problem 1, Part C
freshLabel :: Fresh Label
freshLabel = Fresh (\(e, ms, (l:ls)) -> ( (e, ms, ls), l ))
	     
-- Assignment 8, Problem 1, Part D
putVar::String -> MemLoc -> Fresh ()
putVar str x = Fresh (\(e, ms, ls) -> ( ( (updEnv str x e), ms, ls), () ))


-- Assignment 8, Problem 1, Part E
getVar::String -> Fresh MemLoc
getVar str = Fresh (\(e, ms, ls) -> ( (e, ms, ls), (xtr (findEnv str e)) ))
	      where 
	      xtr (Just z) = z
 
comp :: Exp -> Fresh (MemLoc, [Instruction])
-- Assignment 8, Problem 2, Part A
comp (N x) = Fresh (\(e, (m:ms), ls) -> ( (e, ms, ls) , (m, [Set m x])) ) 

-- Assignment 8, Problem 2, Part B
comp (B True)  = Fresh (\(e, (m:ms), ls) -> ( (e, ms, ls) , (m, [Set m 1])) )
comp (B False) = Fresh (\(e, (m:ms), ls) -> ( (e, ms, ls) , (m, [Set m 0])) )

-- Assignment 8, Problem 2, Part C
comp (If e1 e2 e3) = 
	let (mem1, instr1) = compile e1
	    (mem2, instr2) = compile e2
	    (mem3, instr3) = compile e3 in
	     Fresh (\(e, (m:ms), ls) -> ( (e, ms, ls), (m, (instr1 ++ [(CJump mem1 "L0")] ++ instr2 ++ [(CopyFromTo mem2 m), (Jump  "L1"), (Label "L0")] ++ instr3 ++ [(CopyFromTo mem3 m), (Label "L1")]))))

-- Assignment 8, Problem 2, Part D
comp (App (App (Op Plus) e1) e2) = 
	let (mem1, instr1) = compile e1
	    (mem2, instr2) = compile e2 in
	    Fresh (\(e, (m:ms), ls) -> ( (e, ms, ls), (m,  (instr1 ++ instr2 ++ [Add mem1 mem2 m]))))

comp (App (App (Op Times) e1) e2) = 
	let (mem1, instr1) = compile e1
	    (mem2, instr2) = compile e2 in
	    Fresh (\(e, (m:ms), ls) -> ( (e, ms, ls), (m,  (instr1 ++ instr2 ++ [Mul mem1 mem2 m]))))

-- Assignment 8, Problem 3, Part A
comp (Var str) = Fresh (\(e, (m:ms), ls) -> ( (e, ms, ls), (m,  [Set m (xtr (findEnv str e))])))
	where
	xtr (Just x) = x

-- Assignment 8, Problem 3, Part B
comp (Let [str] e1 e2) = 
	let (mem1, instr1) = compile e1
	    (mem2, instr2) = compile e2 in
	    Fresh (\(e, (m:ms), ls) -> ( ((updEnv str mem1 e), ms, ls), (m, (instr1 ++ instr2 ++ [CopyFromTo mem2 m]))))

-- Assignment 8, Problem 4, Part A
comp (App (App (Op And) e1) e2) = 
	let (mem1, instr1) = compile e1
	    (mem2, instr2) = compile e2 in
	    Fresh (\(e, (m:ms), ls) -> ( (e, ms, ls), (m,  (instr1 ++ instr2 ++ [Mul mem1 mem2 m]))))

-- Assignment 8, Problem 4, Part B
comp (App (Op Not) e1) =
	let (mem1, instr1) = compile e1 in
	    Fresh (\(e, (m:ms), ls) -> ( (e, ms, ls), (m,  (instr1 ++ [(CJump mem1 "L0"), (Set m 0), (Jump "L1"), (Label "L0"), (Set m 1), (Label "L1")]))))

-- Assignment 8, Problem 4, Part C
comp Nil = Fresh (\(e, (m:ms), ls) -> ( (e, ms, ls), (m, [Pass])))

{--
comp (App (App (Op Cons) e1) e2) = 
	let (mem1, instr1) = compile e1
	    (mem2, instr2) = compile e2
	    f_mem1 = freshMemLoc
	    f_mem2 = freshMemLoc in
	    Fresh (\(e, (m:ms), ls) -> ( (e, ms, ls), (m, [(Set f_mem1 mem1), (Set f_mem2 mem2), (CopyFromTo f_mem1 m)])))

-- Assignment 8, Problem 4, Part D
comp (App (Op Head) e1) = 
	let (mem1, instr1) = compile e1 in
	    Fresh (\(e, (m:ms), ls) -> ( (e, ms, ls), (m, [DerefAndCopy mem1 m])))

 comp (App (Op Tail) e1) = 
	let (mem1, instr1) = compile e1 in
	    Fresh (\(e, (m:ms), ls) -> ( (e, ms, ls), (m, [ ])))
--}
comp _ = Fresh (\(env,ms,ls) -> ((env,ms,ls), (-1, [Pass])))


--eof
