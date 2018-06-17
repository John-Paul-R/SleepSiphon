package gun;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

import BetaF.Aim;
import BetaF.Enemy;
import BetaF.SleepSiphon;
import util.TimedPoint;

public class VirtualPointGuns
{
	//Numeric ID for each gun ([0 : Head-On] [1 : Linear Prediction] [2 : Turn Predict] [3 : AvgVel&Heading])
	//private static final int HEAD_ON = 0, LINEAR = 1, TURN_BASIC = 2, AVG_VH = 3;
	private static final int numGuns = 4;
	private ConcurrentHashMap<String, Enemy> enemyMap;
	private SleepSiphon self;
	
	public VirtualPointGuns(SleepSiphon s, ConcurrentHashMap<String, Enemy> e)
	{
		self = s;
		enemyMap = e;
	}
	
	public void updateEnemy(Enemy target)
	{
		long currentTime = self.getTime();
		boolean enemyDataExistsNOW = true;
		int[] bulletSuccess = new int[numGuns];
		LinkedList<VirtualBullet> vBullet = target.getVirtualPointBulletsSet();
		for (int i = 0; i < vBullet.size(); i++)
		{
			VirtualBullet cBullet = vBullet.get(i);
			if (cBullet.getTime() == target.getTime())//if it is the time that the bullet was predicted to impact
			{
				if (cBullet.checkForHit(target))//test if bullet intersects with enemy actual postion
				{
					bulletSuccess[cBullet.getType()] = 1;

				}
				else
				{
					bulletSuccess[cBullet.getType()] = 0;
				}
				
				vBullet.remove(cBullet);//Remove bullet
				i-=1;
			}
			else 
			{
				enemyDataExistsNOW = false;
			}
/*			if(cBullet.getTime() < currentTime)
			{
				vBullet.remove(cBullet);//Remove bullet
				i-=1;
			}*/
		}
		System.out.println(enemyDataExistsNOW);
		if (enemyDataExistsNOW)
		{
			//target.updateGunStats(bulletSuccess);
			System.out.println(bulletSuccess);
		}
	}
	
	public void updateAll()
	{
		
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
		LinkedList<VirtualBullet> vBullet = target.getVirtualPointBulletsSet();
		for (int i = 0; i < numGuns; i++)
		{
			vBullet.add(virtualGunOutput(target, i));
		}
	}
	private VirtualBullet virtualGunOutput(Enemy target, int gunID)
	{
		double BULLET_POWER = Aim.firePowerByDist(self, target);
		TimedPoint selfTimedPos = new TimedPoint(self.getX(), self.getY(), self.getTime());
		VirtualBullet output = null;
		switch (gunID)
		{
		case 0 : output = vGunPoint.headOn(selfTimedPos, target, BULLET_POWER);
		break;
		case 1 : output = vGunPoint.linearPredictionFire(selfTimedPos, target, BULLET_POWER);
		break;
		case 2 : output = vGunPoint.constantTurnPredict(selfTimedPos, target, BULLET_POWER);
		break;
		case 3 : output = vGunPoint.averageMovementGun(selfTimedPos, target, BULLET_POWER);
		break;
		}
		return output;
	}

}
