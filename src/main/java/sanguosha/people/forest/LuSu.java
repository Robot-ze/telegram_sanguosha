package sanguosha.people.forest;

import sanguosha.cards.Card;
import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.Skill;
import components.TimeLimit;

import java.util.ArrayList;
import java.util.List;

import config.Config;
import config.Text;

public class LuSu extends Person {
    public LuSu() {
        super(3, Nation.WU);
    }

    @Skill("好施")
    @Override
    public void drawPhase(boolean fastMode) {

        getPriviMsg().clearHeader2();
        getPriviMsg()
                .setOneTimeInfo1(Text.format("\n💬是否用 好施:你可以多摸两张牌，若你的手牌数大于5，可将一半的手牌交给手牌最少的一名其他角色。"));
        if (! fastMode&&launchSkillPriv("好施")) {
            drawCards(4);
            if (getCards().size() > 5) {
                ArrayList<Person> minPeople = new ArrayList<>();
                int minNum = 10000;
                for (Person p : getGameManager().getPlayersBeginFromPlayer(this)) {
                    if (p == this) {
                        continue;
                    }
                    if (p.getCards().size() == minNum) {
                        minPeople.add(p);
                    } else if (p.getCards().size() < minNum) {
                        minNum = p.getCards().size();
                        minPeople.clear();
                        minPeople.add(p);
                    }
                }
                if (minPeople.size() > 0) {
                    Person minPerson = minPeople.get(0);
                    if (minPeople.size() > 1) {
                        getPriviMsg().setOneTimeInfo1("好施:请在以下手牌最少的玩家中选择一名");
                        minPerson = selectPlayer(minPeople);
                    }
                    if (minPerson == null) {
                        return;
                    }
                    int num = getCards().size() / 2;
                    getPriviMsg().setOneTimeInfo1("好施:请选择" + num + "张卡赠与 " + minPerson.getPlateName());
                    ArrayList<Card> cards = chooseManyFromProvided(num, getCards(),true);// 这里可能为0张

                    if (cards.size() == 0) {// 如果没选，就减去多摸的那两张
                        List<Card> removes = new ArrayList<>();
                        for (int i = 0; i < 2; i++) {
                            removes.add(getCards().get(getCards().size() - 1 - i));
                        }
                        loseCard(removes, false);
                        return;
                    }

                    loseCard(cards, false);
                    minPerson.addCard(cards);

                    String res = Text.format("赠与 %s %s张牌:<i>千金散尽，一笑置之。</i>",
                             minPerson.getHtmlName(), num);
                    getGameManager().getIo().delaySendAndDelete(this, res);
                    //sleep(1000);
                    getGameManager().getIo().printlnPublic(res );
                    //getGameManager().getIo().printlnPublic(res, toString());
                }

            }
        } else {
            super.drawPhase(  fastMode);
        }
    }

    @Override
    public void usePhaseBefore() {

        if (isActiveSkill("缔盟") && hasNotUsedSkill1()) {
            getSkillCards().add("缔盟");
        }
    }

    @Skill("缔盟")
    @Override
    public boolean useSkillInUsePhase(int orderInt) {

        // int orderInt = Integer.valueOf(order) - 1;

        if (orderInt < getSkillCards().size() && getSkillCards().get(orderInt).equals("缔盟") && hasNotUsedSkill1()) {

            List<Person> otherPersons = new ArrayList<>();
            for (Person p : getGameManager().getPlayersBeginFromPlayer(this)) {
                if (p != this) {
                    otherPersons.add(p);
                }
            }
            getPriviMsg()
                    .setOneTimeInfo1(Text.format("\n💬缔盟:选择两名其他角色，你弃置X张牌（X为这两名角色手牌数的差），然后令这两名角色交换手牌。"));

            List<Person> results = chooseManyFromProvided(2, otherPersons,true);
            if (results.size() < 2) {
                return false;
            }
            Person p1 = results.get(0);
            Person p2 = results.get(1);

            if (p1 == null || p2 == null) {
                return false;
            }
            int num = Math.abs(p1.getCards().size()
                    - p2.getCards().size());
            if (num > 0) {
                getPriviMsg().clearHeader2();
                getPriviMsg().setOneTimeInfo1(Text.format("\n💬缔盟,你需弃置%s张牌,然后令这两名角色交换手牌。", num));
                List<Card> cs= chooseManyFromProvided(num, getCards(),true);
                if(cs.size()==0){
                    return false;
                }
                loseCard(cs);
            }

            final ArrayList<Card> c2 = new ArrayList<>(p2.getCards());
            final ArrayList<Card> c1 = new ArrayList<>(p1.getCards());
            p1.getCards().clear();
            p2.getCards().clear();
            p1.addCard(c2);
            p2.addCard(c1);

            setHasUsedSkill1(true);

            String res = Text.format("%s %s :让 %s 与 %s 交换了手牌:<i>以和为贵，以和为贵~</i>",
                    getPlateName(),
                    getSkillHtmlName("缔盟"),
                    p1.getHtmlName(),
                    p2.getHtmlName());
            getGameManager().getIo().delaySendAndDelete(this, res);
            //sleep(1000);
            getGameManager().getIo().printlnPublic(res, toString());
            return true;
        }
        return false;
    }

    @Override
    public String name() {
        return "鲁肃";
    }

    @Override
    public String skillsDescription() {
        return "好施：摸牌阶段，你可以多摸两张牌，然后若你的手牌数大于5，则你将一半的手牌（向下取整）交给手牌最少的一名其他角色。\n" +
                "缔盟：出牌阶段限一次，你可以选择两名其他角色并弃置X张牌（X为这两名角色手牌数的差），然后令这两名角色交换手牌。";
    }
}
