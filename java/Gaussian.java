public final class Gaussian extends Kernel
{
  public Gaussian(int size,double std)
  {
    width = size;
    height = size;
    originX = size/2;
    originY = size/2;

    alpha = new double[width * height];

    for(int x=0;x<width;x++)
      for(int y=0;y<height;y++)
	{
	  int dx = x - originX;
	  int dy = y - originY;
	  double dc = (dx*dx+dy*dy) / (2.0*std*std);

	  alpha[x+y*width] = Math.exp(-dc);
	}

    normalize();
  }
}

/*
Re: making an arbitrary-sized Gaussian filter, suppose you want a filter
of size s1 rows, s2 columns, with standard deviation std.  Then you could
use (in matlab notation)

[x,y] = meshgrid(-(s2-1)/2:(s2-1)/2,-(s1-1)/2:(s1-1)/2);
h = exp(-(x.*x + y.*y)/(2*std*std));
h = h/sum(sum(h));

meshgrid is just a matlab construct for making 2-d arrays, so that
[x,y] = meshgrid([-1 0 1],[-1 0 1]) produces

    [ -1 0 1]      [ -1 -1 -1 ]
x = [ -1 0 1], y = [  0  0  0 ]
    [ -1 0 1]      [  1  1  1 ]
*/
