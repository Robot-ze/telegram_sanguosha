package sanguosha.people.shu;

import config.Text;
import sanguosha.cards.Card;
import sanguosha.cards.EquipType;
import sanguosha.cards.basic.Sha;

import sanguosha.manager.Utils;
import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.Skill;

public class ZhaoYun extends Person {
    public ZhaoYun() {
        super(4, Nation.SHU);
    }

    @Skill("龙胆")
    public Card longDan(String type, boolean passive) {
        Utils.assertTrue(type.equals("杀") || type.equals("闪"), "illegal 龙胆 type:" + type);
        if (passive) {
            if (type.equals("闪")) {
                getPriviMsg().setOneTimeInfo1("\n💬请出 杀 当作 闪");
                Card c = requestCard("杀");
                if (c != null) {
                    String result = Text.format("龙胆闪!<i>进退自如，游刃有余！</i>");
                    //sleep(1000);
                    getGameManager().getIo().printlnPublic(result, toString());
                }
                return c;
            } else {
                getPriviMsg().setOneTimeInfo1("\n💬请出 闪 当作 杀");
                Card c = requestCard("闪");
                if (c != null) {
                    String result = Text.format("龙胆杀!<i>龙威虎胆，斩敌破阵！</i>");
                    //sleep(1000);
                    getGameManager().getIo().printlnPublic(result, toString());
                }
                return c;
            }

        } else {
            Card c = requestCard("闪");
            return c;
        }

    }

    @Override
    public boolean skillShan(Person sourse) {
        getPriviMsg().setOneTimeInfo1("\n💬是否用龙胆：你可以将[杀][闪]互换打出。");
        if (launchSkillPriv("龙胆")) {
            return longDan("闪", true) != null;
        }
        return false;
    }

    @Override
    public boolean skillSha(Person sourse) {
        getPriviMsg().clearHeader2();
        getPriviMsg().setOneTimeInfo1("\n💬是否用龙胆：你可以将[杀][闪]互换打出。");
        if (launchSkillPriv("龙胆")) {
            return longDan("杀", true) != null;
        }
        return false;
    }

    @Override
    public void usePhaseBefore() {
        if (isActiveSkill("龙胆") && getCards().size() > 0 && checkSha(null, false)) {
            getSkillCards().add("龙胆");
        }
    }

    @Override
    public boolean useSkillInUsePhase(int orderInt) {
        // int orderInt = Integer.valueOf(order) - 1;

        if (orderInt < getSkillCards().size()
                && getSkillCards().get(orderInt).equals("龙胆")) {

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
                String result = Text.format("龙胆杀!<i>龙威虎胆，斩敌破阵！</i>");
                //sleep(1000);
                getGameManager().getIo().printlnPublic(result, toString());
            }
            return true;
        }
        return false;
    }

    @Override
    public String name() {
        return "赵云";
    }

    @Override
    public String skillsDescription() {
        return "龙胆：你可以将一张[杀]当[闪]、[闪]当[杀]使用或打出。";
    }
}
