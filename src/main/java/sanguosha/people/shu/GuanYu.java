package sanguosha.people.shu;

import config.Text;
import sanguosha.cards.Card;
import sanguosha.cards.basic.Sha;

import sanguosha.cardsheap.CardsHeap;
import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.Skill;

public class GuanYu extends Person {
    public GuanYu() {
        super(4, Nation.SHU);
    }

    @Skill("武圣")
    public Card wuSheng() {
        Card c = requestRedBlack("red", true);

        return c;
    }

    @Override
    public boolean skillSha(Person sourse) {
        if (launchSkillPriv("武圣")) {
            Card c = wuSheng();
            if (c != null) {
                String res = Text.format("%s %s:<i>看尔乃插标卖首！</i>",
                        getPlateName(), getSkillHtmlName("武圣"));
                getGameManager().getIo().printlnPublic(res, toString());
                // sleep(3000);
            }
            return c != null;
        }
        return false;
    }

    @Override
    public void usePhaseBefore() {

        if (isActiveSkill("武圣") && checkSha() && hasRedHandCard()) {
            getSkillCards().add("武圣");
        }
    }

    @Override
    public boolean useSkillInUsePhase(int orderInt) {
        // int orderInt = Integer.valueOf(order) - 1;

        if (orderInt < getSkillCards().size() && getSkillCards().get(orderInt).equals("武圣")) {
            Card c = wuSheng();
            if (c == null) {
                return true;
            }
            if (!checkSha()) {
                return false;
            }
            Sha sha = new Sha(getGameManager(), c.color(), c.number());

            sha.addReplaceCard(c);
            if (sha.askTarget(this)) {

                String res = Text.format("%s %s:<i>看尔乃插标卖首！</i>",
                        getPlateName(), getSkillHtmlName("武圣"));
                // sleep(3000);
                getGameManager().getIo().printlnPublic(res, toString());

                useCard(sha);
            } else {
                addCard(getGameManager().getCardsHeap().retrieve(c));
            }

            return true;
        }
        return false;
    }

    @Override
    public String name() {
        return "关羽";
    }

    @Override
    public String skillsDescription() {
        return "武圣：你可以将一张红色牌当[杀]使用或打出。";
    }
}
