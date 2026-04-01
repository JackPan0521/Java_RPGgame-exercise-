public class FallDown implements IMoveState {
    private int remainingFrames = 18; // 6張圖 × 3 循環

    @Override
    public void move(SampleRole5 r) {
        r.setDX(0);
        r.setDY(0);
        r.setDim1(3);
        r.setDim2(0);
        remainingFrames--;
    }

    public boolean isFinished() {
        return remainingFrames <= 0;
    }
}
