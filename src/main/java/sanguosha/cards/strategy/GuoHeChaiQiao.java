package sanguosha.cards.strategy;

import config.Text;
import msg.MsgObj;
import sanguosha.cards.Card;
import sanguosha.cards.Color;
import sanguosha.cards.Strategy;
import sanguosha.manager.GameManager;
import sanguosha.people.Person;

public class GuoHeChaiQiao extends Strategy {
    public GuoHeChaiQiao(GameManager gameManager, Color color, int number) {
        super(gameManager, color, number);
    }

    @Override
    public Object use() {

        if (!gotWuXie(getTarget())) {
            getSource().getPriviMsg().setOneTimeInfo1(Text.format("\n💬你对 %s 使用 过河拆桥，请选择TA的一张牌", getTarget()));
            if (getTarget().getAllCardSize() <= 0) { // 别人打出无懈导致手牌没有了，也视作使用成功
                return true;
            }
            Card c = getSource().chooseTargetCards(getTarget(), true);
            if (c == null) {// 如果是AI 则会出现抽空的情况
                return true;
            }
            getTarget().loseCard(c, true);
            String result = Text.format(",%s",
                   c.getHtmlNameWithColor());
            // 这个是判断过无懈的消息
            MsgObj msg = getTarget().getTempActionMsgObj("wuxie");
            msg.text = msg.text + result;
            //sleep(1000);
            getGameManager().getMsgAPI().editCaptionForce (msg);
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "过河拆桥";
    }

    @Override
    public boolean askTarget(Person user) {
        user.getPriviMsg().setOneTimeInfo1("\n💬请选择区域里有牌的一名其他角色。你弃置其区域里的一张牌。");  
        return super.askTarget(user);
    }

    @Override
    public boolean needChooseTarget() {
        return true;
    }

    @Override
    public boolean asktargetAddition(Person user, Person p) {
        if (p.getCardsAndEquipments().isEmpty() && p.getJudgeCards().isEmpty()) {
            getSource().getPriviMsg().setOneTimeInfo1("\n💬你不能选择没有手牌的玩家");
            return false;
        }
        return true;
    }

    @Override
    public String details() {
        return "出牌阶段，对区域里有牌的一名其他角色使用。你弃置其区域里的一张牌。";
    }
}
