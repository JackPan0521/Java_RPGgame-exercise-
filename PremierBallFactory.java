public class PremierBallFactory implements BallFactory {
    @Override
    public BallItem createBallItem() {
        return new PremierBallItem();
    }

    @Override
    public CatchRule createCatchRule() {
        return new PremierBallCatchRule();
    }

    @Override
    public ThrowEffect createThrowEffect() {
        return new PremierBallThrowEffect();
    }
}

class PremierBallItem implements BallItem {
    @Override
    public String getName() {
        return "Premier Ball";
    }
}

class PremierBallCatchRule implements CatchRule {
    private static final double CATCH_RATE = 0.25;

    @Override
    public boolean isCaught() {
        return Math.random() < CATCH_RATE;
    }
}

class PremierBallThrowEffect implements ThrowEffect {
    @Override
    public void play(int x, int y) {
        System.out.println("Premier Ball launched at (" + x + ", " + y + ")");
    }
}
