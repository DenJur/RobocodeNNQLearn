package RLBot;

import java.awt.geom.Point2D;

class Target {
    String name;
    public double bearing;
    public double head;
    public long ctime;
    public double speed;
    public double x, y;
    public double distance;
    public double changehead;
    public double energy;

    public Point2D.Double targetEnemy(long when) {
        double diff = when - ctime;
        double newY, newX;
        newY = y + Math.cos(head) * speed * diff;
        newX = x + Math.sin(head) * speed * diff;
        return new Point2D.Double(newX, newY);
    }
}