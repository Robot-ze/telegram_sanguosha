package components;

import java.util.Set;

import msg.MsgObj;

/**
 * 用来控制阀门的开关的接口
 */
public interface LatchManager {
    /**
     * 缓存阀门，
     * 
     * @return
     */
    public Set<MyCountDownLatch> getLatchs();

    public boolean isRunning();

    /**
     * 加入一个以round的列表中，每两轮删除之前的消息
     * @param msgObj
     */
    public void addMsgToThisRound(MsgObj msgObj);

    /**
     * 删除之前的消息，仅保存permitRemain条
     * @param permitRemain
     */
    public void clearPreMsg(int permitRemain);
}
