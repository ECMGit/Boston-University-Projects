----------------------------------------------------------------
-- Computer Science 320 (Fall, 2009)
-- Concepts of Programming Languages
--
-- Assignments 6, 7, and 8
--   Val.hs

----------------------------------------------------------------
-- Value representation for Mini-Haskell

module Val (Val(..)) where

import Env (Env)
import Exp (Exp(..), Oper(..), showTuple)

data Val = VNil
         | VN Int | VB Bool | VOp Oper
         | Partial Oper Val
         | VTuple [Val]
         | VList [Val]

----------------------------------------------------------------
-- Show Function for Values

instance Show Val where
  show VNil = "[]"
  show (VN n) = show n
  show (VB b) = show b
  show (VOp op) = show op
  show (Partial op v) = "("++show op++" "++show v++")"
  show (VTuple vs) = showTuple (map show vs)
  show (VList bs) = show bs

--eof
