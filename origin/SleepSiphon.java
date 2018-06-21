package origin;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
// API help : http://robocode.sourceforge.net/docs/robocode/robocode/Robot.html
import java.util.concurrent.ConcurrentHashMap;

import gun.VirtualPointGuns;
import robocode.*;
import util.TimedWeightedPoint;
import gun.*;
import java.awt.event.KeyEvent;

/*
SleepSiphon - © 2018 John Paul Rutigliano
==============================================================================
This software is provided 'as-is', without any express or implied
warranty. In no event will the authors be held liable for any damages
arising from the use of this software.

Permission is granted to anyone to use this software for any purpose,
including commercial applications and to alter it and redistribute it
freely, subject to the following restrictions:

  1. The origin of this software must not be misrepresented; you must not
  claim that you wrote the original software. Anything you publish containing
  all or parts of this software must credit author of the original software
  (John Paul Rutigliano).

  2. Altered source versions must be plainly marked as such, and must not be
  misrepresented as being the original software.

  3. This notice may not be removed or altered from any source
  distribution.

  4. This software, and robots derived from this software may not be
  used in any robocode competition without express permission from the author.
==============================================================================
 */

//TODO Bullet power should increase with confidence that it will hit
public class SleepSiphon extends AdvancedRobot
{
	private int totalNumEnemies;
    private int knownEnemies = 0;
    private ConcurrentHashMap<String, Enemy> currentEnemies;
    private ArrayList<SelfData> selfDataHistory;
    
    private int radarMode = 3;
    private int targetMode = 0; //ADD THIS IN FUTURE for different target selection algorithms
    private int moveMode = 9000;
    //private int aimMode = 5;  //REPLACED BY VIRTUAL GUNS (PER ENEMY)
    //private int fireMode = 0; //Not yet implemented
    private String cTargetName = "";
    private Enemy cTargetEnemy;
    private int[] destinationXY = null;
    //global constants
    private boolean is1v1 = false;
    
    private Point2D.Double predictedEnemyPoint = null;
    private double[] predictedEnemyPointAtTime = new double[] {0,0,-1};
    private double[] circleCenter = new double[2];
    private WeightedPoint[] destinationPoints = null;
    
    private int NUM_GUNS = 4;
    //private VirtualPointGuns vPointGuns;
    private VirtualAngleGuns vAngleGuns;
	private File gStats;
	RobocodeFileWriter gStatsWriter;
	private boolean fileOutput = false;
	
	private LinkedList<EnemyWave> eWaves = new LinkedList<EnemyWave>();
	private int pointsPerWave = 3; //TODO : Rework this
	
	//Min Risk Paths
	private Point2D.Double[] destinations = null;
	private TimedWeightedPoint[][] paths = null;
	private Point2D bestDestinationPoint = null;
	private double[] pathRisks = null;
	
	private static boolean gunToggle = true;
	
    Ellipse2D.Double[] closestDistCircles;
    
    private LinkedList<Bullet> myBullets;
    private Gun gunActor;
    private LinkedList<SelfWave> selfWaves;
    private static HashMap<String, EnemyML> enemiesml;
    
    //DEBUG
	private File debugFile;
    private RobocodeFileOutputStream debugWriter;
    private static HashMap<String, int[][]> bulletHitStats = new HashMap<String, int[][]>();;//Dim 1 is each gun type. Dim 2 index 0 is number of hits for that gun type, Dim 2 index 1 is number fired for that gun type.
    private int moveWait = 0;

	public void run() 
	{
		totalNumEnemies = getOthers();
		setAdjustRadarForGunTurn(true);
		setAdjustRadarForRobotTurn(true);
		setAdjustGunForRobotTurn(true);
		setColors(Color.black, Color.gray, Color.white, Color.orange, Color.yellow);
        currentEnemies = new ConcurrentHashMap<String, Enemy>(getOthers()); 
        //Store only the most recent 500 ticks of data for our robot
        selfDataHistory = new ArrayList<SelfData>(1000);
        enemiesml = new HashMap<String, EnemyML>(getOthers());
    	
       // setTurnRightRadians(robocode.util.Utils.normalRelativeAngle(Math.PI/2-getHeadingRadians()));
        //setAhead(1000);
        //setTurnRight(Double.POSITIVE_INFINITY);
		setTurnRadarRight(Double.POSITIVE_INFINITY);
		//setTurnGunRight(Double.POSITIVE_INFINITY);

		predictedEnemyPointAtTime[2] = -1;
		final double FIELD_BUFFER = 12;
		Util.setFieldBoundsArray(this, FIELD_BUFFER);
		//vPointGuns = new VirtualPointGuns(this, currentEnemies);
		vAngleGuns = new VirtualAngleGuns(this, currentEnemies);
		gStats = getDataFile("SleepSiphon_GunStats.csv");
		debugFile = getDataFile("debugOutput.log");
		try {
			debugWriter = new RobocodeFileOutputStream(debugFile);
			debugWriter.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("ERROR > Could not load the PrintWriter 'debugWriter' in the SleepSiphon class.");
			System.out.println("Local Exception Message:  " + e.getLocalizedMessage());
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
/*		try {
			gStatsWriter = new RobocodeFileWriter(gStats);
			gStatsWriter.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
		
		myBullets = new LinkedList<Bullet>();
		selfWaves = new LinkedList<SelfWave>();
		gunActor = new Gun(this, myBullets, selfWaves, currentEnemies, enemiesml);
		double prevHeading = getHeading();
		//TODO When 2 concecutive bullets hit, detect if enemy is shooting at you head on, then adjust direction accordingly (this is to be added to movement systems like four-corners)
	    //TODO also, possibly target people who are shooting at you?
		while(true) {
			//TODO mimimum risk movement 
            //setGunColor(Graphics.cycleRandomColors()); 
			scanOrder = 0;//reset scanOrder for next radar pass
            long currentTime = getTime();
		//COSMETICS
			setGunColor(Graphics.spectrumCycleHSB());
            setRadarColor(Graphics.gunFire(getGunHeat()));
        //UPDATE DATA & DEPENDENCIES
            closestDistCircles = Analysis.enemyDistToClosestCircles(currentEnemies);
    		selfDataHistory.add(
    				new SelfData(
    						getGunHeadingRadians(),
    						getHeadingRadians(),
    						getVelocity(),
    						getX(),
    						getY(),
    						getTime(),
    						prevHeading));
    		prevHeading = getHeadingRadians();
    		if (getOthers() == 1)
    			is1v1 = true;
    		//Update SelfWaves
    		if (getOthers() > 0)
	    		for (int i = 0; i < selfWaves.size(); i++)
	    		{
	    			SelfWave sWave = selfWaves.get(i);
	    			if (sWave.getRemove() == true)
	    			{
	    				selfWaves.remove(i);
	    				i -=1;
	    			}
	    			else
	    			{
	    				sWave.update(currentTime);//This also updates the EnemyML objects (based on their movement relative to my waves (disp vectors))
	    			}
	    		}
    		
    		
            if (cTargetEnemy != null && gunToggle)
            {
            	/*double BULLET_POWER = Aim.firePowerByDist(this, cTargetEnemy);
            	if (aimMode == 0)
            		Aim.basic(this, cTargetEnemy);
            	else if (aimMode == 1)
            	{
            		predictedEnemyPointAtTime = Aim.linearPredictionFire(this, cTargetEnemy, BULLET_POWER);
            	}
            	else if (aimMode == 2)
            	{
            		predictedEnemyPoint = Aim.constantTurnPredict(this, cTargetEnemy, BULLET_POWER);

            	}
            	else if (aimMode == 100)
            	{
            		if (Util.withinTolerance(0.0, Analysis.predictConstantTurn(this, cTargetEnemy), 0.01))
            		{
            			//System.out.println(cTarget + " is LINEAR");
            			predictedEnemyPointAtTime = Aim.linearPredictionFire(this, cTargetEnemy, BULLET_POWER);
            		}
            		else
            		{
            			//System.out.println(cTarget + " is CIRCLE");
            			predictedEnemyPointAtTime[2] = -1;
            			predictedEnemyPoint = Aim.predictCircle(this, cTargetEnemy);
            		}
            	}
            	else if (aimMode == 5)
            	{
            		predictedEnemyPoint = Aim.averageMovementGun(this, cTargetEnemy, BULLET_POWER);
            	}
            	
                //if gun has finished aiming, fire projectile and update current target
                if (getGunTurnRemaining() <= (1.0/8.0)*Math.PI)
                {
                	if (fireMode == 0 && cTargetEnemy != null)
                		setFireBullet(BULLET_POWER);

                }*/
            	//TODO REIMPLEMENT THIS 
            	//vPointGuns.virtualFireAll();//TODO Use the most recent predicted point from the vgun to fire bullet, that way there is no unnecessary recalculation?
            	//vPointGuns.updateAll();
            	vAngleGuns.virtualFireAll();
            	vAngleGuns.updateAll();
            	double BULLET_POWER = Aim.firePowerByDist(this, cTargetEnemy);
            	if (getEnergy() < 0.2) 
            	{
            		BULLET_POWER = 0;
            	}
            	else if (getEnergy() <= 5)
            	{
            		BULLET_POWER = .1;
            	}
            	else if (getEnergy() <= 15)
            	{
            		BULLET_POWER *= .2;
            	}
            	else if (getEnergy() <= 20)
            	{
            		BULLET_POWER *= .3;
            	}
            	else if (getEnergy() <= 25)
            	{
            		BULLET_POWER *= .6;
            	}
            	else if (getEnergy() >= 80)
            	{
            		BULLET_POWER *= 2;
            	}
            	//System.out.println(cTargetName + "\t\t" + cTargetEnemy.getBestGunHitRate());
            	boolean disabledEnemy = false;
            	for (Enemy cEnemy : currentEnemies.values())
            	{
	            	if (cEnemy.getEnergy() == 0)
	            	{
	            			gunActor.basic(cEnemy, 0.1);
	            			disabledEnemy = true;
	            			//System.out.println("NOT Firing Based on ML Data");
	            	}
            	}
            	if (!disabledEnemy && BULLET_POWER > 0)
            	{
            		if (currentTime > 70 || this.getNumRounds() > 1 && cTargetEnemy.getBestGunHitRate() >= 0.15)
	            	{
            			if (getGunHeat() == 0)
            			{
	                	System.out.println(String.format("[%d] %s", currentTime, ("Firing at " + cTargetName + " With vGuns")));
            			}
	            		predictedEnemyPointAtTime = null;
	                	switch (cTargetEnemy.getBestGun())
	                	{
	                	case 0 : gunActor.basic(cTargetEnemy, BULLET_POWER);
	                	break;
	                	case 1 : predictedEnemyPointAtTime = gunActor.linearPredictionFire(cTargetEnemy, BULLET_POWER); predictedEnemyPoint= null;
	                	break;
	                	case 2 : predictedEnemyPoint = gunActor.constantTurnPredict(cTargetEnemy, BULLET_POWER);                	
	                	break;
	                	case 3 : predictedEnemyPoint = gunActor.averageMovementGun(cTargetEnemy, BULLET_POWER);
	                	break;
	                	}
	            	} 
	        		else if (enemiesml.get(cTargetName) != null && enemiesml.get(cTargetName).dataSet.size() > 5)
	            	{
	            		predictedEnemyPoint = Util.limitCoordinateToMap(enemiesml.get(cTargetName).getPredictedCoordinate(new Point2D.Double(getX(), getY()), cTargetEnemy));
	            		gunActor.aimToCoordinate(predictedEnemyPoint);
	            		if (getGunHeat() == 0)
	            		{
		            		gunActor.setFireBullet(BULLET_POWER);
		            		System.out.println(String.format("[%d] %s", currentTime, (" \t\t\tFiring at " + cTargetName +" Based on ML Data")));
	            		}
	            	}
	            	else
	            	{
	            		predictedEnemyPoint = gunActor.constantTurnPredict(cTargetEnemy, BULLET_POWER);
	            		System.out.print(String.format("[%d] No ML or vGun ||", currentTime));
	            	}
            	}
            }	
            
            //else
            	//System.out.println("cTargetEnemy is null!");
            

            if (getGunHeat() > 0.5)
            {
       			//Set current target
            	if (targetMode == 0)
            		cTargetName = Target.closest(currentEnemies);
            	else if (targetMode == 1)
            		System.out.println("THE IMPOSSIBLE HAS HAPPENED");
            	cTargetEnemy = currentEnemies.get(cTargetName);
            }
            
            
            if ((is1v1 || moveMode==9000) && moveMode >= 0)
            {		
            	for (Entry<String, Enemy> entry : currentEnemies.entrySet())
            	{
	            	String cName = entry.getKey(); //This will be changed for melee
	            	Enemy cEnemy = entry.getValue();
	            	boolean crashedEnemy = false;
	            	boolean crashedWall = false;
	            	boolean hitByBullet = false;
	            	if (cEnemy.getClosestDist() < 50)
	            		System.out.println(cName + " " + cEnemy.getClosestDist());
	            	if (cEnemy.getClosestDist() < 50)
	            		crashedEnemy = true;
	            	if (cEnemy.getDistFromWall() < 20 && cEnemy.getVelocity() == 0)
	            		crashedWall = true;
	            	
	            	if (cEnemy != null && cEnemy.numEntries() >= 3 && !crashedEnemy && !crashedWall && !hitByBullet && !cEnemy.getUpdatedWaves())
	            	{//I might have to turn this all back a turn to wait for event data
	            		ArrayList<EnemyData> eData = cEnemy.getDataSet();
	            		int eDataSize = eData.size();
		            	double eDmgTaken = cEnemy.getEnergyLostThisTurn();//This is actually Last Turn
		            	double eEnergyGained = cEnemy.getEnergyGainedThisTurn();
		            	//double eEnergyDropBetweenTurns = Math.abs(eData.get(eDataSize-1).getEnergy()-eData.get(eDataSize-2).getEnergy());
		            	double eEnergyDiffBetweenTurns = cEnemy.getEnergy()-cEnemy.getPreviousData().getEnergy();
		            	double eBulletPower = Math.abs(eEnergyDiffBetweenTurns-eDmgTaken+eEnergyGained);
		            	//if (eEnergyDiffBetweenTurns < -eDmgTaken + eEnergyGained) //Add additional condition to subtract the damage enemy took from ramming into wall (as per wall hit detection function I'll write later)
		            	if (eEnergyDiffBetweenTurns != 0.0)
		            	{
			            	//System.out.println("Energy Difference: " + eEnergyDiffBetweenTurns);
			            	//System.out.println("Bullet Power: " + eBulletPower);
		            	}
		            	if (eEnergyDiffBetweenTurns < 0 && eEnergyDiffBetweenTurns > -3.1)
		            		eWaves.add(
		            				new EnemyWave(
		            						cEnemy.getPreviousData().getX(),
				            				cEnemy.getPreviousData().getY(),
				            				getTime()-1,
				            				Math.abs(eBulletPower),
				            				selfDataHistory.get((int)selfDataHistory.size()-2),
				            				cName)
		            					);
		            	//System.out.println("DATA AGE\t"+selfDataHistory.get((int)getTime()).getAge(getTime()));
		            	//System.out.println("CURRENT TIME\t"+getTime());
		            	//System.out.println(eEnergyDiffBetweenTurns +"\t" + eDmgTaken+"\t" +eEnergyGained);
		            	
		            	cEnemy.setEnergyLost(0);
		            	cEnemy.setEnergyGained(0);
		            	cEnemy.setUpdatedWaves(true);

	            	}
	            	EnemyWave closestWave = null;
	            	double closestDist = Double.POSITIVE_INFINITY;
	            	for (int i = 0; i < eWaves.size(); i++)//increments a counter when waves are close, then removes waves once counter fills to a predefined amount in the EnemyWave class
	            	{
	            		EnemyWave cWave = eWaves.get(i);
	            		double cDist = cWave.getDist(getX(), getY(), getTime());
	            		if (cDist < closestDist)
	            		{
	            			closestDist = cDist;
	            			closestWave = cWave;
	            		}
	            		if (cDist < 40)
	            			cWave.setClose();

	            		if (cWave.getClose() <= 0)
	            		{
	            			eWaves.remove(i);
	            			i--;
	            		}
	            			//System.out.println("WAVE IS NEAR");
	            	}

            	}
        		if ((is1v1 || moveMode == 9000) && cTargetEnemy != null)
        		{            	
        			double minDistance = 20;
        			destinations = MoveActor.generatePointsCircular(new Point2D.Double(getX(), getY()), 16, 150);
        			//destinations = Move.removeCloseCoordinates(destinations, minDistance);
        	    	paths = MoveActor.generatePaths(this, destinations);
        	    	
        	    	double[] pathRisks_wave = Analysis.calcPathRisksByDist_WAVE(paths, eWaves, pointsPerWave, getTime());
        	    	double[] pathRisks_bot = Analysis.calcPathRisksByDist_BOT(paths, cTargetEnemy.getLatest(), getTime());
        	    	Analysis.calcPath_repelWalls(paths);
        	    	//Analysis.calcPath_NearBot(paths, cTargetEnemy);
        	    	double[] pathRepelCorners = Analysis.calcPath_repelCorners(paths);
        	    	pathRisks = Analysis.getPathRisks(paths);
        	    	//pathRisks = Analysis.sumDoubleArrays(pathRisks_wave, pathRisks_bot);//this updates the risk ("Weight") of each point as well
        	    	//pathRisks = Analysis.sumDoubleArrays(pathRisks, pathRepelCorners);
        	    	bestDestinationPoint = destinations[Analysis.getSafestPathIndex(pathRisks)];
        	    	MoveActor.aGoTo(this, bestDestinationPoint.getX(), bestDestinationPoint.getY());
        		}
        		{/*
        			Vector riskVector = closestWave.getRiskVector(new TimedPoint(getX(), getY(), getTime()));
        			
        			double direction = Math.signum(robocode.util.Utils.normalRelativeAngle(riskVector.getDirection()));
        			//need to get the lateral components of this vector (relative to my bot) (Perpendicular to the trajectory of the bullet)
        			//Also need to make sure my angle is perpendicular to it
        			Point2D.Double resultPoint = Util.projectVector(
        					new Point2D.Double(getX(), getY()),
        					Math.atan2(cTargetEnemy.getX()-getX(), cTargetEnemy.getY()-getY())+Math.PI/2*direction,
        					riskVector.getMagnitude());
        			resultPoint = Util.limitCoordinateToMap(resultPoint);
        			Move.aGoTo(this, resultPoint.getX(), resultPoint.getY());
        			setTurnRightRadians(robocode.util.Utils.normalRelativeAngle(riskVector.getDirection()-getHeadingRadians()));
        			setAhead(riskVector.getMagnitude());*/
        		}
            	
      /*      	if (getDistanceRemaining() == 0)
            	{
	            	setTurnRightRadians(Math.PI/2);
	            	setAhead(1000);
            	}*/
            	
            }
            //else
            if (moveWait == 0)
            {
	            switch (moveMode)
	            {
		            case 0 :
		            	MoveActor.random(this);
		            	break;
		            case 1 : 
		            	setMaxVelocity(5);
		        		MoveActor.spin(this);
		        		break;
		            case 2 :
		            	
		            	break;
		            case 3 :
		            	if (getOthers() > 1)
		            		destinationPoints = MoveActor.fourCornersLoose(this);
		            	else 
		            	{
		            		moveMode = 4;
		            		destinationXY = null;
		            	}
		            	break;
		            case 4 : 
		            	if (cTargetEnemy != null)
		            		destinationPoints = MoveActor.circularPointDistribution(this, cTargetEnemy);
		            	break;
		            case 5 : //CURRENT
		            	if (getOthers() > 4)
		            	{
		            		final double HYPOT = 150;
		            		destinationPoints = MoveActor.circleDistribution_NeverClosest2(this, closestDistCircles, currentEnemies, HYPOT);
		            	}
		            	else 
		            	{
		            		moveMode = 9000;
		            		//is1v1 = true;
		            		destinationPoints = null;
		            		destinationXY = null;
		            	}
	            	break;
	            }
            }
	        else
	        {
	        	moveWait -= 1;
	        }
            //targeting
            if (cTargetName != null)
            {
	            switch (radarMode)
	            {
	            case 0 : Radar.spinToCenter(this);
	            	break;
	            case 1 : Radar.infinityLock(cTargetEnemy, this);
	            	break;
	            case 2 : Radar.factorLock(cTargetEnemy, this);
	            	break;
	            }
            }	
            
            if (radarMode == 3)
            {
            	if (currentEnemies.size() < getOthers() || cTargetEnemy == null)
            	{
            		Radar.spinToCenter(this);
            		System.out.print(String.format("[%d] URS ||", currentTime));

            	}
            	else if (getOthers() > 1)
            	{
            		Radar.oldestScanned(currentEnemies, this);	
            		//System.out.println(Util.getOldestName(currentEnemies, this));
            	}
            	else
            	{
                    if (cTargetEnemy != null)
                    {
	            		if (cTargetEnemy.timeSinceUpdate((int)getTime()) > 8)
	            			Radar.spinToCenter(this);
	            		else
	            			Radar.factorLock(cTargetEnemy, this);
                    }

            	}
            }
            else if (radarMode == 0)
            {
            	Radar.spinToCenter(this);
            }
            
            
            execute();
		}
	}
	
	public Point2D.Double getPosition()
	{
		return new Point2D.Double(this.getX(), this.getY());
	}
	
	public void onHitRobot(HitRobotEvent e)
	{
		MoveActor.circularAwayPointDistribution(this, cTargetEnemy, 150);
		moveWait = 16;
	}
	private int scanOrder = 0;
	private int numAdded = 0;
	public void onScannedRobot(ScannedRobotEvent e) {
		
		scanOrder+=1;
		if (knownEnemies < getOthers())
		{
			if (!(currentEnemies.containsKey(e.getName())))
			{
				if (!bulletHitStats.containsKey(e.getName()))
				{
					bulletHitStats.put(e.getName(), new int[NUM_GUNS][2]);
				}
				currentEnemies.put(
						e.getName(), 
						new Enemy(
							new EnemyData(
								e.getEnergy(),
								e.getBearingRadians(),
								e.getDistance(),
								e.getHeadingRadians(),
								e.getVelocity(),
								(int)getTime(),
								getX(),
								getY(),
								getHeadingRadians(),
								0.0,
								0.0, 
								0,
								scanOrder)
							,bulletHitStats.get(e.getName()),
							numAdded++));
				knownEnemies++;
				if (!(enemiesml.containsKey(e.getName())))
				{
					enemiesml.put(e.getName(), new EnemyML());
				}
				//System.out.println("creating new enemy object");
			}
			else
			{
				EnemyData cEnemyLastData = currentEnemies.get(e.getName()).getLatest();
				currentEnemies.get(e.getName()).addEnemyData(new EnemyData(
						e.getEnergy(),
						e.getBearingRadians(),
						e.getDistance(),
						e.getHeadingRadians(),
						e.getVelocity(),
						(int)getTime(),
						getX(),
						getY(),
						getHeadingRadians(),
						currentEnemies.get(e.getName()).getLatestTurnRate(),
						cEnemyLastData.getVelocity(),
						cEnemyLastData.getTimeSinceDecel(), 
						scanOrder));
				currentEnemies.get(e.getName()).setUpdatedWaves(false);
			
			//ML ENEMY OBJECTS
				

			}
			
		}
		else
		{
			EnemyData cEnemyLastData = currentEnemies.get(e.getName()).getLatest();

			currentEnemies.get(e.getName()).addEnemyData(new EnemyData(
					e.getEnergy(),
					e.getBearingRadians(),
					e.getDistance(),
					e.getHeadingRadians(),
					e.getVelocity(),
					(int)getTime(),
					getX(),
					getY(),
					getHeadingRadians(),
					currentEnemies.get(e.getName()).getLatestTurnRate(),
					cEnemyLastData.getVelocity(),
					cEnemyLastData.getTimeSinceDecel(),
					scanOrder));
			currentEnemies.get(e.getName()).setUpdatedWaves(false);

		}
		
		

		//Waves
		/*if (e.getEnergy() )
		currentEnemies.get(e.getName()).addWave()*/
	}
	
	public void onHitByBullet(HitByBulletEvent e) {
		// Replace the next line with any behavior you would like
    	//Move.random(this);
		if (currentEnemies.containsKey(e.getName()))
		{
			Enemy source = currentEnemies.get(e.getName());
			source.setEnergyGained(e.getPower()*3);
		}

	}
	
	public void onBulletHit(BulletHitEvent e)
	{
		if (currentEnemies.containsKey(e.getName()))
			currentEnemies.get(e.getName()).setEnergyLost(
					Rules.getBulletDamage(e.getBullet().getPower()));
	}
	
	public void onHitWall(HitWallEvent e)
	{
		System.out.println("I HIT A WALL!");
	}	
	
	
    public void onRobotDeath(RobotDeathEvent e)
    {
		
		if (fileOutput)
		{
			try {
				gStatsWriter = new RobocodeFileWriter(gStats.getAbsolutePath(), true);
	
				//gStatsWriter.append(e.getName() + "," + currentEnemies.get(e.getName()).getGunStatsString() + "\n");
	
			gStatsWriter.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
    	currentEnemies.remove(e.getName());
    	knownEnemies--;
    	cTargetName = Target.closest(currentEnemies);
    	cTargetEnemy = currentEnemies.get(cTargetName);
    }

    //SAVE ROUNDS STATS
	public void onRoundEnded(RoundEndedEvent e)
	{
		if (fileOutput)
		{
			System.out.println("Data Directory:\t" + gStats.getAbsolutePath());
			
			try {
				gStatsWriter = new RobocodeFileWriter(gStats.getAbsolutePath(), true);
	
			
			for (Entry<String, Enemy> enemy : currentEnemies.entrySet())
			{
				//gStatsWriter.append(enemy.getKey() + "," + enemy.getValue().getGunStatsString() + "\n");
			}
			gStatsWriter.append("\n");
			gStatsWriter.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		else
			System.out.println("File writing is disabled");  //NOTE: If you die before the round ends, this doesn't trigger. (Unless you are the last one to die (I think))
	}
    
    //TOGGLES
    private static boolean showDistanceCircles = false;
    private static boolean showEnemyWaves = false;
    private static boolean showSelfWaves = false;
    private static boolean showMovement = false;
    private static int statsMode = 0;
    private static final int NUMDISPLAYS = 4;
    private static final String disableGunPass = "johnpaul";
    private static String latestEightKeys = "12345678";
    private static boolean vBulletDisplay = false;
    public void onKeyPressed(KeyEvent e)
    {
    	
    	
    	latestEightKeys = latestEightKeys.substring(1) + e.getKeyChar();
    	if (e.getKeyChar() == 'd')
    	{
    		showDistanceCircles = !showDistanceCircles;
    	}
    	else if (e.getKeyChar() == 'w')
    	{
    		showEnemyWaves = !showEnemyWaves;
    	}
    	else if (e.getKeyChar() == 'm')
    	{
    		showMovement = !showMovement;
    	}
    	else if (e.getKeyChar() == 's')
    	{
    		showSelfWaves = !showSelfWaves;
    	}
    	else if (e.getKeyChar() == 'v')
    	{
    		vBulletDisplay = !vBulletDisplay;
    	}
    	else if (e.getKeyChar() == 'x')
    	{
    		if (statsMode < 2)
    			statsMode += 1;
    		else 
    			statsMode = 0;
    	}
    	if (disableGunPass.equals(latestEightKeys))
    	{
    		gunToggle = !gunToggle;
    	}
    }

    public void onPaint(Graphics2D g) 
    {
		long cTime = getTime();

    	g.setColor(new Color(255, 00, 255, 255));
    	g.drawOval((int)getX()-18, (int)getY()-18, 36, 36);
    	g.drawOval((int)getX()-36, (int)getY()-36, 72, 72);
    	g.drawOval((int)getX()-32, (int)getY()-32, 64, 64);

    	if (showDistanceCircles)
    	{
	    	g.setColor(new Color(0xff, 0xff, 0x00, 255));
	    	for (int i = 0; i < closestDistCircles.length; i++)
	    	{
	    		Ellipse2D.Double current = closestDistCircles[i];
	    	g.drawOval((int)current.getX(), (int)current.getY(), (int)current.getWidth(), (int)current.getHeight());
	    	}
    	}
    	if (showSelfWaves)
    	{
    		g.setColor(new Color(0xff, 0xff, 0xff, 150));
    		for (SelfWave wave : selfWaves)
	    	{
	    		Ellipse2D.Double circle = wave.getCircle(cTime);
		    	g.drawOval((int)circle.getX(), (int)circle.getY(), (int)circle.getWidth(), (int)circle.getHeight());
	    	}
    	}
    	if (showEnemyWaves)
    	{
	    	for (EnemyWave wave : eWaves)
	    	{
	    		Enemy waveSource = currentEnemies.get(wave.getSource());
	    		if (waveSource != null)
	    		{
		    		float hueValue = (float) ((double)(waveSource.getNumAddedID())/(double)totalNumEnemies);
		    		Color rgb = new Color(Color.HSBtoRGB(hueValue, 1F, 1F));
					Color rgba = new Color(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), 100);
					g.setColor(rgba);
		    		
		    		Ellipse2D.Double circle = wave.getCircle(cTime);
			    	g.drawOval((int)circle.getX(), (int)circle.getY(), (int)circle.getWidth(), (int)circle.getHeight());
			    	
			    	//g.drawString(wave.getSource(), (int)circle.getX(), (int)circle.getY());
			    	Point2D.Double[] bulletLocs = wave.getBulletLocations(cTime);
			    	for (int j = 0; j < bulletLocs.length; j++)
			    	{
				    	g.setColor(new Color(Color.HSBtoRGB((float)((1.0/bulletLocs.length)*(j+1)), 1, 1)));
				    	int size1 = 8, size2 = 10;
				    	int cBulletX = (int)bulletLocs[j].getX();
				    	int cBulletY = (int)bulletLocs[j].getY();
				    	
				    	g.drawOval(cBulletX-size1/2, cBulletY-size1/2, size1, size1);
				    	//g.drawString(Integer.toString(j), cBulletX-10+count*5, cBulletY-10);
				    	g.setColor(new Color(255, 255, 255, 100));
				    	g.fillOval(cBulletX-size2/2, cBulletY-size2/2, size2, size2);
			    	}
	    		}
	    	}
	    	//System.out.println("Painting " + eWaves.size() + " Waves...");
    	}
    	
    	if (showMovement && destinations != null)
    	{
	    	//TESTING PATHS
			g.setColor(new Color(0x00, 255, 0x00, 100));
	
			Point2D.Double[] destinations = MoveActor.generatePointsCircular(new Point2D.Double(getX(), getY()), 16, 150);
	    	//paths = Move.generatePaths(this, destinations);
	    	//pathRisks = Analysis.calcPathRisks(paths, eWaves, pointsPerWave, getTime());//this updates the risk ("Weight") of each point as well
	    	final int rad = 4;
	    	for (int i = 0; i < paths.length; i++)
	    	{
				g.fillOval((int)destinations[i].getX()-rad, (int)destinations[i].getY()-rad, rad*2, rad*2);
	    		for (int j = 0; j < paths[0].length; j++)
	    		{
	    			TimedWeightedPoint cPoint = paths[i][j];
	    			g.setColor(new Color(Color.HSBtoRGB((float) cPoint.getWeight(), 1, 1)));
	    			g.drawOval((int)cPoint.getX()-rad, (int)cPoint.getY()-rad, rad*2, rad*2);
	    			}
	    	}
	    	
	    	Point2D bestDestinationPoint = destinations[Analysis.getSafestPathIndex(pathRisks)];
	    	g.setColor(new Color(255, 255, 255, 255));
	    	g.drawLine((int)getX(), (int)getY(), (int)bestDestinationPoint.getX(), (int)bestDestinationPoint.getY());
    	}
    	
    	//STATS & OPTIONS TEXT
    	g.setColor(new Color(0x00, 0xff, 0x00, 170));
    	final int yStart = 20;
    	final int xStart = 10;
    	final int lineSpacing = 10;
		g.drawString("EnemyCircles (d):  " + showDistanceCircles, xStart, yStart + 1*lineSpacing);
		g.drawString("EnemyWaves (w):  " + showEnemyWaves, xStart , yStart + 2*lineSpacing);
		g.drawString("MovePaths (m):  " + showMovement, xStart, yStart + 3*lineSpacing);
    	switch (statsMode)
    	{
    	case 0 : g.drawString("Stats Mode: " + statsMode + " (Off)", 10, 10);
    	break;
    	case 1 : 
    		g.drawString("Stats Mode: " + statsMode + " (1v1)", 10, 10);
    		g.drawString("Target Name: " + cTargetName, 10, 110);
    		g.drawString("Best vGun: " + cTargetEnemy.getBestGun(), 10, 100);
    	break;
    	case 2 : 
    		g.drawString("Stats Mode: " + statsMode + " (Melee)", 10, 10);
    	break;
    	}
    	


        // Set the paint color to a red half transparent color
        g.setColor(new Color(0xff, 0x00, 0x00, 0x80));
        for (Entry<String, Enemy> entry : currentEnemies.entrySet())
        {
        	
        	String name = entry.getKey();
        	Enemy enemy = entry.getValue(); 
        	if (name.equals(cTargetName))//show target in different color
        	{
        		g.setColor(new Color(0x00, 0xff, 0x00, 255));
            	g.drawLine((int)enemy.getX(), (int)enemy.getY(), (int)getX(), (int)getY());
            	g.drawRect((int)enemy.getX() - 20, (int)enemy.getY() - 20, 40, 40);
            	g.setColor(new Color(0xff, 0x00, 0x00, 0x80));
        	}
        	else
        	{
        		// Draw a line from our robot to the scanned robot
	        	//	g.drawLine((int)enemy.getLatestX(), (int)enemy.getLatestY(), (int)getX(), (int)getY());
	        	// Draw a filled square on top of the scanned robot that covers it
	        	g.drawRect((int)enemy.getX() - 20, (int)enemy.getY() - 20, 40, 40);
        	}
        	


/*        	g.setColor(new Color(0xff, 0x00, 0x00, 5));
        	if (currentArea != null)
        		g.fillRect(currentArea[1], currentArea[3], currentArea[0]-currentArea[1], currentArea[2]-currentArea[3]);*/
        }
    	if (destinationXY != null)// mark destination of some movement algorithms
    	{
    		g.setColor(new Color(0xff, 0x00, 0x00));
    		g.fillOval(destinationXY[0], destinationXY[1], 10, 10);
    	}
    	else if (destinationPoints != null)
    	{
    		
    		for (int i = 0; i < destinationPoints.length; i++)
    		{
/*        		g.setColor(new Color(((int)(destinationPoints[i].getWeight()*255))%255,
        				((int)(destinationPoints[i].getWeight()*(255/2))+(255/2))%255,
        				0,
        				((int)(destinationPoints[i].getWeight()*(255/2))+(255/2))%255
        				));*/
        		g.setColor(new Color(
        				((int)(destinationPoints[i].getWeight()*255))%255,
        				((int)(destinationPoints[i].getWeight()*(255/2))+(255/4))%255,
        				0,
        				(200)%255
        				));
    			g.fillOval((int)destinationPoints[i].getX(), (int)destinationPoints[i].getY(), 7, 7);
    		}
    	}
    	
    		
    	if (cTargetEnemy != null) {
	    	if (predictedEnemyPointAtTime != null && predictedEnemyPointAtTime[2] != -1)
	    	{
	    		g.setColor(new Color(0x0f, 0xff, 0x00, 0x80));
		    	g.fillOval((int)predictedEnemyPointAtTime[0] - 20, (int)predictedEnemyPointAtTime[1] - 20, 40, 40);
		    	g.drawLine((int)cTargetEnemy.getX(), (int)cTargetEnemy.getY(), (int)predictedEnemyPointAtTime[0], (int)predictedEnemyPointAtTime[1]);
		    	g.drawString(new String("Prediction time offset: " + Math.round(predictedEnemyPointAtTime[2]) + " ticks.  -  " + getName()), 0, 0);
	    		
		    	//circle center drawing
		    	g.setColor(new Color(0xff, 0x00, 0xff, 0x80));
		    	g.fillOval((int)circleCenter[0] - 10+(int)getX(), (int)circleCenter[1] - 10+(int)getY(), 20, 20);
	
	    	}
	    	if (predictedEnemyPoint != null)
	    	{
	    		g.setColor(new Color(0xff, 0xff, 0x00, 0x80));
		    	g.fillOval((int)predictedEnemyPoint.getX() - 20, (int)predictedEnemyPoint.getY() - 20, 40, 40);
		    	g.drawLine((int)cTargetEnemy.getX(), (int)cTargetEnemy.getY(), (int)predictedEnemyPoint.getX(), (int)predictedEnemyPoint.getY());
	    	}
	    	
	    	
	    	vAngleGuns.onPaint(g, vBulletDisplay);
    	}
    	

    	
    	
    	//predict target location
/*    	else if (cTargetEnemy != null)//cTargetEnemy is null if we are last bot standing
    	{
    		g.setColor(new Color(0x00, 0xff, 0x00, 0x80));
	    	final int futureTime = 10;
	    	Point2D.Double predictLoc = Util.predictLocationLinear(cTargetEnemy, futureTime); //getPredictedCoordinates
	    	predictLoc.setLocation(Util.limitValueBounds(predictLoc.getX(), globalXMin, globalXMax),//limit to field bounds
	    			Util.limitValueBounds(predictLoc.getY(), globalYMin, globalYMax)); 
	    	g.fillOval((int)predictLoc.getX() - 20, (int)predictLoc.getY() - 20, 40, 40);
	    	g.drawLine((int)cTargetEnemy.getX(), (int)cTargetEnemy.getY(), (int)predictLoc.getX(), (int)predictLoc.getY());
	    	g.drawString(new String("Prediction time offset: " + futureTime + " ticks.  -  " + getName()), 0, 0);
    	}*/

        
    }
     
    //public void fireBullet(Point targetCoords){}

    //note to self: movement pattern:  Stay several units away from the walls.  Travel the border of the map.  Use Wave tracking to determine where enemy bullets could be. Stop completly before said bullets would connect.
    //Note to self: If change in enemy angle is constant, they are moving in a circle.

}