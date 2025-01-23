package sanguosha.people.wind;

import config.Config;
import config.Text;
import sanguosha.cards.Card;
import sanguosha.cards.Color;
import sanguosha.cards.Equipment;
import sanguosha.cards.basic.Sha;
import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.Skill;
import components.TimeLimit;

public class XiaHouYuan extends Person {
    private boolean noDistanceLimit = false;
 
    private boolean useShenSu1 = false;

    public XiaHouYuan() {
        super(4, Nation.WEI);
    }

    @Skill("神速")
    public boolean useShenSu() {
        try {
            noDistanceLimit = true;

            Sha sha = new Sha(getGameManager(), Color.NOCOLOR, 0);
            sha.addReplaceCard((Card) null);
            getPriviMsg().setOneTimeInfo2("你使用了神速,可攻击一个角色");
            if (sha.askTarget(this)) {

                String res = Text.format("%s %s:<i>急速行军，一蹴而就！</i>",
                        getPlateName(), getSkillHtmlName("神速"));
                getGameManager().getIo().printlnPublic(res, toString());
                //sleep(1000);
                useCard(sha);

                return true;
            }
            return false;
        } finally {
            noDistanceLimit = false;
        }
    }

    @Override
    public void selfBeginPhase() {
        // getPriviMsg().setOneTimeInfo1("神速:跳过判定与摸牌阶段,视为对一名其他角色使用一张[杀]");
        getPriviMsg().clearAll();
        if (launchSkillPublicDeepLink(
                "神速",
                "你可以使用神速:跳过判定(使判定牌无效)与摸牌阶段(不摸牌),视为对一名其他角色使用一张[杀]",
                "shensu1")) {
            
            setskipNoticeUsePublic(true);
      
            boolean used = useShenSu();
            if (used) {
                useShenSu1 = true;
            }
        }
    }

    @Override
    public boolean skipJudge() {
        return useShenSu1;
    }

    @Override
    public boolean skipDraw(boolean fastMode) {
        if (fastMode) {
            return false;
        }
        return useShenSu1;
    }

 

    @Override
    public void usePhase(boolean fastMode) {
        getPriviMsg().setOneTimeInfo1("神速:不出牌并弃一装备牌,视为对一名其他角色使用一张[杀]");

        if (thisLaunchSkill("神速-跳过出牌")) {
            getPriviMsg().setOneTimeInfo1("神速:请弃一装备牌");
            Card c = chooseCard(getCardsAndEquipments(), true);
            TimeLimit t = new TimeLimit(Config.PRIV_RND_TIME_60S);
            while (getGameManager().isRunning() && (!(c instanceof Equipment) && c != null)) {
                if (t.isTimeout()) {
                    return;
                }
                // printlnToIOPriv("you should choose an equipment");
                getPriviMsg().setOneTimeInfo1("神速:请弃一装备牌");
                c = chooseCard(getCardsAndEquipments(), true);
            }
            if (c != null) {
                c.setTaken(false);
                throwCard(c);
                if (useShenSu()) {
                    return;
                } else {// 如果这个假杀没攻击人
                    getGameManager().getCardsHeap().retrieve(c);
                    addCard(c);

                }

            }
        }
        super.usePhase(  fastMode);
    }

    private boolean thisLaunchSkill(String skillName) {
        if (isZuoCi() && !isActiveSkill("神速")) {
            return false;
        }
        int choice = chooseFromProvided(null, true, skillName);
        // if (choice != -1 && choice.equals(skillName)) {
        if (choice == 0) {
            // printlnPriv(this + " uses " + skillName);
            return true;
        }
        return false;
    }

    @Override
    public int getShaDistance() {
        if (noDistanceLimit) {
            return 10000;
        }
        return super.getShaDistance();
    }

    @Override
    public void selfEndPhase(boolean fastMode) {
        if(!isZuoCi()){
            setskipNoticeUsePublic(false); 
        }
        useShenSu1 = false;

    }

    @Override
    public String name() {
        return "夏侯渊";
    }

    @Override
    public String skillsDescription() {
        return "神速：你可以做出如下选择：1.跳过你此回合的判定阶段和摸牌阶段。2.跳过出牌阶段并弃置一张装备牌。你每选择一项，视为对一名其他角色使用一张[杀]。";
    }
}
