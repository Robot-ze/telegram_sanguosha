package msg;

import java.util.Set;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CallBackMap类用于管理回调代码与消息对象的映射关系
 */
public class CallBackMap {
    private Map<String, MsgObj> callBackMap = new ConcurrentHashMap<>();
    private Map<Long, Set<String>> callBackCodes = new ConcurrentHashMap<>();

    /**
     * 将回调代码与消息对象关联并存储
     * 
     * @param callBackCode 回调代码
     * @param msg 消息对象
     */
    public void put(String callBackCode, MsgObj msg) {
        callBackMap.put(callBackCode, msg);
        Set<String> set = callBackCodes.get(msg.chatId);
        if (set == null) {
            set = ConcurrentHashMap.newKeySet();
            callBackCodes.put(msg.chatId, set);
        }
        set.add(callBackCode);
    }

    /**
     * 根据回调代码获取消息对象
     * 
     * @param callBackCode 回调代码
     * @return 消息对象
     */
    public MsgObj get(String callBackCode) {
        return callBackMap.get(callBackCode);
    }

    /**
     * 移除指定的回调代码及其关联的消息对象
     * 
     * @param callBackCode 回调代码
     */
    public void remove(String callBackCode) {
        MsgObj msg = callBackMap.get(callBackCode);
        if(msg==null){
            return;
        }
        Set<String> set = callBackCodes.get(msg.chatId);
        if (set != null) {
            set.remove(callBackCode);
        }
        callBackMap.remove(callBackCode);
    }

    /**
     * 清除当前组的游戏的 callBackCode
     * 
     * @param chatId 聊天ID
     */
    public void clear(Long chatId) {
        Set<String> set = callBackCodes.get(chatId);
        if (set != null) {
            for (String callBackCode : set) {
                callBackMap.remove(callBackCode);
            }
            callBackCodes.remove(chatId);
        }
    }

    /**
     * 清除所有的回调代码和消息对象
     */
    public void clearAll() {
        callBackMap.clear();
        callBackCodes.clear();
    }

    /**
     * 返回callBackMap的字符串表示
     * 
     * @return callBackMap的字符串表示
     */
    @Override
    public String toString() {
        return callBackMap.toString();
    }
}
