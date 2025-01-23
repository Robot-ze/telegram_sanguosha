package sanguosha.people.god;

import sanguosha.cards.Card;
import sanguosha.cards.Color;
import sanguosha.cards.basic.HurtType;
import sanguosha.cards.basic.Sha;
import sanguosha.cards.basic.Shan;
import sanguosha.cards.basic.Tao;
import sanguosha.cards.strategy.WuXieKeJi;
import sanguosha.cardsheap.CardsHeap;
import sanguosha.manager.GameManager;
import sanguosha.people.Person;
import sanguosha.people.PlayerIO;
import sanguosha.skills.ForcesSkill;
import sanguosha.skills.Skill;
import components.TimeLimit;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import config.Config;
import config.Text;
import msg.MsgObj;

public class ShenZhaoYun extends God {
    public ShenZhaoYun() {
        super(2, null);
         //super(0, null);// test 主公会加1血
    }

    @ForcesSkill("绝境")
    @Override
    public void drawPhase(boolean fastMode) {
        // printlnPriv(this + " uses 绝境");
        drawCards(2 + getMaxHP() - getHP());
    }

    @Override
    public int throwPhase(boolean fastMode) {
        // printlnPriv(this + " uses 绝境");
        int num = getCards().size() - getHP() - 2;
        if (num > 0) {
            // printlnPriv(Text.format("You need to throw %d cards", num));
            ArrayList<Card> cs;
            if (fastMode) {// 快速模式直接丢掉后面的牌
                cs = new ArrayList<>();//
                for (int i = 0; i < num; i++) {
                    cs.add(getCards().get(getCards().size() - 1 - i));
                }
            } else {
                getPriviMsg().setOneTimeInfo1(Text.format("💬你需要舍弃 %s 张牌\n", num));
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
    }

    @Skill("龙魂")
    @Override
    public boolean useSkillInUsePhase(int orderInt) {
        if (longHunSha(orderInt)) {
            return true;
        }
        if (longHunTao(orderInt)) {
            return true;
        }
        return false;
    }

    @Override
    public void usePhaseBefore() {

        if (isActiveSkill("龙魂")) {

            int requireNum = Math.max(getHP(), 1);
            int count = 0;
            for (Card c : getCardsAndEquipments()) {
                if (c.color() == Color.DIAMOND) {
                    count++;
                }
            }
            if (count >= requireNum && checkSha()) {
                getSkillCards().add("龙魂[火杀]");
            }

            count = 0;
            for (Card c : getCardsAndEquipments()) {
                if (c.color() == Color.HEART) {
                    count++;
                }
            }
            if (getHP() < getMaxHP() && count >= requireNum) {
                getSkillCards().add("龙魂[桃]");
            }

        }
    }

    /**
     * 杀技能
     * 
     * @param orderInt
     * @return
     */
    private boolean longHunSha(int orderInt) {

        ArrayList<Card> vals = new ArrayList<>();
        int requireNum = Math.max(getHP(), 1);

        for (Card c : getCardsAndEquipments()) {
            if (c.color() == Color.DIAMOND) {
                vals.add(c);
            }
        }

        if (orderInt < getSkillCards().size() &&
                getSkillCards().get(orderInt).equals("龙魂[火杀]") &&
                vals.size() >= requireNum && checkSha()) {
            // printlnPriv(this + " uses 龙魂");

            if (requireNum == 1) {
                String res = Text.format("龙魂:你可以使用%s个%s当[火杀]",
                        requireNum, Card.getColorEmoji(Color.DIAMOND));
                getPriviMsg().setOneTimeInfo1(res);
                Card c = requestColor(Color.DIAMOND);
                if (c != null) {
                    Sha sha = new Sha(getGameManager(), Color.DIAMOND, 0, HurtType.fire);

                    sha.addReplaceCard(c);
                    if (sha.askTarget(this)) {
                        useCard(sha);
                        return true;
                    }
                }
                return false;

            }

            TimeLimit t = new TimeLimit(Config.PRIV_RND_TIME_60S);
            List<Card> cs = new ArrayList<>();
            while (getGameManager().isRunning() && t.isNotTimeout()) {
                cs.clear();
                String res = Text.format("龙魂:你可以使用%s个%s当[火杀]",
                        requireNum, Card.getColorEmoji(Color.DIAMOND));
                getPriviMsg().setOneTimeInfo1(res);

                cs.addAll(chooseManyFromProvided(requireNum, vals, true));
                if (cs.size() == 0) {
                    return false;
                }
                if (cs.size() != requireNum) {
                    getGameManager().getIo().delaySendAndDelete(this, "牌数错误");
                    continue;
                }
                break;
            }

            Sha sha = new Sha(getGameManager(), Color.DIAMOND, 0, HurtType.fire);

            sha.setReplaceCards(cs);
            if (sha.askTarget(this)) {
                loseCard(cs);
                useCard(sha);
                return true;
            }
            return false;
        }
        return false;
    }

    /**
     * 桃技能
     * 
     * @param orderInt
     * @return
     */
    private boolean longHunTao(int orderInt) {

        ArrayList<Card> vals = new ArrayList<>();
        int requireNum = Math.max(getHP(), 1);

        for (Card c : getCardsAndEquipments()) {
            if (c.color() == Color.HEART) {
                vals.add(c);
            }
        }

        if (orderInt < getSkillCards().size() &&
                getSkillCards().get(orderInt).equals("龙魂[桃]") &&
                vals.size() >= requireNum) {
            // printlnPriv(this + " uses 龙魂");
            if (requireNum == 1) {
                String res = Text.format("龙魂:你可以使用%s个%s当[桃]",
                        requireNum, Card.getColorEmoji(Color.HEART));
                getPriviMsg().setOneTimeInfo1(res);
                Card c = requestColor(Color.HEART);
                if (c != null) {
                    Card tao = new Tao(getGameManager(), Color.HEART, 0);
                    tao.addReplaceCard(c);
                    if (useCard(tao)) {
                        return true;
                    }
                }
                return false;
            }
            TimeLimit t = new TimeLimit(Config.PRIV_RND_TIME_60S);
            List<Card> cs = new ArrayList<>();
            while (getGameManager().isRunning() && t.isNotTimeout()) {
                cs.clear();
                String res = Text.format("龙魂:你可以使用%s个%s当[桃]",
                        requireNum, Card.getColorEmoji(Color.HEART));
                getPriviMsg().setOneTimeInfo1(res);

                cs.addAll(chooseManyFromProvided(requireNum, vals, true));
                if (cs.size() == 0) {
                    return false;
                }
                if (cs.size() != requireNum) {
                    getGameManager().getIo().delaySendAndDelete(this, "牌数错误");
                    continue;
                }
                break;
            }

            Card tao = new Tao(getGameManager(), Color.HEART, 0);
            tao.setReplaceCards(cs);
            if (useCard(tao)) {
                loseCard(cs);
                return true;
            }

        }
        return false;

    }

    // Color.DIAMOND
    @Override
    public boolean skillSha(Person sourse) {
        int requireNum = Math.max(getHP(), 1);

        ArrayList<Card> vals = new ArrayList<>();

        for (Card c : getCardsAndEquipments()) {
            if (c.color() == Color.DIAMOND) {
                vals.add(c);
            }
        }

        if (vals.size() >= requireNum) {
            String res = Text.format("龙魂:你可以使用%s个%s当[火杀]",
                    requireNum, Card.getColorEmoji(Color.DIAMOND));
            getPriviMsg().setOneTimeInfo1(res);

            if (launchSkillPriv("龙魂[火杀]")) {

                if (requireNum == 1) {
                    res = Text.format("龙魂:你可以使用%s个%s当[火杀]",
                            requireNum, Card.getColorEmoji(Color.DIAMOND));
                    getPriviMsg().setOneTimeInfo1(res);
                    Card c = requestColor(Color.DIAMOND);
                    return c != null;
                }

                TimeLimit t = new TimeLimit(Config.PRIV_RND_TIME_60S);
                List<Card> cs = new ArrayList<>();
                while (getGameManager().isRunning() && t.isNotTimeout()) {
                    cs.clear();

                    getPriviMsg().setOneTimeInfo1(res);

                    cs.addAll(chooseManyFromProvided(requireNum, vals, true));
                    if (cs.size() == 0) {
                        return false;
                    }
                    if (cs.size() != requireNum) {
                        getGameManager().getIo().delaySendAndDelete(this, "牌数错误");
                        continue;
                    }
                    loseCard(cs);
                    return true;
                }
            }

        }
        return false;
    }

    // Color.CLUB
    @Override
    public boolean skillShan(Person sourse) {
        int requireNum = Math.max(getHP(), 1);

        ArrayList<Card> vals = new ArrayList<>();

        for (Card c : getCardsAndEquipments()) {
            if (c.color() == Color.CLUB) {
                vals.add(c);
            }
        }

        if (vals.size() >= requireNum) {

            String res = Text.format("龙魂:你可以使用%s个%s当[闪]",
                    requireNum, Card.getColorEmoji(Color.CLUB));
            getPriviMsg().setOneTimeInfo1(res);
            if (launchSkillPriv("龙魂[闪]")) {

                if (requireNum == 1) {
                    res = Text.format("龙魂:你可以使用%s个%s当[闪]",
                            requireNum, Card.getColorEmoji(Color.CLUB));
                    getPriviMsg().setOneTimeInfo1(res);
                    Card c = requestColor(Color.CLUB);
                    return c != null;
                }

                TimeLimit t = new TimeLimit(Config.PRIV_RND_TIME_60S);
                List<Card> cs = new ArrayList<>();
                while (getGameManager().isRunning() && t.isNotTimeout()) {
                    cs.clear();

                    getPriviMsg().setOneTimeInfo1(res);

                    cs.addAll(chooseManyFromProvided(requireNum, vals, true));
                    if (cs.size() == 0) {
                        return false;
                    }
                    if (cs.size() != requireNum) {
                        getGameManager().getIo().delaySendAndDelete(this, "牌数错误");
                        continue;
                    }
                    loseCard(cs);
                    return true;
                }
            }
        }
        return false;
    }

    // Color.SPADE
    @Override
    public Card skillWuxie(AtomicReference<PlayerIO> throwedPerson, MsgObj privMsgObj) {
        int requireNum = Math.max(getHP(), 1);

        ArrayList<Card> vals = new ArrayList<>();

        for (Card c : getCardsAndEquipments()) {
            if (c.color() == Color.SPADE) {
                // if (true) {// test
                vals.add(c);
            }
        }

        if (vals.size() >= requireNum) {

            String res = Text.format("龙魂:你可以使用%s个%s当[无懈可击]",
                    requireNum, Card.getColorEmoji(Color.SPADE));
            getPriviMsg().setOneTimeInfo1(res);

            if (launchSkillPriv("龙魂[无懈可击]")) {

                if (requireNum == 1) {
                    res = Text.format("龙魂:你可以使用%s个%s当[无懈可击]",
                            requireNum, Card.getColorEmoji(Color.SPADE));
                    getPriviMsg().setOneTimeInfo1(res);
                    Card c = requestColor(Color.SPADE);
                    if (c != null) {
                        Card wuxie = new WuXieKeJi(getGameManager(), Color.SPADE, 0);
                        wuxie.addReplaceCard(c);
                        if (throwedPerson.compareAndSet(null, this)) {
                            return wuxie;
                        }
                    }
                    return null;
                }

                TimeLimit t = new TimeLimit(Config.PRIV_RND_TIME_60S);
                List<Card> cs = new ArrayList<>();
                while (getGameManager().isRunning() && t.isNotTimeout()) {
                    cs.clear();

                    getPriviMsg().setOneTimeInfo1(res);

                    cs.addAll(chooseManyFromProvided(requireNum, vals, true));
                    if (cs.size() == 0) {
                        return null;
                    }
                    if (cs.size() != requireNum) {
                        getGameManager().getIo().delaySendAndDelete(this, "牌数错误");
                        continue;
                    }
                    break;
                }

                Card wuxie = new WuXieKeJi(getGameManager(), Color.SPADE, 0);

                wuxie.setReplaceCards(cs);
                if (throwedPerson.compareAndSet(null, this)) {
                    loseCard(cs);
                    return wuxie;
                }
                return null;
            }
        }
        return null;
    }

    // Color.HEART
    @Override
    public Card requestTao(String type, AtomicReference<PlayerIO> throwedPerson, MsgObj inMsg) {
        int requireNum = Math.max(getHP(), 1);

        ArrayList<Card> vals = new ArrayList<>();

        for (Card c : getCardsAndEquipments()) {
            if (c.color() == Color.HEART) {
                vals.add(c);
            }
        }
        if (vals.size() >= requireNum) {

            String res = Text.format("龙魂:你可以使用%s个%s当[桃]",
                    requireNum, Card.getColorEmoji(Color.HEART));
            getPriviMsg().setOneTimeInfo1(res);

            if (launchSkillPriv("龙魂[桃]")) {
                if (requireNum == 1) {
                    res = Text.format("龙魂:你可以使用%s个%s当[桃]",
                            requireNum, Card.getColorEmoji(Color.HEART));
                    getPriviMsg().setOneTimeInfo1(res);
                    Card c = requestColor(Color.HEART);
                    if (c != null) {
                        Card tao = new Tao(getGameManager(), Color.HEART, 0);
                        tao.addReplaceCard(c);
                        if (throwedPerson.compareAndSet(null, this)) {
                            return tao;
                        }
                    }
                    return null;
                }

                TimeLimit t = new TimeLimit(Config.PRIV_RND_TIME_60S);
                List<Card> cs = new ArrayList<>();
                while (getGameManager().isRunning() && t.isNotTimeout()) {
                    cs.clear();

                    getPriviMsg().setOneTimeInfo1(res);

                    cs.addAll(chooseManyFromProvided(requireNum, vals, true));
                    if (cs.size() == 0) {
                        return super.requestTao(type, throwedPerson, inMsg);
                    }
                    if (cs.size() != requireNum) {
                        getGameManager().getIo().delaySendAndDelete(this, "牌数错误");
                        continue;
                    }
                    break;
                }

                Card tao = new Tao(getGameManager(), Color.HEART, 0);

                tao.setReplaceCards(cs);
                if (throwedPerson.compareAndSet(null, this)) {
                    loseCard(cs);
                    return tao;
                }
                return null;
            }
        }
        return super.requestTao(type, throwedPerson, inMsg);
    }

    @Override
    public boolean checkShan() {
        if (isActiveSkill("龙魂")) {
            int requireNum = Math.max(getHP(), 1);
            int count = 0;
            for (Card c : getCardsAndEquipments()) {
                if (c.color() == Color.CLUB) {
                    count++;
                }
            }
            if (count >= requireNum) {
                return true;
            }
        }
        return super.checkShan();
    }

    /**
     * 是否能打出杀
     * 
     * @return
     */
    @Override
    public boolean existsSha() {
        if (isActiveSkill("龙魂")) {
            int requireNum = Math.max(getHP(), 1);
            int count = 0;
            for (Card c : getCardsAndEquipments()) {
                if (c.color() == Color.DIAMOND) {
                    count++;
                }
            }
            if (count >= requireNum) {
                return true;
            }
        }
        return super.existsSha();
    }

    /**
     * 检查是否能打出桃
     * 
     * @return
     */
    @Override
    public boolean checkTao() {
        if (isActiveSkill("龙魂")) {
            int requireNum = Math.max(getHP(), 1);
            int count = 0;
            for (Card c : getCardsAndEquipments()) {
                if (c.color() == Color.HEART) {
                    count++;
                }
            }
            if (count >= requireNum) {
                return true;
            }
        }
        return super.checkTao();
    }

    @Override
    public boolean hasWuXieReplace() {
        if (!isActiveSkill("龙魂")) {
            return false;
        }
        int requireNum = Math.max(getHP(), 1);
        int count = 0;
        for (Card c : getCardsAndEquipments()) {
            if (c.color() == Color.SPADE) {
                count++;
            }
        }
        return count >= requireNum;
        // return true;// test
    }

    @Override
    public String name() {
        return "神赵云";
    }

    @Override
    public String skillsDescription() {
        return "绝境：锁定技，摸牌阶段，你多摸等同于你已损失的体力值数的牌；你的手牌上限+2。\n" +
                "龙魂：你可以将花色相同的X张牌按下列规则使用或打出：红桃当[桃]；方块当火[杀]；梅花当[闪]；黑桃当[无懈可击]（X为你的体力值且最少为1）。";
    }
}
