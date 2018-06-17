package BetaF;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.LinkedList;

import gun.BulletVector;
import gun.VirtualBullet;
import robocode.Robot;
import util.Vector2D;
public class Enemy
{
	private static final int BOT = 36;
	private static final int HALF_BOT = 18;
    private ArrayList<EnemyData> data;
    private int currentIndex;
    
    //VIRTUAL GUNS
    private int bestGun = -1; //Numeric ID for each gun ([0 : Head-On] [1 : Linear Prediction] [2 : Turn Predict] [3 : AvgVel&Heading])
    private int[][] gunStats;
    private double energyLostThisTurn;
    private double energyGainedThisTurn;
    private LinkedList<VirtualBullet> virtualPointBullets;
    
    //private double threatLevel; //represents the danger an enemy poses based on a number of factors. Base 1.
    //private double predictability; //double value from 0 to 1 representing the fraction of vBullets from our best gun that hit the enemy
	private LinkedList<BulletVector> angleBullets;
	private int numAddedID;
    //WAVES
    //private LinkedList<EWave>
	private double closestDist;
	private boolean updatedWaves;
    
    //ClASS
    public Enemy(EnemyData initialData, int[][] gunStats, int numAdded)
    {
    	data = new ArrayList<EnemyData>();
    	virtualPointBullets = new LinkedList<VirtualBullet>();
    	angleBullets = new LinkedList<BulletVector>();
    	this.gunStats = gunStats;
    	
    	energyLostThisTurn = 0;
    	energyGainedThisTurn = 0;
    	numAddedID = numAdded;
    	currentIndex = 0;
    	data.add(currentIndex, initialData);
    	closestDist = Integer.MAX_VALUE;
    	updatedWaves = false;
    }
    
    //get
    //all get methods return latest data (Soon: unless specified)
    public EnemyData getLatest() {return data.get(currentIndex);}
    public ArrayList<EnemyData> getDataSet() {return data;}
    public double getEnergy() {return data.get(currentIndex).getEnergy();}
    public double getBearing() {return data.get(currentIndex).getBearing();}
    public double getDistance() {return data.get(currentIndex).getDistance();}
    public double getHeading() {return data.get(currentIndex).getHeading();}
    public double getVelocity() {return data.get(currentIndex).getVelocity();}
    public long getTime() {return data.get(currentIndex).getTime();}
    public long timeSinceUpdate(long currentTime) {return getLatest().getAge(currentTime);}
    public double getX() {return data.get(currentIndex).getX();}
    public double getY() {return data.get(currentIndex).getY();}
    public Point2D.Double getLocation() {return new Point2D.Double(this.getX(), this.getY());}
    public double getLatestTurnRate()
    {
    	if (data.size() > 1)
    		return data.get(currentIndex).getHeading()-data.get(currentIndex-1).getHeading();
    	return 0;
    }
    //other
    public int getNumAddedID()
    {
    	return numAddedID;
    }
    public void newRound(EnemyData initialData)
    {
    	data = new ArrayList<EnemyData>();
    	virtualPointBullets = new LinkedList<VirtualBullet>();
    	angleBullets = new LinkedList<BulletVector>();
    	
    	currentIndex = 0;
    	data.add(currentIndex, initialData);
    	
    	energyLostThisTurn = 0;
    	energyGainedThisTurn = 0;
    }
    public synchronized void addEnemyData(EnemyData newData)
    {
    	currentIndex++;
    	data.add(currentIndex, newData);
    }
    
    public LinkedList<VirtualBullet> getVirtualPointBulletsSet()
    {
    	return virtualPointBullets;
    }
    
	public double getCurrentBearing(Point2D.Double referencePoint)
	{
		double relX = getX() - referencePoint.getX();
		double relY = getY() - referencePoint.getY();
	
		return Math.atan(relX/relY);
	}
	
    
/*	public String getGunStatsString()
	{
		String output = "";
		for (int i = 0; i < gunStats.length; i++)
		{
			output += gunStats[i][0] + ",";
		}
		output += "," + gunStats[i][1];
		return output;
	}*/
	
	public void updateGunStat(boolean hitOrMiss, int gunID)//Predict bullet position at time.  When it is that time, test if that position is w/in the robot area
	{
		gunStats[gunID][1] += 1;
/*		for (int i = 0; i < gunData.length; i++)
		{
			//ROLLING AVG 1
			//gunHits[i] = (int)(((double)gunHits[i]*10 + (double)gunData[i])/11.0);
			//ROLLING AVG 2
			//gunHits[i]*=4 ;
			//gunHits[i]+= gunData[i];
			//totalFired=5;
			//BASIC AVERAGE
			gunHits[i]+=gunData[i];
		}*/
		gunStats[gunID][0] += (hitOrMiss) ? 1 : 0;

	}
	
	public int getBestGun()
	{
		bestGun = -1;
		double bestHitRate = -1;
		for (int i = 0; i < gunStats.length; i++)
		{
			double currentHitRate = ((double)gunStats[i][0] / (double)gunStats[i][1]);
			if (currentHitRate > bestHitRate)
			{
				bestHitRate = currentHitRate;
				bestGun = i;
			}
		}
		return bestGun;
	}
	
	public int numEntries()
	{
		return data.size();
	}
	
	public EnemyData getPreviousData() //Returns current data if there is only one point in data set
	{
		EnemyData output = null;
		if (currentIndex > 1)
			output = data.get(currentIndex-1);
		else
			output = data.get(currentIndex);
		return output;
	}

	public void setEnergyLost(double power)
	{
		energyLostThisTurn = power;
	}
	public void setEnergyGained(double eGain)
	{
		energyGainedThisTurn = eGain;
	}
	public double getEnergyLostThisTurn()
	{
		return energyLostThisTurn;
	}
	public double getEnergyGainedThisTurn()
	{
		return energyGainedThisTurn;
	}

	public long getTimeSinceDecel() {
		return getLatest().getTimeSinceDecel();
	}

	public double getBestGunHitRate() 
	{
		double bestHitRate = 0.0;
		for (int i = 0; i < gunStats.length; i++)
		{
			double currentHitRate = ((double)gunStats[i][0] / (double)gunStats[i][1]);
			if (currentHitRate > bestHitRate)
			{
				bestHitRate = currentHitRate;
			}
		}
		return bestHitRate;
	}

	public Point2D.Double getPosition()
	{
		EnemyData latest = getLatest();
		return new Point2D.Double(getX(), getY());
	}

	public int getScanOrder() {
		return getLatest().getScanOrder();
	}

	public LinkedList<BulletVector> getVirtualAngleBulletsSet() {
		return angleBullets ;
	}

	public long getRelativeAge(long time)
	{
		long relativeAge = getLatest().getAge(time) + getScanOrder();
		return relativeAge;
	}

	public void setClosestDist(double diameter) {
		closestDist = diameter;
	}
	public double getClosestDist() {

		return closestDist;
	}

	public double getDistFromWall() 
	{
		double[] bounds = Util.getAbsoluteFieldBoundsxXyY();
		double x = getX();
		double y = getY();
		
        double shortestDistance = Math.min(Math.min(Math.min(Point2D.distance(x, 0.0, bounds[0], 0.0), Point2D.distance(x, bounds[2], bounds[1], bounds[2])), Point2D.distance(0.0, y, 0.0, bounds[2])),Point2D.distance(bounds[1], y, bounds[1], bounds[3]));

        return shortestDistance;
	}

	public void setUpdatedWaves(boolean b) {
		updatedWaves = b;
	}
	public boolean getUpdatedWaves()
	{
		return updatedWaves;
	}
} 