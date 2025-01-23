package sanguosha.cards.equipments.weapons;

import java.util.ArrayList;
import java.util.List;

import config.Text;
import msg.MsgObj;
import sanguosha.cards.Card;
import sanguosha.cards.Color;
import sanguosha.cards.equipments.Weapon;
import sanguosha.manager.GameManager;

public class GuanShiFu extends Weapon {
    public GuanShiFu(GameManager gameManager, Color color, int number) {
        super(gameManager, color, number, 3);
    }

    @Override
    public Object use(Card sourceCard) {
        if (getOwner().getCardsAndEquipments().size() < 2) {
            // getSource().getInText().setOneTimeInfo1("\n💬你没有足够的卡片");
            return null;
        }
         

        MsgObj publicMsgObj = MsgObj.newMsgObj(getGameManager());
        boolean active = false;
        if (getOwner().isAI()) {
            return false;
        } else {
            active = getOwner().launchSkillPublicDeepLink(
                    publicMsgObj,
                    "贯石斧",
                    Text.format("%s 是否用 %s",
                            getOwner().getHtmlName(), getHtmlName()),
                    "guanshifu");

        }

        if (active) {
            getSource().getPriviMsg().setOneTimeInfo1("贯石斧:请弃置两张牌,令此[杀]依然对其造成伤害");
            List<Card> cardList = new ArrayList<>(); // 为什么不直接用getCardsAndEquipments(),因为这时那张杀 牌还没丢，
            for (Card c : getSource().getCardsAndEquipments()) {
                if (c == sourceCard) {
                    continue;
                }
                cardList.add(c);
            }


            List<Card> cs= getSource().chooseManyFromProvided(2, cardList,true);
            if(cs.size()<2){
                return null;
            }
            getSource().throwCard(cs);

            try {
                return true;
            } finally {
                System.out.println("getSource()="+getSource()+":target="+getTarget());

                String res = Text.format(",<i>贯石斧生效!</i>%s",  getTarget().getHtmlName());
                publicMsgObj.appendText(res);
                getSource().putTempActionMsgObj("guanshifu", publicMsgObj);
                //sleep(1000);
                getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "贯石斧";
    }

    @Override
    public String details() {
        return "每当你使用的[杀]被目标角色使用的[闪]抵消时，你可以弃置两张牌，令此[杀]依然对其造成伤害。\n";
    }
}
