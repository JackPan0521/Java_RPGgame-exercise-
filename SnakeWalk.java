public class SnakeWalk implements IMoveState {
    private static final int DEFAULT_DURATION_FRAMES = 60;

    private int frame = 0;
    private int verticalDir = 1;
    private final int durationFrames;

    private final int speedX;
    private final int waveStep;
    private final int switchPeriod;

    public SnakeWalk() {
        this(8, 3, 12, DEFAULT_DURATION_FRAMES);
    }

    public SnakeWalk(int speedX, int waveStep, int switchPeriod) {
        this(speedX, waveStep, switchPeriod, DEFAULT_DURATION_FRAMES);
    }

    public SnakeWalk(int speedX, int waveStep, int switchPeriod, int durationFrames) {
        this.speedX = speedX;
        this.waveStep = waveStep;
        this.switchPeriod = switchPeriod;
        this.durationFrames = durationFrames;
    }

    @Override
    public void move(SampleRole5 r) {
        frame++;
        if (frame % switchPeriod == 0) {
            verticalDir *= -1;
        }

        int sx = (r.getDir() == 1) ? -speedX : speedX;
        r.setX(r.getX() + sx);
        r.setY(r.getY() + verticalDir * waveStep);

        r.setDim1(1);
        r.setDim2((r.getDir() == 1) ? 1 : 0);
    }

    public boolean isFinished() {
        return frame >= durationFrames;
    }
}
