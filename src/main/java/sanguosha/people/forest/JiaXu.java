package sanguosha.people.forest;

import sanguosha.cards.Card;
import sanguosha.cards.JudgeCard;
import sanguosha.cards.basic.Sha;
import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.ForcesSkill;
import sanguosha.skills.RestrictedSkill;

import java.util.List;

import config.Text;
import msg.MsgObj;

import java.util.ArrayList;

public class JiaXu extends Person {
    private boolean hasLuanWu;

    public JiaXu() {
        super(3, Nation.QUN);
    }

    @ForcesSkill("完杀")
    @Override
    public boolean hasWanSha() {
        // printlnPriv(this + "try uses 完杀");
        // 叫破喉咙，没有人会来救你的

        try {
            return isActiveSkill("完杀");
        } finally {
            String res = Text.format("%s %s:<i>叫破喉咙，没有人会来救你的</i>",
                    getPlateName(), getSkillHtmlName("完杀"));
            getGameManager().getIo().printlnPublic(res, toString());
        }

    }

    public ArrayList<Person> nearestPerson(Person p) {
        ArrayList<Person> canReachList = getGameManager().reachablePeople(p, getShaDistance());
        if (canReachList.isEmpty()) {
            return canReachList;
        }
        ArrayList<Person> nearby = new ArrayList<>();
        int minDistance = 100;
        for (Person p2 : canReachList) {
            if (p2 == p) {
                continue;
            }
            if (p2.hasKongCheng() && p2.getCards().isEmpty()) {
                continue;
            }
            if (getGameManager().calDistance(p, p2) < minDistance) {
                nearby.clear();
                nearby.add(p2);
                minDistance = getGameManager().calDistance(p, p2);
            } else if (getGameManager().calDistance(p, p2) == minDistance) {
                nearby.add(p2);
            }
            // System.out.println("nearby="+nearby);
        }

        return nearby;
    }

    @Override
    public void usePhaseBefore() {

        if (isActiveSkill("乱武") && !hasLuanWu) {
            getSkillCards().add("乱武");
        }
    }

    @RestrictedSkill("乱武")
    @Override
    public boolean useSkillInUsePhase(int orderInt) {
        // int orderInt=Integer.valueOf(order)-1 ;

        if (orderInt < getSkillCards().size() && getSkillCards().get(orderInt).equals("乱武") && !hasLuanWu) {

            String info = Text.format("%s %s 天下大乱:<i>哭喊吧，哀求吧，挣扎吧，然后死吧！</i>",
                    toString(), getSkillHtmlName("乱武"));
            getGameManager().getIo().printlnPublic(info, toString());
            //sleep(1000);

            // 存出一个新数组用来规避那个结构变化
            List<Person> list = new ArrayList<>(getGameManager().getPlayersBeginFromPlayer(this));
            for (int i = 0; i < list.size(); i++) {
                Person p = list.get(i);
                // if (p == this) { //test
                // continue;
                // }
                // 特殊技能是不能用无懈可击的
                p.getPriviMsg().clearHeader2();
                p.getPriviMsg().setOneTimeInfo1(
                        Text.format("\n💬%s,%s 使用了 乱武 天下大乱,你需选择一名距离最近的角色使用一张[杀]，否则受1点伤", p, toString()));
                MsgObj publicMsgObj = MsgObj.newMsgObj(getGameManager());
                boolean active = p.launchSkillPublicDeepLink(
                        publicMsgObj,
                        "杀",
                        Text.format("%s,你需[杀]一名距离最近的角色，否则受1点伤。",
                                p.getHtmlName()),
                        "luanwu1",
                        false);

                if (!active) {
                    int realNum = p.hurt((Card) null, this, 1);
                    String res = Text.format(",放弃,受%s点伤:%s",realNum, p.getHPEmoji());
                    publicMsgObj.appendText(res);
                    //sleep(1000);
                    getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
                    continue;
                }

                // printlnPriv("乱武 towards " + p);
                ArrayList<Person> p2s = nearestPerson(p);
                // System.out.println("nearestPerson(p)="+p2s);

                if (p2s.isEmpty()) {
                    int realNum = p.hurt((Card) null, this, 1);
                    String res = Text.format(",无人可攻击,受%s点伤:%s",  realNum, p.getHPEmoji());
                    publicMsgObj.appendText(res);
                    //sleep(1000);
                    getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
                    continue;
                }
                Person p2 = null;
                if (p2s.size() > 1) {
                    p.getPriviMsg().clearHeader2();
                    p.getPriviMsg().setOneTimeInfo1(
                            Text.format("\n💬%s,%s 使用了 乱武 天下大乱,你需选择一名距离最近的角色使用一张[杀]，否则受1点伤", p, toString()));
                    int pos = p.chooseFromProvided(null, true, p2s);
                    if (pos >= 0) {
                        p2 = p2s.get(pos);
                    }

                }
                if (p2 == null) {
                    int realNum = p.hurt((Card) null, this, 1);
                    String res = Text.format(",未出杀,受%s点伤:%s",  realNum, p.getHPEmoji());
                    publicMsgObj.appendText(res);
                    //sleep(1000);
                    getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
                    continue;
                }
                p.getPriviMsg().setOneTimeInfo1("乱武 天下大乱:请出一个杀");
                Sha sha = p.requestSha(p2);
                if (sha == null) {
                    int realNum = p.hurt((Card) null, this, 1);
                    String res = Text.format(",未出杀,受%s点伤:%s",realNum, p.getHPEmoji());
                    publicMsgObj.appendText(res);
                    //sleep(1000);
                    getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
                    continue;
                }
                getGameManager().getCardsHeap().retrieve(sha);
                sha.setSource(p);
                sha.setTarget(p2);
                if (p.useCard(sha)) {
                    // System.out.println("乱武-杀-触发成功:"+p);
                    // return false;不能直接这样返回
                    // sha.setTaken(true);
                }

                //sleep(1000);
            }

            hasLuanWu = true;
            return true;
        }
        return false;
    }

    @ForcesSkill("帷幕")
    @Override
    public boolean hasWeiMu() {
        // println(this + "try uses 帷幕");
        // System.out.println("try uses 帷幕:"+getActiveSkills());
        return isActiveSkill("帷幕");
    }

    @Override
    public boolean addJudgeCard(JudgeCard c) {
        if (isActiveSkill("帷幕") && c.isBlack()) {
            return false;
        }
        return super.addJudgeCard(c);
    }

    @Override
    public String name() {
        return "贾诩";
    }

    @Override
    public String skillsDescription() {
        return "完杀——锁定技，你的回合内，只有你和处于濒死状态的角色才能使用[桃]。\n" +
                "乱武——限定技，出牌阶段，你可以令所有其他角色除非对各自距离最小的另一名角色使用一张[杀]，否则受1点伤。\n" +
                "帷幕——锁定技，你不能成为黑色锦囊牌的目标。";
    }
}
