public class Stop implements IMoveState {
    public void move(SampleRole5 r) {
        r.setDX(0);
        r.setDY(0);
    }
}