package sanguosha.cards.basic;

import sanguosha.cards.BasicCard;
import sanguosha.cards.Card;
import sanguosha.cards.Color;
import sanguosha.cards.equipments.Shield;
import sanguosha.cards.equipments.Weapon;
import sanguosha.manager.GameManager;
import sanguosha.people.AI;
import sanguosha.people.Person;
import sanguosha.people.forest.DongZhuo;
import components.TimeLimit;

import static sanguosha.cards.Color.NOCOLOR;
import static sanguosha.cards.EquipType.shield;
import static sanguosha.cards.EquipType.weapon;
import java.util.ArrayList;

import java.util.List;

import config.Config;
import config.DescUrl;
import config.Text;
import db.ImgDB;
import msg.MsgObj;

public class Sha extends BasicCard {
    private final HurtType type;
    /** 朱雀羽扇 */
    private boolean useZhuQueYuShan = false;
    private int multiSha = 1;
    private List<Person> targets = new ArrayList<>();

    public Sha(GameManager gameManager, Color color, int number, HurtType type) {
        super(gameManager, color, number);
        this.type = type;
    }

    public Sha(GameManager gameManager, Color color, int number) {
        this(gameManager, color, number, HurtType.normal);
    }

    /*
     * 设置是否是因技能多出来的的杀
     */
    public void setMultiSha(int multiSha) {
        this.multiSha = multiSha;
    }

    /**
     * 获取多角色
     * @return
     */
    public List<Person> getTargets() {
        return this.targets;
    }

    /*
     * 是否是因技能多出来的的杀
     */
    public int getMultiSha() {
        return this.multiSha;
    }

    public boolean useWeapon(String s, Person target,Card sourceCard) {
        // AI不使用武器
        if (getSource() != null && getSource().isAI()) {
            return false;
        }
        if (getSource().hasEquipment(weapon, s)) {
            // if (s.equals("青釭剑") || getSource().chooseNoNull("使用 青釭剑" + s, "取消(Esc)")==1)
            // {
            getSource().getEquipments().get(weapon).setSource(getSource());
            getSource().getEquipments().get(weapon).setTarget(target);
     
            Weapon w=(Weapon)getSource().getEquipments().get(weapon);

            Object result = w.use(sourceCard);
            if (result instanceof Boolean) {
                return (Boolean) result;
            } else {
                return result != null;
            }

            // }
        }
        return false;
    }

    public void shaHit(MsgObj msgObj, Person target) {
        int numHurt = 1;
        if (getSource().isDrunk()) {
            numHurt++;
        }
        if (getSource().hasEquipment(weapon, "古锭刀") && target.getCards().isEmpty()) {
            numHurt++;
        }
        if (getSource().isNaked()) {
            numHurt++;
        }
        HurtType hurtType = useZhuQueYuShan ? HurtType.fire : type;
        boolean isPreLink = target.isLinked();// 为什么要这样缓存，因为已伤害了这个isLinked()的值就会变
        target.putTempActionMsgObj("hurt", msgObj);
        int realNum = target.hurt(getReplaceCards(), getSource(), numHurt, hurtType);

        if (isPreLink && type != HurtType.normal) {
            getGameManager().linkHurt(msgObj, getReplaceCards(), getSource(), target, realNum, hurtType);
            getGameManager().getMsgAPI().editCaptionForce(msgObj);
            // sleep(3000);
        }

        getSource().shaSuccess(target);
        if (!target.checkDead()) {
            for (Person p : getGameManager().getPlayersBeginFromPlayer(getSource())) {
                p.otherPersonHurtBySha(getSource(), target);
            }
        }
    }

    @Override
    public Object use() {
        //Thread.dumpStack();
        getSource().setShaCount(getSource().getShaCount() + 1);

        if (multiSha <= 1) {
            return singleUse(getTarget());
        } else {
            // System.out.println(targets);
            return multiUse(targets);
        }
    }

    public boolean multiUse(List<Person> personList) {
        if (targets.size() == 0) {
            return false;
        }
        for (Person p : personList) {
            singleUse(p);
        }
        return true;
    }

    /**
     * 
     * @param targetReal 这个target
     *                   可能在sha里面会变,只能用局部变量，因为这个taget可能是多个，和card的本身的taget对象不一样
     * @return
     */
    public boolean singleUse(Person target) {

        getSource().shaBegin(this);
        Person changePerson = target.changeSha(this);
        Person targetReal;
        if (changePerson == null) {
            targetReal = target;
        } else {
            targetReal = changePerson;
        }

        targetReal.gotShaBegin(this);

        if (type == HurtType.normal && useWeapon("朱雀羽扇", targetReal,this)) {
            useZhuQueYuShan = true;
        }
        //
        
        //useWeapon("雌雄双股剑", targetReal);
        useWeapon("诸葛连弩", targetReal,this);//只是单纯打印口号
        useWeapon("雌雄双股剑", targetReal,this);
        useWeapon("青釭剑", targetReal,this);

        if (targetReal.canNotBeSha(this, getSource())) {
            // getSource().println("invalid sha");
            return false;
        }

        boolean needDouble = (getSource().hasRouLin() && targetReal.getSex().equals("female"))
                || (targetReal.hasRouLin() && getSource().getSex().equals("female"))
                || getSource().hasWuShuang();

        if (getSource().hasWuShuang()) {
            getSource().wushuangSha();
        }

        // =========这里插入在公开群的点击连接过来
        MsgObj publicMsg = MsgObj.newMsgObj(getGameManager());
        targetReal.putTempActionMsgObj("sha", publicMsg);
        targetReal.putTempActionMsgObj("hurt", publicMsg);
        boolean activeShan;
        boolean shaCanBeShan = getSource().shaCanBeShan(target);

        if (shaCanBeShan) {
            if (targetReal.isAI()) {
                activeShan = true;
                publicMsg.chatId = getGameManager().getChatId();
                ImgDB.setImg(publicMsg, toString());
                publicMsg.text = Text.format("%s,%s 对你 %s",

                        targetReal.getHtmlName(),
                        getSource().getPlateName(),
                        getHtmlNameWithColor(), needDouble ? "2" : "1",
                        "<b><a href=\"" + DescUrl.getDescUrl("闪") + "\">闪</a></b>");
                getGameManager().getMsgAPI().sendImg(publicMsg);
            } else {
                activeShan = getGameManager().getMsgAPI().noticeAndAskPublic(
                        publicMsg,
                        this,
                        targetReal,
                        Text.format("%s,%s 对你 %s",
                                targetReal.getHtmlName(),
                                getSource().getPlateName(),
                                getHtmlNameWithColor(), needDouble ? "2" : "1",
                                "<b><a href=\"" + DescUrl.getDescUrl("闪") + "\">闪</a></b>"),
                        "打出闪", "shan");
                // 去除按键
                getGameManager().getMsgAPI().clearButtons(publicMsg);
            }

        } else {
            activeShan = false;
            publicMsg.chatId = getGameManager().getChatId();
            ImgDB.setImg(publicMsg, toString());
            publicMsg.text = Text.format("%s,%s 对你 %s ,且不可闪",
                    targetReal.getHtmlName(),
                    getSource().getPlateName(),
                    getHtmlNameWithColor());
            getGameManager().getMsgAPI().sendImg(publicMsg);
        }

        /**
         * 闪逻辑
         */
        boolean shanlogic;
        // 这里要分步写，要不提示出不来
        if (activeShan) {// 能闪
            if (needDouble) {
                targetReal.getPriviMsg().clearHeader2();
                if (getSource() instanceof DongZhuo) {
                    targetReal.getPriviMsg().setOneTimeInfo1("\n💬对方是 董卓：锁定技，董卓 对女性角色使用的[杀]需使用两张[闪]才能抵消。");
                } else {
                    targetReal.getPriviMsg().setOneTimeInfo1("\n💬肉林：女性角色对你使用的[杀]需使用两张[闪]才能抵消。");
                }

                // Card shan1 = targetReal.requestShanCardForSha();
                Card shan1 = targetReal.requestShanCard(publicMsg, true, true, getSource());

                if (shan1 != null) {
                    if (getSource() instanceof DongZhuo) {
                        targetReal.getPriviMsg().setOneTimeInfo1("\n💬对方是 董卓：对女性角色使用的[杀]需使用两张[闪]才能抵消,你仍需要打出1张 [闪]。");
                    } else {
                        targetReal.getPriviMsg().setOneTimeInfo1("\n💬肉林：女性角色对你使用的[杀]需使用两张[闪]才能抵消,你仍需要打出1张 [闪]。");
                    }
                    // if (!targetReal.requestShanForSha()) {// 第2个闪没打出来
                    if (!targetReal.requestShan(publicMsg, true, getSource())) {// 第2个闪没打出来
                        // 没选两张用出去的1张要归还人家
                        shanlogic = false;
                        if (shan1.color() != NOCOLOR) {// 有的是虚拟出来的闪
                            getGameManager().getCardsHeap().retrieve(shan1);
                            targetReal.addCard(shan1);
                        }

                    } else {
                        shanlogic = true;
                    }
                } else { // 第一个闪没打出来
                    shanlogic = false;
                }
            } else {// 不需要双闪
                // if (!targetReal.requestShanForSha()) {
                if (!targetReal.requestShan(publicMsg, true, getSource())) {
                    shanlogic = false;
                } else {
                    shanlogic = true;
                }
            }
        } else {
            shanlogic = false;
        }

        if (shanlogic) {// 被闪了
            if (useWeapon("贯石斧", targetReal,this)) {
                MsgObj guanShifuMsg =getSource().getTempActionMsgObjFirstOrder(publicMsg, "guanshifu");
                shaHit(guanShifuMsg, targetReal);
            } else {
                getSource().shaGotShan(targetReal);
                useWeapon("青龙偃月刀", targetReal,this);
            }
        } else if (!useWeapon("寒冰剑", targetReal,this)) {
            useWeapon("麒麟弓", targetReal,this);
            shaHit(publicMsg, targetReal);
            useWeapon("三尖两刃刀", targetReal,this);
        }

        if (targetReal.hasEquipment(shield, null)) {
            ((Shield) targetReal.getEquipments().get(shield)).setValid(true);
        }
        if (!targetReal.hasBaZhen()) {
            targetReal.setBaZhen(true);
        }
        if (getSource().isDrunk()) {
            getSource().setDrunk(false);
            if (!getSource().isDrunkShaUsed()) {
                getSource().setDrunkShaUsed(true);
            }
        }
        useZhuQueYuShan = false;
        return true;

    }

    @Override
    public String toString() {
        if (type == HurtType.normal) {
            return "杀";
        } else if (type == HurtType.fire) {
            return "火杀";
        } else {
            return "雷杀";
        }
    }

    public HurtType getType() {
        return type;
    }

    @Override
    public boolean needChooseTarget() {
        return true;
    }

    @Override
    public boolean askTarget(Person user) {
        targets.clear();
        this.setSource(user);
        if (multiSha <= 1) {
            return selectOne(user);
        } else {
            return selectMulty(user);
        }

    }

    private boolean selectOne(Person user) {
        TimeLimit t = new TimeLimit(Config.PRIV_RND_TIME_60S);
        String preText = null;
        while (getGameManager().isRunning() && t.isNotTimeout()) {
            String result;
            if (preText == null) {
                
                result = Text.format("\n💬你使用了 %s,请选择在你攻击范围内的一位玩家\n",toString());
            } else {
                result = preText;
            }
            user.getPriviMsg().setOneTimeInfo1(result);
            // }

            List<Person> canHitPerson = new ArrayList<>();

            for (Person p : getGameManager().getPlayersBeginFromPlayer(user)) {

                if (p == user) {
                    continue;
                } else if (getGameManager().calDistance(user, p) > user.getShaDistance()) {
                    continue;
                } else if (p.hasKongCheng() && p.getCards().isEmpty()) {
                    continue;
                }
                canHitPerson.add(p);
            }
            if (canHitPerson.size() == 0) {

                try {
                    return false;
                } finally {
                    String res = Text.format("没有你可[杀]的对象");
                    getGameManager().getIo().delaySendAndDelete(user, res);
                }
            }
            Person p = user.selectPlayerExept(canHitPerson);
            if (p == null) {
                return false;
            }
            setTarget(p);

            return true;
        }
        return false;
    }

    private boolean selectMulty(Person user) {
        TimeLimit t = new TimeLimit(Config.PRIV_RND_TIME_60S);
        String preText = null;
        while (getGameManager().isRunning() && t.isNotTimeout()) {
            String result;
            if (preText == null) {
                result = Text.format("\n💬你使用了 杀,在你攻击范围内可以选择最多 %s 位玩家", multiSha + "");
            } else {
                result = preText;
            }
            user.getPriviMsg().setOneTimeInfo1(result);
            // }

            List<Person> canHitPerson = new ArrayList<>();

            for (Person p : getGameManager().getPlayersBeginFromPlayer(user)) {
                if (p == user) {
                    continue;
                } else if (getGameManager().calDistance(user, p) > user.getShaDistance()) {
                    continue;
                } else if (p.hasKongCheng() && p.getCards().isEmpty()) {
                    continue;
                }
                canHitPerson.add(p);
            }
            if (canHitPerson.size() == 0) {
                try {
                    return false;
                } finally {
                    String res = Text.format("没有你可[杀]的对象");
                    getGameManager().getIo().delaySendAndDelete(user, res);
                }
            }

            List<Person> results = user.chooseManyFromProvided(0, canHitPerson);

            if (results.size() == 0) {
                return false;
            }

            if (results.size() > multiSha) {
                preText = Text.format("\n💬您不能选择超过 %s 个目标", multiSha + "");
                continue;
            }
            targets.addAll(results);
            return true;
        }
        return false;
    }

    @Override
    public String help() {
        return "使用时机：出牌阶段限一次。\n" +
                "使用目标：你攻击范围内的一名角色。\n" +
                "作用效果：你对目标角色造成1点" +
                (type == HurtType.fire ? "火焰" : type == HurtType.thunder ? "雷电" : "") + "伤害。";
    }

    public static String hurtTypeString(HurtType type) {
        switch (type) {
            case thunder:
                return "⚡️";
            case fire:
                return "🔥";
            case normal:
                return "🗡";
            default:
                return "🗡";
        }
    }

   
}
