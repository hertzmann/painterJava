// Ken's noise function

import java.util.Random;

class Noise
{
  static private final int B = 0x100;
  static private final int BM = 0xff;

  static private final int N = 0x1000;
  static private final int NP = 12;
  static private final int NM = 0xfff;

  static private int p[] = new int[B + B +2];
  static private double g3[][] = new double[B + B + 2][3];
  static private double g2[][] = new double[B + B + 2][2];
  static private double g1[] = new double[B + B +2];

  static private double s_curve(double t) { return(t*t*(3-2*t)); }

  static private double noise(double arg) 
  {
    int bx0, bx1;
    double rx0, rx1, sx, t, u, v, vec;
    vec=arg;
    t = vec + N;
    bx0 = ((int)t)&BM;
    bx1 = (bx0+1)&BM;
    rx0 = t - (int) t;
    rx1 = rx0 -1;

    sx = s_curve(rx0);
    u = rx0 * g1[p[bx0]];
    v = rx1 * g1[p[bx1]];

    //    System.out.println("N("+arg+") = "+ lerp(sx,u,v));
    //    System.out.println("  sx = "+ sx + "  u +"+u +"   v = "+v);
    //    System.out.println("bx0 = "+bx0+"   g1 = "+g1[p[bx0]]);


    return (lerp(sx, u, v));
  }

  static private double noise2(double[] vec)
  {
        int bx0, bx1, by0, by1, b00, b10, b01, b11;
        double rx0, rx1, ry0, ry1, sx, sy, a, b, t, u, v;
        int i, j;

	t = vec[0] + N;
	bx0 = ((int)t) & BM;
	bx1 = (bx0+1) & BM;
	rx0 = t - (int)t;
	rx1 = rx0 - 1;

	t = vec[1] + N;
	by0 = ((int)t) & BM;
	by1 = (by0+1) & BM;
	ry0 = t - (int)t;
	ry1 = ry0 - 1;

        i = p[ bx0 ];
        j = p[ bx1 ];

        b00 = p[ i + by0 ];
        b10 = p[ j + by0 ];
        b01 = p[ i + by1 ];
        b11 = p[ j + by1 ];

        sx = s_curve(rx0);
        sy = s_curve(ry0);

	u = rx0 * g2[b00][0] + ry0 * g2[b00][1];
	v = rx1 * g2[b10][0] + ry0 * g2[b10][1];
        a = lerp(sx, u, v);

	u = rx0 * g2[b01][0] + ry1 * g2[b01][1];
	v = rx1 * g2[b11][0] + ry1 * g2[b11][1];
        b = lerp(sx, u, v);

        return lerp(sy, a, b);

  }

  static double at2(double rx,double ry,double q[])
  {
    return rx * q[0] + ry * q[1];
  }

  static 
  {
    System.out.println("Initializing noise");

    int i, j, k;
    double t;
    Random r = new Random();
    for(i=0; i<B ; i++) {
      p[i] = i;
      t = (double)(r.nextLong()&BM)/B;
      g1[i] = 2*t-1.;

      for ( j=0; j<2 ; j++)
	g2[i][j] = (double)((r.nextLong() % (B + B)) -B)/B;
      normalize2(g2[i]);

      for ( j=0; j<3 ; j++)
	g3[i][j] = (double)((r.nextLong() % (B + B)) -B)/B;
      normalize3(g3[i]);

    }

    while(--i>0) 
      {
	k = p[i];
	j = (int)(r.nextLong()&BM);
	p[i] = p[j];
	p[j] = k;
      }

    for( i = 0; i <B+2; i++) 
      {
	p[B+i] = p[i];
	g1[B+i] = g1[i];
	for (j = 0 ; j < 2 ; j++) { g2[B + i][j] = g2[i][j]; }
	for (j = 0 ; j < 3 ; j++) { g3[B + i][j] = g3[i][j]; }
      }
  }

  static private void normalize3(double v[]) 
  {
    double s;
    s = Math.sqrt(v[0] * v[0] + v[1] * v[1]+ v[2] * v[2]);
    v[0] = v[0] / s;
    v[1] = v[1] / s;
    v[2] = v[2] / s;
  }

  static private void normalize2(double v[]) 
  {
    double s;
    s = Math.sqrt(v[0] * v[0] + v[1] * v[1]);
    v[0] = v[0] / s;
    v[1] = v[1] / s;
  }

  static double lerp(double t,double a, double b)
  { 
    return a + t*(b-a);
  }

  static Kernel noiseKernel(int width, int height, double minVal, 
			    double maxVal, double range)
  {
    Kernel k = new Kernel(width,height,0,0);

    double[] coords = new double[2];

    for(int x = 0;x < width;x ++)
      {
	coords[0] = range*x / width;

	for(int y = 0;y < height;y ++)
	  {
	    coords[1] = range*y/height;

	    double val = noise2(coords);  // value in range (-1 .. 1)

	    //	    double val = noise(y);

	    k.setLoc(x,y,(val + 1)*(maxVal-minVal)/2 + minVal);
	  }
      }
    return k;
  }
}

