import java.awt.*; 
import java.awt.image.*; 
import java.io.*; 
import java.applet.*; 
import java.net.*; 
import java.util.*;
import java.awt.event.*;

public final class DiffuseTest extends Canvas implements KeyListener
{
  IntImage sourceImage = null;
  IntImage currentImage = null;
  Kernel[] channels = null;

  Image i = null;

  static final double sigma = 3;
  static final double tau = 3;
  static final double lambda = 0.75;

  public static void main(String[] args)
  {
    if (args.length < 1)
      {
	System.out.println("usage: java DiffuseTest <image-name>");
	return;
      }

    Frame f = new Frame("Diffusion:"+args[0]);
    Toolkit t = Toolkit.getDefaultToolkit();

    IntImage sourceImage = null;

    try 
      { 
	System.out.println("Reading image file "+args[0]);

	Image source = t.getImage(args[0]);

	sourceImage = new IntImage(source);
      }
    catch (IOException ioe) 
      {
	System.out.println("IOE "+ioe.getMessage());
	return;
      }
    catch (InterruptedException e) 
      {
	System.err.println("interrupted waiting for pixels!"); 
	return;
      }

    f.setLayout(new BorderLayout());
    
    f.add("Center",new DiffuseTest(sourceImage));
    f.pack();
    f.show();

    System.out.println("Sigma = "+sigma+", lambda = "+lambda+", tau = "+
		       tau);
  }

  DiffuseTest(IntImage sourceImage)
  {
    this.sourceImage = sourceImage;
    this.currentImage = (IntImage)(sourceImage.clone());

    channels = currentImage.split();
    
    setSize(currentImage.width,currentImage.height);

    setBackground(new Color(0xFFFFF3DD));

    addKeyListener(this);
  }

  public void paint(Graphics g)
  {
    if (i == null)
      {
	i = createImage(currentImage.makeImage());
      }

    g.drawImage(i,0,0,null);
  }

  static double t = 0;

  void oneStep()
  {
    t += tau;

    System.out.println("Processing.  t="+t);

    Kernel diffusivity = currentImage.intensity().diffusivity(sigma,lambda);
    
    for(int i=0;i<channels.length;i++)
      channels[i] = channels[i].clmc(diffusivity,tau,i+"");

    currentImage = IntImage.join(channels);

    i = null;

    System.out.println("Done.");
  }

  public void keyPressed(KeyEvent ke)
  {
    char c = ke.getKeyChar();

    if (c == ' ' || c == '\r')
      oneStep();

    repaint();
  }

  public void keyTyped(KeyEvent ke)
  {
  }

  public void keyReleased(KeyEvent ke)
  {
  }
}




