----------------------------------------------------------------
--
-- Ibis (light version: Haskell syntax, limited capability)
-- Copyright (C) 2008-2009
--
-- This software is made available under the GNU GPLv3.
--
-- Result.hs
--   Representation of proof verification results.

----------------------------------------------------------------
-- 

module Result (Result(..), showRs, mkRInclude) where

import Const (Const(..))
import Verification (Verification(..))

data Result =
    OkIntro [String]
  | OkAssume ()
  | R String Verification
  | RInclude String Result
  | ErrUnbound [String]
  | ErrSyntax String String
  | ErrSystem String

mkRInclude :: String -> Result -> Result
mkRInclude n (RInclude n' r) = (RInclude n' r)
mkRInclude n r = RInclude n r

showRs :: Bool -> [Result] -> String
showRs html rs = foldr (\s-> \s'-> s++"\n"++s') "" $ map (showR html) rs

showR :: Bool -> Result -> String
showR html r = case r of
  R s (Verifiable sys (B True))  -> fmt html "blue" $ "Assertion verifiable" ++ maybe "" (\s -> " ("++s++")") Nothing
  R s (Verifiable sys (B False)) -> fmt html "#C82626" $ " *** Assertion verifiably false *** :\n"++s++"\n^"
  R s _                  -> fmt html "#C82626" $ " *** Assertion unverifiable *** :\n"++s++"\n^"
  RInclude n r  -> fmt html "green" $ "In [["++n++"]]: "++showR html r
  OkIntro vs    -> fmt html "black" $ "Introduced: "++show vs
  OkAssume sys  -> fmt html "black" "Assumption ok" ++ maybe "" (\s -> " ("++s++")") Nothing
  ErrUnbound vs -> fmt html "#C82626" " *** Unbound variables *** : "++show vs
  ErrSystem s   -> fmt html "#C82626" $ " *** System error *** : "++s
  ErrSyntax p s -> fmt html "#C82626" $ " *** Syntax error *** : \n"
                   ++fmt html "maroon" (p++" in...\n\n")++s

fmt :: Bool -> String -> String -> String
fmt True color s = "<font color=\""++color++"\">"++s++"</font>"
fmt False _ s = s

--eof
