import java.awt.*;
import java.applet.Applet;
import java.net.*;
import java.io.*;

public class PainterApplet extends Applet
{
  public void init()
  {
    IntImage sourceImage = null;

    try 
      { 
	System.out.println("Reading image file");

	String imgFile = getParameter("image");

	Image source = getImage(new URL(imgFile));

	sourceImage = new IntImage(source);
      }
    catch (MalformedURLException mue) 
      {
	System.out.println("MUE "+mue.getMessage());
      }
    catch (IOException ioe) 
      {
	System.out.println("IOE "+ioe.getMessage());
      }
    catch (InterruptedException e) 
      {
	System.err.println("interrupted waiting for pixels!"); return;
      }

    setLayout(new BorderLayout());
    
    add("Center",new Painter(sourceImage));
  }
}
