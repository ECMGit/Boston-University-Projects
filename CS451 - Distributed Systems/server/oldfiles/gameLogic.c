int makeMove(int k){
//check if space is open
if(board[k]==0){
   //check if its an even or odd turn
   if(turn%2==0){
	board[k]=2;
   }else{
        board[k]=1;
   }
   turn++;
   //successful
   return 1;
}else{
   return -1;
}
}


int checkForWin(){
int win=checkHorizontals();
   if(win==-1){
	win=checkDiagonals();
   }
   if(win==-1){
	win=checkVerticals();
  }
  return win;
}

int checkDiagonals(){
   if(board[0]==1 && board[4]==1 && board[8]==1){
	return 1;
   }else if(board[0]==2 && board[4]==2 && board[8]==2){
	return 2;
   }else if(board[2]==1 && board[4]==1 && board[6]==1){
	return 1;
   }else if(board[2]==2 && board[4]==2 && board[6]==2){
	return 2;
   }else{
	return -1;
   }
}

int checkHorizontals(){
   for(int a=0; a<=6; a=a+3){
	if(board[a]==1 && board[(a+1)]==1 && board[(a+2)]==1){
	   return 1;
	}if(board[a]==2 && board[(a+1)]==2 && board[(a+2)]==2){
	   return 2;}
	}
   return -1;
}

int checkVerticals(){
   for(int a=0; a<=2; a++){
	if(board[a]==1 && board[(a+3)]==1 && board[(a+6)]==1){
	   return 1;
	}if(board[a]==2 && board[(a+3)]==2 && board[(a+6)]==2){
	   return 2;}
	}
   return -1;
}

void initGame(){
//not sure what specifics to put here, we should build this after we have the init handler done
}

