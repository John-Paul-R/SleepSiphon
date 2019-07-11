package move;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import origin.Enemy;
import origin.EnemyData;
import origin.EnemyWave;
import origin.SleepSiphon;
import origin.Util;
import origin.WeightedPoint;
import robocode.Rules;
import util.TimedWeightedPoint;

public class MinRiskPathsOld extends MoveAlgorithm {

    public static final int NUM_DESTINATIONS = 16;
    public static final int DESTINATION_RADIUS = 150;
    public static final int NUM_PATH_POINTS = 16;

    private Point2D.Double[] destinations;
    private TimedWeightedPoint[][] paths;
    private Point2D.Double bestDestinationPoint;

    MinRiskPathsOld() {
    }

    @Override
    public List<MovePath> getPaths(SleepSiphon self, HashMap<String, Enemy> enemies) {
        ArrayList<MovePath> out = null;// new ArrayList<MovePath>(1);
        if (self.getTargetEnemy() != null) {
            long cTime = self.getTime();
            // Generate destinations, then paths to those destinations
            Point2D.Double[] destinations = generatePointsCircular(
                new Point2D.Double(self.getX(), self.getY()),
                NUM_DESTINATIONS,
                DESTINATION_RADIUS);
            TimedWeightedPoint[][] paths = generatePaths(self, destinations);

            // Set danger values (weights) for each point on the path and then the path
            // itself
            calcPathRisksByDist_WAVE(paths, self.getEWaves(), 3, cTime);
            calcPathRisksByDist_BOT(paths, self.getTargetEnemy().getLatest(), cTime);
            calcPath_repelWalls(paths);
            calcPath_repelCorners(paths);

            double[] pathRisks = getPathRisks(paths);

            Point2D.Double bestDestinationPoint = destinations[getSafestPathIndex(pathRisks)];
            MoveActions.aGoTo(self, bestDestinationPoint);
            this.destinations = destinations;
            this.paths = paths;
            this.bestDestinationPoint = bestDestinationPoint;
        }

        return out;
    }

    @Override
    public void paint(Graphics2D g) {
        if (bestDestinationPoint != null)
        {
            final int rad = 4;
            double pathAvg = 0;
            
            g.setColor(new Color(0x00, 255, 0x00, 100));

            for (int i = 0; i < paths.length; i++)
            {
                for (int j = 0; j < paths[0].length; j++)
                {
                    TimedWeightedPoint cPoint = paths[i][j];
                    //g.setColor(new Color(Color.HSBtoRGB( (float)cPoint.getWeight(), 1, 1)));
                    int colorNum = ((int)Math.max(0, Math.min(255, cPoint.getWeight()*33)));
                    
                    g.setColor( new Color( (int)(colorNum*(3D/4D)+255/4), -colorNum/4+255/4, -colorNum/4+255/2, 200 ) );
                    g.fillOval((int)cPoint.getX()-rad, (int)cPoint.getY()-rad, rad*2, rad*2);
                    
                    g.setColor( new Color(55, 55, 55, 200) );
                    g.drawOval((int)cPoint.getX()-rad, (int)cPoint.getY()-rad, rad*2, rad*2);

                    pathAvg += cPoint.getWeight();
                    
                }

                pathAvg /= NUM_PATH_POINTS;

                int colorNum = ((int)Math.max(0, Math.min(255, pathAvg*33)));
                g.setColor( new Color( (int)(colorNum*(3D/4D)+255/4), -colorNum/4+255/4, -colorNum/4+255/2, 200 ) );
                g.fillOval((int)destinations[i].getX()-rad, (int)destinations[i].getY()-rad, rad*2, rad*2);

                pathAvg = 0;
            }

            g.setColor(new Color(255, 255, 255, 255));
            g.drawLine((int)_self.getX(), (int)_self.getY(), (int)bestDestinationPoint.getX(), (int)bestDestinationPoint.getY());

        }
    }

    private Point2D.Double[] generatePointsCircular(Point2D center, int numPoints, double HYPOT) {
        double cAngle = 0;
        double angleIncrement = (2 * Math.PI) / (double) numPoints;
        Point2D.Double[] output = new Point2D.Double[numPoints];
        for (int i = 0; i < numPoints; i++) {
            double cX = HYPOT * Math.sin(cAngle) + center.getX();
            double cY = HYPOT * Math.cos(cAngle) + center.getY();
            output[i] = Util.limitCoordinateToMap(cX, cY);
            cAngle += angleIncrement;
        }
        return output;
    }

    private TimedWeightedPoint[][] generatePaths(SleepSiphon self, Point2D[] destinationPoints) {
        int TIME_LIMIT = NUM_PATH_POINTS;

        double baseSelfX = self.getX();
        double baseSelfY = self.getY();

        TimedWeightedPoint[][] paths = new TimedWeightedPoint[destinationPoints.length][TIME_LIMIT];
        for (int cPath = 0; cPath < destinationPoints.length; cPath++) {
            double velocity = Rules.MAX_VELOCITY;

            double selfX = baseSelfX;
            double selfY = baseSelfY;
            double destX = destinationPoints[cPath].getX() - selfX;
            double destY = destinationPoints[cPath].getY() - selfY;

            double targetAbsBearing = Math.atan2(destX, destY);

            int simTime = 0;
            while (simTime < TIME_LIMIT) {
                selfX += velocity * Math.sin(targetAbsBearing);
                selfY += velocity * Math.cos(targetAbsBearing);

                TimedWeightedPoint point = new TimedWeightedPoint(selfX, selfY, 0, simTime + 1);
                Util.changeCoordinateToMap(point);
                paths[cPath][simTime] = point;
                simTime += 1;
            }
        }
        return paths;
    }

    private double[] calcPathRisksByDist_WAVE(TimedWeightedPoint[][] paths, LinkedList<EnemyWave> waveSet,
            int maxPointsPerWave, long currentTime) {
        int NUMPATHS = paths.length;
        int NUMINTERVALS = paths[0].length;
        double[] pathRisks = new double[paths.length];
        for (int t = 0; t < NUMINTERVALS; t++) {
            // calc wave bullet locs
            WeightedPoint[][] bulletLocs = new WeightedPoint[waveSet.size()][maxPointsPerWave];// Use weighted points so
                                                                                               // that we can determine
                                                                                               // risk of each potential
                                                                                               // bullet location

            for (int i = 0; i < bulletLocs.length; i++) {
                Point2D.Double[] locs = (waveSet.get(i).getBulletLocations(currentTime + paths[0][t].getTime()));
                for (int j = 0; j < maxPointsPerWave; j++) {
                    bulletLocs[i][j] = new WeightedPoint(locs[j]);
                }
            }

            // loop to find & set risk of each point in this ring and add it to total for
            // that path(time interval)
            for (int i = 0; i < NUMPATHS; i++) {
                TimedWeightedPoint cPoint = paths[i][t];

                for (int waveIndex = 0; waveIndex < waveSet.size(); waveIndex++) {
                    for (int wavePointIndex = 0; wavePointIndex < maxPointsPerWave; wavePointIndex++) {
                        cPoint.addWeight(1 / Point2D.distance(cPoint.getX(), cPoint.getY(),
                                bulletLocs[waveIndex][wavePointIndex].getX(),
                                bulletLocs[waveIndex][wavePointIndex].getY()) * 10);// if bulletCircle intersects with
                                                                                    // myPredictedLoc rectangle, risk =
                                                                                    // 1
                    }
                }
                pathRisks[i] += cPoint.getWeight();
            }

        }

        return pathRisks;
    }

    private double[] calcPathRisksByDist_BOT(TimedWeightedPoint[][] paths, EnemyData eData, long currentTime) {
        int NUMPATHS = paths.length;
        int NUMINTERVALS = paths[0].length;
        double eX = eData.getX();
        double eY = eData.getY();
        double[] pathRisks = new double[paths.length];
        for (int t = 0; t < NUMINTERVALS; t++) {
            // loop to find & set risk of each point in this ring and add it to total for
            // that path(time interval)
            for (int i = 0; i < NUMPATHS; i++) {
                TimedWeightedPoint cPoint = paths[i][t];
                cPoint.addWeight(1 / Point2D.distanceSq(cPoint.getX(), cPoint.getY(), eX, eY) * 1000);
                pathRisks[i] += cPoint.getWeight();
            }

        }
        return pathRisks;
    }

    private double[] calcPath_repelWalls(TimedWeightedPoint[][] paths) {
        int NUMPATHS = paths.length;
        int NUMINTERVALS = paths[0].length;
        final double[] bounds = Util.getAbsoluteFieldBoundsxXyY();
        final double RISK_MULTIPLIER = 10;
        double[] pathRisks = new double[paths.length];
        for (int t = 0; t < NUMINTERVALS; t++) {
            // loop to find & set risk of each point in this ring and add it to total for
            // that path(time interval)
            for (int i = 0; i < NUMPATHS; i++) {
                TimedWeightedPoint cPoint = paths[i][t];

                cPoint.addWeight(1 / Point2D.distance(cPoint.getX(), 0, bounds[0], 0) * RISK_MULTIPLIER);
                cPoint.addWeight(
                        1 / Point2D.distance(cPoint.getX(), bounds[2], bounds[1], bounds[2]) * RISK_MULTIPLIER);
                cPoint.addWeight(1 / Point2D.distance(0, cPoint.getY(), 0, bounds[2]) * RISK_MULTIPLIER);
                cPoint.addWeight(
                        1 / Point2D.distance(bounds[1], cPoint.getY(), bounds[1], bounds[3]) * RISK_MULTIPLIER);
                pathRisks[i] += cPoint.getWeight();
            }

        }
        return pathRisks;
    }

    private double[] calcPath_repelCorners(TimedWeightedPoint[][] paths) {
        int NUMPATHS = paths.length;
        int NUMINTERVALS = paths[0].length;
        final double[] bounds = Util.getAbsoluteFieldBoundsxXyY();
        final double RISK_MULTIPLIER = 500;
        double[] pathRisks = new double[paths.length];
        for (int t = 0; t < NUMINTERVALS; t++) {
            // loop to find & set risk of each point in this ring and add it to total for
            // that path(time interval)
            for (int i = 0; i < NUMPATHS; i++) {
                TimedWeightedPoint cPoint = paths[i][t];

                cPoint.addWeight(
                        1 / Point2D.distanceSq(cPoint.getX(), cPoint.getY(), bounds[0], bounds[2]) * RISK_MULTIPLIER);
                cPoint.addWeight(
                        1 / Point2D.distanceSq(cPoint.getX(), cPoint.getY(), bounds[0], bounds[3]) * RISK_MULTIPLIER);
                cPoint.addWeight(
                        1 / Point2D.distanceSq(cPoint.getX(), cPoint.getY(), bounds[1], bounds[2]) * RISK_MULTIPLIER);
                cPoint.addWeight(
                        1 / Point2D.distanceSq(cPoint.getX(), cPoint.getY(), bounds[1], bounds[3]) * RISK_MULTIPLIER);
                pathRisks[i] += cPoint.getWeight();
            }

        }
        return pathRisks;
    }

    private double[] getPathRisks(TimedWeightedPoint[][] paths) {
        double[] result = new double[paths.length];
        for (int i = 0; i < paths.length; i++) {
            for (int j = 0; j < paths[i].length; j++) {
                result[i] += paths[i][j].getWeight();
            }
        }
        return result;
    }

    private int getSafestPathIndex(double[] pathRisks) {
        // iterate through the path risks array to find the path with least risk. Return
        // that path's index
        int safestPathIndex = 0;
        double safestRiskValue = Double.POSITIVE_INFINITY;
        for (int i = 0; i < pathRisks.length; i++) {
            if (pathRisks[i] <= safestRiskValue) {
                safestPathIndex = i;
                safestRiskValue = pathRisks[i];
            }
        }
        return safestPathIndex;
    }

}