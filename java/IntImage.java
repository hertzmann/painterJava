// General image class for basic image-processing functionality

import java.awt.*;
import java.awt.image.*;
import java.io.*;

final public class IntImage implements Serializable
{
  int[] pixels;
  int width;
  int height;

  IntImage(Image source) throws IOException, InterruptedException
  {
    PixelGrabber pg = new PixelGrabber(source, 0, 0, -1, -1, true); 

    int i=0;

    while (!pg.grabPixels() && i++ < 50)
      System.out.println("error - retrying");

    pg.grabPixels();

    if ((pg.getStatus() & ImageObserver.ABORT) != 0) 
      {
	System.err.println("image fetch aborted or errored");
	System.err.println("status = "+Integer.toHexString(pg.getStatus()));
	return;
      }

    pixels = (int[])pg.getPixels();

    width = pg.getWidth();
    height = pg.getHeight();
  }

  public int getPixel(int x,int y)
  {
    return pixels[x+y*width];
  }

  public void setPixel(int x,int y,int color)
  {
    pixels[x+y*width] = color;
  }

  public IntImage(int[] pixels,int width,int height)
  {
    this.pixels = pixels;
    this.width = width;
    this.height = height;
  }

  public IntImage(int width,int height)
  {
    this.width = width;
    this.height = height;
    pixels = new int[width * height];
  }

  public IntImage(int width,int height,int color)
  {
    this(width,height);

    if (color == 0)
      return;

    for(int i=0;i<pixels.length;i++)
      pixels[i] = color;
  }

  public Object clone()
  {
    return new IntImage((int[])pixels.clone(),width,height);
  }

  public IntImage convolve(Kernel kernel)
       // ignores edge effects

       // not true convolution, since we traverse image and kernel in the
       // same directions.  Same if the kernel is symmetric
  {
    IntImage newImage = new IntImage(width,height);

    for(int x=0;x<width;x++)
      for(int y=0;y<height;y++)
	newImage.setPixel(x,y,mult(x,y,kernel));

    return newImage;
  }

  public int mult(int x,int y,Kernel kernel)
  {
    double red = 0;
    double green = 0;
    double blue = 0;

    for(int i=0;i<kernel.width;i++)
      for(int j=0;j<kernel.height;j++)
	{
	  int newX = x + i - kernel.originX;
	  int newY = y + j - kernel.originY;

	  if (newX < 0 || newX >= width || 
	      newY < 0 || newY >= height)
	    continue;

	  double fac = kernel.getLoc(i,j);
	  int c = getPixel(newX,newY);

	  red += (c & 0xFF0000) * fac;
	  green += (c & 0xFF00) * fac;
	  blue += (c & 0xFF) * fac;

	}

    int r  = Math.min((int)red,0xFF0000) & 0xFF0000;
    int g = Math.min((int)green,0xFF00) & 0xFF00;
    int b = Math.min((int)blue,0xFF);

    if (red < 0)
      r = 0;
    if (green < 0)
      g = 0;
    if (blue < 0)
      b = 0;

    return 0xFF000000 | r | g | b;
  }

  // the returned array is a set of error values rather than colors.
  public Kernel difference(IntImage i2)
  {
    double[] result = new double[width * height];

    for(int x=0;x<width;x++)
      for(int y=0;y<height;y++)
	result[x+y*width] = Math.sqrt(colorDist(getPixel(x,y),
						i2.getPixel(x,y)));

    return new Kernel(result,width,height,0,0);
  }

  public long totalError(IntImage i2)
  {
    return localError(i2,0,0,width-1,height-1);
  }

  public long localError(IntImage i2,int x1,int y1,int x2,int y2)
  {
    long result = 0;

    for(int x=x1;x<=x2;x++)
      for(int y=y1;y<y2;y++)
	result += (long)Math.sqrt(colorDist(getPixel(x,y),
					    i2.getPixel(x,y)));

    return result;
  }

  public void paint(int x,int y,Kernel mask,int color)
  {
    for(int i=0;i<mask.width;i++)
      for(int j=0;j<mask.height;j++)
	{
	  int newX = x + i - mask.originX;
	  int newY = y + j - mask.originY;

	  if (newX < 0 || newX >= width || 
	      newY < 0 || newY >= height)
	    continue;

	  int oldColor = getPixel(newX,newY);
	  int newColor = alphaBlend(color,oldColor,mask.getLoc(i,j));

	  setPixel(newX,newY,newColor);
	}
  }

  public static int colorDist(int c1,int c2)
    {
      if ( (c1 >>> 24) < 0xFF || ( c2 >>> 24) < 0xFF)
	return Integer.MAX_VALUE;

      if (Painter.luvDistance)
	{
	  double[] luv1 = RGBtoLUV(c1);
	  double[] luv2 = RGBtoLUV(c2);
	  
	  double diff = 0;
	  for(int i=0;i<3;i++)
	    {
	      double d= luv1[i] - luv2[i];
	      diff += d*d;
	    }
	  return (int)diff; 
	}

      int r1 = (c1 & 0xFF0000) >> 16;
      int r2 = (c2 & 0xFF0000) >> 16;

      int g1 = (c1 & 0xFF00) >> 8;
      int g2 = (c2 & 0xFF00) >> 8;

      int b1 = c1 & 0xFF;
      int b2 = c2 & 0xFF;

      int dr = r1-r2;
      int dg = g1-g2;
      int db = b1-b2;

      return dr*dr+dg*dg+db*db;

    }

  public static int colorAdd(int c1,int c2)
    {
      int r1 = c1 & 0xFF0000;
      int r2 = c2 & 0xFF0000;

      int g1 = c1 & 0xFF00;
      int g2 = c2 & 0xFF00;

      int b1 = c1 & 0xFF;
      int b2 = c2 & 0xFF;

      int sr = Math.min(r1+r2,0xFF0000);
      int sg = Math.min(g1+g2,0xFF00);
      int sb = Math.min(b1+b2,0xFF);

      return 0xFF000000 | sr | sg | sb;
    }

  public static int colorMult(int c1,double fac)
    {
      // common special cases
      if (fac == 0)
	return 0xFF000000;
      
      if (fac == 1)
	return c1;
       
      int r1 = c1 & 0xFF0000;
      int g1 = c1 & 0xFF00;
      int b1 = c1 & 0xFF;

      int mr = Math.min((int)(r1 * fac),0xFF0000) & 0xFF0000;
      int mg = Math.min((int)(g1 * fac),0xFF00) & 0xFF00;
      int mb = Math.min((int)(b1 * fac),0xFF);

      return 0xFF000000 | mr | mg | mb;
    }

  public static int alphaBlend(int c1,int c2,double alpha1)
  {
    //    return colorAdd(colorMult(c1,alpha1),   // has roundoff error
    //		    colorMult(c2,1-alpha1));


    // background color -- this line prevents those ugly speckles from
    // appearing in the image (they are due to the Z-buffer algorithm +
    // color backing + alpha blending)
      if ( ( c2 >>> 24) < 0xFF)
	return c1;

      int r1 = c1 & 0xFF0000;
      int r2 = c2 & 0xFF0000;
      int rn = (int)(alpha1 * r1 + (1-alpha1) * r2);

      int g1 = c1 & 0xFF00;
      int g2 = c2 & 0xFF00;
      int gn = (int)(alpha1 * g1 + (1-alpha1)*g2);

      int b1 = c1 & 0xFF;
      int b2 = c2 & 0xFF;
      int bn = (int)(alpha1 * b1 + (1-alpha1)*b2);

      int rnn = Math.min(rn,0xFF0000) & 0xFF0000;
      int gnn = Math.min(gn,0xFF00) & 0xFF00;
      int bnn = Math.min(bn,0xFF);

      return 0xFF000000 | rnn | gnn | bnn;
  }

  public static int grayToColor(int gray)
  {
    int g = Math.min(gray,0xFF);

    return 0xFF000000 | (g << 16) | (g << 8) | g;
  }

  public static short colorToLuminance(int color)
  {
    int r = (color & 0xFF0000) >> 16;
    int g = (color & 0xFF00) >> 8;
    int b = color & 0xFF;

    return (short)((30*r+59*g+11*b)/100);
  }

  public ImageProducer makeImage()
  {
    return new MemoryImageSource(width,height,pixels,0,width);
  }

  public Kernel intensity()
  {
    Kernel k = new Kernel(width,height,0,0);

    for(int x=0;x<width;x++)
      for(int y=0;y<height;y++)
	{
	  k.setLoc(x,y,colorToLuminance(getPixel(x,y)));
	}

    return k;
  }

  public double colorGradient(int x,int y,boolean xdir)
  {
    double sum = 0;

    if (x < 1 || y < 1 || x>=width-1 || y>=height-1)
      return 0;

    if (xdir)
      {
	for(int i=y-1;i<=y+1;i++)
	  sum += Math.sqrt(colorDist(getPixel(x-1,i),getPixel(x+1,i)));
      }
    else
      {
	for(int i=x-1;i<x+1;i++)
	  sum += Math.sqrt(colorDist(getPixel(i,y-1),getPixel(i,y+1)));
      }

    return sum;
  }

  public ImageProducer subImage(Rectangle bounds)
  {
    int[] im = new int[bounds.width * bounds.height];

    for(int x=0;x<bounds.width;x++)
      for(int y=0;y<bounds.height;y++)
	im[x+y*bounds.width] = getPixel(x+bounds.x,y+bounds.y);
	
    return new MemoryImageSource(bounds.width,bounds.height,im,0,0);
  }

  //  public MemoryImageSource subImage(Rectangle bounds)
  //  {
  //    return new MemoryImageSource(bb.width,bb.height,pixels,bb.x,

  public static int adjustColor(int color,double hfac,double hjit,
				double sfac,double sjit,
				double bfac,double bjit)
  {
    int r = (color & 0xFF0000) >>16;
    int g = (color & 0xFF00) >> 8;
    int b = color & 0xFF;

    //    System.out.println("hfac = "+hfac+", hjit = "+hjit+", sfac = "+sfac+", sjit = "+sjit+", bfac = "+bfac+", bjit = "+bjit);

    //    System.out.println("r = "+r);
    //        System.out.println("g = "+g);
    //        System.out.println("b = "+b);

    //    System.out.println(Integer.toHexString(color));

    float[] f = Color.RGBtoHSB(r,g,b,null);

    //            System.out.println("h = "+f[0]);
    //            System.out.println("s = "+f[1]);
    //            System.out.println("b = "+f[2]);


    // HSB may be in the wrong order somehow

    //    f[0] = (float)(sfac*(f[0] + sjit*(Math.random()-.5)));
    //    f[0] = (float)(f[0] - Math.floor(f[0]));  // hue is periodic; in 0..1

    f[0] = (float)(hfac*(f[0] + hjit*(Math.random()-.5)));
    //    f[0] = (float)(f[0] - Math.floor(f[0]));  // hue is periodic; in 0..1
    if (f[0] > 1)
      f[0] -= 1;
    
    f[1] = (float)(sfac*(f[1] + sjit*(Math.random()-.5)));
    f[1] = Math.min(Math.max(f[1],0),1);

    f[2] = (float)(bfac*(f[2] + bjit*(Math.random()-.5)));
    f[2] = Math.min(Math.max(f[2],0),1);

    //    System.out.println("h = "+f[0]);
    //    System.out.println("s = "+f[1]);
    //    System.out.println("b = "+f[2]);
    
    int newColor = Color.HSBtoRGB(f[0],f[1],f[2]);
    //    int newColor = HSVtoRGB(f[0],f[1],f[2]);

    //    System.out.println(Integer.toHexString(newColor));

    //    System.out.println("====");

    return newColor;
  }

  public static int RGBjitter(int color,double jfac)
  {
    long r = (color & 0xFF0000);
    int g = (color & 0xFF00);
    int b = color & 0xFF;

    r += jfac*(Math.random()-.5)*0xFF0000;
    g += jfac*(Math.random()-.5)*0xFF00;
    b += jfac*(Math.random()-.5)*0xFF;

    r = Math.min(Math.max(r,0),0xFF0000) & 0xFF0000;
    g = Math.min(Math.max(g,0),0xFF00) &   0xFF00;
    b = Math.min(Math.max(b,0),0xFF) &     0xFF;

    return 0xFF000000 | ((int)r) | g | b;
  }

  // multiplies the luminance in the color by lfac
  // doesn't seem to work
  public static int adjustLuminance(int color,double lfac)
  {
    long r = (color & 0xFF0000);
    int g = (color & 0xFF00);
    int b = color & 0xFF;

    double lum = colorToLuminance(color);

    double ladd = lum * (lfac - 1);

    r += 0xFF0000 * ladd;
    g += 0xFF00 * ladd;
    b += ladd;

    r = Math.min(Math.max(r,0),0xFF0000) & 0xFF0000;
    g = Math.min(Math.max(g,0),0xFF00) &   0xFF00;
    b = Math.min(Math.max(b,0),0xFF) &     0xFF;

    return 0xFF000000 | ((int)r) | g | b;
  }
				   

  public int aveColor()
  {
    int r = 0;
    int g = 0;
    int b = 0;

    for(int i=0;i<pixels.length;i++)
      {
	int p = pixels[i];
	
	r += (p >> 16) & 0xFF;
	g += (p >> 8) & 0xFF;
	b += p & 0xFF;
      }

    r = Math.min(r,0xFF);
    g = Math.min(g,0xFF);
    b = Math.min(b,0xFF);

    return 0xFF000000 | (r << 16) | (g << 8) | b;
  }

  /*  // from foley-van dam et al:
  public static int HSVtoRGB(float h,float s,float v)
  {
    if (s == 0)
      {
	int vi = (int)(v * 255);

	return 0xFF000000 | (vi << 16) | (vi << 8) | vi;
      }

    double f,p,q,t,r=0,g=0,b=0;
    int i;

    double h2 = 360 * h;

    if (h2 == 360)
      h2 = 0;

    h2 /= 60;

    i = (int)Math.floor(h2);

    f = h2-i;
    p = v*(1-s);
    q = v*(1-s*f);
    t = v*(1-(s*(1-f)));

    switch(i)
      {
      case 0: r=v; g=t; b=p; break;
      case 1: r=q; g=v; b=p; break;
      case 2: r=p; g=v; b=t; break;
      case 3: r=p; g=q; b=v; break;
      case 4: r=t; g=p; b=v; break;
      case 5: r=v; g=p; b=q; break;
      }

    int rr=((int)(r*0xFF0000)) & 0xFF0000;
    int gg=((int)(g*0xFF00)) & 0xFF00;
    int bb=((int)(b*0xFF)) & 0xFF;

    return 0xFF000000 | rr | gg | bb;
  }
  */  
  // --------------------------------
  // Routines for efficient compositing of multiple brushes
  //
  // We want to be able to paint with multiple masks.  We paint
  // with a mask by alpha-blending the brush color with the entries
  // in the mask.  If two masks overlap, we alpha-blend with the
  // larger mask value.
  
  // The problem is that we need to remember what masks we've
  // painted already, so that we can take the maximum mask value.
  // A naive implementation would keep a array of flag bits and mask
  // values for each pixel.  Before painting a stroke, we zero the
  // flags and mask bits.  This requires a pass over the entire image
  // before every stroke.

  // We avoid this extra pass by using counters instead of flag bits.
  // Each pixel has an associated counter.
  // The counters are all reset to -1 before any painting occurs.
  // The "curCtr" is incremented before every new stroke, and begins
  // at 0.  Another array, "oldColors" stores the color of each pixel
  // before the current stroke, and "totalMask" stores the mask
  // painted at each pixel so far.  The key is that we can determine
  // whether a certain pixel in "oldColors" or "totalMask" is valid
  // simply by testing "counters[i] < curCtr".  When we process a
  // pixel for the first time, we set it's counter to "curCtr" and 
  // initialize the "oldColors" and "totalMask."  Thus each paint
  // operation takes time linear in the size of the mask, which is
  // asymptotically optimal.

  // Additionally, we use a z-buffer to randomize stroke order.

  int[] counters = null;
  int[] oldColors = null;
  int[] zbuffer = null;
  double[] totalMask = null;

  int curCtr = 0;
  int curColor;

  public void makeFlags()
  {
    if (counters != null)
      return;

    counters = new int[pixels.length];
    totalMask = new double[pixels.length];
    oldColors = new int[pixels.length];
    zbuffer = new int[pixels.length];

    for(int i=0;i<counters.length;i++)
      counters[i] = -1;
      
    curCtr = 0;
  }

  public void clearZ()
  {
    for(int i=0;i<zbuffer.length;i++)
      zbuffer[i] = Integer.MAX_VALUE;
  }

  public void bpaint(int x,int y,int z,Kernel mask)
  {
    for(int i=0;i<mask.width;i++)
      for(int j=0;j<mask.height;j++)
	{
	  int newX = x + i - mask.originX;
	  int newY = y + j - mask.originY;

	  if (newX < 0 || newX >= width || 
	      newY < 0 || newY >= height)
	    continue;

	  double alpha = mask.getLoc(i,j);

	  int index = newX+newY*width;

	  if (zbuffer[index] < z)
	      continue;

	  if (counters[index] < curCtr)
	    {
	      counters[index] = curCtr;
	      oldColors[index] = pixels[index];
	      totalMask[index] = alpha;

	      pixels[index] = alphaBlend(curColor,oldColors[index],
					 totalMask[index]);
	      zbuffer[index] = z;
	    }
	  else
	    if (totalMask[index] < alpha)
	      {
		totalMask[index] = alpha;
		pixels[index] = alphaBlend(curColor,oldColors[index],alpha);

		zbuffer[index] = z;
	      }
	}
  }

  public void bpaintline(int x1,int y1,int x2,int y2,int z,Kernel mask)
  {
    if (x2 == x1)
      {
	int ya = Math.min(y1,y2);
	int yb = Math.max(y1,y2);

	for(int y=ya;y<=yb;y++)
	  bpaint(x1,y,z,mask);
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
	    bpaint(x,(int)y,z,mask);
	    y+=m;
	  }
      }
  }

  // returns false if the line was halted at an edge;
  // otherwise, paints the entire line and returns true
  public boolean bpaintline(int x1,int y1,int x2,int y2,int z,Kernel mask,
			    Kernel edgeImage)
  {
    if (x2 == x1)
      {
	int ya = Math.min(y1,y2);
	int yb = Math.max(y1,y2);

	for(int y=ya;y<=yb;y++)
	  bpaint(x1,y,z,mask);
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

	double lastSample = edgeImage.getLoc(xa,ya);

	for(int x=xa;x<=xb;x++)
	  {
	    double newSample = edgeImage.getLoc(x,(int)y);

	    if (newSample < lastSample)
	      return false;

	    bpaint(x,(int)y,z,mask);
	    y+=m;
	  }
      }

    return true;
  }

  public void newStroke(int color)
  {
    curCtr ++;
    curColor = color;
  }

  // a test fcn
  public static void main(String[] args)
  {
    int r = (int)(0xFFFFFF * Math.random()) | 0xFF000000;

    Frame f = new Frame();
    f.setBackground(new Color(r));
    f.setSize(100,100);
    f.show();

    System.out.println("ran = "+Integer.toHexString(r)+" => ");

    int newColor = adjustColor(r,1,1,1,0,1,0);

    f = new Frame();
    f.setBackground(new Color(newColor));
    f.setSize(100,100);
    f.show();

    System.out.println(Integer.toHexString(newColor));
  }

public static float[] RGBtoHSB(int color)
  {
    int r = (color & 0xFF0000) >> 16;
    int g = (color & 0xFF00) >> 8;
    int b = (color & 0xFF);

    float f[] = Color.RGBtoHSB(r,g,b,null);

    return f;

  }

  // from Foley-Van Dam: NTSC RGB
  static final double xr = .67;
  static final double xg = .21;
  static final double xb = .14;
  static final double yr = .33;
  static final double yg = .71;
  static final double yb = .08;
  static final double Yr = .299;
  static final double Yg = .587;
  static final double Yb = .114;
  
  static final double Xw = 98.1012658228;
  static final double Yw = 100;
  static final double Zw = 118.35443038;

  static final double upw = 4*Xw/(Xw+15*Yw+3*Zw);
  static final double vpw = 9*Yw/(Xw+15*Yw+3*Zw);
  
  public static double[] RGBtoLUV(int color)
  {
    int r = (color & 0xFF0000) >> 16;
    int g = (color & 0xFF00) >> 8;
    int b = (color & 0xFF);

    double x = xr*r+xg*g+xb*b;
    double y = yr*r+yg*g+yb*b;

    double Y = Yr*r+Yg*g+Yb*b;
    double X = x*Y/y;
    double Z = (1-x-y)*Y/y;

    double denom = X+15*Y+3*Z;
    double up = 4*X/denom;
    double vp = 9*Y/denom;

    double L = 116 * Math.pow(Y/Yw,1.0/3)-16;
    double u = 13 * L * (up-upw);
    double v = 13 * L * (vp-vpw);

    return new double[] { L, u, v };
  }

  public static Kernel intensity(Kernel[] channels)
  {
    Kernel k = new Kernel(channels[0].width,channels[0].height);

    for(int i=0;i<k.alpha.length;i++)
      {
	k.alpha[i] = 0.3*channels[0].alpha[i]+0.59*channels[1].alpha[i]+
	  0.11*channels[2].alpha[i];
      }
    
    return k;
  }

  public IntImage diffuse(double sigma,double lambda,double tau,double time)
  {
    System.out.println("Beginning anisotropic diffusion.");

    if (time == 0)
      return this;

    Kernel channels[] = split();

    for(double t=0;t<time;t+=tau)
      {
	Kernel diffusivity = intensity(channels).diffusivity(sigma,lambda);
    
	for(int i=0;i<channels.length;i++)
	  channels[i] = channels[i].clmc(diffusivity,tau,i+"");
      }

    return join(channels);
  }
  public Kernel[] split()
  {
    Kernel[] channels = new Kernel[3];;

    channels[0] = new Kernel(width,height,0,0);
    channels[1] = new Kernel(width,height,0,0);
    channels[2] = new Kernel(width,height,0,0);
    
    for(int i=0;i<pixels.length;i++)
      {
	int c = pixels[i];
	
	channels[0].alpha[i] = (c & 0xFF0000) >> 16;
	channels[1].alpha[i] = (c & 0xFF00) >> 8;
	channels[2].alpha[i] = c & 0xFF;
      }

    return channels;
  }

  public static int clamp(double v)
  {
    if (v < 0)
      return 0;

    if (v > 0xFF)
      return 0xFF;
    
    return (int)v;
  }

  public static IntImage join(Kernel[] channels)
  {
    IntImage newImage = new IntImage(channels[0].width,channels[0].height);
    
    for(int i=0;i<channels[0].alpha.length;i++)
      {
	int r = clamp(channels[0].alpha[i]);
	int g = clamp(channels[1].alpha[i]);
	int b = clamp(channels[2].alpha[i]);
	
	newImage.pixels[i] = 0xFF000000 | (r << 16) | (g << 8) | b;
      }

    return newImage;
  }

  
  // separable Gaussian blur
  public IntImage gaussianBlur(double std)
  {
    //    if (std == 0)
    //      System.out.println("Warning: gblur called with std == 0");

    // create the kernels

    // Hack!  (to try to make the blurFac consistent with the previous version)
    std/=6;

    int kernelLen = (int)(std*8);

    if (kernelLen < 3)
      kernelLen = 3;

    int center = kernelLen/2;
    Kernel kX = new Kernel(kernelLen,1,center,0);
    Kernel kY = new Kernel(1,kernelLen,0,center);

    System.out.print("std = "+std+"; G(x)=[");

    double fac = 2*Math.PI*std*std;

    for(int x=0;x<kernelLen;x++)
      {
	double v = Math.exp(-(x-center)*(x-center)/fac);

	kX.setLoc(x,0,v);
	kY.setLoc(0,x,v);
	System.out.print(v+"");
	if (x-1<kernelLen)
	  System.out.print(',');
      }

    System.out.println("]");

    kX.normalize();
    kY.normalize();

    if (kernelLen == 3 && kX.getLoc(0,0) < .01)
      return this;

    IntImage bx = convolve(kX);
    return bx.convolve(kY);
  }

  public static IntImage readPPM(String filename) throws IOException
  {
    InputStream fp = new FileInputStream(filename);

    if (fp.read() != 'P')
      {
	System.out.println("Invalid PPM file type");
	return null;
      }

    int c = fp.read();

    boolean textFile;
    
    switch (c)
      {
      case '3': textFile = true; break;
      case '6': textFile = false; break;
      default: System.out.println("Invalid PPM file type P"+c); return null;
      }

    int width, height;

    // read in newline
    fp.read();
    
    StreamTokenizer st = new StreamTokenizer(fp);

    if (st.nextToken() != st.TT_NUMBER)
      return null;

    width = (int)st.nval;

    if (st.nextToken() != st.TT_NUMBER)
      return null;

    height = (int)st.nval;

    st.nextToken();
    //    fp.read();

    if (!textFile)
      return readRawPPM(fp,width,height);

    IntImage im = new IntImage(width,height);

    for(int y=0;y<height;y++)
      {
	for(int x=0;x<width;x++)
	  {
	    if (st.nextToken() != st.TT_NUMBER)
	      return null;

	    int red = ((int)st.nval)&0xFF;

	    if (st.nextToken() != st.TT_NUMBER)
	      return null;

	    int green = ((int)st.nval)&0xFF;

	    if (st.nextToken() != st.TT_NUMBER)
	      return null;

	    int blue = ((int)st.nval)&0xFF;

	    int color =0xFF000000 | (red << 16) | (green << 8) | blue;

	    im.setPixel(x,y,color);
	  }
      }

    return im;
 }
        
  public static IntImage readRawPPM(InputStream fp,int width,int height)
       throws IOException
  {
    IntImage im = new IntImage(width,height);

    int r,g,b;

    for(int y=0;y<height;y++)
      {
	for(int x=0;x<width;x++)
	  {
	    r = fp.read();	    
	    g = fp.read();	    
	    b = fp.read();

	    //System.out.print("("+data[0]+","+data[1]+","+data[2]+") = ");
		    
	    int color =0xFF000000 | (r << 16) | (g << 8) | b;

	    //	    	    System.out.println(Integer.toHexString(color));

	    im.setPixel(x,y,color);
	  }
      }

    return im;
  }
  
  public void writeAsciiPPM(OutputStream outf)
  {
    PrintStream outfile = new PrintStream(outf);

    outfile.println("P3");

    outfile.println(width+" "+height);
    outfile.println("255");

      for(int y=0;y<height;y++)
      {
    for(int x=0;x<width;x++)
	{
	  int color = getPixel(x,y);

	  int r = (color & 0xFF0000) >> 16;
	  int g = (color & 0xFF00) >> 8;
	  int b = (color & 0xFF);

	  outfile.print(r+" "+g+" "+b+" ");
	}
      
      outfile.print("\n");

      }
  }

  public void writeRawPPM(OutputStream outf) throws java.io.IOException
  {
    PrintStream outfile = new PrintStream(outf);

    outfile.println("P6");

    outfile.println(width+" "+height);
    outfile.println("255");

    byte data[] = new byte[3];

      for(int y=0;y<height;y++)
      {
    for(int x=0;x<width;x++)
	{
	  int color = getPixel(x,y);

	  data[0] = (byte)((color & 0xFF0000) >> 16);
	  data[1] = (byte)((color & 0xFF00) >> 8);
	  data[2] = (byte)((color & 0xFF));

	  outfile.write(data);
	}
      
    //      outfile.print("\n");

      }
  }
}


