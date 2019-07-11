package move;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.List;

import origin.Enemy;
import origin.SleepSiphon;

class RandomMapwide extends MoveAlgorithm {

    private double destinationX;
    private double destinationY;

    RandomMapwide() {

    }

    @Override
    public List<MovePath> getPaths(SleepSiphon self, HashMap<String, Enemy> enemies) {
        final int botDim = (int) self.getHeight();
        
        if (self.getDistanceRemaining() == 0) {
            destinationX = botDim + (int)(Math.random() * self.getBattleFieldWidth()-2*botDim);
            destinationY = botDim + (int)(Math.random() * self.getBattleFieldHeight()-2*botDim);
            MoveActions.goTo(
                self,
                destinationX,
                destinationY);
        }
        return null;
    }

    @Override
    public void paint(Graphics2D g) {
        g.setColor(new Color(255, 0, 0, 150));
        g.fillOval((int)destinationX-8, (int)destinationY-8, 16, 16);
    }

}