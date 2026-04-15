import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import game.framework.*; 
public class MyRole extends SampleRole5
{
    IMoveState mvState = null;
    private final int jumpSpeed;
    private final PositionSubject triggerSubject;
    private BallFactory currentBallFactory;
    private String currentBallName;
    private boolean projectileActive = false;
    private int projectileX;
    private int projectileY;
    private int projectileDX;
    private int projectileLife = 0;
    private static final int PROJECTILE_SPEED = 18;
    private static final int PROJECTILE_MAX_LIFE = 30;
    private static final int PROJECTILE_SIZE = 20;
    private static final int HELD_BALL_SIZE = 16;
    private final Image masterBallImage;
    private final Image pokeBallImage;
    private final Image premierBallImage;
    
    public MyRole(int x, int y, int w, int h ,int jvx, int jvy, int bottom, ImageSequence[][] is,
            PositionSubject triggerSubject) {
        this.x = x; this.y = y; this.w = w; this.h = h; this.jvx = jvx; this.jvy = jvy; this.bottom = bottom;
        this.is= is;
        this.mvState = new Stop();
        this.jumpSpeed = jvy;
        this.triggerSubject = triggerSubject;
        this.currentBallFactory = new PokeBallFactory();
        this.currentBallName = currentBallFactory.createBallItem().getName();
        this.masterBallImage = loadBallImage("Master_Ball.png");
        this.pokeBallImage = loadBallImage("Poke_Ball.png");
        this.premierBallImage = loadBallImage("Premier_ball.png");
    }

    private Image loadBallImage(String... fileNames) {
        for (String fileName : fileNames) {
            try {
                BufferedImage image = ImageIO.read(new File(fileName));
                if (image != null) {
                    return image;
                }
            } catch (Exception e) {
                // Ignore and try next candidate filename.
            }
        }
        return null;
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

        updateProjectile();
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

    private void setBallFactory(BallFactory factory) {
        currentBallFactory = factory;
        currentBallName = currentBallFactory.createBallItem().getName();
        System.out.println("Current ball: " + currentBallName);
    }

    private void throwBall() {
        BallItem ball = currentBallFactory.createBallItem();
        CatchRule rule = currentBallFactory.createCatchRule();
        ThrowEffect effect = currentBallFactory.createThrowEffect();

        currentBallName = ball.getName();

        effect.play(x + w / 2, y + h / 2);
        startProjectile();

        boolean caught = rule.isCaught();
        if (caught) {
            System.out.println(ball.getName() + " catch success");
        } else {
            System.out.println(ball.getName() + " catch failed");
        }
    }

    private void startProjectile() {
        projectileActive = true;
        projectileX = x + w / 2;
        projectileY = y + h / 2 + 20;
        projectileDX = (dir == 1) ? -PROJECTILE_SPEED : PROJECTILE_SPEED;
        projectileLife = 0;
    }

    private void updateProjectile() {
        if (!projectileActive) {
            return;
        }

        projectileX += projectileDX;
        projectileLife++;

        if (projectileLife >= PROJECTILE_MAX_LIFE || projectileX < 0 || projectileX > 1080) {
            projectileActive = false;
        }
    }

    private Color getBallColor() {
        if ("Master Ball".equals(currentBallName)) {
            return new Color(128, 64, 192);
        }
        if ("Premier Ball".equals(currentBallName)) {
            return Color.WHITE;
        }
        return new Color(220, 20, 60);
    }

    private Image getBallImage() {
        if ("Master Ball".equals(currentBallName)) {
            return masterBallImage;
        }
        if ("Premier Ball".equals(currentBallName)) {
            return premierBallImage;
        }
        return pokeBallImage;
    }

    private void drawHeldBall(Graphics g) {
        int handX = (dir == 1) ? x + w / 4 : x + (w * 3) / 4;
        int handY = y + h / 2 + 20;

        Image ballImage = getBallImage();
        if (ballImage != null) {
            g.drawImage(ballImage, handX - HELD_BALL_SIZE / 2, handY - HELD_BALL_SIZE / 2,
                HELD_BALL_SIZE, HELD_BALL_SIZE, null);
        } else {
            Color ballColor = getBallColor();
            g.setColor(ballColor);
            g.fillOval(handX - 6, handY - 6, 12, 12);
            g.setColor(Color.BLACK);
            g.drawOval(handX - 6, handY - 6, 12, 12);
            g.drawLine(handX - 6, handY, handX + 6, handY);
        }
    }

    @Override
    public void display(Graphics g) {
        super.display(g);
        drawHeldBall(g);

        if (projectileActive) {
            Image ballImage = getBallImage();
            if (ballImage != null) {
                g.drawImage(ballImage, projectileX - PROJECTILE_SIZE / 2, projectileY - PROJECTILE_SIZE / 2,
                    PROJECTILE_SIZE, PROJECTILE_SIZE, null);
            } else {
                Color ballColor = getBallColor();
                g.setColor(ballColor);
                g.fillOval(projectileX - 8, projectileY - 8, 16, 16);
                g.setColor(Color.BLACK);
                g.drawOval(projectileX - 8, projectileY - 8, 16, 16);
                g.drawLine(projectileX - 8, projectileY, projectileX + 8, projectileY);
            }
        }
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
            case KeyEvent.VK_3:
                setBallFactory(new PokeBallFactory());
                break;
            case KeyEvent.VK_4:
                setBallFactory(new PremierBallFactory());
                break;
            case KeyEvent.VK_5:
                setBallFactory(new MasterBallFactory());
                break;
            case KeyEvent.VK_C:
                throwBall();
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
