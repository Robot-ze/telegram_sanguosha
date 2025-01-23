package sanguosha.cards.equipments.weapons;

import java.util.List;

import config.Text;
import msg.MsgObj;
import sanguosha.cards.Card;
import sanguosha.cards.Color;
import sanguosha.cards.equipments.Weapon;
import sanguosha.manager.GameManager;
import sanguosha.manager.Utils;
import sanguosha.people.AI;

public class HanBingJian extends Weapon {
    public HanBingJian(GameManager gameManager, Color color, int number) {
        super(gameManager, color, number, 2);
    }

    @Override
    public Object use(Card sourceCard) {
        if (getTarget().getCardsAndEquipments().isEmpty()) {
            return false;
        }

        boolean active = false;
        if (getOwner() .isAI()) {
            return false;
        } else {

            active = getSource().launchSkillPublic(
                    "寒冰剑",

                    Text.format("%s 是否用 %s ,你可以改伤害为随机弃置TA两张牌。",
                            getOwner().getHtmlName(), getHtmlName()),
                    "hanbingjian");

        }

        if (!active) {
            return false;
        }

        if (getTarget().getCardsAndEqSize() <= 0) {
            return true;
        }
        int count = 0;

        for ( ; count < 2; count++) {// 改为随机弃置
            List<Card> cards = getTarget().getCardsAndEquipments();
            if (cards.size() <= 0) {
                break;
            }
            Card c = cards.get(Utils.randint(0, cards.size() - 1));
            getTarget().loseCard(c);
      
        }

        MsgObj msg = getTarget().getTempActionMsgObj("sha");
        if (msg != null) {
            String result = Text.format("\n%s 被弃置%s张牌",
                    getTarget().getHtmlName(), count + "");
            msg.text = msg.text + result;
            getGameManager().getMsgAPI().editCaptionForce(msg);
        }

        return true;
    }

    @Override
    public String toString() {
        return "寒冰剑";
    }

    @Override
    public String details() {
        return "每当你使用[杀]对目标角色造成伤害时，若该角色有牌，你可以防止此伤害，改为依次弃置其两张牌。";
    }
}
