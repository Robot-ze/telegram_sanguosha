package msg;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;

import sanguosha.cards.Card;
import sanguosha.people.Person;
import sanguosha.user.SanUser;

public interface MsgAPI {

  /** 异步发送,不需要任何返回 */
  public <T extends Serializable, Method extends BotApiMethod<T>> void asyncSend(Method method);

  /** 异步发送 */
  public <T extends Serializable, Method extends BotApiMethod<T>> void asyncSend(Method method, MsgObj msg,
      boolean fillReturn );

  /**
   * 删除消息
   * 
   * @param msg
   */
  default public void delMsgForce(MsgObj msg) {
    delMsgForce(msg, true);
  };

  /**
   * 删除消息
   * 
   * @param msg
   */
  default public void delMsg(MsgObj msg) {
    delMsg(msg, true);
  };

  /**
   * 多条删除消息,chatId要都相同
   * 
   * @param msg
   */
  default public void mutiDelMsg(List<MsgObj> msgs) {
    mutiDelMsg(msgs, true);
  };

  /**
   * 发送文字消息
   * 
   * @param msg
   */
  default public void sendMsg(MsgObj msg) {
    sendMsg(msg, true);
  };

  /**
   * 发送带消息的图片
   * 
   * @param msg
   */
  default public void sendImg(MsgObj msg) {
    sendImg(msg, true);
  };

  /**
   * 修改消息，记得msg也要带上按键信息，不然会被清空
   * 
   * @param msg
   */
  default public void editMsg(MsgObj msg) {
    editMsg(msg, true);
  };

  /**
   * 修改按键，其实就是makekup
   * 
   * @param msg
   */
  default public void editButtons(MsgObj msg) {
    editButtons(msg, true);
  };

  /**
   * 删除按键，也删除回调表中的key
   * 
   * @param msg
   */
  default public void clearButtons(MsgObj msg) {
    clearButtons(msg, true);
  };

  // /**
  // *
  // 修改带图片的信息，记得msg也要带上按键信息，不然会被清空，如果后续还要接收回调，记得重置msgid的接收，因为编辑文本和（按钮？）后这个msgid会变
  // *
  // * @param msg
  // */
  // default public void editCaption(MsgObj msg) {
  // editCaption(msg, true);
  // };

  /**
   * 强制修改图片，信息，即使消息被标记删除
   * 修改带图片的信息，记得msg也要带上按键信息，不然会被清空，如果后续还要接收回调，记得重置msgid的接收，因为编辑文本和（按钮？）后这个msgid会变
   * 
   * @param msg
   */
  default public void editPhotoForce(MsgObj msg) {
    editCaptionForce(msg, true);
  };

  /**
   * 强制修改，即使消息被标记删除
   * 修改带图片的信息，记得msg也要带上按键信息，不然会被清空，如果后续还要接收回调，记得重置msgid的接收，因为编辑文本和（按钮？）后这个msgid会变
   * 
   * @param msg
   */
  default public void editCaptionForce(MsgObj msg) {
    editCaptionForce(msg, true);
  };

  /**
   * 返回回调码
   * 
   * @return
   */
  public CallBackMap getCallBackMap();

  /**
   * 删除消息
   * 
   * @param msg
   */
  public void delMsg(MsgObj msg, boolean fillReturn);

  /**
   * 删除消息
   * 
   * @param msg
   */
  public void delMsgForce(MsgObj msg, boolean fillReturn);

  /**
   * 多条删除消息,chatId要都相同
   * 
   * @param msg
   */
  public void mutiDelMsg(List<MsgObj> msgs, boolean fillReturn);

  /**
   * 发送文字消息
   * 
   * @param msg
   */
  public void sendMsg(MsgObj msg, boolean fillReturn);

  /**
   * 发送带消息的图片
   * 
   * @param msg
   */
  public void sendImg(MsgObj msg, boolean fillReturn);

  /**
   * 修改消息，记得msg也要带上按键信息，不然会被清空
   * 
   * @param msg
   */
  public void editMsg(MsgObj msg, boolean fillReturn);

  /**
   * 修改按键，其实就是makekup
   * 
   * @param msg
   */
  public void editButtons(MsgObj msg, boolean fillReturn);

  /**
   * 删除按键，也删除回调表中的key
   * 
   * @param msg
   */
  public void clearButtons(MsgObj msg, boolean fillReturn);

  // /**
  // *
  // 修改带图片的信息，记得msg也要带上按键信息，不然会被清空，如果后续还要接收回调，记得重置msgid的接收，因为编辑文本和（按钮？）后这个msgid会变
  // *
  // * @param msg
  // */
  // public void editCaption(MsgObj msg, boolean fillReturn);

  /**
   * 强制修改，即使消息被标记删除
   * 修改带图片的信息，记得msg也要带上按键信息，不然会被清空，如果后续还要接收回调，记得重置msgid的接收，因为编辑文本和（按钮？）后这个msgid会变
   * 
   * @param msg
   */
  public void editCaptionForce(MsgObj msg, boolean fillReturn);

  /**
   * 强制修改图片，信息，即使消息被标记删除
   * 修改带图片的信息，记得msg也要带上按键信息，不然会被清空，如果后续还要接收回调，记得重置msgid的接收，因为编辑文本和（按钮？）后这个msgid会变
   * 
   * @param msg
   */
  public void editPhotoForce(MsgObj msg, boolean fillReturn);

  /**
   * 给按下按键一个消息回馈
   * 
   * @param msg
   * @param text
   * @param showAlert
   */
  public void answerCallBack(MsgObj msg, String text, boolean showAlert);

  /**
   * 给按下按键一个消息回馈，根据回调id来反馈
   * 
   * @param msg
   * @param text
   * @param showAlert
   */
  public void answerCallBack(String callBackId, String text, boolean showAlert);

  /**
   * 选择一项，不能为空,用 ReturnType.ChooseOne 接收
   * msg.getString(ReturnType.ChooseOne,timeOut );
   * 
   * @param <E>
   * @param opinions
   * @param text
   * @return
   */
  public <E> MsgObj chooseOneFromOpinion(List<E> opinions, MsgObj text);

  /**
   * 选择一项，不能为空,用 ReturnType.ChooseOne 接收
   * msg.getString(ReturnType.ChooseOne,timeOut );
   * 
   * @param <E>
   * @param opinions
   * @param text
   * @return
   */
  public <E> MsgObj chooseOneFromOpinion(List<E> opinions, MsgObj text, String bottonFront, String bottonBehind);

  /**
   * 选择一项，可以跳过, 选择一项，不能为空,用 ReturnType.ChooseOne 接收
   * msg.getString(ReturnType.ChooseOne,timeOut );
   * 
   * @param <E>
   * @param opinions
   * @param text
   * @return
   */
  public <E> MsgObj chooseOneFromOpinionCanBeNull(List<E> opinions, MsgObj text);

  /**
   * 选择无序的多项
   * 
   * @param <E>
   * @param opinions
   * @param num
   * @param text
   * @return
   */
  public <E> MsgObj chooseSomeFromOpinion(List<E> opinions, int num, MsgObj text);

  /**
   * 选择无序的多项,可以为空
   * 
   * @param <E>
   * @param opinions
   * @param num
   * @param text
   * @return
   */
  public <E> MsgObj chooseSomeFromOpinion(List<E> opinions, int num, boolean canBeNull,MsgObj text);

  /**
   * 选错了重置多项选择
   * 
   * @param <E>
   * @param opinions
   * @param num
   * @param text
   * @return
   */
  public <E> MsgObj renewSomeFromOpinion(List<E> opinions, int num, MsgObj text);

  /**
   * 选择有序的多项
   * 
   * @param <E>
   * @param opinions
   * @param num
   * @param text
   * @return
   */
  public <E> MsgObj chooseSomeFromOpinionByOrder(List<E> opinions, int num, MsgObj text);

  /**
   * 选错了重置有序的多项选择
   * 
   * @param <E>
   * @param opinions
   * @param num
   * @param text
   * @return
   */
  public <E> MsgObj renewSomeFromOpinionByOrder(List<E> opinions, int num, MsgObj text);

  /**
   * 选并跳转一个deeplink
   * 
   * @param <E>
   * @param opinions
   * @param inMsgObj
   * @return
   */
  public <E> MsgObj chooseOneDeepLink(List<E> opinions, MsgObj inMsgObj);

  /**
   * 直接是一个deeplink跳转，不判断回调
   * 
   * @param <E>
   * @param opinions
   * @param inMsgObj
   * @return
   */

  public <E> MsgObj chooseOneDeepLinkNoCallback(List<E> opinions, MsgObj inMsgObj);

  /**
   * 提醒玩家从群聊跳转到私聊
   * 
   * @param p           玩家
   * @param noticeText  提醒的内容
   * @param buttonText  按键的文字
   * @param buttonValue 按键的回调值，因为有可能有多个事件等回调，这里一定要和其他地方区分开
   * @return
   */
  public boolean noticeAndAskPublic(Person p, String noticeText, String buttonText, String buttonValue);

  /**
   * 提醒玩家从群聊跳转到私聊
   * 
   * @param c           卡片
   * @param p           玩家
   * @param noticeText  提醒的内容
   * @param buttonText  按键的文字
   * @param buttonValue 按键的回调值，因为有可能有多个事件等回调，这里一定要和其他地方区分开
   * @return
   */
  public boolean noticeAndAskPublic(Card c, Person p, String noticeText, String buttonText, String buttonValue);

  /**
   * 提醒玩家从群聊跳转到私聊，这个方法不会删除发送到电报的消息，可以自行用来修改，或删除操作
   * 
   * @param msgObj      消息包装
   * @param p           玩家
   * @param noticeText  提醒的内容
   * @param buttonText  按键的文字
   * @param buttonValue 按键的回调值，因为有可能有多个事件等回调，这里一定要和其他地方区分开
   * @return
   */
  public boolean noticeAndAskPublic(MsgObj msgObj, Person p, String noticeText, String buttonText, String buttonValue,
      boolean showImg);

  /**
   * 提醒玩家从群聊跳转到私聊,这个方法不会删除发送到电报的消息，可以自行用来修改，或删除操作
   * 
   * @param msgObj      消息包装
   * @param c           卡片
   * @param p           玩家
   * @param noticeText  提醒的内容
   * @param buttonText  按键的文字
   * @param buttonValue 按键的回调值，因为有可能有多个事件等回调，这里一定要和其他地方区分开
   * @return
   */
  public boolean noticeAndAskPublic(MsgObj msgObj, Card c, Person p, String noticeText, String buttonText,
      String buttonValue);

  /**
   * 提醒玩家从群聊跳转到私聊，这个方法不会删除发送到电报的消息，可以自行用来修改，或删除操作，该操作不判断回调
   * 
   * @param msgObj      消息包装
   * @param buttonText  按键的文字
   * @param buttonValue 按键的回调值，因为有可能有多个事件等回调，这里一定要和其他地方区分开
   * @return
   */
  public boolean noticeAndAskPublicNoCallBack(MsgObj msgObj, String buttonText, String buttonValue);

  /**
   * 在群内选一个选项，没有deeplink
   * 
   * @param <E>
   * @param opinions
   * @param inMsgObj
   * @return
   */
  public <E> MsgObj chooseOnePublic(List<E> opinions, MsgObj inMsgObj);

  /**
   * 多个玩家竞争出一张牌，无顺序
   * 
   * @param <E>
   * @param opinions
   * @param inMsgObj
   * @param tag      这个deeplink回调中的参数
   * @return
   */
  public <E> MsgObj comfirmMultyDeepLink(List<E> opinions, MsgObj inMsgObj, String tag);

  /**
   * 多个玩家竞争出一张牌，无顺序
   * 
   * @param publicMsg
   * @param noticeText
   * @param buttonText
   * @param buttonValue
   */
  public void noticeAndAskOneForMulty(MsgObj publicMsg, String buttonText, String buttonValue);

  /**
   * 收集投票，目前主要针对于吉
   * 
   * @param <E>
   * @param opinions
   */
  public <E> void votePublic(List<E> opinions, MsgObj publicMsgObj);

  public void sendAllPersonStaus(List<Person> players, CallbackEven personStatus);

  public void submitRunnable(Runnable runnable);

  public <E> Future<E> submitCallable(Callable<E> callable);

  public void addUser(SanUser testUser);

  public void removeUser(long user_id);

  public void removeUser(String user_id);

  /**
   * 有没有公共发送的消息，主要用来计算出牌倒计时时，如果有公共消息，
   */
  // public boolean hasPublicMsg();
  // public void addPublicMsg(MsgObj publicMsg);
  // public void removePublicMsg(MsgObj publicMsg);

  public void endGame(long chatId);

}
