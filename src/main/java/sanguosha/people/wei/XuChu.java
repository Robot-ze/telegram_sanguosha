package sanguosha.people.wei;

import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.Skill;

public class XuChu extends Person {
    private boolean isNaked = false;

    public XuChu() {
        super(4, Nation.WEI);
    }

    @Skill("裸衣")
    @Override
    public void drawPhase(boolean fastMode) {
        if (! fastMode&&launchSkillPriv("裸衣")) {
            //printlnPriv(this + " draw 1 card from cards heap");
            drawCard();
            isNaked = true;
            return;
        }
        super.drawPhase(  fastMode);
    }

    @Override
    public void selfEndPhase(boolean fastMode) {
        isNaked = false;
    }

    @Override
    public boolean isNaked() {
        if(isNaked){
            String res ="<i>谁来与我大战三百回合！</i>";
            //sleep(1000);
            getGameManager().getIo().printlnPublic(res, toString());
        }
        return isNaked;
    }

    @Override
    public String name() {
        return "许褚";
    }

    @Override
    public String skillsDescription() {
        return "裸衣：摸牌阶段，你可以少摸一张牌，然后本回合你使用[杀]或[决斗]造成伤害时，此伤害+1。";
    }
}
