import game.framework.Game;

public class MusicToggleObserver implements PositionObserver {
    private final Game game;
    private final String[] currentBgmRef;
    private static final String DEFAULT_BGM = "Gameboy.mp3";
    private static final String HOUSE_BGM = "This Is Phonk.mp3";

    public MusicToggleObserver(Game game, String[] currentBgmRef) {
        this.game = game;
        this.currentBgmRef = currentBgmRef;
    }

    @Override
    public void onEnterTrigger(String triggerId) {
        if (!"house_music".equals(triggerId)) {
            return;
        }

        if (!HOUSE_BGM.equals(currentBgmRef[0])) {
            currentBgmRef[0] = HOUSE_BGM;
            game.stopBackgroundMusic();
        }
    }

    @Override
    public void onExitTrigger(String triggerId) {
        if (!"house_music".equals(triggerId)) {
            return;
        }

        if (!DEFAULT_BGM.equals(currentBgmRef[0])) {
            currentBgmRef[0] = DEFAULT_BGM;
            game.stopBackgroundMusic();
        }
    }
}