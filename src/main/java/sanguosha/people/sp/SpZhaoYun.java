package sanguosha.people.sp;

import config.Text;
import sanguosha.cards.Card;
import sanguosha.cards.basic.Sha;
import sanguosha.manager.Utils;
import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.Skill;

public class SpZhaoYun extends Person {
    public SpZhaoYun() {
        super(3, Nation.SHU);
    }

    @Skill("龙胆")
    public Card longDan(String type, boolean passive) {
        Utils.assertTrue(type.equals("杀") || type.equals("闪"), "illegal 龙胆 type:" + type);
        if (passive) {
            if (type.equals("闪")) {
                getPriviMsg().setOneTimeInfo1("\n💬请出 杀 当作 闪，你可以获得对方的一张手牌");
                Card c = requestCard("杀");
                if (c != null) {

                    String result = Text.format("龙胆闪!获对方牌1张:<i>进退自如，游刃有余！陷阵杀敌，一马当先。</i>");
                    //sleep(1000);
                    getGameManager().getIo().printlnPublic(result, toString());
                }
                return c;
            } else {
                getPriviMsg().setOneTimeInfo1("\n💬请出 闪 当作 杀，你可以获得对方的一张手牌");
                Card c = requestCard("闪");
                if (c != null) {
                    String result = Text.format("龙胆杀!获对方牌1张:<i>龙威虎胆，斩敌破阵！主公放心，我去去就回。</i>");
                    //sleep(1000);
                    getGameManager().getIo().printlnPublic(result, toString());
                }
                return c;
            }
        } else {
            getPriviMsg().setOneTimeInfo1("\n💬请出 闪 当作 杀，你可以获得对方的一张手牌");
            Card c = requestCard("闪");
            return c;
        }

    }

    @Override
    public boolean skillShan(Person sourse) {
        getPriviMsg().setOneTimeInfo1("\n💬是否用龙胆：你可以将[杀][闪]互换打出。");
        if (launchSkillPriv("龙胆")) {
            Card c = longDan("闪", true);
            if (c != null) {
                Card got = randomChooseTargetCards(sourse);
                if (got != null) {
                    sourse.loseCard(got, false);
                    addCard(got);
                }
            }
            return c != null;
        }
        return false;
    }

    @Override
    public boolean skillSha(Person sourse) {
        getPriviMsg().clearHeader2();
        getPriviMsg().setOneTimeInfo1("\n💬是否用龙胆：你可以将[杀][闪]互换打出，你可以获得对方的一张手牌");
        if (launchSkillPriv("龙胆")) {
            Card c = longDan("杀", true);
            if (c != null) {
                Card got = randomChooseTargetCards(sourse);
                if (got != null) {
                    sourse.loseCard(got, false);
                    addCard(got);
                }
            }
            return c != null;
        }
        return false;
    }

    @Override
    public void usePhaseBefore() {
        if (isActiveSkill("龙胆") && getCards().size() > 0&&checkSha(null,false) ) {
            getSkillCards().add("龙胆");
        }
    }

    @Override
    public boolean useSkillInUsePhase(int orderInt) {
        // int orderInt = Integer.valueOf(order) - 1;

        if (orderInt < getSkillCards().size() &&
                getSkillCards().get(orderInt).equals("龙胆") 
                
                ) {

            // if (!checkSha(null)) {
            //     return false;
            // }

            Card c = longDan("杀", false);
            if (c != null) {
                Sha sha = new Sha(getGameManager(), c.color(), c.number());
                sha.addReplaceCard(c);
                sha.setMultiSha(1);// 先重置一下

                if (!sha.askTarget(this)) {
                    return false;
                }
                setShaMulti(sha);
                useCard(sha);

                if (sha.getMultiSha() <= 1) {
                    Card got = randomChooseTargetCards(sha.getTarget());
                    if (got != null) {
                        sha.getTarget().loseCard(got, false);
                        addCard(got);
                    }
                } else {// 可能砍到多个人
                    for (Person target : sha.getTargets()) {
                        Card got = randomChooseTargetCards(target);
                        if (got != null) {
                            target.loseCard(got, false);
                            addCard(got);
                        }
                    }

                }

                String result = Text.format("龙胆杀!获对方牌%s张:<i>龙威虎胆，斩敌破阵！主公放心，我去去就回。</i>",
                        sha.getMultiSha());
                //sleep(1000);
                getGameManager().getIo().printlnPublic(result, toString());
            }
            return true;
        }
        return false;
    }

    @Override
    public String name() {
        return "SP赵云";
    }

    @Override
    public String skillsDescription() {
        return "龙胆：你可以将一张[杀]当[闪]、[闪]当[杀]使用或打出。" +
                "冲阵：当你发动“龙胆”时，你可以获得对方的一张手牌。";
    }
}
