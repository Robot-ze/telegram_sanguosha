package sanguosha.people.fire;

import config.Text;
import msg.MsgObj;
import sanguosha.cards.Card;
import sanguosha.people.AI;
import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.ForcesSkill;
import sanguosha.skills.Skill;

public class PangDe extends Person {
    public PangDe() {
        super(4, Nation.QUN);
    }

    @ForcesSkill("马术")
    @Override
    public boolean hasMaShu() {
        return isActiveSkill("马术");
    }

    @Skill("猛进")
    @Override
    public void shaGotShan(Person p) {
        if (!p.getCardsAndEquipments().isEmpty()) {
            MsgObj publicMsgObj = MsgObj.newMsgObj(getGameManager());
            boolean active = launchSkillPublicDeepLink(
                    publicMsgObj,
                    "猛进",
                    Text.format("%s 是否用 %s",
                            getHtmlName(),getSkillHtmlName("猛进")),
                    "mengjin");

            if (active) {
                if (p.getCardsAndEqSize() <= 0) {
                    publicMsgObj.replyMakup = null;
                    publicMsgObj.text = publicMsgObj.text + Text.format(",%s已无手牌", p.getHtmlName());
                    //sleep(1000);
                    getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
                    return;
                }
                Card c = randomChooseTargetCards(p,true);
                p.loseCard(c);
                publicMsgObj.replyMakup = null;
                publicMsgObj.text = publicMsgObj.text
                        + Text.format(",%s被随机弃置一张牌 %s", p.getHtmlName(), c.getHtmlNameWithColor());
                //sleep(1000);
                getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
            } else {
                publicMsgObj.replyMakup = null;
                publicMsgObj.text = publicMsgObj.text + Text.format("\n%s 放弃", getPlateName());
                //sleep(1000);
                getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
            }

        }
    }

    @Override
    public String name() {
        return "庞德";
    }

    @Override
    public String skillsDescription() {
        return  "马术：锁定技，你计算与其他角色的距离-1。\n" +
                "猛进：当你使用的[杀]被目标角色的[闪]抵消时，你可以弃置其一张牌。";
    }
}
