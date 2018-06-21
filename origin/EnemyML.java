package origin;

import java.awt.geom.Point2D;
import java.util.LinkedList;

import util.Vector2D;

public class EnemyML
{
	//I think its more effecient to have the EnemyData object also hold the dVector
	static LinkedList<EDataDispVector> dataSet;//Data set
	private final static int NUM_DATA_POINTS = 500;
	
	public EnemyML()
	{
		if (dataSet == null)
			dataSet = new LinkedList<EDataDispVector>();
	}
	public EnemyML(EDataDispVector e)
	{
		if (dataSet == null)
			dataSet = new LinkedList<EDataDispVector>();
		dataSet.add(e);
	}
	
	private Vector2D[] getNearestNeighbors(int k, EnemyStateML cState)
	{
		EDataDispVector[] kBest = new EDataDispVector[k];
		double[] kBestDistances = new double[k];
		for (int i = 0; i < k; i++)
		{
			kBestDistances[i] = Double.POSITIVE_INFINITY;
		}
		
		for (EDataDispVector cData : dataSet)
		{
			double cDistance = cData.getEnemyState().getWeightedDataDistanceSq(cState);
			//double inverseSqrtDist = 1D/Math.sqrt(Math.sqrt(cDistance));
			//cData.getVector().scaleMagnitude(inverseSqrtDist);
			int i;
			for (i = 0; i < k; i++)
			{
				if (cDistance >= kBestDistances[i])
					//Possibly do this a different way: Add ALL of the vectors based on a scale factor that is (to some degree) inverse of the distance between the data points
				{
					i = -1;
					break;
				}
			}
			if (i >= 0)
			{
				kBest[i-1] = cData;//subtract 1 bc the loop increases the value of i one additional time when it closes
				kBestDistances[i-1] = cDistance;
			}
		}
		
		
		Vector2D[] output = new Vector2D[k];
		for (int i = 0; i < k; i++)
		{
			output[i] = kBest[i].getVector();
		}
		return output;
	}
	
	public Point2D.Double getPredictedCoordinate(Point2D.Double firingPosition, Enemy enemy)
	{
		EnemyStateML cState = new EnemyStateML(
				enemy.getDistance(),
				enemy.getHeading(),
				enemy.getVelocity(), 
				enemy.getLatestTurnRate(),
				enemy.getTimeSinceDecel());
		//double angle = 0;
		int NUM_NEIGHBORS = 1; //idk how to do bucketing.  Probably just going to use the "weight by distance" method.  For now, just using nearest.
		Vector2D[] nearestNeighbors = getNearestNeighbors(NUM_NEIGHBORS, cState);
		Point2D.Double predictedRelPos = nearestNeighbors[0].project();
		Point2D.Double predictedAbsPos = new Point2D.Double(predictedRelPos.getX() + enemy.getX(), predictedRelPos.getY() + enemy.getY());
		return predictedAbsPos;
	}
	
	public void addData(EDataDispVector e)
	{
		if (dataSet.size() > NUM_DATA_POINTS)
		{
			dataSet.remove(0);
		}
		dataSet.add(e);
	}
}
