import java.awt.*;
import java.io.*;

public class Batch
{
  public static void main(String[] args) throws IOException
  {
    if (args.length == 0)
      {
	runFile(System.in);
      }
    else
      if (args[0].equals("-f"))
      {
	System.out.println("Reading script file "+args[1]);
    
	FileInputStream fis = new FileInputStream(args[1]);

	runFile(fis);
      }
    else
      for(int i=0;i<args.length;i++)
	run(args[i]);
  }

  static Painter painter;
  
  static void runFile(InputStream ir) throws IOException
  {
    StreamTokenizer st = new StreamTokenizer(ir);

    st.wordChars('/','/');
    st.wordChars('-','-');
    st.wordChars('.','.');
    st.wordChars('_','_');

    do 
      {
	if (st.nextToken() == st.TT_WORD)
	  {
	    run(st.sval);
	  }
      }
    while(st.ttype != st.TT_EOF);
  }

  static void run(String filename)
  {
    Toolkit t = Toolkit.getDefaultToolkit();

    IntImage sourceImage = null;

    try 
      { 
	System.out.println("Reading image file "+filename);

	if (filename.endsWith("ppm") || filename.endsWith("PPM"))
	  sourceImage = IntImage.readPPM(filename);
	else
	  {
	    Image source = t.getImage(filename);

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

    if (painter == null)
      painter = new Painter(sourceImage);
    else
      painter.setImage(sourceImage);

    String basename = filename.substring(0,filename.length()-4);

    impressionist(painter,"");
    painter.makeStrokes();
    writeFile(painter,basename+"-imp.ppm");
    System.gc();

    expressionist(painter,"");
    painter.makeStrokes();
    writeFile(painter,basename+"-exp.ppm");
    System.gc();

    colorist(painter,"");
    painter.makeStrokes();
    writeFile(painter,basename+"-col.ppm");
    System.gc();

    pointillist(painter,"");
    painter.makeStrokes();
    writeFile(painter,basename+"-poi.ppm");
    System.gc();
/*
    colpoi(painter,"");
    painter.makeStrokes();
    writeFile(painter,basename+"-colpoi.ppm");
    System.gc();*/
  }

  static void writeFile(Painter painter,String newName)
  {
    try
      {
	//	painter.outputImage.writeAsciiPPM(new FileOutputStream(newName));
	painter.outputImage.writeRawPPM(new FileOutputStream(newName));

	System.out.println("Wrote "+newName);
      }
    catch (IOException ioe) 
      {
	System.out.println("IOE "+ioe.getMessage());
      }
  }

  static void impressionist(Painter painter,String filename)
  {
    painter.threshold = 100;
    painter.minBrushSize = 2;
    painter.numLayers = 3;
    painter.opacity = 1;
    painter.gridFac = 1;
    painter.blurFac = .5;
    painter.minLength = 4;
    painter.maxLength = 16;

    painter.hfac = 1;
    painter.hjit = 0;

    painter.sfac = 1;
    painter.sjit = 0;

    painter.bfac = 1;
    painter.bjit = 0;

    painter.RGBjit = 0;

    painter.filterFac = 1;

    /*
    painter.makeStrokes();

    new ImageFrame(painter.outputImage,"Impressionist "+filename);
    */
  }

  static void expressionist(Painter painter,String filename)
  {
    painter.threshold = 50;
    painter.minBrushSize = 2;
    painter.numLayers = 3;
    painter.opacity = .7;
    painter.gridFac = 1;
    painter.blurFac = .5;
    painter.minLength = 10;
    painter.maxLength = 16;

    painter.hfac = 1;
    painter.hjit = 0;

    painter.sfac = 1;
    painter.sjit = .5;

    painter.bfac = 1;
    painter.bjit = 0;

    painter.RGBjit = 0;

    painter.filterFac = .25;

    /*
    painter.makeStrokes();

    new ImageFrame(painter.outputImage,"Expressionist "+filename);
    */
  }

  static void colorist(Painter painter,String filename)
  {
    painter.threshold = 200;
    painter.minBrushSize = 2;
    painter.numLayers = 3;
    painter.opacity = .5;
    painter.gridFac = 1;
    painter.blurFac = .5;
    painter.minLength = 4;
    painter.maxLength = 16;

    painter.hfac = 1;
    painter.hjit = 0;

    painter.sfac = 1;
    painter.sjit = 0;

    painter.bfac = 1;
    painter.bjit = 0;

    painter.RGBjit = .3;

    painter.filterFac = 1;

    /*
    painter.makeStrokes();

    new ImageFrame(painter.outputImage,"Colorist "+filename);
    */
  }

  static void pointillist(Painter painter,String filename)
  {
    painter.threshold = 100;
    painter.minBrushSize = 2;
    painter.numLayers = 2;
    painter.opacity = 1;
    painter.gridFac = .5;
    painter.blurFac = .5;
    painter.minLength = 0;
    painter.maxLength = 0;

    painter.hfac = 1;
    painter.hjit = .3;

    painter.sfac = 1;
    painter.sjit = 1;

    painter.bfac = 1;
    painter.bjit = 0;

    painter.RGBjit = 0;

    painter.filterFac = 1;

    /*
    painter.makeStrokes();

    new ImageFrame(painter.outputImage,"Pointillist "+filename);
    */
  }

  static void colpoi(Painter painter,String filename)
  {
    painter.threshold = 150;
    painter.minBrushSize = 2;
    painter.numLayers = 3;
    painter.opacity = 1;
    painter.gridFac = .75;
    painter.blurFac = .5;
    painter.minLength = 2;
    painter.maxLength = 8;

    painter.hfac = 1;
    painter.hjit = .15;

    painter.sfac = 1;
    painter.sjit = 0.5;

    painter.bfac = 1;
    painter.bjit = 0;

    painter.RGBjit = .15;

    painter.filterFac = 1;

    /*
    painter.makeStrokes();

    new ImageFrame(painter.outputImage,"colpoi "+filename);
    */
  }
}
