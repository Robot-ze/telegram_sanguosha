package sanguosha.people.god;

import java.util.ArrayList;
import java.util.HashSet;

import config.Config;
import config.Text;
import sanguosha.cards.Card;
import sanguosha.cards.Color;
import sanguosha.people.Person;
import sanguosha.skills.Skill;
import components.TimeLimit;

public class ShenLvMeng extends God {
    public ShenLvMeng() {
        super(3, null);
    }

    @Skill("涉猎")
    @Override
    public void drawPhase(boolean fastMode) {

        getPriviMsg().setOneTimeInfo1("💬摸牌阶段，你可以改为亮出牌堆顶的五张牌，然后获得其中每种花色的牌的第一张。");
        if (! fastMode&&launchSkillPriv("涉猎")) {
            // ArrayList<Card> cards = new
            // ArrayList<>(getGameManager().getCardsHeap().getDrawCards(5).subList(0, 5));
            ArrayList<Card> cards = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                cards.add(getGameManager().getCardsHeap().draw());
            }

            ArrayList<Card> selected = new ArrayList<>();
            // getGameManager().getIo().printCardsPublic(cards);
            TimeLimit t = new TimeLimit(Config.PRIV_RND_TIME_60S);
            while (getGameManager().isRunning() && t.isNotTimeout()) {
                String res = "";
                getPriviMsg().setOneTimeInfo1("💬请选择最多4张牌,不能有重复的花色");
                selected = chooseManyCards(0, cards);
                if (selected.size() <= 0) {
                    break;
                }
                HashSet<Color> existingColors = new HashSet<>();
                boolean colorDuplicated = false;
                for (Card c : selected) {
                    if (existingColors.contains(c.color())) {
                        colorDuplicated = true;
                        break;
                    }
                    existingColors.add(c.color());
                    res += "[" + c.getHtmlNameWithColor() + "]";
                }
                if (colorDuplicated) {
                    continue;
                }
                addCard(selected);
                cards.removeAll(selected);
                getGameManager().getCardsHeap().discard(cards);

                String res2 = Text.format("%s 获得 %s:<i>为将者，自当识天晓地。</i>",
                        getSkillHtmlName("涉猎"), res);
                //sleep(1000);
                getGameManager().getIo().printlnPublic(res2, toString());
                return;
            }
        }
        super.drawPhase(  fastMode);
    }

    @Override
    public void usePhaseBefore() {

        if (isActiveSkill("攻心") && hasNotUsedSkill1()) {
            getSkillCards().add("攻心");
        }
    }

    @Skill("攻心")
    @Override
    public boolean useSkillInUsePhase(int orderInt) {
        // int orderInt = Integer.valueOf(order) - 1;

        if (orderInt < getSkillCards().size() && getSkillCards().get(orderInt).equals("攻心") && hasNotUsedSkill1()) {
            getPriviMsg().setOneTimeInfo1("攻心：出牌阶段限一次，你可以观看一名其他角色的手牌，然后你可以展示其中一张红桃牌，选择一项：1.弃置此牌；2.将此牌置于牌堆顶。");
            Person p = selectPlayer();
            if (p != null) {
                setHasUsedSkill1(true);

                // printCards(p.getCards());
                // printlnToIOPriv("choose a HEART card, or q to 取消(Esc)");
                TimeLimit t = new TimeLimit(Config.PRIV_RND_TIME_60S);
                while (getGameManager().isRunning() && t.isNotTimeout()) {
                    getPriviMsg().setOneTimeInfo1("攻心：TA的手牌如下,你可以展示其中一张红桃牌，选择一项：1.弃置此牌；2.将此牌置于牌堆顶。");

                    Card c = chooseCard(p.getCards(), true);
                    if (c != null) {
                        if (c.color() != Color.HEART) {
                            continue;
                        }
                        String res;
                        if (chooseNoNull("弃此牌", "放回牌堆顶") == 1) {
                            p.loseCard(c);
                            res=Text.format("%s 弃置 %s", p.getHtmlName(),c.getHtmlNameWithColor());
                        } else {
                            p.loseCard(c, false);
                            getGameManager().getCardsHeap().getDrawCards(0).addFirst(c);
                            res=Text.format("%s 的 %s 被放回牌堆顶", p.getHtmlName(),c.getHtmlNameWithColor());

                        }
                        String res2= Text.format("%s %s:<i>在我的眼中，你没有秘密。</i>", 
                        getSkillHtmlName("攻心"),res);
                        //sleep(1000);
                        getGameManager().getIo().printlnPublic(res2, toString());

                    }
                    return true;
                }
                return true;

            }

        }
        return false;
    }

    @Override
    public String name() {
        return "神吕蒙";
    }

    @Override
    public String skillsDescription() {
        return "涉猎：摸牌阶段，你可以改为亮出牌堆顶的五张牌，然后获得其中每种花色的牌各一张。\n" +
                "攻心：出牌阶段限一次，你可以观看一名其他角色的手牌，然后你可以展示其中一张红桃牌，选择一项：1.弃置此牌；2.将此牌置于牌堆顶。";
    }
}
