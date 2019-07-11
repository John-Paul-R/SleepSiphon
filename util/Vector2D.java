package util;

import java.awt.geom.Point2D;

public class Vector2D {

    private double x;
    private double y;
    private double angle;
    private double magnitude;

    public Vector2D() {
        this.x = 0;
        this.y = 0;
        this.angle = 0;
        this.magnitude = 0;
    }
    public Vector2D(Point2D xyComponents) {
        this.x = xyComponents.getX();
        this.y = xyComponents.getY();

        this.angle = Math.atan2(y, x);
        this.magnitude = Math.sqrt(x*x + y*y);
    }
    public Vector2D(double angle, double magnitude) {
        
        this.angle = angle;
        this.magnitude = magnitude;

        this.x = magnitude*Math.cos(angle);
        this.y = magnitude*Math.sin(angle);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public double getMagnitude() {
        return magnitude;
    }

    public void setMagnitude(double magnitude) {
        this.magnitude = magnitude;
    }
}