prefix _ [] = []
prefix 1 xs = [head xs]
prefix n xs = (head xs) : (prefix (n-1) (tail xs))

suffix _ [] = []
suffix 0 xs = xs
suffix n xs = suffix (n-1) (tail xs)

split _ _ [] = []
split n y xs = if (suffix n xs == []) then  xs else
               prefix n xs ++ [y] ++ (split n y (suffix n xs))

plane r = [(x/r , y/r) | y <- [-1*r .. 1*r],  x <- [-2*r .. 1*r] ]

orbit (x,y) = bar (0,0) (pp (x,y))
bar (a,b) f  = (a,b) : bar (f (a,b)) f
pp (x,y)(u,v) = (u*u - v*v + x, 2*u*v + y)

disp _ [] = ' '
disp d ((x, y) : xs) = if (d > x) then disp d (xs) else y

mandelbrot r i l = 