package sanguosha.people.wei;

import sanguosha.cards.Card;
import sanguosha.cardsheap.CardsHeap;

import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.Skill;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import config.Text;
import msg.MsgObj;

public class GuoJia extends Person {
    public GuoJia() {
        super(3, Nation.WEI);
    }

    @Skill("天妒")
    @Override
    public void receiveJudge(Card card) {

        if (card == null) {
            return;
        }

        if (launchSkillPublic(
                "使用天妒",
                Text.format(
                        "%s 天妒：判定牌 %s 生效，你可以获得此牌。",
                        getHtmlName(), card.getHtmlNameWithColor()),
                "tiando")) {
            getGameManager().getCardsHeap().retrieve(card);
            addCard(card);
            MsgObj publicMsgObj=getTempActionMsgObj("tiando");
            publicMsgObj.text=publicMsgObj.text+"✅<i>如你所愿。</i>";
            publicMsgObj.isDeleted=false;
            getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
            publicMsgObj.isDeleted=true;
        }
    }

    @Skill("遗计")
    @Override
    public void gotHurt(List<Card> cards, Person source, int num) {
        String code = "yiji1";
        int num2 = num * 2;
        MsgObj publicMsgobj = MsgObj.newMsgObj(getGameManager());
        if (launchSkillPublicDeepLink(
                publicMsgobj,
                "遗计", Text.format(
                        "当你受到1点伤害后，你可以观看牌堆顶的两张牌，然后将这些牌交给任意角色。",
                        getHtmlName()),
                code)) {

            ArrayList<Card> drawCards = getGameManager().getCardsHeap().draw(num2);
            String cardsString = "";
            for (Card c : drawCards) {
                cardsString += "[" + c.getHtmlNameWithColor() + "]";
            }
            String result = Text.format(
                    "\n牌堆顶部%s张牌如下:%s\n请选择一个角色,TA可以获取以上牌", num2, cardsString);
            getPriviMsg().setOneTimeInfo1(result);
            Person p = selectPlayer(true);
            MsgObj publicMsgObj = getTempActionMsgObj(code);
            if (p != null) {
                p.addCard(drawCards);

                result = Text.format(
                        ",%s 得到%s张牌:<i>大道之理，只可意会不可言传。</i>",
                        p.getHtmlName(), 
                        num2);
                publicMsgObj.text = publicMsgObj.text + result;
                publicMsgObj.isDeleted = false;
                getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
                publicMsgObj.isDeleted = true;

            } else {// 如果不给别人，就放回牌堆
                Deque<Card> heap = getGameManager().getCardsHeap().getDrawCards(0);
                for (Card c : drawCards) {// 要倒序插进去
                    heap.addFirst(c);
                }
                result = Text.format(
                        ",没有人获得牌,牌将放回牌堆");
                publicMsgObj.text = publicMsgObj.text + result;
                publicMsgObj.isDeleted = false;
                getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
                publicMsgObj.isDeleted = true;
            }

        }  

    }

    @Override
    public String name() {
        return "郭嘉";
    }

    @Override
    public String skillsDescription() {
        return "天妒：当你的判定牌生效后，你可以获得此牌。\n" +
                "遗计：当你受到1点伤害后，你可以观看牌堆顶的两张牌，然后将这些牌交给任意角色。";
    }
}
