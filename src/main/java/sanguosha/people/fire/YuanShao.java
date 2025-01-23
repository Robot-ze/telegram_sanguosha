package sanguosha.people.fire;

import static sanguosha.people.Identity.KING;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import config.Text;
import sanguosha.cards.Card;
import sanguosha.cards.Color;
import sanguosha.cards.strategy.WanJianQiFa;
import sanguosha.people.Identity;
import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.ForcesSkill;
import sanguosha.skills.KingSkill;
import sanguosha.skills.Skill;

public class YuanShao extends Person {
    private Set<Color> thisRoundColors = new HashSet<>();

    public YuanShao() {
        super(4, Nation.QUN);
    }

    @Override
    public void usePhaseBefore() {

        if (isActiveSkill("乱击") && getCards().size() > 1) {
            getSkillCards().add("乱击");
        }
    }

    @Skill("乱击")
    @Override
    public boolean useSkillInUsePhase(int orderInt) {

        // int orderInt = Integer.valueOf(order) - 1;

        if (orderInt < getSkillCards().size() && getSkillCards().get(orderInt).equals("乱击")) {
            // printlnPriv(this + " uses 乱击");
            Card c1;
            Card c2;

            List<Card> canUseCards = new ArrayList<>();

            for (Card c : getCards()) {
                if (!thisRoundColors.contains(c.color())) {
                    canUseCards.add(c);
                }
            }

            if (canUseCards.size() < 2) {
                return false;
            }
            // getPriviMsg().setOneTimeInfo1("");
            // printlnToIOPriv("choose two cards of same color");
            getPriviMsg().setOneTimeInfo1(Text.format("乱击：你可以将两张花色相同的手牌当[万箭齐发]使用,请选择两张花色相同的牌,不能使用本轮使用过的花色"));
            ArrayList<Card> cardList = chooseManyFromProvided(2, canUseCards, true);
            if (cardList.size() != 2) {
                return false;
            }
            c1 = cardList.get(0);
            c2 = cardList.get(1);
            if (c1 == null || c2 == null) {
                return false;
            }
            if (c1.color() != c2.color()) {
                String res = "乱击:必须选择两张颜色一样的牌";
                getGameManager().getIo().delaySendAndDelete(this, res);
                return false;
            }
            ArrayList<Card> thisCard = new ArrayList<>();
            thisCard.add(c1);
            thisCard.add(c2);
            thisRoundColors.add(c1.color());
            WanJianQiFa wan = new WanJianQiFa(getGameManager(), c1.color(), 0);
            loseCard(thisCard);
            wan.setReplaceCards(thisCard);
            wan.setIsFake(true);
            wan.setSource(this);
            String res = Text.format("<i>弓箭手，准备放箭！</i>");
            getGameManager().getIo().printlnPublic(res, toString());
            //sleep(1000);
            wan.use();

        }
        return false;
    }

    @Override
    public void selfEndPhase(boolean fastMode) {
        thisRoundColors.clear();
    }

    @ForcesSkill("血裔")
    @KingSkill("血裔")
    @Override
    public int throwPhase(boolean fastMode) {
        if (isActiveSkill("血裔") && getIdentity() == Identity.KING) {
            int num = getCards().size() - getHP()
                    - 2 * getGameManager().peoplefromNation(Nation.QUN).size();
            if (num > 0) {

                ArrayList<Card> cs;
                if (fastMode) {// 快速模式直接丢掉后面的牌
                    cs = new ArrayList<>();//
                    for (int i = 0; i < num; i++) {
                        cs.add(getCards().get(getCards().size() - 1 - i));
                    }
                } else {
                    getPriviMsg().setOneTimeInfo1(Text.format("\n💬你需丢弃 %s 张牌", num));
                    cs = chooseManyFromProvided(num, getCards(),true);
                }

                if (cs.size() < num) {// 如果选不够，强制从前面丢起
                    for (Card c : getCards()) {
                        if (cs.indexOf(c) >= 0) {
                            continue;
                        }
                        cs.add(c);
                        if (cs.size() >= num) {
                            break;
                        }
                    }
                }

                loseCard(cs);
                for (Person p : getGameManager().getPlayersBeginFromPlayer(this)) {
                    p.otherPersonThrowPhase(this, cs);
                }
            }
            return num;
        } else {
            return super.throwPhase(fastMode);
        }
    }

    @Override
    public Set<String> getInitialSkills() {
        Set<String> skills = super.getInitialSkills();

        if (getIdentity() != KING) {
            skills.remove("血裔");
        }
        return skills;
    }

    // @Override
    // public void clearAndAddActiveSkill(String... skills) {
    // // TODO Auto-generated method stub
    // super.clearAndAddActiveSkill(skills);
    // }

    @Override
    public String name() {
        return "袁绍";
    }

    @Override
    public String skillsDescription() {
        return "乱击：你可以将两张花色相同的手牌当[万箭齐发]使用。\n" +
                "血裔：主公技，锁定技，你的手牌上限+X（X为其他群势力角色数的两倍）。";
    }
}
