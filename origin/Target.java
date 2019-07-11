package origin;

import java.util.Map;
import java.util.HashMap;

public class Target
{
	public static String closest(HashMap<String, Enemy> enemies)
    {
        String cClosestName = "";
        double cClosestDist = Integer.MAX_VALUE;
        for (Map.Entry<String,Enemy> entry : enemies.entrySet())
        {
            if (entry.getValue().getDistance() < cClosestDist)
            {
                cClosestName = entry.getKey();
                cClosestDist = entry.getValue().getDistance();
            }
        }
        //System.out.println("Targeting: " + cClosestName);
        return cClosestName;
    }
}
