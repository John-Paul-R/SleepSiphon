package BetaF;
public class EnemyData implements BotData
{
    private double energy;
    private double bearing;
    private double distance;
    private double heading;
    private double velocity;
    private int time;
    private double x;
    private double y;
    private double prevHeading;
    private long timeSinceDecel;
    private int scanOrder;
    
    public EnemyData(double e, double b, double d, double h, double v, int t, double selfX, double selfY, double selfHeading, double pH, double pV, long tD, int so)
    {
        energy = e;
        bearing = b;
        distance = d;
        heading = h;
        velocity = v;
        time = t;
        x = (selfX + Math.sin((selfHeading + b) % (2*Math.PI)) * d);
        y = (selfY + Math.cos((selfHeading + b) % (2*Math.PI)) * d);
        prevHeading = pH;
        if (pV == v)
        {
        	timeSinceDecel = 0;
        }
        else
        {
        	timeSinceDecel = tD +1;
        }
        scanOrder = so;
        
    }
    
    //get
    public  double getEnergy() {return energy;}
    public  double getBearing() {return bearing;}
    public  double getDistance() {return distance;}
    public  double getHeading() {return heading;}
    public  double getVelocity() {return velocity;}
    public  long getTime() {return time;}
    public  double getX() {return x;}
    public  double getY() {return y;}
    public  long getTimeSinceDecel() {return timeSinceDecel;}
    public long getAge(long currentTime) {return currentTime - time;}

	@Override
	public double getLatestTurnRate() {
		return heading-prevHeading;
	}

	public int getScanOrder() {
		// TODO Auto-generated method stub
		return scanOrder;
	}
    
} 