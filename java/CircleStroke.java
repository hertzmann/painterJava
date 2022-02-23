class CircleStroke extends Stroke
{
  int x,y,color;
  Kernel mask;

  CircleStroke(int x,int y,int color,Kernel mask)
  {
    this.x=x;
    this.y=y;
    this.color = color;
    this.mask=mask;
  }

  public java.awt.Rectangle boundingBox(int maxWidth,int maxHeight)
  { return null; }
  public Kernel render(int maxWidth,int maxHeight) { return null; }
  public void renderToImage(IntImage image,Kernel edgeImage,int z)
  {
    image.newStroke(color);
    image.bpaint(x,y,z,mask);
  }
  public void flush() { }
}


