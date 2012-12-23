-- The prefix function shows the first n elements of a list
prefix _ [] = []
prefix 1 xs = [head xs]
prefix n xs = (head xs) : (prefix (n-1) (tail xs))

-- The suffix function displays a list
-- after the first n elements have been dropped
suffix _ [] = []
suffix 0 xs = xs
suffix n xs = suffix (n-1) (tail xs)

-- This split funcion inserts a character y into a list
--  every n places
split _ _ [] = []
split n y xs = if (suffix n xs == []) then  xs else
               prefix n xs ++ [y] ++ (split n y (suffix n xs))

-- The plane function displays all points within a certain distance
-- of the origin
plane r = [(x/r , y/r) | y <- [-1*r .. 1*r],  x <- [-2*r .. 1*r] ]

orbit (x,y) = bar (0,0) (pp (x,y))
bar (a,b) f  = (a,b) : bar (f (a,b)) f -- computes and infinite list of points
pp (x,y)(u,v) = (u*u - v*v + x, 2*u*v + y)

disp _ [] = ' '
disp d ((x, y) : xs) = if (d > x) then disp d (xs) else y

norm (x,y) = x*x + y*y

mandelbrot r i l = split ((3*r) + 1) ('\n') [disp (norm y) l | y <- [(orbit x) !! (i) | x <-(plane r)]]

-- Although I was unable to implement this program in C/Java, through my attempts I have learned that functional lanugages such as Haskell provide much more flexibilty. Therefore, programming is much easier. With imperitive programming languages,much more code is necessary, which makes the program prone to more errors.
