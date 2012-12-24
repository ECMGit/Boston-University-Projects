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

	//The window to display images
	ImageViewer imageViewer;
	ImageViewer outputViewer;
	
	//Simple counter for video cutoff
	long counter;
	
	// Flag for negative effect
	boolean isNeg = false;
	
	//The constructor initializes the window for display
	CS440P0()
	{
		imageViewer=new ImageViewer("Input");
		outputViewer = new ImageViewer("Output");
		counter = 0;

	}
	
	
	 
	/**
	 * The central function of VideoSink and the place where students
	 * can edit the code. receiveFrame function is given an image. The 
	 * body of the code will perform high level manipulatations of the 
	 * image, then display the image in the imageViewer. The return values
	 * indicates to the the video source whether or not to keep sending 
	 * images.
	 * 
	 * @param frame The current frame of the video source/
	 * @param firstFrame Whether or not the frame is the first frame of the video
	 * @return true if the video source should continue, or false if the video source should stop.
	 */
	public boolean receiveFrame(CS440Image frame) {

		/* This section of code copies the CS440Image frame.
		 * It was written by Zhiqiang Ren */
		 BufferedImage oimg = frame.getRawImage();
         BufferedImage nimg = new BufferedImage(oimg.getWidth(), oimg.getHeight(),
                         BufferedImage.TYPE_INT_RGB);
         nimg.setData(oimg.getData());
         CS440Image newframe = new CS440Image(nimg);
	
         // End of section by Zhiqiang Ren
         
		int x,y = 0;
				
		for(x=0; x< newframe.width(); x++)
		{
			for(y=0; y< newframe.height(); y++)
			{
				Color c = newframe.get(x, y);
				
				int red = c.getRed();
				int green = c.getGreen();
				int blue = c.getBlue();
					
				/* To prevent primary colors from appearing black in the negative,
				 * ensure that no pixel has a 0 value for one of its R, G, or B values
				 */
				if (red == 0) { red = 1;}
				if (green == 0) { green = 1;}
				if (blue == 0) { blue = 1;}
				
				// Now compute the negative of each pixel
				c = new Color ((255-red), (255-green), (255-blue));
				newframe.set(x,y,c);
			}
		}
		
		// Display input and output video in their respective windows
		displayImage(frame, imageViewer); 
		boolean shouldStop = displayImage(newframe, outputViewer); 
		return 	shouldStop;
	}
	
	/**
	 * This function displays the passed image in a frame.
	 * @param image The image to be displayed
	 */
	public boolean displayImage(CS440Image frame, ImageViewer window)
	{
		// Window is closed.
		if ((!imageViewer.isShowing()) || !outputViewer.isShowing())
		{
			return false;
		}
		
		if((imageViewer == null) || (outputViewer == null)) {
			// System.out.println("now we return false");
			return false;			
		}
		
		window.showImage(frame);
		return true;
	}
	
	/***
	 * Closes the window
	 */
	public void close()
	{
		if(imageViewer!=null)
		{
			this.imageViewer.dispose();
			imageViewer=null;
		}
	}

}
