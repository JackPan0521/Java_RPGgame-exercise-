import java.util.ArrayDeque;

public class BallPool {
    private final ArrayDeque<BallItem> pokePool = new ArrayDeque<>();
    private final ArrayDeque<BallItem> premierPool = new ArrayDeque<>();
    private final ArrayDeque<BallItem> masterPool = new ArrayDeque<>();

    public BallPool(int pokeCount, int premierCount, int masterCount) {
        PokeBallFactory pokeFactory = new PokeBallFactory();
        PremierBallFactory premierFactory = new PremierBallFactory();
        MasterBallFactory masterFactory = new MasterBallFactory();

        for (int i = 0; i < pokeCount; i++) {
            pokePool.addLast(pokeFactory.createBallItem());
        }
        for (int i = 0; i < premierCount; i++) {
            premierPool.addLast(premierFactory.createBallItem());
        }
        for (int i = 0; i < masterCount; i++) {
            masterPool.addLast(masterFactory.createBallItem());
        }
    }

    public BallItem borrowBall(BallFactory factory) {
        String ballName = factory.createBallItem().getName();
        if ("Poke Ball".equals(ballName)) {
            return pokePool.pollFirst();
        }
        if ("Premier Ball".equals(ballName)) {
            return premierPool.pollFirst();
        }
        if ("Master Ball".equals(ballName)) {
            return masterPool.pollFirst();
        }
        return null;
    }

    public void returnBall(BallItem ball) {
        if (ball == null) {
            return;
        }

        String ballName = ball.getName();
        if ("Poke Ball".equals(ballName)) {
            pokePool.addLast(ball);
        } else if ("Premier Ball".equals(ballName)) {
            premierPool.addLast(ball);
        } else if ("Master Ball".equals(ballName)) {
            masterPool.addLast(ball);
        }
    }

    public int getPokeCount() {
        return pokePool.size();
    }

    public int getPremierCount() {
        return premierPool.size();
    }

    public int getMasterCount() {
        return masterPool.size();
    }
}