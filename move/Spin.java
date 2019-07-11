package move;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import origin.Enemy;
import origin.SleepSiphon;
import robocode.Rules;

class Spin extends MoveAlgorithm {

    private static final double VELOCITY = 5;

    Spin() {

    }

    @Override
    public List<MovePath> getPaths(SleepSiphon self, HashMap<String, Enemy> enemies) {
        ArrayList<MovePath> out = new ArrayList<MovePath>(1);
        double velocity = self.getVelocity();

        double heading = self.getHeadingRadians();
        double x = self.getX();
        double y = self.getY();
        // prefer target velocity over target turn rate
        double turnRate;
        // FAILSAFE:
        if (velocity == 0) {
            velocity = 8;
        }
        // double nextTurnRate = Rules.getTurnRate(VELOCITY);
        // double nextVelocity = Math.signum(velocity)*Math.min(8,
        // Rules.ACCELERATION+Math.signum(velocity)*velocity);
        MovePath path = new MovePath();
        for (int i = 0; i < MoveAlgorithm.NUM_STEPS; i++) {
            turnRate = Rules.getTurnRateRadians(velocity);
            heading += turnRate;
            velocity = Math.signum(velocity) * Math.min(VELOCITY, 1.0D + Math.signum(velocity) * velocity);
            x = x + velocity * Math.sin(heading);
            y = y + velocity * Math.cos(heading);
            path.add(x, y, heading, velocity);
        }

        out.add(path);
        return out;
    }

    @Override
    public void paint(Graphics2D g) {

    }

}