import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
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
    private final BallPool ballPool;
    private final ArrayList<BallPickup> pickups = new ArrayList<>();
    private int spawnTimer = 0;
    private static final int SPAWN_INTERVAL = 200;
    private static final int MAX_PICKUPS = 6;
    private final Random spawnRandom = new Random();

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
        this.ballPool = new BallPool(20, 10, 3);
        for (int i = 0; i < 4; i++) { spawnPickup(); }
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

        // 邊界阻擋（以地圖大小為準）
        int mapW = 1408, mapH = 768;
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x > mapW - w) x = mapW - w;
        if (y > mapH - h) y = mapH - h;

        if (model != null) {
            model.setState(x, y);
        }

        if (triggerSubject != null) {
            int centerX = x + w / 2;
            int centerY = y + h / 2;
            triggerSubject.onPositionChanged(centerX, centerY);
        }

        updateProjectile();
        spawnTimer++;
        if (spawnTimer >= SPAWN_INTERVAL && pickups.size() < MAX_PICKUPS) {
            spawnPickup();
            spawnTimer = 0;
        }
        checkPickupCollision();
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
        BallItem ball = ballPool.borrowBall(currentBallFactory);
        if (ball == null) {
            System.out.println(currentBallName + " 用完了!!");
            return;
        }
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
        BallItem ball = ballPool.borrowBall(currentBallFactory);
        if (ball == null) {
            System.out.println(currentBallName + " 用完了!!");
            return;
        }
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

    private int getCurrentBallCount() {
        if ("Master Ball".equals(currentBallName)) return ballPool.getMasterCount();
        if ("Premier Ball".equals(currentBallName)) return ballPool.getPremierCount();
        return ballPool.getPokeCount();
    }

    private void drawHeldBall(Graphics g) {
        if (getCurrentBallCount() <= 0) {
            return;
        }
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

    private void drawBallInventory(Graphics g) {
        int panelX = 1750;
        int panelY = 50;

        Graphics2D g2d = (Graphics2D) g;
        java.awt.geom.AffineTransform oldTransform = g2d.getTransform();
        g2d.setTransform(new java.awt.geom.AffineTransform());
        Composite oldComposite = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.78f));
        g2d.setColor(new Color(20, 20, 20));
        int panelHeight = (getCurrentBallCount() <= 0) ? 244 : 200;
        g2d.fillRoundRect(panelX, panelY, 420, panelHeight, 14, 14);
        g2d.setComposite(oldComposite);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 30));
        g2d.drawString("Ball Pool", panelX + 12, panelY + 44);

        g2d.setFont(new Font("SansSerif", Font.PLAIN, 28));
        int pokeCount = ballPool.getPokeCount();
        int premierCount = ballPool.getPremierCount();
        int masterCount = ballPool.getMasterCount();
        g2d.setColor(pokeCount > 0 ? new Color(255, 120, 120) : Color.RED);
        g2d.drawString("Poke Ball: " + pokeCount + (pokeCount == 0 ? "  !!" : ""), panelX + 12, panelY + 92);
        g2d.setColor(premierCount > 0 ? new Color(245, 245, 245) : Color.RED);
        g2d.drawString("Premier Ball: " + premierCount + (premierCount == 0 ? "  !!" : ""), panelX + 12, panelY + 132);
        g2d.setColor(masterCount > 0 ? new Color(190, 140, 255) : Color.RED);
        g2d.drawString("Master Ball: " + masterCount + (masterCount == 0 ? "  !!" : ""), panelX + 12, panelY + 172);

        if (getCurrentBallCount() <= 0) {
            g2d.setFont(new Font("SansSerif", Font.BOLD, 28));
            g2d.setColor(Color.RED);
            g2d.drawString(currentBallName + " 用完了!", panelX + 12, panelY + 224);
        }
        g2d.setTransform(oldTransform);
    }

    @Override
    public void display(Graphics g) {
        drawPickups(g);
        super.display(g);
        drawGuideLine(g);
        drawHeldBall(g);
        drawBallInventory(g);

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

    private void spawnPickup() {
        int px = 60 + spawnRandom.nextInt(1288);
        int py = 60 + spawnRandom.nextInt(648);
        int r = spawnRandom.nextInt(10);
        BallFactory factory;
        if (r < 6) {
            factory = new PokeBallFactory();
        } else if (r < 9) {
            factory = new PremierBallFactory();
        } else {
            factory = new MasterBallFactory();
        }
        pickups.add(new BallPickup(px, py, factory.createBallItem()));
    }

    private void checkPickupCollision() {
        Iterator<BallPickup> it = pickups.iterator();
        Rectangle playerRect = new Rectangle(x + 10, y + 10, w - 20, h - 20);
        while (it.hasNext()) {
            BallPickup pickup = it.next();
            Rectangle pickupRect = new Rectangle(
                pickup.x - BallPickup.SIZE / 2,
                pickup.y - BallPickup.SIZE / 2,
                BallPickup.SIZE, BallPickup.SIZE);
            if (playerRect.intersects(pickupRect)) {
                ballPool.returnBall(pickup.ballItem);
                System.out.println("Picked up: " + pickup.ballItem.getName());
                it.remove();
            }
        }
    }

    private Image getImageForBallName(String name) {
        if ("Master Ball".equals(name)) return masterBallImage;
        if ("Premier Ball".equals(name)) return premierBallImage;
        return pokeBallImage;
    }

    private Color getBallColorForName(String name) {
        if ("Master Ball".equals(name)) return new Color(128, 64, 192);
        if ("Premier Ball".equals(name)) return Color.WHITE;
        return new Color(220, 20, 60);
    }

    private void drawPickups(Graphics g) {
        int half = BallPickup.SIZE / 2;
        Graphics2D g2d = (Graphics2D) g;
        for (BallPickup pickup : pickups) {
            Image img = getImageForBallName(pickup.ballItem.getName());
            if (img != null) {
                g.drawImage(img, pickup.x - half, pickup.y - half,
                    BallPickup.SIZE, BallPickup.SIZE, null);
            } else {
                Color c = getBallColorForName(pickup.ballItem.getName());
                g.setColor(c);
                g.fillOval(pickup.x - half, pickup.y - half, BallPickup.SIZE, BallPickup.SIZE);
                g.setColor(Color.BLACK);
                g.drawOval(pickup.x - half, pickup.y - half, BallPickup.SIZE, BallPickup.SIZE);
            }
            g2d.setColor(new Color(255, 255, 100, 140));
            g2d.drawOval(pickup.x - half - 4, pickup.y - half - 4,
                BallPickup.SIZE + 8, BallPickup.SIZE + 8);
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
            case KeyEvent.VK_6:
                dx = 0;
                dy = 0;
                setMoveState(new SnakeWalk());
                break;
            case KeyEvent.VK_7:
                dx = 0;
                dy = 0;
                setMoveState(new SpiralWalk());
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
