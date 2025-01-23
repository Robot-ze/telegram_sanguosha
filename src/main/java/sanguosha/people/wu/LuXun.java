package sanguosha.people.wu;

import config.Text;
import msg.MsgObj;
import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.ForcesSkill;
import sanguosha.skills.Skill;

public class LuXun extends Person {
    public LuXun() {
         super(3, Nation.WU);
        //super(1, Nation.WU);
    }

    @ForcesSkill("谦逊")
    @Override
    public boolean hasQianXun() {
        return isActiveSkill("谦逊");
    }

    @Skill("连营")
    @Override
    public void lostHandCardAction() {

        // System.out.println("getCards().size()="+getCards().size());
        if (getCards().isEmpty()) {
            String code = "lianying";
            if (launchSkillPublic(
                    "连营",
                    Text.format("%s 连营:当你失去最后的手牌时，你可以摸一张牌", getHtmlName()),
                    code)) {

                drawCard();
                
                String res = Text.format(",<i>旧的不去，新的不来~</i>" );
                MsgObj tempMsg = getTempActionMsgObj(code);
                tempMsg.appendText(res);
                getGameManager().getMsgAPI().editCaptionForce(tempMsg);
                //sleep(3000);
            }
        }
    }

    @Override
    public String name() {
        return "陆逊";
    }

    @Override
    public String skillsDescription() {
        return "谦逊：锁定技，你不能成为[顺手牵羊]和[乐不思蜀]的目标。\n" +
                "连营：当你失去最后的手牌时，你可以摸一张牌。";
    }
}
