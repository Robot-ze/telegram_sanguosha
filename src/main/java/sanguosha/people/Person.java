package sanguosha.people;

import static sanguosha.cards.EquipType.weapon;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import components.SanProgress;
import components.TextShowUnit;
import config.Config;
import config.DescUrl;
import config.Text;
import msg.CallbackEven;
import msg.MsgObj;
import msg.ReturnType;
import sanguosha.cards.Card;
import sanguosha.cards.Color;
import sanguosha.cards.EquipType;
import sanguosha.cards.Equipment;
import sanguosha.cards.JudgeCard;
import sanguosha.cards.Strategy;
import sanguosha.cards.basic.Jiu;
import sanguosha.cards.basic.Sha;
import sanguosha.cards.basic.Shan;
import sanguosha.cards.basic.Tao;
import sanguosha.cards.equipments.Shield;
import sanguosha.cards.equipments.Weapon;
import sanguosha.cards.equipments.horses.MinusOneHorse;
import sanguosha.cards.equipments.horses.PlusOneHorse;
import sanguosha.cards.equipments.weapons.FangTianHuaJi;
import sanguosha.cards.strategy.TieSuoLianHuan;
import sanguosha.cards.strategy.WuXieKeJi;
import sanguosha.manager.GameManager;
import sanguosha.manager.Status;
import sanguosha.manager.Utils;
import sanguosha.skills.AfterWakeSkill;
import sanguosha.skills.ForcesSkill;
import sanguosha.skills.KingSkill;
import sanguosha.skills.RestrictedSkill;
import sanguosha.skills.Skill;
import sanguosha.skills.WakeUpSkill;
import sanguosha.user.SanUser;

public abstract class Person extends Attributes implements Serializable {
    private boolean isZuoCi = false;
    private ArrayList<Person> huaShenList = new ArrayList<>();
    private Person activeHuaShen = null; // for zuoCi
    private Set<String> activeSkill = new HashSet<>(); // for zuoCi

    private GameManager gameManager;
    private SanUser user = null;

    private String logoImgFileId;
    private String bannerImgFileId;
    private MsgObj showStatusMsg = null;
    private SanProgress progress;
    /** 被 菜闻鸡 清了技能的人 */
    protected Person blankPerson = null;
    /**
     * 给一个初始的序号，只展示，对游戏实际排序无影响
     */
    private int gamePlayerNo;

    private boolean fastMode = false;

    /** 用来展示哪些是本回合新得到的牌 */

    public Person(int maxHP, String sex, Nation nation) {
        Utils.assertTrue(sex.equals("male") || sex.equals("female"), "invalid sex");

        this.initMaxHP(maxHP);
        this.setCurrentHP(maxHP);
        this.setSex(sex);
        this.setNation(nation);
        // 不能在这里加技能，在这加技能会忽略主公技
        // for(String skill :getSkills()){
        // addActiveSkill(skill);
        // }
    }

    public Person(int maxHP, Nation nation) {
        this(maxHP, "male", nation);
    }

    @Override
    public void setGameManager(GameManager gameManager) {
        // System.out.println(this+"调用GameManager");
        this.gameManager = gameManager;

    }

    @Override
    public GameManager getGameManager() {
        return this.gameManager;
    }

    public void run(boolean isNotHuashen) {
        gameManager.addPersonRound();
        fastMode = false;

        // System.out.println("----------" + this + "'s round begins" + "----------");
        if (checkDead()) {
            return;
        }
        setMyRound(true);

        updataAllpersonInfo();

        getPriviMsg().clearAll();

        getOldCards().clear();
        getOldCards().addAll(getCards());

        sleep(1000L);// 等一些之前的发布结束
        if (isTurnedOver()) {
            turnover();
            String res = Text.format("%s 已翻面,跳过此回合", getPlateName());
            // sleep(1000);
            getGameManager().getIo().printlnPublic(res, toString());
            setMyRound(false);
            return;
        }
        setHasUsedSkill1(false);

        selfBeginPhase();

        if (isNotHuashen && huaShen(true)) {
            return;
        }
        // System.out.println("----------" + this + "'s beginPhase()" + "----------");

        //// sleep(3000);

        if (checkDead()) {
            return;
        }
        Set<String> states = new HashSet<>();
        // System.out.println("----------" + this + "'s judgePhase()" + "----------");
        if (!skipJudge()) {
            states = judgePhase();
        }
        if (checkDead()) {
            return;
        }

        getPriviMsg().appendHeader1(this.getHtmlName() + " 现在是你的回合");

        // getPriviMsg().setImg(toString());//先不用设置图像
        // ---------------这里插入在公开群的点击连接过来---------
        if (this.isAI()) {
            fastMode = false;
            MsgObj puMsgObj = MsgObj.newMsgObj(getGameManager());
            puMsgObj.text = Text.format("%s 请出牌", getHtmlName());
            puMsgObj.chatId = gameManager.getChatId();
            puMsgObj.setImg(this.toString());
            gameManager.getMsgAPI().sendImg(puMsgObj);
            // gameManager.getMsgAPI().sendMsg(puMsgObj);
            sleep(3000);
            // gameManager.getMsgAPI().delMsg(puMsgObj);
            // sleep(3000);
        } else {
            if (!getSkipNoticeUsePublic()) {
                MsgObj noticeMsg = MsgObj.newMsgObj(gameManager);
                // 提醒两次
                // System.out.println( Text.format("%s 的回合,请点击按键出牌", getHtmlName()));
                fastMode = !(gameManager.getMsgAPI().noticeAndAskPublic(
                        noticeMsg,
                        this,
                        Text.format("%s 请出牌", getHtmlName()),
                        "点击出牌", "startRound", true));

                String callBackValue = (String) noticeMsg.getReturnAttr(ReturnType.Deeplink, 0);

                if (callBackValue == null) {// 如果用户还没有点击，删掉原再提醒一次
                    gameManager.getMsgAPI().delMsg(noticeMsg);

                    // sleep(1000);
                    // 再取一次
                    callBackValue = (String) noticeMsg.getReturnAttr(ReturnType.Deeplink, 0);
                    if (callBackValue == null) {
                        noticeMsg.isDeleted = false;
                        fastMode = !(gameManager.getMsgAPI().noticeAndAskPublic(
                                noticeMsg,
                                this,
                                Text.format("❗️请注意，%s 请点击按键出牌,如未出牌则跳过此回合", getHtmlName()),
                                "点击出牌", "startRound", true));

                    }
                }

                // 只删除按钮，让别人知道到谁出牌
                gameManager.getMsgAPI().clearButtons(noticeMsg);

            } else {
                fastMode = false;
            }

        }

        // System.out.println("fastMode="+fastMode);

        // System.out.println("----------" + this + "'s drawPhase()" + "----------");
        if (!states.contains("skip draw") && !skipDraw(fastMode)) {
            drawPhase(fastMode);
        }
        setShaCount(0);

        // System.out.println("----------" + this + "'s usePhase()" + "----------");
        if (!states.contains("skip use") && !skipUse(fastMode)) {
            // for (Person p2 : getGameManager().getPlayersBeginFromPlayer(this)) {
            // p2.otherPersonUsePhase(this);
            // }
            if (!fastMode) {// 快速模式就是不出牌
                usePhase(fastMode);
            }

        }
        if (checkDead() || blankPerson != null || getGameManager().status() == Status.end) {
            // 被菜闻鸡清了技能会进另一个循环
            return;
        }
        // System.out.println("----------" + this + "'s throwPhase()" + "----------");
        if (!skipThrow(fastMode)) {
            throwPhase(fastMode);
        }
        setDrunk(false);
        setDrunkShaUsed(false);
        setMyRound(false);
        // if (!fastMode) {// 快速模式不触发结尾摸牌
        endPhase(fastMode);
        // }
        huaShen(false);
        Utils.assertTrue(getHP() <= getMaxHP(), "currentHP exceeds maxHP");
        // println("----------" + this + "'s round ends" + "----------");
    }

    /**
     * 每一轮，轮到该人则更新一次全体信息
     */
    private void updataAllpersonInfo() {
        MsgObj showStatusMsg = getShowStatusMsg();
        if (showStatusMsg != null) {
            String newInfo = getAllPersonInfo();
            if (!newInfo.equals(showStatusMsg.text)) {
                showStatusMsg.text = newInfo;
                gameManager.getMsgAPI().editCaptionForce(showStatusMsg);
            }
        }
    }

    /**
     * 开始回合的操作
     */
    public void selfBeginPhase() {

    }

    public Set<String> judgePhase() {
        Utils.assertTrue(getJudgeCards().size() <= 3,
                "too many judgecards: " + getJudgeCards().size());
        Set<String> states = new HashSet<>();

        int size = getJudgeCards().size();
        for (int i = 0; i < getJudgeCards().size(); i++) {
            // println("judging " + jc);
            JudgeCard jc = getJudgeCards().get(i);
            String state = jc.use();
            if (jc.isNotTaken()) {
                getGameManager().getCardsHeap().discard(jc.getReplaceCards());
            } else {
                jc.setTaken(false);
            }
            if (state != null) {
                // printlnPriv(state);
                states.add(state);
            } else {
                getPriviMsg().setOneTimeInfo1("\n💬 " + jc.toString() + " 作用结束");
            }
            Utils.assertTrue(getJudgeCards().size() == size,
                    "原size: " + size + "不等于 后来的size:" + getJudgeCards().size());
        }
        getJudgeCards().clear();
        return states;
    }

    public void drawPhase(boolean fastMode) {
        // println(this + " draws 2 cards from cards heap");
        // getInText().append("\n"+this+" 从卡牌中抽了两张牌:");
        drawCards(2);
    }

    /**
     * 当前出牌阶段，parseOrder 也在这个方法里
     */
    public void usePhase(boolean fastMode) {
        // TimeLimit t = new TimeLimit(Config.PRIV_ROUND_TIME_120S);
        MsgObj privMsg;
        SanProgress progress = null;
        if (getUser() != null) {
            progress = SanProgress.newInst(gameManager.getMsgAPI(),
                    gameManager, getUser().user_id, Config.PRIV_ROUND_TIME_180S / 1000);
            setProgress(progress);
            progress.start(new CallbackEven() {
                @Override
                public boolean progressStopCount() {
                    if (!isStepInPriv()) {
                        return true;
                    }
                    return false;
                }

                @Override
                public void progressTimeOut() {
                    for (MsgObj ativingMsgObj : getActingPrivMsgs()) {

                        ativingMsgObj.unlockAllAttrbutes();

                        gameManager.getMsgAPI().delMsg(ativingMsgObj);
                    }
                    ;
                }
            });
            sleep(500);
        }

        // 总体120秒，30秒没有点击操作就丢牌
        while (getGameManager().status() != Status.end) {
            changeToBlank();
            if (blankPerson != null) {
                blankPersonRunOnce(fastMode);
                return;
            }
            try {
                if (!(progress.isAborted() || checkDead())) {
                    for (MsgObj privMsgOld : getActingPrivMsgs()) {
                        getGameManager().getMsgAPI().delMsg(privMsgOld);
                        privMsgOld.unlockAllAttrbutes();

                    }

                    // 如果有其他输出，就不显示状态
                    getPriviMsg().setHeader2(getPlayerStatus(this, true));
                    getPriviMsg().clearInfo1();
                    getPriviMsg().clearInfo2();

                    getInjectSkills().clear();
                    getSkillCards().clear();

                    usePhaseBefore();
                    forOtherPersonUsePhaseBefore();
                    if ( getCards().size() >= 2 && getShaCount() < getMaxShaCount()
                            && hasEquipment(EquipType.weapon, "丈八蛇矛")) {
                        getSkillCards().add("丈八蛇矛");
                    }
                    for (Card c : getCards()) {// 所有手牌都重置taken标记，特别是那些牵羊过来的
                        c.setTaken(false);
                    }

                    privMsg = MsgObj.newMsgObj(getGameManager());
                    stepInPriv(privMsg);
                    privMsg.chatId = getUser().user_id;
                    // MsgAPI io = getGameManager().getMsgAPI();

                    List<String> cardButtons = new ArrayList<>();
                    // int i=1;
                    for (int i = 0; i < getSkillCardsAndCards().size(); i++) {
                        // String cardShow="\n["+i+"]"+s;
                        // out+=cardShow;
                        String showText;
                        if (i < getInjectSkills().size() + getSkillCards().size()) {
                            showText = "⭐️ " + getSkillCardsAndCards().get(i);
                        } else {
                            showText = getSkillCardsAndCards().get(i);
                        }

                        cardButtons.add(showText);
                        // i++;
                    }
                    if (cardButtons.size() == 0) {
                        if (progress != null) {
                            progress.abort();
                        }
                        break;
                    }
                    getPriviMsg().setOneTimeInfo1("\n💬请选择一张卡或者一项技能");
                    privMsg.text = getPriviMsg().toString();
                    // ImgDB.setImg(inMsg, getPriviMsg().getShowImg());

                    MsgObj msg = getGameManager().getMsgAPI().chooseOneFromOpinionCanBeNull(cardButtons, privMsg);

                    // 总体90秒，30秒没有点击操作就丢牌
                    long nextTime = System.currentTimeMillis() + Config.PRIV_RND_TIME_60S;
                    int timeOut = (int) (nextTime - System.currentTimeMillis()) / 1000;
                    String order = msg.getString(ReturnType.ChooseOneCard, timeOut);
                    stepOutPriv(privMsg);
                    getGameManager().getMsgAPI().delMsg(msg);

                    // 不出牌和等于挂机
                    if (order == null) {
                        if (progress != null) {
                            progress.abort();
                        }
                        break;
                    }
                    if (order.equals("q")) {
                        if (progress != null) {
                            progress.abort();
                        }
                        break;
                    }
                    parseOrder(order);
                    getGameManager().clearPreMsg(5);
                } else {
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                // sleep(1000);
            }
        }

    }

    /**
     * 把上一次出牌后要变成白板的人变成白板
     */
    protected void changeToBlank() {
        List<Person> toBlankPersons = getGameManager().getChangeToBlankPersons();
        if (toBlankPersons.size() > 0) {

            for (Person source : toBlankPersons) {
                if (source.getUser() != null) {// 如果来源是AI就不换白板了，反正他本身也是白板
                    changePersonExec(source);
                }

            }

            toBlankPersons.clear();
        }
    }

    /**
     * 白板人过度性的运行，因为角色换了，但是原来的角色还在循环中，这里暂时执行一次，等下一次循环再更换为白板人
     */

    protected void changePersonExec(Person source) {
        BlankPerson blankPerson = new BlankPerson(source.getMaxHP());
        blankPerson.setName(source.name());
        blankPerson.setSex(source.getSex());
        blankPerson.setNation(source.getNation());
        blankPerson.setMyRound(source.isMyRound());
        blankPerson.setShowStatusMsg(source.getShowStatusMsg());
        // 这一段是之前别人写的
        source.changePersonAndPos(blankPerson);
        if (source == this) {// 如果是当前的人物，这要运行过渡循环
            source.setBlankPerson(blankPerson);
        }

    }

    protected void blankPersonRunOnce(boolean fastMode) {
        // 如果是他的回合，就会立即执行这一段
        // 这里还有一个问题，如果不是在他的回合，他杀了蔡文姬，就不会立即执行这一段
        blankPerson.getPriviMsg().appendHeader1(this.getHtmlName() + " 现在是你的回合");
        blankPerson.setProgress(getProgress());
        blankPerson.getProgress().setContrlCallBack(new CallbackEven() {
            @Override
            public boolean progressStopCount() {
                if (!blankPerson.isStepInPriv()) {
                    return true;
                }
                return false;
            }

            @Override
            public void progressTimeOut() {
                for (MsgObj ativingMsgObj : blankPerson.getActingPrivMsgs()) {
                    ativingMsgObj.unlockAllAttrbutes();
                    gameManager.getMsgAPI().delMsg(ativingMsgObj);
                }
                ;
            }
        });

        blankPerson.runUseThrowPhase();
        if (!blankPerson.skipThrow(fastMode)) {
            blankPerson.throwPhase(false);
        }
        blankPerson.setDrunk(false);
        blankPerson.setDrunkShaUsed(false);
        blankPerson.setMyRound(false);
        // if (!fastMode) {// 快速模式不触发结尾摸牌
        blankPerson.endPhase(false);
        // }
        // huaShen(false);
        Utils.assertTrue(blankPerson.getHP() <= blankPerson.getMaxHP(), "currentHP exceeds maxHP");
    }

    private void runUseThrowPhase() {
        // 总体120秒，30秒没有点击操作就丢牌
        getActingPrivMsgs().addAll(getActingPrivMsgs());
        MsgObj privMsg;
        while (getGameManager().status() != Status.end) {

            try {
                if (!(progress.isAborted() || checkDead())) {
                    for (MsgObj privMsgOld : getActingPrivMsgs()) {
                        getGameManager().getMsgAPI().delMsg(privMsgOld);
                        privMsgOld.unlockAllAttrbutes();

                    }

                    // 如果有其他输出，就不显示状态
                    getPriviMsg().setHeader2(getPlayerStatus(this, true));
                    getPriviMsg().clearInfo1();
                    getPriviMsg().clearInfo2();

                    getInjectSkills().clear();
                    getSkillCards().clear();

                    usePhaseBefore();
                    forOtherPersonUsePhaseBefore();
                    if ( getCards().size() >= 2
                            && getShaCount() < getMaxShaCount()
                            && hasEquipment(EquipType.weapon, "丈八蛇矛")) {
                        getSkillCards().add("丈八蛇矛");
                    }
                    for (Card c : getCards()) {// 所有手牌都重置taken标记，特别是那些牵羊过来的
                        c.setTaken(false);
                    }

                    privMsg = MsgObj.newMsgObj(getGameManager());
                    stepInPriv(privMsg);
                    privMsg.chatId = getUser().user_id;
                    // MsgAPI io = getGameManager().getMsgAPI();

                    List<String> cardButtons = new ArrayList<>();
                    // int i=1;
                    for (int i = 0; i < getSkillCardsAndCards().size(); i++) {
                        // String cardShow="\n["+i+"]"+s;
                        // out+=cardShow;
                        String showText;
                        if (i < getInjectSkills().size() + getSkillCards().size()) {
                            showText = "⭐️ " + getSkillCardsAndCards().get(i);
                        } else {
                            showText = getSkillCardsAndCards().get(i);
                        }

                        cardButtons.add(showText);
                        // i++;
                    }
                    if (cardButtons.size() == 0) {
                        if (progress != null) {
                            progress.abort();
                        }
                        break;
                    }
                    getPriviMsg().setOneTimeInfo1("\n💬请选择一张卡或者一项技能");
                    privMsg.text = getPriviMsg().toString();
                    // ImgDB.setImg(inMsg, getPriviMsg().getShowImg());

                    MsgObj msg = getGameManager().getMsgAPI().chooseOneFromOpinionCanBeNull(cardButtons,
                            privMsg);

                    // 总体90秒，30秒没有点击操作就丢牌
                    long nextTime = System.currentTimeMillis() + Config.PRIV_RND_TIME_60S;
                    int timeOut = (int) (nextTime - System.currentTimeMillis()) / 1000;
                    String order = msg.getString(ReturnType.ChooseOneCard, timeOut);
                    stepOutPriv(privMsg);
                    getGameManager().getMsgAPI().delMsg(msg);

                    // 不出牌和等于挂机
                    if (order == null) {
                        if (progress != null) {
                            progress.abort();
                        }
                        break;
                    }
                    if (order.equals("q")) {
                        if (progress != null) {
                            progress.abort();
                        }
                        break;
                    }
                    parseOrder(order);
                    getGameManager().clearPreMsg(5);
                } else {
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                // sleep(1000);
            }
        }
    }

    /**
     * 这个其实只调用一次，只是一个过渡变量
     * 
     * @return
     */
    public Person getBlankPerson() {
        return blankPerson;
    }

    /**
     * 因别的人作用，可能在这里插技能
     */
    private void forOtherPersonUsePhaseBefore() {
        for (Person p : gameManager.getPlayersBeginFromPlayer(this)) {
            // System.out.println("p+"+p);
            p.otherPersonUsePhaseBefore(this);
        }

    }

    /**
     * 实时更新 给各自角色实现的出牌释放的技能和武器技能
     */
    public void usePhaseBefore() {

    }

    /**
     * 因别的人作用，可能在这里插技能
     * 
     * @param thatPerson
     */
    public void otherPersonUsePhaseBefore(Person thatPerson) {

    }

    /**
     * 限制出杀
     * 
     * @param card
     * @return
     */
    public boolean checkSha() {
        return checkSha(null, false);
    }

    /**
     * 限制出杀
     * 
     * @param card
     * @return
     */
    public boolean checkSha(Card card, boolean print) {

        if (isMyRound()) {
            if (getShaCount() < getMaxShaCount() || hasEquipment(weapon, "诸葛连弩")) {

            } else {
                if (print) {
                    getGameManager().getIo().delaySendAndDelete(this, "\n💬你不能再使用 杀");
                }

                // getPriviMsg().setOneTimeInfo1("\n💬你不能再使用 杀");
                return false;
            }
        }

        return true;
    }

    /**
     * 设置多目标杀
     * 
     * @param card
     */
    public void setShaMulti(Card card) {
        // if (getCards().size() == 1 && hasEquipment(weapon, "方天画戟")) {
        if (hasEquipment(weapon, "方天画戟")) {
            ((FangTianHuaJi) getEquipments().get(weapon)).setsha(card);
            getEquipments().get(weapon).setSource(this);
            Weapon w= (Weapon) (getEquipments().get(weapon));
            w.use(card);
        }
    }

    public void putOnEquipment(Card card) {
        if (this.getEquipments().get(((Equipment) card).getEquipType()) != null) {
            loseCard(getEquipments().get(((Equipment) card).getEquipType()));
        }
        // println(this + " puts on equipment " + card);
        String ride = card instanceof MinusOneHorse ? "-1" : null;
        ride = card instanceof PlusOneHorse ? "+1" : ride;
        getGameManager().getIo().printlnPublic(
                Text.format(
                        ride == null ? "%s 装备 %s" : "%s 装备 %s",
                        getPlateName(),
                        ride == null ? card.getHtmlNameWithColor() : card.getHtmlNameWithColor() + ride));
        // card.toString());

        getCards().remove(card);
        card.setTaken(true);
        card.setSource(this);
        this.getEquipments().put(((Equipment) card).getEquipType(), (Equipment) card);
    }

    public boolean useCard(Card card) {
        // if (card instanceof Sha) { //这个逻辑已经调到了后面
        // if (!useSha(card)) {
        // return false;
        // }
        // } else
        if ((card instanceof Tao && getHP() == getMaxHP()) || card instanceof Shan ||
                card instanceof WuXieKeJi || (card instanceof Jiu && (isDrunk() || isDrunkShaUsed()))) {
            getPriviMsg().setOneTimeInfo1("\n💬你目前还不能使用 " + card);
            return false;
        } else if (card instanceof Equipment) {
            putOnEquipment(card);
            if (!checkDead()) {
                lostHandCardAction();
            }
            return true;
        } else if (card instanceof JudgeCard) {// 判定牌也是锦囊
            try {
                if (card.addJudgeCard(card.getTarget())) {
                    getCards().remove(card);
                    // getGameManager().getIo().showUsingCard(card);
                    card.setTaken(true);

                    if (!checkDead()) {
                        lostHandCardAction();
                    }
                    return true;
                }
                return false;
            } finally {
                useStrategy();
            }

        } else if (card instanceof Strategy) {
            useStrategy();
            throwCard(card);
            card.setTaken(true);// 为什么策略的要把这个卡设成taken
        }

        if (!checkDead()) {
            // getGameManager().getIo().showUsingCard(card);
            card.use();
        }
        return true;
    }

    /**
     * 出牌，这里有个问题，就是这个order有时是数字，有时又要求是汉字，入参的数字是从1开始的
     */
    public boolean parseOrder(String order1) {
        try {

            Card card;
            int orderInt = Integer.valueOf(order1) - 1;

            if (orderInt < 0) {
                return false;
            }

            if (orderInt < getInjectSkills().size()) {// 使用注入技能
                if (useInjectSkill(orderInt)) {
                    return true;
                } else {
                    return false;
                }

            } else if (orderInt < getInjectSkills().size() + getSkillCards().size()) {// 使用自身技能
                int pos = orderInt - getInjectSkills().size();
                if (useSkillInUsePhase(pos)) {
                    return true;
                } else if (  getSkillCards().get(pos).equals("丈八蛇矛")
                        && hasEquipment(weapon, "丈八蛇矛")
                        && getCards().size() >= 2
                        && getShaCount() < getMaxShaCount()) {

                    getEquipments().get(weapon).setSource(this);
                    card = (Sha) getEquipments().get(weapon).use();
                    if (card == null) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {// 使用卡
                int pos = orderInt - getSkillCards().size() - getInjectSkills().size();
                if (pos < 0) {
                    return false;
                }
                card = getCards().get(pos);
            }

            if (card instanceof TieSuoLianHuan) {// 于吉的蛊惑不能重铸
                getPriviMsg().clearHeader2();
                getPriviMsg().setOneTimeInfo1(Text.format("\n💬你可以丢弃 铁索连环 获得一张新牌"));
                if (chooseNoNull("重铸", "使用") == 1) {
                    loseCard(card);
                    drawCard();
                    // printlnPriv(this + " 重铸 铁索连环");
                    return true;
                }
            }

            if (card instanceof Sha) {
                card.setMultiSha(1);// 先重置一下
                if (!checkSha(card, true)) {
                    return false;
                }
                setShaMulti(card);
            }
            if (!card.askTarget(this)) {
                return false;
            }

            boolean used = useCard(card);

            if (card.isNotTaken() && used) {
                if (!card.getIsFake()) {
                    System.out.println("丢卡：" + card.info() + card.toString());
                    throwCard(card);
                }
                // } else if (!card.isNotTaken()) {
            } else {
                card.setTaken(false);
            }
            return used;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 用别人给的技能
     * 
     * @param orderInt
     * @return
     */
    private boolean useInjectSkill(int orderInt) {
        if (orderInt >= getInjectSkills().size()) {
            return false;
        }
        return getInjectSkills().get(orderInt).use(this);
    }

    public int throwPhase(boolean fastMode) {

        int num = getCards().size() - Math.max(getHP(), 0);
        // System.out.println(this + " card:" + getCards().size() + " hp:" + getHP());
        if (num > 0) {
            // printlnToIO(Text.format("You need to throw %d cards", num));

            getPriviMsg().setOneTimeInfo1(Text.format("💬你需要舍弃 %s 张牌\n", num));
            ArrayList<Card> cs;
            if (fastMode) {// 快速模式直接丢掉后面的牌
                cs = new ArrayList<>();//
                for (int i = 0; i < num; i++) {
                    cs.add(getCards().get(getCards().size() - 1 - i));
                }
            } else {
                cs = chooseManyFromProvided(num, getCards(),true);
            }

            if (cs.size() < num) {// 如果选不够，强制从后面丢起
                // for (Card c : getCards()) {
                for (int i = getCards().size() - 1; i >= 0; i--) {
                    Card c = getCards().get(i);
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

    }

    /*
     * 结束回合
     */
    private void endPhase(boolean fastMode) {
        selfEndPhase(fastMode);
        for (Person p : gameManager.getPlayersBeginFromPlayer(this)) {
            p.otherPersonEndPhase(this, fastMode);
        }
        getPriviMsg().clearAll();
    }

    /**
     * 自己结束回合的时候要做的事情
     * 
     * @param thatPerson 别人
     */
    public void selfEndPhase(boolean fastMode) {

    }

    /**
     * 别人在他结束回合的时候要做的事情
     * 
     * @param thatPerson 别人
     */
    public void otherPersonEndPhase(Person thatPerson, boolean fastMode) {

    }

    public final String help() {
        return (isZuoCi ? "化身：游戏开始时，你随机获得两张武将牌作为\"化身\"牌，然后亮出其中一张，" +
                "获得该\"化身\"牌的一个技能。回合开始时或结束后，你可以更改亮出的\"化身\"牌。\n" +
                "新生：当你受到1点伤害后，你可以获得一张新的\"化身\"牌。\n\n" : "") + skillsDescription();
    }

    public abstract String skillsDescription();

    public String toString() {

        return name();
    }

    public abstract String name();

    private String getGenderEmoji() {
        if (getSex().equals("male")) {
            return "男性";
        } else if (getSex().equals("female")) {
            return "女性";
        } else {
            return "未知";
        }
    }

    private String getNationString() {

        switch (getNation()) {

            case WEI:

                return "魏国";
            case SHU:

                return "蜀国";
            case WU:

                return "吴国";
            case QUN:

                return "群英";

            default:
                return "未知";
        }
    }

    /***
     * 获取状态，privateAccess 不显示手牌，只显示手牌
     */
    public String getPlayerStatus(Person watcher, boolean privateAccess) {
        if (privateAccess) {
            if (getBlankPerson() != null) {
                return getBlankPerson().getStatusPriv(watcher);
            } else {
                return getStatusPriv(watcher);
            }

        } else {
            if (getBlankPerson() != null) {
                return getBlankPerson().getStatusPublic(watcher);
            } else {
                return getStatusPublic(watcher);
            }

        }
    }

    private String getStatusPublic(Person watcher) {
        StringBuilder ans = new StringBuilder();
        TextShowUnit h = new TextShowUnit(8, true);
        for (int i = getHP(); i < 0; i++) {
            h.append("🩶", 1);
        }
        for (int i = 0; i < getHP(); i++) {
            h.append("❤️", 1);
        }
        for (int i = Math.max(0, getHP()); i < getMaxHP(); i++) {
            h.append("🖤", 1);
        }

        TextShowUnit l;
        if (this.equals(watcher)) {// 展示自己
            l = new TextShowUnit(Config.SHOWTEXT_LINECOUNT, false);
        } else {
            l = new TextShowUnit(6, false);
            if (gameManager.getTestMode()) {
                l = new TextShowUnit(Config.SHOWTEXT_LINECOUNT, false);
            } else {
                l = new TextShowUnit(6, false);
            }
        }

        for (Card c : getCards()) {
            if (this.equals(watcher)) {// 展示自己
                l.append("[🃏" + c.getHtmlName() + "]", c.toString().length() + 2);
            } else {
                if (gameManager.getTestMode()) {
                    l.append("[🃏" + c.getHtmlName() + "]", c.toString().length() + 2);
                } else {
                    l.append("🃏", 1);

                }
            }

        }
        TextShowUnit t = new TextShowUnit(Config.SHOWTEXT_LINECOUNT, false);
        t.append("[" + getNationString() + "]", 3);
        t.append("[" + getGenderEmoji() + "]", 3);
        if (getActiveSkills().size() > 0) {
            for (String s : getActiveSkills()) {
                if (this.isAI()) {
                    Person basicPlayer = ((AI) this).getBasicPlayer();
                    t.append("[★" + basicPlayer.getSkillHtmlName(s) + "]", s.length() + 2);

                } else {
                    t.append("[★" + getSkillHtmlName(s) + "]", s.length() + 2);
                }

            }
        }

        if (getEquipments().size() > 0) {

            ArrayList<Card> equips = new ArrayList<>(getEquipments().values());
            for (int i = 1; i <= equips.size(); i++) {
                Card c = equips.get(i - 1);
                String plus = "";
                String type = "";
                if (c instanceof PlusOneHorse) {
                    type = "🐎";
                } else if (c instanceof MinusOneHorse) {
                    type = "🏇";
                } else if (c instanceof Shield) {
                    type = "🛡";
                } else if (c instanceof Weapon) {
                    type = "🗡";
                }
                if (c instanceof PlusOneHorse) {
                    plus = "+1";
                } else if (c instanceof MinusOneHorse) {
                    plus = "-1";
                }
                t.append(Text.format("[%s%s%s]", type, c.getHtmlName(), plus),
                        (c.toString().length()) + (plus.length()) + 2);
            }
        }

        if (getJudgeCards().size() > 0) {

            for (Card c : getJudgeCards()) {
                t.append("[🌀" + c.getHtmlName() + "]", c.toString().length() + 2);
            }

        }

        if (isDrunk() || isDaWu() || isKuangFeng() || isLinked() ||
                isTurnedOver() || isWakenUp()) {

            if (isDrunk())
                t.append("[醉酒]", 3);
            if (isTurnedOver())
                t.append("[翻面]", 3);
            if (isLinked())
                t.append("[连环]", 3);
            if (isKuangFeng())
                t.append("[狂风]", 3);
            if (isDaWu())
                t.append("[大雾]", 3);
            if (isWakenUp())
                t.append("[觉醒]", 3);
        }
        if (getExtraInfo().length() > 0) {
            t.append("[" + getExtraInfo() + "]", getExtraInfo().length() + 1);
        }

        ans.append(h);
        ans.append(l);
        ans.append(t);
        return ans.toString();
    }

    private String getStatusPriv(Person watcher) {

        StringBuilder ans = new StringBuilder();

        ans.append("身份: " + getIdentityHtml() + " [" + getNationString() + "][" + getGenderEmoji() + "]");
        ans.append("\n体力: " + getHPEmoji());

        ans.append("\n手牌:");
        for (int i = 1; i <= getCards().size(); i++) {
            // ans += "[" + getCards().get(i - 1).info() + getCards().get(i - 1)+ "]";
            Card c = getCards().get(i - 1);
            if (!getOldCards().contains(c)) {
                ans.append("[●" + c.getHtmlName() + "]");
            } else {
                ans.append("[" + c.getHtmlName() + "]");
            }

        }

        if (getActiveSkills().size() > 0) {

            ans.append("\n技能:");

            for (String s : getActiveSkills()) {
                if (this.isAI()) {
                    Person basicPlayer = ((AI) this).getBasicPlayer();
                    ans.append("[" + basicPlayer.getSkillHtmlName(s) + "]");
                } else {
                    ans.append("[" + getSkillHtmlName(s) + "]");
                }

            }
        }

        if (getEquipments().size() > 0) {
            ans.append("\n装备:");
            ArrayList<Card> equips = new ArrayList<>(getEquipments().values());
            for (int i = 1; i <= equips.size(); i++) {
                Card c = equips.get(i - 1);
                String plus = "";
                if (c instanceof PlusOneHorse) {
                    plus = "+1";
                } else if (c instanceof MinusOneHorse) {
                    plus = "-1";
                }
                ans.append(Text.format("[%s%s]", c.getHtmlName(), plus));
            }
        }

        if (getJudgeCards().size() > 0) {

            ans.append("\n判定牌:");

            for (Card c : getJudgeCards()) {
                ans.append("[" + c.getHtmlName() + "]");
            }

        }
        if (isDrunk() || isDaWu() || isKuangFeng() || isLinked() ||
                isTurnedOver() || isWakenUp()) {

            ans.append("\n状态: ");
            ans.append(isDrunk() ? "[醉酒]" : "");
            ans.append(isTurnedOver() ? "[翻面]" : "");
            ans.append(isLinked() ? "[连环]" : "");
            ans.append(isKuangFeng() ? "[狂风]" : "");
            ans.append(isDaWu() ? "[大雾]" : "");
            ans.append(isWakenUp() ? "[觉醒]\n" : "");
        }
        String extraInfo = getExtraInfo();
        if (!"".equals(extraInfo)) {
            ans.append("\n附加: ");
            ans.append(getExtraInfo());
        }

        return ans.toString();

    }

    public String getKingSkill() {
        if (getIdentity() != Identity.KING) {
            return "";
        }
        for (Method method : getClass().getDeclaredMethods()) {
            if (method.getAnnotation(KingSkill.class) != null) {
                return method.getAnnotation(KingSkill.class).value();
            }
        }
        return "";
    }

    /**
     * 初始的技能
     */
    public Set<String> getInitialSkills() {
        HashSet<String> skills = new HashSet<>();
        for (Method method : getClass().getDeclaredMethods()) {
            if (method.getAnnotation(Skill.class) != null) {
                if (method.getAnnotation(Skill.class).value().equals("化身") ||
                        method.getAnnotation(Skill.class).value().equals("新生")) {
                    if (!isZuoCi) {
                        continue; // for 左慈
                    }
                }
                skills.add(method.getAnnotation(Skill.class).value());
            } else if (method.getAnnotation(ForcesSkill.class) != null) {
                // if (method.getAnnotation(ForcesSkill.class).value().equals("无双")) {
                // if (!hasWuShuang()) {
                // continue; // for 神吕布
                // }
                // }
                skills.add(method.getAnnotation(ForcesSkill.class).value());
            } else if (method.getAnnotation(RestrictedSkill.class) != null) {
                skills.add(method.getAnnotation(RestrictedSkill.class).value());
            } else if (method.getAnnotation(WakeUpSkill.class) != null) {
                skills.add(method.getAnnotation(WakeUpSkill.class).value());
            } else if (method.getAnnotation(KingSkill.class) != null) {
                skills.add(method.getAnnotation(KingSkill.class).value());
            } else if (method.getAnnotation(AfterWakeSkill.class) != null) {
                if (isWakenUp()) {
                    skills.add(method.getAnnotation(AfterWakeSkill.class).value());
                }
            }
        }
        return skills;
    }

    public void setProgress(SanProgress progress) {
        this.progress = progress;
    }

    public SanProgress getProgress() {
        return progress;
    }

    /**
     * 变成白板人，进入另一个循环
     * 
     * @param blankPerson
     */
    public void setBlankPerson(Person blankPerson) {
        this.blankPerson = blankPerson;
    }

    public boolean isZuoCi() {
        return isZuoCi;
    }

    public void setZuoCi(boolean zuoCi) {
        isZuoCi = zuoCi;
    }

    public void addHuaShenToList(Person p) {
        huaShenList.add(p);
    }

    public ArrayList<Person> getHuaShenList() {
        return huaShenList;
    }

    public void setHuaShenList(ArrayList<Person> huaShen) {
        this.huaShenList = huaShen;
    }

    public void setActiveHuaShen(Person p) {
        activeHuaShen = p;
    }

    public Person getActiveHuaShen() {
        return activeHuaShen;
    }

    public void addActiveSkill(String... skills) {

        for (String skill : skills) {
            activeSkill.add(skill);
        }
    }

    public void clearAndAddActiveSkill(String... skills) {
        activeSkill.clear();
        for (String skill : skills) {
            activeSkill.add(skill);
        }
    }

    /** 否是生效的技能,避免左慈的化身使用多技能 */
    public boolean isActiveSkill(String skill) {
        return getActiveSkills().contains(skill);
    }

    @Override
    public void stepInPriv(MsgObj privMsgObj) {
        if (getBlankPerson() == null) {
            super.stepInPriv(privMsgObj);
        } else {
            getBlankPerson().stepInPriv(privMsgObj);
        }

    };

    @Override
    public void stepOutPriv(MsgObj privMsgObj) {
        if (getBlankPerson() == null) {
            super.stepOutPriv(privMsgObj);
        } else {
            getBlankPerson().stepOutPriv(privMsgObj);
        }
    };

    // public void setPos(int pos) {
    // this.pos = pos;
    // }

    // public int getPos() {
    // return pos;
    // }

    public String getUrl() {
        return DescUrl.getDescUrl(this.toString());
    }

    /**
     * 主要是给白板用的
     * 
     * @param name
     */
    public void setName(String name) {

    }

    /**
     * 获取全幅照
     * 
     * @return
     */
    public String getLogoImgFileId() {
        return this.logoImgFileId;
    }

    /**
     * 获取横幅照
     * 
     * @return
     */
    public String getBannerImgFileId() {
        return this.bannerImgFileId;
    }

    /**
     * 获取带html注释url的名字
     * 
     * @return
     */
    public String getHtmlName() {
        String showName = isZuoCi ? "左慈[" + toString() + "]" : toString();

        if (user == null) {
            return "AI-<b><a href=\"" + getUrl() + "\">" + showName + "</a></b>";

        } else {
            return "<b><a href=\"" + getUrl() + "\">" + showName + "</a></b>(<i><a href=\"tg://user?id="
                    + user.user_id
                    + "\">" + user.full_name + "</a></i>)";
        }

    }

    @Override
    public String getPlateName() {
        String showName = isZuoCi ? "左慈[" + toString() + "]" : toString();
        if (user == null) {
            return "AI-<b>" + showName + "</b>";
        } else {
            return "<b>" + showName + "</b>(<i>" + user.full_name + "</i>)";
        }

    }

    public String getSkillHtmlName(String skillName) {
        return "<b><a href=\"" + getUrl() + "\">" + skillName + "</a></b>";
    };

    public Set<String> getActiveSkills() {
        return activeSkill;
    }

    @Override
    public void setUser(SanUser user) {
        this.user = user;
    }

    @Override
    public SanUser getUser() {
        // TODO Auto-generated method stub
        return this.user;
    }

    public void setFastMode(boolean fastMode) {
        this.fastMode = fastMode;

    }

    private List<Card> deadShowCards = null;

    /**
     * 挂掉时缓存一个卡列表
     * 
     * @param deadShowCards
     */
    public void setDeadShowCard(List<Card> deadShowCards) {
        this.deadShowCards = deadShowCards;
    }

    /**
     * 获取挂掉时的卡列表，因为这时卡牌都已丢掉了
     * 
     * @return
     */
    public List<Card> getDeadShowCards() {
        return this.deadShowCards;
    }

    public void setShowStatusMsg(MsgObj showStatusMsg) {
        this.showStatusMsg = showStatusMsg;
    }

    public MsgObj getShowStatusMsg() {
        return this.showStatusMsg;
    }

    private boolean skipNoticeUsePublic = false;

    @Override
    public void setskipNoticeUsePublic(boolean skipNoticeUsePublic) {
        this.skipNoticeUsePublic = skipNoticeUsePublic;
    }

    @Override
    public boolean getSkipNoticeUsePublic() {
        return this.skipNoticeUsePublic;
    }

    public static String getIdentityString(Identity identity) {

        switch (identity) {
            case KING:

                return "主公";
            case REBEL:

                return "反贼";
            case MINISTER:

                return "忠臣";
            case TRAITOR:

                return "内奸";
            default:
                return "未知";
        }

    }

    /**
     * 为那些群体技能或群体卡统一删除私发提示
     * 
     * @param list
     */
    public static void clearPrivShowTextExectHeader(List<Person> list) {
        for (Person p : list) {
            p.getPriviMsg().clearInfo1();
            p.getPriviMsg().clearInfo2();
            p.getPriviMsg().clearOneTimeInfo1();
            p.getPriviMsg().clearOneTimeInfo2();
        }
    }

    public void setGamePlayerNo(int i) {
        this.gamePlayerNo = i;
    }

    public int getGamePlayerNo() {
        return this.gamePlayerNo;
    }

    /**
     * 判定是不是ai，如果是AI对象或者是ai的basicplayer
     */
    @Override
    public boolean isAI() {
        if (getUser() == null || getAi() != null) {
            return true;
        }
        return false;
    }

    public String getAllPersonInfo() {
        try {
            StringBuilder result = new StringBuilder(
                    Text.format("<blockquote><b>你的身份是 %s</b></blockquote>", getIdentityHtml()));
            List<Person> newOrderPersons = getGameManager().getPlayersBeginFromPlayer(this);
            // System.out.println("newOrderPersons="+newOrderPersons);
            // System.out.println(newOrderPersons);
            Map<String, List<Person>> dbuffMap = new HashMap<>(6);

            for (Person p : newOrderPersons) {

                // System.out.println(p.getPos());
                String isInRound = (p.isMyRound() ? "✅" : "");
                String circleNum = Text.circleNum(p.getGamePlayerNo());
                String isSelf = (p.equals(this) ? "自己-" : "");
                String isKing = p.getIdentity() == Identity.KING ? "👑" : "";
                result.append("\n<b>" + isInRound + circleNum + isSelf + isKing + p.getHtmlName() + "</b>");
                result.append("\n" + p.getPlayerStatus(this, false));

                if (p.isDrunk())
                    putNameToList(dbuffMap, "醉酒", p);

                if (p.isTurnedOver())
                    putNameToList(dbuffMap, "翻面", p);

                if (p.isLinked())
                    putNameToList(dbuffMap, "连环", p);

                if (p.isKuangFeng())
                    putNameToList(dbuffMap, "狂风", p);

                if (p.isDaWu())
                    putNameToList(dbuffMap, "大雾", p);

                if (p.isWakenUp())
                    putNameToList(dbuffMap, "觉醒", p);

            }
            for (String dbuff : dbuffMap.keySet()) {
                List<Person> persons = dbuffMap.get(dbuff);
                result.append("\n【" + dbuff + "】");
                TextShowUnit t = new TextShowUnit(Config.SHOWTEXT_LINECOUNT, false);
                for (Person dp : persons) {
                    t.append("【" + dp.name() + "】", dp.name().length() + 2);
                }
                result.append(t.toString());
            }

            List<Person> dead = getGameManager().getDead();
            if (dead.size() > 0) {
                result.append("\n\n阵亡");
                for (Person d : dead) {
                    result.append("\n    ├🪦" + d.getHtmlName() + " " + d.getIdentityHtml());
                }
            }
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "出错";
        }
    }

    private void putNameToList(Map<String, List<Person>> dbuffMap, String duff, Person p) {
        List<Person> personList = dbuffMap.get(duff);
        if (personList == null) {
            personList = new ArrayList<>(5);
            dbuffMap.put(duff, personList);
        }
        personList.add(p);

    }

    /**
     * 是否手牌中有红色牌
     * 
     * @return
     */
    public boolean hasRedHandCard() {
        for (Card c : getCards()) {
            if (c.isRed()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否手牌中有黑色牌
     * 
     * @return
     */
    public boolean hasBlackHandCard() {
        for (Card c : getCards()) {
            if (c.isBlack()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否手牌中有某色色牌
     * 
     * @return
     */
    public boolean hasColorHandCard(Color color) {
        for (Card c : getCards()) {
            if (c.color() == color) {
                return true;
            }
        }
        return false;
    }

}
