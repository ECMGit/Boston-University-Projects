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
comp (N num) = do
	     x <- freshMemLoc
	     Fresh (\(e, ms, ls) -> ( (e, ms, ls) , (x, [Set x num]))) 

-- Assignment 8, Problem 2, Part B
comp (B True)  = do
		 x <- freshMemLoc
		 Fresh (\(e, (m:ms), ls) -> ( (e, ms, ls) , (m, [Set m 1])))

comp (B False) = do
		 x <- freshMemLoc
		 Fresh (\(e, (m:ms), ls) -> ( (e, ms, ls) , (m, [Set m 0])) )

-- Assignment 8, Problem 2, Part C
comp (If e1 e2 e3) = do
		    x <- freshMemLoc
		    lab1 <- freshLabel
		    lab2 <- freshLabel
		    (mem1, instr1) <- comp e1
		    (mem2, instr2) <- comp e2
		    (mem3, instr3) <- comp e3
		    Fresh (\(e, ms, (l:ls)) -> ( (e, ms, ls), (x, (instr1 ++ [(CJump mem1 lab1)] ++ instr2 ++ [(CopyFromTo mem2 x), (Jump  lab2), (Label lab1)] ++ instr3 ++ [(CopyFromTo mem3 x), (Label lab2)]))))

-- Assignment 8, Problem 2, Part D
comp (App (App (Op Plus) e1) e2) = do
				   x <- freshMemLoc
				   (mem1, instr1) <- comp e1
				   (mem2, instr2) <- comp e2
				   Fresh (\(e, ms, ls) -> ( (e, ms, ls), (x,  (instr1 ++ instr2 ++ [Add mem1 mem2 x]))))

comp (App (App (Op Times) e1) e2) = do
				    x <- freshMemLoc
				    (mem1, instr1) <- comp e1
				    (mem2, instr2) <- comp e2
				    Fresh (\(e, ms, ls) -> ( (e, ms, ls), (x,  (instr1 ++ instr2 ++ [Mul mem1 mem2 x]))))

-- Assignment 8, Problem 3, Part A
comp (Var str) = do
		 x <- freshMemLoc
		 var <- getVar str
		 Fresh (\(e, ms, ls) -> ( (e, ms, ls), (x,  [CopyFromTo var x])))

-- Assignment 8, Problem 3, Part B
comp (Let [str] e1 e2) = do
			 x <- freshMemLoc
			 (mem1, instr1) <- comp e1
			 putVar str mem1
			 (mem2, instr2) <- comp e2
			 Fresh (\(e, ms, ls) -> (((updEnv str mem1 e), ms, ls), (x, (instr1 ++ instr2 ++ [CopyFromTo mem2 x]))))

-- Assignment 8, Problem 4, Part A
comp (App (App (Op And) e1) e2) = do
				  x <- freshMemLoc
				  (mem1, instr1) <- comp e1
				  (mem2, instr2) <- comp e2
				  Fresh (\(e, ms, ls) -> ( (e, ms, ls), (x,  (instr1 ++ instr2 ++ [Mul mem1 mem2 x]))))

comp (App (App (Op Or) e1) e2) = do
				  x <- freshMemLoc
				  (mem1, instr1) <- comp e1
				  (mem2, instr2) <- comp e2
				  Fresh (\(e, ms, ls) -> ( (e, ms, ls), (x,  (instr1 ++ instr2 ++ [Add mem1 mem2 x]))))

-- Assignment 8, Problem 4, Part B
comp (App (Op Not) e1) = do
			 x <- freshMemLoc
			 lab1 <- freshLabel
			 lab2 <- freshLabel
			 (mem1, instr1) <- comp e1
			 Fresh (\(e, ms, ls) -> ( (e, ms, ls), (x,  (instr1 ++ [(CJump mem1 lab1), (Set x 0), (Jump lab2), (Label lab1), (Set x 1), (Label lab2)]))))

-- Assignment 8, Problem 4, Part C
comp Nil = do
	   x <- freshMemLoc
	   Fresh (\(e, ms, ls) -> ( (e, ms, ls), (x, [Set x 0])))

comp (App (App (Op Cons) e1) e2) = do
				   x <-  freshMemLoc
				   x1 <- freshMemLoc
				   x2 <- freshMemLoc
				   (mem1, instr1) <- comp e1
				   (mem2, instr2) <- comp e2
				   Fresh (\(e, ms, ls) -> ((e, ms, ls), (x, (instr1 ++ instr2 ++ [(Set x1 mem1), (Set x2 mem2), (Set x x1)]))))

-- Assignment 8, Problem 4, Part D
comp (App (Op Head) e1) = do
			  x <- freshMemLoc
			  (mem1, instr1) <- comp e1
			  Fresh (\(e, ms, ls) -> ( (e, ms, ls), (x, (instr1 ++ [(DerefAndCopy mem1 x),(DerefAndCopy x x)]))))

comp (App (Op Tail) e1) = do
			  x <- freshMemLoc
			  (mem1, instr1) <- comp e1
			  Fresh (\(e, ms, ls) -> ( (e, ms, ls), (x, (instr1 ++ [(DerefAndCopy mem1 x),(Set mem1 1),(Add x mem1 x),(DerefAndCopy x x)]))))

comp _ = Fresh (\(env,ms,ls) -> ((env,ms,ls), (-1, [Pass])))

--eof
