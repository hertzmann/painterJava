import java.awt.*;
import java.awt.image.*;


public class ImageFrame extends Frame
{
  Image img;

  public ImageFrame(IntImage img,String name)
  {
    this(img.makeImage(),img.width,img.height,name);
  }

  public ImageFrame(IntImage img,String name,int width,int height)
  {
    this(img.makeImage(),width,height,name);
  }

  public ImageFrame(ImageProducer imgp,int width, int height,String name)
  {
    super(name);

    img = createImage(imgp);

    //    int height = img.getHeight(null);  // strange stuff
    //    height = img.getHeight(null);
    //    int width = img.getWidth(null);

    setSize(width,height);

        System.out.println("Making image: width = "+width+", height = "+height);

    show();
  }

  public void paint(Graphics g)
  {
    g.drawImage(img,0,0,null);
  }
}
