package sanguosha.cardsheap;

import static sanguosha.cards.Color.CLUB;
import static sanguosha.cards.Color.DIAMOND;
import static sanguosha.cards.Color.HEART;
import static sanguosha.cards.Color.SPADE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArraySet;

import msg.CallbackEven;
import sanguosha.cards.Card;
import sanguosha.cards.Color;
import sanguosha.cards.basic.HurtType;
import sanguosha.cards.basic.Jiu;
import sanguosha.cards.basic.Sha;
import sanguosha.cards.basic.Shan;
import sanguosha.cards.basic.Tao;
import sanguosha.cards.equipments.horses.ChiTu;
import sanguosha.cards.equipments.horses.DaWan;
import sanguosha.cards.equipments.horses.DiLu;
import sanguosha.cards.equipments.horses.HuaLiu;
import sanguosha.cards.equipments.horses.JueYing;
import sanguosha.cards.equipments.horses.ZhuaHuangFeiDian;
import sanguosha.cards.equipments.horses.ZiXing;
import sanguosha.cards.equipments.shields.BaGuaZhen;
import sanguosha.cards.equipments.shields.BaiYinShiZi;
import sanguosha.cards.equipments.shields.RenWangDun;
import sanguosha.cards.equipments.shields.TengJia;
import sanguosha.cards.equipments.weapons.CiXiongShuangGuJian;
import sanguosha.cards.equipments.weapons.FangTianHuaJi;
import sanguosha.cards.equipments.weapons.GuDingDao;
import sanguosha.cards.equipments.weapons.GuanShiFu;
import sanguosha.cards.equipments.weapons.HanBingJian;
import sanguosha.cards.equipments.weapons.QiLinGong;
import sanguosha.cards.equipments.weapons.QingGangJian;
import sanguosha.cards.equipments.weapons.QingLongYanYueDao;
import sanguosha.cards.equipments.weapons.ZhangBaSheMao;
import sanguosha.cards.equipments.weapons.ZhuGeLianNu;
import sanguosha.cards.equipments.weapons.ZhuQueYuShan;
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
import sanguosha.cards.strategy.judgecards.BingLiangCunDuan;
import sanguosha.cards.strategy.judgecards.LeBuSiShu;
import sanguosha.cards.strategy.judgecards.ShanDian;
import sanguosha.manager.GameManager;
import sanguosha.manager.Utils;
import sanguosha.people.Person;

public class CardsHeap {
    private Deque<Card> drawCards = new ConcurrentLinkedDeque<>();
    private final Set<Card> usedCards = new CopyOnWriteArraySet<>();
    private int remainingShuffleTimes = 5;
    private int numCards;
    private Card judgeCard = null;
    // private final List<Card> allCards = new CopyOnWriteArrayList<>();
    private GameManager gameManager;

    public void addCard(Class<? extends Card> cls, Color color, int num) {
        try {
            Card c = cls.getConstructor(GameManager.class, Color.class, int.class).newInstance(gameManager, color, num);
            // gameManager.getIo().debug(c.info() + c.toString());
            drawCards.add(c);
            // allCards.add(c);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addCard(Class<? extends Card> cls, Color color, int num, HurtType type) {
        try {
            Card c = cls.getConstructor(
                    GameManager.class, Color.class, int.class, HurtType.class)
                    .newInstance(gameManager, color, num, type);

            // gameManager.getIo().debug(c.info() + c.toString());
            drawCards.add(c);
            // allCards.add(c);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addCards(Class<? extends Card> cls, Color color, int... numbers) {
        for (int value : numbers) {
            addCard(cls, color, value);
        }
    }

    public void addCards(Class<? extends Card> cls, HurtType type,
            Color color, int... numbers) {
        for (int value : numbers) {
            addCard(cls, color, value, type);
        }
    }

    // 牌堆我改成和萌娘百科介绍的牌堆
    public void addBasicCards() {

        addCards(Sha.class, HEART, 10, 10, 11);
        addCards(Sha.class, DIAMOND, 6, 7, 8, 9, 10, 13);
        addCards(Sha.class, SPADE, 7, 7, 8, 9, 9, 10, 10);
        addCards(Sha.class, CLUB, 2, 3, 4, 5, 6, 7, 7, 8, 9, 9, 10, 10, 11, 11);
        addCards(Shan.class, DIAMOND, 2, 2, 3, 4, 5, 6, 6, 7, 7, 8, 8, 9, 10, 10, 11, 11, 11);
        addCards(Shan.class, HEART, 2, 2, 8, 9, 11, 12, 13);
        addCards(Tao.class, HEART, 3, 4, 5, 6, 6, 7, 8, 9, 12);
        addCards(Tao.class, DIAMOND, 2, 3, 12);
        addCards(Jiu.class, CLUB, 3, 9);
        addCards(Jiu.class, SPADE, 3, 9);
        addCards(Jiu.class, DIAMOND, 9);
        addCards(Sha.class, HurtType.thunder, SPADE, 4, 5, 6, 7, 8);
        addCards(Sha.class, HurtType.thunder, CLUB, 5, 6, 7, 8);
        addCards(Sha.class, HurtType.fire, DIAMOND, 4, 5);
        addCards(Sha.class, HurtType.fire, HEART, 4, 7, 10);

    }

    public void addStrategyCards() {

        addCards(GuoHeChaiQiao.class, SPADE, 3, 4, 12);
        addCards(GuoHeChaiQiao.class, CLUB, 3, 4);
        addCards(GuoHeChaiQiao.class, HEART, 12);
        addCards(ShunShouQianYang.class, DIAMOND, 3, 4);
        addCards(ShunShouQianYang.class, SPADE, 3, 4, 11);
        addCards(WuZhongShengYou.class, HEART, 7, 8, 9, 11);
        addCards(WuXieKeJi.class, SPADE, 11, 13);
        addCards(WuXieKeJi.class, CLUB, 12, 13);
        addCards(WuXieKeJi.class, DIAMOND, 12);
        addCards(WuXieKeJi.class, HEART, 1, 13);
        addCards(NanManRuQin.class, SPADE, 8, 13);
        addCards(NanManRuQin.class, CLUB, 8);
        addCards(WanJianQiFa.class, HEART, 1);
        addCards(JueDou.class, SPADE, 1);
        addCards(JueDou.class, DIAMOND, 1);
        addCards(JueDou.class, CLUB, 1);
        addCards(LeBuSiShu.class, HEART, 6);
        addCards(LeBuSiShu.class, SPADE, 6);
        addCards(LeBuSiShu.class, CLUB, 6);
        addCards(BingLiangCunDuan.class, CLUB, 4);
        addCards(BingLiangCunDuan.class, SPADE, 10);
        addCards(JieDaoShaRen.class, CLUB, 12, 13);
        addCards(WuGuFengDeng.class, HEART, 3, 4);
        addCards(TaoYuanJieYi.class, HEART, 1);
        addCards(ShanDian.class, SPADE, 1);
        addCards(ShanDian.class, HEART, 12);
        addCards(HuoGong.class, HEART, 2, 3);
        addCards(TieSuoLianHuan.class, SPADE, 11, 12);
        addCards(TieSuoLianHuan.class, CLUB, 10, 11, 12, 13);

    }

    public void addEquipmentCards() {

        addCards(ZhuGeLianNu.class, DIAMOND, 1);
        addCards(ZhuGeLianNu.class, CLUB, 1);
        addCards(BaGuaZhen.class, SPADE, 2);
        addCards(BaGuaZhen.class, CLUB, 2);
        addCards(TengJia.class, CLUB, 2);
        addCards(TengJia.class, SPADE, 2);
        addCards(CiXiongShuangGuJian.class, SPADE, 2);
        addCards(QingGangJian.class, SPADE, 6);
        addCards(GuanShiFu.class, DIAMOND, 5);
        addCards(QingLongYanYueDao.class, SPADE, 5);
        addCards(ZhangBaSheMao.class, SPADE, 12);
        addCards(HanBingJian.class, SPADE, 2);
        addCards(RenWangDun.class, CLUB, 2);
        addCards(GuDingDao.class, SPADE, 1);
        addCards(ZhuQueYuShan.class, DIAMOND, 1);
        addCards(BaiYinShiZi.class, CLUB, 1);
        addCards(FangTianHuaJi.class, DIAMOND, 12);
        addCards(QiLinGong.class, HEART, 5);
        addCards(ZhuaHuangFeiDian.class, HEART, 13);
        addCards(JueYing.class, SPADE, 5);
        addCards(DiLu.class, CLUB, 5);
        addCards(ChiTu.class, HEART, 5);
        addCards(ZiXing.class, DIAMOND, 13);
        addCards(DaWan.class, SPADE, 13);
        addCards(HuaLiu.class, DIAMOND, 13);

    }

    public void init(GameManager gameManager) {
        this.gameManager = gameManager;
        addBasicCards();
        addStrategyCards();
        addEquipmentCards();
        ArrayList<Card> temp = new ArrayList<>(drawCards);
        drawCards.clear();
        Collections.shuffle(temp);
        drawCards.addAll(temp);
        numCards = drawCards.size();
    }

    public void shuffle() {
        remainingShuffleTimes--;
        if (usedCards.isEmpty() || remainingShuffleTimes == 0) {
            gameManager.callItEven();
        }
        // 本来usedCards就是无序的
        drawCards.addAll(usedCards);
        usedCards.clear();
    }

    public void testDrawCard(Class<? extends Card>[] testCardClass) {

        for (Class<? extends Card> claz : testCardClass) {
            List<Card> temp = new ArrayList<>();
            for (Card c : drawCards) {
                if (c.getClass().equals(claz)) {
                    temp.add(c);
                }
            }
            drawCards.removeAll(temp);
            for (Card c : temp) {
                drawCards.addFirst(c);
            }
        }

    }

    /**
     * 从牌堆中抽取一张牌
     * 
     * @return 抽取的牌
     */
    public Card draw() {
        if (drawCards.isEmpty()) {
            shuffle();
        }
        Card c = drawCards.pop();
        c.setTaken(false);
        // System.out.println("摸牌-"+c);
        return c;
    }

    /**
     * 从牌堆中抽取多张牌
     * 
     * @param num 抽取的牌数
     * @return 抽取的牌列表
     */
    public ArrayList<Card> draw(int num) {
        ArrayList<Card> cs = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            cs.add(draw());
        }
        return cs;
    }

    /**
     * 这是全局丢弃卡，角色里也有throw 和lose 方法，注意他们的不同
     * 
     * @param c
     */
    public void discard(Card c) {
        usedCards.add(c);
        c.setTaken(false);
    }

    /**
     * 废弃
     * 
     * @param cs
     */
    public void discard(List<Card> cs) {
        usedCards.addAll(cs);
    }

    /**
     * 判定牌，默认带3000秒的休眠，增加点悬念
     * 
     * @param source
     * @param judgEven
     * @return
     */
    public Card judge(Person source, CallbackEven judgEven) {
        return judge(source, judgEven, true);
    }

    /**
     * 判定牌，可以控制是否休眠
     * 
     * @param source
     * @param judgEven
     * @param sleep    可以控制是否休眠
     * @return
     */
    public Card judge(Person source, CallbackEven judgEven, boolean sleep) {
        source.removeTempActionMsgObj("changeJudge");// 把以前的改判消息删掉
        judgeCard = draw();
        // gameManager.getIo().println("Judge card: ");

        if (sleep) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();

            }
        }
        // gameManager.getIo().printCardPublic(judgeCard);

        Card change = null;
        for (Person p : gameManager.getPlayersBeginFromPlayer(source)) {
            Card c = p.changeJudge(source, judgeCard);

            if (c != null) {
                change = c;
                p.loseCard(c);
                // 这里不要跳出，因为可能有多个人会改判
            }
        }
        if (change != null) {
            // gameManager.getIo().printlnPublic("judge card changed to " + change);
            judgeCard = change;
        }

        discard(judgeCard);
        judgeCard.setOwner(source);
        for (Person p : gameManager.getPlayersBeginFromPlayer(source)) {
            p.otherPersonGetJudge(source, judgeCard);
        }

        if (judgEven.juge(judgeCard)) {
            source.receiveJudge(judgeCard);
        }
        return judgeCard;
    }

    /** 从废牌里回收卡 */
    public Card retrieve(Card c) {
        Utils.assertTrue(usedCards.contains(c), "retrieving card not in usedCards");
        usedCards.remove(c);
        return c;
    }

    public void retrieve(List<Card> cs) {
        for (Card c : cs) {
            retrieve(c);
        }
    }

    /**
     * 获取当前的判定牌
     * 
     * @return 判定牌
     */
    public Card getJudgeCard() {
        usedCards.remove(judgeCard);
        return judgeCard;
    }

    /**
     * 
     * @param num 预先要取出的牌数，估计一下牌数，没牌了就洗牌
     * @return
     */
    public Deque<Card> getDrawCards(int num) {
        if (num > drawCards.size()) {
            shuffle();
        }
        return drawCards;
    }

    /**
     * 获取已使用的牌
     * 
     * @return 已使用的牌集合
     */
    public Set<Card> getUsedCards() {
        return usedCards;
    }

    /**
     * 设置牌堆
     * 
     * @param drawCards 新的牌堆
     */
    public void setDrawCards(Deque<Card> drawCards) {
        this.drawCards = drawCards;
    }

    /**
     * 获取牌堆中剩余的牌数
     * 
     * @return 剩余的牌数
     */
    public int getNumCards() {
        return numCards;
    }

    /**
     * 获取未被抽取的牌
     * 
     * @return 未被抽取的牌
     */
    public Deque<Card> getUnTakenCard() {
        return drawCards;
    }
}
