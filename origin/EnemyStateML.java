package origin;

public class EnemyStateML {
    private double distance;
    private double heading;
    private double velocity;
    private double deltaHeading;
    private double timeSinceDecel;
    
    private static final double distanceWeight = 1D/500D;
    private static final double headingWeight = 1D/(Math.PI*2);
    private static final double velocityWeight = 1D/8D;
    private static final double deltaHeadingWeight = 1D/(robocode.Rules.MAX_TURN_RATE_RADIANS);
    private static final double timeSinceDecelWeight = 1D/10D;
    

    public EnemyStateML(double d, double h, double v, double pH, long tD)
    {
        distance = d;
        heading = h;
        velocity = v;
        deltaHeading = h-pH;
        timeSinceDecel = (double) tD;
    }
    
    //get
    public double getWeightedDataDistanceSq(EnemyStateML nData)
    {
    	double dist = Math.abs(
    			Math.pow(distance - nData.getDistance(), 2)*distanceWeight +
    			Math.pow(heading - nData.getHeading(), 2)*headingWeight +
    			Math.pow(velocity - nData.getVelocity(), 2)*velocityWeight +
    			Math.pow(deltaHeading - nData.getDeltaHeading(), 2)*deltaHeadingWeight +
    			Math.pow(timeSinceDecel - nData.getTimeSinceDecel(), 2)*timeSinceDecelWeight);//TODO HEY THERE!  IF YOU CAN'T GET THIS TO WORK CONSISTANTLY BY TOMORROW, MAKE A "RANDOM WITHIN MEA" GUN FOR THAT PATTERN BOT & OR PLAY THE "NO SHOOTING" GAME.  MAKE HIM FIRE, WHILE YOU DODGE
    																							// Also, *maybe* make melee wave surfing
    																							//update movement to actually act off of enemy danger ( melee )
    	return dist;
    }
    public double getUnweightedDataDistanceSq(EnemyStateML nData)
    {
    	double dist = Math.abs(
    			Math.pow(distance - nData.getDistance(), 2) +
    			Math.pow(heading - nData.getHeading(), 2) +
    			Math.pow(velocity - nData.getVelocity(), 2) +
    			Math.pow(deltaHeading - nData.getDeltaHeading(), 2) +
    			Math.pow(timeSinceDecel - nData.getTimeSinceDecel(), 2));
    	return dist;
    }
    
    public  double getDistance() {return distance;}
    public  double getHeading() {return heading;}
    public  double getVelocity() {return velocity;}
    public double getDeltaHeading() {return deltaHeading;}
    public double getTimeSinceDecel() {return timeSinceDecel;}
}
