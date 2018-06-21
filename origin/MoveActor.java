package origin;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import origin.SleepSiphon;
import robocode.AdvancedRobot;
import robocode.Rules;
import util.TimedWeightedPoint;


public class MoveActor 
{
	
    static int destinationX = 0;
	static int destinationY = 0;
	
	public static void spin(SleepSiphon self)
	{
		self.setTurnLeft(10000);
		self.ahead(10000);
	}
	
	public static void goTo(SleepSiphon self, int x, int y) {//copied method (with minor changes)
        double a;
        self.setTurnRightRadians(Math.tan(
            a = Math.atan2(x -= (int) self.getX(), y -= (int) self.getY()) 
                  - self.getHeadingRadians()));
        self.setAhead(Math.hypot(x, y) * Math.cos(a));
    }
	public static void goTo(SleepSiphon self, double x, double y) {
        double a;
        self.setTurnRightRadians(Math.tan(
            a = Math.atan2(x -= self.getX(), y -= self.getY()) 
                  - self.getHeadingRadians()));
        self.setAhead(Math.hypot(x, y) * Math.cos(a));
    }
	
	public static void aGoTo(SleepSiphon self, double x, double y)
	{
/*		double relX = x -= self.getX();
		double relY = y -= self.getY();
        double a = Math.atan2(relX, relY);
        double b = robocode.util.Utils.normalRelativeAngle(a - self.getHeadingRadians());
        final double QUARTER_PI = Math.PI/4;
        System.out.println(b);
        self.setTurnRightRadians(b);
        self.setAhead(Math.hypot(x, y));
        if (b > QUARTER_PI || b < QUARTER_PI)
        {
            self.setTurnRightRadians((2*Math.PI) - b);
            self.setBack(Math.hypot(x, y));
        }
        else
        {

        }*/
		x -= self.getX();
		y -= self.getY();
	 

		double targetAbsBearing = Math.atan2(x, y);
		double turnToTarget = robocode.util.Utils.normalRelativeAngle(targetAbsBearing - self.getHeadingRadians());

		double dist = Math.hypot(x, y);
	 
		/* This is a simple method of performing "set front as back" */
		double turnAngle = Math.atan(Math.tan(turnToTarget));
		self.setTurnRightRadians(turnAngle);
		if(turnToTarget == turnAngle) {
			self.setAhead(dist);
		} else {
			self.setBack(dist);
		}

    }
	
    public static void random(SleepSiphon self)
    {
        final int botDim = (int) self.getHeight();
        if (self.getDistanceRemaining() == 0)
        	goTo(self, botDim + (int)(Math.random() * self.getBattleFieldWidth()-botDim), botDim + (int)(Math.random() * self.getBattleFieldHeight()-botDim));
    }
    
	private static WeightedPoint[] points = null;
    public static WeightedPoint[] circularPointDistribution(SleepSiphon self, Enemy enemy)
    {
        final int NUM_POINTS = 16;
        final double HYPOT = 150;

        
    	if (self.getDistanceRemaining() == 0)
    	{
	    	final double centerX = self.getX();
	    	final double centerY = self.getY();
	    	final double enemyX = enemy.getX();
	    	final double enemyY = enemy.getY();
	    	
	        final double botDim = self.getHeight();
	        points = new WeightedPoint[NUM_POINTS];
	    	final double[] bounds = Util.getFieldBoundsxXyY();
	    	double cAngle = 0;
	    	double angleIncrement = (2*Math.PI)/(double)NUM_POINTS;
	    	

	    	int point = 0;
	        for (int i = 0; i < NUM_POINTS; i++)
	        {
	        	double cX = Util.limitValueBounds(HYPOT*Math.sin(cAngle)+centerX, bounds[0], bounds[1]);
	        	double cY = Util.limitValueBounds(HYPOT*Math.cos(cAngle)+centerY, bounds[2], bounds[3]);
	        	
	        	points[i] = new WeightedPoint(cX, cY, 1.0/Point2D.distance(enemyX, enemyY, cX, cY));//lower weight = higher danger
	        	
	        	cAngle += angleIncrement;
	        	if (points[i].getWeight() < points[point].getWeight())
	        		point = i;
	        	//System.out.println("" + points[i].getX() + "\t" + points[i].getY());
	        	
	        }
	        
	        point = (int)(Math.random()*(NUM_POINTS));
	        
	        double x = points[point].getX();
	        double y = points[point].getY();
	        points[point].setWeight(1); 
        	goTo(self, x, y);
    	}
    	return points;
    }
    
    private static WeightedPoint[] awayPoints = null;
    public static WeightedPoint[] circularAwayPointDistribution(SleepSiphon self, Enemy enemy, double HYPOT)
    {
        final int NUM_POINTS = 16;
        //final double HYPOT = 150;

        
    	if (self.getDistanceRemaining() == 0 && enemy != null)
    	{
	    	final double centerX = self.getX();
	    	final double centerY = self.getY();
	    	final double enemyX = enemy.getX();
	    	final double enemyY = enemy.getY();
	    	
	        final double botDim = self.getHeight();
	        awayPoints = new WeightedPoint[NUM_POINTS];
	    	final double[] bounds = Util.getFieldBoundsxXyY();
	    	double cAngle = 0;
	    	double angleIncrement = (2*Math.PI)/(double)NUM_POINTS;
	    	

	    	int point = 0;
	        for (int i = 0; i < NUM_POINTS; i++)
	        {
	        	double cX = Util.limitValueBounds(HYPOT*Math.sin(cAngle)+centerX, bounds[0], bounds[1]);
	        	double cY = Util.limitValueBounds(HYPOT*Math.cos(cAngle)+centerY, bounds[2], bounds[3]);
	        	
	        	awayPoints[i] = new WeightedPoint(cX, cY, 1.0/Point2D.distance(enemyX, enemyY, cX, cY));//lower weight = higher danger
	        	
	        	cAngle += angleIncrement;
	        	if (awayPoints[i].getWeight() < awayPoints[point].getWeight())
	        		point = i;
	        	//System.out.println("" + awayPoints[i].getX() + "\t" + points[i].getY());
	        	
	        }
	        
	        //point = (int)(Math.random()*(NUM_POINTS));
	        
	        double x = awayPoints[point].getX();
	        double y = awayPoints[point].getY();
	        awayPoints[point].setWeight(1); 
        	goTo(self, x, y);
    	}
    	return awayPoints;
    }
    public static double findPointRiskBasic(ConcurrentHashMap<String, Enemy> enemies, Point2D.Double point)
    {
    	double risk = 0;
    	
    	
    	
		return 0;
    }
    
    public static Point2D.Double[] minimumRiskBasic(SleepSiphon self, ConcurrentHashMap<String, Enemy> enemies)
    {
    	
    	
		return null;
    }
    
    public static Point2D.Double[] waveSurfing1v1()
    {
    	return null;
    }

    public static WeightedPoint[] fourCornersLoose(SleepSiphon self)
    {
    	final int fw = (int) self.getBattleFieldWidth();
    	final int fh = (int) self.getBattleFieldHeight();
    	final int distFromWalls = 40;
    	//final int allowedError = 5;
    	int x = (int) self.getX();
    	int y = (int) self.getY();

    	if (self.getDistanceRemaining() == 0)
    	{
/*    		if (Util.withinTolerance(distFromWalls, x, allowedError))// if on left side
    		{ System.out.println("left");
    			if (Util.withinTolerance(distFromWalls, y, allowedError))// if on bottom
    	        	goTo(self, distFromWalls, fh-distFromWalls);
    			else if (Util.withinTolerance(fh-distFromWalls, y, allowedError))//if on top
    	        	goTo(self, fw-distFromWalls, fh-distFromWalls);

    		}
    		else if (Util.withinTolerance(fw-distFromWalls, x, allowedError))// if on right side
    		{System.out.println("right");
    			if (Util.withinTolerance(distFromWalls, y, allowedError))// if on bottom
    	        	goTo(self, fw-distFromWalls, distFromWalls);
    			else if (Util.withinTolerance(fh-distFromWalls, y, allowedError))// if on top
    	        	goTo(self, distFromWalls, distFromWalls);

    		}
    		else
	        	goTo(self, distFromWalls, distFromWalls);*/
    		int section = 0;
    		section = Util.currentSection2DUniform(4, x, y, fw, fh);
    		switch (section)
    		{
    			case -1 : System.out.println("Error: invalid section");
    			break;
	    		case 0 : destinationX = distFromWalls; destinationY = fh-distFromWalls;//goto top left
	    		break;
	    		case 1 : destinationX = fw-distFromWalls; destinationY = fh-distFromWalls;//goto top right
	    		break;
	    		case 2 : destinationX = distFromWalls; destinationY = distFromWalls;//goto bottom left
	    		break;
	    		case 3 : destinationX = fw-distFromWalls; destinationY = distFromWalls;// goto bottom right 
	    		break;
    		}
    		goTo(self, destinationX, destinationY);

    	}
    	return new WeightedPoint[] {new WeightedPoint(destinationX, destinationY)};
    }
    
    public static void actualdodging() {}
    
    private static WeightedPoint[] nClosestPoints = null;
    public static WeightedPoint[] circleDistribution_NeverClosest(SleepSiphon self, Ellipse2D.Double[] circles, double HYPOT)
    {
        final int NUM_POINTS = 32;
        //double HYPOT = 150;
        
        
    	if (self.getDistanceRemaining() == 00)
    	{
	    	final double centerX = self.getX();
	    	final double centerY = self.getY();
	    	
	        final double botDim = self.getHeight();
	        nClosestPoints = new WeightedPoint[NUM_POINTS];
	    	final double[] bounds = Util.getFieldBoundsxXyY();
	    	double cAngle = 0;
	    	double angleIncrement = (2*Math.PI)/(double)NUM_POINTS;
	    	boolean noPoints = true;
	    	int count = 0;
	    	
	    	int point = 0;
	    	while (noPoints && count < 2)
	    	{
		        for (int i = 0; i < NUM_POINTS; i++)
		        {
		        	double cX = Util.limitValueBounds(HYPOT*Math.sin(cAngle)+centerX, bounds[0], bounds[1]);
		        	double cY = Util.limitValueBounds(HYPOT*Math.cos(cAngle)+centerY, bounds[2], bounds[3]);
		        	double cWeight = 0;
		        	
		        	for (int j = 0; j < circles.length; j++)
		        	{
		        		if (circles[j].contains(cX, cY))
		        		{
		        			cWeight = 0;
		        			break;
		        		}
	//div c vs f
		        		cWeight = Math.random();
	
		        	}
		        	
		        	nClosestPoints[i] = new WeightedPoint(cX, cY, cWeight);
	
		        	cAngle += angleIncrement;
		        	if (nClosestPoints[i].getWeight() > nClosestPoints[point].getWeight())
		        		point = i;	        	
		        	if (cWeight > 0)
		        		noPoints = false;
		        	
		        }
		        count+=1;
		        HYPOT *=2;
	    	}
	        if (noPoints)
	        {
	        	//if (closest!=null)
	        	//nClosestPoints = circularAwayPointDistribution(self, closest, HYPOT);
	        	point = (int)(Math.random()*(NUM_POINTS));
	
	        	//for (int i = 0; i < 16; i++)
		        //	if (nClosestPoints[i].getWeight() > nClosestPoints[point].getWeight())
		        //		point = i;	  
	        }
	        double x = nClosestPoints[point].getX();
	        double y = nClosestPoints[point].getY();
	        nClosestPoints[point].setWeight(1); 
	        
	        	
        	aGoTo(self, x, y);
    	}
    	return nClosestPoints;
    }
    
    public static TimedWeightedPoint[][] generatePaths(SleepSiphon self, Point2D[] destinationPoints)
    {
    	final double maxVelocity = Rules.MAX_VELOCITY;
    	final double accel = Rules.ACCELERATION;
    	final double decel = Rules.DECELERATION;
    	final int TIME_LIMIT = 16;
    	
    	double baseVelocity = self.getVelocity();
    	double baseTurnRate = Rules.getTurnRate(baseVelocity);//dependant on velocity
		double baseHeading = self.getHeading();
		double baseSelfX = self.getX();
		double baseSelfY = self.getY();
		//System.out.println("\n\nNEW POINTSET");

    	TimedWeightedPoint[][] paths = new TimedWeightedPoint[destinationPoints.length][TIME_LIMIT];
    	for(int cPath = 0; cPath < destinationPoints.length; cPath++)
    	{
    		//double velocity = baseVelocity;
    		double velocity = Rules.MAX_VELOCITY;
    		double turnRate = baseTurnRate;
    		double heading = baseHeading;
    		
    		double selfX = baseSelfX;
    		double selfY = baseSelfY;
			double destX = destinationPoints[cPath].getX()-selfX;
			double destY = destinationPoints[cPath].getY()-selfY;
			
			
			double targetAbsBearing = Math.atan2(destX, destY);
			double turnToTarget = robocode.util.Utils.normalRelativeAngle(targetAbsBearing - self.getHeadingRadians());
	
			double dist = Math.hypot(destX, destY);
		 
			/* This is a simple method of performing "set front as back" */
			double turnAngle = Math.atan(Math.tan(turnToTarget)); // Ensures angle is between -pi/2 and pi/2
			double turnRemaining = turnAngle;
			double angleChange = 0;
			
			int simTime = 0;
			//System.out.println("\n\nNEXT PATH");
			while (simTime < TIME_LIMIT)
			{
/*				System.out.println("turnRemaining:\t" + (turnRemaining/Math.PI) + "pi");
				turnRate = Rules.getTurnRateRadians(velocity)*Math.signum(turnRemaining);//*Math.signum(turnRemaining)
				if (turnRemaining > turnRate)
				{
					angleChange += turnRate;
					turnRemaining-=turnRate;
					
				}
				else if (turnRemaining < turnRate)
				{
					angleChange += turnRemaining;
					turnRemaining -= turnRemaining;
				}
				else //if turnRemaining == 0
				{
					angleChange = 0;
				}*/
				
/*				if (velocity > 0)
					velocity = Util.limitValueBounds(turnToTarget == turnAngle ? velocity+accel : velocity-decel, -decel, accel);
				else if (velocity < 0)
					velocity = Util.limitValueBounds(turnToTarget == turnAngle ? velocity+decel: velocity-accel, -accel, decel);
				else //if velocity is 0
					velocity = Util.limitValueBounds(turnToTarget == turnAngle ? velocity+accel: velocity-accel, -accel, accel);
					
				selfX += 10*velocity*Math.sin(heading);
				selfY += 10*velocity*Math.cos(heading);*/
				selfX += velocity*Math.sin(targetAbsBearing);//simTime*(Math.PI/16)//+angleChange
				selfY += velocity*Math.cos(targetAbsBearing);//simTime*(Math.PI/16)//+angleChange
				
/*				self.setTurnRightRadians(turnAngle);
				if(turnToTarget == turnAngle) {
					self.setAhead(dist);
				} else {
					self.setBack(dist);
				}*/
				
				TimedWeightedPoint point = new TimedWeightedPoint(selfX, selfY, 0, simTime+1);
				Util.changeCoordinateToMap(point);
				paths[cPath][simTime] = point;
				simTime+=1;
			}
    	}
    	return paths;
    }
    
    public static Point2D.Double[] generatePointsCircular(Point2D center, int numPoints, double HYPOT)
    {
    	double cAngle = 0;
    	double angleIncrement = (2*Math.PI)/(double)numPoints;
    	Point2D.Double[] output = new Point2D.Double[numPoints];
		for (int i = 0; i < numPoints; i++)
	    {
		    	double cX = HYPOT*Math.sin(cAngle)+center.getX();
		    	double cY = HYPOT*Math.cos(cAngle)+center.getY();
				output[i] = Util.limitCoordinateToMap(cX, cY);		
				cAngle += angleIncrement;
		}
		return output;
	}
    
    
    private static WeightedPoint[] wavePoints = null;
    public static WeightedPoint[] leastRiskPath(SleepSiphon self, Ellipse2D.Double[] closestToEnemyCircles, double HYPOT)
    {
        final int NUM_POINTS = 32;
        //double HYPOT = 150;
        
        
    	if (self.getDistanceRemaining() == 00)
    	{
	    	final double centerX = self.getX();
	    	final double centerY = self.getY();
	    	
	        final double botDim = self.getHeight();
	        wavePoints = new WeightedPoint[NUM_POINTS];
	    	final double[] bounds = Util.getFieldBoundsxXyY();
	    	double cAngle = 0;
	    	double angleIncrement = (2*Math.PI)/(double)NUM_POINTS;
	    	boolean noPoints = true;
	    	int count = 0;
	    	
	    	int point = 0;
	    	while (noPoints && count < 2)
	    	{
		        for (int i = 0; i < NUM_POINTS; i++)
		        {
		        	double cX = Util.limitValueBounds(HYPOT*Math.sin(cAngle)+centerX, bounds[0], bounds[1]);
		        	double cY = Util.limitValueBounds(HYPOT*Math.cos(cAngle)+centerY, bounds[2], bounds[3]);
		        	double cWeight = 0;
		        	
		        	for (int j = 0; j < closestToEnemyCircles.length; j++)
		        	{
		        		if (closestToEnemyCircles[j].contains(cX, cY))
		        		{
		        			cWeight = 0;
		        			break;
		        		}
	//div c vs f
		        		cWeight = Math.random();
	
		        	}
		        	
		        	wavePoints[i] = new WeightedPoint(cX, cY, cWeight);
	
		        	cAngle += angleIncrement;
		        	if (wavePoints[i].getWeight() > wavePoints[point].getWeight())
		        		point = i;	        	
		        	if (cWeight > 0)
		        		noPoints = false;
		        	
		        }
		        count+=1;
		        HYPOT *=2;
	    	}
	        if (noPoints)
	        {
	        	//if (closest!=null)
	        	//wavePoints = circularAwayPointDistribution(self, closest, HYPOT);
	        	point = (int)(Math.random()*(NUM_POINTS));
	
	        	//for (int i = 0; i < 16; i++)
		        //	if (wavePoints[i].getWeight() > wavePoints[point].getWeight())
		        //		point = i;	  
	        }
	        double x = wavePoints[point].getX();
	        double y = wavePoints[point].getY();
	        wavePoints[point].setWeight(1); 
	        
	        	
        	aGoTo(self, x, y);
    	}
    	return wavePoints;
    }

	public static Point2D.Double[] removeCloseCoordinates(Point2D.Double[] dests, double minDistance) 
	{
		ArrayList<Point2D.Double> temp = new ArrayList<Point2D.Double>();
		for (int i = 1; i < dests.length; i++)
			temp.add(dests[i]);
		for (int i = 1; i < temp.size(); i++)
		{
			if (Point2D.distance(dests[i].getX(), dests[i].getY(), dests[i-1].getX(), dests[i-1].getY()) < minDistance)
			{
				temp.remove(i);
				temp.remove(i-1);
			}
		}
		Point2D.Double[] output = new Point2D.Double[temp.size()];
		output = temp.toArray(output);
		return output;
	}
	public static TimedWeightedPoint[] removeCloseCoordinates(TimedWeightedPoint[] dests, double minDistance) 
	{
		ArrayList<TimedWeightedPoint> temp = new ArrayList<TimedWeightedPoint>();
		for (int i = 1; i < dests.length; i++)
			temp.add(dests[i]);
		for (int i = 1; i < temp.size(); i++)
		{
			if (Point2D.distance(dests[i].getX(), dests[i].getY(), dests[i-1].getX(), dests[i-1].getY()) < minDistance)
			{
				temp.remove(i);
				temp.remove(i-1);
			}
		}
		TimedWeightedPoint[] output = new TimedWeightedPoint[temp.size()];
		output = temp.toArray(output);
		return output;
	}

	public static WeightedPoint[] circleDistribution_NeverClosest2(SleepSiphon self, Ellipse2D.Double[] circles, ConcurrentHashMap<String, Enemy> enemies, double HYPOT) 
	{
	    int NUM_POINTS = 32;
		WeightedPoint[] nClosestPoints2;

	    Collection<Enemy> eDataSet = enemies.values();
	    
	    double centerX = self.getX();
	    double centerY = self.getY();
	    
	    nClosestPoints2 = new WeightedPoint[32];
	    double[] bounds = Util.getFieldBoundsxXyY();
	    double cAngle = 0.0D;
	    double angleIncrement = 0.19634954084936207D;
	    boolean noPoints = true;
	    int count = 0;
	    
	    int point = 0;
	    while ((noPoints) && (count < 2))
	    {
	      for (int i = 0; i < 32; i++)
	      {
	        double cX = Util.limitValueBounds(HYPOT * Math.sin(cAngle) + centerX, bounds[0], bounds[1]);
	        double cY = Util.limitValueBounds(HYPOT * Math.cos(cAngle) + centerY, bounds[2], bounds[3]);
	        double cWeight = 0.0D;
	        
	        for (int j = 0; j < circles.length; j++)
	        {
	          if (circles[j].contains(cX, cY))
	          {
	            cWeight = 2.0D;
	          }
	          
	          double tempWeight = 0.0D;
	          for (Enemy cEnemy : eDataSet)
	          {
	            tempWeight += 1.0D / Point2D.distanceSq(cX, cY, cEnemy.getX(), cEnemy.getY());
	          }
	          cWeight += tempWeight / eDataSet.size();
	          cWeight += Analysis.calcPoint_repelWalls(cX, cY)*0.5;
	        }
	        
	        nClosestPoints2[i] = new WeightedPoint(cX, cY, cWeight);
	        
	        cAngle += angleIncrement;
	        if (nClosestPoints2[i].getWeight() < nClosestPoints2[point].getWeight())
	          point = i;
	        if (cWeight > 0.0D) {
	          noPoints = false;
	        }
	      }
	      count++;
	      HYPOT *= 2.0D;
	    }
	    if (noPoints)
	    {


	      point = (int)(Math.random() * 32.0D);
	    }
	    



	    double x = nClosestPoints2[point].getX();
	    double y = nClosestPoints2[point].getY();
	    nClosestPoints2[point].setWeight(0.5D);
	    

	    aGoTo(self, x, y);
	    
	    return nClosestPoints2;
	  }
}
