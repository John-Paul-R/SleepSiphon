package BetaF;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

import robocode.Bullet;

import BetaF.SleepSiphon;
import robocode.Rules;

public class Gun
{
	private SleepSiphon self;
	private LinkedList<Bullet> bullets;
	private LinkedList<SelfWave> selfWaves;
	private ConcurrentHashMap<String, Enemy> enemies;
	HashMap<String, EnemyML> enemiesml;
	
	public Gun(SleepSiphon s, LinkedList<Bullet> b, LinkedList<SelfWave> sw, ConcurrentHashMap<String, Enemy> enemies, HashMap<String, EnemyML> enemiesml)
	{
		self = s;
		bullets = b;
		selfWaves = sw;
		this.enemies = enemies;
		this.enemiesml = enemiesml;
	}
	
	//General/Utility
    public double firePowerByDist(Enemy target)
    {
    	final double currentDist = Math.sqrt(Math.pow((target.getX() - self.getX()),2) + Math.pow((target.getY() - self.getY()), 2));
    	//final double power = Util.limitValueBounds(1/Math.pow((currentDist/500),2), 1.1, 3); //set power to have an inverse square relationship with distance.  Set bounds to 1.1 and 3
    	final double power = Util.limitValueBounds(1/(currentDist/Math.min(Math.max(self.getBattleFieldWidth(), self.getBattleFieldHeight()), 1000.00)), 1.5, 3);//set power to have an inverse relationship with distance.  Set bounds to 1.05 and 3.  The above method is stupid. That would be for 3d space, not 2d - I think.
    	return power;
    }
    public void aimToCoordinate(double x, double y)
    {
        self.setTurnGunRightRadians(robocode.util.Utils.normalRelativeAngle(
	            Math.atan2(x-self.getX(), y-self.getY())
	            - self.getGunHeadingRadians()));
    }
    public void aimToCoordinate(Point2D.Double coords)
    {
        self.setTurnGunRightRadians(robocode.util.Utils.normalRelativeAngle(
	            Math.atan2(coords.getX()-self.getX(), coords.getY()-self.getY())
	            - self.getGunHeadingRadians()));
    }
    public void setFireBullet(double bulletPower)
    {
    	if (self.getGunHeat() == 0)
    	{
	    	Bullet nBullet = self.setFireBullet(bulletPower);
	    	if (nBullet != null)
	    	{
		    	bullets.add(nBullet);
		    	//System.out.println(nBullet.getName());
		    	selfWaves.add(new SelfWave(self.getX(), self.getY(), self.getTime(), nBullet.getPower(), enemies, enemiesml));
	    	}
	    	
    	}
    }
    
	//Aiming/Prediction algorithms
	public void basic(Enemy target, double BULLET_POWER)
    {
    	if (self.getGunTurnRemaining() == 0)
    	{
	    	if (target != null)
	    	{
	        	double targetAbsoluteBearing = self.getHeadingRadians() + target.getBearing();
	        	self.setTurnGunRightRadians(robocode.util.Utils.normalRelativeAngle(targetAbsoluteBearing - self.getGunHeadingRadians()));
	        	setFireBullet(BULLET_POWER);
	    	}
    	}
    }
    
    //LINEAR PREDICTION (MY CODE & MATH)
    public double[] linearPredictionFire(Enemy target, double BULLET_POWER)
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
	        
    	    this.setFireBullet(BULLET_POWER);
    	}
    	//time = Math.sqrt(((eVelocityX * time)+eX)^2 + ((eVelocityY*time)+eY)^2)/BULLET_POWER;
    	return new double[] {endX, endY, time};
    }
    //TODO if hit accuracy is low, lower bullet strength
    //TODO use average velocity, instead of current (in some cases; perhaps use multi gun to see what is most effective against a given enemy) in order to predict enemy future location (particular useful against Walls, because it allows you to predict its future location, even when it is turning at the corners (might be less effective at longer ranges)
    public Point2D.Double constantTurnPredict(Enemy target,  double BULLET_POWER)
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
        aimToCoordinate(targetCoord);
        
	    this.setFireBullet(BULLET_POWER);
     	return targetCoord;
    }
    public Point2D.Double averageMovementGun(Enemy target, double BULLET_POWER)
    {
    	double avgVelocity = Analysis.getAvgVelocity(target);
    	double avgHeading = Analysis.getAvgHeading(target);
    	
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
	        
    	    this.setFireBullet(BULLET_POWER);
    	}
    	//
    	
    	
		return new Point2D.Double(endX, endY);
    }
}
