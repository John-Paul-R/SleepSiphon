package BetaF;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import robocode.Rules;

public class Wave
{
	double centerX;
	double centerY;
	protected long startTime;
	protected double bulletVelocity;
	//private double MEA;
	
	public Wave(double cX, double cY, long sT, double bP)
	{
		centerX = cX;
		centerY = cY;
		startTime = sT;
		
		bulletVelocity = Rules.getBulletSpeed(bP);
		//MEA = Math.asin(8 / bulletVelocity);
	}	
	
	public Point2D.Double getCenter() {
		return new Point2D.Double(centerX, centerY);
		}
	
	public double getRad(long currentTime)
	{
		return bulletVelocity * (currentTime-startTime);
	}
	public Ellipse2D.Double getCircle(long time)
	{
		
		double currentRadius = bulletVelocity * (time-startTime);
		return new Ellipse2D.Double(
				centerX-currentRadius,
				centerY-currentRadius,
				currentRadius*2,
				currentRadius*2);
	}
	
	//wave.getRisk()
	//Hmm.. some risk needs to be based on *who* fired it (ex: do they have a history of performing well
	//and the other risk components need to be based on if you were the closest enemy to this target, have they hit you recently, etc.
}
