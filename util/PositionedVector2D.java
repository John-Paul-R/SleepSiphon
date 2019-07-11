package util;

import java.awt.geom.Point2D;

public class PositionedVector2D extends Point2D
{
    private double x;
    private double y;
    private Vector2D vector;

    public PositionedVector2D()
	{
        this.x = 0;
        this.y = 0;
        this.vector = new Vector2D(0, 0);
    }
    
	public PositionedVector2D(Point2D location, double angle, double magnitude)
	{
        this.x = location.getX();
        this.y = location.getY();
		this.vector = new Vector2D(angle, magnitude);
    }
    
    public PositionedVector2D(double x, double y, double angle, double magnitude)
	{
        this.x = x;
        this.y = y;
		this.vector = new Vector2D(angle, magnitude);
	}

	public PositionedVector2D(Point2D location, Point2D endPos)
	{
        this.x = location.getX();
        this.y = location.getY();
        this.vector = new Vector2D(
            new Point2D.Double(
                endPos.getX()-location.getX(),
                endPos.getY()-location.getY()));
	}
	public PositionedVector2D(double angle, double magnitude)
	{
        this.x = 0;
        this.y = 0;
		this.vector = new Vector2D(angle, magnitude);
	}

	//TODO implement this
	/*public static Vector2D sum(Vector2D[] addends)
	{
        int numTerms = addends.length;
        Vector2D out = new Vector2D();
        double x1 = 0;
        double y1 = 0;
        double x2 = 0;
        double y2 = 0;
		for (int i = 0; i < numTerms; i++)
		{
            x1 += addends[i].getX();
            y1 += addends[i].getY();
            x2
		}
    }*/

	public double getAngle()
	{
		return vector.getAngle();
	}
    public void setMagnitude(double magnitude)
	{
		vector.setMagnitude(magnitude);
	}
	public double getMagnitude()
	{
		return vector.getMagnitude();
	}
	
	public void scaleMagnitude(double scaleFactor)
	{
		vector.setMagnitude(vector.getMagnitude()*scaleFactor);
	}

	public Point2D.Double project()
	{
		return new Point2D.Double(
				Math.sin(vector.getAngle())*vector.getMagnitude(),
				Math.cos(vector.getAngle())*vector.getMagnitude());
	}
	/*public Point2D.Double projectScaled(double scaleFactor)
	{
		return new Point2D.Double(
				location.getX()+Math.sin(angle)*magnitude*scaleFactor,
				location.getY()+Math.cos(angle)*magnitude*scaleFactor);
	}*/
    
	@Override
	public double getX()
	{
		return x;
    }
	@Override
	public double getY()
	{
		return y;
    }
	@Override
	public void setLocation(double x, double y)
	{
        this.x = x;
        this.y = y;
	}

}
