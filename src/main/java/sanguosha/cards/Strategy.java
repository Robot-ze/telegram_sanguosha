package sanguosha.cards;

import sanguosha.cards.basic.Sha;
import sanguosha.manager.GameManager;
import sanguosha.people.AI;
import sanguosha.people.Person;
import sanguosha.people.PlayerIO;
import components.BlockingMap;
import components.MyCountDownLatch;
import components.TimeLimit;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Set;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicReference;

import config.Config;
import config.DescUrl;
import config.Text;
import msg.CallbackEven;
import msg.MsgObj;
import msg.ReturnType;

import java.util.concurrent.TimeUnit;

public abstract class Strategy extends Card {
    private int distance;
    /**
     * 全局打击的技能
     */
    protected boolean globlStrategy = false;

    public Strategy(GameManager gameManager, Color color, int number, int distance) {
        super(gameManager, color, number);
        this.distance = distance;
    }

    public Strategy(GameManager gameManager, Color color, int number) {
        super(gameManager, color, number);
        this.distance = 100;
    }

    public boolean allNoWuXie() {
        // boolean hasWuXie = false;
        for (Person p : getGameManager().getPlayersBeginFromPlayer(getSource())) {
            if (p instanceof AI) {// 忽略AI
                continue;
            }
            if (p.hasWuXieReplace()) {
                // hasWuXie = true;
                // break;
                return false;
            }
            for (Card c : p.getCards()) {
                if (c.toString().equals("无懈可击")) {
                    // hasWuXie = true;
                    // break;
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 
     * @param target
     * @return
     */
    public boolean gotWuXie(Person target) {
        return gotWuXie(target, true);
    }

    /**
     * 这个无懈可击会一直抵消到大家都没有或不出无懈可击
     * 这个无懈的流程太慢了，这些群体人多了技能一套下来要5分钟，而且猛刷信息，不太适合群里
     * 
     * @param target
     * @param showImg 带不带技能图,比如群体技能作用的第二个人
     * @return
     */
    public boolean gotWuXie(Person target, boolean showImg) {
        if (target == null) {
            return false;
        }
        MsgObj publicMsgObj = MsgObj.newMsgObj(getGameManager());
        target.putTempActionMsgObj("wuxie", publicMsgObj);// 方便别人改消息
        if (showImg) {
            publicMsgObj.setImg(this.toString());
        }
        publicMsgObj.chatId = getGameManager().getChatId();
        publicMsgObj.text = Text.format("%s 对 %s %s",
                getSource().getPlateName(),
                target.getHtmlName(),
                getHtmlNameWithColor()

        );
        String preText = Text.format("%s 对 %s",
                getHtmlNameWithColor(),
                target.getHtmlName()

        );
        // boolean ans = false;
        /**
         * 出牌人
         */
        List<Person> showCardPersonList = new CopyOnWriteArrayList<>();
        // 这样存是为了callback那边能取
        publicMsgObj.forPlayers = new CopyOnWriteArrayList<>();

        String buttonValue = "wuxiekeji";

        publicMsgObj.sonMsgs = ConcurrentHashMap.newKeySet();
        publicMsgObj.sonCallback = new ConcurrentHashMap<>();
        Set<Person> giveUpPersons = ConcurrentHashMap.newKeySet();
        Set<Person> deepLinkPersons = ConcurrentHashMap.newKeySet();
        Deque<MyCountDownLatch> deepLinkLatchs = new ConcurrentLinkedDeque<>();
        TimeLimit t = new TimeLimit(Config.WUXIE_WAIT_TIME_ROOP_60S);
        int round = 0;
        /**
         * 记录一下上一次的出牌人数，因为有的出假牌，可能不计算进去
         */
        int lastShowCardNum = 0;
        while (getGameManager().isRunning() && t.isNotTimeout()) {

            AtomicReference<PlayerIO> showCardPerson = new AtomicReference<>(null);
            publicMsgObj.sonCallback.clear();
            for (MsgObj sonMsg : publicMsgObj.sonMsgs) {
                getGameManager().getMsgAPI().delMsg(sonMsg);
            }
            publicMsgObj.forPlayers.clear();
            publicMsgObj.sonMsgs.clear();

            for (Person p : getGameManager().getPlayers()) {
                if (p.getUser() != null && p.singleCheckWuxie()) {// 不是AI
                    // 指向性锦囊排除第一轮排除锦囊的出牌人
                    if (!globlStrategy && showCardPersonList.size() == 0 && p == getSource()) {
                        // System.out.println("排除" + this);
                        continue;//
                    }
                    publicMsgObj.forPlayers.add(p);
                }

            }

            MyCountDownLatch mainlatch = MyCountDownLatch.newInst(publicMsgObj.forPlayers.size(), getGameManager());

            deepLinkPersons.clear();
            giveUpPersons.clear();
            deepLinkLatchs.clear();

            for (Person p : publicMsgObj.forPlayers) {
                p.getPriviMsg().clearHeader2();
                // p.getInText().clearInfo1();
                // p.getInText().clearInfo2();
                p.getPriviMsg().clearOneTimeInfo1();
                p.getPriviMsg().setOneTimeInfo1(getSource() + Text.format(" 对 %s %s\n", target.toString(), this));
                if (showCardPersonList.size() % 2 == 0) {
                    for (Person sp : showCardPersonList) {
                        p.getPriviMsg().appendOneTimeInfo1(Text.format("\n💬<s>%s 打出</s>", sp));
                    }
                    p.getPriviMsg().appendOneTimeInfo1(Text.format("\n💬用无懈可击,使 %s 无效", this));
                } else {
                    for (int i = 0; i < showCardPersonList.size() - 1; i++) {
                        p.getPriviMsg()
                                .appendOneTimeInfo1(
                                        Text.format("\n💬<s>%s 打出</s>", showCardPersonList.get(i)));
                    }
                    p.getPriviMsg().appendOneTimeInfo1(
                            Text.format("\n<b>💬%s 打出</b>", showCardPersonList.get(showCardPersonList.size() - 1)));
                    p.getPriviMsg().appendOneTimeInfo1(Text.format("\n💬继续用无懈可击抵消"));
                }

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
                                    mainlatch.countDown();

                                    getGameManager().getMsgAPI().answerCallBack(
                                            thisCallBackId, "❌ 你放弃打出无懈", false);
                                } else {
                                    getGameManager().getMsgAPI().answerCallBack(
                                            thisCallBackId, "❌ 你已操作过", false);
                                }
                            } else if (buttonValue.equals(callBackData)) {
                                if (deepLinkPersons.contains(p) || giveUpPersons.contains(p)) {
                                    return;
                                }
                                deepLinkPersons.add(p);
                                // 这一段是如果有人决定出无懈，就要等待这个无懈，要不10秒跳出得太早了
                                MyCountDownLatch singlePersonLatch = MyCountDownLatch.newInst(1, getGameManager());
                                deepLinkLatchs.add(singlePersonLatch);
                                try {
                                    Card wuxieCard = p.requestWuXie(showCardPerson, sonMsgObj);
                                    if (wuxieCard != null) {
                                        showCardPersonList.add(p);
                                        // 如果这个给出了牌，直接解锁
                                        while (getGameManager().isRunning() && mainlatch.getCount() != 0) {
                                            mainlatch.countDown();
                                        }
                                        p.useStrategy();// 这里出无懈也会触发黄月英的技能，不过就不在同一线程，可能有问题

                                    } else {
                                        if (!giveUpPersons.contains(p)) {
                                            giveUpPersons.add(p);
                                            mainlatch.countDown();
                                            if (p.equals(showCardPerson.get())) {// 出了假牌
                                                showCardPerson.set(null);
                                            }
                                        }
                                    }
                                } finally {
                                    singlePersonLatch.countDown();
                                }

                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                });

            }
            String htmlDescWuxie = DescUrl.getDescHtml("无懈可击");

            if (round == 0) {
                getGameManager().getMsgAPI().noticeAndAskOneForMulty(publicMsgObj, "无懈可击", buttonValue);

            }

            round++;

            if (allNoWuXie()) {// 第一个也是已经是最后一个了

                // sleep(1000);
                System.out.println("round=" + round);
                editWuxieInfo(htmlDescWuxie, preText, showImg, round, publicMsgObj, showCardPersonList, true);

                Person.clearPrivShowTextExectHeader(showCardPersonList);

                for (MsgObj sonMsg : publicMsgObj.sonMsgs) {
                    sonMsg.setAttributes(ReturnType.wuxieStop, true);
                    getGameManager().getMsgAPI().delMsg(sonMsg);
                }
                return false;
            }
            // System.out.println("0------publicMsgObj.sonMsgs=" + publicMsgObj.sonMsgs);
            mainlatch.await(Config.WUXIE_WAIT_TIME_15S, TimeUnit.MILLISECONDS);

            // 如果人点了无懈，这里还要等待无懈那部分
            while (!deepLinkLatchs.isEmpty()) {
                deepLinkLatchs.pop().await(Config.PRIV_RND_TIME_60S, TimeUnit.MILLISECONDS);
            }

            // 删掉已发出到私人的消息
            // System.out.println("1publicMsgObj.sonMsgs=" + publicMsgObj.sonMsgs);

            for (MsgObj sonMsg : publicMsgObj.sonMsgs) {

                // 这里不能批量删，因为chatid不一样
                sonMsg.setAttributes(ReturnType.wuxieStop, true);
                getGameManager().getMsgAPI().delMsg(sonMsg);
            }
            // System.out.println("2publicMsgObj.sonMsgs=" + publicMsgObj.sonMsgs);
            // System.out.println("showCardPerson.get()=" + showCardPerson.get());
            if (showCardPerson.get() == null) {
                // sleep(3000);
                System.out.println("没人出无懈");

                // sleep(1000);
                // System.out.println("round=" + round);
                editWuxieInfo(htmlDescWuxie, preText, showImg, round, publicMsgObj, showCardPersonList, true);

                Person.clearPrivShowTextExectHeader(showCardPersonList);

                for (MsgObj sonMsg : publicMsgObj.sonMsgs) {// 重复操作
                    sonMsg.setAttributes(ReturnType.wuxieStop, true);
                    getGameManager().getMsgAPI().delMsg(sonMsg);
                }
                return showCardPersonList.size() % 2 == 1;
            } else {// 有人出无懈

                if (allNoWuXie()) {// 有人出了牌，没人再有无懈
                    // System.out.println("没人有无懈");
                    // 当只有一个无懈可击时，会不出现那个技能图
                    // if (showCardPersonList.size() > 1) {
                    // publicMsgObj.text = formStringShowCardDone(showCardPersonList, htmlDescWuxie,
                    // preText);
                    // }
                    // publicMsgObj.replyMakup = null;
                    // sleep(1000);
                    editWuxieInfo(htmlDescWuxie, preText, showImg, round, publicMsgObj, showCardPersonList, true);

                    Person.clearPrivShowTextExectHeader(showCardPersonList);

                    for (MsgObj sonMsg : publicMsgObj.sonMsgs) {
                        sonMsg.setAttributes(ReturnType.wuxieStop, true);
                        getGameManager().getMsgAPI().delMsgForce(sonMsg);
                    }

                    try {
                        return showCardPersonList.size() % 2 == 1;
                    } finally {
                        if (showCardPersonList.size() == 1) {
                            String res = formStringShowCardDone(showCardPersonList, htmlDescWuxie, preText);
                            // sleep(1000);
                            getGameManager().getIo().printlnPublic(res, "无懈可击");
                        }
                    }

                } else {// 有人出了牌，又有人还有无懈
                    if (showCardPersonList.size() > lastShowCardNum) {
                        // 补丁补丁补丁补丁补丁补丁
                        // --------还要把之前的callbackmap取消按键 清理一下，要不会出现回调等待id错误

                        System.out.println("round=" + round);
                        editWuxieInfo(htmlDescWuxie, preText, showImg, round, publicMsgObj, showCardPersonList, false);
                        // getGameManager().getMsgAPI().clearButtons(publicMsgObj,false);// 把原来的按钮删掉
                        // sleep(1000);

                        long preMsgId = publicMsgObj.getReturnMsgId(0);
                        System.out.println("preMsgId======" + preMsgId);
                        if (preMsgId != -1) {
                            // 这里给取消按键
                            String callBackCodePre = publicMsgObj.chatId + "_" + preMsgId;
                            getGameManager().getMsgAPI().getCallBackMap().remove(callBackCodePre);
                            // publicMsgObj.resetAttributes(ReturnType.Msgid);
                        }

                        // String callBackCodePre = publicMsgObj.chatId + "_" + preMsgId;
                        // System.out.println(" getGameManager().getMsgAPI().getCallBackMap().get()" +
                        // getGameManager().getMsgAPI().getCallBackMap().get(callBackCodePre));
                        // String showText = preText;

                        publicMsgObj.setImg("无懈可击");
                        publicMsgObj.text = formStringShowCardRunning(showCardPersonList, htmlDescWuxie);

                        getGameManager().getMsgAPI().noticeAndAskOneForMulty(
                                publicMsgObj,
                                "继续无懈可击",
                                buttonValue);

                        for (MsgObj sonMsg : publicMsgObj.sonMsgs) {
                            sonMsg.setAttributes(ReturnType.wuxieStop, true);
                            getGameManager().getMsgAPI().delMsgForce(sonMsg);
                        }
                    } else {
                        // 有人出了无懈，有人还有无懈，但是有人出了假牌被作废，这时候 lastShowCardNum 不会增加
                        // 比如蛊惑，于吉竞争出牌了，被质疑了，返回牌是null，就会出现这种情况
                        // System.out.println("怎么会是空的？？？");

                        // publicMsgObj.text = formStringShowCardDone(showCardPersonList, htmlDescWuxie,
                        // preText);
                        // publicMsgObj.replyMakup = null;
                        // sleep(1000);
                        System.out.println("round=" + round);
                        editWuxieInfo(htmlDescWuxie, preText, showImg, round, publicMsgObj, showCardPersonList, true);
                        Person.clearPrivShowTextExectHeader(showCardPersonList);

                        for (MsgObj sonMsg : publicMsgObj.sonMsgs) {// 重复操作
                            sonMsg.setAttributes(ReturnType.wuxieStop, true);
                            getGameManager().getMsgAPI().delMsgForce(sonMsg);
                        }
                        return showCardPersonList.size() % 2 == 1;
                    }

                    lastShowCardNum = showCardPersonList.size();

                }
            }

        }

        for (MsgObj sonMsg : publicMsgObj.sonMsgs) {
            sonMsg.setAttributes(ReturnType.wuxieStop, true);
            getGameManager().getMsgAPI().delMsg(sonMsg);
        }
        if (publicMsgObj.editCount.get() == 0) {
            getGameManager().getMsgAPI().clearButtons(publicMsgObj);
        }
        return showCardPersonList.size() % 2 == 1;
    }

    private void editWuxieInfo(String htmlDescWuxie, String preText,
            boolean showImg, int round, MsgObj publicMsgObj,
            List<Person> showCardPersonList, boolean end) {

        if (end || showCardPersonList.size() > 1) {
            publicMsgObj.text = formStringShowCardDone(showCardPersonList, htmlDescWuxie, preText);
        }

        publicMsgObj.replyMakup = null;
        if (showImg && round == 1) {
            getGameManager().getMsgAPI().editCaptionForce(publicMsgObj, end);// 删掉按钮
        } else {
            if (end && showCardPersonList.size() > 0) { // 显示出过无懈的最后那条
                getGameManager().getMsgAPI().editCaptionForce(publicMsgObj, end);// 删掉按钮
            } else {
                sleep(1000L);
                getGameManager().getMsgAPI().delMsgForce(publicMsgObj, false);// 删掉按钮
                sleep(1000L);
            }
        }
    }

    private String formStringShowCardDone(List<Person> showCardPersonList, String htmlDescWuxie, String preText) {
        String showText = "";
        if (showCardPersonList.size() % 2 == 0) {
            for (Person sp : showCardPersonList) {
                showText += (Text.format("\n<s>%s %s ！</s>",
                        sp.getHtmlName(), htmlDescWuxie));
            }
            showText += (Text.format("\n%s 生效", preText));
        } else {
            for (int i = 0; i < showCardPersonList.size() - 1; i++) {
                showText += (Text.format("\n<s>%s %s ！</s>",
                        showCardPersonList.get(i).getHtmlName(),
                        htmlDescWuxie));
            }
            showText += (Text.format("\n<b>%s %s ！</b>",
                    showCardPersonList.get(showCardPersonList.size() - 1).getHtmlName(),
                    htmlDescWuxie));
            showText += (Text.format("\n%s 失效", preText));
        }
        return showText;
    }

    private String formStringShowCardRunning(List<Person> showCardPersonList, String htmlDescWuxie) {

        String showText = "";
        if (showCardPersonList.size() % 2 == 0) {
            for (Person sp : showCardPersonList) {
                showText += (Text.format("\n<s>%s %s ！</s>",
                        sp.getHtmlName(), htmlDescWuxie));
            }
            // showText += (Text.format("\n💬还有谁用无懈,使 %s 无效", this));
        } else {
            for (int i = 0; i < showCardPersonList.size() - 1; i++) {
                showText += (Text.format("\n<s>%s %s ！</s>",
                        showCardPersonList.get(i).getHtmlName(),
                        htmlDescWuxie));
            }
            showText += (Text.format("\n<b>%s %s ！</b>",
                    showCardPersonList.get(showCardPersonList.size() - 1).getHtmlName(),
                    htmlDescWuxie));
            // showText += (Text.format("\n💬可继续用无懈抵消"));
        }
        return showText;

    }

    public int getDistance() {
        return distance;
    }

    @Override
    public void setSource(Person source) {
        super.setSource(source);
        if (source.hasQiCai()) {
            distance = 100;
        }
    }

    /**
     * 附加选人判断，如果不通过，会循环，不会跳出
     * 
     * @param user
     * @param p
     * @return
     */
    public boolean asktargetAddition(Person user, Person p) {
        return true;
    }

    @Override
    public boolean askTarget(Person user) {
        setSource(user);
        if (!needChooseTarget()) {
            setTarget(user);
            return true;
        }

        TimeLimit t = new TimeLimit(Config.PRIV_RND_TIME_60S);

        List<Person> canReachPersons = new ArrayList<>();

        for (Person p : getGameManager().getPlayersBeginFromPlayer(user)) {
            if (getGameManager().calDistance(user, p) > getDistance()) {
                continue;
            }
            if (p == user) {
                continue;
            }
            canReachPersons.add(p);
        }

        while (getGameManager().isRunning() && t.isNotTimeout()) {
            getSource().getPriviMsg().setOneTimeInfo1("💬请选择一个释放目标");
            Person p = selectTarget(user, canReachPersons);
            if (p == null) {
                return false;
            }

            if (this.isBlack() && p.hasWeiMu()) {
                // user.printlnToIO("can't use that because of 帷幕");
                user.getPriviMsg().clearHeader2();
                user.getPriviMsg().setOneTimeInfo1(Text.format("\n💬黑色[锦囊]不能作用于有 帷幕 的角色，请重新选择"));
                continue;
            }
            if (!asktargetAddition(user, p)) {
                continue;
            }
            return true;
        }
        return false;
    }

    public abstract String details();

    @Override
    public String help() {
        return details() + "\n\n锦囊牌代表了可以使用的各种“锦囊妙计”，每张锦囊上会标有[锦囊]字样。" +
                "锦囊分为两类，延时类锦囊和非延时类锦囊" +
                "使用延时类锦囊牌只需将它置入目标角色的判定区即可，不会立即进行使用结算，而是要到目标角色下回合的判定阶段进行。" +
                "判定区里有延时类锦囊牌的角色不能再次被选择为同名的延时类锦囊牌的目标。" +
                "除此之外的锦囊为“非延时类锦囊”。";
    }

}
