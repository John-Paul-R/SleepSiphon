package move;

import java.awt.geom.Point2D;

class PathPoint extends Point2D {
    
    double x;
    double y;
    double angle; //next movement direction
    double velocity; //target velocity for next travel (equal to dx)

    @Override
    public double getX() {
        return 0;
    }

    @Override
    public double getY() {
        return 0;
    }

    @Override
    public void setLocation(double x, double y) {

    }

}