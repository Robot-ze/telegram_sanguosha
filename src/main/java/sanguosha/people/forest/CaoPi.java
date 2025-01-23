package sanguosha.people.forest;

import java.util.List;
import java.util.Set;

import config.Text;
import msg.MsgObj;
import sanguosha.cards.Card;
import sanguosha.people.Identity;
import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.KingSkill;
import sanguosha.skills.Skill;

public class CaoPi extends Person {
    public CaoPi() {
        super(3, Nation.WEI);
    }

    @Skill("行殇")
    @Override
    public boolean usesXingShang(Person deadPerson) {
        MsgObj publicMsg = MsgObj.newMsgObj(getGameManager());

        if (launchSkillPublic(
                publicMsg,
                "行殇",
                Text.format("\n💬%s 是否 %s：%s 阵亡了，你可以获得其所有牌。",
                        getHtmlName(),
                        getSkillHtmlName("行殇"),
                        deadPerson.getPlateName()),

                "xingshang")) {
            try {
                return true;
            } finally {
                String res = Text.format(",%s 获得%s张牌:<i>生不带来，死不带去。</i>",
                        getPlateName(), deadPerson.getDeadShowCards().size());
                publicMsg.appendText(res);
                //sleep(1000);
                getGameManager().getMsgAPI().editCaptionForce(publicMsg);
            }

        }
        return false;

    }

    @Skill("放逐")
    @Override
    public void gotHurt(List<Card> cards, Person p, int num) {
        MsgObj publicMsg = MsgObj.newMsgObj(getGameManager());

        if (launchSkillPublicDeepLink(
                publicMsg,
                "放逐",
                Text.format("\n💬%s 是否用 %s：当你受伤，可翻面一名角色，TA摸X张牌（X为你已损体力）。",
                        getHtmlName(), getSkillHtmlName("放逐")),
                "fangzhu")) {

            String info = Text.format("放逐:请选择一个玩家");
            getPriviMsg().setOneTimeInfo1(info);
            Person target = selectPlayer();
            if (target != null) {
                int cardNum = getMaxHP() - getHP();
                target.drawCards(cardNum);
                target.turnover();
                String res = Text.format(",%s 摸 %s张牌并翻面:<i>这是本王最后的仁慈。</i>",
                        target.getHtmlName(), cardNum);
                publicMsg.appendText(res);
                //sleep(1000);
                getGameManager().getMsgAPI().editCaptionForce(publicMsg);
            }
        }
    }

    @KingSkill("颂威")
    @Override
    public void otherPersonGetJudge(Person p, Card jugeCard) {
        // 这个技能是给其他人用的，
        if (getIdentity() == Identity.KING && jugeCard.isBlack()
                && p.getNation() == Nation.WEI) {
            MsgObj publicMsg = MsgObj.newMsgObj(getGameManager());
            String self = (p == this) ? "(你)" : "(不是你)";
            if (p.launchSkillPublic(
                    publicMsg,
                    "使用颂威",
                    Text.format("%s,可以令主公 %s %s摸一张牌",
                            getSkillHtmlName("颂威"),
                            this.getPlateName(),
                            self, false),
                    "songwei")) {
                drawCard();
                String res = Text.format(",%s 摸一张牌:<i>大魏皇帝，四海称臣！</i>",
                        getPlateName());
                publicMsg.appendText(res);
                //sleep(1000);
                getGameManager().getMsgAPI().editCaptionForce(publicMsg);
            } else {
                String res = Text.format(":<i>大胆！不纳贡吾必讨汝</i>",
                        getPlateName());
                publicMsg.appendText(res);
                //sleep(1000);
                getGameManager().getMsgAPI().editCaptionForce(publicMsg);
            }

        }
    }

    @Override
    public Set<String> getInitialSkills() {
        Set<String> skills = super.getInitialSkills();

        if (getIdentity() != Identity.KING) {
            skills.remove("颂威");
        }
        return skills;
    }

    // @Override
    // public void clearAndAddActiveSkill(String... skills) {
    // // TODO Auto-generated method stub
    // super.clearAndAddActiveSkill(skills);
    // }

    @Override
    public String name() {
        return "曹丕";
    }

    @Override
    public String skillsDescription() {
        return "行殇：当其他角色死亡时，你可以获得其所有牌。\n" +
                "放逐：当你受到伤害后，你可以令一名其他角色翻面，然后该角色摸X张牌（X为你已损失的体力值）。\n" +
                "颂威：主公技，当其他魏势力角色的黑色判定牌生效后，其可以令你摸一张牌。";
    }
}
