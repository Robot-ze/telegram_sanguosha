package sanguosha.people.mountain;

import sanguosha.cards.Card;
import sanguosha.cards.basic.Sha;

import sanguosha.people.Identity;
import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.AfterWakeSkill;
import sanguosha.skills.InjectSkill;
import sanguosha.skills.KingSkill;
import sanguosha.skills.Skill;
import sanguosha.skills.WakeUpSkill;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import config.Text;
import msg.MsgObj;;

public class SunCe extends Person {

    public SunCe() {
        super(4, Nation.WU);
        // super(1, Nation.WU);//test
    }

    @Skill("激昂")
    public void jiAng(Card c) {
        // 自动触发

        if (c instanceof Sha && c.isRed()) {

            drawCard();
            String res = Text.format("%s:%s 摸一张牌:<i>吾乃江东小霸王孙伯符！</i>", getSkillHtmlName("激昂"), getPlateName());
            //sleep(1000);
            getGameManager().getIo().printlnPublic(res, toString());
        }
    }

    @Override
    public void shaBegin(Card sha) {
        jiAng(sha);
    }

    @Override
    public void gotShaBegin(Sha sha) {
        jiAng(sha);
    }

    @Override
    public void jueDouBegin() {

        drawCard();
        String res = Text.format("%s:%s 摸一张牌:<i>吾乃江东小霸王孙伯符！</i>", getSkillHtmlName("激昂"), getPlateName());
        //sleep(1000);
        getGameManager().getIo().printlnPublic(res, toString());

    }

    // @Skill("鹰扬")
    // @Override
    // public String usesYingYang() {
    // if (launchSkill("鹰扬")) {
    // String[] values=new String[]{"+3", "-3"};
    // int option = chooseFromProvided(true,"+3", "-3");
    // if (option >=0) {
    // return values[option];
    // }
    // }
    // return "";
    // }

    @WakeUpSkill("魂姿")
    public void hunZi() {
        setMaxHpNotSetCurrent(getMaxHP() - 1);
        wakeUp();
        addActiveSkill("英姿", "英魂");
    }

    private Map<Person, InjectSkill> injectSkillInstMap = new HashMap<>();

    @KingSkill("制霸")
    @Override
    public void otherPersonUsePhaseBefore(Person thatPerson) {
        if (thatPerson == this) {// 自己就不插了
            return;
        }
        if (getIdentity() != Identity.KING) {// 不是主公没这技能
            return;
        }
        if (thatPerson.getNation() != Nation.WU) {// 不是吴势力角色
            return;
        }
        InjectSkill skill = injectSkillInstMap.get(thatPerson);
        Person personThis = this;
        if (skill == null) {
            skill = new InjectSkill("制霸(" + toString() + ")") {

                @Override
                public boolean use(Person target) {
                    target.getPriviMsg().clearHeader2();
                    target.getPriviMsg().setOneTimeInfo1(
                            Text.format("\n💬是否用 %s:主公技，你与 %s 拼点，若你输，他可以获得拼点的两张牌",
                                    personThis.getSkillHtmlName("制霸"),
                                    personThis.getHtmlName()));
                    if (!target.getCards().isEmpty() && !getCards().isEmpty()) {

                        if (personThis.isWakenUp() && personThis.launchSkillPublic(

                                "拒绝",
                                Text.format("%s:%s 要求拼点，%s 已觉醒,可拒绝拼点",
                                        personThis.getSkillHtmlName("制霸"),
                                        target.getPlateName(),
                                        personThis.getHtmlName()),
                                "jujuezhiba", false)) {
                            return false;
                        }
                        getGameManager().pinDian(target, personThis);
                        setLaunched();
                        return true;
                    }

                    return false;
                }

            };
            injectSkillInstMap.put(thatPerson, skill);
        }

        if (!skill.isLaunched()) {
            thatPerson.getInjectSkills().add(skill);
        }
    }

    // 清技能
    @Override
    public void otherPersonEndPhase(Person thatPerson, boolean fastMode) {
        InjectSkill skill = injectSkillInstMap.get(thatPerson);
        if (skill != null) {
            skill.setNotLaunched();
        }
    }

    @Override
    public boolean usesZhiBa() {

        if (getIdentity() == Identity.KING) {

            try {
                return true;
            } finally {
                MsgObj publicMsgObj = getTempActionMsgObj("pindian");
                String res = Text.format(",%s 获得2张拼点牌:<i>全仗众将军了。请！</i>", getPlateName());
                publicMsgObj.appendText(res);
                //sleep(1000);
                getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
            }
        } else {
            try {
                return false;
            } finally {
                MsgObj publicMsgObj = getTempActionMsgObj("pindian");
                String res = Text.format(",%s 没有获得拼点牌:<i>汝等虚情假意罢了</i>", getPlateName());
                publicMsgObj.appendText(res);
                //sleep(1000);
                getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
            }
        }

    }

    @Override
    public void selfBeginPhase() {

        if (!isWakenUp() && getHP() == 1) {
            MsgObj publicMsgObj = MsgObj.newMsgObj(getGameManager());
            if (launchSkillPublicDeepLink(
                    publicMsgObj,
                    "魂姿",
                    Text.format("%s 是否使用 魂姿：觉醒技", getHtmlName()),
                    "hunzi1")) {
                // println(this + " uses 若愚");
                hunZi();
                addActiveSkill("激将");
                setskipNoticeUsePublic(true);

                String res = Text.format(",减1点体力上限，获得\"英姿\"和\"英魂\":<i>父亲在上，魂佑江东；公瑾在旁，智定天下！</i>");
                publicMsgObj.appendText(res);
                //sleep(1000);
                getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);

            }

        }
        if (isWakenUp()) {

            yingHun();
        }
    }

    @Override
    public void selfEndPhase(boolean fastMode) {
        if (!isZuoCi()) {
            setskipNoticeUsePublic(false);
        }
    }

    @AfterWakeSkill("英姿")
    @Override
    public void drawPhase(boolean fastMode) {
        getPriviMsg().clearHeader2();
        getPriviMsg().setOneTimeInfo1(Text.format("\n💬是否用 英姿：锁定技，摸牌阶段，你多摸一张牌；你的手牌上限等于X（X为你的体力上限）。\n"));
        if (! fastMode&&isWakenUp() && launchSkillPriv("英姿")) {
            drawCards(3);
        } else {
            super.drawPhase(  fastMode);
        }
    }

    @AfterWakeSkill("英魂")
    public void yingHun() {

        if (getHP() < getMaxHP()) {
            // if (true) {// test
            int x = getMaxHP() - getHP();
            MsgObj publicMsgObj = MsgObj.newMsgObj(getGameManager());
            if (launchSkillPublicDeepLink(
                    publicMsgObj,
                    "英魂",
                    Text.format("%s 是否用 %s,:你现在可以选择一名其他角色：\n" +
                            "1.令其摸%s张牌，弃1张牌\n2.令其摸1张牌，弃%s张牌",
                            getHtmlName(), getSkillHtmlName("英魂"), x, x),
                    "yinghun1")) {

                setskipNoticeUsePublic(true);
                String op1 = "摸" + x + "张牌,扔1张牌";
                String op2 = "摸1张牌,扔" + x + "张牌";

                getPriviMsg().clearHeader2();
                getPriviMsg().setOneTimeInfo1(Text.format("英魂:你现在可以选择一名其他角色：\n" +
                        "1.令其摸%s张牌，弃1张牌；\n2.令其摸1张牌，弃%s张牌", x, x));

                Person p = selectPlayer(false);
                if (p != null) {
                    getPriviMsg().setOneTimeInfo1(Text.format(
                            "英魂:\n1.令其摸%s张牌，然后弃1张牌\n2.令其摸1张牌，然后弃%s张牌", x, x));
                    if (chooseNoNull(op1, op2) == 1) {
                        p.drawCards(x);

                        MsgObj sonPublicMsg = MsgObj.newMsgObj(getGameManager());
                        boolean sonActive = p.launchSkillPublicDeepLink(
                                sonPublicMsg,
                                op1,
                                Text.format(
                                        "%s 你现在必须摸%s张牌，然后弃1张牌", p.getHtmlName(), x),
                                "yinghun2", false);
                        Card c;
                        List<Card> allCard = p.getCardsAndEquipments();
                        if (sonActive) {// 可以选装备牌
                            c = p.chooseCard(allCard);

                        } else {// 强制丢最后一张手牌
                            c = allCard.get(allCard.size() - 1);
                        }

                        p.loseCard(c);
                        String res = Text.format(
                                ",%s 谨遵遗志!<i>列祖列宗，赐我力量，保我大吴！</i>", p.getPlateName());
                        sonPublicMsg.appendText(res);
                        getGameManager().getIo().delaySendAndDelete(this, p.getPlateName() + " 已照办");
                        //sleep(1000);
                        getGameManager().getMsgAPI().editCaptionForce(sonPublicMsg);
                    } else {
                        p.drawCard();
                        MsgObj sonPublicMsg = MsgObj.newMsgObj(getGameManager());
                        List<Card> allCard = p.getCardsAndEquipments();
                        int num = allCard.size();
                        if (num <= x) {
                            p.loseCard(allCard);
                            sonPublicMsg.text = Text.format(
                                    "%s 你现在必须摸1张牌，然后弃%s张牌", p.getHtmlName(), x);
                            sonPublicMsg.chatId = getGameManager().getChatId();
                            //sleep(1000);
                            getGameManager().getMsgAPI().sendMsg(sonPublicMsg);
                        } else {
                            boolean sonActive = p.launchSkillPublicDeepLink(
                                    sonPublicMsg,
                                    op2,
                                    Text.format(
                                            "%s 你现在必须摸1张牌，然后弃%s张牌", p.getHtmlName(), x),
                                    "yinghun2", false);

                            List<Card> cs;
                            if (sonActive) {// 可以选装备牌
                                cs = p.chooseManyFromProvided(x, allCard);
                            } else {// 强制丢最后x张手牌
                                cs = allCard.subList(allCard.size() - x, allCard.size() - 1);
                            }
                            p.loseCard(cs);
                        }

                        String res = Text.format(
                                ",%s 冤魂不散!<i>不诛此贼三族！则吾死不瞑目！</i>", p.getPlateName());
                        sonPublicMsg.appendText(res);
                        getGameManager().getIo().delaySendAndDelete(this, p.getPlateName() + " 已照办");
                        //sleep(1000);
                        getGameManager().getMsgAPI().editCaptionForce(sonPublicMsg);
                    }
                }
            }
        }

    }

    @Override
    public Set<String> getInitialSkills() {
        Set<String> skills = super.getInitialSkills();

        if (getIdentity() != Identity.KING) {
            skills.remove("制霸");
        }
        return skills;
    }

    @Override
    public String name() {
        return "孙策";
    }

    @Override
    public String skillsDescription() {
        return "激昂：当你使用[决斗]或红色[杀]指定目标后，或成为[决斗]或红色[杀]的目标后，你可以摸一张牌。\n" +
                "魂姿：觉醒技，准备阶段，若你的体力值为1，你减1点体力上限，然后获得\"英姿\"和\"英魂\"。\n" +
                "制霸：主公技，其他吴势力角色的出牌阶段限一次，该角色可以与你拼点（若你已觉醒，你可以拒绝此拼点），若其没赢，你可以获得拼点的两张牌。\n" +
                (isWakenUp() ? "\n英姿：锁定技，摸牌阶段，你多摸一张牌；你的手牌上限等于X（X为你的体力上限）。" +
                        "英魂：准备阶段，若你已受伤，你可以选择一名其他角色并选择一项：" +
                        "1.令其摸X张牌，然后弃置一张牌；2.令其摸一张牌，然后弃置X张牌。（X为你已损失的体力值）。" : "");
    }
}
