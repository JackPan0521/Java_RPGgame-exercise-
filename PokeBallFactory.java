public class PokeBallFactory implements BallFactory {
    @Override
    public BallItem createBallItem() {
        return new PokeBallItem();
    }

    @Override
    public CatchRule createCatchRule() {
        return new PokeBallCatchRule();
    }

    @Override
    public ThrowEffect createThrowEffect() {
        return new PokeBallThrowEffect();
    }
}

class PokeBallItem implements BallItem {
    @Override
    public String getName() {
        return "Poke Ball";
    }
}

class PokeBallCatchRule implements CatchRule {
    private static final double CATCH_RATE = 0.35;

    @Override
    public boolean isCaught() {
        return Math.random() < CATCH_RATE;
    }
}

class PokeBallThrowEffect implements ThrowEffect {
    @Override
    public void play(int x, int y) {
        System.out.println("Poke Ball launched at (" + x + ", " + y + ")");
    }
}
