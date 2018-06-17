package BetaF;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import gun.vGunAngle;

import java.awt.geom.*;

import BetaF.SleepSiphon;

public class Util
{
	private static double[] fieldBounds;
	private static double[] absoluteFieldBounds;

	
	public static Enemy getOldest(ConcurrentHashMap<String, Enemy> enemies, SleepSiphon self)
	{
		String oldestName = "";
		long oldestTime = 0;
		for (Entry<String, Enemy> entry : enemies.entrySet())
		{
			long entryTime = entry.getValue().timeSinceUpdate((int)self.getTime());
			if ( entryTime > oldestTime )
			{
				oldestName = entry.getKey();
				oldestTime = entryTime;
			}
		}
		return enemies.get(oldestName);
	}

	public static String getOldestName(ConcurrentHashMap<String, Enemy> enemies, SleepSiphon self)
	{
		String oldestName = "";
		long oldestTime = 0;
		for (Entry<String, Enemy> entry : enemies.entrySet())
		{
			long entryTime = entry.getValue().timeSinceUpdate((int)self.getTime());
			if ( entryTime > oldestTime)
			{
				
				oldestName = entry.getKey();
				oldestTime = entryTime;
			
			}
			else if (entryTime == oldestTime && enemies.get(oldestName) != null)
			{
				if (entry.getValue().getScanOrder() < enemies.get(oldestName).getScanOrder())
				{
					oldestName = entry.getKey();
					oldestTime = entryTime;
				}
			}
			 
		}
		return oldestName;
	}
	public static boolean withinTolerance(double expectedValue, double actualValue, double tolerance)
	{
		if (actualValue >= expectedValue - tolerance && actualValue <= expectedValue + tolerance)
			return true;
		return false;
	}
	public static boolean withinTolerance(int expectedValue, int actualValue, int tolerance)
	{
		if (actualValue >= expectedValue - tolerance && actualValue <= expectedValue + tolerance)
			return true;
		return false;
	}
	public static boolean isPerfectSquare(int x)
	{
		boolean isPSquare = false;
		int sr = (int) Math.sqrt(x);
		if ((int) Math.pow(sr, 2) == x)
			isPSquare = true;
		return isPSquare;
	}
	public static int baseConvertToDec(int num, int fromBase)
 	{
		int output = 0;
		int numOfDigits = 0;
		int temp = num;
		while (temp >= 10)//returns n-1; n being the number of digits 
		{
			temp = temp / 10;
			numOfDigits++;
		}
		
		temp = num;
		for (int i = 0; i <= numOfDigits; i++)
		{
			output += (temp % 10) * (int) Math.pow(fromBase, i);
            temp /= 10;
		}
		return output;
	}
	public static int currentSection2DUniform(int numSections, int x, int y, int areaWidth, int areaHeight) //Calculates current section ID in this format > 0,0, up until bounds > back to bottom, one section to the right > repeat
	{
		if (!isPerfectSquare(numSections))
				return -1;
		int section = 0;
		//int xSec = 0;
		int xTest = 0;
		//int ySec = -1;
		int yTest = 0;
		//int cSec = 0;
		int xSec = -1;
		int ySec = -1;
		
		int oneDimSections = (int) Math.sqrt(numSections);
		while (xSec < (int) Math.sqrt(numSections)-1 && x >= xTest)
			{
				xTest += areaWidth/oneDimSections;
				xSec++;
			}
		while (ySec < (int) Math.sqrt(numSections)-1 && y >= yTest)
			{
				yTest += areaWidth/oneDimSections;
				ySec++;
			}
		//System.out.println("Binary Coords:\t" + xSec+ySec);
		section = baseConvertToDec(xSec*10+ySec, oneDimSections);
		//alternative
/*		while (cSec < numSections)
		{
			if (x >= xTest)
			{
				if (cSec > (int) Math.sqrt(numSections))
				{
					xTest += areaWidth/oneDimSections;
					yTest = 0;
				}
				if (y >= yTest)
				{
					yTest += areaHeight/oneDimSections;
					cSec++;
				}
			}
		}*/
		//System.out.println("Section:\t\t" + section);
		return section; //Makes result index 0 bc I have programmer syndrome
	}
	
	public static Point2D.Double predictLocationLinear(Enemy target, int time)//double x, double y, double velocity, double heading, int timeOfData
	{
		//adjust this for time offset of data being used
		double futureX = target.getX() + (target.getVelocity() * (double) time * Math.sin(target.getHeading()));
		double futureY = target.getY() + (target.getVelocity() * (double) time * Math.cos(target.getHeading()));
		
		return new Point2D.Double(futureX, futureY);
	}
	public static Point2D.Double project(Point2D.Double origin, double direction, double velocity, long time)
	{
		double futureX = origin.getX() + (velocity * (double) time * Math.sin(direction));
		double futureY = origin.getY() + (velocity * (double) time * Math.cos(direction));
		
		return new Point2D.Double(futureX, futureY);
	}
	public static Point2D.Double projectVector(Point2D.Double origin, double direction, double magnitude)
	{
		double futureX = origin.getX() + (magnitude * Math.sin(direction));
		double futureY = origin.getY() + (magnitude * Math.cos(direction));
		
		return new Point2D.Double(futureX, futureY);
	}
	
	public static double limitValueBounds(double value, double min, double max) 
	{
	    return Math.min(max, Math.max(min, value));
	}
	public static Point2D.Double limitCoordinateToMap(Point2D.Double coord) 
	{
		final double bounds[] = getFieldBoundsxXyY();
	    return new Point2D.Double(limitValueBounds(coord.getX(), bounds[0], bounds[1]), Util.limitValueBounds(coord.getY(), bounds[2], bounds[3]));
	}
	public static Point2D.Double limitCoordinateToMap(double x, double y) 
	{
		final double bounds[] = getFieldBoundsxXyY();
	    return new Point2D.Double(limitValueBounds(x, bounds[0], bounds[1]), Util.limitValueBounds(y, bounds[2], bounds[3]));
	}
	public static void changeCoordinateToMap(Point2D point) 
	{
		final double bounds[] = getFieldBoundsxXyY();
	    point.setLocation(limitValueBounds(point.getX(), bounds[0], bounds[1]), Util.limitValueBounds(point.getY(), bounds[2], bounds[3]));
	}
    public static double[] getAbsoluteFieldBoundsxXyY()
    {	
    	return absoluteFieldBounds;

    }
    public static double[] getFieldBoundsxXyY()
    {	
    	return fieldBounds;

    }
    public static Point2D.Double getFieldCenter()
    {	
    	
    	return new Point2D.Double(absoluteFieldBounds[1]/2, absoluteFieldBounds[3]/2);

    }
    public static void setFieldBoundsArray(SleepSiphon self, double buffer)
    {
    	absoluteFieldBounds = new double[] {0, self.getBattleFieldWidth(), 0, self.getBattleFieldHeight()};
    	fieldBounds =  new double[] {self.getWidth()/2+buffer, self.getBattleFieldWidth() - (self.getWidth()/2+buffer), self.getHeight()/2+buffer, self.getBattleFieldHeight() - (self.getHeight()/2+buffer)};
    }
    
    
    
/*    public static double[] getFiringAngles(Map<Integer, BotData> targetHistory, BotData sourceInstant, long time)
    {
    	BotData targetInstant = targetHistory.get((int)time);
    	
    	
    	
		return null;
    }
    public static double[] getFiringAngles(BotData targetInstant, BotData sourceInstant)
    {
    	
    	
    	
		return null;
    }*/
}
