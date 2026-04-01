public class SpecialMove implements IMoveState {
    private int remainingFrames = 12;
    private final int speed = 18;

    @Override
    public void move(SampleRole5 r) {
        r.setDY(0);
        r.setDim1(2);

        if (r.getDir() == 1) {
            r.setDim2(1);
            r.setX(r.getX() - speed);
        } else {
            r.setDim2(0);
            r.setX(r.getX() + speed);
        }

        remainingFrames--;
    }

    public boolean isFinished() {
        return remainingFrames <= 0;
    }
}
