package sanguosha.people.fire;

import java.util.concurrent.atomic.AtomicReference;

import config.Text;
import msg.CallbackEven;
import msg.MsgObj;
import sanguosha.cards.Card;
import sanguosha.cards.EquipType;
import sanguosha.cards.strategy.HuoGong;
import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.people.PlayerIO;
import sanguosha.skills.ForcesSkill;
import sanguosha.skills.Skill;

public class WoLong extends Person {
    private boolean validBaZhen = true;

    public WoLong() {
        super(3, Nation.SHU);
    }

    @Override
    public boolean hasBaZhen() {
        // 这个是用来记录 那把无视防御的剑
        // 这个不要改动
        return validBaZhen;
    }

    @ForcesSkill("八阵")
    @Override
    public boolean skillShan(Person sourse) {
        if (isActiveSkill("八阵") &&
                getEquipments().get(EquipType.weapon) == null
                && validBaZhen) {
            // printlnPriv(this + " uses 八阵");
            Card c = getGameManager().getCardsHeap().judge(this, new CallbackEven() {
                @Override
                public boolean juge(Card card) {
                    if (card.isRed()) {
                        return true;
                    } else {
                        return false;
                    }
                }
            });
            boolean isRed = c.isRed();
            if (isRed) {
                getGameManager().getIo().printlnPublic(
                        Text.format("%s %s,视为出闪:<i>太极生两仪，两仪生四象，四象生八卦。</i>",
                               getSkillHtmlName("八阵"),
                                c.getHtmlNameWithColor()),
                        toString());
            } else {
                getGameManager().getIo().printlnPublic(
                        Text.format("%s %s,技能失效<i>始于无，终于无</i>",
                        getSkillHtmlName("八阵"),
                                c.getHtmlNameWithColor()),
                        toString());
            }

            return isRed;

        }
        return false;
    }

    @Override
    public void setBaZhen(boolean bool) {
        validBaZhen = bool;
    }

    @Override
    public void usePhaseBefore() {

        if (isActiveSkill("火计") && hasRedHandCard()) {
            getSkillCards().add("火计");
        }
    }

    @Skill("火计")
    @Override
    public boolean useSkillInUsePhase(int orderInt) {

        // int orderInt = Integer.valueOf(order) - 1;

        if (orderInt < getSkillCards().size() && getSkillCards().get(orderInt).equals("火计")) {
            // printlnPriv(this + " uses 火计");
            Card c = requestRedBlack("red");
            HuoGong hg = new HuoGong(getGameManager(), c.color(), c.number());
            hg.addReplaceCard(c);
            if (hg.askTarget(this)) {
                String res = Text.format("%s %s 将 %s 当作火攻:<i>此火可助我军大获全胜！</i>",
                        getPlateName(), getSkillHtmlName("火计"), c.getHtmlNameWithColor());
                //sleep(1000);
                getGameManager().getIo().printlnPublic(res, toString());

                useCard(hg);

            } else {
                addCard(getGameManager().getCardsHeap().retrieve(c), false);
            }
        }
        return false;
    }

    @Skill("看破")
    @Override
    public Card skillWuxie(AtomicReference<PlayerIO> throwedPerson, MsgObj privMsgObj) {
        // MsgObj privMsgObj =MsgObj.newMsgObj(getGameManager());
        if (launchSkillPriv(privMsgObj, "看破")) {
            Card c = requestRedBlack("black");
            if (c != null) {
                if (throwedPerson != null && throwedPerson.compareAndSet(null, this) == false) {
                    // 如果别人出过牌了,就拿回这张牌
                    getGameManager().getCardsHeap().retrieve(c);
                    this.addCard(c);
                    return null;
                }
                String res = Text.format("%s %s 将 %s 当作无懈可击:<i>尔等宵小，岂能诈我？</i>",
                        getPlateName(), getSkillHtmlName("看破"), c.getHtmlNameWithColor());
                //sleep(1000);
                getGameManager().getIo().printlnPublic(res, toString());
            }
            return c ;
        }
        return null;
    }

    @Override
    public boolean hasWuXieReplace() {
        if (!isActiveSkill("看破")) {
            return false;
        }
        for(Card c :getCards()){
            if(c.isBlack()){
                return true;
            }
        }
        return false;
    }

    @Override
    public String name() {
        return "卧龙-诸葛亮";
    }

    @Override
    public String skillsDescription() {
        return "八阵：锁定技，若你的装备区里没有防具牌，你视为装备着[八卦阵]。\n" +
                "火计：你可以将一张红色手牌当[火攻]使用。\n" +
                "看破：你可以将一张黑色手牌当[无懈可击]使用。";
    }
}
