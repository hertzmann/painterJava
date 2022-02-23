import java.awt.*;
import java.util.Vector;

class ImageCanvas extends Canvas implements Runnable
{
  IntImage img;
  Image i;

  ImageCanvas(IntImage img)
  {
    newImage(img);

    setSize(img.width,img.height);

    setBackground(new Color(0xFFFFF3DD));
  }

  public void paint(Graphics g)
  {
    if (i == null)
      {
	i = createImage(img.makeImage());
      }

    g.drawImage(i,0,0,null);
  }

  void newImage(IntImage img)
  {
    this.img = img;
    i = null;
  }

  void imageChanged()
  {
    i = null;
  }

  Thread t;

  public void update()
  {
    paint(getGraphics());
  }

  public void run()
  {
    try 
      {
	for ( ; true ; t.sleep(50)) 
	  {
	    paint(getGraphics());
	  }
      }
    catch(InterruptedException e){ System.err.println(e); };
  }
}


