// can be used as a convolution kernel or as a mask

import java.lang.String;

public class Kernel
{
  protected double[] alpha;
  int width;
  int height;
  int originX;   // position of true origin in data coordinates
  int originY;
 
  public Kernel(double[] alpha,int width,int height,int originX,int originY)
  {
    this.alpha = alpha;
    this.width = width;
    this.height = height;
    this.originX = originX;
    this.originY = originY;
  }

  public Kernel(int width,int height,int originX,int originY)
  {
    this.alpha = new double[width * height];
    this.width = width;
    this.height = height;
    this.originX = originX;
    this.originY = originY;
  }

  public Kernel(int width,int height)
  {
    this(width,height,0,0);
  }

  protected Kernel()
  {

  } 

  public double getLoc(int x,int y)
  {
    return alpha[x + y*width];
  }

  public void setLoc(int x,int y,double value)
  {
    alpha[x + y*width] = value;
  }

  // reflected image
  public double getRefLoc(int x,int y)
  {
    //    System.out.println("x = "+x+" y="+y);

    int x1 = x;
    int y1 = y;

    if (x1 < 0)
      x1 = -x1;
    else
    if (x1 >= width-1)
      x1 = 2*width - x1-2;

    if (y1 < 0)
      y1 = -y1;
    else
      if (y1 >= height-1)
	y1 = 2*height - y1-2;
 
    //    System.out.println("x1 = "+x1+" y1="+y1);
    //    System.out.println("width = "+width+", height = "+height);


    return getLoc(x1,y1);
  }      

  public void normalize()
  {
    double sum = 0;

    for(int i=0;i<alpha.length;i++)
      sum += alpha[i];

    if (sum == 0)
      return;

    for(int i=0;i<alpha.length;i++)
      alpha[i] /= sum;
  }

  public void scale(double fac)
  {
    for(int i=0;i<alpha.length;i++)
      alpha[i] *= fac;
  }

  public Kernel convolve(Kernel k)
       // ignores edge effects
  {
    Kernel result = new Kernel(width,height,originX,originY);

    for(int x=0;x<width;x++)
      for(int y=0;y<height;y++)
	{
	  double val = 0;

	  for(int i=0;i<k.width;i++)
	    for(int j=0;j<k.height;j++)
	      {
		int newX = x + i - k.originX;
		int newY = y + j - k.originY;

		if (newX < 0 || newX >= width || 
		    newY < 0 || newY >= height)
		  continue;

		val += getLoc(newX,newY) * k.getLoc(i,j);
	      }

	  result.setLoc(x,y,val);
	}

    return result;
  }

  public String toString()
  {
    StringBuffer sb = new StringBuffer();

    for(int y=0;y<height;y++)
      {
	for(int x=0;x<width;x++)
	  {
	    sb.append(getLoc(x,y));
	    sb.append("  ");
	  }

	sb.append("\n");
      }

    return new String(sb);
  }

  public void print()
  {
    for(int y=0;y<height;y++)
      {
	for(int x=0;x<width;x++)
	  {
	    System.out.print(getLoc(x,y));
	    System.out.print("  ");
	  }

	System.out.print("\n");
      }
  }

  public Object clone()
  {
    return new Kernel((double[])alpha.clone(),width,height,originX,originY);
  }

  public IntImage toImage(double maxVal)
  {
    IntImage img = new IntImage(width,height);

    for(int x=0;x<width;x++)
      for(int y=0;y<height;y++)
	{
	  double val = alpha[x+y*width];

	  val = 256*val/maxVal;

	  int intv = (int)val;

	  if (intv < 0)
	    intv = 0;
	  else
	    if (intv > 255)
	      intv = 255;

	  img.setPixel(x,y,IntImage.grayToColor(intv));
	}

    return img;
  }

  public double mult(int x,int y,Kernel k)
  { 
    double sum = 0;

    for(int i=0;i<k.width;i++) 
      for(int j=0;j<k.height;j++)
	{ 
	  int newX = x+i-k.originX; 
	  int newY = y+j-k.originY;

	  if (newX < 0 || newY < 0 || newX >= width || newY >= height)
	  continue;

	  sum += getLoc(newX,newY)*k.getLoc(i,j);
	}

    return sum;
  }

  public void maxPaint(int x,int y,Kernel mask)
  {
    for(int i=0;i<mask.width;i++)
      for(int j=0;j<mask.height;j++)
	{
	  int newX = x+originX+i-mask.originX;
	  int newY = y+originY+j-mask.originY;

	  if (newX < 0 || newY < 0 || newX >= width || newY >= height)
	    continue;

	  setLoc(newX,newY,Math.max(getLoc(newX,newY),
				    mask.getLoc(i,j)));
	}
  }

  public void maxPaintLine(int x1,int y1,int x2,int y2,Kernel mask)
  {
    if (x2 == x1)
      {
	int ya = Math.min(y1,y2);
	int yb = Math.max(y1,y2);

	for(int y=ya;y<=yb;y++)
	  maxPaint(x1,y,mask);
      }
    else
      {
	int xa,xb,ya,yb;

	if (x1 < x2)
	  {
	    xa = x1;
	    xb = x2;
	    ya = y1;
	    yb = y2;
	  }
	else
	  {
	    xa = x2;
	    xb = x1;
	    ya = y2;
	    yb = y1;
	  }

	double m = ((double)yb-ya)/(xb-xa);
	
	double y = ya;

	for(int x=xa;x<=xb;x++)
	  {
	    maxPaint(x,(int)y,mask);
	    y+=m;
	  }
      }
  }

  public void addPixel(int x,int y,double value)
  {
    alpha[x+y*width] += value;
  }

  public void add(Kernel k,double fac)
  {
    if (fac == 1)
      add(k);
    else
      for(int i=0;i<alpha.length;i++)
	alpha[i] += (fac*k.alpha[i]);
  }

  public void add(Kernel k)
  {
    for(int i=0;i<alpha.length;i++)
      alpha[i] += k.alpha[i];
  }

  public Kernel gradientDifference(Kernel i2)
  {
    Kernel k = new Kernel(width,height,originX,originY);

    for(int x=0;x<width;x++)
      for(int y=0;y<height;y++)
	{
	  // gradient difference is the magnitude of the difference
	  // between the vectors

	  // this computation could be sped by separating Sobel
	  // and taking the difference before the second convolution
	  // (or something like that)

	  double xpg = mult(x,y,Sobel.SOBEL_X);
	  double ypg = mult(x,y,Sobel.SOBEL_Y);
	  double xsg = i2.mult(x,y,Sobel.SOBEL_X);
	  double ysg = i2.mult(x,y,Sobel.SOBEL_Y);

	  double dx = xpg - xsg;
	  double dy = ypg - ysg;

	  k.setLoc(x,y,Math.sqrt(dx*dx+dy*dy));
	}
    return k;
  }

  // angle between two vectors

  public static double angleBetween(double x1,double y1,double x2,double y2)
  {
    double theta1 = Math.atan2(y1,x1);
    double theta2 = Math.atan2(y2,x2);

    double d = theta1-theta2;
    double d1 = Math.abs(d);
    double d2 = Math.abs(d+Math.PI);

    return Math.min(d1,d2);
  }

  public Kernel gradientMagnitude()
  {
    Kernel k = new Kernel(width,height,originX,originY);

    for(int x=0;x<width;x++)
      for(int y=0;y<height;y++)
	{
	  double dx = mult(x,y,Sobel.SOBEL_X);
	  double dy = mult(x,y,Sobel.SOBEL_Y);

	  k.setLoc(x,y,Math.sqrt(dx*dx+dy*dy));
	}
    return k;
  }

  Kernel threshold(double t)
  {
    Kernel k = new Kernel(width,height,originX,originY);

    for(int x=0;x<width;x++)
      for(int y=0;y<height;y++)
	{
	  double v = getLoc(x,y);

	  k.setLoc(x,y,v < t ? 0 : v);
	}
    return k;
  }    

  // diffusivity
  public static double g(double s,double lambda)
  {
    if (s <= 0)
      return 1;

    return 1-Math.exp(-3.315/Math.pow(s/lambda,4));
  }

  static final double h = 3;

  static boolean showImages = false;
  
  public Kernel diffusivity(double sigma,double lambda)
  {
    System.out.println("   blurring");
    Kernel g = convolve(new Gaussian((int)(4*sigma),sigma));

    if (showImages)
      new ImageFrame(g.toImage(255),"Blurred Intensity");

    System.out.println("   computing diffusivity");

    Kernel d = new Kernel(width,height,0,0);

    for(int x=0;x<width;x++)
      for(int y=0;y<height;y++)
	{
	  double diffX = (g.getRefLoc(x-1,y)-g.getRefLoc(x+1,y))/(2*h);
	  double diffY = (g.getRefLoc(x,y-1)-g.getRefLoc(x,y+1))/(2*h);

	  d.setLoc(x,y,(diffX*diffX+diffY*diffY)/2);;
	}

    if (showImages)
      new ImageFrame(d.toImage(8),"Edge Image");

    for(int i=0;i<d.alpha.length;i++)
      d.alpha[i] = g(d.alpha[i],lambda);

    if (showImages)
      new ImageFrame(d.toImage(1),"Diffusivity");

    return d;
  }

  public Kernel clmc(Kernel diffus, double tau,String n)
  {
    Kernel result = new Kernel(width,height,0,0);

    System.out.println("   diffusing");

    for(int x=0;x<width;x++)
      for(int y=0;y<height;y++)
	{
	  double gi,gj,ui,uj;

	  ui = getLoc(x,y);
	  gi = diffus.getLoc(x,y);
	  
	  double s = 0;

	  // horizontal diffusion
	  if (x+1 <width)
	    {
	      uj = getLoc(x+1,y);
	      gj = diffus.getLoc(x+1,y);

	      s += (gi+gj)*(uj-ui);
	    }    

	  if (x > 0)
	    {
	      uj = getLoc(x-1,y);
	      gj = diffus.getLoc(x-1,y);

	      s += (gi+gj)*(uj-ui);
	    }    

	  // vertical diffusion
	  if (y+1 <height)
	    {
	      uj = getLoc(x,y+1);
	      gj = diffus.getLoc(x,y+1);

	      s += (gi+gj)*(uj-ui);
	    }    

	  if (y > 0)
	    {
	      uj = getLoc(x,y-1);
	      gj = diffus.getLoc(x,y-1);

	      s += (gi+gj)*(uj-ui);
	    }    

	  //	  System.out.println("delta = "+(tau*s/(2*h*h)));
	  
	  result.setLoc(x,y,tau*s/(2*h*h) + getLoc(x,y));
	}
    if (showImages)
      new ImageFrame(result.toImage(1000),"Diffused "+n);

    return result;
  } 
}


