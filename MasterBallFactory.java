public class MasterBallFactory implements BallFactory {
    @Override
    public BallItem createBallItem() {
        return new MasterBallItem();
    }

    @Override
    public CatchRule createCatchRule() {
        return new MasterBallCatchRule();
    }

    @Override
    public ThrowEffect createThrowEffect() {
        return new MasterBallThrowEffect();
    }
}

class MasterBallItem implements BallItem {
    @Override
    public String getName() {
        return "Master Ball";
    }
}

class MasterBallCatchRule implements CatchRule {
    @Override
    public boolean isCaught() {
        return true;
    }
}

class MasterBallThrowEffect implements ThrowEffect {
    @Override
    public void play(int x, int y) {
        System.out.println("Master Ball launched at (" + x + ", " + y + ")");
    }
}
