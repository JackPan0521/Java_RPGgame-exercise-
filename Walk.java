public class Walk implements IMoveState {
    public void move(SampleRole5 r) {
        r.setX(r.getX() + r.getDX());
        r.setY(r.getY() + r.getDY());
    }
}