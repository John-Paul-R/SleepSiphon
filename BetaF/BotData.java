package BetaF;
public interface BotData
{    
    //get
    public double getHeading();
    public double getVelocity();
    public long getTime();
    public double getX();
    public double getY();
    public long getAge(long currentTime);
    public double getLatestTurnRate();
    
} 