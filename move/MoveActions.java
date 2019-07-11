package move;

import java.awt.geom.Point2D;

import origin.SleepSiphon;

/**
 * Actions
 *
 * Note: These methods are copied fromt the RoboWiki with some minor changes.
 *
 */
public class MoveActions {

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

    public static void aGoTo(SleepSiphon self, Point2D destinationPoint)
	{

        double x = destinationPoint.getX();
        double y = destinationPoint.getY();
		aGoTo(self, x, y);
    }
}