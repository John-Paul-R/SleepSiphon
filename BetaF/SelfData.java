package BetaF;

public class SelfData implements BotData
{
    private double gunHeading; // Absolute, non-normalized (between 0 & 2pi)
    private double heading;
    private double velocity;
    private long time;  //Time is now the 'key' value for the ConcurrentHashMap
    private double x;
    private double y;
    private double prevHeading;

    public SelfData(double gunAbsAngle, double h, double v, double X, double Y, long t, double pH)
    {
        gunHeading = gunAbsAngle;
        heading = h;
        velocity = v;
        //time = t;
        x = X;
        y = Y;
        time = t;
        prevHeading = pH;
    }
    
    //get
    public double getGunHeading() {return gunHeading;}
    public double getHeading() {return heading;}
    public double getVelocity() {return velocity;}
    //public int getTime() {return time;}
    public double getX() {return x;}
    public double getY() {return y;}

	
	public long getTime() {return time;}

	
	public long getAge(long currentTime) {return currentTime - time ;}
	public double getLatestTurnRate()
	{
		return heading-prevHeading;
	}
}
