package sanguosha.people.qun;

import sanguosha.cards.Card;
import sanguosha.cards.Color;
import sanguosha.cards.basic.Shan;
import sanguosha.cards.strategy.judgecards.ShanDian;
import sanguosha.people.Identity;
import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.people.PlayerIO;
import sanguosha.skills.InjectSkill;
import sanguosha.skills.Skill;
import sanguosha.skills.SpecialSkill;
import components.TimeLimit;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import config.Config;
import config.Text;
import msg.CallbackEven;
import msg.MsgObj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class YuanShu extends Person {
    private Person king;
    private Map<Person, InjectSkill> injectSkillInstMap = new HashMap<>();

    public YuanShu() {
        super(4, Nation.QUN);
        // super(1, Nation.QUN);
    }

    @Override
    public void initialize(Identity identity, int uerPos) {
        // Thread.dumpStack();
        // System.out.println("getIdentity()="+getIdentity());
        // System.out.println("getGameManager().getKing()="+getGameManager().getKing());
        try {// 这里本不该这样写，没办法，偷懒
            king = getIdentity() == Identity.KING ? null : getGameManager().getKing();
        } catch (Exception e) {
            king = null;
        }

    }

    @Skill("伪帝")
    public boolean weiDi(String s) {
        if (!isActiveSkill("伪帝")) {
            return false;
        }
        // System.out.println("s="+s);
        // System.out.println("king.getKingSkill()="+king.getKingSkill());
        return king != null && (king.getKingSkill().equals(s) || s.equals("激将") && isWakenUp());
    }

    @Skill("庸肆")
    @Override
    public void drawPhase(boolean fastMode) {
        // printlnPriv(this + " uses 庸肆");
        int numNations = 0;
        Nation[] nations = { Nation.WEI, Nation.SHU, Nation.WU, Nation.QUN };
        for (Nation nation : nations) {
            if (!getGameManager().peoplefromNation(nation).isEmpty()) {
                numNations++;
            }
        }
        drawCards(2 + numNations);

        String res = Text.format("多摸%s张牌:<i>大汉天下，已半入我手！</i>",

                numNations + "");

        getGameManager().getIo().printlnPublic(res, toString());
        // sleep(3000);
    }

    @Override
    public void usePhaseBefore() {

        // ArrayList<Person> shuPeople =
        // getGameManager().peoplefromNation(Nation.SHU);//这个不主动释放
        // shuPeople.remove(this);
        // if (isActiveSkill("激将") && shuPeople != null && shuPeople.size() > 0 &&
        // getShaCount() < getMaxShaCount()) {
        // getSkillCards().add("激将");
        // }
    }

    @Override
    public boolean useSkillInUsePhase(int orderInt) {
        // int orderInt = Integer.valueOf(order) - 1;

        // 激将不主动释放
        // if (orderInt < getSkillCards().size() &&
        // getSkillCards().get(orderInt).equals("激将")
        // && weiDi("激将")) {
        // // -------选人
        // Person target = selectPlayer(false);
        // if (target == null) {
        // return false;
        // }
        // Card sha = jiJiang();
        // if (sha == null) {
        // return false;
        // }

        // sha.setSource(this);
        // sha.setTarget(target);
        // return useCard(sha);
        // }

        return false;
    }

    @SpecialSkill("激将")
    public Card jiJiang() {
        // if(getShaCount()<=0){ //这几行不能加，加了被动的出杀时候也触发不了
        // return null;
        // }
        ArrayList<Person> shuPeople = getGameManager().peoplefromNation(Nation.SHU);
        shuPeople.remove(this);

        MsgObj publicMsgObj = MsgObj.newMsgObj(getGameManager());
        publicMsgObj.forPlayers = new ArrayList<>();
        AtomicReference<PlayerIO> showCardPerson = new AtomicReference<>(null);
        AtomicReference<Card> showCard = new AtomicReference<>(null);
        for (Person p : shuPeople) {
            if (p.getUser() != null && p.existsSha()) {// 不是AI
                publicMsgObj.forPlayers.add(p);
            }
        }
        if (publicMsgObj.forPlayers.isEmpty()) {

            String reult = Text.format("%s 激将:<i>众位蜀将可有人助我打出一杀</i>", this.getPlateName());
            publicMsgObj.text = reult;
            publicMsgObj.setImg(toString());
            publicMsgObj.chatId = getGameManager().getChatId();
            getGameManager().getMsgAPI().sendImg(publicMsgObj);

            reult = Text.format(",<i>环顾四周,已无蜀将可助一臂之力</i>");
            publicMsgObj.text = publicMsgObj.text + reult;
            getGameManager().getIo().delaySendAndDelete(this, reult);
            // sleep(1000);
            getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
            // sleep(3000);
            return null;
        }

        requestCardForPublic(publicMsgObj, showCardPerson, showCard, "杀",
                "帮TA出杀", "jijiang");

        // System.out.println("showCardPerson.get()=" + showCardPerson.get());
        if (showCardPerson.get() == null) {
            String reult = Text.format(",环顾四周,已无蜀将可助一臂之力");
            publicMsgObj.text = publicMsgObj.text + reult;
            publicMsgObj.replyMakup = null;
            getGameManager().getIo().delaySendAndDelete(this, reult);
            // sleep(1000);
            getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
            return null;
        } else {
            String reult = Text.format(",%s 已助一臂之力打出一杀");
            publicMsgObj.text = publicMsgObj.text + reult;
            publicMsgObj.replyMakup = null;
            getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
            return showCard.get();
        }

    }

    @Override
    public boolean skillSha(Person sourse) {
        if (weiDi("激将") && launchSkillPriv("激将")) {
            return jiJiang() != null;
        }
        return false;

    }

    @SpecialSkill("血裔")
    @Override
    public int throwPhase(boolean fastMode) {
        if (weiDi("血裔")) {
            int num = getCards().size() - getHP()
                    - 2 * getGameManager().peoplefromNation(Nation.QUN).size();
            if (num > 0) {

                ArrayList<Card> cs;
                if (fastMode) {// 快速模式直接丢掉后面的牌
                    cs = new ArrayList<>();//
                    for (int i = 0; i < num; i++) {
                        cs.add(getCards().get(getCards().size() - 1 - i));
                    }
                } else {
                    getPriviMsg().setOneTimeInfo1(Text.format(",💬你需丢弃 %s 张牌", num));
                    cs = chooseManyFromProvided(num, getCards(), true);
                }

                if (cs.size() < num) {// 如果选不够，强制从前面丢起
                    for (Card c : getCards()) {
                        if (cs.indexOf(c) >= 0) {
                            continue;
                        }
                        cs.add(c);
                        if (cs.size() >= num) {
                            break;
                        }
                    }
                }

                loseCard(cs);
                for (Person p : getGameManager().getPlayersBeginFromPlayer(this)) {
                    p.otherPersonThrowPhase(this, cs);
                }
            }
            return num;
        } else {
            return super.throwPhase(fastMode);
        }
    }

    @SpecialSkill("护驾")
    @Override
    public boolean skillShan(Person sourse) {

        if (weiDi("护驾")) {
            getPriviMsg().setOneTimeInfo1("你可以令其他魏势力角色选择是否出“闪”");
            if (launchSkillPriv("护驾")) {
                return hujia() != null;
            }
        }
        return false;
    }

    private Card hujia() {

        // if(getShaCount()<=0){ //这几行不能加，加了被动的出杀时候也触发不了
        // return null;
        // }
        ArrayList<Person> weiPeople = getGameManager().peoplefromNation(Nation.WEI);
        weiPeople.remove(this);

        MsgObj publicMsgObj = MsgObj.newMsgObj(getGameManager());
        publicMsgObj.forPlayers = new ArrayList<>();
        AtomicReference<PlayerIO> showCardPerson = new AtomicReference<>(null);
        AtomicReference<Card> showCard = new AtomicReference<>(null);
        for (Person p : weiPeople) {
            if (p.getUser() != null && p.existsSha()) {// 不是AI
                publicMsgObj.forPlayers.add(p);
            }
        }
        if (publicMsgObj.forPlayers.isEmpty()) {

            String reult = Text.format("%s :<i>魏将何在？来人，护驾！</i>", this.getPlateName());
            publicMsgObj.text = reult;
            publicMsgObj.setImg(toString());
            publicMsgObj.chatId = getGameManager().getChatId();
            getGameManager().getMsgAPI().sendImg(publicMsgObj);
            // sleep(3000);
            reult = Text.format(",<i>环顾四野,已无魏将护驾</i>");
            publicMsgObj.text = publicMsgObj.text + reult;
            getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
            // sleep(3000);
            return null;
        }

        requestCardForPublic(publicMsgObj, showCardPerson, showCard, "闪",
                "帮TA出闪", "hujia1");

        // System.out.println("showCardPerson.get()=" + showCardPerson.get());
        if (showCardPerson.get() == null) {
            String reult = Text.format(",<i>环顾四野,已无魏将护驾</i>");
            publicMsgObj.text = publicMsgObj.text + reult;
            publicMsgObj.replyMakup = null;
            getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
            return null;
        } else {
            String reult = Text.format(",%s 已护驾打出一闪");
            publicMsgObj.text = publicMsgObj.text + reult;
            publicMsgObj.replyMakup = null;
            getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
            return showCard.get();
        }
    }

    @SpecialSkill("救援")
    @Override
    public int gotSavedBy(Person p) {
        if (weiDi("救援") && p.getNation() == Nation.WU) {
            // if ( p.getNation() == Nation.WU) {
            // printlnPriv(this + " uses 救援");
            recover(null, 1);
            String res = Text.format("%s %s:<i>有汝辅佐，甚好！</i>", getPlateName(), getSkillHtmlName("救援"));

            getGameManager().getIo().printlnPublic(res, toString());
            // sleep(3000);
            return 1;
        }
        return 0;
    }

    @SpecialSkill("黄天")
    public void huangTian(Person thatPerson) {
        if (thatPerson == this) {// 自己就不插了
            return;
        }

        if (thatPerson.getNation() != Nation.QUN) {// 不是群势力角色
            return;
        }
        InjectSkill skill = injectSkillInstMap.get(thatPerson);
        Person personThis = this;
        if (skill == null) {
            skill = new InjectSkill("黄天(" + toString() + ")") {

                @Override
                public boolean use(Person target) {
                    target.getPriviMsg().clearHeader2();
                    target.getPriviMsg().setOneTimeInfo1(
                            Text.format("\n💬是否用 黄天:主公技，群势力角色的出牌阶段限一次，可以将一张[闪]或[闪电]交给%s", personThis.getHtmlName()));
                    Card c = null;
                    TimeLimit t = new TimeLimit(Config.PRIV_RND_TIME_60S);
                    while (getGameManager().isRunning() &&
                            ((c = target.requestCard(null)) != null && !(c instanceof Shan || c instanceof ShanDian))) {
                        if (c != null) {// 牌不对得加回去给别人
                            getGameManager().getCardsHeap().retrieve(c);
                            target.addCard(c);
                        }
                        if (t.isTimeout()) {
                            return false;
                        }
                        target.getPriviMsg().setOneTimeInfo1(Text.format("\n💬所选卡牌种类不匹配，你可以将一张[闪]或[闪电]交给他 ", this));
                    }

                    if (c != null) {
                        getGameManager().getCardsHeap().retrieve(c);
                        c.setTaken(true);
                        addCard(c);
                        System.out.println("getCards()=" + getCards());
                        setLaunched();
                        try {
                            return true;
                        } finally {
                            String res = Text.format("%s 将 一个 %s 献给 %s<i>归顺于我，封爵赏地。</i>",
                                    thatPerson.getPlateName(),
                                    c.getHtmlNameWithColor(), getHtmlName());
                            // sleep(1000);
                            getGameManager().getIo().printlnPublic(res, thatPerson.toString());
                        }
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

    @SpecialSkill("制霸")
    public void zhiBa(Person thatPerson) {
        if (thatPerson == this) {// 自己就不插了
            return;
        }
        // if (getIdentity() != Identity.KING) {// 不是主公没这技能
        // return;
        // }
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
                                "jujuezhiba2", false)) {
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

    @Override
    public boolean usesZhiBa() {

        if (weiDi("制霸")) {
            try {
                return true;
            } finally {
                MsgObj publicMsgObj = getTempActionMsgObj("pindian");
                String res = Text.format(",%s 获得2张拼点牌:<i>全仗众英雄了。请！</i>", getPlateName());
                publicMsgObj.appendText(res);
                // sleep(1000);
                getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
            }
        } else {
            try {
                return false;
            } finally {
                MsgObj publicMsgObj = getTempActionMsgObj("pindian");
                String res = Text.format(",%s 没有获得拼点牌:<i>汝等虚情假意罢了</i>", getPlateName());
                publicMsgObj.appendText(res);
                // sleep(1000);
                getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
            }
        }

    }

    // 黄天技能
    // 制霸
    @Override
    public void otherPersonUsePhaseBefore(Person thatPerson) {
        System.out.println("thatPerson=" + thatPerson);
        if (weiDi("黄天")) {
            huangTian(thatPerson);
        }

        if (weiDi("制霸")) {
            zhiBa(thatPerson);
        }

        // zhiBa(p);
    }

    // 黄天,制霸清技能
    @Override
    public void otherPersonEndPhase(Person thatPerson, boolean fastMode) {
        InjectSkill skill = injectSkillInstMap.get(thatPerson);
        if (skill != null) {
            skill.setNotLaunched();
        }
    }

    @SpecialSkill("颂威")
    @Override
    public void otherPersonGetJudge(Person p, Card jugeCard) {
        if (weiDi("颂威") && jugeCard.isBlack()
                && p.getNation() == Nation.WEI) {
            MsgObj publicMsg = MsgObj.newMsgObj(getGameManager());
            String self = (p == this) ? "(你)" : "(不是你)";
            if (p.launchSkillPublic(
                    publicMsg,
                    "使用颂威",
                    Text.format("\n💬%s 你的黑色判定牌生效后，其可以令 %s %s摸一张牌",
                            p.getHtmlName(),
                            this.getPlateName(),
                            self, false),
                    "songwei")) {
                drawCard();
                String res = Text.format(",%s 摸一张牌:<i>玉玺在此，四海称臣！</i>",
                        getPlateName());
                publicMsg.appendText(res);
                // sleep(1000);
                getGameManager().getMsgAPI().editCaptionForce(publicMsg);
            } else {
                String res = Text.format(":<i>汝大胆！不纳贡吾必讨之</i>",
                        getPlateName());
                publicMsg.appendText(res);
                // sleep(1000);
                getGameManager().getMsgAPI().editCaptionForce(publicMsg);
            }

        }
    }

    @SpecialSkill("暴虐")
    @Override
    public void otherPersonMakeHurt(Person source, Person target) {
        if (weiDi("暴虐") && getHP() < getMaxHP() && source.getNation() == Nation.QUN) {
            String self = (source == this) ? "(你)" : "(不是你)";
            String res = Text.format("💬%s是否用 %s：主公技，当你造成伤害，可以进行判定，若结果为%s，%s %s回复1点体力。",
                    source.getHtmlName(), getSkillHtmlName("暴虐"),
                    Card.getColorEmoji(Color.SPADE), getPlateName(), self);

            MsgObj publicMsgObj0 = MsgObj.newMsgObj(getGameManager());
            if (source.launchSkillPublic(
                    publicMsgObj0,
                    "暴虐",
                    res,
                    "baonve1")) {
                Card c = getGameManager().getCardsHeap().judge(source, new CallbackEven() {
                    @Override
                    public boolean juge(Card card) {
                        return (card.color() == Color.SPADE);
                    }
                });
                if (c.color() == Color.SPADE) {
                    recover(null, 1);
                    String res2 = Text.format(",%s 判定成功,%s 回复1体力:<i>汝堪大用，哈哈哈哈哈哈！</i>",
                            c.getHtmlNameWithColor(),
                            getPlateName());
                    MsgObj publicMsgObj = getTempActionMsgObjFirstOrder(publicMsgObj0, "changeJudge");
                    publicMsgObj.appendText(res2);
                    // sleep(1000);
                    getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);

                } else {
                    String res2 = Text.format(",%s 判定失败：<i>可惜,哎，我是不是该增肥了？</i>",
                            c.getHtmlNameWithColor(),
                            getPlateName());
                    MsgObj publicMsgObj = getTempActionMsgObjFirstOrder(publicMsgObj0, "changeJudge");
                    publicMsgObj.appendText(res2);
                    // sleep(1000);
                    getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
                }
            } else {
                String res2 = Text.format(",放弃操作:<i>竖子，竟敢反我！</i>", getPlateName());
                MsgObj publicMsgObj = getTempActionMsgObjFirstOrder(publicMsgObj0, "changeJudge");
                publicMsgObj.appendText(res2);
                // sleep(1000);
                getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);

            }
        }
    }

    @SpecialSkill("若愚")
    @Override
    public void selfBeginPhase() {
        if (weiDi("若愚") && !isWakenUp()) {
            boolean isLowest = true;
            for (Person p : getGameManager().getPlayersBeginFromPlayer(this)) {
                if (p.getHP() < getHP()) {
                    isLowest = false;
                    break;
                }
            }
            if (isLowest) {
                // printlnPriv(this + " uses 若愚");
                setMaxHpNotSetCurrent(getMaxHP() + 1);
                recover(null, 1);
                wakeUp();
            }
        }
    }

    @Override
    public Set<String> getInitialSkills() {
        // Thread.dumpStack();
        // System.out.println("king="+king);
        Set<String> ans = super.getInitialSkills();
        if (king != null) {
            ans.add(king.getKingSkill());
        }
        // if (weiDi("激将")) {
        // ans.add("激将");
        // }
        return ans;
    }

    @Override
    public String name() {
        return "袁术";
    }

    @Override
    public String skillsDescription() {
        return "庸肆：锁定技，摸牌阶段，你多摸X张牌；锁定技，弃牌阶段开始时，你弃置X张牌（X为势力数）。\n" +
                "伪帝：锁定技，你拥有和主公相同的技能";
    }
}
