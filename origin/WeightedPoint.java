package origin;

import java.awt.geom.Point2D;

public class WeightedPoint extends Point2D
{
	public Point2D.Double point;
	private double weight;
	
	public WeightedPoint(double x, double y, double w)
	{
		point = new Point2D.Double(x, y);
		weight = w;
	}
	public WeightedPoint(Point2D.Double p, double w)
	{
		point = p;
		weight = w;
	}
	public WeightedPoint(Point2D.Double p)
	{
		point = p;
		weight = 0;
	}
	public WeightedPoint(double x, double y)
	{
		point = new Point2D.Double(x, y);
	}
	
	
	
	@Override
	public double getX()
	{
		return point.getX();
	}
	@Override
	public double getY()
	{
		return point.getY();
	}
	
	public double getWeight()
	{
		return weight;
	}
	public void setWeight(double w)
	{
		weight = w;
	}
	@Override
	public void setLocation(double x, double y) 
	{
		point.setLocation(x, y);
	}
}
