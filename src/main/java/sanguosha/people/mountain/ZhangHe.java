package sanguosha.people.mountain;

import sanguosha.cards.Card;
import sanguosha.cards.Equipment;
import sanguosha.cards.JudgeCard;
import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.Skill;
import components.TimeLimit;

import java.util.ArrayList;
import java.util.List;

import config.Config;
import config.Text;
import msg.MsgObj;

public class ZhangHe extends Person {

    public ZhangHe() {
        super(4, Nation.WEI);
    }

    @Skill("巧变")
    public boolean qiaoBian(String s) {

        if (thisLaunchSkill("巧变:" + s)) {
            getPriviMsg().setOneTimeInfo1(Text.format("\n💬%s:请弃置一张手牌", "巧变:" + s));
            if (requestCard(null) != null) {
                return true;
            }
        }

        return false;
    }

    /**
     * 因为实际的展示名和技能名不一样，这里建一个新的launchSkill方法
     */
    private boolean thisLaunchSkill(String skillName) {
        if (!isActiveSkill("巧变")) {
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
    public boolean skipJudge() {
        // 跳过判定要在群内提醒,而且没有快速处理
        if (getJudgeCards().size() <= 0) {
            return false;
        }
        if (!isActiveSkill("巧变")) {
            return false;
        }

        MsgObj publicMsgObj = MsgObj.newMsgObj(getGameManager());
        if (launchSkillPublicDeepLink(
                publicMsgObj,
                "巧变:跳过判定",
                Text.format("%s 是否使用 %s,跳过判定阶段", getHtmlName(), getSkillHtmlName("巧变")), "qiaobian")) {
            getPriviMsg().setOneTimeInfo1(Text.format("\n💬%s:请弃置一张手牌", "巧变:跳过判定"));
            if (requestCard(null) != null) {
                setskipNoticeUsePublic(true);
                try {
                    return true;
                } finally {

                    String res = Text.format("%s:%s 跳过判定:<i>用兵之道，变化万千。</i>",
                            getSkillHtmlName("巧变"),
                            getPlateName());
                    //sleep(1000);
                    getGameManager().getIo().printlnPublic(res, toString());
                }
            }

        }

        return false;

    }

    @Override
    public void selfEndPhase(boolean fastMode) {

        if (!isZuoCi()) {
            setskipNoticeUsePublic(false);
        }
    }

    @Override
    public boolean skipDraw(boolean fastMode) {
        if (fastMode) {
            return false;
        }
        getPriviMsg().clearHeader2();
        getPriviMsg().setOneTimeInfo1("\n💬是否用 巧变：弃置一张手牌并跳过 摸牌阶段，你可以获得至多两名角色的各一张手牌");

        if (qiaoBian("跳过摸牌")) {
            List<Person> hasCardPersons = new ArrayList<>();
            for (Person p : getGameManager().getPlayersBeginFromPlayer(this)) {
                if (p == this) {
                    continue;
                }
                if (p.getCards().size() <= 0) {
                    continue;
                }
                hasCardPersons.add(p);
            }
            if (hasCardPersons.size() <= 0) {
                return false;
            }

            TimeLimit t = new TimeLimit(Config.PRIV_RND_TIME_60S);
            while (getGameManager().isRunning() && t.isNotTimeout()) {

                getPriviMsg().setOneTimeInfo1("💬你可以获得至多两名角色的各一张手牌,请选择最多两名有手牌的玩家");
                List<Person> results = chooseManyFromProvided(0, hasCardPersons);
                if (results.size() == 0 || results.size() > 2) {
                    continue;
                }

                for (Person p : results) {
                    Card c1 = randomChooseTargetCards(p);
                    p.loseCard(c1, false);
                    addCard(c1);
                }

                try {
                    return true;

                } finally {
                    String p1 = results.get(0).getHtmlName();
                    String p2 = results.size() == 2 ? results.get(1).getHtmlName() : "";
                    String res = Text.format("%s:抽取%s %s各1张牌:<i>兵无常势，水无常形。</i>",
                            getSkillHtmlName("巧变"),p1, p2);
                    //sleep(1000);
                    getGameManager().getIo().printlnPublic(res, toString());
                }
            }
            return false;
        } else {
            return false;
        }
    }

    @Override
    public boolean skipUse(boolean fastMode) {
        if (fastMode) {
            return true;
        }
        getPriviMsg().clearHeader2();
        getPriviMsg().setOneTimeInfo1("\n💬是否用 巧变：弃置一张手牌并跳过 出牌阶段，你可以将一名角色已装备的装备牌或判定牌置入另一名角色区域里的相应位置");

        if (qiaoBian("跳过出牌")) {
            // printlnToIOPriv("choose source and target");
            getPriviMsg().setOneTimeInfo1("💬请选择要被你抽牌的角色(有装备或判定牌)");
            List<Person> persons = new ArrayList<>();
            for (Person p : getGameManager().getPlayersBeginFromPlayer(this)) {
                // if(p==this){ //可以选自己
                // continue;
                // }
                if (p.getEquipmentsList().size() + p.getJudgeCards().size() > 0) {
                    persons.add(p);
                }
            }

            Person p = selectPlayer(persons);
            if (p == null) {
                return false;
            }

            ArrayList<Card> options = new ArrayList<>(new ArrayList<>(p.getEquipments().values()));
            options.addAll(p.getJudgeCards());

            getPriviMsg().setOneTimeInfo1("💬请选择选择TA的一件装备或判定牌");

            Card c = chooseCard(options, true);
            if (c == null) {
                return true;
            }

            getPriviMsg().setOneTimeInfo1("💬请选择要被你放置牌的角色");

            Person target = selectPlayerExept(getGameManager().getPlayersBeginFromPlayer(this), p);
            if (target == null) {
                return false;
            }

            // c.setTaken(true);
            p.loseCard(c, false);

            if (c instanceof Equipment) {
                target.putOnEquipment(c);
            } else if (c instanceof JudgeCard) {
                target.addJudgeCard((JudgeCard) c);
            } else {
                getGameManager().panic("unknown type of card in 巧变: " + c);
            }

            try {
                return true;
            } finally {

                String res = Text.format("%s 将%s 的 %s 置于 %s:<i>万物皆有变换，岂能长久乎？</i>",
                        getPlateName(),
                        p.getHtmlName(),
                        c.getHtmlNameWithColor(),
                        target.getHtmlName());
                //sleep(1000);
                getGameManager().getIo().printlnPublic(res, toString());
            }

        }
        return false;
    }

    @Override
    public boolean skipThrow(boolean fastMode) {

        if (fastMode) {
            return false;
        }
        int num = getCards().size() - Math.max(getHP(), 0);
        if (num <= 0) {// 如果不需要弃牌则不需要跳过
            return false;
        }
        getPriviMsg().setOneTimeInfo1(Text.format("你需要弃置%s张牌,可用巧变跳过", num));
        return qiaoBian("跳过弃牌");
    }

    @Override
    public String name() {
        return "张郃";
    }

    @Override
    public String skillsDescription() {
        return "巧变：你可以弃置一张手牌并跳过一个阶段（准备阶段和结束阶段除外）。" +
                "若你以此法跳过摸牌阶段，你可以获得至多两名角色的各一张手牌；" +
                "若你以此法跳过出牌阶段，你可以将一名角色场上的装备牌或判定牌置入另一名角色区域里的相应位置。";
    }
}
