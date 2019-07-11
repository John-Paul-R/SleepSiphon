package gun;

import java.awt.geom.Point2D;

import origin.Analysis;
import origin.Enemy;
import origin.Util;
import robocode.Rules;
import util.TimedPoint;

public class vGunAngle
{
	public static double headOn(TimedPoint source, Enemy target, double BULLET_POWER)
    {
		return Math.atan2(target.getX()-source.getX(), target.getY()-source.getY());
    } 
    
    public static double linearPredictionFire(TimedPoint source, Enemy target, double BULLET_POWER)
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
    		time = (Math.min(t1, t2) >= 0 ? Math.min(t1, t2) : Math.max(t1, t2)) + (source.getTime() - target.getTime()); 
    		
    		final double[] bounds = Util.getFieldBoundsxXyY();
    		endX = Util.limitValueBounds(eX+source.getX()+eVelocityX*time, bounds[0], bounds[1]);
    		endY = Util.limitValueBounds(eY+source.getY()+eVelocityY*time, bounds[2], bounds[3]);
 
    	}
    	
    	return Math.atan2(endX-source.getX(), endY-source.getY());
    }

    public static double constantTurnPredict(TimedPoint source, Enemy target, double BULLET_POWER)
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
    
    public static double averageMovementGun(TimedPoint source, Enemy target, double BULLET_POWER)
    {
    	double avgVelocity = Analysis.getAvgVelocity(target);
    	double avgHeading = Analysis.getAvgHeading(target);

    	double time = -1;
    	double endX = 0;
    	double endY = 0;
    	final double eX = target.getX()-source.getX();//enemy X relative to self
    	final double eY = target.getY()-source.getY();//enemy Y relative to self

    	final double avgVelocityX = avgVelocity*Math.sin(avgHeading);
    	final double avgVelocityY = avgVelocity*Math.cos(avgHeading);
    	final double bulletVelocity = Rules.getBulletSpeed(BULLET_POWER);
    	
    	final double c = eX*eX + eY*eY;
    	final double a2 = bulletVelocity*bulletVelocity;
    	final double a = avgVelocityX*avgVelocityX + avgVelocityY*avgVelocityY - a2;
    	final double b = 2*(eX*avgVelocityX + eY*avgVelocityY);

    	final double discrim = b*b - 4*a*c;
    	if (discrim >= 0) //check to make sure solution exists. If solution exists, proceed with calculations.
    	{
    		final double t1 = (-b + Math.sqrt(b*b - 4*a*c))/(2*a);
    		final double t2 = (-b - Math.sqrt(b*b - 4*a*c))/(2*a);
    		time = (Math.min(t1, t2) >= 0 ? Math.min(t1, t2) : Math.max(t1, t2)) + (source.getTime() - target.getTime()); //Ternary operator: If the lower val root is greater than 0, return that value. else return the larger value // add the age of the data we are using for calculations to the time 
    		//assume enemy will stop at walls (constrain x & y values to battlefield)
    		final double[] bounds = Util.getFieldBoundsxXyY();
    		endX = Util.limitValueBounds(eX+source.getX()+avgVelocityX*time, bounds[0], bounds[1]);
    		endY = Util.limitValueBounds(eY+source.getY()+avgVelocityY*time, bounds[2], bounds[3]);

    	}
		return Math.atan2(endX-source.getX(), endY-source.getY());
    }
}
