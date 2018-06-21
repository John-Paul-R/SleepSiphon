package gun;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;

import origin.BotData;
import origin.Enemy;
import origin.Util;
import robocode.Rules;
import util.TimedPoint;

public class enemyGunSim 
{
	public static double headOn(Point2D source, BotData target, double BULLET_POWER)
    {
		return Math.atan2(target.getX()-source.getX(), target.getY()-source.getY());
    } 
    
    public static double linearPredictionFire(Point2D source, BotData target, double BULLET_POWER)
    {
    	double time = -1;
    	double endX = 0;
    	double endY = 0;
    	final double eX = target.getX()-source.getX();//enemy X relative to self
    	final double eY = target.getY()-source.getY();//enemy Y relative to self

    	final double eVelocityX = target.getVelocity()*Math.sin(target.getHeading());
    	final double eVelocityY = target.getVelocity()*Math.cos(target.getHeading());
    	final double bulletVelocity = Rules.getBulletSpeed(BULLET_POWER);
    	
    	final double c = eX*eX + eY*eY;
    	final double a2 = bulletVelocity*bulletVelocity;
    	final double a = eVelocityX*eVelocityX + eVelocityY*eVelocityY - a2;
    	final double b = 2*(eX*eVelocityX + eY*eVelocityY);

    	final double discrim = b*b - 4*a*c;
    	if (discrim >= 0)
    	{
    		final double t1 = (-b + Math.sqrt(b*b - 4*a*c))/(2*a);
    		final double t2 = (-b - Math.sqrt(b*b - 4*a*c))/(2*a);
    		time = (Math.min(t1, t2) >= 0 ? Math.min(t1, t2) : Math.max(t1, t2)); 
    		
    		final double[] bounds = Util.getFieldBoundsxXyY();
    		endX = Util.limitValueBounds(eX+source.getX()+eVelocityX*time, bounds[0], bounds[1]);
    		endY = Util.limitValueBounds(eY+source.getY()+eVelocityY*time, bounds[2], bounds[3]);
 
    	}
    	
    	return Math.atan2(endX-source.getX(), endY-source.getY());
    }

    public static double constantTurnPredict(Point2D source, BotData target, double BULLET_POWER)
    {
    	double turnRate = target.getLatestTurnRate();
    	double relX = target.getX() - source.getX();
    	double relY = target.getY() - source.getY();
    	double cHeading = target.getHeading();
    	double velocity = target.getVelocity();
    	
    	double selfX = source.getX();
    	double selfY = source.getY();
    	
    	double deltaTime = 0;
    	final double bulletVelocity = Rules.getBulletSpeed(BULLET_POWER);
    	
    	while ((bulletVelocity * deltaTime) < Math.abs(Point2D.distance(0, 0, relX, relY)))
    	{
    		relX += (velocity * Math.sin(cHeading + turnRate));
    		relY += (velocity * Math.cos(cHeading + turnRate));
    		cHeading += turnRate;
    		deltaTime += 1;
    	}
		
    	Point2D.Double targetCoord = Util.limitCoordinateToMap(new Point2D.Double(relX + selfX, relY +selfY));
    	return Math.atan2(targetCoord.getX()-source.getX(), targetCoord.getY()-source.getY());
    }
	public static boolean checkForHit(BulletVector bulletVector, Enemy target, long currentTime)
	{

		Rectangle2D.Double enemyArea = new Rectangle2D.Double(target.getX()-18, target.getY()-18, 36, 36);
		return enemyArea.contains(bulletVector.projectFromStartPos(currentTime-bulletVector.getTime()));
	}
	public static boolean checkForHit(Point2D bulletLoc, Enemy target)
	{

		Rectangle2D.Double enemyArea = new Rectangle2D.Double(target.getX()-18, target.getY()-18, 36, 36);
		return enemyArea.contains(bulletLoc);
	}
	

	
}
