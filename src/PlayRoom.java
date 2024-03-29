public class PlayRoom implements CreepingGameUpdateViewService, UserPanelSendParamService {
    private CreepingGame creepingGame;

    private PlayRoomUpdateViewService playRoomUpdateViewService;

    /** 游戏的一些静态常量 */
    private final int lengthOfPole = 300;
    private final int velocityOfAnts = 5;
    private final int incTime = 1;
    private final int[] antsPositions = {30, 80, 110, 160, 250};

    /**
     * maxTime: 历史记录中的最大时间
     * minTime: 历史记录中的最小时间
     * 配合 isPlayed 来更新数据
     */
    private int maxTime = 0;
    private int minTime = 0;

    /**
     * isPlayed: 标识用户是否完成过一次游戏，帮助更新 maxTime / minTime
     */
    private boolean isPlayed = false;

    /**
     * autoIndex: Autoplay 模式中的次数索引
     */
    private int autoIndex = 0;
    /**
     * isAutoplay: 用于标记现在是否处于 Autoplay 模式
     */
    private boolean isAutoplay = false;

    /**
     * 设置 PlayRoom 的 playRoomUpdateViewService 变量
     * @param playRoomUpdateViewService 传递实现了 PlayRoomUpdateViewService 接口的 UserPanel.
     */
    public void setPlayRoomUpdateViewService(PlayRoomUpdateViewService playRoomUpdateViewService) {
        this.playRoomUpdateViewService = playRoomUpdateViewService;
    }

    /** ================== CreepingGameUpdateViewService ==================== */

    /**
     * 当 creepingGame 更新下一步时，便会调用该函数。该函数调用 playRoomUpdateViewService.updateView
     * 让 UserPanel 更新蚂蚁的 UI.
     * @param antsPositions
     * @param antsDirections
     * @param timeCount
     */
    @Override
    public void updateView(int[] antsPositions, int[] antsDirections, int timeCount) {
        playRoomUpdateViewService.updateView(antsPositions, antsDirections, timeCount);
    }

    /**
     * 当 creepingGame 结束时（所有蚂蚁离开木杆），便会调用该函数。该函数传递更新 maxTime
     * 和 minTime 给 UserPanel. 通过调用 playRoomUpdateViewService.updateRecord 实现.
     * @param time
     */
    @Override
    public void updateRecordTime(int time) {
        if (!isPlayed) {
            maxTime = time;
            minTime = time;
            isPlayed = true;
        } else {
            if (time > maxTime) maxTime = time;
            if (time < minTime) minTime = time;
        }
        playRoomUpdateViewService.updateRecord(maxTime, minTime);
    }

    /**
     * 当creepingGame 结束时（所有蚂蚁离开木杆），便会调用该函数。
     * 1. 如果是 Autoplay 模式，会自动开启下一次游戏。
     *  1.1 autoRunGame()
     *  1.2 autoIndex++
     * 2. 如果是非 AutoPlay 模式，在结束时接触按钮的禁用，并重置界面
     */
    @Override
    public void gameEndedMessage(){
        if (isAutoplay){
//            resetGame();
//            this.creepingGame.setEnded(true);
//            this.creepingGame = null;
            autoIndex++;
            autoRunGame();
        } else {
            playRoomUpdateViewService.enableButtonsAfterSinglePlay();
        }
    }

    /** ===================================================================== */

    /** ==================== UserPanelSendParamService ====================== */
    @Override
    public void createGame(int[] antsDirections) {
        this.creepingGame = new CreepingGame(incTime, lengthOfPole, velocityOfAnts, antsPositions, antsDirections);
        this.creepingGame.setCreepingGameUpdateViewService(this);
        this.creepingGame.playGame();
    }

    /** 只由 UI 来调用，一旦调用，将停止 autoplay */
    @Override
    public void resetGame() {
        if(isAutoplay){
            this.isAutoplay = false;
            this.autoIndex = 0;
        }
        this.creepingGame.setEnded(true);
        this.creepingGame = new CreepingGame();
    }

    @Override
    public void autoRunGame() {
        isAutoplay = true;
        int[] directions = new int[5];
        directions[0] = (autoIndex & 16) == 16 ? 1 : -1;
        directions[1] = (autoIndex & 8) == 8 ? 1 : -1;
        directions[2] = (autoIndex & 4) == 4 ? 1 : -1;
        directions[3] = (autoIndex & 2) == 2 ? 1 : -1;
        directions[4] = (autoIndex & 1) == 1 ? 1 : -1;
        this.createGame(directions);
        if (autoIndex == 31) {
            autoIndex = 0;
            isAutoplay = false;
        }
    }
    /** ===================================================================== */

    /**
     * 整个程序的入口
     * @param args
     */
    public static void main(String[] args) {
        UserPanel userPanel = new UserPanel();
        PlayRoom playRoom = new PlayRoom();

        /**
         * 传递接口
         */
        playRoom.setPlayRoomUpdateViewService(userPanel);
        userPanel.setUserPanelSendParamService(playRoom);
    }
}