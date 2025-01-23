package sanguosha.people.wu;

import config.Text;
import sanguosha.cards.Card;
import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.Skill;

public class LvMeng extends Person {
    private boolean hasSha = false;

    public LvMeng() {
        super(4, Nation.WU);
    }

    @Override
    public void selfBeginPhase() {
        hasSha = false;
    }

    @Override
    public void shaBegin(Card card) {
        hasSha = true;
    }

    @Skill("克己")
    @Override
    public int throwPhase(boolean fastMode) {
        if (!hasSha && launchSkillPriv("克己")) {
            String res=Text.format("%s %s:<i>不是不报，时候未到！</i>", getPlateName(),getSkillHtmlName("克己"));
        
            getGameManager().getIo().printlnPublic(res, toString());
            //sleep(3000);
            return 0;
        }
   

        return super.throwPhase(fastMode);
    }

    @Override
    public String name() {
        return "吕蒙";
    }

    @Override
    public String skillsDescription() {
        return "克己：若你未于出牌阶段内使用或打出过[杀]，你可以跳过弃牌阶段。";
    }
}
