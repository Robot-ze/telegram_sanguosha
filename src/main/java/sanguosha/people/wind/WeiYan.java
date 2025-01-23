package sanguosha.people.wind;

import config.Text;
import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.ForcesSkill;

public class WeiYan extends Person {
    public WeiYan() {
        super(4, Nation.SHU);
    }

    @ForcesSkill("狂骨")
    @Override
    public void hurtOther(Person p, int num) {
        if (isActiveSkill("狂骨") && getGameManager().calDistance(this, p) <= 1) {
            // printlnPriv(this + " uses 狂骨");
            recover(null,num);

            //sleep(1000);
            String res = Text.format("回复%s体力%s:<i>我自横扫天下，蔑视群雄又如何！</i>",
                  num,getHPEmoji());
            getGameManager().getIo().printlnPublic(res, toString());
        }
    }

    @Override
    public String name() {
        return "魏延";
    }

    @Override
    public String skillsDescription() {
        return "狂骨：锁定技，当你对距离1以内的一名角色造成1点伤害后，你回复1点体力。";
    }
}
