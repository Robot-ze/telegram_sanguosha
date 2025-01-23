
package telegramBot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import config.Text;
import msg.MsgAPI;
import msg.MsgObj;
import msg.ReturnType;
import sanguosha.manager.GameManager;
import sanguosha.user.SanUser;
import components.LatchManager;
import components.MyCountDownLatch;
import components.SimpleEnDecrypt;

/**
 * 主要控制加入游戏相关的操作
 */
public class JoinGameContrller implements LatchManager {
    MsgAPI msgAPI;
    private boolean testMode = false; //这个是测试时手工设的
    private Map<Long, Object> userMap = new ConcurrentHashMap<>();
    private Map<Long, Map<Long, User>> usersCntGpId = new ConcurrentHashMap<>();
    private Set<MyCountDownLatch> latchs = ConcurrentHashMap.newKeySet();
    private Map<Long, GameManager> gameManagerMap = new ConcurrentHashMap<>();
    private Set<Long> testGroup = new HashSet<>();
    {
        testGroup.add(-1002308456428L);// sanguo
        testGroup.add(-1001865849787L);// gebi
    }

    public JoinGameContrller(
            MsgAPI msgIo) {
        this.msgAPI = msgIo;
    }

    public JoinGameContrller(
            MsgAPI msgIo, boolean testMode) {
        this.msgAPI = msgIo;
        this.testMode = testMode;
    }



    // 主要是处理deeplink开始游戏的逻辑
    public void start(Update update) {
        MsgObj msg=MsgObj.newMsgObj(this);
        msg.chatId=update.getMessage().getChatId();
        msg.text="<a href=\"t.me/"+msgAPI.getBotName()+"?startgroup=start\">💬请将机器人拉入一个群组中</a>,并赋予删除消息权限,在群组中输入 /sha 启动";
        msgAPI.sendMsg(msg);
        
        // DeleteMessage del = DeleteMessage.builder()
        //         .chatId(update.getMessage().getChatId())
        //         .messageId(update.getMessage().getMessageId())
        //         .build();
        // msgAPI.asyncSend(del);
        // System.out.println("先删掉这消息 dealDeepLink2");
    }

    public void lsGame(Update update) {}

    public void createGameTest(Update update) {
        long chat_id = update.getMessage().getChatId();
        if (!testGroup.contains(chat_id)) {
            return;
        }
        createGame(update);
    }

    public void createGame(Update update) {
        //System.out.println(update);
        if (update.getMessage() == null) {
            return;
        }
        long chat_id = update.getMessage().getChatId();
        if (usersCntGpId.containsKey(chat_id)) {
            return;
        }
        Map<Long, User> userCounts = new ConcurrentSkipListMap<>();
        List<MsgObj> tempMsg = new ArrayList<>();
        usersCntGpId.put(chat_id, userCounts);
        MyCountDownLatch latch = MyCountDownLatch.newInst(10, this);

        String imgName = "logo2";
        MsgObj mainMsg = MsgObj.newMsgObj(this);
        mainMsg.setAttributes(ReturnType.JoinLatch, latch);
        mainMsg.setAttributes(ReturnType.MainMsg, mainMsg);
        // tempMsg.add(mainMsg);
        mainMsg.chatId = chat_id;
        mainMsg.setImg(imgName);
        mainMsg.text = Text.INTRO;
        List<String[]> row = new ArrayList<>();

        String joinString = "join" + SimpleEnDecrypt.encrypt(chat_id + "");

        String[] button = new String[] { "⚔️加入游戏", joinString, "deeplink" };
        row.add(button);
        List<List<String[]>> makup = new ArrayList<>();
        makup.add(row);
        mainMsg.replyMakup = makup;
        msgAPI.sendImg(mainMsg);
        //String callBackCode = "join" + chat_id;
        msgAPI.getCallBackMap().put(joinString, mainMsg);

        // -----------------------------------------------------
        long currentTime = System.currentTimeMillis();
        // CurrentTime,
        // JoinTimeLimit,
        // JoinLatch
        mainMsg.setAttributes(ReturnType.CurrentTime, currentTime);
        LatchManager lt = this;
        msgAPI.submitRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    long timeLimit = 600000; //10分钟
                    mainMsg.setAttributes(ReturnType.JoinTimeLimit, timeLimit);
                    long remainTime;
                    long noticeTime = currentTime + getNextTime(timeLimit);
                    Set<Long> oldSet = new HashSet<>();
                    Set<User> newSet = new HashSet<>();
                    while (latch.getCount() > 0
                            && (remainTime = timeLimit - (System.currentTimeMillis() - currentTime)) > 0) {
                        sleep(1000L);

                        // ----------如果没到播报时间则循环
                        if (System.currentTimeMillis() < noticeTime) {
                            continue;
                        }
                        noticeTime = System.currentTimeMillis() + getNextTime(timeLimit);

                        newSet.clear();
                        for (long userId : userCounts.keySet()) {
                            if (!oldSet.contains(userId)) {
                                // 展示新加入的用户
                                User u = userCounts.get(userId);
                                newSet.add(u);
                                oldSet.add(u.getId());
                            }
                        }
                        // ---------如果快要到加入时间，有用户加入则截止时间增加30秒
                        String res = "<i>天地无声,寒风过而无音,惟见烟尘遥起,隐现甲兵森然\n</i>";
                        if (newSet.size() > 0) {
                            if (remainTime < 30000) {
                                timeLimit += 30000;
                                remainTime += 30000;
                            }
                            res += "\n新加入的玩家:";
                            for (User u : newSet) {
                                res += SanUser.getUserMentionText(u) + " ";
                            }

                        }
                        res += "\n目前已加入的玩家:" + userCounts.size();
                        res += "\n剩余时间:" + remainTime / 1000 + "秒";
                        // 这个msg是和外面不同的
                        MsgObj msgNew = MsgObj.newMsgObj(lt);
                        msgNew.setAttributes(ReturnType.JoinLatch, latch);
                        msgNew.setAttributes(ReturnType.MainMsg, mainMsg);
                        tempMsg.add(msgNew);
                        msgNew.chatId = chat_id;
                        // msg.setImg(imgName);
                        // String info = Text.INTRO;
                        msgNew.text = res;
                        List<String[]> row = new ArrayList<>();
                        String[] button = new String[] { "⚔️加入游戏", joinString, "deeplink" };
                        row.add(button);
                        List<List<String[]>> makup = new ArrayList<>();
                        makup.add(row);
                        msgNew.replyMakup = makup;
                        msgAPI.sendMsg(msgNew);
                        // String callBackCode = "join"+chat_id;
                        // msgAPI.getCallBackMap().put(callBackCode, msgNew);

                    }

                    // 跳出循环后
                    if (userCounts.size() < (testMode ? 2 : 4)) {
                        // if (userCounts.size() < 2) { // test

                        String res = "\n参与人数小于4:没有足够的玩家,游戏中止";
                        // 这个msg是和外面不同的
                        MsgObj msgNew = MsgObj.newMsgObj(lt);
                        msgNew.chatId = chat_id;
                        // msg.setImg(imgName);
                        // String info = Text.INTRO;
                        msgNew.text = res;
                        msgAPI.sendMsg(msgNew);
                        sleep(3000);
                        usersCntGpId.remove(chat_id);
                    }
                    msgAPI.mutiDelMsg(tempMsg);
                    sleep(3000);
                    msgAPI.clearButtons(mainMsg);
                    sleep(3000);
                    while (latch.getCount() > 0) {
                        latch.countDown();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        });

        msgAPI.submitRunnable(new Runnable() {

            @Override
            public void run() {
                try {
                    latch.await(5, TimeUnit.MINUTES);
                    // if (userCounts.size() >= 4) {
                    if (userCounts.size() >= (testMode ? 2 : 4)) { // test
                        buildGame(chat_id, userCounts.values());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void buildGame(final long chatId, Collection<User> users) {
        List<SanUser> sanUsers = new ArrayList<>();
        int num=0;
        for (User u : users) {
            if(num>=10){//最多10人
                break;
            }
            String full_name = SanUser.genFullName(u);
            SanUser su = new SanUser(full_name, u.getId(), u.getUserName());
            sanUsers.add(su);
            num++;
        }

        GameManager g = new GameManager(msgAPI, sanUsers);
        gameManagerMap.put(g.getGameId(), g);
        msgAPI.submitRunnable(
                new Runnable() {

                    @Override
                    public void run() {
                        // while (true){
                        try {
                            g.runGame(chatId);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        // }

                    }

                });
    }

    /**
     * 提醒时间的间隔，当越接近报名截止时间，播报的间隔越短
     * 
     * @return
     */
    public long getNextTime(long remainTime) {
        if (remainTime >= 50000) {
            return 120000;
        } else if (remainTime > 25000) {
            return 20000;
        } else {
            return 5000;
        }
    }

    public boolean checkJoinGame(String call_data, User user) {

        if (call_data == null || user == null ||
                !call_data.startsWith("join")) {
            return false;
        }
        String chatIdString = call_data.substring(4);
        String deChatString = SimpleEnDecrypt.decrypt(chatIdString);

        //System.out.println("deChatString=" + deChatString);
        long chatId = Long.valueOf(deChatString);
        Map<Long, User> thisUserSet = usersCntGpId.get(chatId);
        if (thisUserSet == null) {
            return false;
        }
        MsgObj msgPriv = MsgObj.newMsgObj(this);
        msgPriv.chatId = user.getId();
        if (userMap.containsKey(user.getId()) && thisUserSet.containsKey(user.getId())) {

            msgPriv.text = "❌ 你在其他群组已经加入了游戏";
            delaySendAndDelete(msgPriv);
            return false;
        }
        if (!usersCntGpId.containsKey(chatId)) {
            msgPriv.text = "❌ 游戏已结束";
            delaySendAndDelete(msgPriv);
            return true;
        }

        if (thisUserSet.containsKey(user.getId())) {
            msgPriv.text = "✅ 你已在游戏队列中";
            delaySendAndDelete(msgPriv);
            return true;
        }
        thisUserSet.put(user.getId(), user);
        msgPriv.text = "✅ 你成功加入了游戏";
        delaySendAndDelete(msgPriv);

        MsgObj mainMsgObj = msgAPI.getCallBackMap().get(call_data);

        MyCountDownLatch latch = (MyCountDownLatch) mainMsgObj.getReturnAttr(ReturnType.JoinLatch, 10);
        latch.countDown();
        // -------------更改一下主信息------------------

        String res = "\n\n已加入的玩家:";
        int i = 1;
        for (User u : thisUserSet.values()) {
            res += "\n    ├ " + (i++) + " " + SanUser.getUserMentionText(u);
        }
        mainMsgObj.text = Text.INTRO + res;
        msgAPI.editCaptionForce(mainMsgObj);

        // test 这些得删掉
        if (testMode && latch.getCount() == 8) {
            while (latch.getCount() > 0) {
                latch.countDown();
            }
        }
        return true;
    }

    private void delaySendAndDelete(MsgObj msg) {
        msgAPI.sendMsg(msg);
        msgAPI.submitRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    sleep(5000);
                    msgAPI.delMsg(msg);
                } catch (Exception e) {
                }

            }
        });
    }

    public Map<Long, Object> getUserMap() {
        return this.userMap;
    }

    public void addUser(SanUser user) {
        this.userMap.put(user.user_id, user);
    };

    public void removeUser(long user_id) {
        this.userMap.remove(user_id);
    };

    public void removeUser(String user_id) {
        this.userMap.remove(Long.valueOf(user_id));
    }

    @Override
    public Set<MyCountDownLatch> getLatchs() {
        return latchs;
    }

    @Override
    public boolean isRunning() {
        return true;
    };

    public void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addMsgToThisRound(MsgObj msgObj) {

    }

    @Override
    public void clearPreMsg(int permitRemain) {

    }

    public void endGame(long chatId) {

        Map<Long, User> thisUserSet = usersCntGpId.get(chatId);
        if (thisUserSet != null) {
            for (User u : thisUserSet.values()) {
                userMap.remove(u.getId());
            }
            thisUserSet.clear();
        }

       
        usersCntGpId.remove(chatId);
    }

}