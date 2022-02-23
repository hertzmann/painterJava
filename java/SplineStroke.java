import java.lang.String;
import java.awt.Rectangle;
import java.lang.Math;
import java.util.Vector;

final class SplineStroke extends Stroke
{
  Point[] control;   // control polygon
  Kernel mask;       // brush mask
  
  // the following data is saved to avoid recomputation
  Point[] limit = null;   // limit curve
  Kernel image = null;     // rendered mask of the curve

  SplineStroke(int x,int y,int color,Kernel mask)
  {
    control = new Point[] { new Point(x,y) };
    this.color = color;
    this.mask = mask;
  }

  SplineStroke(int x1,int y1,int x2,int y2,int color,Kernel mask)
  {
    control = new Point[] { new Point(x1,y1), new Point(x2,y2) };
    this.color = color;
    this.mask = mask;
  }

  SplineStroke(Point[] cp,int color,Kernel mask)
  {
    this.control = cp;
    this.color = color;
    this.mask = mask;
  }

  Rectangle bbox = null;

  // compute the bounding box of this spline

  public Rectangle boundingBox(int maxWidth,int maxHeight) 
  {
    // render the curve first, if necessary 
    if (limit == null) 
      computeLimitCurve();

    if (bbox != null) 
      return bbox;

    int w2 = mask.width/2;
    int h2 = mask.height/2;
    
    int x1 = (int)limit[0].x - w2;
    int y1 = (int)limit[0].y - h2;
    int x2 = (int)limit[0].x + w2;
    int y2 = (int)limit[0].y + h2;

    for(int i=1;i<limit.length;i++) 
      {
	x1 = (int)Math.min(x1,limit[i].x-w2); 
	y1 = (int)Math.min(y1,limit[i].y-h2); 
	x2 = (int)Math.max(x2,limit[i].x+w2);
	y2 = (int)Math.max(y2,limit[i].y+h2);
      }

    x1 = Math.max(x1,0); 
    y1 = Math.max(y1,0); 
    x2 = Math.min(x2,maxWidth-1); 
    y2 = Math.min(y2,maxHeight-1);

    bbox = new Rectangle(x1,y1,x2-x1,y2-y1);

    if (x2 < x1 || y2 < y1)
      System.out.println("Negative dimensions!");

    return bbox;
  }

  // cubic B-spline interpolation

  void computeLimitCurve() 
  {
    limit = (Point[])control.clone();

    //    System.out.println("Computing limit curve");

    while(!limitIsDone()) 
      {
	//	System.out.println("Subdividing");

	Point[] split = new Point[limit.length*2-1];

	for(int i=0;i<split.length;i++)
	  {
	    if ((i % 2) == 0) 
	      split[i] = limit[i/2];
	    else
	      split[i] = limit[i/2].ave(limit[i/2+1]); 
	  }

	limit = new Point[split.length];

	limit[0] = split[0]; 
	limit[limit.length-1] = split[split.length-1];

	for(int i=1;i<limit.length-1;i++)
	  {
	    limit[i] = split[i-1].mult(.25).add(split[i].mult(.5)).
	      add(split[i+1].mult(.25)); 
	  }
      }

    //    System.out.println("Done with curve");
  }

  static final double THETA_TOL = .1;   //.0001; 
  static final int NEIGHBORHOOD = 2;

  static boolean clean = false;

  // check if more subdivision is required
  // if not, remove unnecessary points from the limit curve
  boolean limitIsDone() 
  {
    if (limit.length < 3)
      return true;

    double dx = limit[1].x - limit[0].x;
    double dy = limit[1].y - limit[0].y;

    double lastTheta = Math.atan2(dy,dx);

    for(int i=2;i<limit.length;i++) 
      {
	double newDx = limit[i].x - limit[i-1].x; 
	double newDy = limit[i].y - limit[i-1].y;

	double newTheta = Math.atan2(newDy,newDy);

	double tDiff = Math.abs(newTheta-lastTheta) % Math.PI;

	//	System.out.println("newDx = "+newDx+", newDy = "+newDy+", tDiff = "+tDiff);

	if ((newDx >= NEIGHBORHOOD || newDy >= NEIGHBORHOOD) && 
	    tDiff > THETA_TOL) 
	  return false; 
	
	dx = newDx; 
	dy = newDy; 
	lastTheta = newTheta;
      }

    // clean out extra points from limit curve

    Vector pts = new Vector();

    pts.addElement(limit[0]);

    Point lastPt = limit[0];

    for(int i=1;i<limit.length-1;i++)
      {
	Point curPt = limit[i];
	Point nextPt = limit[i+1];

	lastTheta = Math.atan2(curPt.y - lastPt.y,curPt.x - lastPt.x);

	double newTheta = Math.atan2(nextPt.y - curPt.y,nextPt.x - curPt.x);

	double tDiff = Math.abs(newTheta-lastTheta) % Math.PI;

	//	System.out.println("Theta diff = "+tDiff);

	if (tDiff > THETA_TOL)
	  {
            pts.addElement(curPt);

	    lastPt = curPt;
	  }
      }

    if (!clean || pts.size() == limit.length-1)
      return true;

    pts.addElement(limit[limit.length-1]);

    System.out.println("Cleaned "+limit.length +" to "+pts.size());

    limit = new Point[pts.size()];
    pts.copyInto(limit);

    return true;
  }

  public Kernel render(int maxWidth,int maxHeight) 
  {
    if (image != null)
      return image;

    boundingBox(maxWidth,maxHeight);

    image = new Kernel(bbox.width,bbox.height,-bbox.x,-bbox.y);

    if (limit.length == 1)
      {
	image.maxPaint((int)limit[0].x,(int)limit[0].y,mask);

	return image;
      }

    for(int i=0;i<limit.length-1;i++)
      {
	image.maxPaintLine((int)limit[i].x,(int)limit[i].y,(int)limit[i+1].x,(int)limit[i+1].y,
			   mask);
      }    

    return image;
  }

  public void flush()
  {
    limit = null;
    image = null;
  }

  public void renderToImage(IntImage image,Kernel edgeImage,int z)
  {
    if (limit == null) 
      computeLimitCurve();
    
    image.newStroke(color);

    if (limit.length == 1)
      {
	image.bpaint((int)limit[0].x,(int)limit[0].y,z,mask);

	return;
      }

    if (edgeImage == null)
      for(int i=0;i<limit.length-1;i++)
	{
	  image.bpaintline((int)limit[i].x,(int)limit[i].y,(int)limit[i+1].x,
			   (int)limit[i+1].y,z,mask);
	}    
    else
      for(int i=0;i<limit.length-1;i++)
	{
	  boolean r = 
	    image.bpaintline((int)limit[i].x,(int)limit[i].y,(int)limit[i+1].x,
			     (int)limit[i+1].y,z,mask,edgeImage);

	  if (!r)
	    return;
	}    
  }
}
