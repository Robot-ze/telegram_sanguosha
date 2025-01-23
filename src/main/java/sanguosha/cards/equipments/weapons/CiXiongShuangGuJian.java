package sanguosha.cards.equipments.weapons;

import config.Text;
import db.ImgDB;
import msg.MsgObj;
import sanguosha.cards.Card;
import sanguosha.cards.Color;
import sanguosha.cards.equipments.Weapon;
import sanguosha.manager.GameManager;
import sanguosha.people.AI;

public class CiXiongShuangGuJian extends Weapon {

    public CiXiongShuangGuJian(GameManager gameManager, Color color, int number) {
        super(gameManager, color, number, 2);
    }

    @Override
    public Object use(Card sourceCard) {

        if (getTarget().getSex().equals(getSource().getSex())) {
            return null;
        }

        getSource().getPriviMsg().clearHeader2();
        getSource().getPriviMsg().setOneTimeInfo1("\n💬你是否用 雌雄双股剑:每当你使用[杀]指定一名异性的目标角色后，你可以令其选择一项：1.弃置一张手牌；2.令你摸一张牌。");
        if (!getSource().launchSkillPriv("雌雄双股剑")) {
            return null;
        }

        // =========这里插入在公开群的点击连接过来
        MsgObj publicMsg = MsgObj.newMsgObj(getGameManager());
        boolean activeDeal;

        if (getTarget() .isAI()) {
            activeDeal = true;
            publicMsg.chatId = getGameManager().getChatId();
            ImgDB.setImg(publicMsg, this.toString());
            publicMsg.text = Text.format("%s,%s 对你使用了 %s ,请你选择一项",
                    getTarget().getHtmlName(),
                    getSource().getPlateName(),
                    getHtmlNameWithColor());
            getGameManager().getMsgAPI().sendImg(publicMsg);

        } else {
            activeDeal = getGameManager().getMsgAPI().noticeAndAskPublic(
                    publicMsg,
                    this,
                    getTarget(),
                    Text.format("%s,%s 对你使用了 %s ,请你选择一项",
                            getTarget().getHtmlName(),
                            getSource().getPlateName(),
                            getHtmlNameWithColor()),
                    "处理", "cixiong");

        }

        if (!activeDeal) {
            getSource().drawCard();
            String result = Text.format("\n%s 新抽了一张牌",
                    getSource().getHtmlName());
            publicMsg.text = publicMsg.text + result;
            publicMsg.replyMakup = null;
            getGameManager().getMsgAPI().editCaptionForce(publicMsg);
            //sleep(3000);
            return null;
        }

        getTarget().getPriviMsg().clearHeader2();
        getTarget().getPriviMsg().setOneTimeInfo1("\n💬你被 雌雄双股剑 攻击了，你必须择一项：1.弃置一张手牌；2.令TA摸一张牌。");
        int choice = getTarget().chooseNoNull(
                "你弃置一张牌", "TA摸一张牌");
        if (choice == 1) {
            Card c = getTarget().requestCard(null);
            if (c == null) {
                getSource().drawCard();
                String result = Text.format("\n%s 新抽了一张牌",
                        getSource().getHtmlName());
                publicMsg.text = publicMsg.text + result;
                publicMsg.replyMakup = null;
                getGameManager().getMsgAPI().editCaptionForce(publicMsg);
            } else {
                String result = Text.format("\n%s 弃置一张牌 %s",
                        getTarget().getHtmlName(),
                        c.getHtmlNameWithColor());
                publicMsg.text = publicMsg.text + result;
                publicMsg.replyMakup = null;
                getGameManager().getMsgAPI().editCaptionForce(publicMsg);
            }
        } else {
            getSource().drawCard();
            String result = Text.format("\n%s 新抽了一张牌",
                    getSource().getHtmlName());
            publicMsg.text = publicMsg.text + result;
            publicMsg.replyMakup = null;
            getGameManager().getMsgAPI().editCaptionForce(publicMsg);
        }
        return null;
    }

    @Override
    public String toString() {

        return "雌雄双股剑";
    }

    @Override
    public String details() {
        return "每当你使用[杀]指定一名异性的目标角色后，你可以令其选择一项：1.弃置一张手牌；2.令你摸一张牌。";
    }
}
