package util;

import java.awt.geom.Point2D;

public class TimedWeightedPoint extends TimedPoint
{
	private double weight;

	public TimedWeightedPoint(double x, double y, double w, long t)
	{
		super(x, y, t);
		weight = w;
	}
	public TimedWeightedPoint(Point2D.Double point, double w, long t)
	{
		super(point.getX(), point.getY(), t);
		weight = w;
	}
	public TimedWeightedPoint(TimedPoint tPoint, double w)
	{
		super(tPoint.getX(), tPoint.getY(), tPoint.getTime());
		weight = w;
	}
	
	public double getWeight()
	{
		return weight;
	}
	public void setWeight(double w)
	{
		weight = w;
	}
	public void addWeight(double w)
	{
		weight += w;
	}
}
