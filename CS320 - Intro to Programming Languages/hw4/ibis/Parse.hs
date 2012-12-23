----------------------------------------------------------------
--
-- Ibis (light version: Haskell syntax, limited capability)
-- Copyright (C) 2008-2009
--
-- This software is made available under the GNU GPLv3.
--
-- Parse.hs
--   Parser implementation using the Parsec library.

----------------------------------------------------------------
-- 

module Parse (parseFile) where

import Text.ParserCombinators.Parsec
import qualified Text.ParserCombinators.Parsec.Token as P
import Text.ParserCombinators.Parsec.Expr
import Text.ParserCombinators.Parsec.Language
import Data.List (partition, sort, isPrefixOf)
import Data.Maybe (catMaybes, listToMaybe)
import Ratio

import Const
import Exp
import Stmt
import Result

----------------------------------------------------------------
-- Parser State Representation.

-- The parser state maintains a stack of inclusions (filenames)
-- to detect recursive inclusion dependencies.

data ParseState = PS Bool [String] --normalize flag, filename
type Parse a = GenParser Char ParseState a

----------------------------------------------------------------
-- Helper functions for manipulating source text strings.

getLines :: Int -> Int -> [String] -> [String]
getLines i j ls = take (j-i) $ drop i ls

getTxt :: Int -> SourcePos -> String -> String
getTxt n pos txt = unlines $ getLines (i-1) (i+n) $ lines txt
  where i = sourceLine pos

----------------------------------------------------------------
-- Exported Functions.

parseFile :: SourceName -> IO (Either Result [Stmt])
parseFile = fileP program

fileP :: Parse a -> String -> IO (Either Result a)
fileP p fname =
  do{ text <- readFile fname
    ; r <- return $ runParser p (PS True [fname]) "" text
    ; case r of
       Left err ->
         do{ pos <- return $ errorPos err
           ; return $ Left $ ErrSyntax (show err) (getTxt 4 pos text)
           }
       Right ss -> return $ Right ss
    }

----------------------------------------------------------------
-- Parsers -----------------------------------------------------
----------------------------------------------------------------

----------------------------------------------------------------
-- Top-level Parser.

program :: Parse [Stmt]
program =
  do{ whiteSpace
    ; ss <- many stmtP
    ; eof
    ; return ss
    }

----------------------------------------------------------------
-- Statements.

introKeys = ["Introduce", "Define"]
assumpKeys = ["Assume that", "Assume", "assume that", "assume"]
assertKeys = ["Assert that", "Assert", "assert that", "assert"]

stmtP :: Parse Stmt
stmtP = introP <|> assumeP <|> assertP <?> "statement"

introP :: Parse Stmt
introP =
  do{ keyP introKeys
    ; xs <- sepBy1 nameP commaSep
    ; punctP
    ; return $ Intro xs
    }
  <?> "variable introduction"

assumeP :: Parse Stmt
assumeP =
  do{ keyP assumpKeys
    ; e <- expP
    ; punctP
    ; return $ Assume e
    }
    <?> "assumption"

assertP :: Parse Stmt
assertP =
  do{ inputStr <- getInput
    ; pos1 <- getPosition
    ; keyP assertKeys
    ; e <- expP
    ; punctP
    ; pos2 <- getPosition
    ; return $ Assert (unlines (getLines 0 (min 7 ((sourceLine pos2)-(sourceLine pos1))) (lines inputStr))) e
    }
    <?> "assertion"

----------------------------------------------------------------
-- Arithmetic Expressions.

expP :: Parse Exp
expP =
  (listSepApp commaSep mkTup)
  $ expNoCommaP

expNoCommaP :: Parse Exp
expNoCommaP =
  (opsP opsLogic).
  (opsRelP)
  $ expBasicInfixes

expBasicInfixes =
  do { e <- expBasic
     ; es <- many expBasicInfix
     ; return $ appInfixes e es
     }
  <?> "expression with functional application"

appInfixes :: Exp -> [(Exp,Exp)] -> Exp
appInfixes e0 [] = e0
appInfixes e0 oes =
  let (o,e) = last oes
  in App (App o (appInfixes e0 (init oes))) e
   
expBasicInfix =
  do { symb "`"
     ; ev <- varP
     ; symb "`"
     ; e <- expBasic
     ; return $ (ev, e)
     }
  <?> "expression with functional application"

expBasic =
  (opsP opsSet).
  (opsP opsArith) 
  $ expAppP

expAppP :: Parse Exp
expAppP =
  do { es <- many1 expNoAppP
     ; return $ foldl App (head es) (tail es)
     }
  <?> "expression with functional application"

expNoAppP :: Parse Exp
expNoAppP = expAtom <|> quantP "\\forall" mkForall
   <?> "simple expression"

expAtom :: Parse Exp
expAtom = 
     --(try consP)
 -- <|> 
     braces expP
 <|> parens expP
 <|> expNumP
 <|> (try varOrConstP)
 <?> "atomic expression"

quantP :: String -> ([(Name, Maybe Exp)] -> Exp -> Exp) -> Parse Exp
quantP str cnstrct =
  do{ reserved str
    ; qvs <- quantVarsList
    ; symb "."
    ; e <- expP
    ; (PS fl _) <- getState --use this
    ; return $ cnstrct (addLimits qvs) e
    }
    <?> "quantifier: "++str

-- If no domain is provided for a variable, look further 
-- down the list and try to find one.
addLimits [] = []
addLimits ((i, Just e):vts) = (i, Just e):(addLimits vts)
addLimits ((i, Nothing):vts) = (i, nxt vts):(addLimits vts)
  where nxt = \qvs->listToMaybe $ catMaybes (map snd qvs)

quantVarsList = sepBy1 ((try quantVarWithIn) <|> quantVarNoIn) commaSep
      <?> "quantifier variable"

quantVarNoIn = do{x <- nameP; return (x, Nothing)}
quantVarWithIn =
  do{ x <- nameP
    ; reservedOp "\\in"
    ; e <- expNoCommaP
    ; return (x, Just e)
    }

opsRelP :: Parse Exp -> Parse Exp
opsRelP p =
  do{ e <- p
    ; rest <- many$do{ o<-foldr (<|>) (last opLL) (init opLL); e<-p; return (o,e)}
    ; return $ if rest==[] then e else listAnd $ mkRelExp e rest
    }
  where
    mkRelExp e [(o,e')] = [bOp o e e']
    mkRelExp e ((o,e'):oes) = (bOp o e e'):mkRelExp e' oes
    opLL = map (\(op,str,_) -> do{reservedOp str; return op}) (head opsRel)

opsP :: OpTable -> Parse Exp -> Parse Exp
opsP t = buildExpressionParser $ map (map o) t
 where
  o (op, str, "") = prefix str $ App (C op)
  o (op, str, a) = binary str (bOp op) (assoc a)
  assoc "L" = AssocLeft
  assoc "R" = AssocRight

expNumP :: Parse Exp
expNumP =
  do{ n <- naturalOrFloat
    ; return $ case n of
        Left  n -> C$N$ toRational n
        Right d -> C$N$ toRational d
    }
    <?> "numeric literal"

varOrConstP = foldr (<|>) varP $ (map opP opStrPairs) ++ [nilP]
varP = do{x <- nameP; return $ Var x}<?>"variable"
constP (op,str) = do{reserved str; return $ C op}<?>str
opP (op,str) = do{reserved "\\op"; reservedOp str; return $ C op}
nilP = do{reserved "[]"; return $ C Nil}
consP = do{reserved "(:)"; return $ C Cons}

----------------------------------------------------------------
-- Punctuation

punctAny = symb "," <|> symb "." <|> symb ";" <|> symb ":"
punctP = many punctAny <?> "punctuation mark(s): .,:;"
commaP = many (symb ",") <?> "comma"
commaSep = do {skipMany1 $ space <|> char ','; return ()}
doubleSlashSep = skipMany1 (reserved " " <|> reserved "\\\\")

----------------------------------------------------------------
-- Brackets.

parens :: Parse Exp -> Parse Exp
parens p =
  do{ b1 <- (brackP "(" Round )
    ; e <- p
    ; b2 <- (brackP ")" Round )
    ; return $ mkBrack b1 b2 e
    }
    <?> "parenthesized expression"

brackP str ret = do{symb str; return ret}<?> "bracket"
braces p       = between (symb "{") (symb "}") p
parens0 p      = between (symb "(") (symb ")") p

----------------------------------------------------------------
-- Other basic parsers.

nameP :: Parse Name
nameP = do{i <- idP; if i == "\\" then pzero else return (i,-1)}

keyP :: [String] -> Parse ()
keyP l = foldl (<|>) (head l') (tail l') where l' = map reserved l

wordP = do{w <- symb "-"; return "-"} <|> wordP'
wordP' :: Parse String
wordP' = do{w <- idP; if w!!0 == '\\' then pzero else return w}

listSepApp :: Parse b -> ([a] -> a) -> Parse a -> Parse a
listSepApp sepP f p = do{es <- sepBy1 p sepP; return $ f es}

----------------------------------------------------------------
-- Parsec Definitions.

lang = P.makeTokenParser langDef
langDef
  = emptyDef
  { commentStart    = "{-"
  , commentEnd      = "-}"
  , commentLine     = "--"
  , nestedComments  = True
  , identStart      = letter <|> oneOf "\\"
  , identLetter     = alphaNum <|> oneOf "'"
  , opStart         = opLetter langDef
  , opLetter        = oneOf "-<=>/\\+"
  , reservedOpNames = opStrs ++
                      ["<=>","=>",":","[]","\\/","/\\","->"]
  , reservedNames   =
      opStrs ++
      introKeys ++ assumpKeys ++ assertKeys ++
      ["\\forall",
       "\\op", "\\p", "\\l",
       ".", "\\\\", "|", ":"]
    , caseSensitive = True
    }

whiteSpace      = P.whiteSpace lang
reserved        = P.reserved lang
reservedOp      = P.reservedOp lang
symb            = P.symbol lang
idP             = P.identifier lang
natural         = P.natural lang
naturalOrFloat  = P.naturalOrFloat lang

binary  str f assoc = Infix (do{ reservedOp str; return f }) assoc
prefix  str f = Prefix (do{ reservedOp str; return f })
postfix str f = Postfix (do{ reservedOp str; return f })

--eof
