package origin;

import util.PositionedVector2D;

public class EDataDispVector
{
	//TimedPoint startPos; //This is timed so that we can scale the vector by bullet travel time (The time that the enemy had to move) (Honestly no sure if this will be helpful tho)
	PositionedVector2D dVector; //Output set
	EnemyStateML enemyState;
	
	public EDataDispVector(EnemyStateML eState, PositionedVector2D vector)
	{
		enemyState = eState;
		dVector = vector;
	}
	
	public EnemyStateML getEnemyState()
	{
		return enemyState;
	}
	public PositionedVector2D getVector()
	{
		return dVector;
	}
	
}
