package sanguosha.people;

import sanguosha.cards.Card;
import sanguosha.cards.Color;
import sanguosha.cards.Equipment;
import sanguosha.cards.Strategy;
import sanguosha.cards.basic.HurtType;
import sanguosha.cards.basic.Sha;
import sanguosha.manager.GameManager;
import sanguosha.manager.Status;
import sanguosha.manager.Utils;
import components.TimeLimit;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import config.Config;
import config.Text;
import msg.MsgObj;

import java.util.HashSet;
import java.util.Collections;

import static sanguosha.cards.EquipType.weapon;
import static sanguosha.people.Identity.REBEL;

/**
 * AI是不释放技能的
 */
public class AI extends Person {
    private Person basicPlayer;
    private Person primaryEnemy = null;

    // private boolean continuteUseCard;
    private String outName = null;

    public AI(GameManager gameManager) {

        super(4, Nation.QUN);
        setGameManager(gameManager);
    }

    public void setOutName(String outName) {
        this.outName = outName;
    }

    @Override
    public void selfBeginPhase() {
        if (getIdentity() == Identity.REBEL) {
            primaryEnemy = getGameManager().getKing();
        } else if (getIdentity() == Identity.TRAITOR) {
            boolean attackKing = true;
            for (Person p : getGameManager().getPlayers()) {
                // 有忠臣或反贼就不攻击主公
                if (p.getIdentity() == Identity.MINISTER || p.getIdentity() == REBEL) {
                    attackKing = false;
                }
            }

            if (attackKing) {
                primaryEnemy = getGameManager().getKing();
            } else {
                List<Person> tempList = new ArrayList<>(getGameManager().getPlayers());
                Collections.shuffle(tempList);
                for (Person p : tempList) {
                    if (p.getIdentity() == Identity.KING || p == this) {
                        continue;
                    }
                    primaryEnemy = p;
                    break;
                }
            }

        } else {// 主公和忠臣
            List<Person> tempList = new ArrayList<>(getGameManager().getPlayers());
            Collections.shuffle(tempList);
            for (Person p : tempList) {
                if (p.getIdentity() == Identity.KING || p == this) {
                    continue;
                }
                primaryEnemy = p;
                break;
            }
        }
    }

    @Override
    public void setGameManager(GameManager gameManager) {
        if (basicPlayer != null) {
            basicPlayer.setGameManager(gameManager);
            super.setGameManager(gameManager);
        } else {
            super.setGameManager(gameManager);
        }
    }

    // @Override
    // public int hurt(List<Card> cs, Person source, int num, HurtType type) {
    // //得加上 要不有的技能不会触发，加上了之后又不会触发死亡...
    // 因为这个方式涉及到playe列表的更改，用basicPlayer 执行会出错
    // if (basicPlayer != null) {
    // return basicPlayer.hurt(cs, source, num, type);
    // } else {
    // return super.hurt(cs, source, num, type);
    // }
    // }

    /**
     * 设置主要敌人
     * 
     * @return
     */
    public Person getPrimaryEnemy() {
        return primaryEnemy;
    }

    /**
     * 获取主要敌人
     * 
     * @return
     */
    public void setPrimaryEnemy(Person primaryEnemy) {
        this.primaryEnemy = primaryEnemy;
    }

    @Override
    public void gotHurt(List<Card> cards, Person p, int num) {
        primaryEnemy = p;
        if (basicPlayer != null) {
            basicPlayer.gotHurt(cards, p, num);
        } else {
            super.gotHurt(cards, p, num);
        }
    }

    // private void stupidUsePhase() {
    // int index = 1;
    // while (true) {
    // printlnToIO(this + "'s current hand cards: ");
    // printCards(getCards());
    // try {
    // Thread.//sleep(5000);
    // } catch (InterruptedException e) {
    // break;
    // }
    // if (index <= getCards().size()) {
    // if (!parseOrder(index + "")) {
    // index++;
    // }
    // } else {
    // break;
    // }
    // }
    // }

    private void use(Card c, Person target) {
        c.setSource(this);
        c.setTarget(target);
        boolean used = useCard(c);
        if (c.isNotTaken() && used) {
            throwCard(c);
            // } else if (!c.isNotTaken()) {
        } else {
            c.setTaken(false);
        }
        // continuteUseCard = true;
    }

    private boolean sleep() {
        try {
            Thread.sleep(5000);
            return true;
        } catch (InterruptedException e) {
            return false;
        }
    }

    private boolean sleep(int time) {
        try {
            Thread.sleep(time);
            return true;
        } catch (InterruptedException e) {
            return false;
        }
    }

    /**
     * 如果使用成功就等待2秒，不使用则不计时
     * 
     * @param c
     * @return
     */
    private boolean useAI(Card c) {
        if (primaryEnemy == null) {
            return false;
        }
        switch (c.toString()) {
            case "杀":
                // System.out.println(getShaCount());
                if (getGameManager().calDistance(this, primaryEnemy) <= this.getShaDistance()
                        && !primaryEnemy.hasKongCheng() && (getShaCount() < getMaxShaCount()
                                || hasEquipment(weapon, "诸葛连弩"))) {
                    use(c, primaryEnemy);
                    return true;
                }
                return false;
            case "过河拆桥": // fallthrough
            case "火攻": // fallthrough
            case "顺手牵羊": // fallthrough
            case "乐不思蜀": // fallthrough
            case "兵粮寸断": // fallthrough
            case "决斗": // fallthrough
                if (skillCheck(c, primaryEnemy)) {
                    use(c, primaryEnemy);
                    return true;
                }
                return false;
            case "桃":
                if (getHP() < getMaxHP()) {
                    use(c, this);
                    return true;
                }
                return false;
            case "铁索连环":
                loseCard(c);
                drawCard();
                // printlnPriv(this + " 重铸 铁索连环");
                return true;
            case "酒":
                if (!isDrunk() && !isDrunkShaUsed()) {
                    use(c, this);
                    return true;
                }
                return false;
            case "闪电": // fallthrough
            case "无中生有": // fallthrough
            case "南蛮入侵": // fallthrough
            case "万箭齐发": // fallthrough
            case "五谷丰登": // fallthrough
            case "桃园结义":
                use(c, this);
                return true;
            default:
                return false;
        }
    }

    private boolean skillCheck(Card c, Person p) {
        // 先存一个状态
        boolean isSourceNull = c.getSource() == null;
        if (isSourceNull) {
            c.setSource(this);
        }
        if (c.isBlack() && p.hasWeiMu()) {
            // user.printlnToIO("can't use that because of 帷幕");
            return false;
        }
        if (c instanceof Strategy) {
            if (getGameManager().calDistance(this, p) > ((Strategy) c).getDistance()) {

                return false;
            }
            if (!((Strategy) c).asktargetAddition(this, p)) {

                return false;
            }
        }
        return true;

    }

    @Override
    public void usePhase(boolean fastMode) {
        changeToBlank();
        if (blankPerson != null) {
            blankPersonRunOnce(fastMode);
            return;
        }

        TimeLimit t = new TimeLimit(Config.PRIV_ROUND_TIME_180S);
        while (getGameManager().isRunning() && t.isNotTimeout()) {
            // continuteUseCard = false;
            if (!sleep()) {
                break;
            }
            // 这先换成一个set避免迭代器结构改变的情况
            Set<Card> cards = new HashSet<>(getCards());

            for (Card c : cards) {
                // System.out.println(this + " usecard :" + c);
                // Set<Card> oldCards = new HashSet<>(getCards());
                if (c instanceof Equipment) {
                    putOnEquipment(c);
                    sleep(3000);
                } else {
                    if (useAI(c)) {
                        sleep(3000);
                    }
                }
                if (getGameManager().status() == Status.end || checkDead()) {
                    return;
                }
            }
            // //sleep(5000);
            // 折腾半天还是那几张牌
            if (sameCards(getCards(), cards)) {
                break;
            }
            System.out.println(this + " usecard");
        }
    }

    public int throwPhase(boolean fastMode) {

        int num = getCards().size() - Math.max(getHP(), 0);
        // System.out.println(this + " card:" + getCards().size() + " hp:" + getHP());
        if (num > 0) {
            // printlnToIO(Text.format("You need to throw %d cards", num));

            ArrayList<Card> cs;
            // 快速模式直接丢掉前面面的牌
            cs = new ArrayList<>();//
            for (int i = 0; i < num; i++) {
                cs.add(getCards().get(i));
            }

            loseCard(cs);
            for (Person p : getGameManager().getPlayersBeginFromPlayer(this)) {
                p.otherPersonThrowPhase(this, cs);
            }
        }

        return num;

    }

    private boolean sameCards(List<Card> newList, Set<Card> OldSet) {
        if (newList.size() != OldSet.size()) {
            return false;
        }
        for (Card c : newList) {
            if (!OldSet.contains(c)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Card requestRedBlack(String color, boolean fromEquipments) {
        try {
            sleep(3000);
            ArrayList<Card> options;
            if (fromEquipments) {
                options = getCardsAndEquipments();
            } else {
                options = getCards();
            }
            for (Card c : options) {
                if ((c.isRed() && color.equals("red")) || (c.isBlack() && color.equals("black"))) {
                    // printlnPriv(this + " chooses " + c);
                    loseCard(c);
                    return c;
                }
            }

            return null;
        } finally {
            sleep(3000);
        }
    }

    @Override
    public Card requestColor(Color color, boolean fromEquipments) {
        try {
            sleep(3000);
            ArrayList<Card> options;
            if (fromEquipments) {
                options = getCardsAndEquipments();
            } else {
                options = getCards();
            }
            for (Card c : options) {
                if (c.color() == color) {
                    // printlnPriv(this + " chooses " + c);
                    loseCard(c);
                    return c;
                }
            }

            return null;
        } finally {
            sleep(3000);
        }
    }

    @Override
    public Card requestCard(String type) {
        try {
            sleep(3000);
            if (type != null && (type.equals("无懈可击") || type.equals("桃"))) {
                // println(this + " 取消(Esc)");
                return null;
            }
            for (Card c : getCards()) {
                if (c.toString().equals(type)) {
                    // printlnPriv(this + " chooses " + c);
                    loseCard(c);
                    return c;
                }
            }

            // println(this + " 取消(Esc)");
            return null;
        } finally {
            sleep(3000);
        }
    }

    @Override
    public <E> int chooseFromProvided(MsgObj prMsgObj, boolean canBeNull, List<E> choices) {
        try {
            int option = Utils.randint(0, choices.size());// 这里没错
            if (option == choices.size()) {
                // println(this + " ai chooseFromProvided 取消(Esc)");
                if (canBeNull) {
                    return -1;
                } else {
                    return 0;
                }

            }

            // println(this + " chooses option " + option);
            return option;
        } finally {
            sleep(3000);
        }
    }

    @Override
    public <E> ArrayList<E> chooseManyFromProvided(int num, List<E> choices) {
        try {
            // println(this + " chooses options 1 - " + num);
            return new ArrayList<>(choices.subList(0, num));
        } finally {
            sleep(3000);
        }
    }

    @Override
    public String name() {
        return Text.format("AI[%s]", outName);
    }

    @Override
    public Set<String> getActiveSkills() {
        if (basicPlayer != null) {
            return basicPlayer.getActiveSkills();
        } else {
            return super.getActiveSkills();
        }

    }

    /**
     * 获取带html注释url的名字
     * 
     * @return
     */
    @Override
    public String getHtmlName() {
        if (basicPlayer != null) {
            // System.out.println("AI[<b><a
            // href=\""+basicPlayer.getUrl()+"\">"+basicPlayer.toString()+"</a></b>]");
            return "AI[<b><a href=\"" + basicPlayer.getUrl() + "\">" + basicPlayer.toString() + "</a></b>]";
        } else {
            return "<b><a href=\"" + getUrl() + "\">" + toString() + "</a></b>";
        }

    }

    @Override
    public String getPlateName() {
        if (basicPlayer != null) {
            // System.out.println("AI[<b><a
            // href=\""+basicPlayer.getUrl()+"\">"+basicPlayer.toString()+"</a></b>]");
            return "AI[<b>" + basicPlayer.toString() + "</b>]";
        } else {
            return "<b>" + toString() + "</b>";
        }
    }

    @Override
    public String skillsDescription() {
        return "我是AI，我是辣鸡，纯瞎玩";
    }

    public void setBasicPlayer(Person basicPlayer) {
        this.basicPlayer = basicPlayer;
    }

    public Person getBasicPlayer() {
        return basicPlayer;
    }

    // ==============skill 有风险的方法，以下技能都是涉及到
    // basicPlayer===============================
    public void shaGotShan(Person p) {
        if (basicPlayer != null) {
            basicPlayer.shaGotShan(p);
        } else {
            super.shaGotShan(p);
        }
    }

    public void shaSuccess(Person p) {
        if (basicPlayer != null) {
            basicPlayer.shaSuccess(p);
        } else {
            super.shaSuccess(p);
        }
    }

    public void gotShaBegin(Sha sha) {
        if (basicPlayer != null) {
            basicPlayer.gotShaBegin(sha);
        } else {
            super.gotShaBegin(sha);
        }
    }

    public boolean skillSha(Person sourse) {
        if (basicPlayer != null) {
            return basicPlayer.skillSha(sourse);
        }
        return super.skillSha(sourse);
    }

    public boolean skillShan(Person sourse) {
        if (basicPlayer != null) {
            return basicPlayer.skillShan(sourse);
        }
        return super.skillShan(sourse);
    }

    public Card skillWuxie(AtomicReference<PlayerIO> throwedPerson, MsgObj privMsgObj) {
        if (basicPlayer != null) {
            return basicPlayer.skillWuxie(throwedPerson, privMsgObj);
        }
        return super.skillWuxie(throwedPerson, privMsgObj);
    }

    public boolean isNaked() {
        if (basicPlayer != null) {
            return basicPlayer.isNaked();
        }
        return super.isNaked();
    }

    public void shaBegin(Card card) {
        if (basicPlayer != null) {
            basicPlayer.shaBegin(card);
        } else {
            super.shaBegin(card);
        }
    }

    public void jueDouBegin() {
        if (basicPlayer != null) {
            basicPlayer.jueDouBegin();
        } else {
            super.jueDouBegin();
        }
    }

    @Override
    public void receiveJudge(Card card) {
        if (basicPlayer != null) {
            basicPlayer.receiveJudge(card);
        } else {
            super.receiveJudge(card);
        }
    }

    public int gotSavedBy(Person p) {
        if (basicPlayer != null) {
            return basicPlayer.gotSavedBy(p);
        } else {
            return super.gotSavedBy(p);
        }
    }

    public boolean shaCanBeShan(Person p) {
        if (basicPlayer != null) {
            return basicPlayer.shaCanBeShan(p);
        }
        return super.shaCanBeShan(p);
    }

    public Card changeJudge(Person person, Card d) {
        if (basicPlayer != null) {
            return basicPlayer.changeJudge(person, d);
        }
        return super.changeJudge(person, d);
    }

    public int numOfTian() {
        if (basicPlayer != null) {
            return basicPlayer.numOfTian();
        }
        return super.numOfTian();
    }

    /**
     * 用来给人物实现释放技能的接口，如果返回true则跳出parseOrder的流程，不进入弃牌等，如果返回false则要进行弃牌
     * 
     * @param order
     * @return
     */
    public boolean useSkillInUsePhase(int order) {
        if (basicPlayer != null) {
            return basicPlayer.useSkillInUsePhase(order);
        }
        return super.useSkillInUsePhase(order);
    }

    public boolean hasMaShu() {
        if (basicPlayer != null) {
            return basicPlayer.hasMaShu();
        }
        return super.hasMaShu();
    }

    public boolean hasQiCai() {
        if (basicPlayer != null) {
            return basicPlayer.hasQiCai();
        }
        return super.hasQiCai();
    }

    public boolean hasKongCheng() {
        if (basicPlayer != null) {
            return basicPlayer.hasKongCheng();
        }
        return super.hasKongCheng();
    }

    public boolean hasQianXun() {
        if (basicPlayer != null) {
            return basicPlayer.hasQianXun();
        }
        return super.hasQianXun();
    }

    public boolean hasHongYan() {
        if (basicPlayer != null) {
            return basicPlayer.hasHongYan();
        }
        return super.hasHongYan();
    }

    public boolean hasHuoShou() {
        if (basicPlayer != null) {
            return basicPlayer.hasHuoShou();
        }
        return super.hasHuoShou();
    }

    public boolean hasDuanLiang() {
        if (basicPlayer != null) {
            return basicPlayer.hasDuanLiang();
        }
        return super.hasDuanLiang();
    }

    public boolean hasJuXiang() {
        if (basicPlayer != null) {
            return basicPlayer.hasJuXiang();
        }
        return super.hasJuXiang();
    }

    public boolean hasBaZhen() {
        if (basicPlayer != null) {
            return basicPlayer.hasBaZhen();
        }
        return super.hasBaZhen();
    }

    public boolean hasFeiYing() {
        if (basicPlayer != null) {
            return basicPlayer.hasFeiYing();
        }
        return super.hasFeiYing();
    }

    public void setBaZhen(boolean bool) {
        if (basicPlayer != null) {
            basicPlayer.setBaZhen(bool);
        } else {
            super.setBaZhen(bool);
        }
    }

    public boolean hasWuShuang() {
        if (basicPlayer != null) {
            return basicPlayer.hasWuShuang();
        }
        return super.hasWuShuang();
    }

    public boolean hasRouLin() {
        if (basicPlayer != null) {
            return basicPlayer.hasRouLin();
        }
        return super.hasRouLin();
    }

    public boolean hasWeiMu() {
        if (basicPlayer != null) {
            return basicPlayer.hasWeiMu();
        }
        return super.hasWeiMu();
    }

    public boolean hasWanSha() {
        if (basicPlayer != null) {
            return basicPlayer.hasWanSha();
        }
        return super.hasWanSha();
    }

    public boolean usesXingShang(Person deadPerson) {
        if (basicPlayer != null) {
            return basicPlayer.usesXingShang(deadPerson);
        }
        return super.usesXingShang(deadPerson);
    }

    public String usesYingYang() {
        if (basicPlayer != null) {
            return basicPlayer.usesYingYang();
        }
        return super.usesYingYang();
    }

    public boolean usesZhiBa() {
        if (basicPlayer != null) {
            return basicPlayer.usesZhiBa();
        }
        return super.usesZhiBa();
    }

    public boolean skipJudge() {
        if (basicPlayer != null) {
            return basicPlayer.skipJudge();
        }
        return super.skipJudge();
    }

    public boolean skipDraw(boolean fastMode) {
        if (basicPlayer != null) {
            return basicPlayer.skipDraw(fastMode);
        }
        return super.skipDraw(fastMode);
    }

    public boolean skipUse(boolean fastMode) {
        if (basicPlayer != null) {
            return basicPlayer.skipUse(fastMode);
        }
        return super.skipUse(fastMode);
    }

    public boolean skipThrow(boolean fastMode) {
        if (basicPlayer != null) {
            return basicPlayer.skipThrow(fastMode);
        }
        return super.skipThrow(fastMode);
    }

    public void hurtOther(Person p, int num) {
        if (basicPlayer != null) {
            basicPlayer.hurtOther(p, num);
        } else {
            super.hurtOther(p, num);
        }
    }

    // otherPersonUsePhase

    public void otherPersonUsePhaseBefore(Person thaPerson) {
        if (basicPlayer != null) {
            basicPlayer.otherPersonUsePhaseBefore(thaPerson);
        } else {
            super.otherPersonUsePhaseBefore(thaPerson);
        }
    }

    public void otherPersonThrowPhase(Person p, ArrayList<Card> cards) {
        if (basicPlayer != null) {
            basicPlayer.otherPersonThrowPhase(p, cards);
        } else {
            super.otherPersonThrowPhase(p, cards);
        }
    }

    public void otherPersonGetJudge(Person p, Card jugeCard) {
        if (basicPlayer != null) {
            basicPlayer.otherPersonGetJudge(p, jugeCard);
        } else {
            super.otherPersonGetJudge(p, jugeCard);
        }
    }

    public void otherPersonMakeHurt(Person source, Person target) {
        if (basicPlayer != null) {
            basicPlayer.otherPersonMakeHurt(source, target);
        } else {
            super.otherPersonMakeHurt(source, target);
        }
    }

    public void otherPersonHurtBySha(Person source, Person target) {
        if (basicPlayer != null) {
            basicPlayer.otherPersonHurtBySha(source, target);
        } else {
            super.otherPersonHurtBySha(source, target);
        }
    }

    public void initialize(Identity identity, int uerPos) {
        if (basicPlayer != null) {
            basicPlayer.initialize(identity, uerPos);
        } else {
            super.initialize(identity, uerPos);
        }
    }

    public void killOther() {
        if (basicPlayer != null) {
            basicPlayer.killOther();
        } else {
            super.killOther();
        }
    }

    public String getExtraInfo() {
        if (basicPlayer != null) {
            return basicPlayer.getExtraInfo();
        }
        return super.getExtraInfo();
    }

    @Override
    public void selfDeadAction(Person source) {
        if (basicPlayer != null) {
            basicPlayer.selfDeadAction(source);
        } else {
            super.selfDeadAction(source);
        }

    }

    @Override
    public boolean checkSha(Card card, boolean print) {
        if (basicPlayer != null) {
            basicPlayer.checkSha(card, print);
        } else {
            super.checkSha(card, print);
        }
        return false;
    }

    @Override
    public int getShaDistance() {
        if (basicPlayer != null) {
            return basicPlayer.getShaDistance();
        }
        return super.getShaDistance();
    }

    @Override
    public void die() {
        if (basicPlayer != null) {
            basicPlayer.die();
        }
        super.die();
    }

}
