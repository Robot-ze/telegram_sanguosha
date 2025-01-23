package sanguosha.people.wu;

import config.Config;
import config.Text;
import sanguosha.cards.Card;
import sanguosha.cards.Color;
import sanguosha.cards.JudgeCard;
import sanguosha.cards.basic.Sha;
import sanguosha.cards.strategy.judgecards.LeBuSiShu;

import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.Skill;
import components.TimeLimit;

public class DaQiao extends Person {
    public DaQiao() {
        super(3, "female", Nation.WU);
    }

    @Override
    public void usePhaseBefore() {

        if (isActiveSkill("国色") && getCards().size() > 0) {
            getSkillCards().add("国色");
        }
    }

    @Skill("国色")
    @Override
    public boolean useSkillInUsePhase( int orderInt) {
        //int orderInt = Integer.valueOf(order) - 1;

        if (orderInt < getSkillCards().size() && getSkillCards().get(orderInt).equals("国色")) {
            getPriviMsg().setOneTimeInfo1("国色：你可以将一张方块牌当[乐不思蜀]使用。");
            Card c = requestColor(Color.DIAMOND, true);
            if (c == null) {
                return true;
            }
            LeBuSiShu le = new LeBuSiShu(getGameManager(), c.color(), c.number());
            le.addReplaceCard(c);
            getPriviMsg().setOneTimeInfo1("国色：你可以将一张方块牌当[乐不思蜀]使用。请选择角色");
            if (le.askTarget(this)) {
                useCard(le);
            } else {
                addCard(getGameManager().getCardsHeap().retrieve(c), false);
            }
            String res=Text.format("%s %s:<i>倾心一笑，愿君驻足。</i>", getPlateName(),getSkillHtmlName("国色"));   
            getGameManager().getIo().printlnPublic(res, toString());
            //sleep(3000);

            return true;
        }
        return false;
    }

    @Skill("流离")
    @Override
    public Person changeSha(Sha sha) {
        if (launchSkillPublicDeepLink(
                "流离",
                "当你成为[杀]的目标时，你可以弃置一张牌并将此[杀]转移给你攻击范围内的一名其他角色。（不能是此[杀]的使用者）",
                "liuli1")) {
            getPriviMsg().setOneTimeInfo1("流离：当你成为[杀]的目标时，你可以弃置一张牌并将此[杀]转移给你攻击范围内的一名其他角色。（不能是此[杀]的使用者）\n请选择一张卡");
            Card c = requestCard(null);
            if (c == null) {
                return null;
            }

            Person p = null;
            getPriviMsg().setOneTimeInfo1("流离： 请选择一个转移目标");

            p = selectPlayerExept(getGameManager().reachablePeople(this, getShaDistance()), sha.getSource());
            if (p == null) {
                addCard(c);
                return null;
            }
            String res = Text.format("%s 将杀转移给 %s:<i>你来嘛~</i>", getPlateName(), p.getHtmlName());
       
            getGameManager().getIo().printlnPublic(res, toString());
            //sleep(3000);

            return p;

        }
        return null;
    }

    @Override
    public String name() {
        return "大乔";
    }

    @Override
    public String skillsDescription() {
        return "国色：你可以将一张方块牌当[乐不思蜀]使用。\n" +
                "流离：当你成为[杀]的目标时，你可以弃置一张牌并将此[杀]转移给你攻击范围内的一名其他角色。（不能是此[杀]的使用者）";
    }
}
