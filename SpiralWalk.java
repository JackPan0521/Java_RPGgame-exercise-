public class SpiralWalk implements IMoveState {
    private static final int DEFAULT_DURATION_FRAMES = 60;

    private boolean initialized = false;
    private int frame = 0;
    private final int durationFrames;
    private double centerX;
    private double centerY;

    private double angle = 0.0;
    private double radius = 8.0;

    private final double angleStep;
    private final double radiusStep;

    public SpiralWalk() {
        this(0.28, 1.6, DEFAULT_DURATION_FRAMES);
    }

    public SpiralWalk(double angleStep, double radiusStep) {
        this(angleStep, radiusStep, DEFAULT_DURATION_FRAMES);
    }

    public SpiralWalk(double angleStep, double radiusStep, int durationFrames) {
        this.angleStep = angleStep;
        this.radiusStep = radiusStep;
        this.durationFrames = durationFrames;
    }

    @Override
    public void move(SampleRole5 r) {
        frame++;

        if (!initialized) {
            centerX = r.getX();
            centerY = r.getY();
            initialized = true;
        }

        angle += angleStep;
        radius += radiusStep;

        int nx = (int) Math.round(centerX + Math.cos(angle) * radius);
        int ny = (int) Math.round(centerY + Math.sin(angle) * radius);

        r.setX(nx);
        r.setY(ny);

        r.setDim1(1);
        r.setDim2((Math.cos(angle) < 0) ? 1 : 0);
        r.setDir((Math.cos(angle) < 0) ? 1 : 0);
    }

    public boolean isFinished() {
        return frame >= durationFrames;
    }
}
