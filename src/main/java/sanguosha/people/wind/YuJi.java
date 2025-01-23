package sanguosha.people.wind;

import sanguosha.cards.Card;
import sanguosha.cards.Color;
import sanguosha.cards.EquipType;
import sanguosha.cards.basic.HurtType;
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
import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.people.PlayerIO;
import sanguosha.skills.Skill;
import components.MyCountDownLatch;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import msg.ReturnType;
import msg.CallbackEven;
import msg.MsgAPI;
import msg.MsgObj;
import config.Config;
import config.DescUrl;
import config.Text;
import db.ImgDB;

import static sanguosha.cards.Color.HEART;
import static sanguosha.cards.Color.NOCOLOR;

public class YuJi extends Person {
    private HashMap<String, Class<? extends Card>> cardsMap = new HashMap<>();
    private List<String> intenShowTypes = new ArrayList<>();
    private List<String> intenTypes = new ArrayList<>();

    public YuJi() {
        super(3, Nation.QUN);
        putCard(Sha.class);
        putCard(Shan.class);
        putCard(Tao.class);
        putCard(Jiu.class);
        putCard(GuoHeChaiQiao.class);
        putCard(HuoGong.class);
        putCard(JieDaoShaRen.class);
        putCard(JueDou.class);
        putCard(NanManRuQin.class);
        putCard(ShunShouQianYang.class);
        putCard(TaoYuanJieYi.class);
        putCard(TieSuoLianHuan.class); // 于吉的蛊惑不能重铸铁索
        putCard(WanJianQiFa.class);
        putCard(WuGuFengDeng.class);
        putCard(WuXieKeJi.class);
        putCard(WuZhongShengYou.class);
    }

    public void putCard(Class<? extends Card> cls) {
        Card c;
        try {
            c = cls.getConstructor(GameManager.class, Color.class, int.class)
                    .newInstance(getGameManager(), NOCOLOR, 0);
            cardsMap.put(c.toString(), cls);
            intenTypes.add(c.toString());
            intenShowTypes.add(c.info() + c.toString());
            if (cls.equals(Sha.class)) {// 加上火杀雷杀
                c = cls.getConstructor(GameManager.class, Color.class, int.class, HurtType.class)
                        .newInstance(getGameManager(), NOCOLOR, 0, HurtType.fire);
                intenTypes.add(c.toString());
                intenShowTypes.add(c.info() + c.toString());

                c = cls.getConstructor(GameManager.class, Color.class, int.class, HurtType.class)
                        .newInstance(getGameManager(), NOCOLOR, 0, HurtType.thunder);
                intenTypes.add(c.toString());
                intenShowTypes.add(c.info() + c.toString());
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Skill("蛊惑")
    public boolean guHuo(Card c, String type) {
        ArrayList<Person> forPlayers = new ArrayList<>();
        for (Person p : getGameManager().getPlayers()) {
            if (p == this) {// 自己不能质疑 test
                continue;
            }
            if (p.getUser() == null) {
                continue;
            }
            forPlayers.add(p);
        }
        MsgObj publicMsgObj = MsgObj.newMsgObj(getGameManager());
        publicMsgObj.setImg(this.toString());
        publicMsgObj.chatId = getGameManager().getChatId();
        publicMsgObj.forPlayers = forPlayers;
        String res = Text.format(
                "%s 打出了[%s]牌[%s],是否有人质疑?",
                getPlateName(), getSkillHtmlName("蛊惑"), DescUrl.getDescHtml(type));
        publicMsgObj.text = res;
        List<String[]> options = new ArrayList<>();
        options.add(new String[] { "质疑/N", "N" });
        options.add(new String[] { "相信/Y", "Y" });

        MyCountDownLatch latch = MyCountDownLatch.newInst(forPlayers.size(), getGameManager());
        publicMsgObj.sonCallback = new ConcurrentHashMap<>();
        Set<Person> questionPersons = ConcurrentHashMap.newKeySet();
        Set<Person> belivePersons = ConcurrentHashMap.newKeySet();
        for (Person p : forPlayers) {
            publicMsgObj.sonCallback.put(p.getUser().user_id, new CallbackEven() {
                @Override
                public void vote(long userId, String result) {
                    try {
                        System.out.println("CallbackEven vote");
                        String callBackId = publicMsgObj.callbackIds.get(userId, 3, TimeUnit.SECONDS);
                        if (questionPersons.contains(p) || belivePersons.contains(p)) {
                            getGameManager().getMsgAPI().answerCallBack(callBackId, "💬你已操作过", false);
                            return;
                        }
                        if ("N".equals(result)) {
                            questionPersons.add(p);
                            getGameManager().getMsgAPI().answerCallBack(callBackId, "💬你质疑这张牌", false);

                        } else {
                            belivePersons.add(p);
                            getGameManager().getMsgAPI().answerCallBack(callBackId, "💬你相信这张牌", false);
                        }
                        latch.countDown();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            });
        }
        getGameManager().getMsgAPI().votePublic(options, publicMsgObj);

        latch.await(Config.PUBLIC_ACTION_TIME_15S / 1000, TimeUnit.SECONDS);

        // sleep(1000);
        getGameManager().getMsgAPI().clearButtons(publicMsgObj);
        // 没人质疑
        if (questionPersons.size() == 0) {
            // sleep(1000);
            String res2 = ",无人质疑";
            publicMsgObj.text = publicMsgObj.text + res2;
            getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);

            return true;
        }
        // 有人质疑
        if (c.toString().equals(type) ||
                (type.equals("杀") && c instanceof Sha) ||
                (type.equals("火杀") && c instanceof Sha && ((Sha) c).getType() == HurtType.fire) ||
                (type.equals("雷杀") && c instanceof Sha && ((Sha) c).getType() == HurtType.thunder)) {
            // printlnPriv("蛊惑 card is real: " + c.info() + c.toString());
            String hurtString = "";
            for (Person p : questionPersons) {
                int real = p.hurt((Card) null, this, 1);
                hurtString += Text.format(",%s 受%s点伤%s", p.getHtmlName(), real, p.getHPEmoji());
            }
            String res1 = Text.format(",%s 为真:%s:<i>道法玄机，变幻莫测。</i>", c.getHtmlNameWithColor(), hurtString);
            publicMsgObj.text = publicMsgObj.text + res1;
            try {
                return c.color() == Color.HEART;
            } finally {
                // sleep(1000);
                getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
            }

        } else {
            // printlnPriv("蛊惑 card is fake, real card: " + c.info() + c.toString());
            String drawString = "";
            for (Person p : questionPersons) {
                p.drawCard();
                drawString += Text.format(",%s 摸1张牌", p.getHtmlName());
            }
            String res1 = Text.format(",%s 为假:%s", c.getHtmlNameWithColor(), drawString);

            publicMsgObj.text = publicMsgObj.text + res1;

            try {
                return false;
            } finally {
                // sleep(1000);
                getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);

            }
        }
    }

    private Card createFakeCard(String type) {
        // System.out.println("fake type="+type);
        switch (type) {
            case "酒":
                return new Jiu(getGameManager(), NOCOLOR, 0);
            case "杀":

                return new Sha(getGameManager(), NOCOLOR, 0);
            case "闪":

                return new Shan(getGameManager(), NOCOLOR, 0);
            case "桃":

                return new Tao(getGameManager(), NOCOLOR, 0);

            case "无懈可击":
                return new WuXieKeJi(getGameManager(), NOCOLOR, 0);

            default:
                return null;
        }
    }

    @Override
    public Card requestCard(String type, AtomicReference<PlayerIO> throwedPerson, MsgObj inMsg) {
        // System.out.println(getPriviMsg().getAll());

        // String info1 = getPriviMsg().getInfo1().toString();
        String oneTimeInfo1 = getPriviMsg().getOneTimeInfo1().toString();

        if (type != null && getCards().size() > 0) {// 只有要求特定的卡才学要蛊惑
            getPriviMsg().clearHeader2();
            String res = "\n💬是否用[蛊惑]：你可以打出[%s]，若无人质疑，该牌结算。若有质疑：牌为真，质疑者受1点伤；牌为假，质疑者摸1张牌。被质疑的牌失效。仅当牌为%s且为真时，该牌结算。";
            getPriviMsg().setOneTimeInfo1(Text.format(res, type, Card.getColorEmoji(HEART)));
            if (launchSkillPriv("蛊惑")) {
                getPriviMsg().setOneTimeInfo1(Text.format("\n♻️蛊惑:请出实牌，替换为[蛊惑]牌 [%s]",
                        DescUrl.getDescHtml(type)));
                Card c = super.requestCard(null, throwedPerson, inMsg);// 竞争出牌在这里，如果这里抢到，别人就不能再出
                if (c != null) {
                    if (throwedPerson != null && throwedPerson.get() != this) {
                        // 如果别人出过牌了,就拿回这张牌
                        getGameManager().getCardsHeap().retrieve(c);
                        this.addCard(c);
                        if (getUser() != null) {
                            getGameManager().getIo().delaySendAndDelete(this, "其他玩家已经出过牌");

                        }
                        return null;
                    }
                    if (guHuo(c, type)) {

                        Card fakeCard = createFakeCard(type);
                        fakeCard.setSource(this);
                        fakeCard.setIsFake(true);
                        fakeCard.addReplaceCard(c);
                        loseCard(c);// 把原牌丢掉，返回假牌//这里改为丢假牌时再处理
                        // loseCard(fakeCard);

                        return fakeCard;
                    } else {
                        return null;
                    }
                }
                // return null;
            }
        }
        // 把原来的提示存回来
        // getPriviMsg().setInfo1(info1);
        getPriviMsg().setOneTimeInfo1(oneTimeInfo1);
        inMsg.isDeleted = false;
        return super.requestCard(type, throwedPerson, inMsg);
    }

    @Override
    public void usePhaseBefore() {

        if (isActiveSkill("蛊惑") && getCards().size() > 0) {
            getSkillCards().add("蛊惑");
        }
    }

    private boolean preCheck(Card card) {
        if (card instanceof Sha) {
            if (!(getShaCount() < getMaxShaCount() || hasEquipment(EquipType.weapon, "诸葛连弩"))) {
                getGameManager().getIo().delaySendAndDelete(this, "💬你不能再使用 杀");
                return false;
            }
        }
        if ((card instanceof Tao && getHP() == getMaxHP()) || card instanceof Shan ||
                card instanceof WuXieKeJi || (card instanceof Jiu && (isDrunk() || isDrunkShaUsed()))) {
            getGameManager().getIo().delaySendAndDelete(this, "💬你还无法使用 " + card.toString());

            return false;
        }
        return true;
    }

    @Override
    public boolean useSkillInUsePhase(int orderInt) {
        // System.out.println("调用蛊惑useSkillInUsePhase");
        // System.out.println("order="+order);
        // int orderInt = Integer.valueOf(order1) - 1;

        if (orderInt < getSkillCards().size() && getSkillCards().get(orderInt).equals("蛊惑")) {
            // String out=(this + " 使用了[蛊惑]，请选择你想要打出的[蛊惑]牌\n");
            getPriviMsg().clearHeader2();
            String res = "\n💬是否用[蛊惑]：你可以打出任意牌，若无人质疑，该牌结算。若有质疑：牌为真，质疑者受1点伤；牌为假，质疑者摸1张牌。被质疑的牌失效。仅当牌为%s且为真时，该牌结算。";
            getPriviMsg().setOneTimeInfo1(Text.format(res, Card.getColorEmoji(HEART)));
            MsgObj privMsg = MsgObj.newMsgObj(getGameManager());
            stepInPriv(privMsg);
            privMsg.text = getPriviMsg().toString();
            privMsg.chatId = getUser().user_id;
            MsgAPI io = getGameManager().getMsgAPI();
            ImgDB.setImg(privMsg, getPriviMsg().getShowImg());
            MsgObj msg = io.chooseOneFromOpinionCanBeNull(intenShowTypes, privMsg);
            long nextTime = System.currentTimeMillis() + Config.PRIV_RND_TIME_60S;
            int timeOut = (int) (nextTime - System.currentTimeMillis()) / 1000;
            String idex = msg.getString(ReturnType.ChooseOneCard, timeOut);
            io.delMsg(msg);
            stepOutPriv(privMsg);

            if (idex == null || "q".equals(idex)) {
                return true;
            }

            String type = intenTypes.get(Integer.valueOf(idex) - 1);
            try {
                Card intend;
                if (cardsMap.containsKey(type)) {
                    intend = cardsMap.get(type).getConstructor(GameManager.class, Color.class, int.class)
                            .newInstance(getGameManager(), NOCOLOR, 0);
                } else if (type.equals("火杀")) {
                    intend = new Sha(getGameManager(), NOCOLOR, 0, HurtType.fire);
                } else if (type.equals("雷杀")) {
                    intend = new Sha(getGameManager(), NOCOLOR, 0, HurtType.thunder);
                } else {
                    // printlnToIOPriv("wrong type");
                    return true;
                }
                intend.setIsFake(true);

                if (!preCheck(intend)) {
                    return true;
                }

                getPriviMsg().setOneTimeInfo1(
                        Text.format("\n♻️请出实牌，替换为[蛊惑]牌 [%s]", intend.info() + intend.toString()));

                Card c = requestCard(null);
                if (c == null) {
                    return true;
                }
                intend.setSource(this);
                intend.addReplaceCard(c);

                if (intend.askTarget(this)) {
                    if (guHuo(c, type)) {
                        useCard(intend);
                    }
                } else {
                    if (c != null) {
                        addCard(getGameManager().getCardsHeap().retrieve(c), false);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        }
        return false;
    }

    @Override
    public boolean checkShan() {
        if (isActiveSkill("蛊惑") && getCards().size() > 0) {
            return true;
        }
        return super.checkShan();
    }

    /**
     * 是否能打出杀
     * 
     * @return
     */
    @Override
    public boolean existsSha() {
        if (isActiveSkill("蛊惑") && getCards().size() > 0) {
            return true;
        }
        return super.existsSha();
    }

    /**
     * 检查是否能打出桃
     * 
     * @return
     */
    @Override
    public boolean checkTao() {
        if (isActiveSkill("蛊惑") && getCards().size() > 0) {
            return true;
        }
        return super.checkTao();
    }

    @Override
    public boolean hasWuXieReplace() {
        if (!isActiveSkill("蛊惑")) {
            return false;
        }
        if (getCards().size() > 0) {
            return true;
        }
        return false;
    }

    @Override
    public String name() {
        return "于吉";
    }

    @Override
    public String skillsDescription() {
        return "蛊惑：出牌阶段你可以说出任何一种基本牌或普通锦囊牌，并正面朝下使用或出手牌。体力值不为0的其他角色依次选择是否质疑。" +
                "若无角色质疑，则该牌按你所述之牌结算。若有角色质疑则亮出验明：若为真，质疑者各失去1点体力；若为假，质疑者各摸一张牌。无论真假，弃置被质疑的牌。" +
                "仅当被质疑的牌为红桃花色且为真时，该牌仍然可以进行结算。";
    }
}
