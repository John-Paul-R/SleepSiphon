package origin;

import java.awt.geom.Point2D;
import java.util.LinkedList;

import util.Vector2D;

public class EDataDispVector
{
	//TimedPoint startPos; //This is timed so that we can scale the vector by bullet travel time (The time that the enemy had to move) (Honestly no sure if this will be helpful tho)
	Vector2D dVector; //Output set
	EnemyStateML enemyState;
	
	public EDataDispVector(EnemyStateML eState, Vector2D vector)
	{
		enemyState = eState;
		dVector = vector;
	}
	
	public EnemyStateML getEnemyState()
	{
		return enemyState;
	}
	public Vector2D getVector()
	{
		return dVector;
	}
	
}
