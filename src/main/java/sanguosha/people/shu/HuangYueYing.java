package sanguosha.people.shu;

import config.Text;
import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.ForcesSkill;
import sanguosha.skills.Skill;

public class HuangYueYing extends Person {
    public HuangYueYing() {
        super(3, "female", Nation.SHU);
    }

    @Skill("急智")
    @Override
    public void useStrategy() {

        //getPriviMsg().setOneTimeInfo1("\n💬是否用集智：当你使用普通锦囊牌时，你可以摸一张牌。");
        //if (launchSkillPriv("急智")) {
            drawCard();

            String result=Text.format("%s:<i>机巧谋略，我皆不输于你</i>", 
            getSkillHtmlName("急智"));
            //sleep(1000);
            getGameManager().getIo().printlnPublic(result, toString());
            //sleep(3000);
       // }
    }

    @ForcesSkill("奇才")
    @Override
    public boolean hasQiCai() {
        return isActiveSkill("奇才");
    }

    @Override
    public String name() {
        return "黄月英";
    }

    @Override
    public String skillsDescription() {
        return "集智：当你使用普通锦囊牌时，你可以摸一张牌。\n" +
                "奇才：锁定技，你使用锦囊牌无距离限制。";
    }
}
