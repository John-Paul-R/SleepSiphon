package move;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import origin.Paintable;
import util.PositionedVector2D;

class MovePath implements Paintable {

    private ArrayList<PositionedVector2D> points;
    private PositionedVector2D action;

    MovePath(double x, double y, double heading, double velocity) {
        points = new ArrayList<PositionedVector2D>(MoveAlgorithm.NUM_STEPS);
        action = new PositionedVector2D(x, y, heading, velocity);
        points.add(action);
    }
    MovePath() {
        points = new ArrayList<PositionedVector2D>(MoveAlgorithm.NUM_STEPS);
    }

    void add(double x, double y, double heading, double velocity) {
        points.add(new PositionedVector2D(x, y, heading, velocity));
    }

    PositionedVector2D getAction() {
        if (action == null)
            action = points.get(0);
        return action;
    }

    @Override
    public void paint(Graphics2D g) {
        int[] xs = new int[points.size()+1];
        int[] ys = new int[points.size()+1];
        int index = 0;
        g.setColor(Color.WHITE);
        for (PositionedVector2D p : points) {
            xs[index] = (int)p.getX();
            ys[index] = (int)p.getY();
            //g.drawOval(xs[index]-8, ys[index]-8, 16, 16);
            index++;
        }
        Point2D endpoint = points.get(index-1).project();
        xs[index] = xs[index-1] + (int)endpoint.getX();
        ys[index] = ys[index-1] + (int)endpoint.getY();
        g.drawPolyline(xs, ys, points.size()+1);
    }
    
    
	
}