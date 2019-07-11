package origin;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

public class WaveManager implements Paintable {

    SleepSiphon _self;

    public WaveManager(SleepSiphon self) {
        _self = self;
    }

    public void updateWaves() {
        HashMap<String, Enemy> currentEnemies = _self.getEnemies();
        LinkedList<SelfData> selfDataHistory = _self.getSelfDataHistory();
        LinkedList<EnemyWave> eWaves = _self.getEWaves();
        long cTime = _self.getTime();
        double selfX = _self.getX();
        double selfY = _self.getY();

        for (Entry<String, Enemy> entry : currentEnemies.entrySet()) {
            String cName = entry.getKey(); // This will be changed for melee
            Enemy cEnemy = entry.getValue();
            boolean crashedEnemy = false;
            boolean crashedWall = false;
            boolean hitByBullet = false;
            if (cEnemy.getClosestDist() < 50)
                System.out.println(cName + " " + cEnemy.getClosestDist());
            if (cEnemy.getClosestDist() < 50)
                crashedEnemy = true;
            if (cEnemy.getDistFromWall() < 20 && cEnemy.getVelocity() == 0)
                crashedWall = true;

            if (cEnemy != null && cEnemy.numEntries() >= 3 && !crashedEnemy && !crashedWall && !hitByBullet
                    && !cEnemy.getUpdatedWaves()) {// I might have to turn this all back a turn to wait for event data
                ArrayList<EnemyData> eData = cEnemy.getDataSet();
                int eDataSize = eData.size();
                double eDmgTaken = cEnemy.getEnergyLostThisTurn();// This is actually Last Turn
                double eEnergyGained = cEnemy.getEnergyGainedThisTurn();
                // double eEnergyDropBetweenTurns =
                // Math.abs(eData.get(eDataSize-1).getEnergy()-eData.get(eDataSize-2).getEnergy());
                double eEnergyDiffBetweenTurns = cEnemy.getEnergy() - cEnemy.getPreviousData().getEnergy();
                double eBulletPower = Math.abs(eEnergyDiffBetweenTurns - eDmgTaken + eEnergyGained);
                // if (eEnergyDiffBetweenTurns < -eDmgTaken + eEnergyGained) //Add additional
                // condition to subtract the damage enemy took from ramming into wall (as per
                // wall hit detection function I'll write later)
                if (eEnergyDiffBetweenTurns != 0.0) {
                    // System.out.println("Energy Difference: " + eEnergyDiffBetweenTurns);
                    // System.out.println("Bullet Power: " + eBulletPower);
                }
                if (eEnergyDiffBetweenTurns < 0 && eEnergyDiffBetweenTurns > -3.1)
                    eWaves.add(new EnemyWave(cEnemy.getPreviousData().getX(), cEnemy.getPreviousData().getY(),
                            cTime - 1, Math.abs(eBulletPower), selfDataHistory.get((int) selfDataHistory.size() - 2),
                            cName));
                // System.out.println("DATA
                // AGE\t"+selfDataHistory.get((int)cTime).getAge(cTime));
                // System.out.println("CURRENT TIME\t"+cTime);
                // System.out.println(eEnergyDiffBetweenTurns +"\t" + eDmgTaken+"\t"
                // +eEnergyGained);

                cEnemy.setEnergyLost(0);
                cEnemy.setEnergyGained(0);
                cEnemy.setUpdatedWaves(true);

            }
            EnemyWave closestWave = null;
            double closestDist = Double.POSITIVE_INFINITY;
            for (int i = 0; i < eWaves.size(); i++)// increments a counter when waves are close, then removes waves once
                                                   // counter fills to a predefined amount in the EnemyWave class
            {
                EnemyWave cWave = eWaves.get(i);
                double cDist = cWave.getDist(selfX, selfY, cTime);
                if (cDist < closestDist) {
                    closestDist = cDist;
                    closestWave = cWave;
                }
                if (cDist < 40)
                    cWave.setClose();

                if (cWave.getClose() <= 0) {
                    eWaves.remove(i);
                    i--;
                }
                // System.out.println("WAVE IS NEAR");
            }
        }
    }

    @Override
    public void paint(Graphics2D g) {

        HashMap<String, Enemy> currentEnemies = _self.getEnemies();
        LinkedList<EnemyWave> eWaves = _self.getEWaves();
        int totalNumEnemies = _self.getOthers();
        long cTime = _self.getTime();

        for (EnemyWave wave : eWaves)
        {
            Enemy waveSource = currentEnemies.get(wave.getSource());
            if (waveSource != null)
            {
                float hueValue = (float) ((double)(waveSource.getNumAddedID())/(double)totalNumEnemies);
                Color rgb = new Color(Color.HSBtoRGB(hueValue, 1F, 1F));
                Color rgba = new Color(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), 100);
                g.setColor(rgba);

                Ellipse2D.Double circle = wave.getCircle(cTime);
                g.drawOval((int)circle.getX(), (int)circle.getY(), (int)circle.getWidth(), (int)circle.getHeight());

                //g.drawString(wave.getSource(), (int)circle.getX(), (int)circle.getY());
                Point2D.Double[] bulletLocs = wave.getBulletLocations(cTime);
                for (int j = 0; j < bulletLocs.length; j++)
                {
                    g.setColor(new Color(Color.HSBtoRGB((float)((1.0/bulletLocs.length)*(j+1)), 1, 1)));
                    int size1 = 8, size2 = 10;
                    int cBulletX = (int)bulletLocs[j].getX();
                    int cBulletY = (int)bulletLocs[j].getY();

                    g.drawOval(cBulletX-size1/2, cBulletY-size1/2, size1, size1);
                    //g.drawString(Integer.toString(j), cBulletX-10+count*5, cBulletY-10);
                    g.setColor(new Color(255, 255, 255, 100));
                    g.fillOval(cBulletX-size2/2, cBulletY-size2/2, size2, size2);
                }
            }
        }
        //System.out.println("Painting " + eWaves.size() + " Waves...");
    }
    
}