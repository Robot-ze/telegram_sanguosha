package sanguosha.people.qun;

import java.util.concurrent.atomic.AtomicReference;

import config.Text;
import msg.MsgObj;
import sanguosha.cards.Card;
import sanguosha.cards.basic.Tao;
import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.people.PlayerIO;
import sanguosha.skills.Skill;

public class HuaTuo extends Person {

    public HuaTuo() {
        super(3, Nation.QUN);
        // super(1, Nation.QUN);
    }

    @Skill("急救")
    @Override
    public Card requestTao(String type, AtomicReference<PlayerIO> throwedPerson, MsgObj inMsg) {
        getPriviMsg().clearHeader2();
        getPriviMsg().setOneTimeInfo1("\n💬是否用急救");
        // if (!isMyRound() && launchSkillPriv("急救")) {
        if (launchSkillPriv("急救")) {
            getPriviMsg().setOneTimeInfo1("\n💬请出红色牌当作一个桃");
            Card c = requestRedBlack("red", true);
            if (c != null) {
                // 如果有人已经出牌，则放弃
                if (throwedPerson != null) {
                    if (throwedPerson.compareAndSet(null, this) == false) {
                        getGameManager().getCardsHeap().retrieve(c);
                        addCard(c);
                        return null;
                    }
                }
                String res = Text.format("<i>救人一命，胜造七级浮屠~</i>");
                // sleep(1000);
                getGameManager().getIo().printlnPublic(res, toString());
                // sleep(3000);
                return c;
            }
        }
        return super.requestTao(type, throwedPerson, inMsg);
    }

    @Override
    public void usePhaseBefore() {

        if (isActiveSkill("青囊") && hasNotUsedSkill1()) {
            getSkillCards().add("青囊");
        }
    }

    @Skill("青囊")
    @Override
    public boolean useSkillInUsePhase(int orderInt) {
        // int orderInt = Integer.valueOf(order) - 1;

        if (orderInt < getSkillCards().size() && getSkillCards().get(orderInt).equals("青囊") && hasNotUsedSkill1()) {

            getPriviMsg().setOneTimeInfo1("\n💬是否用青囊，出牌阶段限一次，你可以弃置一张手牌令一名角色回复1点体力。");

            Person p = selectPlayer(true);
            if (p == null) {
                return false;
            }
            // if (p.getHP() == p.getMaxHP()) {
            // printlnToIOPriv("you can't choose person with maxHP");
            // return true;
            // }
            getPriviMsg().setOneTimeInfo1("\n💬青囊，请选择一张牌");
            Card c = requestCard(null);
            if (c == null) {
                return true;
            }
            p.recover(null, 1);
            setHasUsedSkill1(true);

            String res = Text.format("%s 恢复1点体力%s:<i>越老越要补啊！</i>",

                    p.getHtmlName(),
                    p.getHPEmoji());

            getGameManager().getIo().printlnPublic(res, toString());
            // sleep(3000);
            return true;
        }
        return false;
    }

    /**
     * 检查是否能打出桃
     * 
     * @return
     */
    @Override
    public boolean checkTao() {
        if (isActiveSkill("急救")) {
            for (Card c : getCards()) {
                if (c.isRed()) {
                    return true;
                }
            }

        }

        return super.checkTao();
    }

    @Override
    public String name() {
        return "华佗";
    }

    @Override
    public String skillsDescription() {
        return "急救：你的回合外，你可以将一张红色牌当[桃]使用。\n" +
                "青囊：出牌阶段限一次，你可以弃置一张手牌令一名角色回复1点体力。";
    }
}
