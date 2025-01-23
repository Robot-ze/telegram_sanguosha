package sanguosha.people.fire;

import java.util.ArrayList;

import msg.MsgObj;
import sanguosha.cards.Card;
import sanguosha.cards.Color;
import sanguosha.cards.strategy.TieSuoLianHuan;
import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.RestrictedSkill;
import sanguosha.skills.Skill;

import config.Text;

public class PangTong extends Person {
    private boolean hasNiePan = false;

    public PangTong() {
        super(3, Nation.SHU);
        // super(1, Nation.SHU);
    }

    @Override
    public void usePhaseBefore() {

        if (isActiveSkill("连环") && hasColorHandCard(Color.CLUB)) {
            getSkillCards().add("连环");
        }
    }

    @Skill("连环")
    @Override
    public boolean useSkillInUsePhase(int orderInt) {
        // int orderInt = Integer.valueOf(order) - 1;

        if (orderInt < getSkillCards().size() && getSkillCards().get(orderInt).equals("连环")) {
            getPriviMsg()
                    .setOneTimeInfo1(Text.format("\n💬连环：你可以将一张梅花手牌当[铁索连环]使用或重铸。", Card.getColorEmoji(Color.CLUB)));
            Card c = requestColor(Color.CLUB);
            if (c == null) {
                return true;
            }

            getPriviMsg().setOneTimeInfo1(Text.format("\n💬连环：请选择重铸或使用"));
            if (chooseNoNull("重铸", "使用") == 1) {
                drawCard();
                String res = Text.format("%s %s 重铸 %s:<i>回炉再造，变废为宝！</i>",
                        getPlateName(), getSkillHtmlName("连环"), c.getHtmlNameWithColor());
                // sleep(1000);
                getGameManager().getIo().printlnPublic(res, toString());
                return true;
            } else {
                TieSuoLianHuan ts = new TieSuoLianHuan(getGameManager(), c.color(), 0);
                if (ts.askTarget(this)) {
                    useCard(ts);
                    // 假的不用丢
                    String res = Text.format("%s %s 使用 %s 当作铁索连环:<i>铁索连舟，如履平地！</i>",
                            getPlateName(), getSkillHtmlName("连环"), c.getHtmlNameWithColor());
                    // sleep(1000);
                    getGameManager().getIo().printlnPublic(res, toString());
                } else {
                    addCard(getGameManager().getCardsHeap().retrieve(c), false);
                }
            }
        }
        return false;
    }

    @RestrictedSkill("涅槃")
    @Override
    public void dying(Person source) {
        if (getUser() == null) {
            super.dying(source);
            return;
        }
        if (!hasNiePan) {
            hasNiePan = true;

            MsgObj publicMsgObj = MsgObj.newMsgObj(getGameManager());

            boolean active = launchSkillPublic(
                    publicMsgObj,
                    "涅槃",
                    Text.format("%s 是否用 %s", getHtmlName(), getSkillHtmlName("涅槃")),
                    "pannie"

            );

            if (active) {
                loseCard(getRealJudgeCards());
                loseCard(getCardsAndEquipments());
                setCurrentHP(3);
                drawCards(3);
                if (isTurnedOver()) {
                    turnover();
                }
                if (isLinked()) {
                    switchLink();
                }
                setDrunk(false);
                setDrunkShaUsed(false);
                publicMsgObj.replyMakup = null;
                publicMsgObj.text = publicMsgObj.text + Text.format(",凤从天降,%s复活:<i>涅槃是乐，浩然大均！</i>",
                        getHtmlName());
                // sleep(1000);
                getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
                return;
            } else {
                publicMsgObj.replyMakup = null;
                publicMsgObj.text = publicMsgObj.text + Text.format(",%s放弃复活", getPlateName());
                // sleep(1000);
                getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
            }
        }
        super.dying(source);
    }

    @Override
    public String name() {
        return "庞统";
    }

    @Override
    public String skillsDescription() {
        return "连环：你可以将一张梅花手牌当[铁索连环]使用或重铸。\n" +
                "涅槃：限定技，当你处于濒死状态时，你可以弃置区域里的所有牌，然后复原你的武将牌，摸三张牌，将体力回复至3点。";
    }
}
