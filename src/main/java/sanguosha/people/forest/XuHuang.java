package sanguosha.people.forest;

import config.Text;
import sanguosha.cards.Card;
import sanguosha.cards.JudgeCard;
import sanguosha.cards.Strategy;
import sanguosha.cards.strategy.judgecards.BingLiangCunDuan;
import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.Skill;

public class XuHuang extends Person {
    public XuHuang() {
        super(4, Nation.WEI);
    }

    @Override
    public void usePhaseBefore() {

        if (isActiveSkill("断粮") && getCards().size() > 0) {
            getSkillCards().add("断粮");
        }
    }

    @Skill("断粮")
    @Override
    public boolean useSkillInUsePhase(int orderInt) {
        // int orderInt=Integer.valueOf(order)-1 ;

        if (orderInt < getSkillCards().size() && getSkillCards().get(orderInt).equals("断粮")) {
            getPriviMsg().clearHeader2();
            getPriviMsg()
                    .setOneTimeInfo1(Text.format("\n💬请选择一张黑色牌，断粮：你可以将一张黑色基本牌或黑色装备牌当[兵粮寸断]使用；你可以对距离为2的角色使用[兵粮寸断]。"));
            
                    Card c = requestRedBlack("black", true);
            if (c == null) {
                return true;
            }
            if (c instanceof Strategy) {
                // printlnToIOPriv("you should use black basic card or equipment");
                getGameManager().getIo().delaySendAndDelete(this, "牌型错误，不能选锦囊牌");
                addCard(c, false);
                return true;
            }
            BingLiangCunDuan bing = new BingLiangCunDuan(getGameManager(), c.color(), c.number());
            bing.setOwner(this);
            bing.addReplaceCard(c);
            if (bing.askTarget(this)) {
                String res = Text.format("<i>截其源，断其粮，贼可擒也！</i>" );
                //sleep(1000);
                getGameManager().getIo().printlnPublic(res, toString());

                useCard(bing);

            } else {
                addCard(getGameManager().getCardsHeap().retrieve(c), false);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean hasDuanLiang() {
        return true;
    }

    @Override
    public String name() {
        return "徐晃";
    }

    @Override
    public String skillsDescription() {
        return "断粮：你可以将一张黑色基本牌或黑色装备牌当[兵粮寸断]使用；你可以对距离为2的角色使用[兵粮寸断]。";
    }
}
