package sanguosha.manager;

import static sanguosha.people.Identity.KING;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
 
import components.CircularList;
import components.LatchManager;
import components.MyCountDownLatch;
import config.Config;
import config.DescUrl;
import config.Rating;
import config.Text;
import db.Database;
import msg.CallbackEven;
import msg.MsgAPI;
import msg.MsgObj;
import msg.ReturnType;
import sanguosha.cards.Card;
import sanguosha.cards.EquipType;
import sanguosha.cards.Equipment;
import sanguosha.cards.basic.HurtType;
import sanguosha.cards.basic.Sha;
import sanguosha.cardsheap.CardsHeap;
import sanguosha.cardsheap.PeoplePool;
import sanguosha.people.AI;
import sanguosha.people.Identity;
import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.user.SanUser;

/**
 * 
 */

public class GameManager implements LatchManager {
    // private final Set<Person> playersInGame = ConcurrentHashMap.newKeySet();
    private final List<Person> players = new CopyOnWriteArrayList<>();
    private final List<SanUser> sanUsers;
    /**
     * 初始玩家数量，这个值不会变
     */
    private int intialNumPlayers = 3;
    /**
     * 玩家数量，这个值会变
     */
    private int numPlayers = 3;
    /**
     * 游戏运行时变动的身份人头表
     */
    private final Map<Identity, List<Person>> idMap = new ConcurrentHashMap<>();
    /**
     * 游戏开始时不动的身份人头表
     */
    private final Map<Identity, List<Person>> idMapStatic = new ConcurrentHashMap<>();
    private final List<Person> winners = new CopyOnWriteArrayList<>();
    private List<Person> dead = new CopyOnWriteArrayList<>();

    private Status gameStatus = Status.preparing;
    private String panic = "";
    private int round = 1;
    private int personRound = 1;

    private IO io;
    private MsgAPI msgApi;
    private PeoplePool peoplePool;
    private CardsHeap cardsHeap;
    private long chatId;
    private boolean testMode = false;
    private long gameId;
    private Set<MyCountDownLatch> latchs = ConcurrentHashMap.newKeySet();

    public Class<? extends Person>[] testRoles;
    @SuppressWarnings("unchecked")
    private Class<? extends Card>[] testCardClass = new Class[0];
    /**
     * 计算一下数量，看每局都少了或多了那些牌
     */
    private Map<Card, Integer> oditor_num_for_test = new ConcurrentHashMap<>();
    private Deque<MsgObj> msgDeque = new ConcurrentLinkedDeque<>();
    private List<Person> changeToBlankPersons = new ArrayList<>();

    /**
     * 增加一个成白板的人，这里从流程看是线程安全的
     * 
     * @return
     */
    public void addblankPerson(Person p) {
        changeToBlankPersons.add(p);
    }

    /**
     * 获下一循环将被变成白板的人，这里从流程看是线程安全的
     * 
     * @return
     */
    public List<Person> getChangeToBlankPersons() {
        return changeToBlankPersons;
    }

    /**
     * 初始化游戏管理器
     * @param msgIo 消息API
     * @param sanUsers 用户列表
     */ 
    public GameManager(MsgAPI msgIo, List<SanUser> sanUsers) {
        this.msgApi = msgIo;
        this.sanUsers = sanUsers;
        Collections.shuffle(sanUsers);
        String gameIdString = Database.get("gameId");
        if (gameIdString == null) {
            gameId = 0;
        } else {
            gameId = Long.valueOf(gameIdString) + 1;
        }
        Database.put("gameId", gameId + "");
    }

    /**
     * 设置测试模式
     * @param testMode 是否为测试模式
     */
    public void setTestMode(boolean testMode) {
        this.testMode = testMode;
    }

    /**
     * 设置测试模式
     * @param testMode 是否为测试模式
     * @param testRoles 测试角色
     * @param testCardClass 测试卡牌
     */
    public void setTestMode(boolean testMode, Class<? extends Person>[] testRoles,
            Class<? extends Card>[] testCardClass) {
        this.testMode = testMode;
        this.testRoles = testRoles;
        this.testCardClass = testCardClass;
    }

    /**
     * 玩家选角色数
     * 
     * @param numPlayers
     * @return
     */
    private int getOpinionNum(int numPlayers) {
        return 3;
        // return numPlayers >= 9 ? 3 : 4;
    }

    /**
     * 开始游戏
     * @param chatId 聊天ID
     */ 
    public void startGame(long chatId) {
        gameStatus = Status.running;

        this.chatId = chatId;
        io = new IO();
        io.init(this);

        setNumPlayers(Math.max(sanUsers.size(), 4));
        int opinionNum = getOpinionNum(numPlayers);
        peoplePool = new PeoplePool(opinionNum);
        peoplePool.init(this);
        cardsHeap = new CardsHeap();
        cardsHeap.init(this);
        idMap.put(Identity.KING, new ArrayList<>());
        idMap.put(Identity.MINISTER, new ArrayList<>());
        idMap.put(Identity.TRAITOR, new ArrayList<>());
        idMap.put(Identity.REBEL, new ArrayList<>());
        idMapStatic.put(Identity.KING, new ArrayList<>());
        idMapStatic.put(Identity.MINISTER, new ArrayList<>());
        idMapStatic.put(Identity.TRAITOR, new ArrayList<>());
        idMapStatic.put(Identity.REBEL, new ArrayList<>());
        // 混排玩家顺序
        Collections.shuffle(sanUsers);
        // io.printlnToIO("player 1, your identity is KING\nchoose your person");
        // ------------选一主公------------------------------------------------------------------------------------
        GameManager gameManagerThis = this;
        MsgObj noticeKingMsg = MsgObj.newMsgObj(this);
        sendKingChooseNotice(noticeKingMsg, sanUsers.get(0));
        List<Person> kingOpinions = peoplePool.allocPeopleForKing(opinionNum);
        Person selected = initialChoosePerson(Identity.KING, kingOpinions, sanUsers.get(0),
                sanUsers,
                this);
        // 把没选中的人加回池子
        for (Person p : kingOpinions) {
            if (p != selected) {
                peoplePool.rePut(p);
            }
        }
        selected.setUser(sanUsers.get(0));
        selected.setGameManager(gameManagerThis);
        Identity king = peoplePool.allocIdentityForKing();
        selected.initialize(king, 1);
        selected.setIdentityAndSkill(king);
        // selected.setPos(0);
        players.add(selected);
        idMap.get(Identity.KING).add(selected);
        idMapStatic.get(Identity.KING).add(selected);
        if (numPlayers > 4) {
            selected.initMaxHP(selected.getMaxHP() + 1);
        }
        msgApi.delMsg(noticeKingMsg);
        // sleep(3000);
        // -----------------选其他人
        // 打乱一下顺序
        peoplePool.shufflePeople();

        MsgObj noticeOtherMsg = MsgObj.newMsgObj(this);
        sendOtherPeopleNotice(selected, noticeOtherMsg, sanUsers);
        MyCountDownLatch latch = MyCountDownLatch.newInst(sanUsers.size() - 1, this);

        for (int i = 2; i <= numPlayers; i++) {
            players.add(null); // 先加上占位
            if (i <= sanUsers.size()) {

                Identity identity = peoplePool.allocIdentity();
                int ii = i;
                msgApi.submitRunnable(new Runnable() {
                    @Override
                    public void run() {
                        // io.printlnToIO("player " + i + ", your identity is " + identity.toString() +
                        // "\nchoose your person");
                        List<Person> opinions = peoplePool.allocPeople();
                        Person selected = initialChoosePerson(identity, opinions, sanUsers.get(ii - 1),
                                sanUsers,
                                gameManagerThis);
                        // 把没选中的人加回池子
                        for (Person p : opinions) {
                            if (p != selected) {
                                peoplePool.rePut(p);
                            }
                        }
                        selected.setUser(sanUsers.get(ii - 1));
                        selected.setGameManager(gameManagerThis);
                        selected.initialize(identity, ii);
                        selected.setIdentityAndSkill(identity);

                        // selected.setPos(ii - 1);
                        players.set(ii - 1, selected);
                        idMap.get(identity).add(selected);
                        idMapStatic.get(identity).add(selected);
                        latch.countDown();

                    }
                });

            } else {
                Identity identity = peoplePool.allocIdentity();
                // io.printlnToIO("player " + i + ", your identity is " + identity.toString() +
                // "\nchoose your person");

                ArrayList<Person> alloc = peoplePool.allocPeople();

                System.out.println("peoplePool.allocPeople()=" + alloc);
                selected = alloc.get(0);
                // selected.setUser(sanUsers.get(i - 1));
                selected.setGameManager(gameManagerThis);
                selected.initialize(identity, i);
                selected.setIdentityAndSkill(identity);

                // selected.setPos(i - 1);
                players.set(i - 1, selected);
                idMap.get(identity).add(selected);
                idMapStatic.get(identity).add(selected);
                // TODO 这里有问题
                // AI ai = new AI(this);
                // switchAi(ai, selected);
            }

        }

        latch.await(5, TimeUnit.MINUTES);

        for (int i = sanUsers.size(); i < players.size(); i++) {
            // 切换AI不能在多线程里并发，因为players的位置可能会变
            AI ai = new AI(gameManagerThis);
            switchAi(ai, players.get(i));
        }

        for (Person p : players) {
            // playersInGame.add(p);

            p.drawCards(4, false);

        }
        msgApi.delMsg(noticeOtherMsg);
        // sleep(3000);
        // peoplePool.getPeople().clear();
    }

    /**
     * 发送主公选择通知
     * @param noticeKingMsg 通知消息
     * @param user 用户
     */
    private void sendKingChooseNotice(MsgObj noticeKingMsg, SanUser user) {

        String res = Text.format("%s 你是主公,请在90秒内前往机器人 @shanguoshabot 选择一名角色",
                user.getUserMentionText());
        noticeKingMsg.text = res;
        noticeKingMsg.chatId = chatId;
        noticeKingMsg.setImg("allStatus");
        msgApi.noticeAndAskPublicNoCallBack(noticeKingMsg, "请选择主公角色", "chooseRole");

    }

    /**
     * 发送其他角色选择通知
     * @param king 主公
     * @param noticeOtherMsg 通知消息
     * @param users 用户列表
     */
    private void sendOtherPeopleNotice(Person king, MsgObj noticeOtherMsg, List<SanUser> users) {

        String peopleString = "";
        for (int i = 1; i < users.size(); i++) {// 排除第一个
            peopleString += Text.circleNum(i + 1) + users.get(i).getUserMentionText() + "\n";
        }
        String res = Text.format("%s主公的角色是: %s\n%s\n排位如上所示,请各位英雄90秒内前往机器人 @shanguoshabot 选择一名角色",
                Text.circleNum(1),
                king.getHtmlName(),
                peopleString);
        noticeOtherMsg.text = res;
        noticeOtherMsg.chatId = chatId;
        noticeOtherMsg.setImg("allStatus");
        msgApi.noticeAndAskPublicNoCallBack(noticeOtherMsg, "请选择角色", "chooseRole");

    }

    /**
     * 开始测试游戏
     * @param chatId 聊天ID
     */
    public void startGameForTest(long chatId) {
        gameStatus = Status.running;
        testMode = true;
        this.chatId = chatId;
        io = new IO();
        io.init(this);

        setNumPlayers(Math.max(sanUsers.size(), 5));
        int opinionNum = getOpinionNum(numPlayers);
        peoplePool = new PeoplePool(opinionNum);
        peoplePool.init(this);
        // 把测试的角色都删了
        // for(Person p:peoplePool.getPeople()){
        // System.out.println("-------------------------------");
        // System.out.println(p.name());
        // System.out.println(p.skillsDescription());
        // }
        Set<String> temp_name_set=new HashSet<>();
        for (Class<? extends Person> p : testRoles) {
            temp_name_set.add(p.getName());
        }

        for(Person p :new ArrayList<>(peoplePool.getPeople())){
            if (temp_name_set.contains(p.getClass().getName())){
                System.out.println("删掉:"+p.toString()+" "+p);
                peoplePool.getPeople().remove(p);
            }
        }
        
        cardsHeap = new CardsHeap();
        cardsHeap.init(this);
        // Map<String,String> temp=new HashMap<>();
        // for(Card c:cardsHeap.getUnTakenCard()){
        // temp.put(c.toString(),c.help());

        // }
        // for(String c:temp.keySet()){
        // System.out.println("--------------------------------------------");
        // System.out.println(c);
        // System.out.println(temp.get(c));
        // }

        for (Card c : cardsHeap.getUnTakenCard()) {

            oditor_num_for_test.put(c, 0);
        }


        idMap.put(Identity.KING, new ArrayList<>());
        idMap.put(Identity.MINISTER, new ArrayList<>());
        idMap.put(Identity.TRAITOR, new ArrayList<>());
        idMap.put(Identity.REBEL, new ArrayList<>());
        idMapStatic.put(Identity.KING, new ArrayList<>());
        idMapStatic.put(Identity.MINISTER, new ArrayList<>());
        idMapStatic.put(Identity.TRAITOR, new ArrayList<>());
        idMapStatic.put(Identity.REBEL, new ArrayList<>());

        // io.printlnToIO("player 1, your identity is KING\nchoose your person");
        try {
            GameManager gameManagerThis = this;
            SanUser testUser = new SanUser("测试人", 6785745413L, "@some_one_red");
            msgApi.addUser(testUser);
            Person selected = testRoles[0].getConstructor().newInstance();
            selected.setUser(testUser);
            selected.setGameManager(gameManagerThis);
            Identity king = peoplePool.allocIdentityForKing();
            selected.initialize(king, 1);
            selected.setIdentityAndSkill(king);

            // System.out.println("selected.getIdentity()=" + selected.getIdentity());
            // selected.setPos(0);
            players.add(selected);
            idMap.get(Identity.KING).add(selected);
            idMapStatic.get(Identity.KING).add(selected);
            if (numPlayers > 4) {
                selected.initMaxHP(selected.getMaxHP() + 1);
            }

            MyCountDownLatch latch = MyCountDownLatch.newInst(numPlayers - 1, this);
            for (int i = 2; i <= numPlayers; i++) {
                int ii = i;
                msgApi.submitRunnable(new Runnable() {
                    @Override
                    public void run() {// 这个是模拟多线程环境下
                        try {

                            Identity identity = peoplePool.allocIdentity();
                            // io.printlnToIO("player " + i + ", your identity is " + identity.toString() +
                            // "\nchoose your person");

                            // selected = testRoles[i - 1].getConstructor().newInstance();
                            ArrayList<Person> alloc = peoplePool.allocPeople();
                            Person selected;
                            if (ii < testRoles.length) {
                                selected = testRoles[ii - 1].getConstructor().newInstance();
                            } else {
                                selected = alloc.get(0);
                            }

                            selected.setGameManager(gameManagerThis);
                            selected.initialize(identity, ii);
                            selected.setIdentityAndSkill(identity);

                            System.out.println(selected + " 身份 " + Person.getIdentityString(identity));
                            // selected.setPos(i - 1);
                            players.add(selected);
                            idMap.get(identity).add(selected);
                            idMapStatic.get(identity).add(selected);

                            latch.countDown();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                sleep(100);
            }

            latch.await(5, TimeUnit.MINUTES);
            for (int i = 1; i < players.size(); i++) {
                // 切换AI不能在多线程里并发，因为players的位置可能会变
                AI ai = new AI(gameManagerThis);
                switchAi(ai, players.get(i));
            }

            // 测试模式会出现重复的人，没有大碍
            peoplePool.shufflePeople();// 打乱一下顺序，

            cardsHeap.testDrawCard(testCardClass);

            for (Person p : players) {
                // playersInGame.add(p);
                p.drawCards(4, false);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // peoplePool.getPeople().clear();
    }

    /**
     * 运行游戏
     * @param chatId 聊天ID
     */ 
    public void runGame(long chatId) {
        try {

            if (testMode) {
                startGameForTest(chatId);
            } else {
                startGame(chatId);
            }
            extendsInit();
            showGroupNotice();
            showAllPerson();
            sleep(3000);
            while (!gameIsEnd()) {
                round++;
                for (int i = 0; i < players.size(); i++) {
                    try {
                        // System.out.println("players=" + players);
                        players.get(i).run(true);
                        if (gameIsEnd()) {
                            endGame();
                            return;
                        }
                        Utils.checkCardsNum(this);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
            endGame();

        } catch (Exception e) {
            panic("\nno line found");
            e.printStackTrace();
        }
    }

    /**
     * 一些附加的初始化
     */
    private void extendsInit() {
        for (int i = 0; i < getPlayers().size(); i++) {
            getPlayers().get(i).setGamePlayerNo(i + 1);
        }

    }

    /**
     * 显示群组通知
     */
    private void showGroupNotice() {
        StringBuilder res = new StringBuilder();
        int idx = 1;
        for (Person p : getPlayers()) {
            res.append("\n");
            res.append(Text.circleNum(idx));
            if (p.getIdentity() == KING) {
                res.append(DescUrl.getDescHtml("👑主公"));
            }
            if (p.getUser() == null) {
                res.append("AI 是:" + DescUrl.getDescHtml(p.toString()));
            } else {
                res.append(p.getUser().getUserMentionText());
                res.append(" 是:");
                res.append(DescUrl.getDescHtml(p.toString()));

            }
            idx++;
        }
        res.append("\n\n10秒后游戏开始⚔️");
        getIo().printlnPublic(res.toString(), "fight_start");
        if (!testMode) {
            sleep(Config.PUBLIC_ACTION_TIME_15S);
        }
    }

    /**
     * 增加一个角色的人
     * 
     * @param identity
     * @param person
     */
    public void addIndentity(Identity identity, Person person) {
        List<Person> indentitis = idMap.get(identity);
        indentitis.add(person);
        indentitis = idMapStatic.get(identity);
        indentitis.add(person);
    }

    /**
     * 删除一个角色的人
     * 
     * @param identity
     * @param person
     */
    public void removeIndentity(Identity identity, Person person) {
        List<Person> indentitis = idMap.get(identity);
        indentitis.remove(person);
        indentitis = idMapStatic.get(identity);
        indentitis.remove(person);
    }

    private void showAllPerson() {

        getMsgAPI().sendAllPersonStaus(getPlayers(), new CallbackEven() {
            @Override
            public String getPersonStatusExec(Person person) {
                try {
                    String result = person.getAllPersonInfo();
                    System.out.println("result.length()=" + result.length());
                    return result;
                } catch (Exception e) {
                    e.printStackTrace();
                    return "出错";
                }

            }
        });

    }

    // private void putNameToList(Map<String, List<Person>> dbuffMap, String duff,
    // Person p) {
    // List<Person> personList = dbuffMap.get(duff);
    // if (personList == null) {
    // personList = new ArrayList<>(5);
    // dbuffMap.put(duff, personList);
    // }
    // personList.add(p);

    // }

    /**
     * 
     * 因为有一些群杀技能，会导致在这一回合内只有忠臣存活的奇怪情况
     * 程序的判定符合以下真值表：
     * 
     * <table>
     * <tr>
     * <th>主公死</th>
     * <th>忠臣死</th>
     * <th>反贼死</th>
     * <th>内奸死</th>
     * <th>主公赢</th>
     * <th>反贼赢</th>
     * <th>内奸赢</th>
     * </tr>
     * 
     * </table>
     * 
     * 主公死 忠臣死 反贼死 内奸死 主公赢 反贼赢 内奸赢
     * 1 1 1 1 0 1 0
     * 1 1 1 0 0 0 1
     * 1 1 0 1 0 1 0
     * 1 1 0 0 0 1 0
     * 1 0 1 1 0 1 0
     * 1 0 1 0 0 1 0
     * 1 0 0 1 0 1 0
     * 1 0 0 0 0 1 0
     * 0 1 1 1 1 0 0
     * 0 1 1 0 0 0 0
     * 0 1 0 1 0 0 0
     * 0 1 0 0 0 0 0
     * 0 0 1 1 1 0 0
     * 0 0 1 0 0 0 0
     * 0 0 0 1 0 0 0
     * 0 0 0 0 0 0 0
     * 
     * @return
     */
    public boolean gameIsEnd() {
        if (status() == Status.end || status() == Status.error) {
            return true;
        }
        List<Person> loser = new ArrayList<>();
        Utils.assertTrue(winners.isEmpty(), "winners are not empty");
        if (isNoRoles(Identity.KING) && // 就算反贼全部死完，如果还有忠臣，奸臣把主公杀了也算反贼赢
                (!isNoRoles(Identity.MINISTER) ||
                        !isNoRoles(Identity.REBEL) ||
                        idMap.get(Identity.TRAITOR).size() > 1)) {
            String out = ("<b>🎉反贼胜利</b>\n\n") +
                    "\"群雄并起，苍天易色\"\n" + //
                    "<i>山呼海啸，反旗漫天，铁骑直指宫阙。江山易姓，天下分封。乱世之中，一时豪杰笑谈风云，尽显英雄本色</i>";

            winners.addAll(idMapStatic.get(Identity.REBEL));
            // ----------------------------------------------
            loser.addAll(idMapStatic.get(Identity.KING));
            loser.addAll(idMapStatic.get(Identity.MINISTER));
            loser.addAll(idMapStatic.get(Identity.TRAITOR));
            sendFinishMsg("反贼", out, winners, loser);
            return true;
        } else if (isNoRoles(Identity.KING)) {
            String out = ("<b>🎉内奸胜利</b>\n\n") +
                    "\"笑里藏刀，奸人当道\"\n" + //
                    "<i>暗流涌动，弄人心于股掌之间，登九五之位。世人只知其威，不识其毒。天下虽安，然心寒如冰，世道再不太平</i>";
            winners.addAll(idMapStatic.get(Identity.TRAITOR));
            // ----------------------------------------------
            loser.addAll(idMapStatic.get(Identity.KING));
            loser.addAll(idMapStatic.get(Identity.MINISTER));
            loser.addAll(idMapStatic.get(Identity.REBEL));

            sendFinishMsg("内奸", out, winners, loser);
            // io.printlnPublic(out);
            return true;
        } else if (isNoRoles(Identity.TRAITOR) && isNoRoles(Identity.REBEL)) {
            String out = ("<b>🎉主公和忠臣胜利</b>\n\n") +
                    "\"虎牢关前风云起，铜雀台下乾坤定\"\n" + //
                    "<i>主公端坐龙椅，忠臣环立众心归一。外敌尽伏内乱已平，天下归心盛世已现。三军欢呼万民同庆，千古之业遂成于今</i>";
            winners.addAll(idMapStatic.get(Identity.KING));
            winners.addAll(idMapStatic.get(Identity.MINISTER));

            // ----------------------------------------------
            loser.addAll(idMapStatic.get(Identity.REBEL));
            loser.addAll(idMapStatic.get(Identity.TRAITOR));
            sendFinishMsg("主忠", out, winners, loser);
            // io.printlnPublic(out);

            return true;
        } else {
            return false;
        }

    }

    /**
     * 发送游戏结束消息
     * @param img 图片
     * @param msg 消息
     * @param wins 胜利者
     * @param losers 失败者
     */
    private void sendFinishMsg(String img, String msg, List<Person> wins, List<Person> losers) {
        StringBuilder sb = new StringBuilder();
        sb.append(msg);
        sb.append("\n\n<b>🏆胜利</b>");

        for (Person p : wins) {
            sb.append("\n    ├");
            if (dead.contains(p)) {
                sb.append("🪦");
            }
            sb.append(p.getIdentityHtml() + " ");
            sb.append(p.getHtmlName());
        }

        sb.append("\n<b>💀失败</b>");
        for (Person p : losers) {
            sb.append("\n    ├");
            if (dead.contains(p)) {
                sb.append("🪦");
            }
            sb.append(p.getIdentityHtml() + " ");
            sb.append(p.getHtmlName());
        }
        MsgObj publicMsg = MsgObj.newMsgObj(this);
        publicMsg.text = sb.toString();
        publicMsg.chatId = getChatId();
        publicMsg.setImg(img);
        getMsgAPI().sendImg(publicMsg);
    }

    /**
     * 游戏是否还在运行
     * 
     * @return
     */
    public boolean isRunning() {
        return status() == Status.running || status() == Status.preparing;
    }

    /**
     * 获取游戏ID
     * @return 游戏ID
     */
    public long getGameId() {
        return gameId;
    }

    /**
     * 设置游戏ID
     * @param gameId 游戏ID
     */
    public void setGameId(long gameId) {
        this.gameId = gameId;
    }

    /**
     * 获取聊天ID
     * @return 聊天ID
     */
    public long getChatId() {
        return chatId;
    }

    /**
     * 获取消息API
     * @return 消息API
     */
    public MsgAPI getMsgAPI() {
        return this.msgApi;
    }

    /**
     * 获取IO
     * @return IO
     */
    public IO getIo() {
        return this.io;
    }

    /**
     * 获取人物池
     * @return 人物池
     */
    public PeoplePool getPeoplePool() {
        return peoplePool;
    }

    /**
     * 获取牌堆
     * @return 牌堆
     */
    public CardsHeap getCardsHeap() {
        return cardsHeap;
    }

    /**
     * 调用事件
     */
    public void callItEven() {
        io.printlnPublic("call it even");
        endGame();
    }

    /**
     * 恐慌
     * @param s 恐慌信息
     */
    public void panic(String s) {
        panic += "panic at " + Thread.currentThread().getStackTrace()[1].getFileName();
        panic += " line" + Thread.currentThread().getStackTrace()[1].getLineNumber() + ": " + s;
        io.printlnPublic(panic);
        gameStatus = Status.error;

    }

    /**
     * 获取游戏状态
     * @return 游戏状态
     */
    public Status status() {
        return gameStatus;
    }

    /**
     * 链接伤害
     * 
     * @param msgObj 传入这个msgObj可以追加写出链接伤害
     * @param cards
     * @param source
     * @param target
     * @param num
     * @param type
     */
    public void linkHurt(MsgObj msgObj, List<Card> cards, Person source, Person target, int num, HurtType type) {
        Utils.assertTrue(type == HurtType.fire ||
                type == HurtType.thunder, "link hurt wrong type");
        int realNum = num;
        for (Person p : players) {
            if (p.isLinked()) {
                realNum = p.hurt(cards, source, realNum, type);
                if (msgObj != null) {
                    msgObj.text = msgObj.text + Text.format("\n%s 受%s点%s链接伤害:%s",
                            p.getHtmlName(),
                            realNum + "",
                            Sha.hurtTypeString(type),
                            p.getHPEmoji());
                }
            }
        }
    }

    /**
     * 拼点
     * @param source 源
     * @param target 目标
     * @return 是否拼点成功
     */ 
    public boolean pinDian(Person source, Person target) {
        // Utils.assertTrue(source.getCards().size() >= 1, "pindian source has no
        // cards");
        // Utils.assertTrue(target.getCards().size() >= 1, "pindian target has no
        // cards");

        if (source.getCards().size() <= 0 || target.getCards().size() <= 0) {
            return false;
        }
        // io.printlnPublic(source + " launches 拼点 towards " + target);
        Card c1 = null;
        Card c2 = null;

        source.getPriviMsg().appendOneTimeInfo1("\n💬你发起了与 " + target + "的拼点，请出一张手牌");
        c1 = source.requestCard(null);
        if (c1 == null) {
            return false;
        }
        MsgObj puMsgObj = MsgObj.newMsgObj(this);
        puMsgObj.setImg(source.toString());
        puMsgObj.chatId = getChatId();

        // System.out.println("target="+target);
        if (target.isAI()) {
            String result = Text.format("%s,%s 与你拼点,请出一张牌",
                    target.getHtmlName(),
                    source.getPlateName());
            puMsgObj.text = result;
            getMsgAPI().sendImg(puMsgObj);
            // sleep(1000);
            c2 = target.getCards().get(Utils.randint(0, target.getCards().size() - 1));
            target.loseCard(c2);

        } else {
            boolean activeShan = getMsgAPI().noticeAndAskPublic(
                    puMsgObj,
                    target,
                    Text.format("%s,%s 与你拼点,请出一张牌",
                            target.getHtmlName(),
                            source.getPlateName()),
                    "拼点", "pindian", true);
            puMsgObj.replyMakup = null;
            getMsgAPI().clearButtons(puMsgObj);
            if (activeShan) {
                target.getPriviMsg().clearHeader2();
                target.getPriviMsg().setOneTimeInfo1("\n💬" + target + " 发起了与你的拼点，请出一张手牌");
                c2 = target.requestCard(null);
            } else {
                c2 = target.getCards().get(Utils.randint(0, target.getCards().size() - 1));
                target.loseCard(c2);
            }
        }

        // //sleep(3000);

        int num1 = c1.number();
        int num2 = c2.number();
        String yy1 = source.usesYingYang();
        String yy2 = target.usesYingYang();
        if (yy1.equals("+3")) {
            num1 = Math.max(num1 + 3, 13);
        } else if (yy1.equals("-3")) {
            num1 = Math.min(num1 - 3, 1);
        }
        if (yy2.equals("+3")) {
            num2 = Math.max(num2 + 3, 13);
        } else if (yy2.equals("-3")) {
            num2 = Math.min(num2 - 3, 1);
        }
        String result = Text.format(",%s:%s,%s:%s,%s 胜利",
                source.getPlateName(),
                c1.getHtmlNameWithColor(),
                target.getPlateName(),
                c2.getHtmlNameWithColor(),
                (num1 > num2 ? source.getHtmlName() : target.getHtmlName()));
        puMsgObj.text = puMsgObj.text + result;
        puMsgObj.replyMakup = null;
        // sleep(1000);
        getMsgAPI().editCaptionForce(puMsgObj);
        source.putTempActionMsgObj("pindian", puMsgObj);
        target.putTempActionMsgObj("pindian", puMsgObj);
        // io.printlnPublic((num1 > num2 ? source : target) + " wins");
        // System.out.println("target="+target);
        if (num1 < num2 && target.usesZhiBa()) {// 制霸技能，target是孙策 孙策硬拼点的拿走卡
            getCardsHeap().retrieve(c1);
            getCardsHeap().retrieve(c2);
            target.addCard(c1);
            target.addCard(c2);
        }
        return num1 > num2;
    }

    /**
     * 人物死亡
     * @param p 死亡人物
     */ 
    public void die(Person p) {

        List<Card> deadShowCards = new ArrayList<>();
        p.setDeadShowCard(deadShowCards);

        if (p.getExtraCards() != null) {
            deadShowCards.addAll(p.getExtraCards());
            cardsHeap.discard(p.getExtraCards());
        }
        deadShowCards.addAll(p.getCards());
        deadShowCards.addAll(p.getRealJudgeCards());
        deadShowCards.addAll(p.getEquipments().values());

        Person cp = null;
        for (Person p2 : players) {
            if (!p2.isDead() && p2.usesXingShang(p)) {
                cp = p2;
                break;
            }
        }
        if (cp == null) {
            System.out.println("p.getCards()=" + p.getCards());

            p.loseCard(p.getCards());
            p.loseCard(new ArrayList<>(p.getRealJudgeCards()));
            p.loseCard(new ArrayList<>(p.getEquipments().values()));
            p.loseCard(p.getExtraCards());// 比如那些田啊，不屈牌啊
            // System.out.println("p.getExtraCards()=" + p.getExtraCards());
        } else {
            p.loseCard(p.getCards(), false);
            p.loseCard(new ArrayList<>(p.getRealJudgeCards()), false);
            p.loseCard(new ArrayList<>(p.getEquipments().values()), false);
            cp.addCard(deadShowCards);
            p.loseCard(p.getExtraCards());// 比如那些田啊，不屈牌啊
        }
        // System.out.println("要删除 p=" + p + " " + p.hashCode());
        // for (Person pp : players) {
        // System.out.println("列表中 pp=" + pp + " " + pp.hashCode());

        // }
        players.remove(p);
        // 重置一下这个元素之后元素位置标记
        // for (int i = p.getPos(); i < players.size(); i++) {
        // players.get(i).setPos(i);
        // }
        // for (Person pp : players) {
        // System.out.println("删除后列表中 pp=" + pp + " " + pp.hashCode());

        // }
        numPlayers = players.size();
        idMap.get(p.getIdentity()).remove(p);
        if (p.getAi() != null) {
            dead.add(p);
        }

        // io.println(p + " 阵亡,身份: " + p.getIdentity());
    }

    /**
     * 死亡惩罚
     * @param dead_p 死亡人物
     * @param source 源
     */ 
    public void deathRewardPunish(Person dead_p, Person source) {
        String result = Text.format(",身份: %s", dead_p.getIdentityHtml());
        List<Card> deadShowCards = dead_p.getDeadShowCards();
        String deadShowCardString = "";
        for (Card c : deadShowCards) {
            deadShowCardString += ("[" + c.getHtmlNameWithColor() + "]");
        }
        result += Text.format("\n丢牌: %s", deadShowCardString);

        if (dead_p.getIdentity() == Identity.REBEL) {
            if (source != null) {
                result += Text.format(",%s 讨逆有功,重赏3张牌", source.getHtmlName());
                source.drawCards(3);
            } else {
                result += Text.format(",<i>天命难测，横祸难逃</i>");
            }

        } else if (dead_p.getIdentity() == Identity.MINISTER) {
            if (source != null && source.getIdentity() == Identity.KING) {
                result += Text.format(",%s 错杀忠良,痛哭涕零,丢所有牌", source.getHtmlName());
                source.loseCard(source.getCards());
                source.loseCard(new ArrayList<>(source.getEquipments().values()));
            } else {
                result += Text.format(",<i>天道无常，臣休矣</i>", source.getHtmlName());
            }
        }

        MsgObj taoMsg = dead_p.getTempActionMsgObj("tao");
        taoMsg.appendText(result);
        sleep(3000);
        getMsgAPI().editCaptionForce(taoMsg);
        // sleep(3000);
    }

    /**
     * 哪种角色死完了
     * 
     * @param id
     * @return
     */
    public boolean isNoRoles(Identity id) {
        return idMap.get(id).isEmpty();
    }

    /**
     * 计算距离
     * @param p1 人物1
     * @param p2 人物2
     * @return 距离
     */ 
    public int calDistance(Person p1, Person p2) {
        if (p1 == null || p2 == null) {
            return 99999;
        }
        if (p1 == p2) {
            return 0;
        }
        int pos1 = players.indexOf(p1);
        int pos2 = players.indexOf(p2);
        int dis = Math.max(pos1 - pos2, pos2 - pos1);
        dis = Math.min(dis, numPlayers - dis);
        if (p2.hasEquipment(EquipType.plusOneHorse, null)) {
            dis++;
        }
        if (p2.hasFeiYing()) {
            dis++;
        }
        if (p1.hasEquipment(EquipType.minusOneHorse, null)) {
            dis = Math.max(dis - 1, 1);
        }
        if (p1.hasMaShu()) {
            dis = Math.max(dis - 1, 1);
        }
        dis = Math.max(dis - p1.numOfTian(), 1);
        return dis;
    }

    /**
     * 获取可达人物
     * @param p1 人物1
     * @param distance 距离
     * @return 可达人物列表
     */ 
    public ArrayList<Person> reachablePeople(Person p1, int distance) {
        ArrayList<Person> ans = new ArrayList<>();
        for (Person p : players) {
            if (calDistance(p1, p) <= distance) {
                ans.add(p);
            }
        }
        ans.remove(p1);
        return ans;
    }

    /**
     * 获取国家人物
     * @param n 国家
     * @return 国家人物列表
     */ 
    public ArrayList<Person> peoplefromNation(Nation n) {
        ArrayList<Person> ans = new ArrayList<>();
        for (Person p : players) {
            if (p.getNation() == n) {
                ans.add(p);
            }
        }
        return ans;
    }

    /**
     * 获取主公
     * @return 主公
     */ 
    public Person getKing() {
        List<Person> kingList = idMapStatic.get(Identity.KING);
        if (kingList.size() <= 0) {
            return null;
        }
        return kingList.get(0);
    }

    /**
     * 获取玩家数量
     * @return 玩家数量
     */ 
    public int getNumPlayers() {
        return numPlayers;
    }

    /**
     * 设置玩家数量
     * @param numPlayers 玩家数量
     */  
    public void setNumPlayers(int numPlayers) {
        this.numPlayers = numPlayers;
        this.intialNumPlayers = numPlayers;
    }

    /**
     * 获取玩家列表
     * @return 玩家列表
     */ 
    public List<Person> getPlayers() {
        return players;
    }

    /**
     * 返回一个从当前人物开始的数组，用来判定群体技能的顺序
     * 
     * @param person
     * @return
     */
    public List<Person> getPlayersBeginFromPlayer(Person person) {
        int index = getPlayers().indexOf(person);
        if (index < 0) {
            return players;
        }
        // List<Person> newList = new ArrayList<>( new CircularList<>(players, index));
        List<Person> newList = new CircularList<>(players, index);
        return newList;
    }

    public void test_count_card(Card c) {
        test_count_card(null, c);
    }

    /**
     * 测试计数卡牌
     * @param owner 拥有者
     * @param c 卡牌
     */ 
    public void test_count_card(Object owner, Card c) {
        if (!testMode) {
            return;
        }
        if (!oditor_num_for_test.containsKey(c)) {
            // System.out.println("\n" + c.info() + c.toString() + ": 不存在");
            return;
        }

        oditor_num_for_test.put(c, oditor_num_for_test.get(c) + 1);
        if (owner != null) {
            // System.out.println(owner + " " + c.info() + c.toString() + " " +
            // oditor_num_for_test.get(c));
        }
    }

    /**
     * 清除测试计数卡牌
     */  
    public void test_clear_oditor() {
        if (!testMode) {
            return;
        }
        for (Card c : oditor_num_for_test.keySet()) {
            oditor_num_for_test.put(c, 0);
        }
    }

    /**
     * 显示非法牌
     */     
    public void test_show_card_illegle() {
        if (!testMode) {
            return;
        }
        String s = "---------非法牌-----------------";
        for (Card c : oditor_num_for_test.keySet()) {
            int num = oditor_num_for_test.get(c);
            if (num != 1) {
                s += "\n数量:" + num + " " + c.info() + c.toString() + ":" + oditor_num_for_test.get(c)
                        + " 属于：" + c.getOwner() + " 源：" + c.getSource() + " 对方:" + c.getTarget() + "isNotTaken:"
                        + c.isNotTaken();
            }
        }

        System.out.println(s);

    }

    /**
     * 获取回合数
     * @return 回合数
     */  
    public int getRound() {
        return round;
    }

    /**
     * 获取测试模式
     * @return 测试模式
     */ 
    public boolean getTestMode() {
        return testMode;
    }

    /**
     * 获取所有玩家的 @名
     * @return 所有玩家的 @名
     */
    public String getAllPlayerHtml() {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < getPlayers().size(); i++) {
            Person p = getPlayers().get(i);
            b.append(p.getHtmlName());
            if (i < getPlayers().size() - 1) {
                b.append(",");
            }
        }
        return b.toString();
    }

    /**
     * 人物和AI的对调
     * 
     * @param p
     * @param p2
     */
    public void switchAi(AI p, Person p2) {
        if (p.isAI()) {
            ((AI) p).setOutName(p2.toString());
            ((AI) p).setBasicPlayer(p2);
            p2.setAi(p);
        }
        p.setNation(p2.getNation());
        p.setSex(p2.getSex());
        p.initMaxHP(p2.getMaxHP());
        p.setCurrentHP(p2.getHP());
        p.setDaWu(p2.isDaWu());
        p.setKuangFeng(p2.isKuangFeng());
        p.setIdentityAndSkill(p2.getIdentity());
        p.setDrunk(p2.isDrunk());
        p.setDrunkShaUsed(p2.isDrunkShaUsed());
        p.setShaCount(p2.getShaCount());
        p.setGamePlayerNo(p2.getGamePlayerNo());

        if (p2.isLinked() != p.isLinked()) {
            p.switchLink();
        }
        if (p2.isTurnedOver() != p.isTurnedOver()) {
            p.turnover();
        }
        p.getCards().clear();
        p.getCards().addAll(p2.getCards());
        for (Equipment equipment : p2.getEquipments().values()) {
            p.getEquipments().put(equipment.getEquipType(), equipment);
        }
        p.getJudgeCards().clear();
        p.getJudgeCards().addAll(p2.getJudgeCards());
        if (p2.getExtraCards() != null && p2.getGameManager() != null) {// 之前的收集的集气，集田之类的牌，全部丢进废牌
            p2.getGameManager().getCardsHeap().discard(p2.getExtraCards());
            p2.getExtraCards().clear();
        }
        // loseCard(p.getCards(), false, false);
        // loseCard(new ArrayList<>(p.getRealJudgeCards()), false, false);
        // loseCard(new ArrayList<>(p.getEquipments().values()), false, false);
        p2.getCards().clear();
        p2.getRealJudgeCards().clear();
        p2.getEquipments().clear();

        p.setHuaShenList(p2.getHuaShenList());
        // if(hasWakenUp()){
        // p.wakeUp();
        // }
        // selectHuaShen(p);
        // p.setZuoCi(p2.isZuoCi());
        int index = getPlayers().indexOf(p2);
        // p.setPos(index);
        getPlayers().set(index, p);// 这个不能在多线程里设，会错
        p.setProgress(p2.getProgress());
        idMap.get(p2.getIdentity()).remove(p2);
        idMap.get(p2.getIdentity()).add(p);
        idMapStatic.get(p2.getIdentity()).remove(p2);
        idMapStatic.get(p2.getIdentity()).add(p);

        // System.out.println(p + "技能 =" + p.getActiveSkills());
    }

    /**
     * 睡眠
     * @param time 睡眠时间
     */  
    public void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 获取闸门列表
     * @return 闸门列表
     */  
    public Set<MyCountDownLatch> getLatchs() {
        return latchs;
    }

    /**
     * 结束游戏
     */  
    public void endGame() {
        if (gameStatus == Status.end) {
            return;
        }
        gameStatus = Status.end;

        msgApi.endGame(this.chatId);
        // --------------清理----------------
        // --------释放所有闸门-----------
        for (MyCountDownLatch latch : latchs) {
            if (latch.getCount() > 0) {
                latch.countDown();
            }
        }
        latchs.clear();
        // --------清除回调码-----------
        for (Person p : players) {
            if (p.getUser() != null) {
                getMsgAPI().getCallBackMap().clear(p.getUser().user_id);
            }

        }

        for (Person p : dead) {
            if (p.getUser() != null) {
                getMsgAPI().getCallBackMap().clear(p.getUser().user_id);
            }

        }

        getMsgAPI().getCallBackMap().clear(chatId);
        // ---------删掉展示的总览信息-----------------
        for (Person p : players) {
            if (p.getShowStatusMsg() != null) {
                getMsgAPI().delMsg(p.getShowStatusMsg());
            }

        }

    }

    /**
     * 初始选人
     * 
     * @param people
     * @param user
     * @param lt
     * @return
     */
    public Person initialChoosePerson(Identity identity, List<Person> people, SanUser user, List<SanUser> users,
            LatchManager lt) {
        // Utils.assertTrue(!people.isEmpty(), "initial people are empty");
        ArrayList<String> options = new ArrayList<>();
        for (Person p1 : people) {
            options.add(p1.toString());
        }
        // printlnToIO("choose a psoner:");

        MsgObj inMsgObj = MsgObj.newMsgObj(lt);
        inMsgObj.chatId = user.user_id;
        inMsgObj.setImg(Person.getIdentityString(identity));
        String res = Text.format("<b>你可挑选的角色：</b>\n");
        for (Person p : people) {
            int[] rating = Rating.getRating(p.toString());

            res += "\n🔸<b>" + DescUrl.getDescHtml(p.toString()) + "</b>";
            if (rating != null) {
                String ratString = "<b>\n评分:";
                if (rating[0] != -1) {
                    ratString += "主[" + rating[0] + "],";
                }
                if (rating[1] != -1) {
                    ratString += "忠[" + rating[1] + "],";
                }
                if (rating[2] != -1) {
                    ratString += "反[" + rating[2] + "],";
                }
                if (rating[3] != -1) {
                    ratString += "内[" + rating[3] + "]</b>";
                }
                //ratString += "\n";
                res += ratString;
            }
            res += "\n<i>" + p.skillsDescription() + "</i>\n";
        }
        res += Text.format("\n<b>顺位图：</b>\n");

        for (int i = 0; i < users.size(); i++) {
            SanUser other = users.get(i);
            if (i == 0) {
                if (getKing() == null) {
                    res += Text.circleNum(i + 1) + "👑" + other.getUserMentionText();
                } else {
                    res += Text.circleNum(i + 1) + "👑" + getKing().getHtmlName();
                }

            } else {
                res += Text.circleNum(i + 1) + other.getUserMentionText();
            }
            res += "\n";
        }

        res += Text.format("<blockquote><b>你的身份是 %s</b></blockquote>\n",
                DescUrl.getDescHtml(Person.getIdentityString(identity)));

        inMsgObj.text = res;
        msgApi.chooseOneFromOpinion(people, inMsgObj,"🔸","");
        String idx = inMsgObj.getString(ReturnType.ChooseOneCard, Config.PRIV_RND_CHOOSE_ROLE_TIME_90S / 1000);
        msgApi.delMsg(inMsgObj);
        // System.out.println("idx=" + idx);
        // panic("end of initialChoosePlayer reached");
        if (idx == null) {
            return people.get(0);
        }
        return people.get(Integer.valueOf(idx) - 1);
    }

    /**
     * 添加消息到本轮
     * @param msgObj 消息
     */  
    public void addMsgToThisRound(MsgObj msgObj) {
        msgDeque.addLast(msgObj);
    }

    /**
     * 清除预消息
     * @param permitRemain 允许剩余消息数
     */  
    public void clearPreMsg(int permitRemain) {

        if (msgDeque.size() <= permitRemain) {
            return;
        }
        List<MsgObj> needDelMsgs = new ArrayList<>(); // 需要删的
        List<MsgObj> keepDelMsgs = new ArrayList<>();// 需要保留的
        while (msgDeque.size() > 0) {
            MsgObj msg = msgDeque.pollLast();
            if (msg.chatId == chatId && !msg.isDeleted && !msg.isUpdateStatus) {
                if (keepDelMsgs.size() < permitRemain) {
                    keepDelMsgs.add(msg);
                } else {
                    needDelMsgs.add(msg);
                }
            }
        }

        for (int i = keepDelMsgs.size() - 1; i >= 0; i--) {// 把那些不删的加回队列
            msgDeque.add(keepDelMsgs.get(i));
        }
        msgApi.mutiDelMsg(needDelMsgs);
    }

    /**
     * 返回死亡列表，是原表，不是一个新表
     * 
     * @return
     */
    public List<Person> getDead() {
        return dead;
    }

    /**
     * 获取用户列表
     * @return 用户列表
     */   
    public List<SanUser> getUserList() {
        return sanUsers;
    }

    /**
     * 获取初始玩家数量
     * 
     * @return
     */
    public int getIntialNumPlayers() {
        return intialNumPlayers;
    }

    /**
     * 获取人物回合数
     * @return 人物回合数
     */  
    public int getPersonRound() {
        return personRound;
    }

    /**
     * 增加人物回合数
     */     
    public void addPersonRound() {
        this.personRound++;
    }

    
}
