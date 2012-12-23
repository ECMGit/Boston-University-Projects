----------------------------------------------------------------
--
-- Ibis (light version: Haskell syntax, limited capability)
-- Copyright (C) 2008-2009
--
-- This software is made available under the GNU GPLv3.
--
-- Verification.hs
--   Representation of the result of a verification attempt.

----------------------------------------------------------------
-- 

module Verification where

import Const
import Exp

data Verification = 
    Verifiable () Const 
  | Unknown
  | Potential (() -> Verification)

----------------------------------------------------------------
-- Functions for values of type Verification Bool

(&&&) v1 v2 = case v1 of
  Verifiable s (B True) -> case v2 of
    Verifiable s' (B True) -> Verifiable (max s s') (B True)
    Verifiable s' (B False) -> Verifiable (max s s') (B False)
    _ -> v2
  Verifiable s (B False) -> Verifiable s (B False)
  Unknown -> case v2 of
    Verifiable s (B False) -> Verifiable s (B False)
    _ -> Unknown
  Potential vf -> case v2 of
    Verifiable s (B True) -> v1
    Potential vf' -> Potential (\() -> vf () &&& vf' ())
    falOrUnv -> falOrUnv
  _ -> Unknown

(|||) v1 v2 = case v1 of
  Verifiable s (B True) -> v1
  Verifiable s (B False) -> case v2 of
    Verifiable s' (B False) -> Verifiable (min s s') (B False)
    Verifiable s' (B True) -> v2
    Potential vf -> Potential $ \() -> v1 ||| vf ()
    Unknown -> v1
  Potential vf -> case v2 of
    Verifiable s (B True) -> v2
    Verifiable s (B False) -> Potential $ \() -> vf () ||| v2
    Potential vf' -> Potential (\() -> vf () ||| vf' ())
    Unknown -> v1
    _ -> Unknown
  Unknown -> v2
  _ -> Unknown

(&*&) v1 v2 = case v1 of
  Verifiable s (B True) -> case v2 of
    Verifiable s' (B True) -> Verifiable (max s s') (B True)
    Verifiable s' (B False) -> Verifiable (max s s') (B False)
    _ -> v2
  Verifiable s (B False) -> Verifiable s (B False)

  Potential vf -> case v2 of
    Verifiable s (B True) -> v1
    Potential vf' -> Potential (\() -> vf () &&& vf' ())
    falOrUnv -> falOrUnv
  Unknown -> Unknown
  _ -> Unknown

(|/|) v1 v2 = case v1 of
  Verifiable s (B True) -> v1
  Verifiable s (B False) -> case v2 of
    Verifiable s (B b) -> v2
    Potential vf -> Potential $ \() -> v1 |/| vf ()
    Unknown -> Unknown
  Potential vf -> case v2 of
    Verifiable s (B True) -> v2
    Verifiable s (B False) -> Potential $ \() -> vf () |/| v2
    Potential vf' -> Potential (\() -> vf () |/| vf' ())
    Unknown -> Unknown
  Unknown -> Unknown
  _ -> Unknown

notV r = case r of
  Verifiable s (B b) -> Verifiable s (B $ not b)
  _ -> Unknown

orV = foldl (|||) Unknown
orV' = foldl (|/|) (Verifiable () (B False))
andV = foldl (&&&) (Verifiable () (B True))

boolToV b = if b then Verifiable () (B True) else Unknown

isVTrue (Verifiable s (B True)) = True
isVTrue _ = False

isVTrue' v = case v of
  Verifiable s (B True) -> v
  Potential vf -> Potential $ \() -> isVTrue' $ vf ()
  _ -> Unknown

sysMod sys' v = case v of
  Verifiable sys c -> Verifiable sys' c
  Potential vf -> Potential $ \() -> sysMod sys' $ vf ()
  _ -> v

expToVer e ow = case e of
  C (B b) -> Verifiable () (B b)
  C (N n) -> Verifiable () (N n)
  C c -> Unknown
  _ -> ow

--eof
