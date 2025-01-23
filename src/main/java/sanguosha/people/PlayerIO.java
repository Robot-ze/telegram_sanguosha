package sanguosha.people;

import sanguosha.cards.Card;
import sanguosha.cards.Color;
import sanguosha.cards.EquipType;
import sanguosha.cards.Equipment;
import sanguosha.cards.JudgeCard;
import sanguosha.cards.basic.Sha;
import sanguosha.manager.GameManager;
import sanguosha.manager.Utils;
import sanguosha.user.SanUser;
import components.SanProgress;
import components.TimeLimit;

import static sanguosha.cards.Color.HEART;
import static sanguosha.cards.Color.SPADE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.telegram.telegrambots.meta.api.objects.User;

import msg.ReturnType;
import msg.MsgAPI;
import msg.MsgObj;
import msg.ShowText;
import config.Config;
import config.Text;
import db.ImgDB;

public interface PlayerIO {

    public boolean isMyRound();

    /**
     * 获取记录是否刚从群聊移入私聊，用来计时
     * 
     * @return
     */
    public boolean isStepInPriv();

    /**
     * 进入私聊 用来计时
     * 
     * @return
     */
    public void stepInPriv(MsgObj privMsgObj);

    /**
     * 退出私聊 用来计时
     * 
     * @return
     */
    public void stepOutPriv(MsgObj privMsgObj);

    /**
     * 返回针对于此角色还未完成的msgObj，主要用来超时清理
     * 
     * @return
     */
    public Set<MsgObj> getActingPrivMsgs();

    public Set<Card> getOldCards();

    public ShowText getPriviMsg();

    public ShowText getPublicMsg();

    void setGameManager(GameManager gameManager);

    GameManager getGameManager();

    default Card requestRedBlack(String color, boolean fromEquipments) {
        Utils.assertTrue(color.equals("red") || color.equals("black"), "invalid color");
        Card c;
        String ColorString = "red".equals(color) ? "红色" : "黑色";
        // getPriviMsg().clearHeader2();
        getPriviMsg().setOneTimeInfo1("\n💬请选择一张 " + ColorString + " 的手牌");
        List<Card> colorCards = new ArrayList<>();

        for (Card cc : (fromEquipments ? getCardsAndEquipments() : getCards())) {
            if ((color.equals("red") && cc.isRed()) || (color.equals("black") && cc.isBlack())) {
                colorCards.add(cc);
            }
        }
        if (colorCards.size() <= 0) {
            getGameManager().getIo().delaySendAndDelete((Person) this, "💬你没有" + ColorString + "的手牌");
            return null;
        }
        c = chooseCard(colorCards, true);

        return c;
    }

    default Card requestRedBlack(String color) {
        return requestRedBlack(color, false);
    }

    default Card requestColor(Color color, boolean fromEquipments) {

        List<Card> colorCards = new ArrayList<>();
        if (fromEquipments) {
            for (Card cc : getCardsAndEquipments()) {
                if (isSameColor(cc, color)) {
                    colorCards.add(cc);
                }
            }
        } else {
            for (Card cc : getCards()) {
                if (isSameColor(cc, color)) {
                    colorCards.add(cc);
                }
            }
        }
        if (colorCards.size() <= 0) {
            getGameManager().getIo().delaySendAndDelete((Person) this, "💬你没有" + Card.getColorEmoji(color) + "的手牌");
            return null;
        }
        getPriviMsg().setOneTimeInfo1(Text.format("💬你需要选择 %s 颜色的牌", Card.getColorEmoji(color)));
        Card c = chooseCard(colorCards, true);
        loseCard(c);
        return c;
    }

    private boolean isSameColor(Card c, Color color) {
        if (c == null) {
            return false;
        } else {

            if (c.color() == color) {
                return true;
            } else {// 为什么要写这一段，因为有的人有技能， color()在丢牌之后会显示原色
                if (color == HEART &&
                        this instanceof Person &&
                        ((Person) this).hasHongYan() &&
                        c.color() == SPADE) {
                    return true;
                }
            }
            return false;
        }

    }

    default Card requestColor(Color color) {
        return requestColor(color, false);
    }

    /**
     * 请求卡
     * 
     * @param type
     * @return
     */
    default Card requestCard(String type) {
        MsgObj privMsgObj = MsgObj.newMsgObj(getGameManager());
        stepInPriv(privMsgObj);
        Card c = requestCard(type, null, privMsgObj);
        stepOutPriv(privMsgObj);
        return c;

    }

    /**
     * 需要竞争的请求卡
     * 
     * @param type
     * @param throwedPerson 已出牌人的原子链接
     * @param inMsg         消息对象
     * @return
     */
    default Card requestCard(String type, AtomicReference<PlayerIO> throwedPerson, MsgObj inMsg) {
        if (getUser() == null) {
            return null;
        }
        if (getCards().isEmpty()) {
            return null;
        }

        MsgAPI io = getGameManager().getMsgAPI();
        inMsg.chatId = getUser().user_id;
        inMsg.resetAttributes(ReturnType.ChooseOneCard);
        // int i = 1;
        List<String> stringChoices = new ArrayList<>();
        List<Card> cardChoices = new ArrayList<>();
        for (Card choice : getCards()) {
            if (type == null) {// 如果不要求种类
                stringChoices.add(((Card) choice).info() + choice.toString());
                cardChoices.add(choice);
            } else if (type.equals(choice.toString()) ||
                    (type.equals("杀") && choice instanceof Sha)) {// 要求种类 ,火杀之类的的也是杀

                stringChoices.add(((Card) choice).info() + choice.toString());
                cardChoices.add(choice);
            }
            // getInText().append("[" + i++ + "] " + choice.toString() + " ");
        }
        if (type != null && cardChoices.size() <= 0) {
            getGameManager().getIo().delaySendAndDelete((Person) this, "💬你没有类型为 " + type + " 的手牌");

        }
        // if(stringChoices.size()<=0){
        // return null;
        // }
        // System.out.println(" getPriviMsg().toString()="+
        // getPriviMsg().toAllPartString());
        inMsg.text = getPriviMsg().toString();
        ImgDB.setImg(inMsg, getPriviMsg().getShowImg());
        MsgObj msg = io.chooseOneFromOpinionCanBeNull(stringChoices, inMsg);
        // doesn't use chooseFromProvided because need info()
        long nextTime = System.currentTimeMillis() + Config.PRIV_RND_TIME_60S;

        TimeLimit t = new TimeLimit(Config.PRIV_RND_TIME_60S);
        while (getGameManager().isRunning() && t.isNotTimeout()) {
            int timeOut = (int) (nextTime - System.currentTimeMillis()) / 1000;
            String order = msg.getString(ReturnType.ChooseOneCard, timeOut);
            if (order == null || order.equals("q")) {
                io.delMsg(msg);

                return null;
            }

            try {
                Card c = cardChoices.get(Integer.parseInt(order) - 1);
                if (type != null && !c.toString().equals(type) &&
                        !(type.equals("杀") && c instanceof Sha)) {
                    msg.resetAttributes(ReturnType.ChooseOneCard);
                    io.answerCallBack(msg, "❌ 牌型错误,请重新选择", false);
                    continue;
                }
                if (throwedPerson != null) {
                    if (throwedPerson.compareAndSet(null, this)) {
                        io.delMsg(msg);// 先删信息再丢牌
                        loseCard(c);

                        return c;
                    } else {
                        io.answerCallBack(msg, "❌ 已有他人先出牌", false);
                        io.delMsg(msg);

                        return null;
                    }
                } else {
                    loseCard(c);
                    io.delMsg(msg);

                    return c;
                }

            } catch (Exception e) {
                e.printStackTrace();
                msg.resetAttributes(ReturnType.ChooseOneCard);
                io.answerCallBack(msg, "❌ 牌型错误,请重新选择", false);
            }
        }

        return null;

    }

    // default String showAllCards() {
    // return getPlayerStatus(false, true);
    // }

    default <E> int chooseFromProvided(MsgObj privMsg, boolean canBeNull, E... choices) {
        List<E> options = Arrays.asList(choices);
        return chooseFromProvided(privMsg, canBeNull, options);
    }

    /**
     * 返回从0开始的 int
     */
    default <E> int chooseFromProvided(MsgObj privMsg, boolean canBeNull, List<E> choices) {
        if (choices.isEmpty()) {
            // printlnToIO(this + " 无牌可选\n");
            return -1;
        }
        if (getUser() == null) {
            return Utils.randint(0, choices.size() - 1);
        }

        // int i = 1;
        // String out="";
        boolean isPerson = false;
        for (E choice : choices) {
            if (!isPerson && (choice instanceof Person)) {
                isPerson = true;
            }
            // getInText().append("[" + choice.toString() + "]");
        }
        // printlnToIO(out);
        MsgObj inMsg;
        if (privMsg == null) {
            inMsg = MsgObj.newMsgObj(getGameManager());

        } else {
            inMsg = privMsg;
        }
        stepInPriv(inMsg);
        // inMsg.setImg(toString());
        inMsg.text = getPriviMsg().toString();
        inMsg.chatId = getUser().user_id;
        // ImgDB.setImg(inMsg, getPriviMsg().getShowImg());
        MsgAPI io = getGameManager().getMsgAPI();
        MsgObj msg;
        if (canBeNull || isPerson) {
            msg = io.chooseOneFromOpinionCanBeNull(choices, inMsg);
        } else {
            msg = io.chooseOneFromOpinion(choices, inMsg);
        }

        // MsgObj msg= io.chooseOneFromOpinion(choices,inMsg);
        long nextTime = System.currentTimeMillis() + Config.PRIV_RND_TIME_60S;
        TimeLimit t = new TimeLimit(Config.PRIV_RND_TIME_60S);

        try {

            while (getGameManager().isRunning() && t.isNotTimeout()) {
                try {
                    int timeOut = (int) (nextTime - System.currentTimeMillis()) / 1000;
                    String order = msg.getString(ReturnType.ChooseOneCard, timeOut);
                    if (order == null || order.equals("q")) {
                        io.delMsg(msg);
                        if (canBeNull) {
                            return -1;
                        } else {
                            // 不选 又不能为空的话强制选第一个
                            return 0;
                        }

                    }

                    int option = Integer.parseInt(order) - 1;
                    io.delMsg(msg);
                    return option;
                    // return choices.get(option);
                } catch (NumberFormatException | IndexOutOfBoundsException e) {
                    msg.resetAttributes(ReturnType.ChooseOneCard);
                    io.answerCallBack(msg, "❌ 牌型错误,请重新选择", false);
                }
            }

            if (canBeNull) {
                return -1;
            } else {
                // 不选 又不能为空的话强制选第一个
                return 0;
            }
        } finally {
            stepOutPriv(inMsg);
        }
    }

    /**
     * 返回从0开始的 int，发送到公开群里的按键，没有deeplink
     */
    default <E> int chooseFromProvidedPublic(boolean canBeNull, List<E> choices, MsgObj publicMsgObj) {
        if (choices.isEmpty()) {
            // printlnToIO(this + " 无牌可选\n");
            return -1;
        }
        if (getUser() == null) {
            return Utils.randint(0, choices.size() - 1);
        }

        // int i = 1;
        // String out="";
        /**
         * 选人还是选物
         */
        boolean isPerson = false;
        for (E choice : choices) {
            if (!isPerson && (choice instanceof Person)) {
                isPerson = true;
            }
            // getInText().append("[" + choice.toString() + "]");
        }

        MsgAPI msgApi = getGameManager().getMsgAPI();
        MsgObj msg;
        if (canBeNull || isPerson) {
            msg = msgApi.chooseOneFromOpinionCanBeNull(choices, publicMsgObj);
        } else {
            msg = msgApi.chooseOneFromOpinion(choices, publicMsgObj);
        }

        // MsgObj msg= io.chooseOneFromOpinion(choices,inMsg);
        long nextTime = System.currentTimeMillis() + Config.PRIV_RND_TIME_60S;
        TimeLimit t = new TimeLimit(Config.PRIV_RND_TIME_60S);
        while (getGameManager().isRunning() && t.isNotTimeout()) {
            try {
                int timeOut = (int) (nextTime - System.currentTimeMillis()) / 1000;
                String order = msg.getString(ReturnType.ChooseOneCard, timeOut);
                if (order == null || order.equals("q")) {
                    // msgApi.delMsg(msg);
                    if (canBeNull) {
                        return -1;
                    } else {
                        // 不选 又不能为空的话强制选第一个
                        return 0;
                    }
                }
                int option = Integer.parseInt(order) - 1;
                // io.delMsg(msg);
                return option;
                // return choices.get(option);
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                msg.resetAttributes(ReturnType.ChooseOneCardPublic);
                msgApi.answerCallBack(msg, "❌ 牌型错误,请重新选择", false);
            }
        }

        if (canBeNull) {
            return -1;
        } else {
            // 不选 又不能为空的话强制选第一个
            return 0;
        }

    }

    /**
     * 返回从1开始的选择
     * 
     * @param <E>
     * @param choices
     * @return
     */
    default <E> int chooseNoNull(E... choices) {
        int ans = -2;
        TimeLimit t = new TimeLimit(Config.PRIV_RND_TIME_60S);
        do {
            if (t.isTimeout()) {
                break;
            }
            ans = chooseFromProvided(null, false, choices);
        } while (getGameManager().isRunning() && ans == -2);
        return ans + 1;// 这个是补丁
    }

    /**
     * 返回从1开始的选择
     * 
     * @param <E>
     * @param choices
     * @return
     */
    default <E> int chooseNoNull(ArrayList<E> choices) {
        int ans = -2;
        TimeLimit t = new TimeLimit(Config.PRIV_RND_TIME_60S);
        do {
            if (t.isTimeout()) {
                break;
            }
            ans = chooseFromProvided(null, false, choices);
        } while (getGameManager().isRunning() && ans == -2);
        return ans + 1;// 这个是补丁
    }

    default <E> ArrayList<E> chooseManyFromProvided(int num, List<E> choices) {
        return chooseManyFromProvided(num, choices, false, "", "");
    }

    default <E> ArrayList<E> chooseManyFromProvided(int num, List<E> choices, String bottonFront, String bottonBehind) {
        return chooseManyFromProvided(num, choices, false, bottonFront, bottonBehind);
    }

    default <E> ArrayList<E> chooseManyFromProvided(int num, List<E> choices, boolean canBeNull) {
        return chooseManyFromProvided(num, choices, canBeNull, "", "");
    }

    default <E> ArrayList<E> chooseManyFromProvided(int num, List<E> choices, boolean canBeNull, String bottonFront,
            String bottonBehind) {
        if (getUser() == null || choices.isEmpty() || (num != 0 && num > choices.size())) {
            // printlnToIO(this + " has not enough options to choose");
            return new ArrayList<>();
        }

        // int i = 1;
        // String out="";
        List<String> choiceString = new ArrayList<>();
        for (E choice : choices) {
            if (choice instanceof Card) {
                choiceString.add(bottonFront + ((Card) choice).info() + choice.toString() + bottonBehind);
            } else if (choice instanceof Person) {
                Person p = (Person) choice;
                choiceString.add(bottonFront + (p.getGamePlayerNo() > 0 ? Text.circleNum(p.getGamePlayerNo())
                        : "") + p.toString() + bottonBehind);
            } else {
                // System.out.println(bottonFront + choice.toString() + bottonBehind);
                choiceString.add(bottonFront + choice.toString() + bottonBehind);
            }
            // getInText().append("[" + choice.toString() + "]");
        }
        // printlnToIO(out);
        MsgObj inMsg = MsgObj.newMsgObj(getGameManager());
        stepInPriv(inMsg);
        // inMsg.setImg(toString());
        inMsg.text = getPriviMsg().toString();
        inMsg.chatId = getUser().user_id;

        MsgAPI io = getGameManager().getMsgAPI();
        MsgObj msg = io.chooseSomeFromOpinion(choiceString, num, canBeNull, inMsg);
        ArrayList<E> ans = new ArrayList<>();
        long nextTime = System.currentTimeMillis() + Config.PRIV_RND_TIME_60S;
        TimeLimit t = new TimeLimit(Config.PRIV_RND_TIME_60S);
        try {

            while (getGameManager().isRunning() && t.isNotTimeout()) {
                try {
                    int timeOut = (int) (nextTime - System.currentTimeMillis()) / 1000;
                    ans.clear();
                    Object result = msg.getReturnAttr(ReturnType.ChooseMany, timeOut);
                    // System.out.println("Object result=" + result);
                    if (result == null || !(result instanceof List)) {
                        io.delMsg(msg);
                        return ans;
                    }

                    @SuppressWarnings("unchecked")
                    List<String> inputList = (List<String>) result;

                    if (inputList.size() == 0 && (num == 0 || canBeNull)) {
                        io.delMsg(msg);
                        return ans;
                    }
                    // else if (input.equals("help")) {
                    // getGameManager().getIo().showHelp("[choose multiple options]: input several
                    // numbers to " +
                    // "make your choice, split with space, e.g. '1 2 3' ");
                    // return chooseManyFromProvided(num, choices);
                    // }

                    if (inputList.size() != num && num != 0) {// 如果超时了，一样会触发这个提醒，就会报错
                        // System.out.println("❌ 数量错误,你的目前选择数为:" + inputList.size());
                        msg.resetAttributes(ReturnType.ChooseMany);
                        io.answerCallBack(msg, "❌ 数量错误,你的目前选择数为:" + inputList.size(), false);
                        // if (inputList.size() > 0) {// 这里如果没更改，电报会不让你发消息
                        io.renewSomeFromOpinion(choiceString, num, inMsg);
                        // } else {
                        // // 只重置接收，不发消息
                        // inMsg.resetAttributes(ReturnType.ChooseManyThisTime);
                        // inMsg.resetAttributes(ReturnType.ChooseMany);

                        // }
                        continue;
                    }
                    for (String s : inputList) {// 这里可能会为空
                        int option = Integer.parseInt(s) - 1;
                        // if (ans.contains(choices.get(option))) {
                        // printlnToIO("can't choose the same option: " + s);
                        // return chooseManyFromProvided(num, choices);
                        // }
                        ans.add(choices.get(option));
                    }
                    io.delMsg(msg);
                    return ans;
                } catch (NumberFormatException | IndexOutOfBoundsException e) {
                    msg.resetAttributes(ReturnType.ChooseMany);
                    io.answerCallBack(msg, "❌ 未知错误,请重新选择", false);
                }
            }
            return ans;
        } finally {
            stepOutPriv(inMsg);
        }
    }

    default <E> ArrayList<E> chooseManyFromProvidedByOrder(int num, List<E> choices) {
        return chooseManyFromProvidedByOrder(num, choices, false);
    }

    default <E> ArrayList<E> chooseManyFromProvidedByOrder(int num, List<E> choices, boolean canBeNull) {
        if (getUser() == null || choices.isEmpty() || (num != 0 && num > choices.size())) {
            // printlnToIOPriv(this + " has not enough options to choose");
            return new ArrayList<>();
        }

        // int i = 1;
        // String out = "";
        List<String> choiceString = new ArrayList<>();
        for (E choice : choices) {
            if (choice instanceof Card) {
                choiceString.add(((Card) choice).info() + choice.toString());
            } else if (choice instanceof Person) {
                Person p = (Person) choice;
                choiceString.add((p.getGamePlayerNo() > 0 ? Text.circleNum(p.getGamePlayerNo())
                        : "") + p.toString());
            } else {
                choiceString.add(choice.toString());
            }
            // out += ("[" + i++ + "] " + choice.toString() + " ");
        }
        // printlnToIO(out);
        MsgObj inMsg = MsgObj.newMsgObj(getGameManager());
        stepInPriv(inMsg);
        // inMsg.setImg(toString());
        inMsg.text = getPriviMsg().toString();
        inMsg.chatId = getUser().user_id;

        MsgAPI io = getGameManager().getMsgAPI();
        MsgObj msg = io.chooseSomeFromOpinionByOrder(choiceString, num, inMsg);
        ArrayList<E> ans = new ArrayList<>();
        long nextTime = System.currentTimeMillis() + Config.PRIV_RND_TIME_60S;
        TimeLimit t = new TimeLimit(Config.PRIV_RND_TIME_60S);
        try {

            while (getGameManager().isRunning() && t.isNotTimeout()) {
                try {
                    int timeOut = (int) (nextTime - System.currentTimeMillis()) / 1000;
                    ans.clear();
                    Object result = msg.getReturnAttr(ReturnType.ChooseMany, timeOut);
                    // System.out.println("Object result=" + result);
                    if (result == null || !(result instanceof List)) {
                        io.delMsg(msg);
                        return ans;
                    }

                    @SuppressWarnings("unchecked")
                    List<String> inputList = (List<String>) result;

                    if (inputList.size() == 0 && (num == 0 || canBeNull)) {
                        io.delMsg(msg);
                        return ans;
                    }

                    if (inputList.size() != num && num != 0) {
                        // System.out.println("❌ 数量错误,你的目前选择数为:" + inputList.size());
                        msg.resetAttributes(ReturnType.ChooseMany);
                        io.answerCallBack(msg, "❌ 数量错误,你的目前选择数为:" + inputList.size(), false);
                        // if(split.length>0){//这里如果没更改，电报会不让你发消息
                        io.renewSomeFromOpinionByOrder(choiceString, num, inMsg);
                        // }else{
                        // //只重置接收，不发消息
                        // inMsg.resetAttributes(AttrType.ChooseManySingle);
                        // inMsg.resetAttributes(AttrType.ChooseMany);

                        // }
                        continue;
                    }
                    for (String s : inputList) {
                        int option = Integer.parseInt(s) - 1;
                        ans.add(choices.get(option));
                    }
                    io.delMsg(msg);
                    return ans;
                } catch (NumberFormatException | IndexOutOfBoundsException e) {
                    msg.resetAttributes(ReturnType.ChooseMany);
                    io.answerCallBack(msg, "❌ 未知错误,请重新选择", false);
                }
            }
            return ans;
        } finally {
            stepOutPriv(inMsg);
        }
    }

    /**
     * 选一张卡，不能为空
     * 
     * @param cs
     * @return
     */
    default Card chooseCard(List<Card> cs) {
        return chooseCard(cs, false);
    }

    /**
     * 私人界面选卡，不会丢卡，不像request
     * 
     * @param cs
     * @param allowNull
     * @return
     */
    default Card chooseCard(List<Card> cs, boolean allowNull) {
        return chooseCard(cs, allowNull, "", "");
    }

    /**
     * 私人界面选卡，不会丢卡，不像request
     * 
     * @param cs
     * @param allowNull
     * @return
     */
    default Card chooseCard(List<Card> cs, boolean allowNull, String buttonFront, String buttonBehind) {
        if (cs == null || cs.size() == 0) {
            return null;
        }
        ArrayList<String> showText = new ArrayList<>();
        for (Card c : cs) {
            showText.add(buttonFront + c.info() + c.toString() + buttonBehind);
        }
        int choose = chooseFromProvided(null, allowNull, showText);
        if (choose >= 0) {
            return cs.get(choose);
        } else {
            return null;
        }
    }

    /**
     * 公开页面选卡,不主动删除消息，不会丢卡，不像request
     * 
     * @param cs
     * @param allowNull
     * @return
     */
    default Card chooseCardPublic(ArrayList<Card> cs, boolean allowNull, MsgObj publicMsgObj) {
        if (cs == null || cs.size() == 0) {
            return null;
        }
        ArrayList<String> showText = new ArrayList<>();
        for (Card c : cs) {
            showText.add(c.info() + c.toString());
        }
        int choose = chooseFromProvidedPublic(allowNull, showText, publicMsgObj);
        if (choose >= 0) {
            return cs.get(choose);
        } else {
            return null;
        }
    }

    /**
     * 公开页面选多张卡,不会丢卡，不像request，如果要可取消的用 chooseManyFromProvided
     * 
     * @param num
     * @param cs
     * @return
     */
    default ArrayList<Card> chooseManyCards(int num, List<Card> cs) {
        return chooseManyFromProvided(num, cs);
    }

    /**
     * 公开页面选多张卡,不会丢卡，不像request,加上按键前后的图标，如果要可取消的用 chooseManyFromProvided
     * 
     * @param num
     * @param cs
     * @return
     */
    default ArrayList<Card> chooseManyCards(int num, List<Card> cs, String bottonFront, String bottonBehind) {
        return chooseManyFromProvided(num, cs, bottonFront, bottonBehind);
    }

    /**
     * 抽出匿名的牌，这些地方可能牌是会有变化的，比如打出了无懈，又被别人解除了,不会丢卡，不像request
     * 
     * @param cs
     * @return
     */

    default Card chooseAnonymousCard(List<Card> cs) {

        Utils.assertTrue(!cs.isEmpty(), "cards are empty");

        if (cs.size() == 1) {
            return cs.get(0);
        }
        ArrayList<String> options = new ArrayList<>();
        for (int i = 0; i < cs.size(); i++) {
            options.add("🃏 牌" + (i + 1));
        }
        int opt = chooseNoNull(options);
        Collections.shuffle(cs);
        // System.out.println("chooseAnonymousCard idx=" + opt);
        if (opt < 1) {
            opt = 1;
        }
        return cs.get(opt - 1);
        // return cs.get(Integer.parseInt(opt.replace("card","")) - 1);
    }

    /**
     * 私聊，发技能
     * 
     * @param skillName
     * @return
     */
    default boolean launchSkillPriv(String skillName) {
        return launchSkillPriv(null, skillName);
    }

    /**
     * 私聊，发技能，能传入msg对象
     * 
     * @param privMsg
     * @param skillName
     * @return
     */
    default boolean launchSkillPriv(MsgObj privMsg, String skillName) {
        if (this.isAI()) {
            // printlnPriv(this + " uses " + skillName);
            return true;
        }
        if (isZuoCi() && !isActiveSkill(skillName)) {
            return false;
        }
        int choice = chooseFromProvided(privMsg, true, skillName);
        // if (choice != -1 && choice.equals(skillName)) {
        if (choice == 0) {
            // printlnPriv(this + " uses " + skillName);
            return true;
        }
        return false;
    }

    /**
     * 在群里询问释放技能，在私聊也出询问选项，在私聊出后续选项,显示图片
     * 
     * @param skillName
     * @param skillDesc
     * @param skillCode
     * @return
     */
    default boolean launchSkillPublicDeepLink(String skillName, String skillDesc, String skillCode) {
        MsgObj publicMsg = MsgObj.newMsgObj(getGameManager());
        return launchSkillPublicDeepLink(publicMsg, skillName, skillDesc, skillCode, true);
    }

    /**
     * 在群里询问释放技能，在私聊也出询问选项，在私聊出后续选项,是否显示图片
     * 
     * @param skillName
     * @param skillDesc
     * @param skillCode
     * @return
     */
    default boolean launchSkillPublicDeepLink(String skillName, String skillDesc, String skillCode, boolean showImg) {
        MsgObj publicMsg = MsgObj.newMsgObj(getGameManager());
        return launchSkillPublicDeepLink(publicMsg, skillName, skillDesc, skillCode, showImg);
    }

    /**
     * 在群里询问释放技能，在私聊也出询问选项，在私聊出后续选项,点击了会清除按钮, 显示图片
     * 
     * @param msgObj
     * @param skillName
     * @param skillDesc
     * @param skillCode
     * @return
     */
    default boolean launchSkillPublicDeepLink(MsgObj msgObj, String skillName, String skillDesc, String skillCode) {
        return launchSkillPublicDeepLink(msgObj, skillName, skillDesc, skillCode, true);
    }

    /**
     * 在群里询问释放技能，在私聊也出询问选项，在私聊出后续选项,点击了会清除按钮,是否显示图片
     * 
     * @param skillName
     * @param skillDesc
     * @param skillCode
     * @return
     */
    default boolean launchSkillPublicDeepLink(MsgObj msgObj, String skillName, String skillDesc, String skillCode,
            boolean showImg) {
        if (this.isAI()) {
            msgObj.text = skillDesc;
            msgObj.chatId = getGameManager().getChatId();
            if (showImg) {
                msgObj.setImg(toString());
            }
            getGameManager().getMsgAPI().sendMsg(msgObj);

            return false;
        } else {
            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<String> atomResult = new AtomicReference<>(null);
            MsgObj privateMsg = MsgObj.newMsgObj(getGameManager());
            MsgObj publicMsgObj = msgObj;
            PlayerIO thisPerson = this;

            getGameManager().getMsgAPI().submitRunnable(new Runnable() {
                @Override
                public void run() {
                    try {
                        getPriviMsg().setOneTimeInfo1(skillDesc);
                        boolean actived = launchSkillPriv(privateMsg, skillName);
                        if (actived) {
                            atomResult.compareAndSet(null, skillCode);
                        } else {
                            atomResult.compareAndSet(null, "q");
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        latch.countDown();
                    }
                }

            });

            getGameManager().getMsgAPI().submitRunnable(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (getUser() == null) {
                            return;
                        }
                        List<String[]> buttonList = new ArrayList<>();
                        buttonList.add(new String[] { "👉" + skillName, skillCode, "deeplink" });
                        publicMsgObj.chatId = getGameManager().getChatId();
                        publicMsgObj.user_chatId = getUser().user_id;
                        if (showImg) {
                            publicMsgObj.setImg(thisPerson.toString());
                        }

                        publicMsgObj.text = skillDesc;
                        thisPerson.putTempActionMsgObj(skillCode, publicMsgObj);
                        getGameManager().getMsgAPI().chooseOneDeepLink(
                                buttonList, publicMsgObj);
                        String result = publicMsgObj.getString(ReturnType.Deeplink,
                                Config.PUBLIC_NOTICE_TIME_25S / 1000);

                        getGameManager().getMsgAPI().clearButtons(publicMsgObj);

                        if (skillCode.equals(result)) {
                            atomResult.compareAndSet(null, skillCode);
                        } else {
                            atomResult.compareAndSet(null, "q");
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        latch.countDown();
                    }
                }

            });

            stepInPriv(privateMsg);
            try {

                try {
                    latch.await(Config.PUBLIC_NOTICE_TIME_25S / 1000, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                getGameManager().getMsgAPI().delMsg(privateMsg);
                privateMsg.unlockAttrbutes(ReturnType.ChooseOneCard);
                // //sleep(1000);
                // if(publicMsgObj.replyMakup!=null){
                // getGameManager().getMsgIO().clearButtons(publicMsgObj);
                // }

                publicMsgObj.unlockAttrbutes(ReturnType.Deeplink);
                if (skillCode.equals(atomResult.get())) {
                    return true;
                }
                return false;
            } finally {
                stepOutPriv(privateMsg);
            }
        }

    }

    /**
     * 在群里释放技能，私聊也发送一个对话，没有deeplink,只删除按钮，不删除消息
     * 
     * @param skillName
     * @param skillDesc
     * @param skillCode
     * @return
     */
    default boolean launchSkillPublic(String skillName, String skillDesc, String skillCode) {
        MsgObj publicMsgObj = MsgObj.newMsgObj(getGameManager());
        return launchSkillPublic(publicMsgObj, skillName, skillDesc, skillCode, true);
    }

    /**
     * 在群里释放技能，私聊也发送一个对话，没有deeplink,只删除按钮，不删除消息
     * 
     * @param skillName
     * @param skillDesc
     * @param skillCode
     * @return
     */
    default boolean launchSkillPublic(String skillName, String skillDesc, String skillCode, boolean showImg) {
        MsgObj publicMsgObj = MsgObj.newMsgObj(getGameManager());
        return launchSkillPublic(publicMsgObj, skillName, skillDesc, skillCode, showImg);
    }

    /**
     * 在群里释放技能，私聊也发送一个对话，没有deeplink,只删除按钮，不删除消息，显示图片
     * 
     * @param publicMsgObj
     * @param skillName
     * @param skillDesc
     * @param skillCode
     * @return
     */
    default boolean launchSkillPublic(MsgObj publicMsgObj, String skillName, String skillDesc, String skillCode) {
        return launchSkillPublic(publicMsgObj, skillName, skillDesc, skillCode, true);
    }

    /**
     * 在群里释放技能，私聊也发送一个对话，没有deeplink,只删除按钮，不删除消息，是否显示图片
     * 
     * @param publicMsgObj
     * @param skillName
     * @param skillDesc
     * @param skillCode
     * @param showImg
     * @return
     */
    default boolean launchSkillPublic(MsgObj publicMsgObj, String skillName, String skillDesc, String skillCode,
            boolean showImg) {

        if (this.isAI()) {
            return false;
        } else {

            CountDownLatch latch = new CountDownLatch(1);
            AtomicReference<String> atomResult = new AtomicReference<>(null);
            MsgObj privateMsg = MsgObj.newMsgObj(getGameManager());
            // MsgObj publicMsgObj = MsgObj.newMsgObj(getGameManager());
            PlayerIO thisPerson = this;

            getGameManager().getMsgAPI().submitRunnable(new Runnable() {
                @Override
                public void run() {
                    try {
                        getPriviMsg().setOneTimeInfo1(skillDesc);
                        boolean actived = launchSkillPriv(privateMsg, skillName);
                        if (actived) {
                            atomResult.compareAndSet(null, skillCode);
                        } else {
                            atomResult.compareAndSet(null, "q");
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        latch.countDown();
                    }
                }

            });

            getGameManager().getMsgAPI().submitRunnable(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (getUser() == null) {
                            return;
                        }
                        List<String[]> buttonList = new ArrayList<>();
                        buttonList.add(new String[] { "👉" + skillName, skillCode });
                        publicMsgObj.chatId = getGameManager().getChatId();
                        publicMsgObj.user_chatId = getUser().user_id;
                        if (showImg) {
                            publicMsgObj.setImg(thisPerson.toString());
                        }
                        publicMsgObj.text = skillDesc;
                        thisPerson.putTempActionMsgObj(skillCode, publicMsgObj);
                        getGameManager().getMsgAPI().chooseOnePublic(
                                buttonList, publicMsgObj);
                        String result = publicMsgObj.getString(ReturnType.ChooseOneOpinionPublic,
                                Config.PUBLIC_NOTICE_TIME_25S / 1000);

                        getGameManager().getMsgAPI().clearButtons(publicMsgObj);

                        if (skillCode.equals(result)) {
                            atomResult.compareAndSet(null, skillCode);
                        } else {
                            atomResult.compareAndSet(null, "q");
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        latch.countDown();
                    }
                }

            });

            stepInPriv(privateMsg);
            try {

                try {
                    latch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                getGameManager().getMsgAPI().delMsg(privateMsg);
                privateMsg.unlockAttrbutes(ReturnType.ChooseOneCard);
                // //sleep(1000);
                // if(publicMsgObj.replyMakup!=null){
                // getGameManager().getMsgIO().clearButtons(publicMsgObj);
                // }

                publicMsgObj.unlockAttrbutes(ReturnType.ChooseOneOpinionPublic);
                if (skillCode.equals(atomResult.get())) {
                    return true;
                }
                return false;
            } finally {
                stepOutPriv(privateMsg);
            }
        }

    }

    default Card chooseTargetCards(Person target, boolean includeJudge) {
        // printlnToIOPriv("choose a card from " + target);
        // printlnToIO("chooseTargetCards:"+target.showAllCards());

        int option;
        if (includeJudge && !target.getEquipments().isEmpty()
                && !target.getRealJudgeCards().isEmpty()) {

            option = chooseNoNull("手牌", "装备牌", "判定牌");
        } else if (!target.getEquipments().isEmpty()) {

            option = chooseNoNull("手牌", "装备牌");
        } else if (includeJudge && !target.getRealJudgeCards().isEmpty()) {

            option = chooseNoNull("手牌", "判定牌");
        } else {

            option = 1;
        }

        // 如果手牌也为空，则返回空
        if (option == 1 && target.getCards().isEmpty()) {//
            return null;

        }
        Card c = null;

        getPriviMsg().setOneTimeInfo1("\n💬请选择TA的一张牌");
        TimeLimit t = new TimeLimit(Config.PRIV_RND_TIME_60S);
        while (getGameManager().isRunning() && c == null) {
            if (t.isTimeout()) {
                return c;
            }
            if (option == 1) {
                c = chooseAnonymousCard(target.getCards());
            } else if (option == 2) {
                c = chooseCard(new ArrayList<>(target.getEquipments().values()), false);
            } else {
                c = chooseCard(new ArrayList<>(target.getRealJudgeCards()), false);
            }
        }
        return c;
    }

    /**
     * 随机抽手牌
     * 
     * @param target
     * @return
     */
    default Card randomChooseTargetCards(Person target) {
        return randomChooseTargetCards(target, false);
    }

    /**
     * 随机抽卡，可抽装备
     * 
     * @param target
     * @return
     */
    default Card randomChooseTargetCards(Person target, boolean fromEquipments) {
        if (fromEquipments) {
            List<Card> list = target.getCardsAndEquipments();
            if (list.size() <= 0) {
                return null;
            }
            return list.get(Utils.randint(0, list.size() - 1));

        } else {

            if (target.getCards().size() <= 0) {
                return null;
            }
            return target.getCards().get(Utils.randint(0, target.getCards().size() - 1));
        }

    }

    default Card chooseTargetCards(Person target) {
        return chooseTargetCards(target, false);
    }

    default Person selectPlayer(List<Person> people) {
        return selectPlayer(people, false);
    }

    /**
     * 不选自己
     * 
     * @return
     */
    default Person selectPlayer() {
        return selectPlayer(getGameManager().getPlayersBeginFromPlayer((Person) this));
    }

    default Person selectPlayer(boolean chooseSelf) {
        return selectPlayer(getGameManager().getPlayersBeginFromPlayer((Person) this), chooseSelf);
    }

    /**
     * 去除一些人,可为空
     * 
     * @param chooseSelf
     * @return
     */
    default Person selectPlayerExept(List<Person> people, Person... exeptPerson) {
        ArrayList<Person> options = new ArrayList<>();
        for (Person p1 : people) {
            options.add(p1);
        }
        for (Person p1 : exeptPerson) {
            options.remove(p1);
        }
        if (options.size() == 0) {
            return null;
        }

        // printlnToIO("choose a player:");
        // getPriviMsg().setOneTimeInfo1(Text.format("\n💬请选择1个目标:", toString()));
        int option = chooseFromProvided(null, true, options);
        if (option == -1) {
            return null;
        }
        Person selected = options.get(option);
        for (Person p1 : people) {
            if (p1.equals(selected)) {
                return p1;
            }
        }
        return null;
    }

    default Person selectPlayer(List<Person> people, boolean chooseSelf) {
        ArrayList<Person> options = new ArrayList<>();
        for (Person p1 : people) {
            options.add(p1);
        }
        if (!chooseSelf) {
            options.remove(this);
        }
        if (options.size() == 0) {
            return null;
        }
        // printlnToIO("choose a player:");
        // getPriviMsg().setOneTimeInfo1(Text.format("\n💬请选择1个目标:", toString()));
        int option = chooseFromProvided(null, true, options);
        if (option == -1) {
            return null;
        }
        Person selected = options.get(option);
        for (Person p1 : people) {
            if (p1.equals(selected)) {
                return p1;
            }
        }
        return null;
    }

    ArrayList<Card> getCards();

    HashMap<EquipType, Equipment> getEquipments();

    ArrayList<JudgeCard> getJudgeCards();

    ArrayList<Card> getRealJudgeCards();

    Identity getIdentity();

    int getHP();

    int getMaxHP();

    /**
     * 失去卡，没有说是主动还是被动失去
     * 
     * @param c
     */
    void loseCard(Card c);

    boolean isWakenUp();

    boolean hasWuShuang();

    boolean isDrunk();

    boolean isDrunkShaUsed();

    boolean isDaWu();

    boolean isKuangFeng();

    boolean isTurnedOver();

    boolean isLinked();

    String getPlayerStatus(Person watcher, boolean privateAccess);

    /**
     * 获取技能，这个可以在具体人物里覆盖，特别是应对左慈的化身
     * 
     * @return
     */
    Set<String> getInitialSkills();

    Set<String> getActiveSkills();

    void addCard(Card c, boolean print);

    boolean isZuoCi();

    /** 左慈加技能 */
    void addActiveSkill(String... skill);

    /** 左慈加技能,删掉原来的，加上特定的 */
    void clearAndAddActiveSkill(String... skill);

    /** 左慈判断技能,这个要在某些具体的角色里覆盖 */
    boolean isActiveSkill(String skill);

    public String getHtmlName();

    public String getPlateName();

    public void setUser(SanUser user);

    public SanUser getUser();

    public boolean isAI();

    /**
     * 有些 杀 无懈可击，桃，之类公共消息需要编辑，追加，给这个一个方法来缓存消息
     * 
     * @return
     */
    public MsgObj getTempActionMsgObj(String msg_name);

    /**
     * 有些 杀 无懈可击，桃，之类公共消息需要编辑，追加，给这个一个方法来缓存消息
     * 按优先顺序，返回这些标签中不为空的第一个，
     * 
     * @return
     */
    public MsgObj getTempActionMsgObjFirstOrder(String... msg_name);

    /**
     * 有些 杀 无懈可击，桃，之类公共消息需要编辑，追加，给这个一个方法来缓存消息
     * 按优先顺序，返回这些标签中不为空的第一个，否则就输出给定的msg
     * 
     * @return
     */
    public MsgObj getTempActionMsgObjFirstOrder(MsgObj lastChoose, String... msg_name);

    /**
     * 有些 杀 无懈可击，桃，之类公共消息需要编辑，追加，给这个一个方法来缓存消息,只能取一次，就会变成空
     */
    public void putTempActionMsgObj(String msg_name, MsgObj msgObj);

    /**
     * 删除公共消息链接
     */
    public void removeTempActionMsgObj(String msg_name);

    public void sleep(long time);

    /**
     * 设置进度条
     * 
     * @param progress
     */
    public void setProgress(SanProgress progress);

    /**
     * 获取进度条
     * 
     * @return
     */
    public SanProgress getProgress();

    /**
     * 给定一个初始编号，给玩家好记住
     * 
     * @param i
     */
    public void setGamePlayerNo(int i);

    /**
     * 定一个初始编号，给玩家好记住
     * 
     * @param i
     */
    public int getGamePlayerNo();

    /**
     * 获取装备和手牌，装备牌排在前面
     * 
     * @return
     */
    public ArrayList<Card> getCardsAndEquipments();

    /**
     * 只获取装备
     * 
     * @return
     */
    public ArrayList<Card> getEquipmentsList();

    public void setFastMode(boolean fastMode);

}
