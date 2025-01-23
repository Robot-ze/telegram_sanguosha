package sanguosha.manager;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import msg.MsgObj;
import sanguosha.people.Attributes;

/***
 * 这个时原来的io，已经被msgAPI替换了，还有一些残余方法
 */
public class IO {
    // private final Scanner sn = new Scanner(System.in);
    private GameManager gameManager;
    private LinkedBlockingDeque<MsgObj> msgQeque = new LinkedBlockingDeque<>();

    public void init(GameManager gameManager) {
        this.gameManager = gameManager;
        gameManager.getMsgAPI().submitRunnable(new Runnable() {
            @Override
            public void run() {
                sendQueueMsgExec();
            }
        });
       
    }

    /**
     * 发送队列消息
     */
    private void sendQueueMsgExec() {

        while (gameManager.isRunning()) {
            try {
                MsgObj printMsg = msgQeque.poll(5, TimeUnit.SECONDS);
                //sleep(1000L);
                try {
                    gameManager.getMsgAPI().sendImg(printMsg);
                } finally {
                    sleep(2000L);
                }
            } catch (InterruptedException e) {

            }
        }
    }

    /**
     * 只是加入广播队列，在队列里是按每两秒1次发送，只适合那些不需要操作的文字和图片展示
     * @param s
     */
    public void printlnPublic(String s) {
        printlnPublic(s, null);
        // System.out.println(s);
    }

    /**
     * 只是加入广播队列，在队列里是按每两秒1次发送，只适合那些不需要操作的文字和图片展示
     * 
     * @param s   显示的字
     * @param img 数据库里的图的名称 比如"杀"
     */
    public void printlnPublic(String s, String img) {

        MsgObj msg = MsgObj.newMsgObj(gameManager);
        msg.chatId = gameManager.getChatId();
        // msg.text = "[全局]" + s;
        msg.text = s;
        if(img!=null){
            msg.setImg(img);
        }
     
        try {
            msgQeque.putLast(msg);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 恐慌
     * @param s 恐慌信息
     */ 
    public void panic(String s) {
        String panic = "";
        panic += "panic at " + Thread.currentThread().getStackTrace()[1].getFileName();
        panic += " line" + Thread.currentThread().getStackTrace()[1].getLineNumber() + ": " + s;
        printlnPublic(panic);

    }

    /**
     * 展示一下再删除
     * 
     * @param priv_p
     * @param text
     */
    public void delaySendAndDelete(Attributes priv_p, String text) {
        if (priv_p.getUser() == null) {// AI
            return;
        }
        MsgObj priv = MsgObj.newMsgObj(gameManager);
        priv.text = text;
        priv.chatId = priv_p.getUser().user_id;
        gameManager.getMsgAPI().sendMsg(priv);
        gameManager.getMsgAPI().submitRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    sleep(5000);
                    gameManager.getMsgAPI().delMsg(priv);
                } catch (Exception e) {
                }

            }
        });
    }

    /**
     * 展示一下再删除
     * 
     * @param msg
     */
    public void delaySendAndDelete(MsgObj msg) {
        gameManager.getMsgAPI().sendMsg(msg);
        gameManager.getMsgAPI().submitRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    sleep(5000);
                    gameManager.getMsgAPI().delMsg(msg);
                } catch (Exception e) {
                }

            }
        });
    }

    /**
     * 睡眠
     * @param time 睡眠时间
     */ 
    public void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
