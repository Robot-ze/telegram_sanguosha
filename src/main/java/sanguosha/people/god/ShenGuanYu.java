package sanguosha.people.god;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import config.Text;
import msg.CallbackEven;
import sanguosha.cards.Card;
import sanguosha.cards.Color;
import sanguosha.cards.basic.Sha;
import sanguosha.cards.basic.Tao;
import sanguosha.cards.strategy.TaoYuanJieYi;
import sanguosha.manager.Utils;
import sanguosha.people.Person;
import sanguosha.skills.ForcesSkill;

public class ShenGuanYu extends God {
    private HashMap<Person, Integer> mengYan = new HashMap<>();
    private boolean useHeartSha = false;
    private int thisRound = -1;

    public ShenGuanYu() {
        super(5, null);
        // super(1, null);// test

    }

    @ForcesSkill("武神")

    public Card wuShen() {
        Card c = requestColor(Color.HEART, true);

        return c;
    }

    @Override
    public boolean skillSha(Person sourse) {
        if (launchSkillPriv("武神")) {
            Card c = wuShen();
            if (c != null) {
                String res = Text.format("%s %s:<i>看尔乃插标卖首！</i>",
                        getPlateName(), getSkillHtmlName("武神"));
                getGameManager().getIo().printlnPublic(res, toString());
                // sleep(3000);
            }
            return c != null;
        }
        return false;
    }

    @Override
    public void usePhaseBefore() {

        if (isActiveSkill("武神") && checkSha() && hasRedHandCard()) {
            getSkillCards().add("武神");
        }
    }

    @Override
    public boolean useSkillInUsePhase(int orderInt) {
        // int orderInt = Integer.valueOf(order) - 1;

        if (orderInt < getSkillCards().size() && getSkillCards().get(orderInt).equals("武神")) {
            Card c = wuShen();
            if (c == null) {
                return true;
            }

            Sha sha = new Sha(getGameManager(), c.color(), c.number());

            sha.addReplaceCard(c);

            sha.setMultiSha(1);// 先重置一下
            if (!checkSha(sha, true)) {
                return false;
            }else{
                addCard(getGameManager().getCardsHeap().retrieve(c)); 
            }
            setShaMulti(sha);

            if (sha.askTarget(this)) {

                String res = Text.format("%s %s:<i>看尔乃插标卖首！</i>",
                        getPlateName(), getSkillHtmlName("武神"));
                // sleep(3000);
                getGameManager().getIo().printlnPublic(res, toString());

                useCard(sha);
                // ---------------------注意这里
                if (useHeartSha) {
                    useHeartSha = false;
                }
            } else {
                addCard(getGameManager().getCardsHeap().retrieve(c));
            }

            return true;
        }
        return false;
    }

    @Override
    public boolean checkSha(Card card, boolean print) {
        // System.out.println("checkSha=" + card);

        if (super.checkSha(card, print)) {
            useHeartSha = card.color() == Color.HEART;
            return true;
        }
        return false;
    }

    @Override
    public int getShaDistance() {
        if (useHeartSha) {
            return 10000;
        }
        return super.getShaDistance();
    }

    @Override
    public void gotHurt(List<Card> cards, Person p, int num) {
        // System.out.println(p + " gets " + num + " 梦魇 marks");
        if (p == null || p == this) {// 自己就不插了
            return;
        }
        boolean showImg = false;
        if (getGameManager().getPersonRound() > thisRound) {
            thisRound = getGameManager().getPersonRound();
            showImg = true;
        }
        mengYan.putIfAbsent(p, 0);
        mengYan.put(p, mengYan.get(p) + num);
        String res = Text.format("%s 被 %s 标记 × %s",
                p.getHtmlName(), getSkillHtmlName("梦魇"), mengYan.get(p));
        // sleep(1000);
        getGameManager().getIo().printlnPublic(res, showImg ? toString() : null);
        // printlnPriv("now " + p + " has " + mengYan.get(p) + " 梦魇 marks");
    }

    @ForcesSkill("梦魇")
    @Override
    public void die() {
        System.out.println("梦魇  死亡 触发!" + mengYan);

        super.die();
        if (getGameManager().getPlayers().size() <= 2) {// 最后的主公不能带走
            return;
        }
        int maxMarks = 0;
        ArrayList<Person> maxPerson = new ArrayList<>();
        for (Person p : mengYan.keySet()) {
            if (mengYan.get(p) == maxMarks) {
                maxPerson.add(p);
            } else if (mengYan.get(p) > maxMarks) {
                maxMarks = mengYan.get(p);
                maxPerson.clear();
                maxPerson.add(p);
            }
        }
        System.out.println("maxPerson=" + maxPerson);

        Person luckyGuy;
        if (maxPerson.size() > 0) {
            luckyGuy = maxPerson.get(Utils.randint(0, maxPerson.size() - 1));
        } else {
            return;
        }

        Card c = getGameManager().getCardsHeap().judge(luckyGuy, new CallbackEven() {
            @Override
            public boolean juge(Card card) {
                if (!(card instanceof Tao || card instanceof TaoYuanJieYi)) {
                    return true;
                } else {
                    return false;
                }
            }
        });
        if (!(c instanceof Tao || c instanceof TaoYuanJieYi)) {
            luckyGuy.die();

            String res = Text.format("%s 带走了 %s:<i>死当追汝之魂！</i>",

                    getSkillHtmlName("梦魇"), luckyGuy.getHtmlName());
            // sleep(1000);
            getGameManager().getIo().printlnPublic(res, toString());
        } else {
            String res = Text.format("%s 饶恕了 %s:<i>吾去矣</i>",

                    getSkillHtmlName("梦魇"), luckyGuy.getHtmlName());
            // sleep(1000);
            getGameManager().getIo().printlnPublic(res, toString());
        }
    }

    @Override
    public String getExtraInfo() {
        String ans = "梦魇: ";
        for (Person p : mengYan.keySet()) {
            ans += p + "×" + mengYan.get(p).toString() + " ";
        }
        return ans;
    }

    @Override
    public String name() {
        return "神关羽";
    }

    @Override
    public String skillsDescription() {
        return "武神：锁定技，你的红桃手牌视为[杀]；你使用红桃[杀]无距离限制。\n" +
                "武魂：锁定技，当你受到1点伤害后，你令伤害来源获得1枚“梦魇”标记；" +
                "当你死亡时，你令拥有最多“梦魇”标记的一名其他角色判定，若结果不为[桃]或[桃园结义]，则该角色死亡。";
    }
}
