package util;



public class Vector
{
	//ALL ANGLES IN RADIANS
	private double magnitude;
	private double direction;
	
	public Vector(double m, double d)
	{
		magnitude = m;
		direction = d;
	}
	
	public static Vector add(Vector v1, Vector v2)
	{
		double sumVert = v1.getVerticalComponent() + v2.getVerticalComponent();
		double sumHoriz = v1.getHorizontalComponent() + v2.getVerticalComponent();
		double newMag = Math.hypot(sumVert, sumHoriz);
		double newDir = Math.atan2(sumVert, sumHoriz);
		return new Vector(newMag, newDir);
	}
	public void add(Vector v2)
	{
		double sumVert = this.getVerticalComponent() + v2.getVerticalComponent();
		double sumHoriz = this.getHorizontalComponent() + v2.getVerticalComponent();
		magnitude = Math.hypot(sumVert, sumHoriz);
		direction = Math.atan2(sumVert, sumHoriz);
	}
	public double getMagnitude() {return magnitude;}
	public double getDirection() {return direction;}
	public double getVerticalComponent() {return magnitude * Math.sin(direction);}
	public double getHorizontalComponent() {return magnitude * Math.cos(direction);}

	public void setMagnitude(double m)
	{
		magnitude = m;
	}
	
}
