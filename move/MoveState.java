package move;

import java.awt.geom.Point2D;
import robocode.AdvancedRobot;

public class MoveState extends Point2D {
    private double x, y, heading, velocity;

    public MoveState() {
        x=0;
        y=0;
        heading=0;
        velocity=0;
    }
    public MoveState(AdvancedRobot self) {
        x = self.getX();
        y = self.getY();
        heading = self.getHeadingRadians();
        velocity = self.getVelocity();
    }
    @Override
    public double getX() {
        return x;
    }
    @Override
    public double getY() {
        return y;
    }
    public double getHeading() {
        return heading;
    }
    public double getVelocity() {
        return velocity;
    }
    public void setX(double x) {
        this.x=x;
    }
    public void setY(double y) {
        this.y=y;
    }
    public void setHeading(double heading) {
        this.heading = heading;
    }
    public void setVelocity(double velocity) {
        this.velocity = velocity;
    }
    @Override
    public void setLocation(double x, double y) {
        this.x = x;
        this.y = y;
    }

}