package msg;

import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import db.ImgDB;
import sanguosha.people.Person;
import components.BlockingMap;
import components.LatchManager;
import components.MyCountDownLatch;

/**
 * 简化的反馈的消息对象,用来衔接游戏内消息和电报机器人的消息
 */
public class MsgObj {

    private LatchManager latchManager;

    /**
     * 用来多次接收的msg，记录一个旧的msgid
     */
    public int oldMsgId = -1;

    /**
     * 被修改的次数
     */
    public AtomicInteger editCount = new AtomicInteger(0);
    /**
     * 有的流程需要判断是否删除了，不一定要用，
     */
    public boolean isDeleted = false;

    /**
     * 消息发送到此chat_id,有可能是群或频道或个人
     */
    public long chatId;
    /**
     * 这个是由群组跳转到目的私聊的userid
     */
    public long user_chatId;

    /**
     * 这个是由群组跳转到目的私聊的多个userid
     */
    public List<Person> forPlayers;

    /**
     * 这个是由群组跳转到目的私聊的多个userid
     */
    public Set<Long> forPlayersUserIdSet;

    /**
     * 由群组deeplink产生的子消息
     */
    public Set<MsgObj> sonMsgs;

    /**
     * 由群组deeplink产生的子回调 ,key是user_id
     */
    public Map<Long, CallbackEven> sonCallback;

    /**
     * 由群组deeplink产生的多个从电报来的 callbackid
     */
    public BlockingMap<Long, String> callbackIds;
    /**
     * 这是消息发送后返回的id
     */
    // public long delayMsgId = -1;
    /**
     * 回复的对象
     */
    public int replyToMessageId;
    /**
     * 消息文档
     */
    public String text;

    public AtomicReference<String> imgName=new AtomicReference<String>(null);
    public AtomicReference<CallbackEven> imgWatingCallBack=new AtomicReference<CallbackEven>(null);
 
    /**
     * 图片id，有此项就忽略文件
     */
    public String localImgFileId;
    /**
     * 图片文件
     */
    public InputStream localImgFileStream;

    /**
     * 收返回信息的超时时间
     */
    public int timeOut = 0;

    public boolean isChooseOneCard = false;

    public boolean isChooseOneCardPublic = false;

    public boolean isChooseOneOpinionPublic = false;

    public boolean isChooseMany = false;

    /** 单个用户的Deeplink */
    public boolean isDeeplink = false;
    /** 多个用户的Deeplink，主要是请求桃和无懈可击 */
    public boolean isMultyDeeplink = false;

    public boolean isUpdateStatus = false;

    public boolean isVotePublic = false;

    /**
     * 不让别人调用，只能用工厂方法返回对象
     */
    private MsgObj(LatchManager latchManager) {

        this.latchManager = latchManager;
        latchManager.addMsgToThisRound(this);

        returnAttr = new BlockingMap<>(latchManager);

    }

    /**
     * 普通的消息对象
     * 
     * @return
     */
    public static MsgObj newMsgObj(LatchManager latchManager) {
        MsgObj m = new MsgObj(latchManager);
        return m;
    }

    /**
     * 选项按键
     */
    public List<List<String[]>> replyMakup;

    /**
     * 回调的内容
     */
    private BlockingMap<ReturnType, Object> returnAttr;

    /** 回复回调一个notice */
    public String answerNotice;

    // -----------------------------------------------------
    public Long getChatId() {
        return this.chatId;
    }

    /**
     * 设置一些延时接收的信息
     * 
     * @param key
     * @param value
     */
    public void setAttributes(ReturnType key, Object value) {

        returnAttr.put(key, value);
        // System.out.println("setAttributes="+value);
    }

    /**
     * 获取一些延时接收的信息
     * 
     * @param key
     * @param timeout
     * @return
     */
    public Object getReturnAttr(ReturnType key, int timeout) {
        try {
            return returnAttr.get(key, timeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            return null;
        }
    }

    /**
     * 只返回int
     * 
     * @param KEY
     * @param timeout
     * @return
     */
    public int getReturnMsgId(int timeout) {
        try {
            return (Integer) getReturnAttr(ReturnType.Msgid, timeout);
        } catch (Exception e) {
            // e.printStackTrace();
            return -1;
        }

    }

    /**
     * 只返回int
     * 
     * @param key
     * @param timeout
     * @return
     */
    public String getString(ReturnType key, int timeout) {
        try {
            return (String) getReturnAttr(key, timeout);
        } catch (Exception e) {
            return null;
        }

    }

    /**
     * 删除这个attr，可以再次接收
     * 
     * @param key
     */
    public void delAttributes(ReturnType key) {
        // long msgid= getReturnMsgId(0);
        // System.out.println(msgid+ "-delAttributes(ReturnType key):"+key);
        // Thread.dumpStack();
        returnAttr.del(key);
    }

    /**
     * 重置这个attr，可以再次接收
     * 
     * @param key
     */
    public void resetAttributes(ReturnType key) {
        // long msgid= getReturnMsgId(0);
        // System.out.println(msgid+ "-resetAttributes(ReturnType key):"+key);
        // Thread.dumpStack();
        returnAttr.del(key);
    }

    /**
     * 重置所有，可以再次接收
     * 
     * @param key
     */
    public void resetAll() {
        // long msgid= getReturnMsgId(0);
        // System.out.println(msgid+ "-resetAttributes(ReturnType key):"+key);
        // Thread.dumpStack();
        unlockAllAttrbutes();
        returnAttr.clear();
    }

    public void setImg(String img_name) {
        ImgDB.setImg(this, img_name);
    }

    public LatchManager getLatchManager() {
        return latchManager;
    }

    public void appendText(String text) {
        this.text = this.text + text;
    }

    /**
     * 释放一个获取返回值的等待
     * 
     * @param key
     */
    public void unlockAttrbutes(ReturnType key) {
        returnAttr.unlock(key);
    }

    /**
     * 释放全部的返回等待
     * 
     * @param key
     */
    public void unlockAllAttrbutes() {
        for (ReturnType key : returnAttr.getExistsKeys()) {
            returnAttr.unlock(key);
        }

    }

    public static void main(String[] args) {
        LatchManager latchManager = new LatchManager() {

            @Override
            public Set<MyCountDownLatch> getLatchs() {
                return new HashSet<>();
            }

            @Override
            public boolean isRunning() {
                return true;
            }

            @Override
            public void addMsgToThisRound(MsgObj msgObj) {

            }

            @Override
            public void clearPreMsg(int permitRemain) {

            }
        };
        MsgObj aaa = MsgObj.newMsgObj(latchManager);
        aaa.setAttributes(ReturnType.MainMsg, 111111);
        System.out.println(aaa.getReturnAttr(ReturnType.MainMsg, 0));
        aaa.unlockAttrbutes(ReturnType.MainMsg);
        aaa.resetAttributes(ReturnType.MainMsg);
        System.out.println(aaa.getReturnAttr(ReturnType.MainMsg, 0));
    }
}
