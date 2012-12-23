#include <stdio.h>

/* Tim Duffy
 * Problem Set #2
 * BU ID# U19028127
*/

// 1.a
int foo1A(int x)
{
  return (x << 4) - x;
}

/*
  pushl %ebp        - points to start of frame
  movl %esp,%ebp    - mark where we left off in the stack
  movl 8(%ebp),%eax - move x to eax
  sall $4,%eax      - shift eax left by 4
  subl 8(%ebp),%eax - subtract x
  movl %ebp,%esp    - move frame pointer to where we left off
  popl %ebp         - pop frame pointer
  ret               - return
*/

// 1.b
int foo1B(int x)
{
  return  (x >= 0) ? (x >> 4) : (x + 15);
}


/*
  pushl %ebp        - points to start of frame
  movl %esp,%ebp    - mark where we left off in the stack
  movl 8(%ebp),%eax - move x to eax
  testl %eax,%eax   - test if eax is zero
  jge .L4           - Jump if eax is >= 0
  addl $15,%eax     - else: add 15 to x

.L4:
  sarl $4,%eax      - shift eax right by 4
  movl %ebp,%esp    - move frame pointer to where we left off
  popl %ebp         - pop frame pointer
  ret               - return
*/


// 2
int fun(int *ap, int *bp)
{
    int a;
    a = *ap + *bp;
    a = *ap;
    return a;
}

/*
  pushl %ebp        - save frame pointer
  movl %esp,%ebp    - new frame pointer
  movl 8(%ebp),%edx - edx = ap
  movl 12(%ebp),%eax- eax = bp
  movl %ebp,%esp    - restore stack pointer
  movl (%edx),%edx  - edx = *ap
  addl %edx,(%eax)  - *bp = *bp + *ap
  movl %edx,%eax    - a = *ap
  popl %ebp         - restore frame pointer
  ret               - return
*/


//3
int foo3(int *a, int n, int val) {
  int i;
  
  for(i = (n-1); ((int)((char)*a +  4*i) = val) && i >= 0 ; i-- ){
    ;
  }
  
  return i;
}

/*
  pushl %ebp        	- points to start of frame
  movl %esp,%ebp    	- mark where we left off in the stack
  movl 8(%ebp),%ecx 	- ecx= *a
  movl 16(%ebp),%edx	- edx= val
  movl 12(%ebp),%eax	- i = n 
  decl %eax         	- i--
  js .L3		- If negative, go to L3

.L7:
  cmpl %edx,(%ecx,%eax,4) - compare mem[ecx + 4*eax] with val
  jne .L3		  - if not equal, jump to L3
  decl %eax		  - i--
  jns .L7		  - if i is greater than zero, go to L7

.L3:			
  movl %ebp,%esp
  popl %ebp
  ret
*/


int mat1[36][60];
int mat2[60][36];
int copy_element(int i, int j)
{
     mat1[i][j] = mat2[j][i];
}

/*
This generates the following IA32 assembly code:
copy_element:
         pushl %ebp
         movl %esp,%ebp
         pushl %ebx
         movl 8(%ebp),%ecx           - ecx = i
         movl 12(%ebp),%ebx          - ebx = j
         movl %ecx,%edx              - edx = i
         leal (%ebx,%ebx,8),%eax     - eax = j+j*8 = j*9
         sall $4,%edx                - i= i*16
         sall $2,%eax                - j= j*36
         subl %ecx,%edx              - i = (i*16) - i = i*15
         movl mat2(%eax,%ecx,4),%eax - eax = *(mat2 + j*36 + (4*i))
         sall $2,%edx                - i = i*60
         movl %eax,mat1(%edx,%ebx,4) - *(mat1 + i*60 + (4*j)) = eax
         movl -4(%ebp),%ebx
         movl %ebp,%esp
         popl %ebp
         ret
 (a) What is the value of M? 36
 (b) What is the value of N? 60

*/


int silly(int n, int *p)
{
    int val, val2;
    if (n > 0)
         val2 = silly(n << 1, &val);
    else
         val = val2 = 0;

    *p = val + val2 + n;
    return val + val2;
}

/*
silly:
         pushl %ebp
         movl %esp,%ebp
         subl $20,%esp         - allocate 20 bites for variables
         pushl %ebx            - push ebx on stack
         movl 8(%ebp),%ebx     - ebx = n
         testl %ebx,%ebx       - test if ebx = 0
	   jle .L3             - go to L3 if n <= 0. otherwise continue
         addl $-8,%esp         - allocate 8 more bites for parameters
         leal -4(%ebp),%eax    - eax = &val
         pushl %eax            - push val on stack at -4(ebp)
         leal (%ebx,%ebx),%eax - eax = n*2
         pushl %eax            - push eax on stack at
         call silly            - recursive call
         jmp .L4               - go to L4
         .p2align 4,,7
.L3:                            (else)
         xorl %eax,%eax        - eax (val2) = 0
         movl %eax,-4(%ebp)    - val = 0
.L4:
         movl -4(%ebp),%edx    - edx = val
         addl %eax,%edx        - val = val2 + val
         movl 12(%ebp),%eax    - eax = p
         addl %edx,%ebx        - n = n + (val + val2)
         movl %ebx,(%eax)      - *p = n + (val + val2)
         movl -24(%ebp),%ebx   - update base address upon exit of recusive call
         movl %edx,%eax        - p = val2 + val
         movl %ebp,%esp        - return stack pointer
         popl %ebp             - return frame pointer
         ret                   - return p


(a) Is the variable val stored on the stack? If so, at what byte offset (relative to %ebp) is it stored, and why is it necessary to store it on the stack?  

-- val is saved at -4(ebp). It is necessary to save it beacuse its memory address is needed in the recursive call to silly, and the registers must be free to compute this recursive call.

(b) Is the variable val2 stored on the stack? If so, at what byte offset (relative to %ebp) is it stored, and why is it necessary to store it on the stack? 

-- Val2 is not stored on the stack. When the recursive call to silly returns, its value is stored in eax, which is val2.

(c) What (if anything) is stored at -24(%ebp)? If something is stored there, why is it necessary to store it? 

-- The base address of the recursive call to silly() is stored at -24(%ebp).

(d) What (if anything) is stored at -8(%ebp)? If something is stored there, why is it necessary to store it?

-- Nothing is stored at -8(%ebp). This space is unused.

*/

int main()
{ //Testing purposes only }
