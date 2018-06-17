package BetaF;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import BetaF.SleepSiphon;
import robocode.Rules;
import util.TimedWeightedPoint;

public class Analysis
{
	
	public static double predictConstantTurn(SleepSiphon self, Enemy target) // This function either returns an expected turn amount, or returns 0.  (if 0, either the target is not turning, or the pattern could not be deduced.)
	{
		double futureTurnSpeed = 0;
		//int patternID = -1;// -1 = a pattern could not be identified //Method originally had different purpose
		ArrayList<EnemyData> dataSet = target.getDataSet();
		int dataSetSize = dataSet.size();
		final int numDataReq = 5; //number of supporting data we need to set pattern
		if (dataSet.size() > numDataReq+1)
		{
			double[] turnDist = new double[numDataReq];
			long[] timeOfData = new long[numDataReq]; //time of above data
			long[] timeSinceLastData = new long[numDataReq]; //time of above data
			for (int i = dataSetSize-numDataReq; i < dataSetSize; i++)
			{
				turnDist[i-(dataSetSize-numDataReq)] = dataSet.get(dataSetSize-i).getHeading() - dataSet.get(dataSetSize-i-1).getHeading();
				timeOfData[i-(dataSetSize-numDataReq)] = dataSet.get(dataSetSize-i).getTime();
				timeSinceLastData[i-(dataSetSize-numDataReq)] = dataSet.get(dataSetSize-i).getTime() - dataSet.get(dataSetSize-i-1).getTime();
			}
			//calculate turn speed, not amount
			double[] turnSpeed = new double[numDataReq];
			for(int i = 0; i < numDataReq; i++)
			{
				turnSpeed[i] = turnDist[i]/(timeOfData[i]/timeSinceLastData[i]);
			}
			
			for(int i = 0; i < numDataReq-1; i++)
			{
				if( turnSpeed[i] <= turnSpeed[i+1]+10 && turnSpeed[i] >= turnSpeed[i+1]-10)
				{
					futureTurnSpeed = turnSpeed[i];
				}
				else
					return 0;
	
			}
		}
		//System.out.println("Turn Speed: " + futureTurnSpeed);++
		return futureTurnSpeed;
	}
	
	//LOOK HERE
	private final static double numDataReq = 3;
	public static double getAvgVelocity(Enemy target) // This function either returns an expected turn amount, or returns 0.  (if 0, either the target is not turning, or the pattern could not be deduced.)
	{
		ArrayList<EnemyData> dataSet = target.getDataSet();
		int dataSetSize = dataSet.size();
		 //number of supporting data we need to set pattern
		double avgVelocity = 0;
		if (dataSetSize > numDataReq)
		{
			for (int i = 0; i < numDataReq; i++)
			{
				avgVelocity += dataSet.get(dataSetSize-i-1).getVelocity();
			}
			avgVelocity /= (double)numDataReq;
		}
		return avgVelocity;
	}
	
	public static double getAvgHeading(Enemy target) // This function either returns an expected turn amount, or returns 0.  (if 0, either the target is not turning, or the pattern could not be deduced.)
	{
		ArrayList<EnemyData> dataSet = target.getDataSet();
		int dataSetSize = dataSet.size();
		 //number of supporting data we need to set pattern
		double avgHeading = 0;
		if (dataSetSize > numDataReq)
		{
			for (int i = 0; i < numDataReq; i++)
			{
				avgHeading += dataSet.get(dataSetSize-i-1).getHeading();
			}
			avgHeading /= (double)numDataReq;
		}
		return avgHeading;
	}	
	
	public static Point2D.Double predictInterceptionPoint_TargetConstantTurn(SleepSiphon self, Enemy target, double turnRate, double bulletPower)//double turnRate, double velocity, double time, double dataAge
	{
		final double selfX = self.getX();
		final double selfY = self.getY();
		final double eX = target.getX() - self.getX();
		final double eY = target.getY() - self.getY();
		final double bulletVelocity = Rules.getBulletSpeed(bulletPower);

		
		double previousX = 0;
		double previousY = 0;
		double time = 0;
		
		double finalX = eX;
		double finalY = eY;
		
		double eVX = target.getVelocity() * Math.sin(target.getHeading());
		double eVY = target.getVelocity() * Math.sin(target.getHeading());
		
		while (Point2D.distance(selfX, selfY, finalX, finalY) >= (bulletVelocity * (time - (self.getTime() - target.getTime()))))
		{
			eVX = target.getVelocity() * Math.sin(target.getHeading() + (turnRate * time));
			eVY = target.getVelocity() * Math.cos(target.getHeading() + (turnRate * time));
			
			finalX += eVX;
			finalY += eVY;
			time++;
		}
		
		final double [] bounds= Util.getFieldBoundsxXyY();
		
		return new Point2D.Double (Util.limitValueBounds(finalX+selfX, bounds[0], bounds[1]),
				Util.limitValueBounds(finalY+selfY, bounds[0], bounds[1]));
	}
	//TODO : path Points are easier to calculate if they are done as RINGS, rather than PATHS
	// This is because you only have to reevaluate the points of waves once per time interval(once per ring), rather than rather than (once per point)
	public static Point2D calcPointRisk(Point2D point)
	{
		return point;
		
	}
	
	public static Point2D[] calcPathRisk(Point2D[] path)
	{
		return path;
		
	}
    public static double calcPoint_repelWalls(double x, double y) {
        double[] bounds = Util.getAbsoluteFieldBoundsxXyY();
        double[] r = new double[]{1.0 / Point2D.distanceSq(x, 0.0, bounds[0], 0.0), 1.0 / Point2D.distanceSq(x, bounds[2], bounds[1], bounds[2]), 1.0 / Point2D.distanceSq(0.0, y, 0.0, bounds[2]), 1.0 / Point2D.distanceSq(bounds[1], y, bounds[1], bounds[3])};
        double totalRisk = 0.0;
        int i = 0;
        while (i < 4) {
            totalRisk += r[i];
            ++i;
        }
        return totalRisk / 4.0;
    }
	public static double[] calcPathRisks_WAVE(TimedWeightedPoint[][] paths, LinkedList<EnemyWave> waveSet, int pointsPerWave, long currentTime)
	{
		int NUMPATHS = paths.length;
		int NUMINTERVALS = paths[0].length;
		final double DIAM = 10;
		final double BOTDIM = 36;
		double[] pathRisks = new double[paths.length];
		for (int t = 0; t < NUMINTERVALS; t++)
		{
			//calc wave bullet locs
			WeightedPoint[][] bulletLocs = new WeightedPoint[waveSet.size()][pointsPerWave];//Use weighted points so that we can determine risk of each potential bullet location
			Ellipse2D.Double[][] bulletCircles = new Ellipse2D.Double[waveSet.size()][pointsPerWave];
			
			for(int i = 0; i < bulletLocs.length; i++)
			{
				Point2D.Double[] locs = (waveSet.get(i).getBulletLocations(currentTime + paths[0][t].getTime()));
				for (int j = 0; j < pointsPerWave; j++)
				{
					bulletLocs[i][j] = new WeightedPoint(locs[j]);
					bulletCircles[i][j] = new Ellipse2D.Double(locs[j].getX()-DIAM/2, locs[j].getY()-DIAM/2, DIAM, DIAM);//point to circle
				}
			}
			
			//loop to find & set risk of each point in this ring and add it to total for that path(time interval)
			for(int i = 0; i < NUMPATHS; i++)
			{
				TimedWeightedPoint cPoint = paths[i][t];
				Rectangle2D.Double botAreaAtPoint = new Rectangle2D.Double(cPoint.getX()-BOTDIM/2, cPoint.getY()-BOTDIM/2, BOTDIM, BOTDIM);
				checkForIntersections:
				for (int waveIndex = 0; waveIndex < waveSet.size(); waveIndex++)
				{
					for (int wavePointIndex = 0; wavePointIndex < pointsPerWave; wavePointIndex++)
					{
						System.out.println("BULLET LOC:\t" + (int)bulletLocs[waveIndex][wavePointIndex].getX() + "\t" + (int)bulletLocs[waveIndex][wavePointIndex].getY());
						System.out.println("ROBOT LOC:\t" + (int)cPoint.getX() + "\t" + (int)cPoint.getY());
						if (bulletCircles[waveIndex][wavePointIndex].intersects(botAreaAtPoint))
						{
							cPoint.setWeight(5.5);//if bulletCircle intersects with myPredictedLoc rectangle, risk = 1
							System.out.println("BULLET INTERSECTS PROJECTED BOT LOCATION");
							break checkForIntersections;
						}
					}
				}
				cPoint.setWeight(cPoint.getWeight()+Math.random()*0.1);
				//System.out.println(cPoint.getWeight());
				//add risk of this point to risk of path
				pathRisks[i] += cPoint.getWeight();
			}

		}
		
		//iterate through the path risks array to find the path with least risk. Return that path's index
/*		int safestPathIndex = 0;
		double safestRiskValue = Double.POSITIVE_INFINITY;
		for (int i = 0; i < NUMPATHS; i++)
		{
			if (pathRisks[i] < safestRiskValue)
			{
				safestPathIndex = i;
				safestRiskValue = pathRisks[i];
			}
		}*/
		
		return pathRisks;
	}
	
	public static int getSafestPathIndex(double[] pathRisks)
	{
		//iterate through the path risks array to find the path with least risk. Return that path's index
		int safestPathIndex = 0;
		double safestRiskValue = Double.POSITIVE_INFINITY;
		for (int i = 0; i < pathRisks.length; i++)
		{
			if (pathRisks[i] <= safestRiskValue)
			{
				safestPathIndex = i;
				safestRiskValue = pathRisks[i];
			}
		}
		return safestPathIndex;
	}
	
	public static double[] calcPathRisksByDist_WAVE(TimedWeightedPoint[][] paths, LinkedList<EnemyWave> waveSet, int maxPointsPerWave, long currentTime)
	{
		int NUMPATHS = paths.length;
		int NUMINTERVALS = paths[0].length;
		double[] pathRisks = new double[paths.length];
		for (int t = 0; t < NUMINTERVALS; t++)
		{
			//calc wave bullet locs
			WeightedPoint[][] bulletLocs = new WeightedPoint[waveSet.size()][maxPointsPerWave];//Use weighted points so that we can determine risk of each potential bullet location
			
			for(int i = 0; i < bulletLocs.length; i++)
			{
				Point2D.Double[] locs = (waveSet.get(i).getBulletLocations(currentTime + paths[0][t].getTime()));
				for (int j = 0; j < maxPointsPerWave; j++)
				{
					bulletLocs[i][j] = new WeightedPoint(locs[j]);
				}
			}
			
			//loop to find & set risk of each point in this ring and add it to total for that path(time interval)
			for(int i = 0; i < NUMPATHS; i++)
			{
				TimedWeightedPoint cPoint = paths[i][t];

				for (int waveIndex = 0; waveIndex < waveSet.size(); waveIndex++)
				{
					for (int wavePointIndex = 0; wavePointIndex < maxPointsPerWave; wavePointIndex++)
					{
						//System.out.println("BULLET LOC:\t" + (int)bulletLocs[waveIndex][wavePointIndex].getX() + "\t" + (int)bulletLocs[waveIndex][wavePointIndex].getY());
						//System.out.println("ROBOT LOC:\t" + (int)cPoint.getX() + "\t" + (int)cPoint.getY());
						
						cPoint.addWeight(1/Point2D.distance(cPoint.getX(), cPoint.getY(),
								bulletLocs[waveIndex][wavePointIndex].getX(), bulletLocs[waveIndex][wavePointIndex].getY())*10);//if bulletCircle intersects with myPredictedLoc rectangle, risk = 1
						//System.out.println("BULLET INTERSECTS PROJECTED BOT LOCATION");
						
					}
				}
				//cPoint.setWeight(cPoint.getWeight()+Math.random()*0.1);
				//System.out.println(cPoint.getWeight());
				//add risk of this point to risk of path
				pathRisks[i] += cPoint.getWeight();
			}

		}
		
		//iterate through the path risks array to find the path with least risk. Return that path's index
/*		int safestPathIndex = 0;
		double safestRiskValue = Double.POSITIVE_INFINITY;
		for (int i = 0; i < NUMPATHS; i++)
		{
			if (pathRisks[i] < safestRiskValue)
			{
				safestPathIndex = i;
				safestRiskValue = pathRisks[i];
			}
		}*/
		
		return pathRisks;
	}
	public static double[] calcPathRisksByDist_BOT(TimedWeightedPoint[][] paths, EnemyData eData, long currentTime)
	{
		int NUMPATHS = paths.length;
		int NUMINTERVALS = paths[0].length;
		double eX = eData.getX();
		double eY = eData.getY();
		double[] pathRisks = new double[paths.length];
		for (int t = 0; t < NUMINTERVALS; t++)
		{
			//loop to find & set risk of each point in this ring and add it to total for that path(time interval)
			for(int i = 0; i < NUMPATHS; i++)
			{
				TimedWeightedPoint cPoint = paths[i][t];
				cPoint.addWeight(1/Point2D.distanceSq(cPoint.getX(), cPoint.getY(), eX, eY)*1000);
				pathRisks[i] += cPoint.getWeight();
			}

		}
		return pathRisks;
	}
	public static double[] calcPath_repelCorners(TimedWeightedPoint[][] paths)
	{
		int NUMPATHS = paths.length;
		int NUMINTERVALS = paths[0].length;
		final double[] bounds = Util.getAbsoluteFieldBoundsxXyY();
		final double RISK_MULTIPLIER = 500;
		double[] pathRisks = new double[paths.length];
		for (int t = 0; t < NUMINTERVALS; t++)
		{
			//loop to find & set risk of each point in this ring and add it to total for that path(time interval)
			for(int i = 0; i < NUMPATHS; i++)
			{
				TimedWeightedPoint cPoint = paths[i][t];
				
				cPoint.addWeight(1/Point2D.distanceSq(cPoint.getX(), cPoint.getY(), bounds[0], bounds[2])*RISK_MULTIPLIER);
				cPoint.addWeight(1/Point2D.distanceSq(cPoint.getX(), cPoint.getY(), bounds[0], bounds[3])*RISK_MULTIPLIER);
				cPoint.addWeight(1/Point2D.distanceSq(cPoint.getX(), cPoint.getY(), bounds[1], bounds[2])*RISK_MULTIPLIER);
				cPoint.addWeight(1/Point2D.distanceSq(cPoint.getX(), cPoint.getY(), bounds[1], bounds[3])*RISK_MULTIPLIER);
				pathRisks[i] += cPoint.getWeight();
			}

		}
		return pathRisks;
	}
	public static double[] calcPath_repelWalls(TimedWeightedPoint[][] paths)
	{
		int NUMPATHS = paths.length;
		int NUMINTERVALS = paths[0].length;
		final double[] bounds = Util.getAbsoluteFieldBoundsxXyY();
		final double RISK_MULTIPLIER = 10;
		double[] pathRisks = new double[paths.length];
		for (int t = 0; t < NUMINTERVALS; t++)
		{
			//loop to find & set risk of each point in this ring and add it to total for that path(time interval)
			for(int i = 0; i < NUMPATHS; i++)
			{
				TimedWeightedPoint cPoint = paths[i][t];
				
				cPoint.addWeight(1/Point2D.distance(cPoint.getX(), 0, bounds[0], 0)*RISK_MULTIPLIER);
				cPoint.addWeight(1/Point2D.distance(cPoint.getX(), bounds[2], bounds[1], bounds[2])*RISK_MULTIPLIER);
				cPoint.addWeight(1/Point2D.distance(0, cPoint.getY(), 0, bounds[2])*RISK_MULTIPLIER);
				cPoint.addWeight(1/Point2D.distance(bounds[1], cPoint.getY(), bounds[1], bounds[3])*RISK_MULTIPLIER);
				pathRisks[i] += cPoint.getWeight();
			}

		}
		return pathRisks;
	}
	public static double[] sumDoubleArrays(double[] arr1, double[] arr2)
	{
		if (arr1.length != arr2.length)
		{
			System.out.println("Error in BetaF.Analysis.sumDoubleArrays:  arr1.length != arr2.length");
			return arr1;
		}
		double[] result = new double[arr1.length];
		for (int i = 0; i < arr1.length; i++)
		{
			result[i] = arr1[i] + arr2[i];
		}
		return result;
	}
	public static double[] getPathRisks(TimedWeightedPoint[][] paths)
	{
		double[] result = new double[paths.length];
		for (int i = 0; i < paths.length; i++)
		{
			for (int j = 0; j < paths[i].length; j++)
			{
				result[i] += paths[i][j].getWeight();
			}
		}
		return result;
	}

	public static void calcPath_NearBot(TimedWeightedPoint[][] paths, Enemy cTargetEnemy) 
	{//TODO : Do this properly, instead of guessing at  the math
		int NUMPATHS = paths.length;
		int NUMINTERVALS = paths[0].length;
		final double eX = cTargetEnemy.getX();
		final double eY = cTargetEnemy.getY();
		final double RISK_MULTIPLIER = 0.0001; //.0000001;
		double[] pathRisks = new double[paths.length];
		for (int t = 0; t < NUMINTERVALS; t++)
		{
			for(int i = 0; i < NUMPATHS; i++)
			{
				TimedWeightedPoint cPoint = paths[i][t];
				
				cPoint.addWeight(Point2D.distance(cPoint.getX(), cPoint.getY(), eX, eY)*RISK_MULTIPLIER);
				pathRisks[i] += cPoint.getWeight();
			}

		}
	}
	
	public static Ellipse2D.Double[] enemyDistToClosestCircles(ConcurrentHashMap<String, Enemy> enemies)
    {
    	Ellipse2D.Double[] circles = new Ellipse2D.Double[enemies.size()];
    	int i = 0;
    	for (Map.Entry<String, Enemy> enemy : enemies.entrySet())
    	{
    		double currentX = enemy.getValue().getX(), currentY = enemy.getValue().getY();
    		String currentName = enemy.getKey();
    		double closestDist = Double.POSITIVE_INFINITY;
    		String closestName = null;
    		
    		for (Map.Entry<String, Enemy> other : enemies.entrySet())
    		{
    			double currentDist = Point2D.distance(other.getValue().getX(), other.getValue().getY(), currentX, currentY);
    			if (currentDist < closestDist && currentName != other.getKey())
    			{
    				closestDist = currentDist;
    				closestName = other.getKey();
    			}
    		}
    		
    		double circleX = currentX - closestDist;
    		double circleY = currentY - closestDist;
    		double diameter = closestDist * 2;
    		
    		
    		enemy.getValue().setClosestDist(closestDist);
    		circles[i] = new Ellipse2D.Double(circleX, circleY, diameter, diameter);
    		i+=1;
    	}
		return circles;
	}
}