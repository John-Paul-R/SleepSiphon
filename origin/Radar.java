package origin;

import java.awt.geom.Point2D;
import java.util.HashMap;

import robocode.Rules;

public class Radar
{

	public static void infinityLock(Enemy target, SleepSiphon self)
    {
        if (self.getRadarTurnRemaining() == 0)
            self.setTurnRadarRight(Double.POSITIVE_INFINITY);
    }

	public static void spinToCenter(SleepSiphon self)
	{
		if (self.getRadarTurnRemaining() == 0)
		{
			Point2D.Double center = Util.getFieldCenter();
			double spinDirection = Math.signum( robocode.util.Utils.normalRelativeAngle(Math.atan2(center.getX()-self.getX(), center.getY()-self.getY()) - self.getRadarHeadingRadians()) );
			self.setTurnRadarRight(Double.POSITIVE_INFINITY*spinDirection);
		}
    }

	// not my code
	public static void factorLock(Enemy target, SleepSiphon self)
	{
		final double FACTOR = 36/Point2D.distance(target.getX(), target.getY(), self.getX(), self.getY())*8*Math.PI;
		if (target != null)
    	{
			double absBearing = target.getBearing() + self.getHeadingRadians();
			self.setTurnRadarRightRadians( FACTOR * robocode.util.Utils.normalRelativeAngle(absBearing - self.getRadarHeadingRadians()) );
    	}
	} //end of "not my code"

	//Actual oldest scanned
	private static Enemy oldestScanned = null;
	private static long oldestScannedAge = -1;
	private static double turnAmountRemaining = 0;
	private static long timeToScanAll = 1;
	public static void oldestScanned(HashMap<String, Enemy> enemies, SleepSiphon self)
	{
		long currentTime = self.getTime();
		String nOldestScannedName = Util.getOldestName(enemies, self);
		Enemy nOldestScanned = enemies.get(nOldestScannedName);
		//System.out.println(nOldestScannedName);
		turnAmountRemaining -= (Math.PI/4);
		if (nOldestScanned != null)
		{
	    	if (nOldestScanned != oldestScanned || oldestScannedAge != nOldestScanned.getLatest().getAge(self.getTime()))
	    	{
				oldestScanned = nOldestScanned;
				oldestScannedAge = nOldestScanned.getLatest().getAge(currentTime);
				//System.out.println("Oldest Scanned Name: " + nOldestScannedName);
	    	}
		}
		if (oldestScanned != null && oldestScanned.timeSinceUpdate(currentTime) > 5)
		{
			spinToCenter(self);
		}
		else if (oldestScanned != null /*&& turnAmountRemaining <= 0*//* && nOldestScanned != oldestScanned*/)
		{
			double relX = oldestScanned.getX()-self.getX();
			double relY = oldestScanned.getY()-self.getY();
			double absBearing = Math.atan2(relX, relY);
			double radarTurnToTarget = robocode.util.Utils.normalRelativeAngle(absBearing - self.getRadarHeadingRadians());
			long timeSinceOldestScan = oldestScanned.getPreviousData().getAge(currentTime);
			double mea = calcSimpleMEA(self.getPosition(), oldestScanned.getPosition(), Rules.MAX_VELOCITY, (int) Math.signum(radarTurnToTarget), timeSinceOldestScan);

			double turnAngle = robocode.util.Utils.normalRelativeAngle(mea - self.getRadarHeadingRadians()); //radarTurnToTarget;
			turnAmountRemaining = Math.abs(turnAngle);
			self.setTurnRadarRightRadians(turnAngle);

/*			if (turnAngle > Math.PI/4)
			{
				self.setTurnRadarRightRadians(Double.POSITIVE_INFINITY*turnAngle);
				//System.out.println("RIGHTMAX");
				//System.out.println(turnAngle);
			}
			else if (turnAngle < -Math.PI/4)
			{
				self.setTurnRadarLeftRadians(Double.POSITIVE_INFINITY*Math.abs(turnAngle));
				//System.out.println("LEFTMAX");
				//System.out.println(turnAngle);

			}
			else *//*if (turnAngle >= 0)*/
			{
				//self.setTurnRadarRightRadians(Math.signum(turnAngle) * Math.ceil(Math.abs(turnAngle) / (Math.PI/4)));
				//System.out.println("RIGHT");
				//System.out.println(turnAngle);
			}
			/*else
			{
				self.setTurnRadarLeftRadians(Math.abs(turnAngle));
				//System.out.println("LEFT");
				//System.out.println(turnAngle);
			}*/


		}


	}
	public static double calcSimpleMEA(Point2D reference, Point2D target, double velocity, int angularDirectionSignnum, long time)
	{
		double relX = target.getX()-reference.getX();
		double relY = target.getY()-reference.getY();
		double bearing = Math.atan2(relX, relY);
		double perpendicular = bearing + Math.PI/2;

		double maxEscapeDistance = velocity * angularDirectionSignnum * time;
		double maxRelEscapeX = maxEscapeDistance * Math.sin(perpendicular) + relX;
		double maxRelEscapeY = maxEscapeDistance * Math.cos(perpendicular) + relY;
		double maxEscapeAngle = Math.atan2(maxRelEscapeX, maxRelEscapeY);

		return maxEscapeAngle;
	}
	public static double calcSimpleMEA(Point2D reference, Point2D target, double velocity, double heading, long time)
	{
		double relX = target.getX()-reference.getX();
		double relY = target.getY()-reference.getY();
		double bearing = Math.atan2(relX, relY);
		double perpendicular = bearing + Math.PI/2;
		double headingOffsetFromBearing = heading - bearing;
		double lateralDirection = Math.signum(normalizeAngle(headingOffsetFromBearing));

		double maxEscapeDistance = velocity * lateralDirection * time;
		double maxRelEscapeX = maxEscapeDistance * Math.sin(perpendicular) + relX;
		double maxRelEscapeY = maxEscapeDistance * Math.cos(perpendicular) + relY;
		double maxEscapeAngle = Math.atan2(maxRelEscapeX, maxRelEscapeY);

		return maxEscapeAngle;
	}


	public static double normalizeAngle(double angle)
	{
		double pi = Math.PI;
		while (angle > pi)
		{
			angle -= pi;
		}
		while (angle <= -pi)
		{
			angle += pi;
		}
		return angle;
	}
/*	public static void oldestScanned(HashMap<String, Enemy> enemies, SleepSiphon self)
	{
		Enemy target = Util.getOldest(enemies, self);



		final double FACTOR = Double.POSITIVE_INFINITY;
		if (target != null)
    	{
			//do this based on max time to scan everyone else, not based on if the current target is still the oldest
			if ( target != oldestScanned)
			{
				oldestScanned = target;
				double absBearing = target.getBearing() + self.getHeadingRadians();
				double direction = robocode.util.Utils.normalRelativeAngle(absBearing - self.getRadarHeadingRadians());
				self.setTurnRadarRightRadians( FACTOR * direction );
			}

    	}
	}*/

/*	public static int oldestScanned(HashMap<String, Enemy> enemies, SleepSiphon self, int numScanned)
	{
		String oldestName = Util.getOldestName(enemies, self);
		Enemy oldest = enemies.get(oldestName);

		final double FACTOR = 5;
		if (oldest != null)
		{
            System.out.println(numScanned);
            System.out.println(oldestName);

				double turnAmount = robocode.util.Utils.normalRelativeAngle(oldest.getBearing() + self.getHeadingRadians() - self.getRadarHeadingRadians());
				self.setTurnRadarRightRadians(FACTOR * turnAmount);
				System.out.println(turnAmount);
				numScanned = 0;

		}
		else
			System.out.println("Oldest is null");
		return numScanned;
	}*/
// end of not my code
}
