package move;

import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.List;

import origin.Enemy;
import origin.Paintable;
import origin.SleepSiphon;

abstract class MoveAlgorithm implements Paintable {
    static final int NUM_STEPS = 80; //Number of ticks into the future to plot self movement

    static List<MovePath> latestPaths;

    protected SleepSiphon _self;

    protected void init(SleepSiphon self) {
        _self = self;
    }

    //Path at index 0 is the path that should be taken
    abstract List<MovePath> getPaths(SleepSiphon self, HashMap<String, Enemy> enemies);

    public void paint(Graphics2D g) {
        if (latestPaths != null) {
            for (MovePath path : latestPaths) {
                path.paint(g);
            }
        }
    }
}