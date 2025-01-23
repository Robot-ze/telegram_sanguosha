package sanguosha.people.wu;

import sanguosha.cards.Card;
import sanguosha.people.Identity;
import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.Skill;
import java.util.List;
import java.util.Set;

import config.Text;

import java.util.ArrayList;

import msg.MsgObj;

public class SunShangXiang extends Person {
    public SunShangXiang() {
        super(3, "female", Nation.WU);
    }

    @Skill("枭姬")
    @Override
    public void lostEquipment() {
        String code = "xiaoji";
        if (launchSkillPublic(
                "枭姬",
                Text.format("%s 枭姬：当你失去装备区里的一张牌后，你可以摸两张牌。", getHtmlName()),
                code)) {
            drawCards(2);

            String res = Text.format("%s %s:<i>弓马何须忌红装！</i>", getPlateName(), getSkillHtmlName("枭姬"));

            MsgObj msg = getTempActionMsgObj(code);
            msg.appendText(res);
            getGameManager().getMsgAPI().editCaptionForce(msg);
            // sleep(3000);
        }
    }

    @Override
    public void usePhaseBefore() {

        if (isActiveSkill("结姻") && getCards().size() >= 2 && hasNotUsedSkill1()) {
            getSkillCards().add("结姻");
        }
    }

    @Skill("结姻")
    @Override
    public boolean useSkillInUsePhase(int orderInt) {
        // int orderInt = Integer.valueOf(order) - 1;

        // System.out.println(" getSkillCards()=" + getSkillCards());
        if (getCards().size() >= 2 &&
                orderInt < getSkillCards().size() &&
                getSkillCards().get(orderInt).equals("结姻") && hasNotUsedSkill1()) {
            // printlnPriv(this + " uses 结姻");

            List<Person> men = new ArrayList<>();

            for (Person p : getGameManager().getPlayersBeginFromPlayer(this)) {
                if (p.getSex().equals("female")) {
                    continue;
                }
                // if (p.getHP() == p.getMaxHP()) {
                // continue;
                // }
                men.add(p);
            }
            if (men.size() == 0) {
                return false;
            }
            String res = Text.format("结姻：出牌阶段限一次，你可以弃置两张手牌，令你和一名已受伤的男性角色各回复1点体力。请选择一个角色");
            getPriviMsg().setOneTimeInfo1(res);
            Person p = selectPlayer(men);
            if (p == null) {
                return false;
            }

            ArrayList<Card> cs = chooseManyCards(0, getCards());
            if (cs.size() != 2) {
                return false;
            }
            loseCard(cs);
            this.recover(null, 1);
            p.recover(null, 1);
            setHasUsedSkill1(true);

            res = Text.format("%s 对 %s 使用 %s:<i>吾愿携弩征战沙场，助君一战。</i>",
                    getPlateName(),
                    p.getHtmlName(),
                    getSkillHtmlName("结姻"));

            getGameManager().getIo().printlnPublic(res, toString());
            // sleep(3000);
            return true;
        }
        return false;
    }

    @Override
    public String name() {
        return "孙尚香";
    }

    @Override
    public String skillsDescription() {
        return "结姻：出牌阶段限一次，你可以弃置两张手牌，令你和一名已受伤的男性角色各回复1点体力。\n" +
                "枭姬：当你失去装备区里的一张牌后，你可以摸两张牌。";
    }
}
