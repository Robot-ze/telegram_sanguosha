package sanguosha.people.forest;

import config.Text;
import msg.MsgObj;
import sanguosha.cards.Card;

import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.ForcesSkill;
import sanguosha.skills.Skill;

public class ZhuRong extends Person {
    public ZhuRong() {
        super(4, "female", Nation.SHU);
    }

    @ForcesSkill("巨象")
    @Override
    public boolean hasJuXiang() {
        try {
            return isActiveSkill("巨象");
        } finally {
            String result = Text.format("%s %s 获得 南蛮入侵:<i>烈刃之火，横扫千军！</i>", getHtmlName(),
                    getSkillHtmlName("巨象"));
            //sleep(1000);
            getGameManager().getIo().printlnPublic(result, toString());
        }

    }

    @Skill("烈刃")
    @Override
    public void shaSuccess(Person p) {
        if (getCards().size() > 0 && p.getCards().size() > 0) {

            if (launchSkillPublicDeepLink(

                    "烈刃",
                    Text.format("%s 是否用 %s",
                            getHtmlName(), getSkillHtmlName("烈刃")),
                    "lieren1"

            )) {
                if (getGameManager().pinDian(this, p)) {
                    MsgObj publicMsg = getTempActionMsgObj("pindian");

                    if (p.getCardsAndEqSize() <= 0) { // 别人拼点导致手牌没有了，也视作使用成功
                        String result = Text.format(",%s 已无手牌:<i>象群撤退</i>", p.getHtmlName());
                        publicMsg.text = publicMsg.text + result;
                        getGameManager().getIo().delaySendAndDelete(this,Text.format("%s 已无手牌,你无牌可拿", p.getHtmlName()));
                        //sleep(1000);
                        getGameManager().getMsgAPI().editCaptionForce(publicMsg);
                        return;
                    }
                    Card c = randomChooseTargetCards(p);
                    p.loseCard(c, false);
                    addCard(c);
                    String result = Text.format(",%s 被夺走一张牌:<i>看你等如何抵挡我这大象阵，哈哈哈哈哈~上！</i>", p.getHtmlName());
                    publicMsg.text = publicMsg.text + result;
                    getGameManager().getIo().delaySendAndDelete(this,Text.format("你获得 %s 一张牌",p.getHtmlName()));
                    //sleep(1000);
                    getGameManager().getMsgAPI().editCaptionForce(publicMsg);
                }
            }
        }
    }

    @Override
    public String name() {
        return "祝融";
    }

    @Override
    public String skillsDescription() {
        return "巨象：锁定技，[南蛮入侵]对你无效；当其他角色使用的[南蛮入侵]结算结束后，你获得之。\n" +
                "烈刃：当你使用[杀]对目标角色造成伤害后，你可以与其拼点，若你赢，你获得其一张牌。";
    }
}
