package sanguosha.people.qun;

import sanguosha.cards.Card;
import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.Skill;
import components.TimeLimit;
import java.util.List;
import java.util.ArrayList;

import config.Config;
import config.Text;
import msg.MsgObj;

public class DiaoChan extends Person {
    public DiaoChan() {
        super(3, "female", Nation.QUN);
        // super(1, "female", Nation.QUN);//forTest
    }

    @Skill("闭月")
    @Override
    public void selfEndPhase(boolean fastMode) {

        MsgObj publicOMsgObj = MsgObj.newMsgObj(getGameManager());
        if (launchSkillPublic(
            publicOMsgObj,
                "闭月",
                Text.format("%s %s：结束阶段，你可以摸一张牌",
                        getHtmlName(), getSkillHtmlName("闭月")),
                "biyue1", false)) {
            drawCard();
            String res = Text.format(",摸一张牌:<i>嗯哼哼~妾身美吗？</i>", getPlateName());
            publicOMsgObj.appendText(res);
            //sleep(1000);
            getGameManager().getMsgAPI().editCaptionForce(publicOMsgObj);
            // sleep(3000);
        }
    }

    @Override
    public void usePhaseBefore() {

        if (isActiveSkill("离间") && hasNotUsedSkill1()) {
            getSkillCards().add("离间");
        }
    }

    @Skill("离间")
    @Override
    public boolean useSkillInUsePhase(int orderInt) {

        // int orderInt = Integer.valueOf(order) - 1;

        if (orderInt < getSkillCards().size() && getSkillCards().get(orderInt).equals("离间") && hasNotUsedSkill1()) {
            Person p1 = null;
            Person p2 = null;

            String showText = "离间：出牌阶段限一次，你可以弃置一张牌，令一名男性角色视为对另一名男性角色使用一张[决斗]。";

            List<Person> men = new ArrayList<>();
            for (Person p : getGameManager().getPlayers()) {
                if (p.getSex().equals("female")) {

                    continue;
                }
                men.add(p);
            }
            if (men.size() <= 1) {
                getPriviMsg().setOneTimeInfo2("没有足够的男人");
                return false;
            }
            getPriviMsg().setOneTimeInfo1(showText);
            ArrayList<Person> plist = chooseManyFromProvidedByOrder(2, men, true);
            if (plist.size() <= 1) {
                getPriviMsg().setOneTimeInfo2("没有足够的男人");
                return false;
            }

            p1 = plist.get(0);
            p2 = plist.get(1);
            if (p1 == null || p2 == null) {
                return false;
            }

            if (p1 == null || p2 == null) {
                return false;
            }

            showText = "离间：请弃置一张牌";
            getPriviMsg().setOneTimeInfo1(showText);
            if (requestCard(null) == null) {
                return false;
            }

            String res = Text.format("%s 与 %s 陷入情斗:<i>夫君，你要替妾身做主啊~</i>",

                    // getPlateName(),
                    // getSkillHtmlName("离间"),
                    p1.getHtmlName(),
                    p2.getHtmlName());

            getGameManager().getIo().printlnPublic(res, toString());
            // sleep(3000);
            // 决斗
            p1.jueDouBegin();
            p2.jueDouBegin();

            TimeLimit t = new TimeLimit(Config.PRIV_RND_TIME_60S);
            while (getGameManager().isRunning() && t.isNotTimeout()) {
                if ((!p2.hasWuShuang() && p1.requestSha(p2) == null) ||
                        p2.hasWuShuang() && (p1.requestSha(p2) == null ||
                                p1.requestSha(p2) == null)) {
                    int realHurt = p1.hurt((Card) null, p2, 1);
                    MsgObj lastMsg = p1.getTempActionMsgObj("sha");
                    String result = Text.format(",败阵,受%s点伤害%s",
                            realHurt, p1.getHPEmoji());
                    lastMsg.text = lastMsg.text + result;
                    getGameManager().getMsgAPI().editCaptionForce(lastMsg);

                    break;
                }
                if ((!p1.hasWuShuang() && p2.requestSha(p1) == null) ||
                        p1.hasWuShuang() && (p2.requestSha(p1) == null ||
                                p2.requestSha(p1) == null)) {
                    int realHurt = p2.hurt((Card) null, p1, 1);
                    MsgObj lastMsg = p2.getTempActionMsgObj("sha");
                    String result = Text.format(",败阵,受%s点伤害%s",
                           realHurt, p2.getHPEmoji());
                    lastMsg.text = lastMsg.text + result;
                    getGameManager().getMsgAPI().editCaptionForce(lastMsg);

                    break;
                }
            }
            setHasUsedSkill1(true);
            return true;
        }
        return false;
    }

    @Override
    public String name() {
        return "貂蝉";
    }

    @Override
    public String skillsDescription() {
        return "离间：出牌阶段限一次，你可以弃置一张牌，令一名男性角色视为对另一名男性角色使用一张[决斗]。\n" +
                "闭月：结束阶段，你可以摸一张牌。";
    }
}
