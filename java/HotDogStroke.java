class HotDogStroke extends Stroke
{
  int x;int y;
  double dx,dy;
  Kernel mask;
  int color;
int length;

  HotDogStroke(int x,int y,double dx,double dy,int length,int color,Kernel mask)
  {
    this.x=x;
    this.y=y;
    this.dx=dx;
    this.dy=dy;
    this.mask=mask;
    this.length=length;
    this.color=color;

    if (dx != 0 || dy != 0)
      {
	double ms = Math.sqrt(dx*dx+dy*dy);
	this.dx*=length;
	this.dx/=ms;
	this.dy*=length;
	this.dy/=ms;
      }
  }

  public java.awt.Rectangle boundingBox(int maxWidth,int maxHeight)
  { return null; }

  public Kernel render(int maxWidth,int maxHeight) { return null; }

  public void renderToImage(IntImage image,Kernel edgeImage,int z)
  {
    image.newStroke(color);

    image.bpaintline((int)(x-dx),(int)(y-dy),
		     (int)(x+dx),(int)(y+dy),z,mask);
  }

  public void flush() { }
}
