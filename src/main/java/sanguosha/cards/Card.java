package sanguosha.cards;

import sanguosha.cards.basic.Jiu;
import sanguosha.cards.basic.Sha;
import sanguosha.cards.basic.Shan;
import sanguosha.cards.basic.Tao;
import sanguosha.cards.strategy.GuoHeChaiQiao;
import sanguosha.cards.strategy.HuoGong;
import sanguosha.cards.strategy.JieDaoShaRen;
import sanguosha.cards.strategy.JueDou;
import sanguosha.cards.strategy.NanManRuQin;
import sanguosha.cards.strategy.ShunShouQianYang;
import sanguosha.cards.strategy.TaoYuanJieYi;
import sanguosha.cards.strategy.TieSuoLianHuan;
import sanguosha.cards.strategy.WanJianQiFa;
import sanguosha.cards.strategy.WuGuFengDeng;
import sanguosha.cards.strategy.WuXieKeJi;
import sanguosha.cards.strategy.WuZhongShengYou;
import sanguosha.manager.GameManager;
import sanguosha.manager.Utils;
import sanguosha.people.Person;

import static sanguosha.cards.Color.NOCOLOR;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import config.DescUrl;

public abstract class Card implements Serializable {
    private final Color color;
    private final int number;
    private Person target;
    private Person source;
    private boolean isTaken = false;
    private Person owner = null;
    /**
     * 这个起始是用来实现卡替换的，初始实例化时，本卡就是替换卡
     */
    private List<Card> replaceCards = new ArrayList<>(1);
    private GameManager gameManager;
    private boolean isFake = false;

    public Card(GameManager gameManager, Color color, int number) {
        this.color = color;
        this.number = number;
        this.replaceCards.add(this);
        this.gameManager = gameManager;
        // System.out.println(this.getClass()+".class"+" "+this.toString());
    }

    /** 有调换，标明这张牌是那几张牌换的 */
    public void setReplaceCards(List<Card> replaceCards) {
        setIsFake(true);
        this.replaceCards = replaceCards;
    }

    /** 有调换，标明这张牌是那张牌换的 */
    public void addReplaceCard(Card replaceCard) {
        setIsFake(true);
        ArrayList<Card> cs = new ArrayList<>(1);
        cs.add(replaceCard);
        this.replaceCards = cs;

    }

    /** 获取这张牌是用哪些牌换成的 */
    public List<Card> getReplaceCards() {
        return replaceCards;
    }

    public void setGameManager(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public Color color() {
        if (owner != null && owner.hasHongYan() && color == Color.SPADE) {
            // gameManager.getIo().printlnPublic(owner + " uses 红颜");
            return Color.HEART;
        }
        return color;
    }

    public int number() {
        return number;
    }

    public boolean isBlack() {
        return color() == Color.SPADE || color() == Color.CLUB;
    }

    public boolean isRed() {
        return color() == Color.HEART || color() == Color.DIAMOND;
    }

    public void setSource(Person source) {
        this.source = source;
    }

    public Person getSource() {
        return source;
    }

    public void setTarget(Person target) {
        this.target = target;
    }

    public Person getTarget() {
        return target;
    }

    /**
     * 是不是假的牌，用于于吉等；
     * 
     * @param isFake
     */
    public void setIsFake(boolean isFake) {
        this.isFake = isFake;
    }

    public boolean getIsFake() {
        return isFake;
    }

    public abstract Object use();


    public boolean needChooseTarget() {
        return false;
    }

    public abstract String toString();

    /** 花色和点数 */
    public String info() {
        String num;
        switch (number) {
            case 0:
                num = "-";
                break;
            case 1:
                num = "A";
                break;
            case 11:
                num = "J";
                break;
            case 12:
                num = "Q";
                break;
            case 13:
                num = "K";
                break;
            default:
                if (number >= 2 && number <= 10) {
                    num = number + "";
                } else {
                    throw new AssertionError("wrong number: " + number);
                }
        }
        String col;
        switch (color()) {
            case DIAMOND:
                col = "♦️";
                break;
            case HEART:
                col = "♥️";
                break;
            case SPADE:
                col = "♠️";
                break;
            case CLUB:
                col = "♣️";
                break;
            default:
                col = "✨";
        }
        return col + num + " ";
    }

    // /**花色和点数 */
    // public String info() {
    // String num;
    // if (number == 0) {
    // num = "-";
    // } else if (number == 1) {
    // num = "A";
    // } else if (number <= 10 && number >= 2) {
    // num = number + "";
    // } else if (number == 11) {
    // num = "J";
    // } else if (number == 12) {
    // num = "Q";
    // } else {
    // Utils.assertTrue(number == 13, "wrong number: " + number);
    // num = "K";
    // }
    // String col;
    // if (color() == Color.DIAMOND) {
    // col = "♦️方块";
    // } else if (color() == Color.HEART) {
    // col = "♥️红桃";
    // } else if (color() == Color.SPADE) {
    // col = "♠️黑桃";
    // } else if (color() == Color.CLUB) {
    // col = "♣️梅花";
    // } else {
    // col = "✨无色";
    // }
    // return col + num + " ";
    // }

    /**
     * 这个是被别人拿走，或自己装在装备上，装在判定牌里，的标记,主要在此次的发动后，避免被回收，
     * 
     * @param taken
     */
    public void setTaken(boolean taken) {
        isTaken = taken;
    }

    /** 没有被别人拿走 */
    public boolean isNotTaken() {
        return !isTaken;
    }

    public void setOwner(Person owner) {
        this.owner = owner;
    }

    public Person getOwner() {
        return owner;
    }

    /**
     * 传入自身
     * 
     * @param user
     * @return
     */
    public Person selectTarget(Person self) {
        source = self;
        target = self.selectPlayer(false);
        return target;
    }

    /**
     * 传入列表
     * 
     * @param self
     * @param persons
     * @return
     */
    public Person selectTarget(Person self, List<Person> persons) {
        source = self;
        target = self.selectPlayer(persons);
        return target;
    }

    /*
     * 设置多重杀
     */
    public void setMultiSha(int multiSha) {

    }

    /*
     * 是否多重杀
     */
    public int getMultiSha() {
        return 1;
    }

    /**
     * 选择卡的释放对象
     */
    public boolean askTarget(Person user) {
        setSource(user);
        if (!this.needChooseTarget()) {
            setTarget(user);
            return true;
        }

        return selectTarget(user) != null;
    }

    public abstract String help();

    public String getUrl() {
        return DescUrl.getDescUrl(toString());
    };

    public String getHtmlName() {
        return "<b><a href=\"" + getUrl() + "\">" + toString() + "</a></b>";
    };

    public String getHtmlNameWithColor() {
        return "<b><a href=\"" + getUrl() + "\">" + info() + toString() + "</a></b>";
    };

    public boolean addJudgeCard(Person p) {
        return p.addJudgeCard((JudgeCard) this);
    }

    public static String getHtmlNameFromName(String cardName) {
        return "<b><a href=\"" + DescUrl.getDescUrl(cardName) + "\">" + cardName + "</a></b>";
    };

    public void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static String getColorEmoji(Color color) {

        switch (color) {
            case DIAMOND:
                return "♦️";
            case HEART:
                return "♥️";
            case SPADE:
                return "♠️";
            case CLUB:
                return "♣️";

            default:
                return "✨";
        }

    }

    public  static <E extends Card> String getBasicNameByClass(Class<E> type){
        switch (type.getName()) {
            case "Sha":
                return "杀";
            case "Shan":
                return "闪";
            case "Tao":
                return "桃";
            case "Jiu":
                return "酒";
            case "GuoHeChaiQiao":
                return "过河拆桥";
            case "HuoGong":
                return "火攻";
            case "JieDaoShaRen":
                return "借刀杀人";
            case "JueDou":
                return "决斗";
            case "NanManRuQin":
                return "南蛮入侵";
            case "ShunShouQianYang":
                return "顺手牵羊";
            case "TaoYuanJieYi":
                return "桃园结义";
            case "TieSuoLianHuan":
                return "铁索连环"; // 于吉的蛊惑不能重铸铁索
            case "WanJianQiFa":
                return "万箭齐发";
            case "WuGuFengDeng":
                return "无顾风灯";
            case "WuXieKeJi":
                return "无懈可击";
            case "WuZhongShengYou":
                return "无中生有";
            default:
                return "未知卡牌";
        }
    }
 

}
