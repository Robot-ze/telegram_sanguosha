package msg;

import sanguosha.cards.Card;
import sanguosha.people.Person;
 

public interface CallbackEven   {
    /**
     * deepLink的回调
     * @param thiMsgObj
     * @param callBackData
     */
    default public void DeepLinkExec(MsgObj thiMsgObj, String callBackData){

    };
    /**
     * 在群里请求多卡时的附加回调
     * @param thiMsgObj
     * @param callBackData
     * @return
     */
    default Card additionGetCardExec(Card preCard,Person person,MsgObj thiMsgObj){
        return null;
    };

        /**
     * 在群里请求多卡时的附加回调
     * @param thiMsgObj
     * @param callBackData
     * @return
     */
    default String getPersonStatusExec( Person person ){
        return null;
    };


    /**
     * 定义牌的生效，可以写在回调里，也可以写在回调外
     * @param card
     * @return
     */
    default boolean juge(Card card){
        return false;
    }
    /**
     * 暂停进度条的计时
     */
    default boolean progressStopCount( ){
            return false;
    }
    /**
     * 进度条超时操作
     */
    default void progressTimeOut( ){
        
    }

    /**
     * 投票
     */
    default void vote(long userId,String result){

    }

        /**
     * 投票
     */
    default void sendImgMsgDone(String imgFileId,String uid){

    }
}
