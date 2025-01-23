package sanguosha.cards.strategy;

import config.Text;
import msg.MsgObj;
import sanguosha.cards.Card;
import sanguosha.cards.Color;
import sanguosha.cards.Strategy;
import sanguosha.manager.GameManager;
import sanguosha.people.Person;

public class ShunShouQianYang extends Strategy {

    public ShunShouQianYang(GameManager gameManager, Color color, int number) {
        super(gameManager, color, number, 1);
    }

    @Override
    public Object use() {

        Person p = getTarget();

        if (!gotWuXie(p)) {
            getSource().getPriviMsg().setOneTimeInfo1(Text.format("\n💬你对 %s 使用了顺手牵羊，请选择TA的1张手牌", getTarget()));
            if (getTarget().getAllCardSize() <= 0) { // 别人打出无懈导致手牌没有了，也视作使用成功
                return true;
            }
            Card c = getSource().chooseTargetCards(getTarget(), true);
            if (c == null) {
                return true;
            }
            getTarget().loseCard(c, false);
            getSource().addCard(c);

            String result = Text.format(",偷1张牌" );
            // 这个是判断过无懈的消息
            MsgObj msg = getTarget().getTempActionMsgObj("wuxie");
            msg.text = msg.text + result;
            //sleep(1000);
            getGameManager().getMsgAPI().editCaptionForce(msg);
            return true;
        }
        return false;
    }

    @Override
    public boolean askTarget(Person user) {
        user.getPriviMsg().setOneTimeInfo1("\n💬请选择距离为1且区域里有牌的一名其他角色。你获得其区域里的一张牌。");
        return super.askTarget(user);
    }

    @Override
    public String toString() {
        return "顺手牵羊";
    }

    @Override
    public boolean needChooseTarget() {
        return true;
    }

    @Override
    public boolean asktargetAddition(Person user, Person p) {
        if (p.hasQianXun()) {
            getSource().getPriviMsg().setOneTimeInfo1("\n💬不能选择拥有 谦逊 技能的目标");
            return false;
        }
        if (p.getCardsAndEquipments().isEmpty() && p.getJudgeCards().isEmpty()) {
            getSource().getPriviMsg().setOneTimeInfo1("\n💬不能选择没有手牌的目标");
            return false;
        }
        return true;
    }

    @Override
    public String details() {
        return "出牌阶段，对距离为1且区域里有牌的一名其他角色使用。你获得其区域里的一张牌。";
    }
}
