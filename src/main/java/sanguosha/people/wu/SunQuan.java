package sanguosha.people.wu;

import sanguosha.cards.Card;

import sanguosha.people.Identity;
import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.ForcesSkill;
import sanguosha.skills.KingSkill;
import sanguosha.skills.Skill;
import java.util.Set;

import config.Text;

import java.util.ArrayList;

public class SunQuan extends Person {
    public SunQuan() {
         super(4, Nation.WU);
        //super(1, Nation.WU);
    }

    @Override
    public void usePhaseBefore() {

        if (isActiveSkill("制衡") && getCards().size() > 0 && hasNotUsedSkill1()) {
            getSkillCards().add("制衡");
        }
    }

    @Skill("制衡")
    @Override
    public boolean useSkillInUsePhase( int orderInt) {
        //int orderInt = Integer.valueOf(order) - 1;

        if (orderInt < getSkillCards().size() && getSkillCards().get(orderInt).equals("制衡") && hasNotUsedSkill1()) {
            // printlnPriv(this + " uses 制衡");
            getPriviMsg().setOneTimeInfo1("制衡：出牌阶段限一次，你可以弃置任意张牌，然后摸等量的牌。");
            ArrayList<Card> cs = chooseManyCards(0, getCardsAndEquipments());
            if (!cs.isEmpty()) {
                loseCard(cs);
                drawCards(cs.size());

                String res = Text.format("%s %s:<i>容我三思。</i>", getPlateName(), getSkillHtmlName("制衡"));
                //sleep(1000);
                getGameManager().getIo().printlnPublic(res, toString());
                //sleep(3000);
            }
            setHasUsedSkill1(true);
            return true;
        }
        return false;
    }

    @ForcesSkill("救援")
    @KingSkill("救援")
    @Override
    public int gotSavedBy(Person p) {
        if (isActiveSkill("救援") && getIdentity() == Identity.KING && p.getNation() == Nation.WU) {
            // if ( p.getNation() == Nation.WU) {
            // printlnPriv(this + " uses 救援");
            recover(null,1);
            String res = Text.format("%s %s:<i>有汝辅佐，甚好！</i>", getPlateName(), getSkillHtmlName("救援"));
           
            getGameManager().getIo().printlnPublic(res, toString());
            //sleep(3000);
            return 1;
        }
        return 0;
    }

    @Override
    public Set<String> getInitialSkills() {
        Set<String> skills = super.getInitialSkills();

        if (getIdentity() != Identity.KING) {
            skills.remove("救援");
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
        return "孙权";
    }

    @Override
    public String skillsDescription() {
        return "制衡：出牌阶段限一次，你可以弃置任意张牌，然后摸等量的牌。\n" +
                "救援：主公技，锁定技，其他吴势力角色对你使用[桃]回复的体力+1。";
    }
}
