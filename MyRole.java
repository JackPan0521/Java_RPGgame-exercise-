import java.awt.event.*;
import game.framework.*; 
public class MyRole extends SampleRole5
{
    IMoveState mvState = null;
    private final int jumpSpeed;
    private final PositionSubject triggerSubject;
    
    public MyRole(int x, int y, int w, int h ,int jvx, int jvy, int bottom, ImageSequence[][] is,
            PositionSubject triggerSubject) {
        this.x = x; this.y = y; this.w = w; this.h = h; this.jvx = jvx; this.jvy = jvy; this.bottom = bottom;
        this.is= is;
        this.mvState = new Stop();
        this.jumpSpeed = jvy;
        this.triggerSubject = triggerSubject;
    }
    
    @Override
    public void run() {
        if (mvState != null) {
            mvState.move(this);
        }

        if (mvState instanceof Jumper && !jumpAbility) {
            if (dx != 0 || dy != 0) {
                dim1 = 1;
                if (dx > 0) {
                    dim2 = 0;
                    dir = 0;
                } else if (dx < 0) {
                    dim2 = 1;
                    dir = 1;
                } else if (dy < 0) {
                    dim2 = 2;
                } else if (dy > 0) {
                    dim2 = 3;
                }
                setMoveState(new Walk());
            } else {
                dim1 = 0;
                dim2 = 0;
                setMoveState(new Stop());
            }
        }

        if (mvState instanceof SpecialMove && ((SpecialMove) mvState).isFinished()) {
            dx = 0;
            dy = 0;
            dim1 = 0;
            dim2 = 0;
            setMoveState(new Stop());
        }

        if (mvState instanceof FallDown && ((FallDown) mvState).isFinished()) {
            dx = 0;
            dy = 0;
            dim1 = 0;
            dim2 = 0;
            setMoveState(new Stop());
        }

        // 邊界阻擋
        int screenW = 1080, screenH = 720;
        if (x < -10) x = -10;
        if (y < 0) y = 0;
        if (x > screenW - w + 10) x = screenW - w +10;
        if (y > screenH - h) y = screenH - h;

        if (model != null) {
            model.setState(x, y);
        }

        if (triggerSubject != null) {
            int centerX = x + w / 2;
            int centerY = y + h / 2;
            triggerSubject.onPositionChanged(centerX, centerY);
        }
    }
    
    private void setMoveState(IMoveState mvState) {
        this.mvState =mvState ;
    }

    private boolean isJumping() {
        return jumpAbility || mvState instanceof Jumper;
    }

    private boolean isUsingSpecialMove() {
        return mvState instanceof SpecialMove || mvState instanceof FallDown;
    }
    
        @Override
    public void keyPressed(KeyEvent e) {
        if (isUsingSpecialMove()) {
            return;
        }

        if (isJumping()) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    dx = -10;
                    dir = 1;
                    dim1 = 2;
                    dim2 = 1;
                    return;
                case KeyEvent.VK_RIGHT:
                    dx = 10;
                    dir = 0;
                    dim1 = 2;
                    dim2 = 0;
                    return;
                case KeyEvent.VK_UP:
                case KeyEvent.VK_DOWN:
                    return;
                case KeyEvent.VK_SPACE:
                    return;
            }
        }

        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                dy = -10; dx = 0; dim1=1; dim2=2;
                setMoveState(new Walk());
                break;
            case KeyEvent.VK_DOWN:
                dy = 10; dx = 0; dim1=1; dim2=3;
                setMoveState(new Walk());
                break;
            case KeyEvent.VK_LEFT:
                dy = 0; dx = -10; dim1=1; dim2=1; dir=1;
                setMoveState(new Walk());
                break;
            case KeyEvent.VK_RIGHT:
                dy = 0; dx = 10; dim1=1; dim2=0; dir=0;
                setMoveState(new Walk());
                break;
            case KeyEvent.VK_SPACE:
    
                if (!jumpAbility) {
                    setBottom(getY());
                    jumpAbility = true;
                    setJVY(jumpSpeed);
            
                    dim1 = 2;
                    dim2 = (dir == 1) ? 1 : 0;
            
                    setMoveState(new Jumper());
                }
            
                break;
            case KeyEvent.VK_1:
                dx = 0;
                dy = 0;
                setMoveState(new SpecialMove());
                break;
            case KeyEvent.VK_2:
                dx = 0;
                dy = 0;
                setMoveState(new FallDown());
                break;
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        if (isUsingSpecialMove()) {
            return;
        }

        if (isJumping()) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_RIGHT:
                    dx = 0;
                    break;
                default:
                    break;
            }
            return;
        }

        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_RIGHT:
                dx = 0; dy = 0;
                dim1 = 0;
                dim2 = 0;
                setMoveState(new Stop());
                break;
        }
    }
}
