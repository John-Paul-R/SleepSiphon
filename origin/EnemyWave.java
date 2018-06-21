package origin;

import java.awt.geom.Point2D;

import gun.vGunAngle;
import gun.vGunPoint;
import util.TimedPoint;
import gun.VirtualBullet;
import gun.enemyGunSim;

public class EnemyWave extends Wave
{
	//TODO make base risk based on who was closest to source enemy
	private static final int NUMGUNS = 3; //will currently only test for HeadOn, Linear, and "Circular" (constant turn rate)
	private double bulletPower;
	private double baseRisk = 1000; //Risk value based on:  Were you the closest bot to the enemy when they fired, has the enemy hit you recently & bullet power
	private double[] firingAngles;
	private String sourceName;
	private int close = 4; //Is a counter that decrements when wave comes close to crashing/crashes.  When reaches zero, wave is removed.
	//private Point2D.Double[] bulletLocs = new Point2D.Double[NUMGUNS]; 
	
	public EnemyWave(double cX, double cY, long sT, double bP, BotData self, String sN) 
	{
		super(cX, cY, sT, bP);
		sourceName = sN;
		bulletPower = bP;
		firingAngles = generateFiringAngles(new Point2D.Double(cX, cY), self);
	}
	
	public String getSource()
	{
		return sourceName;
	}

	private Point2D.Double[] bulletLocationBuffer = new Point2D.Double[NUMGUNS];;
	private long bufferTime = -1;
	public double getRisk(TimedPoint point)//Gets the risk from this wave at a given point  (inverse square of distance from bullet hotspot?)
	{
		double x1 = point.getX(), y1 = point.getY();
		double bulletRiskFactor = 0;
		
		if (bufferTime != point.getTime())
			updatePredictedBulletLocations(point.getTime()-super.startTime);
		
		
		for (int i = 0; i < NUMGUNS; i++)
		{
			double dist = Point2D.distance(bulletLocationBuffer[0].getX(), bulletLocationBuffer[0].getY(), x1, y1);
			bulletRiskFactor += 1/(dist*dist);//risk factor scales inversely with the square of the distance from it
		}
		return baseRisk * bulletRiskFactor;
	}
	/*//Just testing...
	public Vector getRiskVector(TimedPoint point)//Gets the risk from this wave at a given point  (inverse square of distance from bullet hotspot?)
	{
		double x1 = point.getX(), y1 = point.getY();
		Vector bulletRiskVector = new Vector(0,0);
		
		if (bufferTime != point.getTime())
			updatePredictedBulletLocations(point.getTime()-super.startTime);
		
		
		for (int i = 0; i < NUMGUNS; i++)
		{
			Vector cBulletRisk = new Vector(
					Point2D.distance(bulletLocationBuffer[i].getX(), bulletLocationBuffer[i].getY(), x1, y1),
					Math.atan2(bulletLocationBuffer[i].getX()-x1, bulletLocationBuffer[i].getY()-y1));
			bulletRiskVector.add(cBulletRisk);
			}
		bulletRiskVector.setMagnitude(baseRisk * bulletRiskVector.getMagnitude());
		return bulletRiskVector;
	}*/
	public double getDist(double x, double y, long time)
	{
		double relX = x-centerX, relY = y-centerY;
		Point2D.Double waveEdge = Util.project(super.getCenter(), Math.atan2(relX, relY), super.bulletVelocity, time-super.startTime);

		return Point2D.distance(waveEdge.getX(), waveEdge.getY(), x, y);
	}
	
	public Point2D.Double[] getBulletLocations(long time)
	{
		updatePredictedBulletLocations(time - super.startTime);
		return bulletLocationBuffer;
	}
	
	// (PRIVATE / BACKEND)
	private void updatePredictedBulletLocations(long timeOffset)
	{ 
		for (int i = 0; i < NUMGUNS; i++)
		{
			bulletLocationBuffer[i] = Util.project(super.getCenter(), firingAngles[i], super.bulletVelocity, timeOffset);
		}
	}
	private double[] generateFiringAngles(Point2D.Double source, BotData target)//This runs once, when the wave is created
	{
		double[] output = new double[NUMGUNS];
		output[0] = robocode.util.Utils.normalRelativeAngle(enemyGunSim.headOn(source, target, bulletPower));
		output[1] = robocode.util.Utils.normalRelativeAngle(enemyGunSim.linearPredictionFire(source, target, bulletPower));
		output[2] = robocode.util.Utils.normalRelativeAngle(enemyGunSim.constantTurnPredict(source, target, bulletPower));
		//System.out.println("Angles: \t" + output[0] +"\t" +output[1] +"\t"+output[2]);
		return output;
	}

	public void setClose() 
	{
		close -= 1;
	}
	public int getClose()
	{
		return close;
	}
	
}
