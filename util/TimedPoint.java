package util;

import java.awt.geom.Point2D;

public class TimedPoint extends Point2D
{
	double X;
	double Y;
	long t;
	
	public TimedPoint(double x, double y, long time)
	{
		this.X = x;
		this.Y = y;
		this.t = time;
	}
	
	@Override
	public double getX() {return X;}

	@Override
	public double getY() {return Y;}
	
	public Point2D.Double getPoint() {return new Point2D.Double(X, Y);}
	
	public long getTime() {return t;}

	@Override
	public void setLocation(double x, double y)
	{
		X = x;
		Y = y;
	}
}
