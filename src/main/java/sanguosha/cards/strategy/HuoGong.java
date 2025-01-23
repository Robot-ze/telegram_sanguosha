package sanguosha.cards.strategy;

import sanguosha.cards.Card;
import sanguosha.cards.Color;
import sanguosha.cards.Strategy;
import sanguosha.cards.basic.HurtType;
import sanguosha.manager.GameManager;
import sanguosha.manager.Utils;
import sanguosha.people.AI;
import sanguosha.people.Person;


import config.Text;

import java.util.ArrayList;

import msg.MsgObj;

public class HuoGong extends Strategy {

    public HuoGong(GameManager gameManager, Color color, int number) {
        super(gameManager, color, number);
    }

    @Override
    public Object use() {

        if (!gotWuXie(getTarget())) {
            //// sleep(3000);
            // =========这里插入在公开群的点击连接过来
            MsgObj publicMsg = MsgObj.newMsgObj(getGameManager());
            boolean activeShowCard;

            if (getTarget() .isAI()) {
                activeShowCard = true;
                publicMsg.chatId = getGameManager().getChatId();
                //ImgDB.setImg(publicMsg, getTarget().toString());
                publicMsg.text = Text.format("%s,%s 出 %s ,请出示一张牌",
                        getTarget().getHtmlName(),
                        getSource().getPlateName(),
                        getHtmlNameWithColor());
                getGameManager().getMsgAPI().sendMsg(publicMsg);

            } else {
                activeShowCard = getGameManager().getMsgAPI().noticeAndAskPublic(
                        publicMsg,
                        getTarget(),
                        Text.format("%s,%s 出 %s ,请出示一张牌",
                                getTarget().getHtmlName(),
                                getSource().getPlateName(),
                                getHtmlNameWithColor()),
                        "出示一张牌", "huogong", false);

            }
            //sleep(1000);
            getGameManager().getMsgAPI().delMsg(publicMsg);
            // sleep(3000);

            // 如果没点就随便抽一张
            Card c = null;
            if (activeShowCard) {
                getTarget().getPriviMsg().clearHeader2();
                getTarget().getPriviMsg().setOneTimeInfo1(Text.format(
                        "\n💬%s,%s 对你 火攻，请展示一张牌:若对方弃置一张同色牌，TA对你造成1点🔥伤。", getTarget(),
                        getSource()));
                c = getTarget().chooseCard(getTarget().getCards(), false);
            } else {
                ArrayList<Card> list = getTarget().getCards();
                c = list.get(Utils.randint(0, list.size() - 1));
            }

            if (c == null) {
                return false;// AI有时不会判断是否无牌，直接返回空的
            }
         
            // =================给主动方============================

            MsgObj publicMsg2 = MsgObj.newMsgObj(getGameManager());
            boolean activeShowCard2;

            if (getSource() .isAI()) {
                activeShowCard2 = true;
                publicMsg2.chatId = getGameManager().getChatId();
                //ImgDB.setImg(publicMsg2, getSource().toString());
                publicMsg2.text = Text.format("%s 出示了 %s",
                  
                        getTarget().getPlateName(),
                        c.getHtmlNameWithColor());
                getGameManager().getMsgAPI().sendMsg(publicMsg2);

            } else {
                activeShowCard2 = getSource().launchSkillPublicDeepLink(
                        publicMsg2,
                        "出示同色牌",
                        Text.format("%s 出示了 %s",
                                getTarget().getPlateName(),
                                c.getHtmlNameWithColor()),
                        "huogong2",false);

            }

            // 如果没点就随便抽一张
            Card c2 = null;
            if (activeShowCard2) {
                getSource().getPriviMsg().clearHeader2();
                getSource().getPriviMsg().setOneTimeInfo1(Text.format(
                        "💬%s,请展示一张与 %s 同色牌", getSource(),
                        Card.getColorEmoji(c.color()) ));
                c2 = getSource().requestColor(c.color());
            }

            //getGameManager().getMsgAPI().clearButtons(publicMsg2);
            // sleep(3000);
          
            if (c2 != null) {
                boolean isPreLink = getTarget().isLinked();// 为什么要这样缓存，因为已伤害了这个值就会变
                int realNum = getTarget().hurt(getReplaceCards(), getSource(), 1, HurtType.fire);
                String  result = Text.format(",%s 丢弃同色牌 %s",
                        getSource().getPlateName(), c2.getHtmlNameWithColor());// 这个是判断过无懈的消息
                result += Text.format(",%s 受到%s🔥伤%s",
                        getTarget().getHtmlName(), realNum, getTarget().getHPEmoji());
                        publicMsg2.text = publicMsg2.text + result;// 第2次
                if (isPreLink) {
                    ArrayList<Card> cs = new ArrayList<>();
                    cs.add(this);
                    getGameManager().linkHurt(publicMsg2, cs, getSource(), getTarget(), realNum, HurtType.fire);
                    //getGameManager().getMsgAPI().editCaptionForce(publicMsg2);

                }
                //sleep(1000);
                publicMsg2.replyMakup=null;
                getGameManager().getMsgAPI().editCaptionForce(publicMsg2);
                // sleep(3000);
            } else {
                String  result = Text.format(",无同色牌,火攻失效" 
                       );
                        publicMsg2.text = publicMsg2.text + result;
                //sleep(1000);
                publicMsg2.replyMakup=null;
                getGameManager().getMsgAPI().editCaptionForce(publicMsg2);
            }

            return true;
        }
        return false;
    }

    @Override
    public boolean askTarget(Person user) {
        user.getPriviMsg().setOneTimeInfo1("\n💬请选择一名角色,目标角色展示一张手牌，然后若你弃置一张与所展示牌相同花色的手牌，你对其造成1点火焰伤害");
        return super.askTarget(user);
    }

    @Override
    public String toString() {
        return "火攻";
    }

    @Override
    public boolean needChooseTarget() {
        return true;
    }

    @Override
    public boolean asktargetAddition(Person user, Person p) {
        if (p.getCards().isEmpty()) {
            getSource().getPriviMsg().setOneTimeInfo1("\n💬你不能选择没有手牌的玩家");
            return false;
        }
        return true;
    }

    @Override
    public String details() {
        return "目标角色展示一张手牌，然后若你弃置一张与所展示牌相同花色的手牌，你对其造成1点火焰伤害。";
    }
}
