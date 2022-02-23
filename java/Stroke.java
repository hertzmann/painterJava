abstract class Stroke 
{
  boolean painted = false; 
  int color;

  abstract public java.awt.Rectangle boundingBox(int maxWidth,int maxHeight);
  abstract public Kernel render(int maxWidth,int maxHeight);
  abstract public void renderToImage(IntImage image,Kernel edgeImage,int z);
  public void renderToImage(IntImage image,int z) { renderToImage(image,null,z); }
  abstract public void flush();
}
