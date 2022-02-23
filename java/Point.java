final class Point
{
  final double x;
  final double y;

  Point(double x,double y)
  {
    this.x = x;
    this.y = y;
  }

  Point add(Point p2)
  {
    return new Point(x+p2.x,y+p2.y);
  }

  Point mult(double fac)
  {
    return new Point(x*fac,y*fac);
  }

  Point ave(Point p2)
  {
    return new Point((x+p2.x)/2,(y+p2.y)/2);
  }

  public String toString()
  {
    return "("+x+","+y+")";
  }
}
