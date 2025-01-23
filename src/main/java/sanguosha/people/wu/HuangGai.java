package sanguosha.people.wu;

import config.Text;
import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.Skill;

public class HuangGai extends Person {
    public HuangGai() {
        super(4, Nation.WU);
    }

    @Override
    public void usePhaseBefore() {

        if (isActiveSkill("苦肉")) {//没有血都可以触发
            getSkillCards().add("苦肉");
        }
    }

    @Skill("苦肉")
    @Override
    public boolean useSkillInUsePhase( int orderInt) {
        //int orderInt = Integer.valueOf(order) - 1;

        if (orderInt < getSkillCards().size() && getSkillCards().get(orderInt).equals("苦肉")) {
            //printlnPriv(this + " uses 苦肉");
            loseHP(1,this);
            drawCards(2);
            
            String res = Text.format("失去1点体力%s，然后摸两张牌:<i>请鞭笞我吧，公瑾！</i>",getHPEmoji());
            getGameManager().getIo().printlnPublic(res, toString());
            //sleep(3000);
            return true;
        }
        return false;
    }

    @Override
    public String name() {
        return "黄盖";
    }

    @Override
    public String skillsDescription() {
        return "苦肉：出牌阶段，你可以失去1点体力，然后摸两张牌。";
    }
}
