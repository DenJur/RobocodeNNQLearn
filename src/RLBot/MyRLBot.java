package RLBot;

import robocode.*;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.File;

public class MyRLBot extends AdvancedRobot {
    public static final double PI = Math.PI;
    private Target enemy;
    private static NeuralNetTrain nnrlTrainer;
    private static double imReward = 0.0;
    private double firePower;
    public static double[] nnInputs = new double[Action.NumberStats+Action.NumActions];
    private final String weightsFile = "nnWeight.txt";
    private final File wFile = new File(weightsFile);
    private final boolean exists=wFile.exists();

    public void run() {
        /* robocode colors */
        setBodyColor(new Color(128, 128, 50));
        setGunColor(new Color(50, 50, 20));
        setRadarColor(Color.RED);
        setScanColor(Color.WHITE);
        setBulletColor(Color.PINK);

        /* RL NN trainer object */

        if(nnrlTrainer==null && exists)
            nnrlTrainer = new NeuralNetTrain(true);
        else if(nnrlTrainer==null)
            nnrlTrainer= new NeuralNetTrain(false);
        enemy = new Target();
        /* init of RLBot & enemey distance */
        enemy.distance = 1000;

        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);
        turnRadarRightRadians(2 * PI);

        while (true) {
            firePower = 300.0 / enemy.distance;
            if (firePower > 3.0)
                firePower = 3.0;
            setupRadarScan();
            setupGunMove();
            setupRobotMovement(firePower);
            /* give control back to robocode manager */
            execute();
        }
    }

    private void setupRobotMovement(double pwr) {
        getNNInputs();
//        nnrlTrainer.nnTrain(nnInputs, imReward);

        imReward = 0.0;

        switch (nnrlTrainer.selectAction(nnInputs)) {
            case Action.RobotAhead:
                setAhead(Action.RobotMoveDistance);
                break;
            case Action.RobotBack:
                setBack(Action.RobotMoveDistance);
                break;
            case Action.RobotTurnLeft:
                setTurnLeft(Action.RobotTurnDegree);
                break;
            case Action.RobotTurnRight:
                setTurnRight(Action.RobotTurnDegree);
                break;
//            case Action.RobotFire:
//                gunFire(pwr);
//                break;
        }
    }

    private void getNNInputs() {
        for(int i=0;i<nnInputs.length; i++)
            nnInputs[i]=-1;

        double[] encoded=NeuralNetState.getEnergy(getEnergy());
        System.arraycopy(encoded, 0, nnInputs, 0, encoded.length);
        encoded=NeuralNetState.getEnergy(enemy.energy);
        System.arraycopy(encoded, 0, nnInputs, 5, encoded.length);
//        encoded=NeuralNetState.getHeat(getGunHeat());
//        System.arraycopy(encoded, 0, nnInputs, 10, encoded.length);
        encoded=NeuralNetState.getTargetDistance(enemy.distance);
        System.arraycopy(encoded, 0, nnInputs, 10, encoded.length);
        encoded=NeuralNetState.getX(getX());
        System.arraycopy(encoded, 0, nnInputs, 20, encoded.length);
        encoded=NeuralNetState.getY(getY());
        System.arraycopy(encoded, 0, nnInputs, 28, encoded.length);
        encoded=NeuralNetState.getHeading(getHeading());
        System.arraycopy(encoded, 0, nnInputs, 34, encoded.length);
//        encoded=NeuralNetState.getTargetBearing(enemy.bearing);
//        System.arraycopy(encoded, 0, nnInputs, 34, encoded.length);
        int action=nnrlTrainer.selectAction(nnInputs);
        nnInputs[Action.NumberStats+action]=1;
    }

    private void gunFire(double pwr) {
        if (getGunHeat() == 0)
            setFire(pwr);
    }

    private void setupRadarScan() {
        double radarOffset;
        radarOffset = getRadarHeadingRadians() -
                (PI / 2 - Math.atan2(enemy.y - getY(), enemy.x - getX()));
        if (radarOffset > PI)
            radarOffset -= 2 * PI;
        if (radarOffset < -PI)
            radarOffset += 2 * PI;
        if (radarOffset < 0)
            radarOffset -= PI / 10;
        else
            radarOffset += PI / 10;
        // turn the radar
        setTurnRadarLeftRadians(radarOffset);
    }

    /*
     * Move the gun to the predicted next bearing of the enemy.
     */
    private void setupGunMove() {

        long time;
        long nextTime;
        Point2D.Double p;
        p = new Point2D.Double(enemy.x, enemy.y);
        for (int i = 0; i < 20; i++) {
            nextTime = (int) Math.round((getRange(getX(), getY(), p.x, p.y) / (20 - (3 * firePower))));
            time = getTime() + nextTime - 10;
            p = enemy.targetEnemy(time);
        }
        //offsets the gun by the angle to the next shot based on linear targeting provided by the enemy class
        double gunOffset = getGunHeadingRadians() -
                (PI / 2 - Math.atan2(p.y - getY(), p.x - getX()));
        setTurnGunLeftRadians(normaliseBearing(gunOffset));
    }

    /**
     * If a bearing is not within the -pi to pi range,
     * alters it to provide the shortest angle.
     *
     * @param ang The original angle.
     * @return The shortest angle.
     */
    double normaliseBearing(double ang) {
        if (ang > PI)
            ang -= 2 * PI;
        if (ang < -PI)
            ang += 2 * PI;
        return ang;
    }

    /**
     * Returns the distance between two x,y coordinates.
     *
     * @param x1 First x.
     * @param y1 First y.
     * @param x2 Second x.
     * @param y2 Second y.
     * @return The distance between (x1, y1) and (x2, y2).
     */
    public double getRange(double x1, double y1, double x2, double y2) {
        double xo = x2 - x1;
        double yo = y2 - y1;
        double h = Math.sqrt(xo * xo + yo * yo);
        return h;
    }

    public void onBulletHit(BulletHitEvent e) {
        // big reward when hitting the target
        imReward += e.getBullet().getPower() * 3.0;
    }

    public void onBulletHitBullet(BulletHitBulletEvent e) {
        // this means bad which I am on the target line of enemy...
        imReward += -3.0;
    }

    public void onDeath(DeathEvent event)
    {
        imReward += -100.0;
    }

    public void onWin(WinEvent event)
    {
        imReward += 100.0;
    }

    public void onBulletMissed(BulletMissedEvent e) {
//        imReward += -e.getBullet().getPower();
    }

    public void onHitByBullet(HitByBulletEvent e) {
        imReward += -e.getBullet().getPower() * 3.0;
    }

    public void onHitRobot(HitRobotEvent e) {
        imReward += -3.0;
    }

    public void onHitWall(HitWallEvent e) {
        imReward += -3.0;
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        /* only update enemey status when enemy distance is
         * less than last reported enemey distance
         * this may not be a good practice */
        double absbearing_rad = (getHeadingRadians() + e.getBearingRadians()) % (2 * PI);
        double h = normaliseBearing(e.getHeadingRadians() - enemy.head);
        h = h / (getTime() - enemy.ctime);
        enemy.changehead = h;
        enemy.x = getX() + Math.sin(absbearing_rad) * e.getDistance();
        enemy.y = getY() + Math.cos(absbearing_rad) * e.getDistance();
        enemy.bearing = e.getBearingRadians();
        enemy.head = e.getHeadingRadians();
        enemy.name = e.getName();
        enemy.ctime = getTime();
        enemy.speed = e.getVelocity();
        enemy.distance = e.getDistance();
        enemy.energy = e.getEnergy();
        gunFire(firePower);
    }

    public void onRoundEnded(RoundEndedEvent event) {
//        if (NeuralNetTrain.EPSILON > 0.3) {
//            NeuralNetTrain.EPSILON -= 0.00001;
//        }
//        nnrlTrainer.nnSave();
        enemy.distance = 1000;
    }

}
