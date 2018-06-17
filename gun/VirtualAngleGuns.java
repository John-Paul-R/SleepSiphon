package gun;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

import BetaF.Aim;
import BetaF.Enemy;
import BetaF.SleepSiphon;
import BetaF.Util;
import BetaF.WeightedPoint;
import robocode.Rules;
import util.TimedPoint;
import util.Vector2D;

public class VirtualAngleGuns 
{
	//Numeric ID for each gun ([0 : Head-On] [1 : Linear Prediction] [2 : Turn Predict] [3 : AvgVel&Heading])
	//private static final int HEAD_ON = 0, LINEAR = 1, TURN_BASIC = 2, AVG_VH = 3;
	private static final int numGuns = 4;
	private ConcurrentHashMap<String, Enemy> enemyMap;
	private SleepSiphon self;
	
	public VirtualAngleGuns(SleepSiphon s, ConcurrentHashMap<String, Enemy> e)
	{
		self = s;
		enemyMap = e;
	}
	
	public void updateEnemy(Enemy target)
	{
		long currentTime = self.getTime();
		long scanTime = target.getTime();
		if (scanTime == currentTime)
		{
			boolean enemyDataExistsNOW = true;
			int[] bulletSuccess = new int[numGuns];
			LinkedList<BulletVector> vBullets = target.getVirtualAngleBulletsSet();
			for (int i = 0; i < vBullets.size(); i++)
			{
				BulletVector cBulletVector = vBullets.get(i);
				//if (cBulletVector.getTime() == target.getTime())//if it is the time that the bullet was predicted to impact
				
				double rad = cBulletVector.getLengthAtTime(currentTime);
				double eDistFromBulletStart = cBulletVector.getStartPos().distance(target.getPosition());
				WeightedPoint projectedBulletLoc = new WeightedPoint(cBulletVector.projectByRad(rad));
				double cWeight = .1;
				if (Util.withinTolerance(rad, eDistFromBulletStart, 18))
				{
					target.updateGunStat(enemyGunSim.checkForHit(projectedBulletLoc, target), cBulletVector.getGunID());
					cWeight += .9;
					//System.out.println("Updated Gun " + cBulletVector.getGunID() + " for " + target);
	
				}
				vBulletLocs.add(projectedBulletLoc);
				projectedBulletLoc.setWeight(cWeight);
	
	/*				vBullets.remove(cBulletVector);//Remove bullet
					i-=1;*/
				
				if (projectedBulletLoc.getWeight() == 1 || cBulletVector.getAge(currentTime) > 100)
				{
					vBullets.remove(cBulletVector);//Remove bullet
					i-=1;
				}
			}
		}
	}
	
	public void updateAll()
	{
		vBulletLocs = new LinkedList<WeightedPoint>();
		//Update gun success rate for each enemy
		for (Enemy enemy : enemyMap.values())
		{
			updateEnemy(enemy);
		}
	}
	
	public void virtualFireAll()
	{
		for (Enemy enemy : enemyMap.values())
		{
			virtualFire(enemy);
		}
	}
	private void virtualFire(Enemy target)
	{
		LinkedList<BulletVector> vBullets = target.getVirtualAngleBulletsSet();
		for (int i = 0; i < numGuns; i++)
		{
			vBullets.add(virtualGunOutput(target, i));
		}
	}
	private BulletVector virtualGunOutput(Enemy target, int gunID)
	{
		long currentTime = self.getTime();
		double BULLET_POWER = Aim.firePowerByDist(self, target);
		double BULLET_SPEED = Rules.getBulletSpeed(BULLET_POWER);
		TimedPoint selfTimedPos = new TimedPoint(self.getX(), self.getY(), currentTime);
		BulletVector bulletVector = null;
		switch (gunID)
		{
		case 0 : bulletVector = new BulletVector(selfTimedPos, vGunAngle.headOn(selfTimedPos, target, BULLET_POWER), BULLET_SPEED, gunID);
		break;
		case 1 : bulletVector = new BulletVector(selfTimedPos, vGunAngle.linearPredictionFire(selfTimedPos, target, BULLET_POWER), BULLET_SPEED, gunID);
		break;
		case 2 : bulletVector = new BulletVector(selfTimedPos, vGunAngle.constantTurnPredict(selfTimedPos, target, BULLET_POWER), BULLET_SPEED, gunID);
		break;
		case 3 : bulletVector = new BulletVector(selfTimedPos, vGunAngle.averageMovementGun(selfTimedPos, target, BULLET_POWER), BULLET_SPEED, gunID);
		break;
		}
		return bulletVector;
	}
	private LinkedList<WeightedPoint> vBulletLocs = new LinkedList<WeightedPoint>();
	public void onPaint(Graphics2D g, boolean enabled)
	{
		if (enabled)
		{
			g.setColor(new Color(255, 20, 20, 185));
			int count = 0;
			int totalBullets = vBulletLocs.size();
			final int rad = 6;
			//BULLET POINTS
			for (WeightedPoint c : vBulletLocs)
			{
				Color rgb = new Color(Color.HSBtoRGB((float) ((double)count/totalBullets), 1F, 1F));
				Color rgba = new Color(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), (int)(c.getWeight()*255));
				g.setColor(rgba);
				g.fillOval((int)c.getX()-rad/2, (int)c.getY()-rad/2, rad, rad);
				count++;
			}
			//BULLET LINES
	/*		Point2D.Double prevBulletLoc = vBulletLocs.get(0);
			for (Point2D.Double c : vBulletLocs)
			{
				g.setColor(new Color(Color.HSBtoRGB((float) ((double)count/totalBullets), 1, 1)));
				g.drawLine((int)c.getX(), (int)c.getY(), (int)prevBulletLoc.getX(), (int)prevBulletLoc.getY());
				count++;
				prevBulletLoc = c;
			}*/
		}
	}
}
