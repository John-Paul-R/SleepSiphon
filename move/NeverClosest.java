package move;

import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.List;

import origin.Enemy;
import origin.SleepSiphon;

class NeverClosest extends MoveAlgorithm {

    NeverClosest() {

    }

    @Override
    public List<MovePath> getPaths(SleepSiphon self, HashMap<String, Enemy> enemies) {
        return null;
    }

    @Override
    public void paint(Graphics2D g) {

    }

}