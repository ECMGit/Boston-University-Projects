----------------------------------------------------------------
-- Computer Science 320 (Fall, 2009)
-- Concepts of Programming Languages
--
-- Assignments 6, 7, and 8
--   Exp.hs

----------------------------------------------------------------
-- Abstract syntax for the Mini-Haskell interpreter

module Exp (Oper(..), Exp(..), opStrs, bopApp, manyApp, tuple,
            showTuple) 
  where

data Oper = Plus | Times | Equal
          | And | Or | Not
          | Cons | Head | Tail
  deriving Eq

data Exp = Nil
         | N Int | B Bool | Op Oper
         | Var String
         | App Exp Exp
         | Tuple [Exp]
         | If Exp Exp Exp
         | Let [String] Exp Exp
  deriving Eq

----------------------------------------------------------------
-- Helper functions useful to the parser.

bopApp :: Oper -> Exp -> Exp -> Exp
bopApp op e1 e2 = App (App (Op op) e1) e2

manyApp :: [Exp] -> Exp
manyApp [f]      = f
manyApp (f:e:es) = manyApp ((App f e):es)

tuple :: [Exp] -> Exp
tuple [e] = e
tuple es = Tuple es

----------------------------------------------------------------
-- Printing functions for Abstract Syntax

opStrs = [ (Plus, "(+)"), (Times, "(*)"), (Equal, "(==)")
         , (And, "(&&)"), (Or, "(||)"), (Not, "not")
         , (Cons, "(:)"), (Head, "head"), (Tail, "tail") ]

showTuple [] = "()"
showTuple [x] = x
showTuple xs = "("++foldr (\s-> \t-> s++", "++t) 
                          (last xs) (init xs)++")"

instance Show Oper where show o' = head$[s|(o,s)<-opStrs,o==o']

instance Show Exp where
  show Nil   = "[]"
  show (N n) = show n
  show (B b) = show b
  show (Var x) = x
  show (Op o) = show o
  show (App e1 e2) = "("++show e1++" "++show e2++")"
  show (Tuple es) = showTuple (map show es)
  show (If e1 e2 e3) = "if "++show e1++" then "
                       ++show e2++" else "++show e3
  show (Let xs e be) = "let "++showTuple xs++" = "++show e
                       ++" in\n"++show be

--eof
