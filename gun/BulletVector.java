package gun;

import java.awt.geom.Point2D;

import util.TimedPoint;

public class BulletVector extends Point2D
{
	Point2D startPos;
	private double angle; //in radians
	private double magnitude;
	private long time;
	private int gunID;

	public BulletVector(TimedPoint startPos, double a, double m, int gID)
	{
		this.startPos = new Point2D.Double(startPos.getX(), startPos.getY());
		angle = a;
		magnitude = m;
		time = startPos.getTime();
		gunID = gID;
	}

	public BulletVector(TimedPoint startPos, Point2D endPos, int gID)
	{
		this.startPos = startPos;
		angle = Math.atan2(endPos.getX()-startPos.getX(), endPos.getY()-startPos.getY());;
		magnitude = startPos.distance(endPos);
		time = startPos.getTime();
		gunID = gID;
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
	public Point2D.Double projectFromStartPos(double currentTime)
	{
		return new Point2D.Double(
				startPos.getX()+Math.sin(angle)*magnitude*(currentTime-time),
				startPos.getY()+Math.cos(angle)*magnitude*(currentTime-time));
	}
	public Point2D.Double projectByRad(double rad)
	{
		return new Point2D.Double(
				startPos.getX()+Math.sin(angle)*rad,
				startPos.getY()+Math.cos(angle)*rad);
	}
	public double getLengthAtTime(double currentTime)
	{
		return (magnitude*(currentTime-time));
	}
	public int getGunID()
	{
		return gunID;
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

	public long getTime() {
		// TODO Auto-generated method stub
		return time;
	}
	public long getAge(long currentTime) {
		// TODO Auto-generated method stub
		return currentTime-time;
	}
}
