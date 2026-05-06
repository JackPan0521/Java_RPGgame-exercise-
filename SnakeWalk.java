public class SnakeWalk implements IMoveState {
    private int frame = 0;
    private int verticalDir = 1;

    private final int speedX;
    private final int waveStep;
    private final int switchPeriod;

    public SnakeWalk() {
        this(8, 3, 12);
    }

    public SnakeWalk(int speedX, int waveStep, int switchPeriod) {
        this.speedX = speedX;
        this.waveStep = waveStep;
        this.switchPeriod = switchPeriod;
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
}
