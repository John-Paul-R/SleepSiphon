package util;

import java.awt.geom.Point2D;

public class Vector2D extends Point2D
{
	Point2D startPos;
	private double angle; //in radians
	private double magnitude;
	
	public Vector2D(Point2D startPos, double a, double m)
	{
		this.startPos = new Point2D.Double(startPos.getX(), startPos.getY());
		angle = a;
		magnitude = m;
	}
	
	public Vector2D(Point2D startPos, Point2D endPos)
	{
		this.startPos = startPos;
		angle = Math.atan2(endPos.getX()-startPos.getX(), endPos.getY()-startPos.getY());;
		magnitude = startPos.distance(endPos);
		
	}
	public Vector2D(double a, double m)
	{
		angle = a;
		magnitude = m;
	}
	
	//TODO implement this
/*	public static Vector2D vectorSum(Vector2D[] addends)
	{
		int numTerms = addends.length;
		for (int i = 0; i < numTerms; i++)
		{
			
		}
	}*/
	
	public Point2D getStartPos()
	{
		return new Point2D.Double(startPos.getX(), startPos.getY());
	}
	
	public double getAngle()
	{
		return angle;
	}
	
	public double getMagnitude()
	{
		return magnitude;
	}
	public void setMagnitude(double m)
	{
		magnitude = m;
	}
	public void scaleMagnitude(double scaleFactor)
	{
		magnitude *= scaleFactor;
	}
	
	public Point2D.Double project()
	{
		return new Point2D.Double(
				Math.sin(angle)*magnitude,
				Math.cos(angle)*magnitude);
	}
	public Point2D.Double projectFromStartPos(double scaleFactor)
	{
		return new Point2D.Double(
				startPos.getX()+Math.sin(angle)*magnitude*scaleFactor,
				startPos.getY()+Math.cos(angle)*magnitude*scaleFactor);
	}
	
	
	@Override
	public double getX() 
	{
		return startPos.getX();
	}

	@Override
	public double getY()
	{
		return startPos.getY();
	}

	@Override
	public void setLocation(double arg0, double arg1) 
	{
		startPos = new Point2D.Double(arg0, arg1);
	}

}
