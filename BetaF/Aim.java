package BetaF;

import java.awt.geom.Point2D;
import java.util.concurrent.ConcurrentHashMap;

import BetaF.SleepSiphon;
import robocode.Rules;

public class Aim
{
    public static void basic(SleepSiphon self, Enemy target, double BULLET_POWER)
    {
    	if (self.getGunTurnRemaining() == 0)
    	{
	    	if (target != null)
	    	{
	        	double targetAbsoluteBearing = self.getHeadingRadians() + target.getBearing();
	        	self.setTurnGunRightRadians(robocode.util.Utils.normalRelativeAngle(targetAbsoluteBearing - self.getGunHeadingRadians()));
	        	self.setFire(BULLET_POWER);
	        	//System.out.println("Condition Direction: " + (targetAbsoluteBearing - self.getGunHeadingRadians())/Math.PI + " pi");
		    	//System.out.println("Target Bearing: " + (target.getLatestBearing()/Math.PI) + " pi");
		    	//System.out.println("Turn Remaining: " + (self.getGunTurnRemainingRadians()/Math.PI)+" pi\n");
	    	}
    	}
    }
    
    
    //LINEAR PREDICTION (COPIED)
/*    public static double[] linearPrediction(SleepSiphon self, // I need to adjust this for the delay between firing.  My robot moves, which throws off the aim.
    		Enemy target)
    {
    	double t = 0; double endX = 0; double endY = 0;
		//Basic
	    double bulletPower = 3;
	    double headOnBearing = self.getHeadingRadians() + target.getLatestBearing();
	    double linearBearing = headOnBearing + Math.asin(target.getLatestVelocity() / Rules.getBulletSpeed(bulletPower) * Math.sin(target.getLatestHeading() - headOnBearing));
	    self.setTurnGunRightRadians(Utils.normalRelativeAngle(linearBearing - self.getGunHeadingRadians()));
	    self.setFire(bulletPower);
		
		//Precise & Assuming stop at walls
		//NOT_MY_CODE
	    final double FIREPOWER = 2;
	    final double ROBOT_WIDTH = 16,ROBOT_HEIGHT = 16;
	    // Variables prefixed with e- refer to enemy, b- refer to bullet and r- refer to robot
	    final double eAbsBearing = self.getHeadingRadians() + target.getBearing();
	    final double selfX = self.getX(), selfY = self.getY(),
	        bulletVelocity = Rules.getBulletSpeed(FIREPOWER);
	    final double 
	    	enemyX = selfX + target.getDistance()*Math.sin(eAbsBearing),
	        enemyY = selfY + target.getDistance()*Math.cos(eAbsBearing),
	        enemyVelocity = target.getVelocity(),
	        enemyHeading = target.getHeading();
	    // These constants make calculating the quadratic coefficients below easier
	    final double A = (enemyX - selfX)/bulletVelocity;
	    final double B = enemyVelocity/bulletVelocity*Math.sin(enemyHeading);
	    final double C = (enemyY - selfY)/bulletVelocity;
	    final double D = enemyVelocity/bulletVelocity*Math.cos(enemyHeading);
	    // Quadratic coefficients: a*(1/t)^2 + b*(1/t) + c = 0
	    final double a = A*A + C*C;
	    final double b = 2*(A*B + C*D);
	    final double c = (B*B + D*D - 1);
	    final double discrim = b*b - 4*a*c; // (b^2 - 4(ac)) -- discriminant of quadratic formula
	    if (discrim >= 0) {//check to make sure solution exists. If solution exists, proceed with calculations.
	        // Reciprocal of quadratic formula
	        final double t1 = 2*a/(-b - Math.sqrt(discrim));
	        final double t2 = 2*a/(-b + Math.sqrt(discrim));
	        t = Math.min(t1, t2) >= 0 ? Math.min(t1, t2) : Math.max(t1, t2);//Ternary operator: if condition (left) is true, return val1 (middle). Else return val2 (right)
	        // Assume enemy stops at walls
	        endX = Util.limitValueBounds(
	            enemyX + enemyVelocity*t*Math.sin(enemyHeading), //endX = enemy current position + v*t*sin(heading) (in robocode, 0 rad is due north, so x val is sin)
	            ROBOT_WIDTH/2, self.getBattleFieldWidth() - ROBOT_WIDTH/2);//limit simply sets endX to the x coordinate of the border in the enemy's projected location is outside of the map
	        endY = Util.limitValueBounds(
	            enemyY + enemyVelocity*t*Math.cos(enemyHeading),
	            ROBOT_HEIGHT/2, self.getBattleFieldHeight() - ROBOT_HEIGHT/2);
	        self.setTurnGunRightRadians(robocode.util.Utils.normalRelativeAngle(
	            Math.atan2(endX - selfX, endY - selfY)
	            - self.getGunHeadingRadians()));
	        self.setFire(FIREPOWER);
    	}
    	 return new double[] {endX, endY, t};
    }*/
    
    public static double firePowerByDist(SleepSiphon self, Enemy target)
    {
    	final double currentDist = Math.sqrt(Math.pow((target.getX() - self.getX()),2) + Math.pow((target.getY() - self.getY()), 2));
    	//final double power = Util.limitValueBounds(1/Math.pow((currentDist/500),2), 1.1, 3); //set power to have an inverse square relationship with distance.  Set bounds to 1.1 and 3
    	final double power = Util.limitValueBounds(1/(currentDist/Math.min(Math.max(self.getBattleFieldWidth(), self.getBattleFieldHeight()), 1000.00)), 1.5, 3);//set power to have an inverse relationship with distance.  Set bounds to 1.05 and 3.  The above method is stupid. That would be for 3d space, not 2d - I think.
    	return power;
    }
    
    
    //LINEAR PREDICTION (MY CODE & MATH)
    public static double[] linearPredictionFire(SleepSiphon self, Enemy target, double BULLET_POWER)
    {
    	double time = -1;
    	double endX = 0;
    	double endY = 0;
    	final double eX = target.getX()-self.getX();//enemy X relative to self
    	final double eY = target.getY()-self.getY();//enemy Y relative to self
    	//calculations, etc
    	final double eVelocityX = target.getVelocity()*Math.sin(target.getHeading());
    	final double eVelocityY = target.getVelocity()*Math.cos(target.getHeading());
    	final double bulletVelocity = Rules.getBulletSpeed(BULLET_POWER);
    	//b^2 * t^2 = ((mt)+q)^2 + ((nt)+r)^2
    	//b^2 * t^2 = ((m*m)t^2) + 2*qmt + q*q + ((n*n)t^2) + 2*rnt + r*r
    	/*
    	 	c = q*q + r*r  //c = constant
			a = n*n + m*m
			b = 2*(q*n + r*m)
    	 */
    	
    	final double c = eX*eX + eY*eY;
    	final double a2 = bulletVelocity*bulletVelocity;
    	final double a = eVelocityX*eVelocityX + eVelocityY*eVelocityY - a2;
    	final double b = 2*(eX*eVelocityX + eY*eVelocityY);

    	final double discrim = b*b - 4*a*c;
    	if (discrim >= 0) //check to make sure solution exists. If solution exists, proceed with calculations.
    	{
    		final double t1 = (-b + Math.sqrt(b*b - 4*a*c))/(2*a);
    		final double t2 = (-b - Math.sqrt(b*b - 4*a*c))/(2*a);
    		time = (Math.min(t1, t2) >= 0 ? Math.min(t1, t2) : Math.max(t1, t2)) + (self.getTime() - target.getTime()); //Ternary operator: If the lower val root is greater than 0, return that value. else return the larger value // add the age of the data we are using for calculations to the time 
    		//assume enemy will stop at walls (constrain x & y values to battlefield)
    		final double[] bounds = Util.getFieldBoundsxXyY();
    		endX = Util.limitValueBounds(eX+self.getX()+eVelocityX*time, bounds[0], bounds[1]);
    		endY = Util.limitValueBounds(eY+self.getY()+eVelocityY*time, bounds[2], bounds[3]);
    		
    		//System.out.println(endX + "\t" + endY);
	        self.setTurnGunRightRadians(robocode.util.Utils.normalRelativeAngle(
    	            Math.atan2(endX-self.getX(), endY-self.getY())
    	            - self.getGunHeadingRadians()));
	        
    	    self.setFire(BULLET_POWER);
    	}
    	//time = Math.sqrt(((eVelocityX * time)+eX)^2 + ((eVelocityY*time)+eY)^2)/BULLET_POWER;
    	return new double[] {endX, endY, time};
    }
    //TODO if hit accuracy is low, lower bullet strength
    //TODO use average velocity, instead of current (in some cases; perhaps use multi gun to see what is most effective against a given enemy) in order to predict enemy future location (particular useful against Walls, because it allows you to predict its future location, even when it is turning at the corners (might be less effective at longer ranges)
    public static Point2D.Double constantTurnPredict(SleepSiphon self, Enemy target,  double BULLET_POWER)
    {
    	double turnRate = target.getLatestTurnRate();
    	double relX = target.getX() - self.getX();
    	double relY = target.getY() - self.getY();
    	double cHeading = target.getHeading();
    	double velocity = target.getVelocity();
    	
    	double selfX = self.getX();
    	double selfY = self.getY();
    	
    	double deltaTime = 0;
    	final double bulletVelocity = Rules.getBulletSpeed(BULLET_POWER);
    	
    	while ((bulletVelocity * deltaTime) < Math.abs(Point2D.distance(0, 0, relX, relY)))
    	{
    		relX += (velocity * Math.sin(cHeading + turnRate));
    		relY += (velocity * Math.cos(cHeading + turnRate));
    		cHeading += turnRate;
    		deltaTime += 1;
    	}
		
    	Point2D.Double targetCoord = Util.limitCoordinateToMap(new Point2D.Double(relX + selfX, relY +selfY));
        aimToCoordinate(self, targetCoord);
        
	    self.setFire(BULLET_POWER);
     	return targetCoord;
    }
    
    public static Point2D.Double predictCircle(SleepSiphon self, Enemy target)
    {
    	final double firePower = Aim.firePowerByDist(self, target);
    	Point2D.Double predictedCoordinate = Analysis.predictInterceptionPoint_TargetConstantTurn(self, target, Analysis.predictConstantTurn(self, target), firePower);
    	
    	aimToCoordinate(self, predictedCoordinate);
    	
    	self.setFireBullet(firePower);
    	
    	return predictedCoordinate;
    }
    public static Point2D.Double averageMovementGun(SleepSiphon self, Enemy target, double BULLET_POWER)
    {
    	double avgVelocity = Analysis.getAvgVelocity(target);
    	double avgHeading = Analysis.getAvgHeading(target);
    	
    	Point2D.Double predictedPoint;

    	double time = -1;
    	double endX = 0;
    	double endY = 0;
    	final double eX = target.getX()-self.getX();//enemy X relative to self
    	final double eY = target.getY()-self.getY();//enemy Y relative to self

    	final double avgVelocityX = avgVelocity*Math.sin(avgHeading);
    	final double avgVelocityY = avgVelocity*Math.cos(avgHeading);
    	final double bulletVelocity = Rules.getBulletSpeed(BULLET_POWER);
    	
    	final double c = eX*eX + eY*eY;
    	final double a2 = bulletVelocity*bulletVelocity;
    	final double a = avgVelocityX*avgVelocityX + avgVelocityY*avgVelocityY - a2;
    	final double b = 2*(eX*avgVelocityX + eY*avgVelocityY);

    	final double discrim = b*b - 4*a*c;
    	if (discrim >= 0) //check to make sure solution exists. If solution exists, proceed with calculations.
    	{
    		final double t1 = (-b + Math.sqrt(b*b - 4*a*c))/(2*a);
    		final double t2 = (-b - Math.sqrt(b*b - 4*a*c))/(2*a);
    		time = (Math.min(t1, t2) >= 0 ? Math.min(t1, t2) : Math.max(t1, t2)) + (self.getTime() - target.getTime()); //Ternary operator: If the lower val root is greater than 0, return that value. else return the larger value // add the age of the data we are using for calculations to the time 
    		//assume enemy will stop at walls (constrain x & y values to battlefield)
    		final double[] bounds = Util.getFieldBoundsxXyY();
    		endX = Util.limitValueBounds(eX+self.getX()+avgVelocityX*time, bounds[0], bounds[1]);
    		endY = Util.limitValueBounds(eY+self.getY()+avgVelocityY*time, bounds[2], bounds[3]);
    		
	        self.setTurnGunRightRadians(robocode.util.Utils.normalRelativeAngle(
    	            Math.atan2(endX-self.getX(), endY-self.getY())
    	            - self.getGunHeadingRadians()));
	        
    	    self.setFire(BULLET_POWER);
    	}
    	//
    	
    	
		return predictedPoint = new Point2D.Double(endX, endY);
    }
    public static void aimToCoordinate(SleepSiphon self, double x, double y)
    {
        self.setTurnGunRightRadians(robocode.util.Utils.normalRelativeAngle(
	            Math.atan2(x-self.getX(), y-self.getY())
	            - self.getGunHeadingRadians()));
    }
    public static void aimToCoordinate(SleepSiphon self, Point2D.Double coords)
    {
        self.setTurnGunRightRadians(robocode.util.Utils.normalRelativeAngle(
	            Math.atan2(coords.getX()-self.getX(), coords.getY()-self.getY())
	            - self.getGunHeadingRadians()));
    }
}
