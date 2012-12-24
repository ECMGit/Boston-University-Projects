import java.awt.Image;
import java.awt.Color;
import java.awt.image.BufferedImage;

/***********
 * The VideoSink class represents the entry point for high level
 * analysis of videos. Images are fed to the CS585Hw1 class via 
 * the receiveFrame. The videoSink has the ability to display 
 * images with the ImageViewer class.
 * 
 * @author Sam Epstein
 **********/
public class CS440P0 {
	
	Color bgPxl = new Color(0,0,0);
	Color skinPxl = new Color(128,0,255);
	Color[] centPxl = {new Color(0, 255,255), new Color(255,0,255), new Color(255,255,0)};
	Color[] boxPxl = {new Color(255,0,0), new Color(0,255,0), new Color(0,0,255)};
	
	CS440Image lastframe;
	CS440Image beforelast;
	CS440Image skinFrame;
	CS440Image Motion1;	// Track shape 1
	CS440Image Motion2;	// Track shape 2
	CS440Image Motion3;	// Track shape 3
	CS440Image copy;
	
	int templateCounter = -1;
	int maxFrames = 10;
	int lumChangeThreshold = 25;	// Threshold for lumosity change
	int distChangeThreshold = 50;
	
	int[][] motionlabels;
	int[][] SDlabels;
	int SDshapes = 0;
	
	/* This holds the x and y centroid locations, and height and width of the BB,
	 * and circularity for each shape over a number of frames
	 */
	double[][][] shapeLoc = new double[maxFrames + 2][3][6];
	
	/* This holds a shape number, and if the shape should be negative(1) or not(0) in the form
	 * negativeshapes[shape number][isNegative]
	 */
	boolean[] blueShapes = {false,false,false};
	
	/* The info arrays contain the following (for the largest 3 shapes appearing in the image):
	 * [i][0] is the x centroid of blob i
	 * [i][1] is the y centroid of blob i
	 * [i][2] is the minimum x value of blob i (used for bounding box)
	 * [i][3] is the mamimum x value of blob i (used for bounding box)
	 * [i][4] is the minimum y value of blob i (used for bounding box)
	 * [i][5] is the mamimum y value of blob i (used for bounding box)
	 */
	int[][] MDinfo = new int[3][6];
	int[][] SDinfo = new int[3][6];
	
	//The window to display images
	ImageViewer imageViewer;
	int height;
	int width;
	
	//The constructor initializes the window for display
	CS440P0()
	{
		imageViewer=new ImageViewer("Input");
	}
	
	/**
	 * The central function of VideoSink and the place where students
	 * can edit the code. receiveFrame function is given an image. The 
	 * body of the code will perform high level manipulations of the 
	 * image, then display the image in the imageViewer. The return values
	 * indicates to the the video source whether or not to keep sending 
	 * images.
	 * 
	 * @param frame The current frame of the video source/
	 * @param firstFrame Whether or not the frame is the first frame of the video
	 * @return true if the video source should continue, or false if the video source should stop.
	 */
	public boolean receiveFrame(CS440Image frame) {
				
		height = frame.height();
		width = frame.width();
		
		copy = new CS440Image(copyImg(frame));
		
		// Initialize frames if needed
		if(lastframe == null || beforelast == null || Motion1 == null){
			lastframe = new CS440Image(copyImg(frame));		// Used for motion detection
			beforelast = new CS440Image(copyImg(frame));	// Used for better motion detection
			skinFrame = new CS440Image(copyImg(frame));		// Used to display bounding box around skin colored areas
			Motion1 = new CS440Image(copyImg(frame));	
			Motion2 = new CS440Image(copyImg(frame));
			Motion3 = new CS440Image(copyImg(frame));

			motionlabels = new int[frame.width()][frame.height()];
			SDlabels = new int[frame.width()][frame.height()];
		}
			
		//Initialize shape locations array
		if(templateCounter == -1){
			templateCounter = 0;
			for(int i = 0; i < maxFrames; i++){
				for(int j = 0; j < 3; j++){
					for(int k = 0; k < 6; k++){
						shapeLoc[i][j][k] = 0;
					}
				}
			}
		}
		
		// Save locations in the last two frames
		for(int j = 0; j < 3; j++){
			for(int k = 0; k < 6; k++){
				shapeLoc[0][j][k] = shapeLoc[maxFrames-2][j][k];
				shapeLoc[1][j][k] = shapeLoc[maxFrames-1][j][k];
			}
		}
		
		// Reset locations every <maxframes> frames
		if (templateCounter >= maxFrames){
			templateCounter = 0;
			
			for(int i=2; i<maxFrames+2; i++){
				for(int j=0; j< 3; j++){
					for(int k = 0; k < 6; k++){
						shapeLoc[i][j][k] = 0;
					}
				}
			}
			
			for(int x=0; x< width; x++)
			{
				for(int y=0; y< height; y++)
				{
					Motion1.set(x, y, new Color(255,255,255));
					Motion2.set(x, y, new Color(255,255,255));
					Motion3.set(x, y, new Color(255,255,255));
				}
			}
		}
	
								/* Image Analysis */
		
		/* Detect skin colored pixels */
		for(int x=0; x< width; x++)
		{
			for(int y=0; y< height; y++)
			{
				if(isSkin(frame, x, y)){ skinFrame.set(x, y, skinPxl); }
				else { skinFrame.set(x, y, bgPxl); }
				
				// Also, reset labels
				SDlabels[x][y] = -1;
			}
		}
	
		crop(skinFrame);
		erode(skinFrame, skinPxl, 5);
		dilate(skinFrame, skinPxl, 20);
		crop(skinFrame);
		
		// Label connected areas
		SDshapes = 0;
		for(int x=1; x< width-2; x++)
		{
			for(int y=1; y< height-2; y++)
			{
				if(skinFrame.get(x, y).equals(skinPxl)){

					// get labels
					int a = SDlabels[x][y-1];
					if(a == -1){ a = 10000; }
					int b = SDlabels[x-1][y-1];
					if(b == -1){ b = 10000; }
					int c = SDlabels[x-1][y];
					if(c == -1){ c = 10000; }
					int d = SDlabels[x-1][y+1];
					if(d == -1){ d = 10000; }
					
					int min1 = Math.min(a, b);
					int min2 = Math.min(c, d);
					int supermin = Math.min(min1,min2);
					
					if(supermin == 10000){ // If there are no neighboring skinPxls
						SDlabels[x][y] = SDshapes;
						SDshapes++;
					}
					else{
						SDlabels[x][y] = supermin;
						SDlabels[x][y-1] = supermin;
						SDlabels[x-1][y-1] = supermin;
						SDlabels[x-1][y] = supermin;
						SDlabels[x-1][y+1] = supermin;
					}
				}	
			}
		}
			
		for(int x=width-2; x>0; x--)
		{
			for(int y=height-2; y> 1; y--)
			{
				if(skinFrame.get(x, y).equals(skinPxl)){
					
					// get labels of neighbors
					int[] arr = new int[5];
					arr[0] = SDlabels[x-1][y];
					arr[1] = SDlabels[x+1][y];
					arr[2] = SDlabels[x][y+1];
					arr[3] = SDlabels[x][y-1];
					arr[4] = SDlabels[x][y];
					
					// Find the minimum label;
					int min = arr[0];
					for(int i = 0; i < 5; i ++){
						if((arr[i] < min)&& (arr[i] != -1)){ min = arr[i]; }
					}

					// Relabel all neighbors, but avoid mislabeling a non-skinPxl as one.
					if(SDlabels[x][y] != -1) { SDlabels[x][y] = min; }
					if(SDlabels[x][y+1] != -1) { SDlabels[x][y+1] = min; }
					if(SDlabels[x+1][y-1] != -1) { SDlabels[x+1][y-1] = min; }
					if(SDlabels[x+1][y] != -1) { SDlabels[x+1][y] = min; }
					if(SDlabels[x+1][y+1] != -1) { SDlabels[x+1][y+1] = min; }
					
				}
			} 
		}
		
		// Find the sizes (number of pixels) of each shape.
		int[] SDsizes = new int[SDshapes];
		
		if (SDshapes > 0){
			for(int i=0; i<SDshapes; i++){ SDsizes[i] = 0; }	// Initalize all to 0
			
			for(int x=1; x< width-2; x++)
			{
				for(int y=1; y< height-2; y++)
				{
					if(SDlabels[x][y] != -1){ SDsizes[SDlabels[x][y]]++; }
				}
			}
		}
		
		// Find the 3 largest objects in each image
		int[] SDlargest3 = findLargest(SDsizes);
		

		// Calculate centroid and bounding box information of each of the 3 largest shapes
		SDinfo = calcInfo(skinFrame, SDlabels, SDlargest3);
		
		// Extract labels
		int l1 = SDlargest3[0];
		int l2 = SDlargest3[1];
		int l3 = SDlargest3[2];
		
		// Sort shapes by x coordinate, rather than area
		int xc1 = SDinfo[0][0];
		int xc2 = SDinfo[1][0];
		int xc3 = SDinfo[2][0];
		
		int minxc = Math.min(xc1, Math.min(xc2,xc3));
		if (minxc == xc1){
			
			SDlargest3[0] = l1;
			
			int min2 = Math.min(xc2, xc3);
			if(min2 == xc2){
				SDlargest3[1] = l2;
				SDlargest3[2] = l3;
			}
			else{
				SDlargest3[1] = l3;
				SDlargest3[2] = l2;
			}
		}
		else if (minxc == xc2){
			
			SDlargest3[0] = l2;
			
			int min2 = Math.min(xc1, xc3);
			if(min2 == xc1){
				SDlargest3[1] = l1;
				SDlargest3[2] = l3;
			}
			else{
				SDlargest3[1] = l3;
				SDlargest3[2] = l1;
			}	
		}
		else {
			SDlargest3[0] = l3;
			
			int min2 = Math.min(xc1, xc2);
			if(min2 == xc1){
				SDlargest3[1] = l1;
				SDlargest3[2] = l2;
			}
			else{
				SDlargest3[1] = l2;
				SDlargest3[2] = l1; 
			}
		}
		
		// recalculate centroid and bb info
		SDinfo = calcInfo(skinFrame, SDlabels, SDlargest3);
		
		int currframe = templateCounter + 2;
		
		// Record the location of the centroid, bounding box, and center of the BB
		for (int shape=0; shape<3; shape++) {
			shapeLoc[currframe][shape][0] = SDinfo[shape][0];		// x centroid
			shapeLoc[currframe][shape][1] = SDinfo[shape][1];		// y centroid
			shapeLoc[currframe][shape][2] = (SDinfo[shape][3] - SDinfo[shape][2]);		// Height of BB
			shapeLoc[currframe][shape][3] = (SDinfo[shape][5] - SDinfo[shape][4]);		// Width of BB
			shapeLoc[currframe][shape][4] = SDinfo[shape][2] + ((SDinfo[shape][3] - SDinfo[shape][2])/2);	// x center of BB
			shapeLoc[currframe][shape][5] = SDinfo[shape][4] + ((SDinfo[shape][5] - SDinfo[shape][4])/2);	// y center of BB 
		}
		
		// Paint each shape differently
		for(int x=0; x< width; x++)
		{
			for(int y=0; y< height; y++){
				if(SDlabels[x][y] == SDlargest3[0]){ skinFrame.set(x, y, new Color(255,0,0)); }
				else if(SDlabels[x][y] == SDlargest3[1]){ skinFrame.set(x, y, new Color(0,255,0)); }
				else if(SDlabels[x][y] == SDlargest3[2]){ skinFrame.set(x, y, new Color(0,0,255)); }
				else{ skinFrame.set(x, y, bgPxl); }
			}
		}
				
		/* Gesture detection */
		char[] gests = {0,0,0};
				
		// Use location of BB and centroid
		for(int s = 0; s < 3; s++){
							
			int x1,y1,x2,y2,x3,y3,bx1,bx2,bx3,by1,by2,by3,a1,a2,a3;
			
			// Centroid
			x1 = (int)shapeLoc[currframe-2][s][0];
			y1 = (int)shapeLoc[currframe-2][s][1];

			x2 = (int)shapeLoc[currframe-1][s][0];
			y2 = (int)shapeLoc[currframe-1][s][1];

			x3 = (int)shapeLoc[currframe][s][0];
			y3 = (int)shapeLoc[currframe][s][1];
			
			// Bounding box
			bx1 = (int)shapeLoc[currframe-2][s][4];
			by1 = (int)shapeLoc[currframe-2][s][5];

			bx2 = (int)shapeLoc[currframe-1][s][4];
			by2 = (int)shapeLoc[currframe-1][s][5];

			bx3 = (int)shapeLoc[currframe][s][4];
			by3 = (int)shapeLoc[currframe][s][5];
			
			// Area
			a1 = (int) (shapeLoc[currframe-2][s][2] * shapeLoc[currframe][s][3]);
			a2 = (int) (shapeLoc[currframe-1][s][2] * shapeLoc[currframe][s][3]);
			a3 = (int) (shapeLoc[currframe][s][2] * shapeLoc[currframe][s][3]);
			
			double dist1 = java.lang.Math.pow((x3-x2), 2) + java.lang.Math.pow((y3-y2),2); 
			double dist2 = java.lang.Math.pow((x2-x1), 2) + java.lang.Math.pow((y2-y1),2);
			
			double bbdist1 = java.lang.Math.pow((bx3-bx2), 2) + java.lang.Math.pow((by3-by2),2); 
			double bbdist2 = java.lang.Math.pow((bx2-bx1), 2) + java.lang.Math.pow((by2-by1),2);
								
			if((dist1 > distChangeThreshold) && (dist2 > distChangeThreshold)
					&& (bbdist1 > distChangeThreshold) && (bbdist2 > distChangeThreshold)){
			
				int areaThresh = 2000;
				if((a1 > areaThresh) && (a2 > areaThresh) && (a3 > areaThresh)){
					// Motion detected - apply negative effect
					blueShapes[s] = true;

					gests[s] = 'w';
					if(s==1){ gests[1] = 'n';}
				}
			}
			else{
				blueShapes[s] = false;
			}
		}
		
		if(gests[0] == 'w' && gests[2] == 'w'){ System.out.println("You are waving BOTH hands!"); }
		else if(gests[0] == 'w') { System.out.println("You are waving your Right Hand!"); }
		else if(gests[2] == 'w') { System.out.println("You are waving your Left Hand!"); }
		else if(gests[1] == 'n') { System.out.println("I see you shaking your head!"); }
		
		// Display a bounding box around the three objects
		for(int s = 0; s < 3; s++){
			int minx = SDinfo[s][2];
			int maxx = SDinfo[s][3];
			int miny = SDinfo[s][4];
			int maxy = SDinfo[s][5];
			
			// if the box is too small, don't show it!
			// if(((maxx-minx) * (maxy-miny)) < 4000 ){ continue;}
		
			// Top edge
			for(int i=minx; ((i< maxx+5) && i<width); i++){
				for (int j = miny; ((j>miny-5) && (j>0)); j--){
					copy.set(i,j,boxPxl[s]);
					skinFrame.set(i,j,boxPxl[s]);
				}
			}
			
			// Bottom edge
			for(int i=maxx; ((i> minx-5) && i>0); i--){
				for (int j = maxy; ((j<maxy+5) && (j<height-1)); j++){
					copy.set(i,j,boxPxl[s]);
					skinFrame.set(i,j,boxPxl[s]);
				}
			}
			
			// Left edge
			for(int i=minx; ((i> minx-5) && i>0); i--){
				for (int j = maxy; ((j>miny-5) && (j>0)); j--){
					copy.set(i,j,boxPxl[s]);
					skinFrame.set(i,j,boxPxl[s]);
				}
			}
			
			// Right edge
			for(int i=maxx; ((i< maxx+5) && i<width); i++){
				for (int j = miny; ((j<maxy+5) && (j<height)); j++){
					copy.set(i,j,boxPxl[s]);
					skinFrame.set(i,j,boxPxl[s]);
				}
			}
			
			// Convert skin pixels inside the bounding box to negative, if motion was detected.
			if(blueShapes[s]){
				for(int i=minx; i< maxx; i++){
					for (int j = miny; j<maxy; j++){
						
						if(isSkin(copy, i, j)){
							int R = (copy.get(i,j).getRed());
							int G = (copy.get(i,j).getGreen());
						
							copy.set(i,j, new Color(R,G,255));
						}
					}
				}
			}
		}
		
		// Paint a small area around each centroid
		for(int i = 0; i < 3; i++){
			try {
				for (int x = 1; x < 4; x++) {
					for (int y = 1; y < 4; y++) {
						copy.set(SDinfo[i][0] + x, SDinfo[i][1] + y, centPxl[i]);
						copy.set(SDinfo[i][0] + x, SDinfo[i][1] - y, centPxl[i]);
						copy.set(SDinfo[i][0] - x, SDinfo[i][1] + y, centPxl[i]);
						copy.set(SDinfo[i][0] - x, SDinfo[i][1] - y, centPxl[i]);
					}
				}
			}
			catch (ArrayIndexOutOfBoundsException e) { System.out.println("error"); }
		}
		
		// Update last frame and before last frames
		beforelast = new CS440Image(copyImg(lastframe));
		lastframe = new CS440Image(copyImg(frame));
		templateCounter++; // update the number of frames displayed in our motion template

		boolean shouldStop = false;
		// Display input and output video in their respective windows
		if((imageViewer.getKey() == '1')){	// Press 1 for skin color detection + bounding box
			shouldStop = displayImage(skinFrame);
		}
		else if((imageViewer.getKey() == '2')){	// Press 2 for no alterations
			shouldStop = displayImage(frame);
		}
		else{									// Press something else for no alterations
			shouldStop = displayImage(copy);
		}
		 
		return 	shouldStop;
	}

	/* This method takes in an image, an array of lables (which shape each blob in the image belongs to)
	 * and calculates the area, x centroid, and y centroid for the 3 largest shapes,
	 * who's labels are stored in the array largest' 
	 */
	public int[][] calcInfo(CS440Image img, int[][] labels, int[] largest){

		int[][] arr = new int[3][6];
		int xcenter, ycenter, minx, maxx, miny, maxy, counter;

		for(int i = 0; i < 3; i++){
			
			xcenter = 0;
			ycenter = 0;
			counter = 0;
			minx = 100000;
			maxx = 0;
			miny = 100000;
			maxy = 0;
			
			for (int x = 0; x < img.width(); x++) {
				for (int y = 0; y < img.height(); y++) {
					if (labels[x][y] == largest[i]) {
						xcenter += x;
						ycenter += y;
						if (x<minx){ minx = x; }
						if (x>maxx){ maxx = x; }
						if (y<miny){ miny = y; }
						if (y>maxy){ maxy = y; }
						counter++;
					}
				}
			}
		
			if (counter != 0) {
				arr[i][0] = xcenter / counter;
				arr[i][1] = ycenter / counter;
				arr[i][2] = minx;
				arr[i][3] = maxx;
				arr[i][4] = miny;
				arr[i][5] = maxy;
			}
		}
				
		return arr;
		
	}
	
	/* Closes the window */
	public void close()
	{
		if(imageViewer!=null)
		{
			this.imageViewer.dispose();
			imageViewer=null;
		}
	}
	/* This method returns a copy of img */
	public BufferedImage copyImg(CS440Image img){
		
		BufferedImage oimg = img.getRawImage();
		BufferedImage nimg = new BufferedImage(oimg.getWidth(), oimg.getHeight(), BufferedImage.TYPE_INT_RGB);
		nimg.setData(oimg.getData());
		
		return nimg;
	}

	/* This method crops the borders of the image as to avoid errors when dilating and labeling */
	public void crop(CS440Image img){
		for(int x=0; x < width; x++){	// Top and bottom edges
			img.set(x, 0, bgPxl);
			img.set(x, height-1, bgPxl);
		}
		
		for(int y=0; y<height; y++){	// Right and left edges			
			img.set(0, y, bgPxl);
			img.set(height-1, y, bgPxl);
		}
	}	
	
	/* This method dilates blobs in img by a factor of k. This is used to fill small gaps,
	 * so that area and centroid calculation is more accurate
	 */
	public void dilate(CS440Image img, Color c, int k){
		
		// First, create an array showing locations of skinPxls
		int[][] dist = new int[img.width()][img.height()];
		
		for (int i=0; i< dist.length; i++){
	        for (int j=0; j<dist[i].length; j++){
	        	if(img.get(i, j).equals(c)){ dist[i][j] = 1; }
	        	else { dist[i][j] = 0; }
	        }
		}
		
		// Then Calculate distance to closest skinPxl
		for (int i=0; i< img.width(); i++){
	        for (int j=0; j< img.height(); j++){

	        	if (dist[i][j] == 1){
	        		dist[i][j] = 0;  // Pixel is on - it is 0 spaces away from itself
	        	}
	        	else {
	        		// pixel was off
	        		dist[i][j] = dist.length + dist[i].length;	// Maximum distance between any two pixels
	        		if (i>0) dist[i][j] = Math.min(dist[i][j], dist[i-1][j]+1);		// Check above
	        		if (j>0) dist[i][j] = Math.min(dist[i][j], dist[i][j-1]+1);		// Check to the left
	        	}
	        }
		}
		
	    // Now we scan in the other direction
	    for (int i=img.width()-1; i>=0; i--){
	        for (int j=img.height()-1; j>=0; j--){
	            if (i+1<dist.length) dist[i][j] = Math.min(dist[i][j], dist[i+1][j]+1);		// Check below
	            if (j+1<dist[i].length) dist[i][j] = Math.min(dist[i][j], dist[i][j+1]+1);	// Check to the right
	        }
	    }
	    
	    // Dilate the image using dist
	    for (int i=0; i<dist.length; i++){
	        for (int j=0; j<dist[i].length; j++){
	            if(dist[i][j]<=k){ img.set(i, j, c); }
	            else{ img.set(i, j, bgPxl); }
	        }
	    }
	}
	
	/**
	 * This function displays the passed image in a frame.
	 * @param image The image to be displayed
	 */
	public boolean displayImage(CS440Image frame)
	{
		// Window is closed.
		if ((!imageViewer.isShowing()) || (imageViewer == null))
		{
			return false;
		}
		
		imageViewer.showImage(frame);
		return true;
	}
	

	/* Erode performs similar to dilate, except it works on
	 * background pixles. This method removes small objects
	 * and rough blob edges.
	 */
	public void erode(CS440Image img, Color c, int k){
		
		// First, create an array showing locations of bgPxls
		int[][] dist = new int[img.width()][img.height()];
		
		for (int i=0; i< dist.length; i++){
	        for (int j=0; j<dist[i].length; j++){
	        	if(img.get(i, j).equals(bgPxl)){ dist[i][j] = 1; }
	        	else { dist[i][j] = 0; }
	        }
		}
		
		// Then Calculate distance to closest skinPxl
		for (int i=0; i< img.width(); i++){
	        for (int j=0; j< img.height(); j++){

	        	if (dist[i][j] == 1){
	        		dist[i][j] = 0;  // Pixel is on - it is 0 spaces away from itself
	        	}
	        	else {
	        		// pixel was off
	        		dist[i][j] = dist.length + dist[i].length;	// Maximum distance between any two pixels
	        		if (i>0) dist[i][j] = Math.min(dist[i][j], dist[i-1][j]+1);		// Check above
	        		if (j>0) dist[i][j] = Math.min(dist[i][j], dist[i][j-1]+1);		// Check to the left
	        	}
	        }
		}
		
	    // Now we scan in the other direction
	    for (int i=img.width()-1; i>=0; i--){
	        for (int j=img.height()-1; j>=0; j--){
	            if (i+1<dist.length) dist[i][j] = Math.min(dist[i][j], dist[i+1][j]+1);		// Check below
	            if (j+1<dist[i].length) dist[i][j] = Math.min(dist[i][j], dist[i][j+1]+1);	// Check to the right
	        }
	    }
	    
	    // Erode the image using dist
	    for (int i=0; i<dist.length; i++){
	        for (int j=0; j<dist[i].length; j++){
	            if(dist[i][j]<=k){ img.set(i, j, bgPxl); }
	            else{ img.set(i, j, c); }
	        }
	    }
	}
	
	public int[] findLargest(int[] sizes){

		int b1 = 0;
		int b2 = 0;
		int b3 = 0;
		int[] bigLabels = {0,0,0};
		
		if(sizes.length>0){
			// 1st largest shape
			for(int i = 0; i<sizes.length; i++){
				if(sizes[i] > b1){
					b1 = sizes[i];
					bigLabels[0] = i;
				}
			}
		}
		else{ bigLabels[0] = -2; }
		
		if(sizes.length>1){
			// 2nd largest shape
			for(int i = 0; i<sizes.length; i++){
				if((sizes[i] > b2) && (sizes[i] != b1)){
					b2 = sizes[i];
					bigLabels[1] = i;
				}
			}
		}
		else{ bigLabels[1] = -2; }
		
		if(sizes.length > 2){
			// 3rd largest in motionTemp
			for(int i = 0; i<sizes.length; i++){
				if((sizes[i] > b3) && (sizes[i] != b1) && (sizes[i] != b2)){
					b3 = sizes[i];
					bigLabels[2] = i;
				}
			}
		}
		else{ bigLabels[2] = -2; }
		
		return bigLabels;
	}
	
	/* This method uses the RGB values of a pixel to calculate hue, saturation, and intensity.
	 * If all these parameters fall within a certain range, the pixel is considered a skin pixel
	 */
	public boolean isSkin(CS440Image frame, int x, int y){
		
		int R, G, B;
		float H, S, L;
		
		R = frame.get(x, y).getRed();
		G = frame.get(x, y).getGreen();
		B = frame.get(x, y).getBlue();
		
		float[] hsbVals = Color.RGBtoHSB(R, G, B, null);
		H = hsbVals[0];
		S = hsbVals[1];
		L = hsbVals[2];
		
		double hmin = 0.00;
		double hmax = 0.1;
		
		double smin = 0.2;
		double smax = 0.6;
		double lmin = 0.4;
		double lmax = 0.9;
		
		if( ((hmin <= H)&&(H <= hmax)) && ((smin <= S)&&(S <= smax)) && ((lmin <= L)&&(L <= lmax))){
			return true;
		  } 
		else { return false; }
	}
}