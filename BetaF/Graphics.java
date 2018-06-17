package BetaF;
import java.awt.Color;
public class Graphics
{
	private static Color goal = new Color(255, 255, 255);
	private static Color current = new Color(20, 10, 5);
	
	final static int brightness = 100; //max 255
	
	private static int genColorNum() //generates a rancom color component (integer) with a minimum brightness 'brightness'
	{
		return (int)(Math.random()*(255-brightness)+brightness);
	}
	private static Color genColor() //generates a random color with minimum brightness 'brightness'
	{
		return new Color(genColorNum(), genColorNum(), genColorNum());
	}
	
	private static double ratio = 1; //cycles from 1.0 to 1.0
	public static Color cycleRandomColors()
	{
		
		if (ratio <= 0)
		{
			current  = goal;
			goal = genColor();
			ratio = 1;
			if (current == null)
				current = genColor();
		}

		int red = (int)Math.abs((ratio * current.getRed()) + ((1 - ratio) * goal.getRed()));
		int green = (int)Math.abs((ratio * current.getGreen()) + ((1 - ratio) * goal.getGreen()));
		int blue = (int)Math.abs((ratio * current.getBlue()) + ((1 - ratio) * goal.getBlue()));
		ratio -= .05;
		return new Color(red, green, blue);	
	}
	
	private static float c = -1;
	public static Color spectrumCycleHSB()
	{
		if (c > 255)
			c = 0;
		c+=.01;
		//System.out.println(c);
		return new Color(Color.HSBtoRGB(c, (float)1, (float)1));
		
	}
	
	final static int gunConstant = 15;
	private static int gunCooldownCounter = gunConstant;
	public static Color gunFire(double gunHeat)
	{
		if (gunHeat <= 0)
			gunCooldownCounter = gunConstant;
		if (gunCooldownCounter > 0)
			gunCooldownCounter-=1;
		//System.out.println(gunCooldownCounter);
		return new Color((int)((255/gunConstant)* gunCooldownCounter), (int)((255/gunConstant)* gunCooldownCounter), (int)((255/gunConstant)* gunCooldownCounter));
		
	}
}