package sanguosha.people.god;

import java.util.ArrayList;

import config.Config;
import config.Text;
import sanguosha.cards.Card;
import sanguosha.people.Identity;
import sanguosha.people.Person;
import sanguosha.skills.Skill;
import components.TimeLimit;

public class ShenZhuGeLiang extends God {
    private final ArrayList<Card> stars = new ArrayList<>();

    public ShenZhuGeLiang() {
        super(3, null);
        // super(1, null);//test
    }

    @Skill("七星")
    public void qiXing() {
        ArrayList<Card> handCards;
        // printlnToIOPriv("current stars: ");
        // printCards(stars);
        TimeLimit t = new TimeLimit(Config.PRIV_RND_TIME_60S);
        do {
            if (t.isTimeout()) {
                return;
            }
            // printlnToIOPriv("choose cards to exchange with stars");
            String cardString = "你的星牌如下:\n";
            for (Card c : stars) {
                cardString += c.getHtmlNameWithColor() + "\n";
            }
            String res = cardString + Text.format("💬你可以选择最多%s张手牌和星牌调换", stars.size());
            getPriviMsg().setOneTimeInfo1(res);
            handCards = chooseManyCards(0, getCards());
        } while (getGameManager().isRunning() && handCards.size() > stars.size());
        int num = handCards.size();
        if (num != 0) {
            String res = Text.format("💬请选择%s张星牌来调换", num);
            getPriviMsg().setOneTimeInfo1(res);
            ArrayList<Card> starCards = chooseManyFromProvided(num, stars, true, "🌟", "");
            if (starCards.size() < num) {// 如果不够就从头开始丢
                for (Card sc : stars) {
                    if (starCards.indexOf(sc) < 0) {
                        starCards.add(sc);
                        if (starCards.size() >= num) {
                            break;
                        }
                    }

                }
            }

            getCards().removeAll(handCards);
            stars.removeAll(starCards);
            getCards().addAll(starCards);
            stars.addAll(handCards);
        }
    }

    @Skill("狂风")
    public void kuangFeng() {
        // printlnToIOPriv("choose players");
        String res = "💬狂风:选择1个释放对象";
        getPriviMsg().setOneTimeInfo1(res);
        Person p = selectPlayer(true);
        if (p == null) {
            return;
        }
        res = Text.format("💬狂风:请出1张“星”牌");
        getPriviMsg().setOneTimeInfo1(res);
        Card c = chooseCard(stars, true, "🌟", "");
        if (c == null) {
            return;
        }
        stars.remove(c);
        getGameManager().getCardsHeap().discard(c);
        p.setKuangFeng(true);
        res = Text.format("%s 在%s中:<i>万事俱备，只欠业火</i>", p.getHtmlName(), getSkillHtmlName("狂风"));
        getGameManager().getIo().printlnPublic(res, toString());

    }

    @Skill("大雾")
    public void daWu() {
        ArrayList<Person> people;
        int num;
        TimeLimit t = new TimeLimit(Config.PRIV_RND_TIME_60S);
        do {

            if (t.isTimeout()) {
                return;
            }
            // printlnToIOPriv("choose players");
            String res = "💬大雾:可选择多个释放对象";
            getPriviMsg().setOneTimeInfo1(res);
            people = chooseManyFromProvided(0, getGameManager().getPlayersBeginFromPlayer(this));
            num = people.size();
        } while (getGameManager().isRunning() && num > stars.size());
        if (num == 0) {
            return;
        }
        String res = Text.format("💬大雾:请出%s张“星”牌", num);
        getPriviMsg().setOneTimeInfo1(res);
        ArrayList<Card> starCards = chooseManyFromProvided(num, stars, true, "🌟", "");

        if (starCards.size() < num) {// 如果不够就从头开始丢
            for (Card sc : stars) {
                if (starCards.indexOf(sc) < 0) {
                    starCards.add(sc);
                    if (starCards.size() >= num) {
                        break;
                    }
                }

            }
        }

        stars.removeAll(starCards);
        getGameManager().getCardsHeap().discard(starCards);
        res = "";
        for (Person p : people) {
            p.setDaWu(true);
            res += p.getHtmlName() + ",";
        }
        res += Text.format("在%s中隐去:<i>此非万全之策，惟惧天雷。</i>", getSkillHtmlName("大雾"));
        getGameManager().getIo().printlnPublic(res, toString());
    }

    @Override
    public void initialize(Identity identity, int uerPos) {
        super.initialize(identity, uerPos);
        // printlnPriv(this + " uses 七星");
        stars.addAll(getGameManager().getCardsHeap().draw(7));
        qiXing();
    }

    @Override
    public void selfBeginPhase() {
        for (Person p : getGameManager().getPlayersBeginFromPlayer(this)) {
            p.setKuangFeng(false);
            p.setDaWu(false);
        }
    }

    @Override
    public void drawPhase(boolean fastMode) {
        super.drawPhase(fastMode);
        getPriviMsg().setOneTimeInfo1("💬你可以用任意张手牌替换等量的“星”");
        if (!fastMode && launchSkillPriv("七星")) {
            qiXing();
        }
    }

    @Override
    public void selfEndPhase(boolean fastMode) {
        if (fastMode) {
            return;
        }
        getPriviMsg().setOneTimeInfo1("💬狂风:你可以暂时使1名角色受到火焰🔥伤害+1");

        if (launchSkillPriv("狂风") && stars.size() > 0) {
            kuangFeng();
        }
        getPriviMsg().setOneTimeInfo1("💬大雾:你可以暂时使任意名角色避免普通🗡和火焰🔥伤害");

        if (launchSkillPriv("大雾") && stars.size() > 0) {
            daWu();
        }

    }

    @Override
    public void die() {
        super.die();
        for (Person p : getGameManager().getPlayersBeginFromPlayer(this)) {
            p.setKuangFeng(false);
            p.setDaWu(false);
        }
    }

    @Override
    public ArrayList<Card> getExtraCards() {
        return stars;
    }

    @Override
    public String getExtraInfo() {
        return "星×" + stars.size();
    }

    @Override
    public String name() {
        return "神诸葛亮";
    }

    @Override
    public String skillsDescription() {
        return "七星：游戏开始时，你将牌堆顶的七张牌扣置于你的武将牌上，称为“星”，然后你可以用任意张手牌替换等量的“星”；" +
                "摸牌阶段结束时，你可以用任意张手牌替换等量的“星”。\n" +
                "狂风：结束阶段，你可以移去一张“星”并选择一名角色，然后直到你的下回合开始之前，当该角色受到火焰伤害时，此伤害+1。\n" +
                "大雾：结束阶段，你可以移去任意张“星”并选择等量的角色，然后直到你的下回合开始之前，当这些角色受到非雷电伤害时，防止此伤害。";
    }
}
