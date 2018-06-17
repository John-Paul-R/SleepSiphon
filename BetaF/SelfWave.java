package BetaF;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import util.Vector2D;

public class SelfWave extends Wave
{
	private long aliveTime;
	private static long maxAliveTime;
	private ConcurrentHashMap<String, Enemy> enemies;
	private HashMap<String, EnemyML> enemiesml;
	
	private HashMap<String, EnemyStateML> eStartStates;
	private HashMap<String, Point2D.Double> eStartLocs;
	
	private boolean remove;
	
	public SelfWave(double centerX, double centerY, long startTime, double bulletPower, ConcurrentHashMap<String, Enemy> enemies, HashMap<String, EnemyML> enemiesml)
	{
		super(centerX, centerY, startTime, bulletPower);
		remove = false;
		this.enemies = enemies;
		this.enemiesml = enemiesml;
		eStartStates = new HashMap<String, EnemyStateML>();
		eStartLocs = new HashMap<String, Point2D.Double>();	
		for (Entry<String, Enemy> e : enemies.entrySet())
		{
			String cName = e.getKey();
			Enemy cEnemy = e.getValue();
			
			EnemyStateML nState = new EnemyStateML(
					cEnemy.getDistance(),
					cEnemy.getHeading(),
					cEnemy.getVelocity(), 
					cEnemy.getLatestTurnRate(),
					cEnemy.getTimeSinceDecel());
			eStartStates.put(cName, nState);
			eStartLocs.put(cName, cEnemy.getLocation());
		}
	}
	
	public void update(long currentTime)
	{
		if (aliveTime > maxAliveTime || eStartStates.size() == 0)
			remove = true;
		double cRad = this.getRad(currentTime);
		//Check if bullet has crashed on enemy in last turn.  If it has, generate disp vector and put together in a EDataDispVector object.
		Iterator<String> iterator = eStartStates.keySet().iterator();
		while ( iterator.hasNext()) //Cycles through the enemies still in "eStartStates" -- these are the enemies who have not yet been hit by the wave
		{
			String uncrashedEnemy = iterator.next();
			if (!enemies.containsKey(uncrashedEnemy))
			{
				iterator.remove();
			}
			else
			{
				
				Enemy cEnemy = enemies.get(uncrashedEnemy);
				double eX = cEnemy.getX();
				double eY = cEnemy.getY();
				
				double bearing = Math.atan2(eX - centerX, eY - centerY);
				
				Point2D.Double waveClosestPos = new Point2D.Double(centerX + cRad * Math.sin(bearing), centerY + cRad * Math.cos(bearing));
	
				
				//System.out.println(this.toString() + "Testing if wave crashed on enemy: " + uncrashedEnemy);
				//OLD: if enemy is within wave circle//eX < waveMaxX && eX > waveMinX && eY < waveMaxY && eY > waveMinY
				
				//NEW: if closest point on wave circle is w/in certain dist to enemy
				if (waveClosestPos.distance(cEnemy.getLocation()) < 20D)
				{
					//System.out.println(this.toString());
					//System.out.println("updating enemy ml data for " + uncrashedEnemy);
					Vector2D dVector = new Vector2D(eStartLocs.get(uncrashedEnemy), cEnemy.getLocation());
					enemiesml.get(uncrashedEnemy).addData(new EDataDispVector(eStartStates.get(uncrashedEnemy), dVector));
					iterator.remove();
				}
				
			}
			
		}
	}

	public boolean getRemove() 
	{
		return remove;
	}
}
