public class BallPickup {
    public final int x;
    public final int y;
    public final BallItem ballItem;
    public static final int SIZE = 24;

    public BallPickup(int x, int y, BallItem ballItem) {
        this.x = x;
        this.y = y;
        this.ballItem = ballItem;
    }
}
