package sanguosha.people;

import sanguosha.cards.Card;
import sanguosha.cards.Color;
import sanguosha.cards.EquipType;
import sanguosha.cards.Equipment;
import sanguosha.cards.JudgeCard;
import sanguosha.cards.basic.HurtType;
import sanguosha.cards.basic.Jiu;
import sanguosha.cards.basic.Sha;
import sanguosha.cards.basic.Shan;
import sanguosha.cards.basic.Tao;
import sanguosha.cards.equipments.Shield;
import sanguosha.cards.equipments.Weapon;
import sanguosha.skills.InjectSkill;
import components.BlockingMap;
import components.JoinArrayList;
import components.MyCountDownLatch;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import config.Config;
import config.DescUrl;
import config.Text;
import db.ImgDB;

import java.util.concurrent.TimeUnit;

import msg.CallbackEven;
import msg.MsgObj;
import msg.ReturnType;
import msg.ShowText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static sanguosha.cards.Color.NOCOLOR;
import static sanguosha.cards.EquipType.shield;
import static sanguosha.cards.EquipType.weapon;

public abstract class Attributes implements HuaShen {
    private boolean isTurnedOver = false;
    private boolean isLinked = false;
    private boolean isDrunk = false;
    /**
     * 使用过酒杀
     */
    private boolean isDrunkShaUsed = false;
    private boolean isDead = false;
    private String sex = "male";
    private Nation nation;
    private Identity identity;
    private int maxShaCount = 1;
    private boolean hasUsedSkill1 = false;

    /**
     * 一些英雄和武器的预置技能
     */
    private final ArrayList<InjectSkill> injectSkills = new ArrayList<>();
    /**
     * 一些英雄和武器的预置技能
     */
    private final ArrayList<String> skillcards = new ArrayList<>();
    private final ArrayList<Card> cards = new ArrayList<>();
    private Set<Card> oldCards = new HashSet<>();
    private final List<String> skillAndCards = new JoinArrayList(injectSkills, skillcards, cards);
    private final HashMap<EquipType, Equipment> equipments = new HashMap<>();
    private final ArrayList<JudgeCard> judgeCards = new ArrayList<>();
    private int maxHP = 3;
    private int currentHP;
    /**
     * 计算杀的个数从0算起
     */
    private int shaCount = 0;
    private boolean myRound = false;
    private boolean isKuangFeng = false;
    private boolean isDaWu = false;
    private boolean hasWakenUp = false;
    private ShowText intText = new ShowText();
    private ShowText outText = new ShowText();
    private Map<String, MsgObj> tempActionMsgObjMap = null;
    private Set<MsgObj> actingPrivMsgs = new HashSet<>();
    private AI ai = null;

    @Override
    public synchronized void putTempActionMsgObj(String msg_name, MsgObj msgObj) {
        if (this.tempActionMsgObjMap == null) {
            this.tempActionMsgObjMap = new HashMap<>();
        }
        tempActionMsgObjMap.put(msg_name, msgObj);

    }

    @Override
    public synchronized void removeTempActionMsgObj(String msg_name) {

        tempActionMsgObjMap.remove(msg_name);

    }

    @Override
    public MsgObj getTempActionMsgObj(String msg_name) {
        if (tempActionMsgObjMap == null) {
            return null;
        }
        return tempActionMsgObjMap.get(msg_name);
    }

    @Override
    public MsgObj getTempActionMsgObjFirstOrder(String... msg_name) {
        if (tempActionMsgObjMap == null) {
            return null;
        }
        for (String name : msg_name) {

            MsgObj msgObj = tempActionMsgObjMap.get(name);
            if (msgObj != null) {
                System.out.println(name + ":" + msgObj);
                return msgObj;
            }
        }
        return null;
    }

    @Override
    public MsgObj getTempActionMsgObjFirstOrder(MsgObj lastChoose, String... msg_name) {
        if (tempActionMsgObjMap == null) {
            return null;
        }
        for (String name : msg_name) {
            MsgObj msgObj = tempActionMsgObjMap.get(name);
            if (msgObj != null) {
                return msgObj;
            }
        }
        return lastChoose;
    }

    /**
     * 被ai控制，设置ai
     * 
     * @param ai
     */
    public void setAi(AI ai) {
        this.ai = ai;
    }

    /** 是否已经被AI控制 */
    public AI getAi() {
        return this.ai;
    }

    public Set<Card> getOldCards() {
        return oldCards;
    }

    public ShowText getPriviMsg() {
        return intText;
    }

    public ShowText getPublicMsg() {
        return outText;
    }

    @Override
    public boolean hasWuShuang() {
        return false;
    }

    public void wakeUp() {
        // printlnPriv(this + " wakes up!");
        hasWakenUp = true;
    }

    public boolean isWakenUp() {
        return hasWakenUp;
    }

    public boolean isDaWu() {
        return isDaWu;
    }

    public void setDaWu(boolean daWu) {
        isDaWu = daWu;
    }

    public boolean isKuangFeng() {
        return isKuangFeng;
    }

    public void setKuangFeng(boolean kuangFeng) {
        isKuangFeng = kuangFeng;
    }

    public boolean isMyRound() {
        return myRound;
    }

    public void setMyRound(boolean myRound) {
        this.myRound = myRound;
    }

    public boolean isStepInPriv() {
        return actingPrivMsgs.size() > 0;
    };

    public void stepInPriv(MsgObj privMsgObj) {
        actingPrivMsgs.add(privMsgObj);
    };

    public void stepOutPriv(MsgObj privMsgObj) {
        actingPrivMsgs.remove(privMsgObj);
    };

    @Override
    public Set<MsgObj> getActingPrivMsgs() {
        return actingPrivMsgs;
    }

    public void initMaxHP(int maxHP) {
        this.maxHP = maxHP;
        this.currentHP = Math.max(currentHP, maxHP);
    }

    public void setMaxHpNotSetCurrent(int maxHP) {
        this.maxHP = maxHP;
        if (this.currentHP > this.maxHP) {
            this.currentHP = maxHP;
        }

    }

    public int getMaxHP() {
        return maxHP;
    }

    public void setShaCount(int shaCount) {
        this.shaCount = shaCount;
    }

    public int getShaCount() {
        return shaCount;
    }

    public void setCurrentHP(int currentHP) {
        this.currentHP = currentHP;
    }

    /**
     * 减去生命
     * 
     * @param sub
     */
    public void subCurrentHP(int sub) {
        currentHP -= sub;
    }

    public int getHP() {
        return currentHP;
    }

    /**
     * 获取预先扣减的体力的表情
     * 
     * @return
     */
    public String getHPEmojiMinus(int minus) {
        int currentHPTemp = currentHP;
        currentHPTemp -= minus;
        String temp = "";
        for (int i = 0; i < currentHPTemp; i++) {
            temp += "❤️";
        }
        for (int i = Math.max(0, currentHPTemp); i < maxHP; i++) {
            temp += "🖤";
        }
        return temp;
    }

    /**
     * 获取体力的表情
     * 
     * @return
     */
    public String getHPEmoji() {
        String temp = "";

        for (int i = currentHP; i < 0; i++) {// 负值也显示
            temp += "🩶";
        }
        for (int i = 0; i < currentHP; i++) {
            temp += "❤️";
        }
        for (int i = Math.max(0, currentHP); i < maxHP; i++) {
            temp += "🖤";
        }
        return temp;
    }

    public int getMaxShaCount() {
        return maxShaCount;
    }

    public void setMaxShaCount(int maxShaCount) {
        this.maxShaCount = maxShaCount;
    }

    public Nation getNation() {
        return nation;
    }

    public void setNation(Nation nation) {
        this.nation = nation;
    }

    public Identity getIdentity() {
        return identity;
    }

    public boolean getIsNB() {
        return false;
    }

    public String getIdentityHtml() {
        String identiy = Person.getIdentityString(identity);
        return "<b><a href=\"" + DescUrl.getDescUrl(identiy) + "\">" + identiy + "</a></b>";

    }

    public void setIdentityAndSkill(Identity identity) {
        this.identity = identity;
        // 加技能放在设置身份这里
        for (String skill : getInitialSkills()) {
            addActiveSkill(skill);
        }
    }

    public void setIdentityOnly(Identity identity) {
        this.identity = identity;

    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    /**
     * 检查全部人的阵亡，返回自己是否阵亡
     * 
     * @return
     */
    public boolean checkDead() {
        // if (isDead) {
        // return true;
        // }
        // getGameManager().checkDying();
        // return false;
        return isDead;
    }

    /**
     * 单纯返回自己是否阵亡
     * 
     * @return
     */
    public boolean isDead() {

        return isDead;
    }

    @Override
    public boolean isDrunk() {

        return isDrunk;
    }

    /**
     * 喝醉，每次出牌只限制一次
     * 
     * @param drunk
     */

    public void setDrunk(boolean drunk) {
        isDrunk = drunk;
        if (isDrunk) {
            getPriviMsg().appendOneTimeInfo1("\n💬 呜呼，你喝醉了,下一张[杀]的伤害值+1（未濒死时，每回合限一次）\"");
        }
    }

    /**
     * 是否已经酒杀过
     * 
     * @return
     */
    public boolean isDrunkShaUsed() {
        return isDrunkShaUsed;
    }

    /**
     * 酒杀，每次出牌只限制一次
     * 
     * @param drunk
     */
    public void setDrunkShaUsed(boolean drunkShaUsed) {
        this.isDrunkShaUsed = drunkShaUsed;

    }

    public boolean isLinked() {
        return isLinked;
    }

    public boolean isTurnedOver() {
        return isTurnedOver;
    }

    /**
     * 更改链接状态
     */
    public void switchLink() {
        isLinked = !isLinked;
        // printlnPriv(this + " is linked, now " + (isLinked ? "连环状态" : "非连环状态"));
    }

    public void turnover() {
        isTurnedOver = !isTurnedOver;
        // printlnPriv(this + " is turned over, now " + (isTurnedOver ? "反面朝上" :
        // "正面朝上"));
    }

    public void die() {
        isDead = true;
        getGameManager().die((Person) this);
        if (getGameManager().gameIsEnd()) {
            getGameManager().endGame();
        }
    }

    public boolean hasNotUsedSkill1() {
        if (hasUsedSkill1) {
            // printlnToIOPriv("you have used this skill in this phase");
            return false;
        }
        return true;
    }

    public void setHasUsedSkill1(boolean hasUsedSkill1) {
        this.hasUsedSkill1 = hasUsedSkill1;
    }

    public ArrayList<Card> getCards() {
        if (getAi() != null) {
            return getAi().getCards();
        } else {
            return cards;
        }

    }

    public List<String> getSkillCardsAndCards() {
        return skillAndCards;
    }

    /**
     * 用于每局开始时的被注入的技能动态展示
     * 
     * @return
     */
    public List<InjectSkill> getInjectSkills() {
        return injectSkills;
    }

    /**
     * 用于每局开始时的技能动态展示
     * 
     * @return
     */
    public List<String> getSkillCards() {
        return skillcards;
    }

    public HashMap<EquipType, Equipment> getEquipments() {
        return equipments;
    }

    public ArrayList<JudgeCard> getJudgeCards() {
        return judgeCards;
    }

    /**
     * 有的判定牌是别的牌替换打出的，要获得实际的牌
     */
    public ArrayList<Card> getRealJudgeCards() {
        ArrayList<Card> ans = new ArrayList<>();
        for (JudgeCard jc : judgeCards) {
            ans.add(jc.getReplaceCards().get(0));
        }
        return ans;
    }

    public void removeJudgeCard(Card c) {
        for (JudgeCard jc : judgeCards) {
            if (jc.getReplaceCards().get(0) == c) {
                judgeCards.remove(jc);
                return;
            }
        }
    }

    /**
     * 获取装备和手牌，装备牌排在前面
     * 
     * @return
     */
    public ArrayList<Card> getCardsAndEquipments() {
        ArrayList<Card> ans = new ArrayList<>(equipments.values());
        ans.addAll(cards);
        return ans;
    }

    /**
     * 只获取装备
     * 
     * @return
     */
    public ArrayList<Card> getEquipmentsList() {
        ArrayList<Card> ans = new ArrayList<>(equipments.values());
        return ans;
    }

    /**
     * 返回所有卡包括 手牌，装备的张数
     * 
     * @return
     */
    public int getCardsAndEqSize() {

        return cards.size() + equipments.size();
    }

    /**
     * 返回所有卡包括 手牌，装备，判断牌的张数
     * 
     * @return
     */
    public int getAllCardSize() {

        return cards.size() + equipments.size() + judgeCards.size();
    }

    public int getShaDistance() {
        if (equipments.get(weapon) != null) {
            return ((Weapon) equipments.get(weapon)).getDistance();
        }
        return 1;
    }

    public boolean addJudgeCard(JudgeCard c) {
        // Thread.dumpStack();
        for (JudgeCard jc : judgeCards) {

            if (jc.toString().equals(c.toString())) {
                return false;
            }
        }
        judgeCards.add(c);
        return true;
    }

    public boolean hasEquipment(EquipType type, String name) {
        if (name == null) {
            return equipments.get(type) != null;
        }
        if (equipments.get(type) == null) {
            return false;
        }
        return equipments.get(type).toString().equals(name);
    }

    public void addCard(Card c) {
        if (getAi() != null) {
            getAi().addCard(c);
        } else {
            addCard(c, false);
        }

    }

    public void addCard(Card c, boolean print) {
        if (c == null) {
            return;
        }
        getCards().add(c);
        if (print) {
            // printlnPriv(this + " got 1 card");
            // printlnToIOPriv(this + " got card: ");
            // printCard(c);
        }
        c.setOwner((Person) this);
    }

    public void addCard(List<Card> cs) {
        addCard(cs, true);
    }

    public void addCard(List<Card> cs, boolean print) {
        // String out=this + " got " + cs.size() + " cards"+"\n";

        for (Card c : cs) {
            getCards().add(c);
            if (print) {
                // getInText().appendInfo1("["+c.info()+c.toString()+"]");
                // out+=(c+"\n");
            }
            c.setOwner((Person) this);
        }
        // println(out);
    }

    public void drawCard() {
        addCard(getGameManager().getCardsHeap().draw());
    }

    public void drawCards(int num, boolean print) {
        // System.out.println( this+"getGameManager():"+getGameManager());
        addCard(getGameManager().getCardsHeap().draw(num), print);
    }

    public void drawCards(int num) {
        drawCards(num, true);
    }

    public void loseCard(List<Card> cs) {
        loseCard(cs, true);
    }

    public void loseCard(List<Card> cs, boolean throwAway) {
        if (cs == null || cs.size() <= 0) {
            return;
        }
        ArrayList<Card> newCs = new ArrayList<>(cs);
        getPublicMsg().clearInfo1();
        getPublicMsg().appendInfo1(this + " 失去手牌: ");
        for (Card c : newCs) {
            loseCard(c, throwAway);
            getPublicMsg().appendInfo1("[" + c.info() + c + "]");
        }
        // getGameManager().getIo().println(getPublicMsg().toString());
    }

    public void loseCard(Card c) {
        loseCard(c, true);
    }

    public void loseCard(Card c, boolean throwAway) {
        loseCard(c, throwAway, true);
    }

    public void loseCard(ArrayList<Card> cs, boolean throwAway, boolean print) {
        if (cs == null || cs.size() <= 0) {
            return;
        }
        ArrayList<Card> newCs = new ArrayList<>(cs);
        getPublicMsg().clearInfo1();
        getPublicMsg().appendInfo1(this + " 失去手牌: ");
        for (Card c : newCs) {
            loseCard(c, throwAway, false);
            getPublicMsg().appendInfo1("[" + c.info() + c + "]");
        }

        getGameManager().getIo().printlnPublic(getPublicMsg().toString());

    }

    public void loseCard(Card c, boolean throwAway, boolean print) {
        if (c == null) {// 一般是由超时引发的
            return;
        }
        // 没颜色的都是假牌,但是替换牌里可能有真牌
        if (c.color() == NOCOLOR || c.getIsFake()) {
            return;
        }

        if (getAi() != null) {
            getAi().loseCard(c, throwAway, print);
            return;
        }

        if (getRealJudgeCards().contains(c)
                || getRealJudgeCards().contains(c.getReplaceCards().get(0))) {
            removeJudgeCard(c);
            if (print && throwAway) {
                // printlnPriv(this + " 丢弃判定牌: " + c);
                // getGameManager().getIo().printCardPublic(c);
            }
        } else if (c instanceof Equipment && getEquipments().containsValue(c)) {
            getEquipments().remove(((Equipment) c).getEquipType());
            if (c.toString().equals("白银狮子")) {
                recover(null, 1);
            }
            if (!checkDead()) {
                lostEquipment();
            }
            if (print && throwAway) {
                // printlnPriv(this + " 丢弃装备: " + c);
                // getGameManager().getIo().printCardPublic(c);
            }
        } else if (getCards().contains(c) || getCards().contains(c.getReplaceCards().get(0))) {
            getCards().remove(c);
            if (!checkDead()) {
                lostHandCardAction();
            }

            // if (print && throwAway) {
            // println(this + " lost hand card: ");
            // getGameManager().getIo().printCardPublic(c);
            // }
        }

        // for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
        // System.out.println(ste.toString());
        // }

        c.setOwner(null);
        if (throwAway) {
            getGameManager().getCardsHeap().discard(c);
        } else {
            c.setTaken(true);
        }
    }

    public void throwCard(List<Card> cs) {
        for (Card c : cs) {
            throwCard(c);
        }
    }

    public void throwCard(Card c) {

        loseCard(c, true, false);
    }

    public int hurt(List<Card> cs, Person source, int num) {
        return hurt(cs, source, num, HurtType.normal);
    }

    public int hurt(Card card, Person source, int num) {
        ArrayList<Card> cs = new ArrayList<>();
        cs.add(card);
        return hurt(cs, source, num, HurtType.normal);
    }

    public int hurt(Card card, Person source, int num, HurtType type) {
        ArrayList<Card> cs = new ArrayList<>();
        cs.add(card);
        return hurt(cs, source, num, type);
    }

    public int hurt(List<Card> cs, Person source, int num, HurtType type) {
        if (isDaWu() && type != HurtType.thunder) {
            String res = Text.format("%s 在大雾中,攻击失效", getPlateName());
            getGameManager().getIo().printlnPublic(res);
            return 0;
        }
        int realNum = num;

        if (hasEquipment(shield, "藤甲") && ((Shield) getEquipments().get(shield)).isValid()) {
            if (type == HurtType.fire) {
                realNum++;
            }
        }
        if (isKuangFeng() && type == HurtType.fire) {
            String res = Text.format("%s 在狂风中,火焰🔥伤害+1", getPlateName());
            getGameManager().getIo().printlnPublic(res);
            realNum++;
        }
        // 杀的提醒移到这里，因为有时展示得太晚了
        MsgObj hurtMsgObj = getTempActionMsgObj("hurt");
        if (hasEquipment(shield, "白银狮子") && ((Shield) getEquipments().get(shield)).isValid()) {
            if (num > 1) {
                if (hurtMsgObj != null) {
                    hurtMsgObj.appendText(",已装备白银狮子");
                }
                realNum = 1;
            }
        }

        // sleep(1000);
        if (hurtMsgObj != null) {
            removeTempActionMsgObj("hurt");
            hurtMsgObj.text = hurtMsgObj.text + Text.format(",受%s点%s伤:%s",
                    // getPlateName(),
                    realNum + "",
                    Sha.hurtTypeString(type),
                    getHPEmojiMinus(realNum));
            hurtMsgObj.setImg(toString() + "|hurt");
            hurtMsgObj.replyMakup = null;
            getGameManager().getMsgAPI().editPhotoForce(hurtMsgObj);

        }

        // 伤害小于0就返回了，
        if (realNum <= 0) {
            return realNum;
        }
        gotHurt(cs, source, realNum);

        loseHP(realNum, source);

        if (type != HurtType.normal && isLinked()) {
            switchLink();
        }
        if (source != null) {
            source.hurtOther((Person) this, realNum);
        }
        for (Person p : getGameManager().getPlayersBeginFromPlayer((Person) this)) {
            // for (Person p : getGameManager().getPlayers()) {
            p.otherPersonMakeHurt(source, (Person) this);
        }

        if (!checkDead()) {
            // if (isZuoCi() && launchSkillPriv("新生")) {
            if (isZuoCi()) {
                xinSheng();
            }
        } else {
            if (source != null) {
                source.killOther();
                getGameManager().deathRewardPunish((Person) this, source);
            }

        }
        return realNum;
    }

    /**
     * 这个是不会触发技能的，hurt会触发技能,里面有死亡逻辑
     * 
     * @param num
     */
    public void loseHP(int num, Person source) {
        subCurrentHP(num);
        if (getHP() <= 0) {
            dying(source);
        }
    }

    public void recover(MsgObj puMsgObj, int num) {
        if (getHP() == getMaxHP()) {
            return;
        }
        setCurrentHP(Math.min(getHP() + num, getMaxHP()));
        getPriviMsg().setOneTimeInfo1(this + " 恢复了 " + num + " 体力:" + getHP() + "/" + getMaxHP());
    }

    public boolean askTao() {
        boolean wanSha = false;
        Person wanShaPerson = null;
        for (Person p : getGameManager().getPlayersBeginFromPlayer((Person) this)) {
            if (p.isMyRound() && p.hasWanSha()) {
                wanSha = true;
                wanShaPerson = p;
                break;
            }
        }
        MsgObj publicMsgObj = MsgObj.newMsgObj(getGameManager());
        publicMsgObj.forPlayers = new ArrayList<>();
        putTempActionMsgObj("tao", publicMsgObj);
        AtomicReference<PlayerIO> showCardPerson = new AtomicReference<>(null);
        AtomicReference<Card> showCard = new AtomicReference<>(null);
        for (Person p : getGameManager().getPlayersBeginFromPlayer((Person) this)) {
            if (p.getUser() == null) {// 排除ai
                continue;
            }
            if (p == this) {
                if (!(p.checkTao() || p.checkJiu())) {
                    continue;
                }
                publicMsgObj.forPlayers.add(p);

            } else if (((!wanSha) || (p == wanShaPerson)) && p.checkTao()) {
                publicMsgObj.forPlayers.add(p);
            }
        }

        if (publicMsgObj.forPlayers.isEmpty()) {

            String reult = Text.format("%s 濒死,帮TA出一张 %s(本人可出 %s)", this.getPlateName(),
                    Card.getHtmlNameFromName("桃"), Card.getHtmlNameFromName("酒"));
            publicMsgObj.text = reult;
            publicMsgObj.setImg(toString() + "|help_me");
            publicMsgObj.chatId = getGameManager().getChatId();
            getGameManager().getMsgAPI().sendImg(publicMsgObj);
            sleep(3000);

            publicMsgObj.setImg(toString() + "|dead");
            reult = Text.format(",%s 阵亡", this.getPlateName());
            publicMsgObj.text = publicMsgObj.text + reult;
            getGameManager().getMsgAPI().editPhotoForce(publicMsgObj);
            // sleep(3000);
            return false;
        }
        Person thisPerson = (Person) this;
        String reult = Text.format("%s 濒死,帮TA出一张 %s(本人打出 %s)", this.getPlateName(),
                Card.getHtmlNameFromName("桃"), Card.getHtmlNameFromName("酒"));
        publicMsgObj.text = reult;
        publicMsgObj.setImg(toString() + "|help_me");
        publicMsgObj.chatId = getGameManager().getChatId();
        requestCardForPublic(publicMsgObj, showCardPerson, showCard, "桃",
                "帮TA出桃", "tao", new CallbackEven() {
                    @Override
                    public Card additionGetCardExec(Card preCard, Person person, MsgObj thiMsgObj) {
                        String hasDoneString = (String) thiMsgObj.getReturnAttr(ReturnType.doneAskCardPublic, 0);
                        if ("桃".equals(hasDoneString)) {// 如果超时了，或者有人出了，就连酒都不用出了
                            return null;
                        }
                        if (preCard == null && thisPerson.equals(person)) {
                            // --------重置一下msg的状态
                            // thiMsgObj.resetAttributes(ReturnType.ChooseOnePublic);
                            thiMsgObj.resetAttributes(ReturnType.ChooseOneCard);
                            thiMsgObj.resetAttributes(ReturnType.Msgid);
                            thiMsgObj.isDeleted = false;
                            showCardPerson.set(null);
                            // ------重新请求一张酒
                            String res = "请你出 酒";
                            person.getPriviMsg().setOneTimeInfo1(res);
                            return person.requestCard("酒", showCardPerson, thiMsgObj);
                        }
                        return null;
                    }
                });
        if (showCardPerson.get() == null) {
            reult = Text.format(",%s 阵亡", getPlateName());
            publicMsgObj.setImg(toString() + "|dead");
            publicMsgObj.text = publicMsgObj.text + reult;
            publicMsgObj.replyMakup = null;
            getGameManager().getMsgAPI().editPhotoForce(publicMsgObj);

            return false;
        } else {
            String type;
            if (showCard.get() instanceof Tao) {
                type = "桃";
                int plus = gotSavedBy((Person) showCardPerson.get());
                reult = Text.format("%s 出🍑 %s,%s 恢复%s点体力%s", showCardPerson.get().getPlateName(),
                        showCard.get().getHtmlNameWithColor(), getHtmlName(), (1 + plus), getHPEmojiMinus(-1));
            } else {
                type = "酒";
                reult = Text.format("%s 出🍶 %s,恢复1点体力%s", showCardPerson.get().getHtmlName(),
                        showCard.get().getHtmlNameWithColor(), getHPEmojiMinus(-1));
            }

            // publicMsgObj.text = publicMsgObj.text + reult;
            // publicMsgObj.replyMakup = null;
            getGameManager().getMsgAPI().clearButtons(publicMsgObj);
            // sleep(1000);
            getGameManager().getIo().printlnPublic(reult, type);

            return true;
        }

    }

    /**
     * 预备阵亡
     * 
     * @param source
     */
    public void dying(Person source) {
        // Thread.dumpStack();
        // if (isDead) {
        // return;
        // }
        // getGameManager().addDeadAction(new Action((Person) this, source));

        int needTao = 1 - getHP();
        if (needTao <= 0) {
            return;
        }

        for (int i = 0; i < needTao; i++) {
            if (!askTao()) {
                die();
                selfDeadAction(source);
                return;
            } else {
                recover(null, 1);
            }
        }
    }

    public boolean checkShan() {

        for (Card c : getCards()) {
            if (c instanceof Shan) {
                return true;
            }
        }

        return false;
    }

    /**
     * 
     * @param needPublicNotice 是否需要在群中提醒
     * @return
     */
    public boolean requestShan(MsgObj publicMsgObj, boolean sendSingleMsg, Person source) {
        return requestShanCard(publicMsgObj, true, sendSingleMsg, source) != null;
    }

    /**
     * 
     * @param needPublicNotice 是否需要在群中提醒,是否要显示图片
     * @return
     */
    public boolean requestShan(MsgObj publicMsgObj, boolean showImg, boolean sendSingleMsg, Person source) {
        return requestShanCard(publicMsgObj, showImg, sendSingleMsg, source) != null;
    }

    /**
     * 
     * @param needPublicNotice 是否需要在群中提醒
     * @return
     */
    public Card requestShanCard(MsgObj publicMsgObj, boolean showImg, boolean sendSingleMsg, Person source) {
        MsgObj publicMsg;
        boolean needPublicNotice;
        if (publicMsgObj == null) {
            publicMsg = MsgObj.newMsgObj(getGameManager());
            putTempActionMsgObj("shan", publicMsg);
            needPublicNotice = true;
        } else {
            publicMsg = publicMsgObj;
            needPublicNotice = false;
        }

        boolean activeShan;

        if (hasEquipment(shield, "八卦阵") && ((Shield) getEquipments().get(shield)).isValid()) {
            if ((boolean) getEquipments().get(shield).use()) {
                Card shan = new Shan(getGameManager(), NOCOLOR, 0);
                sendShanNotice(publicMsg, shan, sendSingleMsg);
                return shan;
            }
        }
        if (!needPublicNotice) {
            activeShan = true;
        } else {
            if (this.isAI()) {
                activeShan = true;
                publicMsg.chatId = getGameManager().getChatId();
                if (showImg) {
                    ImgDB.setImg(publicMsg, toString());
                }
                publicMsg.text = Text.format("%s 请你出闪", ((Person) this).getHtmlName());
                getGameManager().getMsgAPI().sendImg(publicMsg);
            } else {

                activeShan = getGameManager().getMsgAPI().noticeAndAskPublic(
                        publicMsg,
                        (Person) this,
                        Text.format("%s 请你出闪", ((Person) this).getHtmlName()),
                        "打出闪", "shan", showImg);
                // 去除按键

                getGameManager().getMsgAPI().clearButtons(publicMsg);
                // System.out.println();
            }
        }
        if (!activeShan) {
            // sendCanNotSha(publicMsg);
            return null;
        }

        if (skillShan(source)) {
            Card shan = new Shan(getGameManager(), NOCOLOR, 0);
            sendShanNotice(publicMsg, shan, sendSingleMsg);
            return shan;
        }

        if (!checkShan()) {
            getGameManager().getIo().delaySendAndDelete(this, "💬你已没有 闪 ");
            return null;
        }
        getPriviMsg().clearHeader2();
        getPriviMsg().setOneTimeInfo1("\n💬请你出一张 闪");

        Card shan = requestCard("闪");
        if (shan != null) {
            // getGameManager().getIo().println(
            // Text.format("%s 我闪! 打出了一张%s", getHtmlName(), shan.getHtmlNameWithColor()),
            // "闪");

            sendShanNotice(publicMsg, shan, sendSingleMsg);
        }
        return shan;
    }

    private void sendShanNotice(MsgObj publicMsg, Card card, boolean sendSingleMsg) {
        if (sendSingleMsg) {
            String result = Text.format("%s %s !",
                    getPlateName(), card.getHtmlNameWithColor());
            // sleep(1000);
            getGameManager().getIo().printlnPublic(result, "闪");
        } else {
            String result = Text.format(",%s !",
                    card.getHtmlNameWithColor());
            publicMsg.appendText(result);
            // sleep(1000);
            getGameManager().getMsgAPI().editCaptionForce(publicMsg);
        }

    }

    // /**
    // * 单独给杀实现的闪
    // *
    // * @return
    // */
    // public boolean requestShanForSha() {
    // return requestShanCardForSha() != null;
    // }

    // /**
    // * 单独给杀实现的闪
    // *
    // * @return
    // */
    // public Card requestShanCardForSha() {

    // if (hasEquipment(shield, "八卦阵") && ((Shield)
    // getEquipments().get(shield)).isValid()) {
    // if ((boolean) getEquipments().get(shield).use()) {
    // // 这里不用输出，因为装备那边有输出
    // Card shan = new Shan(getGameManager(), NOCOLOR, 0);
    // return shan;
    // }
    // }
    // if (skillShan()) {
    // // 这里不用输出，因为技能那边有输出
    // Card shan = new Shan(getGameManager(), NOCOLOR, 0);
    // return shan;
    // }

    // if (!checkShan()) {
    // return null;
    // }
    // getPriviMsg().clearHeader2();
    // getPriviMsg().setOneTimeInfo1("\n💬请你出 闪");

    // //Card shan = requestCard("闪");//为什么
    // Card shan =requestShanCard();//为什么
    // if (shan != null) {
    // getGameManager().getIo().printlnPublic(
    // Text.format("%s 我闪! 打出了一张%s", getHtmlName(), shan.getHtmlNameWithColor()),
    // "闪");
    // }
    // return shan;
    // }

    /**
     * 是否能打出杀
     * 
     * @return
     */
    public boolean existsSha() {
        for (Card c : getCards()) {
            if (c instanceof Sha) {
                return true;
            }
        }

        return false;
    }

    /**
     * 请求杀
     * 
     * @return
     */
    public Sha requestSha(Person source) {
        return requestSha(source, true);
    }

    public Sha requestSha(Person source, boolean showImg) {

        MsgObj publicMsg = MsgObj.newMsgObj(getGameManager());
        putTempActionMsgObj("sha", publicMsg);
        boolean activeSha;

        if (this.isAI()) {
            activeSha = true;
            publicMsg.chatId = getGameManager().getChatId();
            if (showImg) {
                ImgDB.setImg(publicMsg, toString());
            }

            publicMsg.text = Text.format("%s 请出杀", ((Person) this).getHtmlName());
            getGameManager().getMsgAPI().sendImg(publicMsg);
        } else {
            activeSha = launchSkillPublicDeepLink(
                    publicMsg,
                    "打出杀",
                    Text.format("%s 请出杀", ((Person) this).getHtmlName()),
                    "sha", showImg);
            // 去除按键

            getGameManager().getMsgAPI().clearButtons(publicMsg);
            // System.out.println();
        }

        if (!activeSha) {
            sendCanNotSha(publicMsg);
            return null;
        }

        if (hasEquipment(weapon, "丈八蛇矛") && getCards().size() >= 2) {
            getPriviMsg().setOneTimeInfo1(Text.format("💬是否使用 %s", DescUrl.getDescHtml("丈八蛇矛")));
            if (launchSkillPriv("丈八蛇矛")) {
                Card sha = (Sha) getEquipments().get(weapon).use();
                if (sha != null) {
                    sendShaNotice(publicMsg, sha);
                } else {
                    sendCanNotSha(publicMsg);
                }
                return (Sha) sha;
            }

        }
        if (skillSha(source)) {
            Sha sha = new Sha(getGameManager(), Color.NOCOLOR, 0);
            sendShaNotice(publicMsg, sha);
            return sha;
        }
        if (!existsSha()) {
            getGameManager().getIo().delaySendAndDelete(this, "💬你已没有 杀");
            sendCanNotSha(publicMsg);
            return null;
        }
        getPriviMsg().clearHeader2();
        getPriviMsg().setOneTimeInfo1("\n💬请你出一张 杀");
        Card sha = requestCard("杀");
        if (sha != null) {
            sendShaNotice(publicMsg, sha);
        } else {
            sendCanNotSha(publicMsg);
        }
        return (Sha) sha;
    }

    private void sendShaNotice(MsgObj publicMsg, Card card) {
        String result = Text.format(",%s!",
                card.getHtmlNameWithColor());
        publicMsg.text = publicMsg.text + result;
        // sleep(1000);
        getGameManager().getMsgAPI().editCaptionForce(publicMsg);
    }

    private void sendCanNotSha(MsgObj publicMsg) {
        // String result = Text.format("\n%s 未打出杀",
        // getPlateName());
        // publicMsg.text = publicMsg.text + result;
        // getGameManager().getMsgIO().editCaption(publicMsg);
    }

    // public boolean requestWuXie() {
    // if (skillWuxie()) {
    // return true;
    // }
    // if(!singleCheckWuxie()){
    // return false;
    // }
    // return requestCard("无懈可击") != null;
    // }

    // ------------增加一个需要竞争的

    public Card requestWuXie(AtomicReference<PlayerIO> throwedPerson, MsgObj privMsgObj) {
        stepInPriv(privMsgObj);
        try {
            if (!singleCheckWuxie()) {
                return null;
            }
            Boolean wuxieStop = (Boolean) privMsgObj.getReturnAttr(ReturnType.wuxieStop, 0);
            if (wuxieStop != null && wuxieStop == true) {
                return null;
            }
            Card skillWuxie = skillWuxie(throwedPerson, privMsgObj);
            if (skillWuxie != null) {
                return skillWuxie;
            }
            if (getPriviMsg().getOneTimeInfo1().length() == 0) {
                getPriviMsg().setOneTimeInfo1("💬请出一个 无懈可击");
            }
            wuxieStop = (Boolean) privMsgObj.getReturnAttr(ReturnType.wuxieStop, 0);// 这里要再判一次
            if (wuxieStop != null && wuxieStop == true) {
                return null;
            }
            if (privMsgObj.isDeleted) {// 这里要重置一下，有可能之前那个被删除了，要不后面的删除不了
                privMsgObj.isDeleted = false;
            }
            // 为什么需要这个重置呢，因为不清空，如果有技能无懈，后面的requestCard 就设不进去ChooseOneCard
            privMsgObj.resetAttributes(ReturnType.ChooseOneCard);
            Card c = requestCard("无懈可击", throwedPerson, privMsgObj);
            // System.out.println("这里的c=" + c);
            return c;
        } finally {
            stepOutPriv(privMsgObj);
        }
    }

    /**
     * 看单个用户是否有无懈可击
     * 
     * @return
     */
    public boolean singleCheckWuxie() {
        if (hasWuXieReplace()) {
            // hasWuXie = true;
            // break;
            return true;
        }
        for (Card c : getCards()) {
            if (c.toString().equals("无懈可击")) {
                // hasWuXie = true;
                // break;
                return true;
            }
        }
        return false;
    }

    /**
     * 通过 publicMsgObj 请求全体的出卡
     * 
     * @param publicMsgObj
     * @param showCardPerson
     * @param card_name
     */
    public void requestCardForPublic(MsgObj publicMsgObj,
            AtomicReference<PlayerIO> showCardPerson, AtomicReference<Card> showCard,
            String card_name, String buttonText, String buttonValue) {
        requestCardForPublic(publicMsgObj,
                showCardPerson, showCard,
                card_name, buttonText, buttonValue, null);
    }

    /**
     * 通过 publicMsgObj 请求全体的出卡 多一个附加回调
     * 
     * @param publicMsgObj
     * @param showCardPerson
     * @param showCard
     * @param card_name
     * @param buttonText
     * @param buttonValue
     * @param additionGeCard
     */
    public void requestCardForPublic(MsgObj publicMsgObj,
            AtomicReference<PlayerIO> showCardPerson, AtomicReference<Card> showCard,
            String card_name, String buttonText, String buttonValue, CallbackEven additionGeCard) {

        List<Person> showCardPersonList = new CopyOnWriteArrayList<>();
        publicMsgObj.sonMsgs = ConcurrentHashMap.newKeySet();
        publicMsgObj.sonCallback = new ConcurrentHashMap<>();
        Set<Person> giveUpPersons = ConcurrentHashMap.newKeySet();
        Set<Person> deepLinkPersons = ConcurrentHashMap.newKeySet();

        putTempActionMsgObj(buttonValue, publicMsgObj);// 方便别人改消息
        publicMsgObj.setImg(this.toString());
        publicMsgObj.chatId = getGameManager().getChatId();

        MyCountDownLatch latch = MyCountDownLatch.newInst(publicMsgObj.forPlayers.size(), getGameManager());

        for (Person p : publicMsgObj.forPlayers) {
            p.getPriviMsg().clearHeader2();
            // p.getInText().clearInfo1();
            // p.getInText().clearInfo2();
            p.getPriviMsg().setOneTimeInfo1(Text.format("%s 请你出 %s", this.getPlateName(), card_name));
            MsgObj sonMsgObj = MsgObj.newMsgObj(getGameManager());
            publicMsgObj.sonMsgs.add(sonMsgObj);
            publicMsgObj.sonCallback.put(p.getUser().user_id, new CallbackEven() {
                // 这里是一个回调，用户点击deeplink按钮后来到这里
                @Override
                public void DeepLinkExec(MsgObj thiMsgObj, String callBackData) {
                    try {
                        if ("q".equals(callBackData)) {

                            BlockingMap<Long, String> callbackIds = thiMsgObj.callbackIds;
                            String thisCallBackId = callbackIds.get(p.getUser().user_id, 10, TimeUnit.SECONDS);
                            if (thisCallBackId == null) {
                                // System.out.println("thisCallBackId 为空，不正常");
                                return;
                            }

                            if (!giveUpPersons.contains(p) && !deepLinkPersons.contains(p)) {
                                giveUpPersons.add(p);
                                latch.countDown();
                                getGameManager().getMsgAPI().answerCallBack(
                                        thisCallBackId, "❌ 你已放弃", false);
                            } else {
                                getGameManager().getMsgAPI().answerCallBack(
                                        thisCallBackId, "❌ 你已操作过", false);
                            }
                        } else if (buttonValue.equals(callBackData)) {
                            if (deepLinkPersons.contains(p) || giveUpPersons.contains(p)) {
                                return;
                            }
                            deepLinkPersons.add(p);

                            Card c;
                            if ("桃".equals(card_name)) {
                                c = p.requestTao(card_name, showCardPerson, sonMsgObj);
                            } else {
                                c = p.requestCard(card_name, showCardPerson, sonMsgObj);
                            }

                            if (additionGeCard != null && c == null) {
                                c = additionGeCard.additionGetCardExec(c, p, sonMsgObj);
                            }
                            showCard.set(c);
                            if (c != null) {
                                showCardPersonList.add(p);
                                // 如果这个给出了牌，直接解锁
                                while (getGameManager().isRunning() && latch.getCount() != 0) {
                                    latch.countDown();
                                }
                                for (MsgObj priMsg : publicMsgObj.sonMsgs) {
                                    priMsg.setAttributes(ReturnType.doneAskCardPublic, card_name);
                                    getGameManager().getMsgAPI().delMsg(priMsg);
                                }
                            } else {
                                if (!giveUpPersons.contains(p)) {
                                    giveUpPersons.add(p);
                                    latch.countDown();
                                }
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

            });
        }

        getGameManager().getMsgAPI().noticeAndAskOneForMulty(publicMsgObj, buttonText, buttonValue);

        latch.await(Config.PUBLIC_NOTICE_TIME_25S, TimeUnit.MILLISECONDS);

        // 删掉已发出到私人的消息
        for (MsgObj priMsg : publicMsgObj.sonMsgs) {
            priMsg.setAttributes(ReturnType.doneAskCardPublic, card_name);
            getGameManager().getMsgAPI().delMsg(priMsg);
        }
    }

    /**
     * 检查是否能打出桃
     * 
     * @return
     */
    public boolean checkTao() {
        for (Card c : getCards()) {
            if (c instanceof Tao) {
                return true;
            }
        }

        return false;
    }

    public Card requestTao(String type, AtomicReference<PlayerIO> throwedPerson, MsgObj inMsg) {
        if (!checkTao()) {
            getGameManager().getIo().delaySendAndDelete(this, "💬你已没有 桃 ");
            return null;
        }
        getPriviMsg().clearHeader2();
        getPriviMsg().setOneTimeInfo1("\n💬请你出一张 桃");
        return requestCard("桃", throwedPerson, inMsg);
    }

    /**
     * 检查是否能打出酒
     * 
     * @return
     */

    public boolean checkJiu() {
        for (Card c : getCards()) {
            if (c instanceof Jiu) {
                return true;
            }
        }
        if (getActiveSkills().contains("酒池")) {
            return true;
        }
        // 假装他思考
        // sleep(3000);
        return false;
    }

    public boolean requestJiu() {
        if (!checkJiu()) {
            getGameManager().getIo().delaySendAndDelete(this, "💬你已没有 酒");
            return false;
        }
        getPriviMsg().clearHeader2();
        getPriviMsg().setOneTimeInfo1("\n💬请你出一张 酒");
        return requestCard("酒") != null;
    }

    public boolean canNotBeSha(Sha sha, Person p) {
        if (hasEquipment(shield, "藤甲") && ((Shield) getEquipments().get(shield)).isValid()) {
            if (sha.getType() == HurtType.normal) {

                getGameManager().getIo().printlnPublic(
                        Text.format("%s 装备藤甲,%s 的 %s 无效，",
                                getPlateName(),
                                sha.getSource().getHtmlName(),
                                sha.getHtmlNameWithColor()),
                        "藤甲");

                return true;
            }
        }
        if (hasEquipment(shield, "仁王盾") && ((Shield) getEquipments().get(shield)).isValid()) {
            boolean black = sha.isBlack();
            if (black) {

                getGameManager().getIo().printlnPublic(
                        Text.format("%s 装备仁王盾,%s 的黑色 %s 无效，",
                                getPlateName(),
                                sha.getSource().getHtmlName(),
                                sha.getHtmlName()),
                        "仁王盾");
            }
            return black;
        }
        return false;
    }

    /**
     * 返回手牌sha的数量
     * 
     * @return
     */
    public int getShaNum() {
        int num = 0;
        for (Card c : cards) {
            if (c instanceof Sha) {
                num++;
            }
        }
        return num;
    }

    @Override
    public ArrayList<Card> getExtraCards() {
        return null;
    }

    public void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
