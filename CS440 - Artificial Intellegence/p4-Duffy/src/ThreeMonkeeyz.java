/*
 * ThreeMonkeeyz.Java - Given an Atropos game board as a command line argument, this program will determine the best possible next move
 */

public class ThreeMonkeeyz {

	public static void main(String[] args) {

		/* Read input (args [0]) 
		 * Input is of the format [boardrow_1][boardrow_2][...][boardrow_n]LastPlay:(Color,height,dist from l, dist from r)
		 * */

		String[] tokens = args[0].split("]");
		int size = tokens.length-1;

		int[][] board  = new int[size][size];

		// create the game board
		for(int i = 0; i<size; i++){
			for(int j = 1; j< tokens[i].length(); j++){
				board[size-i-1][j-1] = Integer.parseInt("" + tokens[i].charAt(j));
			}
		}

		// shift last line (height = 0) over
		for(int i = board[0].length-1; i > 0; i--){
			board[0][i] = board[0][i-1];
		}
		board[0][0] = -1;

		// info about LastPlay
		// If we go first, pick at random
		if(tokens[size].equalsIgnoreCase("LastPlay:null")){
			String first = "(" + 1 + "," + 1 + "," + board[1].length/2 + "," + (board[1].length - (board[1].length/2)- 1 ) + ")";
			System.out.print(first);
			return;
		}

		int LastColor = Integer.parseInt("" + tokens[size].charAt(10));
		int H = Integer.parseInt("" + tokens[size].charAt(12));
		int L = Integer.parseInt("" + tokens[size].charAt(14));
		int R = Integer.parseInt("" + tokens[size].charAt(16));

		// Choose the best possible move (return format is move[bestColor][bestPosition]
		int[] move = firstLvl(board, H, L);

		int newH;
		int newL;
		int newColor = move[0];

		// append info about our move to the nextMove string
		switch(move[1]){

		// If move[1] = 0, we should make our move in the top right neighbor of the last move
		case 0: 
			newH = H+1;
			newL = L;
			break;

			// If move[1] = 1, we should make our move in the right neighbor of the last move
		case 1: 
			newH = H;
			newL = L+1;
			break;

			// If move[1] = 2, we should make our move in the bottom right neighbor of the last move
		case 2:	
			newH = H-1;
			newL = L+1;
			break;

			// If move[1] = 3, we should make our move in the bottom left neighbor of the last move
		case 3:	
			newH = H-1;
			newL = L;
			break;

			// If move[1] = 4, we should make our move in the bottom left neighbor of the last move
		case 4:
			newH = H;
			newL = L-1;
			break;

			// If move[1] = 5, we should make our move in the bottom left neighbor of the last move
		case 5:
			newH = H+1;
			newL = L-1;
			break;

		default:
			int randomPick[] = noMoreMoves(board);
			newColor = randomPick[0];
			newH = randomPick[1];
			newL = randomPick[2];
			break;

		}

		// create a string for our next move
		String nextMove = "(" + newColor +  "," + newH + "," + newL + ",";
		nextMove += (size - newH - newL);
		nextMove += ")";

		System.out.print(nextMove);
	}

	/* 
	 * Given the coordinates of the last move, H and L, this function determines the best move for us
	 * For each possible move(by trying all safe colors in each possible position), we perform a 2nd level
	 * analysis to determine opponents best move, then select our next move by choosing the option
	 * that gives our opponent the worst outcomes.
	 * 
	 * A third level check prevents us from choosing a path that will lead to our immediate loss in the next turn
	 */
	public static int[] firstLvl(int[][]b, int lastH, int lastL){

		// For each color and neighbor, keep track of the score
		int[][] scores = new int[3][6];

		// initialize all scores to -1. 
		// If the score remains -1 after scoring process, it is not a possible move
		for(int n = 0; n < 6; n++){
			for(int c = 0; c <3; c++){
				scores[c][n] = -1;
			}
		}

		/* 
		 * For each neighbor of the last move, we 'pretend' to move there, and determine a score
		 * The score is based on the number of safe moves we will have on our next turn.
		 */

		// Top right neighbor of the last move
		if(b[lastH+1][lastL] == 0)
		{
			for(int color = 1; color <=3; color++){
				if( isSafe(b, lastH+1, lastL, color) ){

					int[][] fakeboard = copyBoard(b);
					fakeboard[lastH+1][lastL] = color;
					scores[color-1][0] = secondLvl(fakeboard, lastH+1, lastL);
				}
			}
		}

		// Right neighbor of the last move
		if(b[lastH][lastL+1] == 0)
		{
			for(int color = 1; color <=3; color++){
				if( isSafe(b, lastH, lastL+1, color) ){

					int[][] fakeboard = copyBoard(b);
					fakeboard[lastH][lastL+1] = color;
					scores[color-1][1] = secondLvl(fakeboard, lastH, lastL+1);
				}
			}
		}

		// Bottom right neighbor of the last move
		if(b[lastH-1][lastL+1] == 0)
		{
			for(int color = 1; color <=3; color++){
				if( isSafe(b, lastH-1, lastL+1, color) ){

					int[][] fakeboard = copyBoard(b);
					fakeboard[lastH-1][lastL+1] = color;
					scores[color-1][2] = secondLvl(fakeboard, lastH-1, lastL+1);
				}
			}
		}

		// Bottom left neighbor of the last move
		if(b[lastH-1][lastL] == 0)
		{
			for(int color = 1; color <=3; color++){
				if( isSafe(b, lastH-1, lastL, color) ){

					int[][] fakeboard = copyBoard(b);
					fakeboard[lastH-1][lastL] = color;
					scores[color-1][3] = secondLvl(fakeboard, lastH-1, lastL);
				}
			}
		}

		// Left neighbor of the last move
		if(b[lastH][lastL-1] == 0)
		{
			for(int color = 1; color <=3; color++){
				if( isSafe(b, lastH, lastL-1, color) ){

					int[][] fakeboard = copyBoard(b);
					fakeboard[lastH][lastL-1] = color;
					scores[color-1][4] = secondLvl(fakeboard, lastH, lastL-1);
				}
			}
		}

		// Top left neighbor of the last move
		if(b[lastH+1][lastL-1] == 0)
		{
			for(int color = 1; color <=3; color++){
				if( isSafe(b, lastH+1, lastL-1, color) ){

					int[][] fakeboard = copyBoard(b);
					fakeboard[lastH+1][lastL-1] = color;
					scores[color-1][5] = secondLvl(fakeboard, lastH+1, lastL-1);
				}
			}
		}

		// pick the move that maximizes score!
		int bestCase = -1;
		int bestColor = -1;
		int bestPos = -1;

		for(int n = 0; n < 6; n++){
			for(int c = 0; c <3; c++){
				if(scores[c][n] > bestCase){
					bestCase = scores[c][n];
					bestColor = c+1;
					bestPos = n;
				}
			}
		}

		int[] move = {bestColor, bestPos};

		// return move to main for processing and output.
		return move;
	}

	/* 
	 * Given a board and the coordinates of the last move (which is our potential move at h, el)
	 * this function determines the best move for our opponent, which minimizes the number of safe options
	 * we will have on our next turn.
	 * 
	 * This function returns the number of safe moves we will have on our next turn, if the opponent picks
	 * the worst possible option
	 */
	public static int secondLvl(int[][] b, int lastH, int lastL)
	{
		/*
		 * scores[c][n] contains the score when playing color c at neighbor n, where:
		 * n = 0 is the top right neighbor to the last move
		 * n = 1 is the right neighbor to the last move
		 * n = 2 is the bottom right neighbor to the last move
		 * n = 3 is the bottom left neighbor to the last move
		 * n = 4 is the left neighbor to the last move
		 * n = 5 is the top left neighbor to the last move
		 */
		int scores[][] = new int[3][6];

		// start at -1 to show that move is not possible
		for(int n = 0; n < 6; n++){
			for(int c = 0; c <3; c++){
				scores[c][n] = -1;
			}
		}

		/*
		 * For each open spot, determine a score for each color choice.
		 * score is the number of safe moves we will have for our next turn, assuming that the opponent 
		 * chooses the move that will minimize our win probability.
		 */

		// Top right neighbor of our last move
		if(b[lastH+1][lastL] == 0)
		{
			// if playing a color in this space is safe, create a fake board for evaluation
			// otherwise, set the score for this color and spot to 0
			for(int color = 1; color <=3; color++){
				if( isSafe(b, lastH+1, lastL, color) ){

					int[][] fakeboard = copyBoard(b);
					fakeboard[lastH+1][lastL] = color;
					scores[color-1][0] = thirdLvl(fakeboard, lastH+1, lastL);
				}
			}
		}

		// Right neighbor of our last move
		if(b[lastH][lastL+1] == 0)
		{
			for(int color = 1; color <=3; color++){
				if( isSafe(b, lastH, lastL+1, color) ){

					int[][] fakeboard = copyBoard(b);
					fakeboard[lastH][lastL+1] = color;
					scores[color-1][1] = thirdLvl(fakeboard, lastH, lastL+1);
				}
			}
		}

		// Bottom right neighbor of our last move
		if(b[lastH-1][lastL+1] == 0)
		{
			for(int color = 1; color <=3; color++){
				if( isSafe(b, lastH-1, lastL+1, color) ){

					int[][] fakeboard = copyBoard(b);
					fakeboard[lastH-1][lastL+1] = color;
					scores[color-1][2] = thirdLvl(fakeboard, lastH-1, lastL+1);
				}
			}
		}

		// Bottom left neighbor of our last move
		if(b[lastH-1][lastL] == 0)
		{
			for(int color = 1; color <=3; color++){
				if( isSafe(b, lastH-1, lastL, color) ){

					int[][] fakeboard = copyBoard(b);
					fakeboard[lastH-1][lastL] = color;
					scores[color-1][3] = thirdLvl(fakeboard, lastH-1, lastL);
				}
				else{ scores[color-1][3] = 0; }
			}
		}

		// Left neighbor of our last move
		if(b[lastH][lastL-1] == 0)
		{
			for(int color = 1; color <=3; color++){
				if( isSafe(b, lastH, lastL-1, color) ){

					int[][] fakeboard = copyBoard(b);
					fakeboard[lastH][lastL-1] = color;
					scores[color-1][4] = thirdLvl(fakeboard, lastH, lastL-1);
				}
			}
		}

		// Top left neighbor of our last move
		if(b[lastH+1][lastL-1] == 0)
		{
			for(int color = 1; color <=3; color++){
				if( isSafe(b, lastH+1, lastL-1, color) ){

					int[][] fakeboard = copyBoard(b);
					fakeboard[lastH+1][lastL-1] = color;
					scores[color-1][5] = thirdLvl(fakeboard, lastH+1, lastL-1);
				}
			}
		}				

		/* Determine worst case */
		int worstScore = 100;
		for(int n = 0; n < 6; n++){
			for(int c = 0; c <3; c++){
				if((scores[c][n] < worstScore) && (scores[c][n] >= 0)){
					worstScore = scores[c][n];
				}
			}
		}

		// if everything is -1, then our move has taken the last spot, and our opponent can choose anywhere
		if(worstScore == 100){return -1; }
		else{ return worstScore; }
	}	

	/* 
	 * Given a board and the coordinates of the last move (which is our opponents potential move at h, el)
	 * this function returns the number of safe moves available.
	 */
	public static int thirdLvl(int[][] b, int lastH, int lastL)
	{
		int numSafeMoves = 0;

		// Top right neighbor of our last move
		if(b[lastH+1][lastL] == 0)
		{
			for(int color = 1; color <=3; color++){
				if( isSafe(b, lastH+1, lastL, color) ){
					numSafeMoves++;
				}
			}
		}

		// Right neighbor of our last move
		if(b[lastH][lastL-1] == 0)
		{		
			for(int color = 1; color <=3; color++){
				if( isSafe(b, lastH, lastL-1, color) ){
					numSafeMoves++;
				}
			}
		}

		// Bottom right neighbor of our last move
		if(b[lastH-1][lastL+1] == 0)
		{		
			for(int color = 1; color <=3; color++){
				if( isSafe(b, lastH-1, lastL+1, color) ){
					numSafeMoves++;
				}
			}
		}

		// Bottom left neighbor of our last move
		if(b[lastH-1][lastL] == 0)
		{		
			for(int color = 1; color <=3; color++){
				if( isSafe(b, lastH-1, lastL, color) ){ numSafeMoves++; }
			}
		}

		// Left neighbor of our last move
		if(b[lastH][lastL+1] == 0)
		{			
			for(int color = 1; color <=3; color++){
				if( isSafe(b, lastH, lastL+1, color) ){ numSafeMoves++; }
			}
		}

		// Top left neighbor of our last move
		if(b[lastH+1][lastL-1] == 0)
		{
			for(int color = 1; color <=3; color++){
				if( isSafe(b, lastH+1, lastL-1, color) ){ numSafeMoves++; }
			}
		}	

		//System.out.println(numSafeMoves);
		return numSafeMoves;
	}

	/*
	 * Given a board, a position, and a color, this function 
	 * returns true if the move is safe, and false otherwise
	 */
	public static boolean isSafe(int[][] board, int h, int el, int c){

		boolean isSafe = true;

		/*
		 * For each pair of neighboring nodes, check to see if placing the given color
		 * in the given spot will result in loss of the game
		 */

		// Left and top left neighbors
		if( (board[h][el-1] != board[h+1][el-1]) && (board[h][el-1] !=0) && (board[h+1][el-1] !=0) )
		{
			//3+2+1 = 6; so subtracting the 2 other values will give the "danger color"
			int dangerColor = 6-board[h][el-1]-board[h+1][el-1];
			if (dangerColor == c){ isSafe = false; }
		}

		// Top left and top right neighbors
		if( (board[h+1][el-1] != board[h+1][el]) && (board[h+1][el-1] !=0) && (board[h+1][el] !=0) )
		{
			int dangerColor = 6-board[h+1][el-1]-board[h+1][el];
			if (dangerColor == c){ isSafe = false; }
		}

		// Top right and right neighbors
		if( (board[h+1][el] != board[h][el+1]) && (board[h+1][el] !=0) && (board[h][el+1] !=0) )
		{
			int dangerColor = 6-board[h+1][el]-board[h][el+1];
			if (dangerColor == c){ isSafe = false; }
		}

		// Right and bottom right neighbors
		if( (board[h][el+1] != board[h-1][el+1]) && (board[h][el+1] !=0) && (board[h-1][el+1] !=0) )
		{
			int dangerColor = 6-board[h][el+1]-board[h-1][el+1];
			if (dangerColor == c){ isSafe = false; }
		}

		// Bottom right and bottom left neighbors
		if( (board[h-1][el+1] != board[h-1][el]) && (board[h-1][el+1] !=0) && (board[h-1][el] !=0) )
		{
			int dangerColor = 6-board[h-1][el+1]-board[h-1][el];
			if (dangerColor == c){ isSafe = false; }
		}

		// Bottom left and left neighbors
		if( (board[h-1][el] != board[h][el-1]) && (board[h-1][el] !=0) && (board[h][el-1] !=0) )
		{
			int dangerColor = 6-board[h-1][el]-board[h][el-1];
			if (dangerColor == c){ isSafe = false; }
		}

		return isSafe;
	}

	/*
	 * Pretty print function to display the given board.
	 * This function was borrowed from the score code developed by Kyle Burke and Prof. Shang-Hua Teng
	 */
	public static void printBoard(int[][] board, int C, int H, int L, int R){

		/* Print out board*/
		String string = "";
		for (int i = board.length - 1; i >=0 ; i --) {
			//set up some nice spacing.
			for (int space = 0; space < 2*(i - 1); space++) {
				string += " ";
			}
			if (i == 0) {
				string += "  ";
			}
			string +="[";
			if(i>0){
				for (int j = 0; j < board.length-i+1; j++) {
					string += board[i][j];
					if(j<board.length -i) { string += "   "; }
				}
			}
			else{
				for (int j = 0; j < board.length-i-1; j++) {
					string += board[i][j+1];
					if(j<board.length -i -2) { string += "   "; }
				}
			}

			string += "]\n";
		}
		string += "Last Play:(";
		string += C;
		string += ",";
		string += H;
		string += ",";
		string += L;
		string += ",";
		string += R;
		string += ")";

		System.out.println(string);	 
	}

	/* Returns a new copy of the given board */
	public static int[][] copyBoard(int[][] source){

		// create new board
		int[][] b = new int[source.length][source.length];

		// copy data from original
		for(int i = 0; i < b.length; i++){
			for(int j = 0; j < b.length; j++){
				b[i][j] = source[i][j];
			}
		}

		return b;
	}

	/*
	 * This function is called when our opponents last move has no open neighbors.
	 * This scans the board and returns the first open position with a safe move available.
	 */
	public static int[] noMoreMoves(int[][] board){

		int[] move = {-1,-1,-1};

		boolean foundSafe = false;
		// scan the entire board
		for(int i = 1; i < board.length; i++){

			if(foundSafe){ break; }

			for(int j = 1; j <  board.length-i; j++){

				if(foundSafe){ break; }

				// if we find an open space, find its score
				if(board[i][j] == 0){
					for(int c = 1; c <3; c++){

						if(foundSafe){ break; }

						/* If we never find a safe move, we have no choice but to loose.
						 * If no safe moves are available, we will choose the last empty
						 * spot found and loose gracefully.
						 */

						move[0] = c;
						move[1] = i;
						move[2] = j;

						if(isSafe(board, i, j, c)){
							move[0] = c;
							move[1] = i;
							move[2] = j;
							foundSafe = true;
							return move;
						}
					}
				}
			}
		}
		return move;
	}
}
