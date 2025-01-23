package sanguosha.people.qun;

import java.util.Set;

import config.Text;
import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.ForcesSkill;

public class LvBu extends Person {
    public LvBu() {
        super(4, Nation.QUN);
    }


    @Override
    public void wushuangSha() {

        String res = Text.format("<i>神挡杀神，佛挡杀佛！</i>");
        getGameManager().getIo().printlnPublic(res, toString());
        //sleep(3000);
    }

    @ForcesSkill("无双")
    @Override
    public boolean hasWuShuang() {
        //System.out.println("getActiveSkills()="+getActiveSkills());
        return isActiveSkill("无双");
    }

    @Override
    public Set<String> getInitialSkills() {
        // TODO Auto-generated method stub
        return super.getInitialSkills();
    }

    @Override
    public String name() {
        return "吕布";
    }

    @Override
    public String skillsDescription() {
        return "无双：锁定技，你使用的[杀]需两张[闪]才能抵消；与你进行[决斗]的角色每次需打出两张[杀]。";
    }
}
