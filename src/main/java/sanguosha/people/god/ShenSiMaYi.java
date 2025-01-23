package sanguosha.people.god;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import config.Text;
import msg.MsgObj;
import sanguosha.cards.Card;
import sanguosha.people.Identity;
import sanguosha.people.Person;
import sanguosha.skills.AfterWakeSkill;
import sanguosha.skills.ForcesSkill;
import sanguosha.skills.Skill;
import sanguosha.skills.WakeUpSkill;

public class ShenSiMaYi extends God {
    private int renMark = 0;
    private boolean hasKilled = false;
    private int thisRound = -1;

    public ShenSiMaYi() {
        super(4, null);
    }

    @ForcesSkill("忍戒")
    @Override
    public void gotHurt(List<Card> cards, Person p, int num) {
        // printlnPriv(this + " uses 忍戒");
        showNotice(num);
        // printlnPriv(this + " now has " + renMark + " 忍mark");
        fangZhu();
    }

    @Override
    public int throwPhase(boolean fastMode) {
        int num = super.throwPhase(fastMode);
        showNotice(num);
        return num;
    }

    private void showNotice(int num) {
        if (num <= 0) {
            return;
        }
        renMark += num;
        boolean showImg = false;
        if (getGameManager().getPersonRound() > thisRound) {
            thisRound = getGameManager().getPersonRound();
            showImg = true;
        }
        String res = Text.format("%s+%s:<i>忍一时风平浪静。</i>", getSkillHtmlName("忍戒"), num);
        // sleep(1000);
        getGameManager().getIo().printlnPublic(res, showImg ? toString() : null);

    }

    @WakeUpSkill("拜印")
    @Override
    public void selfBeginPhase() {
        if (!isWakenUp() && renMark >= 4) {
            MsgObj publicMsgObj = MsgObj.newMsgObj(getGameManager());
            if (launchSkillPublicDeepLink(
                    publicMsgObj,
                    "拜印",
                    Text.format("是否使用 觉醒技 %s",
                            getSkillHtmlName("拜印")),
                    "baiying1")) {
                setMaxHpNotSetCurrent(getMaxHP() - 1);
                wakeUp();
                setskipNoticeUsePublic(true);
                String res = Text.format("%s 获得技能 %s",
                        getPlateName(), getSkillHtmlName("极略"));
                publicMsgObj.text = res;
                // sleep(1000);
                getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
            }
        }
    }

    @AfterWakeSkill("极略")
    private boolean jiLue(String s) {
        String res = Text.format("你目前有%s枚“忍”,是否-1“忍”释放 %s",
                renMark, getSkillHtmlName(s));
        getPriviMsg().setOneTimeInfo1(res);
        if (isWakenUp() && renMark > 0 && launchSkillPriv(s + "(极略)")) {
            renMark--;
            getPriviMsg().setHeader2(getPlayerStatus(this, true));
            // printlnPriv(this + " now has " + renMark + " 忍mark");
            return true;
        }
        return false;
    }

    private boolean jiLuePublic(MsgObj publicMsgObj, String s) {
        String res = Text.format("你目前有%s枚“忍”,是否-1“忍”释放 %s",
                renMark, getSkillHtmlName(s));
        // getPriviMsg().setOneTimeInfo1(res);
        if (isWakenUp() && renMark > 0 && launchSkillPublic(
                publicMsgObj,
                s + "(极略)",
                res,
                "jilve_pub")) {
            renMark--;
            // printlnPriv(this + " now has " + renMark + " 忍mark");
            return true;
        }
        return false;
    }

    private boolean jiLueAutoTriger() {

        if (isWakenUp() && renMark > 0) {
            renMark--;
            // printlnPriv(this + " now has " + renMark + " 忍mark");
            return true;
        }
        return false;
    }

    @AfterWakeSkill("鬼才")
    @Override
    public Card changeJudge(Person target, Card d) {


        if (isWakenUp() && renMark > 0) {

            MsgObj publicMsgObj = MsgObj.newMsgObj(getGameManager());
            // 伤害和那些什么写在改判牌这里
            target.putTempActionMsgObj("changeJudge", publicMsgObj);
            String code = "smy_guicai";
            
            if (launchSkillPublicDeepLink(
                    publicMsgObj,
                    "鬼才(极略)",
                    Text.format("%s 的判定牌是 %s",
                            target.getPlateName(),
                            d.getHtmlNameWithColor()),
                    code)) {
                getPriviMsg().setOneTimeInfo1(Text.format("\n💬请出牌，代替 %s 的判定牌 %s",
                        target.getPlateName(),
                        d.getHtmlNameWithColor()));
                if (jiLueAutoTriger()) {
                    Card c = requestCard(null);
                    if (c != null) {

                        getGameManager().getCardsHeap().retrieve(c);
                        getGameManager().getCardsHeap().discard(d);

                        String result = Text.format(",改判 %s:<i>老夫，即是天命！</i>",

                                c.getHtmlNameWithColor());
                        publicMsgObj.text = publicMsgObj.text + result;

                        getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);

                        return c;
                    }
                }
            }
        }
        return null;

    }

    @AfterWakeSkill("放逐")
    public void fangZhu() {
        MsgObj publicMsg = MsgObj.newMsgObj(getGameManager());

        if (isWakenUp() && renMark > 0 && launchSkillPublicDeepLink(
                publicMsg,
                "放逐",
                Text.format("\n💬%s 是否用 %s",
                        getHtmlName(), getSkillHtmlName("放逐")),
                "fangzhu")) {
            if (jiLue("放逐")) {
                String info = Text.format("放逐:请选择一个玩家");
                getPriviMsg().setOneTimeInfo1(info);
                Person target = selectPlayer();
                if (target != null) {
                    int cardNum = getMaxHP() - getHP();
                    target.drawCards(cardNum);
                    target.turnover();
                    String res = Text.format(",%s 摸 %s张牌并翻面:<i>赦你死罪，你去吧！</i>",
                            target.getHtmlName(), cardNum);
                    publicMsg.appendText(res);
                    // sleep(1000);
                    getGameManager().getMsgAPI().editCaptionForce(publicMsg);
                }
            }
        }

    }

    @AfterWakeSkill("急智")
    @Override
    public void useStrategy() {
        if (jiLue("急智")) {
            drawCard();

            String result = Text.format("%s:<i>顺应天意，得道多助。</i>",
                    getSkillHtmlName("急智"));
            // sleep(1000);
            getGameManager().getIo().printlnPublic(result, toString());
        }
    }

    @Override
    public void usePhaseBefore() {

        if (isWakenUp() && renMark > 0 && getCards().size() > 0 && hasNotUsedSkill1()) {
            getSkillCards().add("制衡(极略)");
        }
    }

    @AfterWakeSkill("制衡")
    @Override
    public boolean useSkillInUsePhase(int orderInt) {
        if (isWakenUp() && renMark > 0 &&
                orderInt < getSkillCards().size() &&
                getSkillCards().get(orderInt).equals("制衡(极略)")
                && hasNotUsedSkill1()) {
            if (jiLue("制衡")) {
                // printlnPriv(this + " uses 制衡");
                getPriviMsg().setOneTimeInfo1("制衡：出牌阶段限一次，你可以弃置任意张牌，然后摸等量的牌。");
                ArrayList<Card> cs = chooseManyCards(0, getCardsAndEquipments());
                if (!cs.isEmpty()) {
                    loseCard(cs);
                    drawCards(cs.size());

                    String res = Text.format("%s %s:<i>天之道，轮回也。</i>", getPlateName(), getSkillHtmlName("制衡"));
                    // sleep(1000);
                    getGameManager().getIo().printlnPublic(res, toString());
                    // sleep(3000);
                }
                setHasUsedSkill1(true);
                return true;
            }
        }
        return false;
    }

    @AfterWakeSkill("完杀")
    @Override
    public boolean hasWanSha() {
        MsgObj publicMsgObj = MsgObj.newMsgObj(getGameManager());
        if (jiLuePublic(publicMsgObj, "完杀")) {
            try {
                return true;
            } finally {
                String res = Text.format("%s %s:<i>叫破喉咙，没有人会来救你的</i>",
                        getPlateName(), getSkillHtmlName("完杀"));
                publicMsgObj.text = res;
                getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
            }
        }
        return false;
    }

    @Override
    public void killOther() {
        hasKilled = true;
    }

    @Skill("连破") // 不一定要是本人的回合
    @Override
    public void otherPersonEndPhase(Person thatPerson, boolean fastMode) {
        if (!isZuoCi()) {
            setskipNoticeUsePublic(false);
        }
        if (hasKilled) {
            hasKilled = false;
            String res = Text.format("%s:<i>受命于天，既寿永昌！</i>", getSkillHtmlName("连破"));
            // sleep(1000);
            getGameManager().getIo().printlnPublic(res, toString());

            run(true);
        }

    }

    @Override
    public Set<String> getActiveSkills() {

        Set<String> skills = super.getActiveSkills();

        if (isWakenUp()) {
            skills.add("极略");
        }
        return skills;
    }

    @Override
    public String getExtraInfo() {
        return "忍戒×" + renMark;
    }

    @Override
    public String name() {
        return "神司马懿";
    }

    @Override
    public String skillsDescription() {
        return "忍戒：锁定技，当你受到伤害后，或于弃牌阶段内弃置手牌后，你获得X枚“忍”标记（X为伤害值或弃置的手牌数）。\n" +
                "连破：当你杀死一名角色后，你可于此回合结束后获得一个额外回合。\n" +
                "拜印：觉醒技，准备阶段开始时，若“忍”标记的数量不小于4，你减1点体力上限，然后获得“极略”。\n" +
                (isWakenUp() ? "极略：你可以弃置1枚“忍”标记，发动下列一项技能：“鬼才”、“放逐”、“集智”、“制衡”或“完杀”。\n" +
                        "鬼才：当一名角色的判定牌生效前，你可以出手牌代替之。\n" +
                        "放逐：当你受到伤害后，你可以令一名其他角色翻面，然后该角色摸X张牌（X为你已损失的体力值）。\n" +
                        "集智：当你使用普通锦囊牌时，你可以摸一张牌。\n" +
                        "制衡：出牌阶段限一次，你可以弃置任意张牌，然后摸等量的牌。\n" +
                        "完杀：锁定技，你的回合内，只有你和处于濒死状态的角色才能使用[桃]。" : "");
    }
}
