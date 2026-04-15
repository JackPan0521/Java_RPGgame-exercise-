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
    private int projectileDY;
    private int projectileLife = 0;
    private static final int PROJECTILE_SPEED = 18;
    private static final int PROJECTILE_MAX_LIFE = 30;
    private static final int PROJECTILE_SIZE = 20;
    private static final int HELD_BALL_SIZE = 16;
    private int mouseWorldX;
    private int mouseWorldY;
    private boolean hasMousePosition = false;
    private final Image masterBallImage;
    private final Image pokeBallImage;
    private final Image premierBallImage;
    
    public MyRole(int x, int y, int w, int h ,int jvx, int jvy, int bottom, ImageSequence[][] is,
            PositionSubject triggerSubject, Game gameCtl) {
        this.x = x; this.y = y; this.w = w; this.h = h; this.jvx = jvx; this.jvy = jvy; this.bottom = bottom;
        this.is= is;
        this.mvState = new Stop();
        this.jumpSpeed = jvy;
        this.triggerSubject = triggerSubject;
        this.gameCtl = gameCtl;
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

    private void throwBallFixed(int vx, int vy) {
        BallItem ball = currentBallFactory.createBallItem();
        CatchRule rule = currentBallFactory.createCatchRule();
        ThrowEffect effect = currentBallFactory.createThrowEffect();

        currentBallName = ball.getName();

        effect.play(x + w / 2, y + h / 2);
        startProjectileWithDirection(vx, vy);

        boolean caught = rule.isCaught();
        if (caught) {
            System.out.println(ball.getName() + " catch success");
        } else {
            System.out.println(ball.getName() + " catch failed");
        }
    }

    private void startProjectileWithDirection(int vx, int vy) {
        Point hand = getHandPosition();
        projectileActive = true;
        projectileX = hand.x;
        projectileY = hand.y;
        projectileDX = vx;
        projectileDY = vy;
        projectileLife = 0;
    }

    private void startProjectile() {
        Point hand = getHandPosition();
        projectileActive = true;
        projectileX = hand.x;
        projectileY = hand.y;
        if (hasMousePosition) {
            double vx = mouseWorldX - hand.x;
            double vy = mouseWorldY - hand.y;
            double dist = Math.sqrt(vx * vx + vy * vy);
            if (dist > 0) {
                projectileDX = (int) Math.round(vx / dist * PROJECTILE_SPEED);
                projectileDY = (int) Math.round(vy / dist * PROJECTILE_SPEED);
            } else {
                projectileDX = (dir == 1) ? -PROJECTILE_SPEED : PROJECTILE_SPEED;
                projectileDY = 0;
            }
        } else {
            projectileDX = (dir == 1) ? -PROJECTILE_SPEED : PROJECTILE_SPEED;
            projectileDY = 0;
        }
        projectileLife = 0;
    }

    private void updateProjectile() {
        if (!projectileActive) {
            return;
        }

        projectileX += projectileDX;
        projectileY += projectileDY;
        projectileLife++;

        if (projectileLife >= PROJECTILE_MAX_LIFE
                || projectileX < 0 || projectileX > 1408
                || projectileY < 0 || projectileY > 768) {
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
        Point hand = getHandPosition();
        int handX = hand.x;
        int handY = hand.y;

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

    private Point getHandPosition() {
        int handX = (dir == 1) ? x + w / 4 : x + (w * 3) / 4;
        int handY = y + h / 2 + 20;
        return new Point(handX, handY);
    }

    @Override
    public void display(Graphics g) {
        super.display(g);
        drawGuideLine(g);
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

    private void drawGuideLine(Graphics g) {
        if (!hasMousePosition) {
            return;
        }

        Point hand = getHandPosition();
        int fromX = hand.x;
        int fromY = hand.y;

        Graphics2D g2d = (Graphics2D) g;
        Stroke oldStroke = g2d.getStroke();
        g2d.setColor(new Color(0, 191, 255));
        g2d.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f,
            new float[] {8f, 8f}, 0f));
        g2d.drawLine(fromX, fromY, mouseWorldX, mouseWorldY);
        g2d.setStroke(oldStroke);
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
            case KeyEvent.VK_F:
                throwBallFixed(0, -PROJECTILE_SPEED);
                break;
            case KeyEvent.VK_B:
                throwBallFixed(0, PROJECTILE_SPEED);
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

    @Override
    public void mouseMoved(MouseEvent e) {
        int insetLeft = 0;
        int insetTop = 0;
        if (e.getComponent() instanceof Container) {
            Insets insets = ((Container) e.getComponent()).getInsets();
            insetLeft = insets.left;
            insetTop = insets.top;
        }

        int localX = e.getX() - insetLeft;
        int localY = e.getY() - insetTop;

        int shiftX = (gameCtl != null) ? gameCtl.getShiftX() : 0;
        int shiftY = (gameCtl != null) ? gameCtl.getShiftY() : 0;

        mouseWorldX = localX + shiftX;
        mouseWorldY = localY + shiftY;

        if (gameCtl != null) {
            if (mouseWorldX < 0) mouseWorldX = 0;
            if (mouseWorldY < 0) mouseWorldY = 0;
        }
        hasMousePosition = true;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseMoved(e);
    }
}
