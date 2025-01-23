package sanguosha.people.wei;

import config.Config;
import config.Text;
import msg.CallbackEven;
import sanguosha.cards.Card;
import sanguosha.cards.Color;
import sanguosha.cardsheap.CardsHeap;

import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.Skill;
import components.TimeLimit;

public class ZhenJi extends Person {
 

    public ZhenJi() {
        super(3, "female", Nation.WEI);
    }

    @Skill("洛神")
    @Override
    public void selfBeginPhase() {
        // getPriviMsg().setOneTimeInfo1("洛神：准备阶段，你可以进行判定，若结果为黑色，你获得此牌，然后你可以重复此流程");

        if (launchSkillPublicDeepLink(
                "洛神",
                "准备阶段，多摸一张牌，若结果为黑色，你获得此牌，然后你可以重复此流程",
                "luoshen")) {
                    setskipNoticeUsePublic(true);
            int count = 0;
            Card c = getGameManager().getCardsHeap().judge(this, new CallbackEven() {
                @Override
                public boolean juge(Card card) {
                    return card.isBlack();
                }
            });
            TimeLimit t = new TimeLimit(Config.PRIV_RND_TIME_60S);
            while (getGameManager().isRunning() && c.isBlack()) {
                if (t.isTimeout()) {
                    return;
                }
                addCard(getGameManager().getCardsHeap().getJudgeCard());
                count++;
                String res = "洛神：准备阶段，多摸一张牌，你获得此牌，然后你可以重复此流程\n你已获得牌数:" + count;
                getPriviMsg().setOneTimeInfo1(res);
                if (launchSkillPriv("洛神")) {
                    c = getGameManager().getCardsHeap().judge(this, new CallbackEven() {
                        @Override
                        public boolean juge(Card card) {
                            return card.isBlack();
                        }
                    });
                } else {
                    break;
                }
            }

            String result = Text.format("%s 通过 %s 获取了%s张牌:<i>髣髴兮若轻云之蔽月。</i>",
                    getPlateName(),
                    getSkillHtmlName("洛神"),
                    count + "");
       
            getGameManager().getIo().printlnPublic(result, toString());
            //sleep(3000);

        }
    }

 

    @Override
    public void selfEndPhase(boolean fastMode) {
        if(!isZuoCi()){
            setskipNoticeUsePublic(false); 
        }
    
 
    }

    @Skill("倾国")
    @Override
    public boolean skillShan(Person sourse) {

        getPriviMsg().setOneTimeInfo1("倾国：你可以将一张黑色手牌当[闪]使用或打出。");
        if (launchSkillPriv("倾国")) {
            Card c = requestRedBlack("black", true);
            if (c != null) {
                String result = Text.format("<i>凌波微步，罗袜生尘</i>"
                );
                //sleep(1000);
                getGameManager().getIo().printlnPublic(result, toString());
                //sleep(3000);
            }
            return c != null;
        }

        return false;
    }

    @Override
    public String name() {
        return "甄姬";
    }

    @Override
    public String skillsDescription() {
        return "倾国：你可以将一张黑色手牌当[闪]使用或打出。\n" +
                "洛神：准备阶段，你可以进行判定，若结果为黑色，你获得此牌，然后你可以重复此流程。";
    }
}
