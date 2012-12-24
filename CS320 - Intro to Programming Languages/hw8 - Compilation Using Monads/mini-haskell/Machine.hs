----------------------------------------------------------------
-- Computer Science 320 (Fall, 2009)
-- Concepts of Programming Languages
--
-- Assignments 6, 7, and 8
--   Machine.hs

----------------------------------------------------------------
-- Definition of a machine language for Mini-Haskell.

module Machine (run, disp, MemLoc, Label, Instruction (..))
  where

import Err
import Exp

type Label = String
type MemLoc = Int

data Instruction = Set MemLoc Int
                 | CopyFromTo MemLoc MemLoc
                 | DerefAndCopy MemLoc MemLoc
                 | Add MemLoc MemLoc MemLoc
                 | Mul MemLoc MemLoc MemLoc
                 | Label Label
                 | Jump Label
                 | CJump MemLoc Label
                 | Pass
  deriving Show

disp mc = foldr (\s-> \s'-> s++"\n"++s') "" (map show mc)

----------------------------------------------------------------
-- Abstract machine memory representation.

type Memory = [(MemLoc, Int)]

set :: MemLoc -> Int -> Memory -> Memory
set l v m = (l,v):(filter (\(l',v')->not $ l==l') m)

get :: Memory -> MemLoc -> Int
get m l = head $ [v'|(l',v')<-m, l==l'] ++ [-1]

----------------------------------------------------------------
-- Abstract machine for machine code.

run :: Int -> [Instruction] -> Int
run rloc mc = (\(Just m)-> get m rloc) $ exec [] mc mc

exec :: Memory -> [Instruction] -> [Instruction] -> Maybe Memory
exec m ((Set l v):cs) cs'            = exec (set l v m) cs cs'
exec m ((CopyFromTo l1 l2):cs) cs'   = exec (set l2 (get m l1) m) cs cs'
exec m ((DerefAndCopy l1 l2):cs) cs' = exec (set l2 (get m (get m l1)) m) cs cs'
exec m ((Add l1 l2 l):cs) cs'        = exec (set l (get m l1 + get m l2) m) cs cs'
exec m ((Mul l1 l2 l):cs) cs'        = exec (set l (get m l1 * get m l2) m) cs cs'
exec m ((Label l):cs) cs'            = exec m cs cs'
exec m ((Jump l):cs) cs'             = goto l m cs' cs'                                   
exec m ((CJump ml l):cs) cs'         = if (get m ml) /= 0 then exec m cs cs' else goto l m cs' cs'
exec m (Pass:cs) cs'                 = exec m cs cs'
exec m [] cs'                        = Just m

goto lbl m cs' ((Label l):cs) = if l == lbl then exec m cs cs' else goto lbl m cs' cs
goto lbl m cs' (cmd:cs)       = goto lbl m cs' cs
goto lbl m cs' []             = Just m -- if there is no label, we stop

--eof
