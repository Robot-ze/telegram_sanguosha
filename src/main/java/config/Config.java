package config;

public class Config {
    
  
    /**
     * 发送到私聊的按键不点击的超时时间
     */
    public static final int PRIV_RND_TIME_60S=60000;

        /**
     * 发送到私聊的按键不点击的超时时间
     */
    public static final int PRIV_RND_CHOOSE_ROLE_TIME_90S=90000;
        /**
     * 轮到出牌整一轮的时间限制
     */
    public static final int PRIV_ROUND_TIME_180S=180000;
      /**
     * 发送到群的按键不点击的超时时间
     */
    public static final int PUBLIC_NOTICE_TIME_25S=25000;     /**
    /**
     * 私聊不点击按键AI接管的次数
     */
    public static final int PRIV_TIME_OUT_NUM_SWITCH_AI=3;
    /**
     * 群聊不点击按键AI接管的次数
     */
    public static final int PUBLIC_TIME_OUT_NUM_SWITCH_AI=3;

          /**
     * 发送到群里的循环点击无懈，格斗等的超时时间
     */
    public static final int PUBLIC_ACTION_TIME_15S=15000;    

    /**
     * 状态总表中每行显示的装备，属性包括括号的总长度
     */
    public static final int SHOWTEXT_LINECOUNT=14;
    /**
     * 等单个无懈的时间
     */
    public static final int WUXIE_WAIT_TIME_15S=15000;

       /**
     * 等多个无懈的时间
     */
    public static final int WUXIE_WAIT_TIME_ROOP_60S=60000;
    
     /**
     * 动态行数
     */
    public static int getRow(int num) {
        if (num < 4) {
            return 2;
        } else if (num < 11) {
            return 2;
        } else {
            return 3;
        }
    }
}
