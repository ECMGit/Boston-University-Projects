----------------------------------------------------------------
--
-- Ibis (light version: Haskell syntax, limited capability)
-- Copyright (C) 2008-2009
--
-- This software is made available under the GNU GPLv3.
--
-- Const.hs
--   Representation of primitive mathematical constants.

----------------------------------------------------------------
-- 

module Const where

import Ratio -- external library

import Set

data Bracket = Round | Square | Curly | Angle | Oxford | Bar
  deriving (Show, Eq, Ord)

type PredC = [Maybe String] -- global English phrase predicates

data Const =
    Pow | Neg | Times | Div | Plus | Minus | Circ | Cons | Nil | Append
  | Eql | Neq | Lt | Lte | Gt | Gte 
  | In | Arrow
  | And | Or | Imp | Iff
  | B Bool | N Rational
  | TC [Const]
  | Brack Bracket Bracket
  | FalToUnknown
  | SearchIff
  deriving (Show, Eq, Ord)

type OpTable = [[(Const, String, String)]]

opsArith  =
  [ [ (Pow,   "^", "L") ]
  , [ (Neg,   "-", "") ]
  , [ (Times, "*", "L")
    , (Div,   "/", "L")
    ]
  , [ (Plus,  "+", "L")
    , (Minus, "-", "L")
    ]
  , [ (Circ,  "\\circ", "L")
    ]
  , [ (Cons,  ":", "R"),
      (Append,  "++", "L")
    ]
  ]
opsSet  =
  [ [ (Arrow, "->", "R")
    ]
  ]
opsRel =
  [ [ (Eql,  "=", "L")
    , (Neq,  "\\neq", "L")
    , (Gt,   ">", "L")
    , (Gte,  "\\geq", "L")
    , (Lt,   "<", "L")
    , (Lte,  "\\leq", "L")
    , (In,   "\\in", "L")
    ] 
  ]
opsLogic =
  [ [ (And, "/\\", "L")
    , (Or,  "\\/", "L")
    ]
  , [ (Imp, "=>", "R")
    , (Iff, "<=>", "L")
    ]
  ]

opStrPairs = map (\(x,y,_)->(x,y)) (concat $ opsArith++opsSet++opsRel++opsLogic)
opStrs = map snd opStrPairs

showCommaSep xs = foldr (\x y -> x ++ ", " ++ y) (last xs) (init xs)

--eof
