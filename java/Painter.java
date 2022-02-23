import java.awt.*; 
import java.awt.image.*; 
import java.io.*; 
import java.applet.*; 
import java.net.*; 
import java.util.*;

// The main painting panel

// For a description of the painting algorithm, see the SIGGRAPH 98 paper

public final class Painter extends Panel
{
  IntImage sourceImage;      // the original image to be processed
  IntImage outputImage;      // the resulting painting

  // lastFrame: paint over last frame instead of starting anew
  static IntImage lastFrame = null;
  static boolean paintOverLastFrame = false;

  ImageCanvas illus;         // a component that displays the current painting

  // create the panel and set up the local variables
  public Painter(IntImage sourceImage)
  {
    this.sourceImage = sourceImage;         

    illus = new ImageCanvas(sourceImage);   // display the source image
    illus.repaint();

    setLayout(new BorderLayout());

    // add the image display and the image controls
    add("Center",illus);
    add("South",new Controls(this));
  }

  public void setImage(IntImage sourceImage)
  {
    this.sourceImage = sourceImage;
  }

  // ----------------------- Image Processing Parameters ---------------------
  // Parameters that control the painting style
  // Most of these parameters are also described in the SIGGRAPH submission

  int threshold = 100;       // how much error to allow before painting over

  int minBrushSize = 2;      // smallest brush radius
  int numLayers = 3;         // number of brushes

  double lengthFac = 1;      // ratio of stroke length to brush diameter
  
  double gridFac = 1;        // radius of grid spacing to brush radius
  double blurFac = .5;       // radius of blurring in reference image to brush radius

  boolean rand = true;       // if true, randomize the stroke orders

  double aa = .1;            // size of falloff region, for anti-aliasing brush strokes

  static final int CANVAS_COLOR = 0x00FFF3DD;  // alpha < FF for colorDist() method

  //  Kernel intensity;          
 
  int minLength = 4;        // minimum stroke length (number of control points)
  int maxLength = 16;       // maximum stroke length

  boolean animate = false;  // animate the painting process? (slow)

  boolean nonLinearDiffusion = false;   // use nonlinear diffusion to generate reference images

  static boolean lumDistance = false;   // use luminance to weight distance
  static boolean luvDistance = false;   // use CIE LUV to compute color distance

  double sfac = 1;          // saturation multiplier
  double bfac = 1;          // value multiplier
  double hfac = 1;          // hue multiplier
  
  double sjit = 0;          // saturation jitter
  double bjit = 0;          // value jitter
  double hjit = 0;          // hue jitter

  double RGBjit = 0;        // jitter for R, G, and B components

  double opacity = 1;       // brush stroke opacity (1=opaque, 0=invisible)

  double gradFac = 0;       // importance of gradient in threshold

  double filterFac = 1;     // IIR filter factor for stroke control points

  boolean clip = false;     // attempt to clip images to source edges

  double clipFac = 12;      

  boolean underpaint = false;  // paint the canvas the average color first

  boolean updateError = true; // use the new image error after every stroke

  Random randomizer = new Random();

  // ---------------- makeStrokes(): Create a painting -----------------

  void makeStrokes()
  {
    int canvasColor = CANVAS_COLOR;

    if (underpaint)     // compute the average color and saturate it
      canvasColor = IntImage.adjustColor(sourceImage.aveColor(),
					 1,0,2,0,1,0);

    // create a new image, painted canvasColor
    outputImage = new IntImage(sourceImage.width,sourceImage.height,
			       canvasColor);

    if (lastFrame != null)
      outputImage = lastFrame;

    // initialize the painting flags -- these flags are used to
    // accelerate brush stroke painting
    outputImage.makeFlags();

    //illus.newImage(outputImage);
    //    illus.repaint();
    
    // list of brush sizes, from largest to smallest
    int[] brushSizes = new int[numLayers];

    brushSizes[numLayers-1] = minBrushSize;

    for(int i=numLayers-2;i>=0;i--)
      brushSizes[i] = brushSizes[i+1]*2;

    // create the reference images
    IntImage[] refImages = new IntImage[numLayers];

    System.out.println("Making reference images");

    if (blurFac == 0)
      {
	// no blurring necessary, so just copy the source image
	for(int i=0;i<numLayers;i++)
	  refImages[i] = sourceImage;
      }
    else
      {
	for(int i=0;i<numLayers;i++)
	  {
	    if (nonLinearDiffusion)
	      refImages[i] = sourceImage.diffuse(1.5*blurFac*brushSizes[i],
						 .75,2,6*(numLayers-i));
	    else
	      {
		//		Kernel g = new Gaussian((int)(1.5*blurFac*brushSizes[i]),
		//					1.5*blurFac*brushSizes[i]);
		//		refImages[i] = sourceImage.convolve(g);

		refImages[i] = sourceImage.gaussianBlur(blurFac*brushSizes[i]);
	      }

	    //	    new ImageFrame(refImages[i],"Reference Image "+i);
	  }
      }

    Thread thr = null;    // animation thread

    if (animate)
      {
	illus.getGraphics().clearRect(0,0,sourceImage.width,
				      sourceImage.height);

	thr = new Thread(illus);
	illus.t = thr;
	thr.setPriority(8);
	thr.start();
      }

    // Do each layer of the painting
    
    for(int i=0;i<numLayers;i++)
      {
	IntImage source = refImages[i];

	randomizer.setSeed(brushSizes[i]);

	doLayer(brushSizes[i],threshold,source);

	new ImageFrame((IntImage)(outputImage.clone()),"Layer "+i);
      }

    System.out.println("Done painting");

    // Display the painting
    illus.newImage(outputImage);
    illus.repaint();
    
    if (paintOverLastFrame)
      lastFrame = outputImage;
  }

  // ------------------------- Layer algorithm --------------------------------
  //
  // Create the brush strokes for one layer, with one brush size, with respect
  // to a reference image

  void doLayer(int brushRadius,int threshold,IntImage refImage)
  {
    System.out.println("brushRadius = "+brushRadius+", threshold = "+threshold);

    // compute the grid spacing
    int gridStep = (int)Math.max(brushRadius*gridFac,1);

    // the brush mask
    Kernel brush = new AntiAliasedCircle(brushRadius,aa*brushRadius);

    // display the brush
    //    new ImageFrame(brush.toImage(1),"brush "+brushRadius,100,100);

    brush.scale(opacity);               // make the brush semi-transparent

    Kernel Bn = (Kernel)brush.clone();  // create a normalized version of the brush
    Bn.normalize();                     // (i.e. kernel values sum to 1)

    System.out.println("e = | I - P | ");

    Kernel error = refImage.difference(outputImage);  // pixelwise color error terms

    // gradient error stuff
    if (gradFac != 0)
      {
	System.out.println("Computing gradient error terms");

	IntImage oblur = (IntImage)outputImage.clone();

	//	IntImage oblur = outputImage.gblur(brushRadius);

	//	new ImageFrame(oblur,"painting before brush "+brushRadius);

	Kernel ptgIntensity = oblur.intensity();

	Kernel error2 = refImage.intensity().gradientDifference(ptgIntensity);

	error2.scale(gradFac);

	//	new ImageFrame(error2.toImage(300),"error before brush "+brushRadius);

	error.add(error2);
      }

    Kernel intensity2 = refImage.intensity();    // make an intensity image of the reference

    int n = 0;   // stroke counters
    int m = 0;

    System.out.println("Painting brushstrokes");

    // total error term within a region
    double errsum = 0;

    int length = (int)(brushRadius * lengthFac);   // distance between control points

    // previous distance between control points
    double lastdx = 0;
    double lastdy = 0;

    // clear the Z-buffer
    outputImage.clearZ();

    // divide the image into a regular grid and iterate over each cell in the grid
    for(int x=0;x<refImage.width;x+=gridStep)
      for(int y=0;y<refImage.height;y+=gridStep)
	{
	  double totalErr = 0;      // total error for a cell
	  double maxErr = 0;        // max error within a cell
	  double cx = x;            // current control point location
	  double cy = y;

	  // compute totalErr and maxErr
	  for(int i=x-gridStep/2;i<=x+gridStep/2;i++)
	    for(int j=y-gridStep/2;j<=y+gridStep/2;j++)
	      {
		if (i < 0 || i >= refImage.width || j < 0 || j >= refImage.height)
		  continue;

		double err = error.getLoc(i,j);

		totalErr += err;
		
		if (err > maxErr)
		  {
		    cx = i;
		    cy = j;
		    maxErr = err;
		  }
	      }

	  errsum += totalErr;

	      int z = rand ? randomizer.nextInt() : 0;

	  // compare the total error with the threshold
	  if (totalErr > threshold * gridStep * gridStep)
	    {
	      int color = refImage.getPixel((int)cx,(int)cy);

	      // use the following code for strokes in the style of Litwinowicz or
	      // Haeberli's impressionist
	      //	      	  double dx = intensity2.mult((int)cx,(int)cy,Sobel.SOBEL_X);
		  //	      	  double dy = intensity2.mult((int)cx,(int)cy,Sobel.SOBEL_Y);
		  
		  //		  Stroke str = new HotDogStroke((int)cx,(int)cy,-dy,dx,brushRadius,color,brush);
	      //	  	  strokes.addElement(new CircleStroke((int)cx,(int)cy,color.getColor(),brush));
		  
	      // list of control points in the stroke
	      Vector points = new Vector();

	      // the max error point is the start of the stroke
	      points.addElement(new Point(cx,cy));

	      // find the rest of the control points
	      for(int k=1;k<=maxLength;k++)
		{
		  // compute gradient
		  double dx = intensity2.mult((int)cx,(int)cy,Sobel.SOBEL_X);
		  double dy = intensity2.mult((int)cx,(int)cy,Sobel.SOBEL_Y);

		  // get the gradient magnitude
		  double ms = Math.sqrt(dx*dx+dy*dy);

		  // gradient is too small
		  if (length*ms < 1)
		    break;

		  // compute normal
		  double d = dx;
		  dx = -dy;
		  dy = d;

		  // reverse direction for smoothness
		  if (k > 1 && lastdx*dx+lastdy*dy < 0)
		    {
		      dx *= -1;
		      dy *= -1;
		    }

		  // IIR filter the direction
		  dx = (1-filterFac)*lastdx+filterFac*dx;
		  dy = (1-filterFac)*lastdy+filterFac*dy;

		  // compute the new magnitude
		  ms = Math.sqrt(dx*dx+dy*dy);
		      
		  lastdx = dx;
		  lastdy = dy;
		  
		  // compute the next control point
		  
		  cx = cx + length * dx/ms;
		  cy = cy + length * dy/ms;
		  
		  // make sure the point is still within bounds
		  if (cx < 0 || cy < 0 || cx >= refImage.width ||
		      cy >= refImage.height)
		    break;

		  // get the color under the control point
		  int newColor = refImage.getPixel((int)cx,(int)cy);

		  if (updateError)
		    {
		      if (error.getLoc((int)cx,(int)cy) <
			  Math.sqrt(IntImage.colorDist(color,newColor)) &&
			  k >= minLength)
			break;
		    }
		  else
		    // check the error of the stroke with the control point
		    if (IntImage.colorDist(color,outputImage.
					   getPixel((int)cx,(int)cy)) < 
			IntImage.colorDist(color,newColor)
			&& k >= minLength)
		      break;

		  points.addElement(new Point(cx,cy));
		}

	      Point[] ps = new Point[points.size()];
	      points.copyInto(ps);

	      int newColor = color;

	      // process the stroke color with the hsv multipliers/jitter
	      newColor = IntImage.adjustColor(newColor,
					      hfac,hjit,bfac,bjit,
					      sfac,sjit);

	      // jitter the RGB components
	      if (RGBjit != 0)
		newColor = IntImage.RGBjitter(newColor,RGBjit);	      

	      // create the new stroke
	      Stroke str = new SplineStroke(ps,newColor,brush);


	      // render the stroke
	      str.renderToImage(outputImage,z);

	      illus.imageChanged();

	      str.flush();

	      if (writeSteps)
		step();

	      n++;
	    }
	  m++;
	}
    
    if (writeSteps)
      writeStep();

    System.out.print("   "+n+" strokes, "+(100.0*n/m)+"%, ");
    System.out.println("Ave err = "+(errsum/(m*gridStep*gridStep)));
  }

  // The main procedure -- loads a picture and opens a window for it

  public static void main(String[] args)
  {
    if (args.length < 1)
      {
	System.out.println("usage: java Painter <image-name>");
	return;
      }

    Frame f = new Frame("Painter:"+args[0]);
    Toolkit t = Toolkit.getDefaultToolkit();

    IntImage sourceImage = null;

    try 
      { 
	System.out.println("Reading image file "+args[0]);

	Image source = t.getImage(args[0]);

	sourceImage = new IntImage(source);
      }
    catch (IOException ioe) 
      {
	System.out.println("IOE "+ioe.getMessage());
      }
    catch (InterruptedException e) 
      {
	System.err.println("interrupted waiting for pixels!"); return;
      }

    f.setLayout(new BorderLayout());
    
    f.add("Center",new Painter(sourceImage));
    f.pack();
    f.show();
  }


// hacks for making animation

final boolean writeSteps = false;

final String filename = "ptg-anim-";
int sn = 0;
final int x = 10;
int imct = 0;

public void step()
  {
    sn ++;
    if (sn >= x)
      {
	sn = 0;
	writeStep();
      }
  }

public void writeStep()
  {
    Batch.writeFile(this,filename+(imct++)+".ppm");
  }

}

