import java.awt.*;
import java.awt.event.*;

public class ColorTest extends Canvas implements KeyListener
{
  float h = 1;
  float s = 1;
  float v = 1;

  ColorTest()
  {
    setSize(100,200);
    addKeyListener(this);
  }

  public void paint(Graphics g)
  {
        g.setColor(new Color(Color.HSBtoRGB(h,s,v)));
        g.fillRect(0,0,200,200);
	/*
        for(int x=0;x<100;x++)
      for(int y=0;y<100;y++)
	{
	  int color = Color.HSBtoRGB(x/100.0f,1,y/100.0f);
	  Color col = new Color(color);
	  g.setColor(col);
	  g.drawLine(x,y,x,y);
	}
    for(int x=0;x<100;x++)
      for(int y=0;y<100;y++)
	{
	  int color = Color.HSBtoRGB(x/100.0f,1-y/100.0f,1);
	  Color col = new Color(color);
	  g.setColor(col);
	  g.drawLine(x,y+100,x,y+100);
	}*/
  }

  public static void main(String[] args)
  {
    Frame f = new Frame();
    f.setLayout(new BorderLayout());
    f.add("Center",new ColorTest());
    f.pack();
    f.show();
  }

  static float stepSize = .05f;
  
  public void keyPressed(KeyEvent ke)
  {
    char c = ke.getKeyChar();

    switch(c)
      {
      case 'q': h -= stepSize; break;
      case 'w': h += stepSize; break;
      case 'a': s -= stepSize; break;
      case 's': s += stepSize; break;
      case 'z': v -= stepSize; break;
      case 'x': v += stepSize; break;
      default:;
      }

    System.out.println("h = "+h+", s = "+s+",v = "+v);

    repaint();
  }

  public void keyTyped(KeyEvent ke)
  {
  }

  public void keyReleased(KeyEvent ke)
  {
  }

}  
