package move;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.List;

import origin.Enemy;
import origin.SleepSiphon;
import origin.Util;

class FourCorners extends MoveAlgorithm {

    private double destinationX;
    private double destinationY;

    FourCorners() {

    }

    @Override
    public List<MovePath> getPaths(SleepSiphon self, HashMap<String, Enemy> enemies) {
        
        final int fw = (int) self.getBattleFieldWidth();
    	final int fh = (int) self.getBattleFieldHeight();
    	final int distFromWalls = 40;
    	//final int allowedError = 5;
    	int x = (int) self.getX();
    	int y = (int) self.getY();

    	if (self.getDistanceRemaining() == 0)
    	{

    		int section = 0;
    		section = Util.currentSection2DUniform(4, x, y, fw, fh);
    		switch (section)
    		{
    			case -1 : System.out.println("Error: invalid section");
    			break;
	    		case 0 : destinationX = distFromWalls; destinationY = fh-distFromWalls;//goto top left
	    		break;
	    		case 1 : destinationX = fw-distFromWalls; destinationY = fh-distFromWalls;//goto top right
	    		break;
	    		case 2 : destinationX = distFromWalls; destinationY = distFromWalls;//goto bottom left
	    		break;
	    		case 3 : destinationX = fw-distFromWalls; destinationY = distFromWalls;// goto bottom right
	    		break;
    		}
    		MoveActions.goTo(self, destinationX, destinationY);

    	}
    	//return new WeightedPoint[] {new WeightedPoint(destinationX, destinationY)};
        
        return null;
    }

    @Override
    public void paint(Graphics2D g) {
        g.setColor(new Color(255, 0, 0, 150));
        g.fillOval((int)destinationX-8, (int)destinationY-8, 16, 16);
    }

}