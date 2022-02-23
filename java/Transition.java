import java.awt.*;
import java.io.*;

public class Transition
{
  public static void main(String[] args)
  {
    for(int i=0;i<args.length;i++)
      run(args[i]);
  }

  static Painter ai;

  static void run(String filename)
  {
    Toolkit tk = Toolkit.getDefaultToolkit();

    IntImage sourceImage = null;

    try 
      { 
	System.out.println("Reading image file "+filename);

	if (filename.endsWith("ppm") || filename.endsWith("PPM"))
	  sourceImage = IntImage.readPPM(filename);
	else
	  {
	    Image source = tk.getImage(filename);

	    sourceImage = new IntImage(source);
	  }
      }
    catch (IOException ioe) 
      {
	System.out.println("IOE "+ioe.getMessage());
      }
    catch (InterruptedException e) 
      {
	System.err.println("interrupted waiting for pixels!"); return;
      }

    if (ai == null)
      ai = new Painter(sourceImage);
    else
      ai.setImage(sourceImage);

    String basename = filename.substring(0,filename.length()-4);

    for(int t=0;t<25;t++)
      {
	impexp(ai,t/25.0);

	ai.makeStrokes();

	writeFile(ai,basename+"-"+t+".ppm");
	
	System.gc();
      }

    for(int t=25;t<50;t++)
      {
	expcol(ai,(t-25)/25.0);

	ai.makeStrokes();

	writeFile(ai,basename+"-"+t+".ppm");
	
	System.gc();
      }

    for(int t=50;t<75;t++)
      {
	colpoi(ai,(t-50)/25.0);

	ai.makeStrokes();

	writeFile(ai,basename+"-"+t+".ppm");
	
	System.gc();
      }

    for(int t=75;t<100;t++)
      {
	poiimp(ai,(t-75)/25.0);

	ai.makeStrokes();

	writeFile(ai,basename+"-"+t+".ppm");
	
	System.gc();
      }

  }

  static void writeFile(Painter ai,String newName)
  {
    try
      {
	//	ai.outputImage.writeAsciiPPM(new FileOutputStream(newName));
	ai.outputImage.writeRawPPM(new FileOutputStream(newName));

	System.out.println("Wrote "+newName);
      }
    catch (IOException ioe) 
      {
	System.out.println("IOE "+ioe.getMessage());
      }
  }

  static int round(double d)
  {
    return (int)Math.floor(d+.5);
  }

  static void impexp(Painter ai,double t)
  {
    ai.threshold = round(100*(1-t)+50*t);
    ai.minBrushSize = 2;
    ai.numLayers = 3;
    ai.opacity = 1*(1-t)+.7*t;
    ai.gridFac = 1;
    ai.blurFac = .5;
    ai.minLength = round(4*(1-t)+10*t);
    ai.maxLength = 16;

    ai.hfac = 1;
    ai.hjit = 0;

    ai.sfac = 1;
    ai.sjit = .5*t;

    ai.bfac = 1;
    ai.bjit = 0;

    ai.RGBjit = 0;

    ai.filterFac = 1*(1-t)+.25*t;

  }

  static void expcol(Painter ai,double t)
  {
    ai.threshold = round(50*(1-t)+200*t);
    ai.minBrushSize = 2;
    ai.numLayers = 3;
    ai.opacity = .7*(1-t)+.5*t;
    ai.gridFac = 1;
    ai.blurFac = .5;
    ai.minLength = round(10*(1-t)+4*t);
    ai.maxLength = 16;

    ai.hfac = 1;
    ai.hjit = 0;

    ai.sfac = 1;
    ai.sjit = .5*(1-t);

    ai.bfac = 1;
    ai.bjit = 0;

    ai.RGBjit = 0.3*t;

    ai.filterFac = .25*(1-t)+t;

  }

  static void colpoi(Painter ai,double t)
  {
    ai.threshold = round(200*(1-t)+100*t);
    ai.minBrushSize = 2;
    ai.numLayers = round(3*(1-t)+2*t);
    ai.opacity = .5*(1-t)+t;
    ai.gridFac = 1-t+.5*t;
    ai.blurFac = .5;
    ai.minLength = round(4*(1-t));
    ai.maxLength = round(16*(1-t));

    ai.hfac = 1;
    ai.hjit = 0.3*t;

    ai.sfac = 1;
    ai.sjit = t;

    ai.bfac = 1;
    ai.bjit = 0;

    ai.RGBjit = .3*(1-t);

    ai.filterFac = 1;
  }


  static void poiimp(Painter ai,double t)
  {
    ai.threshold = 100;
    ai.minBrushSize = 2;
    ai.numLayers = round(2*(1-t)+3*t);
    ai.opacity = 1;
    ai.gridFac = .5*(1-t)+t;
    ai.blurFac = .5;
    ai.minLength = round(4*t);
    ai.maxLength = round(16*t);

    ai.hfac = 1;
    ai.hjit = .3*(1-t);

    ai.sfac = 1;
    ai.sjit = 1*(1-t);

    ai.bfac = 1;
    ai.bjit = 0;

    ai.RGBjit = 0;

    ai.filterFac = 1;
  }
}
