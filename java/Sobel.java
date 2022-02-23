class Sobel extends Kernel
{
  Sobel(boolean xfilter)
  {
    width = 3;
    height = 3;
    originX = 1;
    originY = 1;

    if (xfilter)
      alpha = new double[] { -1, 0, 1,
			     -2, 0, 2, 
			     -1, 0, 1};
    else
      alpha = new double[] { -1, -2, -1,
			      0, 0, 0,
			      1, 2, 1};
  }

  static final Kernel SOBEL_X = new Sobel(true);
  static final Kernel SOBEL_Y = new Sobel(false);
   
}
