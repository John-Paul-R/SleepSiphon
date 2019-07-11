package move;

import java.awt.Graphics2D;
import java.util.List;

import origin.Paintable;
import origin.SleepSiphon;
import util.PositionedVector2D;

public class MoveActor implements Paintable {

    SleepSiphon _self;
    MoveAlgorithm _mode;

    public MoveActor(SleepSiphon self) {
        _self = self;
    }

    public void execute() {
        List<MovePath> paths = _mode.getPaths(_self, _self.getEnemies());
        //A MoveAlgorithm's 'getPaths' method will return null if it tells SleepSiphon to move directly.
        //Otherwise, use the generated paths to give SleepSiphon movement instructions:
        if (paths != null) {
            PositionedVector2D action = paths.get(0).getAction();
            //System.out.println(action.getMagnitude());
            //System.out.println(_self.getVelocity());
            _self.setTurnRightRadians(action.getAngle()-_self.getHeadingRadians());
            _self.setMaxVelocity(action.getMagnitude());
            _self.setAhead(1000);
            
            //MoveActions.aGoTo(_self, destination);
        }
        
    }

    public void setMoveMode(int mode) {

        switch (mode) {
        case 0:
            _mode = new MinRiskPaths();
            break;
        case 1:
            _mode = new MinRiskPathsOld();
            break;
        case 2:
            _mode = new RandomMapwide();
            break;
        case 3:
            _mode = new Spin();
            break;
        case 4:
            _mode = new FourCorners();
            break;
        default:
            new MinRiskPaths();
            break;
        }

        _mode.init(_self);
    }

    @Override
    public void paint(Graphics2D g) {
        _mode.paint(g);

    }
}