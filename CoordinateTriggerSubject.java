import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class CoordinateTriggerSubject implements PositionSubject {
    private final List<PositionObserver> observers = new ArrayList<>();
    private final Rectangle triggerArea;
    private final String triggerId;
    private boolean wasInside = false;

    public CoordinateTriggerSubject(Rectangle triggerArea, String triggerId) {
        this.triggerArea = triggerArea;
        this.triggerId = triggerId;
    }

    @Override
    public void addObserver(PositionObserver o) {
        observers.add(o);
    }

    @Override
    public void removeObserver(PositionObserver o) {
        observers.remove(o);
    }

    @Override
    public void onPositionChanged(int x, int y) {
        boolean isInside = triggerArea.contains(x, y);
        if (!wasInside && isInside) {
            for (PositionObserver o : observers) {
                o.onEnterTrigger(triggerId);
            }
        } else if (wasInside && !isInside) {
            for (PositionObserver o : observers) {
                o.onExitTrigger(triggerId);
            }
        }
        wasInside = isInside;
    }
}