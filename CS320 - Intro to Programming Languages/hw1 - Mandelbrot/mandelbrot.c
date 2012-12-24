#include <stdio.h>
#include <string.h>

main (){

  int res, orbit;

  printf("Please enter the resolution: ");
  scanf("%d",&res);getchar();

  printf("Enter the orbit index: ");
  scanf("%d",&orbit);getchar();

}

char[] suffix(int n, char xs[]){

  int len = strlen(xs)
  char str[] = [len - n];
 
  for(int i = 0; i < len - n -1; i++){
    str[i] = xs[i+n]
  }
  
  return str;
}
  
  // char[] split (int n, char x, char[] xs){
  
  // char[] newArr
  
  //if  
  

//split ((3*r) + 1) ('\n') [disp (norm y) l | y <- [(orbit x) !! (i) | x <-(plane r)]]
*/

int strlen(char str[]){
   int i;
   for(i=0; i&lt80; i++)
   {
	 if(str[i]=='\0')
	 {
	    return(i);
	 }
   }
}
