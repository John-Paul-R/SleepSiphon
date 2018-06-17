package gun;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import BetaF.Enemy;

public class VirtualBullet extends util.TimedPoint
{

	int sourceGun;
	
	public VirtualBullet(Point2D.Double l, long t, int s)
	{
		super(l.getX(), l.getY(), t);
		sourceGun = s; //Numeric ID for each gun ([0 : Head-On] [1 : Linear Prediction] [2 : Turn Predict] [3 : AvgVel&Heading])
	}
	
	public Point2D.Double getBulletLoc()
	{
		return new Point2D.Double(super.getX(), super.getY());
	}

	public long getTime()
	{
		return super.getTime();
	}
	
	public int getType()
	{
		return sourceGun;
	}
	
	public boolean checkForHit(Enemy target)
	{

		Rectangle2D.Double enemyArea = new Rectangle2D.Double(target.getX()-18, target.getY()-18, 36, 36);
		return enemyArea.contains(this.getBulletLoc());
	}
}
