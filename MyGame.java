import game.framework.*;
import java.util.*;
import java.awt.*; //for Color.white
public class MyGame {
    public static void main(String[] args) {
    final String[] currentBgm = {"Gameboy.mp3"};
//Step 1. 產生遊戲背景物件: 標題, 寬度, 高度, 背景顏色
    GameContext ctx = new GameContext ("MY Warrior Game", 1080, 720, Color.white){
        @Override
        public String getBackgroundImgPath(){
            return "background2.png";
        }
        
        @Override
        public String getBackgroundMusicPath(){
            return currentBgm[0];
        }
    } ;
//Step 2. 產生遊戲物件 (false: 啟用非固定視窗，鏡頭可跟角色移動)
    Game gameEngine = new Game(ctx, false); //Game就是遊戲引擎
//Step 3. 產生各種角色 (目前是空的)
    ArrayList<Role> myroles = new ArrayList<> (); //建立角色清單
    //[act][dir]: act:0 stop, act 1: walk, act 2: fly
    ImageSequence[][] is = {  { new ImageSequence("stop/" , "png", 1)},
                              { new ImageSequence("walk_right/" , "png", 8),
                                new ImageSequence("walk_left/" , "png", 8),
                                new ImageSequence("walk_up/" , "png", 8),
                                new ImageSequence("walk_down/" , "png", 8)}, 
                              { new ImageSequence("fly_right/" , "png", 1), 
                                new ImageSequence("fly_left/" , "png", 1) },
                            { new ImageSequence("special_move/" , "png", 6) }
                         };  //建立角色分鏡圖

    CoordinateTriggerSubject houseTrigger =
        new CoordinateTriggerSubject(new Rectangle(560, 170, 140, 120), "house_music");
    houseTrigger.addObserver(new MusicToggleObserver(gameEngine, currentBgm));
        
    MyRole player = new MyRole(200, 350, 100, 100, 0, -30, 400, is, houseTrigger, gameEngine);
    myroles.add(player ); 
    gameEngine.setMainRole(player); //指定鏡頭跟隨主角
    gameEngine.setSpace(0, 1408, 0, 768); //背景地圖範圍
    gameEngine.setFurtherXY(120, 80); //緩衝型跟拍：靠近邊緣時才推動鏡頭
    gameEngine.registerKeyEventHandler(player); //註冊接受鍵盤事件
    gameEngine.registerMouseMotionHandler(player); //註冊滑鼠移動事件
    
//Step 4: 開始執行
    gameEngine.go(myroles);
}
}