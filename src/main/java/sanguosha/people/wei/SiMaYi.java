package sanguosha.people.wei;

import sanguosha.cards.Card;

import sanguosha.cardsheap.CardsHeap;
import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.Skill;

import java.util.ArrayList;
import java.util.List;

import config.Text;
import msg.MsgObj;

public class SiMaYi extends Person {
    public SiMaYi() {
        super(3, Nation.WEI);
    }

    @Skill("鬼才")
    @Override
    public Card changeJudge(Person target, Card d) {

        MsgObj publicMsgObj = MsgObj.newMsgObj(getGameManager());
         //伤害和那些什么写在改判牌这里
        target.putTempActionMsgObj("changeJudge",publicMsgObj);
        String code = "guicai";
        if (launchSkillPublicDeepLink(
                publicMsgObj,
                "鬼才",
                Text.format("%s 的判定牌是 %s，你可以替换之。",
                        target.getPlateName(),
                        d.getHtmlNameWithColor()),
                code)) {
            getPriviMsg().setOneTimeInfo1(Text.format("\n💬请出牌，代替 %s 的判定牌 %s",
                    target.getPlateName(),
                    d.getHtmlNameWithColor()));
            Card c = requestCard(null);
            if (c != null) {
                
                 
                getGameManager().getCardsHeap().retrieve(c);
                getGameManager().getCardsHeap().discard(d);

                String result = Text.format(",改判 %s:<i>吾乃天命之子！</i>",
              
                         c.getHtmlNameWithColor());
                publicMsgObj.text = publicMsgObj.text + result;
                publicMsgObj.isDeleted = false;
                getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
                publicMsgObj.isDeleted = true;
                return c;
            }  
        } 
        return null;
    }

    @Skill("反馈")
    @Override
    public void gotHurt(List<Card> cards, Person p, int num) {
        if (p != null && p.getCardsAndEqSize() > 0) {
            MsgObj publicMsgObj = MsgObj.newMsgObj(getGameManager());
            String code = "fankui";
            if (launchSkillPublicDeepLink(
                    publicMsgObj,
                    "反馈",
                    Text.format("受到 %s 的伤害，可获得TA一张牌。",
                             p.getPlateName()),
                    code)) {
      

                Card c = chooseTargetCards(p);
                p.loseCard(c, false);
                addCard(c);

                String result = "✅<i>出来混，早晚要还的！</i>";
                publicMsgObj.text = publicMsgObj.text + result;
                publicMsgObj.isDeleted = false;
                getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
                publicMsgObj.isDeleted = true;
            } 
        }
    }

    @Override
    public String name() {
        return "司马懿";
    }

    @Override
    public String skillsDescription() {
        return "反馈：当你受到伤害后，你可以获得伤害来源的一张牌。\n" +
                "鬼才：当一名角色的判定牌生效前，你可以出手牌代替之。";
    }
}
