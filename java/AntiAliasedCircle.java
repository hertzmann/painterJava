import java.lang.Math;

class AntiAliasedCircle extends Kernel
{
  AntiAliasedCircle(int radius,double falloff)
  {
    //    double f = radius * (1-falloff);

    if (falloff >= radius)
      falloff = radius - 1;

    double f = radius - falloff;

    width = 2*radius;
    height = 2*radius;
    originX = radius;
    originY = radius;

    double cx = (width + 1)/2.0;
    double cy = (height + 1)/2.0;

    alpha = new double[width * height];

    for(int x=0;x<width;x++)
      for(int y=0;y<height;y++)
	{
	  double dx = x - cx;
	  double dy = y - cy;

	  double d = dx*dx + dy*dy;

	  if (d > radius *radius)
	    alpha[x+y*width] = 0;
	  else
	    if (d > f*f)
	      {
		double t = (radius - Math.sqrt(d))/falloff;
		
		alpha[x+y*width] = t * t * (3 - 2*t);
	      }
	    else
	      alpha[x+y*width] = 1;
	}
  }
}
