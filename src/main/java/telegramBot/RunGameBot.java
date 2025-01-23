package telegramBot;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Future;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessages;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.LinkPreviewOptions;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.name.BotName;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import config.Config;
import config.Text;
import db.ImgDB;
import msg.ReturnType;
import msg.CallBackMap;
import msg.CallbackEven;
import msg.MsgAPI;
import msg.MsgObj;

import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.name.GetMyName;

import sanguosha.cards.Card;
import sanguosha.manager.GameManager;
import sanguosha.people.Person;
import sanguosha.user.SanUser;
import components.BlockingMap;
import components.TimeLimit;

/**
 * 这个类一定不能有阻塞方法，如果阻塞了这个机器人，程序就会变卡，阻塞方法放在游戏管理器那边
 */
public class RunGameBot implements LongPollingSingleThreadUpdateConsumer, MsgAPI {

    private final TelegramClient telegramClient;
    private final CallBackMap callBackMap = new CallBackMap();
    private boolean testMode = false;
    private String bootName;

    public CallBackMap getCallBackMap() {
        return callBackMap;
    }

    private ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(50, 1000, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>());

    private JoinGameContrller joinGameCtrl;

    /**
     * 正常选人，不补AI
     * 
     * @param botToken
     */
    public RunGameBot(String botToken) {
        testMode = false;
        telegramClient = new OkHttpTelegramClient(botToken);
        getBotName();
        joinGameCtrl = new JoinGameContrller(this);

    }

    /**
     * 测试，正常选人，会补AI
     * 
     * @param botToken
     * @param testMode
     */
    public RunGameBot(String botToken, boolean testMode) {
        // testMode = true;
        telegramClient = new OkHttpTelegramClient(botToken);
        getBotName();
        if (testMode) {
            joinGameCtrl = new JoinGameContrller(this, testMode);
        } else {
            joinGameCtrl = new JoinGameContrller(this);
        }

    }

    /**
     * 测试，指定人，牌，补AI
     * 
     * @param botToken
     * @param test
     * @param testRoles
     * @param testCardClass
     */
    public RunGameBot(String botToken, boolean test, Class<? extends Person>[] testRoles,
            Class<? extends Card>[] testCardClass) {
        testMode = true;
        joinGameCtrl = new JoinGameContrller(this, testMode);
        telegramClient = new OkHttpTelegramClient(botToken);
        getBotName();
        RunGameBot thisBot = this;
        Future<String> f = threadPoolExecutor.submit(
                new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        int i = 10;
                        while (i > 0) {
                            try {
                                long ChatId = -1002308456428L;
                                List<SanUser> users = new ArrayList<>();
                                //这里单机测试
                                users.add(new SanUser("测试人", 11111L, "@xxxxxx"));
                                GameManager g = new GameManager(thisBot, users);
                                g.setTestMode(test, testRoles, testCardClass);
                                g.runGame(ChatId);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            i--;
                        }
                        return "done";
                    }

                });

    }

    private boolean isMsgFromGroup(Message ApiMsg) {
        if (ApiMsg.getChat().isGroupChat() || ApiMsg.getChat().isSuperGroupChat()) {
            return true;
        }
        return false;
    }

    private void getBotName() {
        GetMyName getMyName = GetMyName.builder().build();
        try {
            BotName bn = telegramClient.execute(getMyName);
            bootName = bn.getName();
        } catch (TelegramApiException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void consume(Update update) {
        // 这个方法好像是单线程的，相同用户来的都是同一个线程

        if (update.hasCallbackQuery()) {
            dealButtonCallBack(update);
        } else if (update.getMessage() != null
                && update.getMessage().isCommand()) {// 处理那种deeplink的命令

            // System.out.println(update.getMessage());

            String[] commandAndPara = update.getMessage().getText().split(" ");

            // https://t.me/mxbjqr_bot?start=UE1ZV3RtVVFmUlJnd2dHNmVwTkdRUT09
            // /start UE1ZV3RtVVFmUlJnd2dHNmVwTkdRUT09
            if (commandAndPara.length < 2) {
                if (update.getMessage().isUserMessage()) {
                    switch (commandAndPara[0]) {
                        case "/start":
                            joinGameCtrl.start(update);
                            break;
                        case "/ls":
                            joinGameCtrl.lsGame(update);
                            break;
                        default:
                            break;
                    }

                }
                if (isMsgFromGroup(update.getMessage()) &&
                        ("/sha".equals(commandAndPara[0]) || "/sha@shanguoshabot".equals(commandAndPara[0]))) {
                    if (testMode) {
                        joinGameCtrl.createGameTest(update);
                    } else {
                        joinGameCtrl.createGame(update);

                    }
                }
                return;
            }
            // System.out.println("commandAndPara[1]=" + commandAndPara[1]);
            String commandPara = commandAndPara[1];

            // -------------创建游戏-----------------

            // --------------这里有个参加逻辑也是写在这里--------------

            if ("join".equals(commandPara)) {
                // TODO 加入逻辑
                return;
            }
            dealDeepLink(update, commandPara);
        }
    }

    public void submitRunnable(Runnable runnable) {
        threadPoolExecutor.execute(runnable);
    };

    public <E> Future<E> submitCallable(Callable<E> callable) {
        return threadPoolExecutor.submit(callable);
    }

    /**
     * 比较两个按钮数组是否相同
     * 
     * @param button1
     * @param button2
     * @return
     */
    private boolean compareButtons(List<List<String[]>> button1, List<List<String[]>> button2) {

        if (button1 == null || button2 == null) {
            return false;
        }
        if (button1.size() != button2.size()) {
            return false;
        }
        for (int i = 0; i < button1.size(); i++) {
            List<String[]> button11 = button1.get(i);
            List<String[]> button22 = button2.get(i);
            if (button11.size() != button22.size()) {
                return false;
            }
            for (int j = 0; j < button11.size(); j++) {
                String[] pair1 = button11.get(j);
                String[] pair2 = button22.get(j);
                if (pair1.length != pair2.length) {
                    return false;
                }
                for (int k = 0; k < pair1.length; k++) {
                    if (!pair1[k].equals(pair2[k])) {
                        return false;
                    }
                }
            }

        }
        return true;

    }

    private void dealButtonCallBack(Update update) {
        // Set variables
        String call_data = update.getCallbackQuery().getData();
        // System.out.println("call_data " + call_data);
        System.out.println("callBackMap " + callBackMap);

        long message_id = update.getCallbackQuery().getMessage().getMessageId();
        long chat_id = update.getCallbackQuery().getMessage().getChatId();
        String callBackId = update.getCallbackQuery().getId();
        String callBackCode = formatCallBackCode(chat_id, message_id);
        MsgObj msgObj = callBackMap.get(callBackCode);
        // System.out.println(msgObj);
        User user = update.getCallbackQuery().getFrom();
        Long thisUserId = user.getId();
        if (msgObj != null) {
            if (msgObj.isChooseOneCardPublic && msgObj.user_chatId != thisUserId) {
                answerCallBack(callBackId, "❌ 你不是角色的操作人,无权操作", false);
                return;
            } else if (msgObj.isDeeplink && msgObj.user_chatId != thisUserId) {
                answerCallBack(callBackId, "❌ 你不是角色的操作人,无权操作", false);
                return;
            } else// 这个是多人选一个
            if (msgObj.isMultyDeeplink && !msgObj.forPlayersUserIdSet.contains(thisUserId)) {
                answerCallBack(callBackId, "❌ 你不必操作", false);
                return;
            } else if (msgObj.isChooseOneOpinionPublic && msgObj.user_chatId != thisUserId) {
                answerCallBack(callBackId, "❌ 你不是角色的操作人,无权操作", false);
                return;
            } else if (msgObj.isVotePublic && !msgObj.forPlayersUserIdSet.contains(thisUserId)) {
                answerCallBack(callBackId, "❌ 你不必操作", false);
                return;
            }
            // --------------------存callBackId----------------
            if (msgObj.isMultyDeeplink || msgObj.isVotePublic) { // 这里可能同时有多个人的回调id
                msgObj.callbackIds.put(thisUserId, callBackId);
            } else {
                msgObj.setAttributes(ReturnType.CallBackId, callBackId);
            }
            // --------------------结果-------------------------
            if (msgObj.isChooseOneCard) {
                msgObj.setAttributes(ReturnType.ChooseOneCard, call_data);
            } else if (msgObj.isDeeplink) {
                msgObj.setAttributes(ReturnType.Deeplink, call_data);
            } else if (msgObj.isChooseMany) {
                msgObj.setAttributes(ReturnType.ChooseManyThisTime, call_data);
            } else if (msgObj.isMultyDeeplink) {
                Map<Long, CallbackEven> deepLinkCallback = msgObj.sonCallback;
                if (deepLinkCallback == null) {
                    return;
                }
                CallbackEven callBack = deepLinkCallback.get(thisUserId);
                if (callBack == null) {
                    answerCallBack(callBackId, "❌ 您不必操作", false);
                    return;
                }
                callBack.DeepLinkExec(msgObj, call_data);
            } else if (msgObj.isChooseOneCardPublic) {
                msgObj.setAttributes(ReturnType.ChooseOneCardPublic, call_data);
            } else if (msgObj.isUpdateStatus) {
                // System.out.println(msgObj);
                msgObj.setAttributes(ReturnType.UpdateStatus, call_data);
            } else if (msgObj.isChooseOneOpinionPublic) {
                msgObj.setAttributes(ReturnType.ChooseOneOpinionPublic, call_data);
            } else if (msgObj.isVotePublic) {
                Map<Long, CallbackEven> voteCallback = msgObj.sonCallback;
                if (voteCallback == null) {
                    return;
                }
                CallbackEven callBack = voteCallback.get(thisUserId);
                if (callBack == null) {
                    answerCallBack(callBackId, "❌ 您不必操作", false);
                    return;
                }
                callBack.vote(user.getId(), call_data);
            }

        }
    }

    private void dealDeepLink(Update update, String commandPara) {
        User user = update.getMessage().getFrom();
        Long thisUserId = user.getId();
        // System.out.println("dealDeepLink1");
        // --------------游戏逻辑------------------------------

        if (joinGameCtrl.checkJoinGame(commandPara, user)) {
            DeleteMessage del = DeleteMessage.builder()
                    .chatId(update.getMessage().getChatId())
                    .messageId(update.getMessage().getMessageId())
                    .build();
            asyncSend(del);
            return;
        }
        String idx = update.getMessage().getFrom().getId() + "_deeplink";
        // System.out.println("deeplink callBackMap=" + callBackMap);
        MsgObj msgObj = callBackMap.get(idx);
        if (msgObj == null) {
            DeleteMessage del = DeleteMessage.builder()
                    .chatId(update.getMessage().getChatId())
                    .messageId(update.getMessage().getMessageId())
                    .build();
            asyncSend(del);
            return;
        }
        // 先删掉这消息

        DeleteMessage del = DeleteMessage.builder()
                .chatId(update.getMessage().getChatId())
                .messageId(update.getMessage().getMessageId())
                .build();
        asyncSend(del);
        // System.out.println("先删掉这消息 dealDeepLink2");
        // ------------处理逻辑----------------------
        if (msgObj.isDeeplink) {
            msgObj.setAttributes(ReturnType.Deeplink, commandPara);
        } else if (msgObj.isMultyDeeplink) {
            Map<Long, CallbackEven> deepLinkCallback = msgObj.sonCallback;
            if (deepLinkCallback == null) {
                return;
            }
            CallbackEven callBack = deepLinkCallback.get(thisUserId);
            if (callBack == null) {
                return;
            }
            threadPoolExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    callBack.DeepLinkExec(msgObj, commandPara);
                }
            });

        }

        // -------------删掉这条start消息---------
        // System.out.println("dealDeepLink3");

    }

    /**
     * 只获取部分想要返回的值
     * 
     * @param resultMsg
     * @param msg
     * @param getResaultType
     */
    private void fillResult(Message resultMsg, MsgObj msg, boolean fillReturn) {
        // msg.delayMsgId = resultMsg.getMessageId();
        if (!fillReturn) {
            return;
        }
        // Message apiMsg = (Message) msg.getReturnAttr(ReturnType.Result, 0);
        System.out.println("oldMsgId resultMsg.getMessageId():" + msg.oldMsgId + "," + resultMsg.getMessageId());
        // if (resultMsg.getMessageId() > msg.oldMsgId) {// 只接收新的msgid，edit的就不接收了
        msg.setAttributes(ReturnType.Msgid, resultMsg.getMessageId());
        msg.oldMsgId = resultMsg.getMessageId();
        // } else {
        // return;
        // }

        msg.setAttributes(ReturnType.Result, resultMsg);

        // if (resultMsg.getPhoto() != null && resultMsg.getPhoto().size() > 0) {//
        // 只处理第一张
        // String fileId = resultMsg.getPhoto().get(0).getFileId();
        // // System.out.println("ReturnType.ImgFileId:"+fileId);
        // msg.setAttributes(ReturnType.ImgFileId, fileId);
        // }

    }

    @Override
    public void delMsgForce(MsgObj msg) {
        boolean preStaus = msg.isDeleted;
        msg.isDeleted = false;
        delMsg(msg);
        msg.isDeleted = preStaus;
        sleep(100);
    }

    @Override
    public void delMsgForce(MsgObj msg, boolean fillReturn) {
        boolean preStaus = msg.isDeleted;
        msg.isDeleted = false;
        delMsg(msg, fillReturn);
        msg.isDeleted = preStaus;
        sleep(100);
    }

    @Override
    public void delMsg(MsgObj msg, boolean fillReturn) {
        // sleep(1000L);
        try {

            if (msg == null) {
                return;
            }
            if (msg.isDeleted) {
                return;
            }
            // msg.editCount.addAndGet(1);
            msg.isDeleted = true;
            long chat_id = msg.chatId;
            long msgId = msg.getReturnMsgId(0);
            if (msgId < 0) {// 就是起始没有发送信息
                return;
            }
            String callBackCode = formatCallBackCode(chat_id, msgId);
            callBackMap.remove(callBackCode);

            if (msg.isDeeplink) {
                callBackMap.remove(msg.user_chatId + "_deeplink");
            } else if (msg.isMultyDeeplink) {
                for (Long user_id : msg.forPlayersUserIdSet) {
                    callBackMap.remove(user_id + "_deeplink");
                }
            }
            DeleteMessage del = DeleteMessage.builder()
                    .chatId(chat_id)
                    .messageId((int) msgId)
                    .build();
            asyncSend(del, msg, fillReturn);
        } finally {
            // sleep(1000L);
            sleep(100);
        }
    }

    public void mutiDelMsg(List<MsgObj> msgs, boolean fillReturn) {
        sleep(1000L);
        try {
            Long chat_id_all = null;
            List<Integer> msgIds = new ArrayList<>();
            for (MsgObj msg : msgs) {
                if (msg == null) {
                    continue;
                }
                if (msg.isDeleted) {
                    continue;
                }
                // msg.editCount.addAndGet(1);
                msg.isDeleted = true;
                long chat_id = msg.chatId;
                chat_id_all = chat_id;

                long msgId = msg.getReturnMsgId(0);
                if (msgId < 0) {// 就是起始没有发送信息
                    continue;
                }
                msgIds.add((int) msgId);
                String callBackCode = formatCallBackCode(chat_id, msgId);
                callBackMap.remove(callBackCode);

                if (msg.isDeeplink) {
                    callBackMap.remove(msg.user_chatId + "_deeplink");
                } else if (msg.isMultyDeeplink) {
                    for (Long user_id : msg.forPlayersUserIdSet) {
                        callBackMap.remove(user_id + "_deeplink");
                    }
                }
            }
            if (msgIds.size() == 0) {
                return;
            }

            DeleteMessages deleteMessages = DeleteMessages.builder()
                    .chatId(chat_id_all)
                    .messageIds(msgIds).build();
            asyncSend(deleteMessages);
        } finally {
            sleep(1000L);
        }
    }

    @Override
    public void sendMsg(MsgObj msg, boolean fillReturn) {
        if (msg == null) {
            return;
        }
        String message_text = msg.text;
        message_text = (message_text == null || message_text.equals("")) ? "message:" : message_text;

        InlineKeyboardMarkup replyMakup;
        if (msg.replyMakup == null) {
            replyMakup = null;
        } else {
            replyMakup = buildMarkup(msg.replyMakup);
        }

        SendMessage message = SendMessage // Create a message object
                .builder()
                .chatId(msg.getChatId())
                .replyToMessageId(msg.replyToMessageId)
                .text(message_text)
                .replyMarkup(replyMakup)
                .parseMode("HTML")
                .linkPreviewOptions(LinkPreviewOptions.builder().isDisabled(true).build())
                .build();
        asyncSend(message, msg, fillReturn);
        sleep(100);
    }

    private boolean isSameMsg(Message apiMsg, String text, ReplyKeyboard makup) {
        // System.out.println(apiMsg);
        String textApi = apiMsg.getCaption();
        if (textApi == null) {
            textApi = apiMsg.getText();
        }
        if (textApi.equals(text)) {
            if (apiMsg.getReplyMarkup() == null && makup == null) {
                return true;
            }
            if (apiMsg.getReplyMarkup().equals(makup)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void editMsg(MsgObj msg, boolean fillReturn) {
        if (msg == null) {
            return;
        }
        if (msg.isDeleted) {
            return;
        }
        Integer msgId = msg.getReturnMsgId(0);
        if (msgId == -1) {
            return;
        }

        InlineKeyboardMarkup replyMakup;
        if (msg.replyMakup == null) {
            replyMakup = null;
            // 没有按钮就不需要回调码了
            // int newMsgId=msg.getReturnMsgId( 3);
            callBackMap.remove(formatCallBackCode(msg.chatId, msgId));
        } else {
            replyMakup = buildMarkup(msg.replyMakup);
        }
        // msg.resetAttributes(ReturnType.Msgid);
        EditMessageText message = EditMessageText // Create a message object
                .builder()
                .chatId(msg.getChatId())
                .messageId(msgId)
                .text(msg.text)
                .replyMarkup(replyMakup)
                .parseMode("HTML")
                .linkPreviewOptions(LinkPreviewOptions.builder().isDisabled(true).build())
                .build();

        asyncSend(message, msg, fillReturn);
        sleep(100);
    }

    private void editCaption(MsgObj msg, boolean fillReturn) {
        sleep(1000L);
        try {

            if (msg == null) {
                return;
            }
            if (msg.isDeleted) {
                return;
            }
            if (msg.localImgFileStream == null && msg.localImgFileId == null) {
                editMsg(msg, fillReturn);
                return;
            }
            Integer msgId = msg.getReturnMsgId(0);
            if (msgId == -1) {
                return;
            }

            InlineKeyboardMarkup replyMakup;
            if (msg.replyMakup == null) {
                replyMakup = null;
                // 没有按钮就不需要回调码了
                // int newMsgId=msg.getReturnMsgId(ReturnType.Msgid, 3);
                callBackMap.remove(formatCallBackCode(msg.chatId, msgId));
            } else {
                replyMakup = buildMarkup(msg.replyMakup);
            }

            Message apiMsg = (Message) msg.getReturnAttr(ReturnType.Result, 0);
            if (apiMsg != null) {// 消息没更改就不发送
                if (isSameMsg(apiMsg, msg.text, replyMakup)) {

                    // System.out.println("消息相同！！！！！！！！！");
                    return;
                }
            }

            // msg.resetAttributes(ReturnType.Msgid);
            EditMessageCaption message = EditMessageCaption // Create a message object
                    .builder()
                    .chatId(msg.getChatId())
                    .messageId(msgId)
                    .caption(msg.text)
                    .replyMarkup(replyMakup)
                    .parseMode("HTML")
                    .build();
            asyncSend(message, msg, fillReturn);
        } finally {
            sleep(1000L);
        }
    }

    @Override
    public void editCaptionForce(MsgObj msg, boolean fillReturn) {
        boolean preStaus = msg.isDeleted;
        msg.isDeleted = false;
        editCaption(msg, fillReturn);
        msg.isDeleted = preStaus;
        sleep(100);
    }

    private void editPhoto(MsgObj msg, boolean fillReturn) {
        sleep(1000L);
        try {

            if (msg == null) {
                return;
            }
            if (msg.isDeleted) {
                return;
            }
            if (msg.localImgFileStream == null && msg.localImgFileId == null) {
                editMsg(msg, fillReturn);
                return;
            }
            Integer msgId = msg.getReturnMsgId(0);
            if (msgId == -1) {
                return;
            }

            InlineKeyboardMarkup replyMakup;
            if (msg.replyMakup == null) {
                replyMakup = null;
                // 没有按钮就不需要回调码了
                // int newMsgId=msg.getReturnMsgId(ReturnType.Msgid, 3);
                callBackMap.remove(formatCallBackCode(msg.chatId, msgId));
            } else {
                replyMakup = buildMarkup(msg.replyMakup);
            }

            // Message apiMsg = (Message) msg.getReturnAttr(ReturnType.Result, 0);
            // if (apiMsg != null) {// 消息没更改就不发送
            // if (isSameMsg(apiMsg, msg.text, replyMakup)) {

            // // System.out.println("消息相同！！！！！！！！！");
            // return;
            // }
            // }
            InputMedia inputMedia = (msg.localImgFileId == null)
                    ? new InputMediaPhoto(msg.localImgFileStream, msg.imgName.get())
                    : new InputMediaPhoto(msg.localImgFileId);
            inputMedia.setCaption(msg.text);
            inputMedia.setParseMode("HTML");

            // msg.resetAttributes(ReturnType.Msgid);
            EditMessageMedia message = EditMessageMedia // Create a message object
                    .builder()
                    .chatId(msg.getChatId())
                    .messageId(msgId)
                    .media(inputMedia)
                    .replyMarkup(replyMakup)
                    .build();
            asyncSend(message, msg, fillReturn);
        } finally {
            sleep(1000L);
        }
    }

    @Override
    public void editPhotoForce(MsgObj msg) {
        editPhotoForce(msg, true);
    }

    @Override
    public void editPhotoForce(MsgObj msg, boolean fillReturn) {
        boolean preStaus = msg.isDeleted;
        msg.isDeleted = false;
        editPhoto(msg, fillReturn);
        msg.isDeleted = preStaus;
        sleep(100);
    }

    @Override
    public void answerCallBack(String callBackId, String text, boolean showAlert) {

        String answerText = text;
        AnswerCallbackQuery answer = AnswerCallbackQuery.builder()
                .callbackQueryId(callBackId)
                .text(answerText)
                .build();
        asyncSend(answer);
        sleep(100);

    }

    @Override
    public void answerCallBack(MsgObj msg, String text, boolean showAlert) {
        if (msg == null) {
            return;
        }
        // 如果已经响应过就不再响应
        String lastAnser = msg.getString(ReturnType.LastAnswer, 0);

        String callBackId = msg.getString(ReturnType.CallBackId, 1);
        if (lastAnser != null && lastAnser.equals(callBackId)) {
            return;
        }
        String answerText = text;
        AnswerCallbackQuery answer = AnswerCallbackQuery.builder()
                .callbackQueryId(callBackId)
                .text(answerText)
                .build();
        asyncSend(answer);
        msg.setAttributes(ReturnType.LastAnswer, callBackId);
        sleep(100);
    }

    @Override
    public void sendImg(MsgObj msg, boolean fillReturn) {
        if (msg == null) {
            return;
        }
        if (msg.localImgFileStream == null && msg.localImgFileId == null) {
            sendMsg(msg, fillReturn);
            return;
        }
        String message_text = msg.text;
        message_text = (message_text == null || message_text.equals("")) ? "message:" : message_text;
        InlineKeyboardMarkup replyMakup;
        if (msg.replyMakup == null) {
            replyMakup = null;
        } else {
            replyMakup = buildMarkup(msg.replyMakup);
        }
        SendPhoto message = SendPhoto // Create a message object
                .builder()
                .chatId(msg.getChatId())
                .replyToMessageId(msg.replyToMessageId)
                .photo(msg.localImgFileId == null ? new InputFile(msg.localImgFileStream, msg.imgName.get())
                        : new InputFile(msg.localImgFileId))
                .caption(message_text)
                .replyMarkup(replyMakup)
                .parseMode("HTML")
                .build();
        asyncSend(message, msg, fillReturn);
        sleep(100);
    }

    @Override
    public void editButtons(MsgObj msg, boolean fillReturn) {
        if (msg == null) {
            return;
        }

        if (msg.isDeleted) {
            return;
        }
        Integer msgId;
        if (msg.oldMsgId > 0) {
            msgId = msg.oldMsgId;
        } else {
            msgId = msg.getReturnMsgId(0);
        }
        if (msgId == -1) {
            return;
        }
        ReplyKeyboard replyMarkup = buildMarkup(msg.replyMakup);
        Message apiMsg = (Message) msg.getReturnAttr(ReturnType.Result, 0);
        if (isSameMsg(apiMsg, msg.text, replyMarkup)) {
            return;
        }

        // msg.resetAttributes(ReturnType.Msgid);
        EditMessageReplyMarkup message = EditMessageReplyMarkup // Create a message object
                .builder()
                .chatId(msg.getChatId())
                .messageId(msgId)
                .replyMarkup(buildMarkup(msg.replyMakup))
                .build();
        asyncSend(message, msg, fillReturn);
        sleep(100);
    }

    @Override
    public void clearButtons(MsgObj msg, boolean fillReturn) {
        if (msg == null) {
            return;
        }
        // 相当于删除
        if (msg.isDeleted) {
            return;
        }
        // msg.isDelete=true;
        Integer newMsgId = msg.getReturnMsgId(0);

        if (newMsgId == -1) {
            return;
        }

        // msg.replyMakup.clear();
        msg.replyMakup = null;
        // int newMsgId=msg.getInt(ReturnType.Msgid, 3);

        System.out.println("clearButtons=" + newMsgId);
        if (newMsgId != -1) {
            callBackMap.remove(formatCallBackCode(msg.chatId, newMsgId));
            if (msg.isDeeplink) {
                callBackMap.remove(msg.user_chatId + "_deeplink");
            } else if (msg.isMultyDeeplink) {
                for (Long user_id : msg.forPlayersUserIdSet) {
                    callBackMap.remove(user_id + "_deeplink");
                }
            }
        }
        // msg.resetAttributes(ReturnType.Msgid);
        EditMessageReplyMarkup message = EditMessageReplyMarkup // Create a message object
                .builder()
                .chatId(msg.getChatId())
                .messageId(newMsgId)
                .replyMarkup(null)
                .build();
        asyncSend(message, msg, fillReturn);
        sleep(100);
    }

    /** 从键值对换成按键和回调内容 */
    private InlineKeyboardMarkup buildMarkup(List<List<String[]>> kvList) {
        if (kvList == null) {
            return null;
        }
        List<InlineKeyboardRow> rows = new ArrayList<>();
        for (List<String[]> r1 : kvList) {
            List<InlineKeyboardButton> buttons = new ArrayList<>();
            for (String[] entry : r1) {
                if (entry.length == 3 && entry[2] == "deeplink") {
                    buttons.add(
                            InlineKeyboardButton
                                    .builder()
                                    .text(entry[0])
                                    .url(Text.format("https://t.me/%s?start=%s", bootName, entry[1]))
                                    .build());
                } else {
                    buttons.add(
                            InlineKeyboardButton
                                    .builder()
                                    .text(entry[0])
                                    .callbackData(entry[1])
                                    .build());
                }

            }
            InlineKeyboardRow InlineKeyboardRow = new InlineKeyboardRow(buttons);
            rows.add(InlineKeyboardRow);
        }
        return new InlineKeyboardMarkup(rows);
    }

    /** 异步发送,不需要任何返回 */
    public <T extends Serializable, Method extends BotApiMethod<T>> void asyncSend(Method method) {
        asyncSend(method, null, false);
    }

    /** 异步发送 */
    public <T extends Serializable, Method extends BotApiMethod<T>> void asyncSend(Method method, MsgObj msg,
            boolean fillReturn) {
        if (msg != null) {
            msg.unlockAttrbutes(ReturnType.Msgid);
            msg.resetAttributes(ReturnType.Msgid);
            // msg.resetAll();
        }
        threadPoolExecutor.submit(
                new Runnable() {
                    @Override
                    public void run() {
                        int round = 0;
                        // System.out.println("msg " + msg);
                        while (round < 2) {
                            try {

                                Serializable result = telegramClient.execute(method); // Sending our message object to
                                                                                      // user
                                // System.out.println("result:"+result);
                                if (result instanceof Message) {
                                    // 这里放返回逻辑
                                    Message resultMsg = (Message) result;
                                    fillResult(resultMsg, msg, fillReturn);

                                } else {
                                    // System.out.println("asyncSend 返回的不是消息对象");
                                }
                                break;
                            } catch (TelegramApiException e) {
                                System.out.println(e);
                                // System.out.println(getResaultType);
                                // System.out.println(msg.text);
                                // e.printStackTrace();
                            }
                            round++;
                        }

                    }

                });
    }

    /** 异步发送 */
    private void asyncSend(SendPhoto method, MsgObj msg, boolean fillReturn) {
        if (msg != null) {
            msg.unlockAttrbutes(ReturnType.Msgid);
            msg.resetAttributes(ReturnType.Msgid);
            // msg.resetAll();
        }

        threadPoolExecutor.submit(
                new Runnable() {
                    @Override
                    public void run() {
                        int round = 0;
                        while (round < 2) {
                            try {
                                Message result = telegramClient.execute(method); // Sending our message object to user
                                fillResult(result, msg, fillReturn);
                                if (msg.imgWatingCallBack.get() != null) {
                                    PhotoSize pz = result.getPhoto().get(0);
                                    msg.imgWatingCallBack.get().sendImgMsgDone(pz.getFileId(), pz.getFileUniqueId());
                                }
                                break;
                            } catch (TelegramApiException e) {
                                System.out.println(e);
                                if (e.toString().contains("wrong file")) {// 图片出错可能是图片失效了，重新发一张
                                    // 清除缓存，重新读本地文件
                                    ImgDB.clearImgChache(msg.imgName.get());
                                    ImgDB.setImg(msg, msg.imgName.get());
                                    method.setPhoto(
                                            msg.localImgFileId == null
                                                    ? new InputFile(msg.localImgFileStream, msg.imgName.get())
                                                    : new InputFile(msg.localImgFileId));
                                }

                            }
                            round++;
                        }

                    }

                });

    }

    /** 异步发送 */
    private void asyncSend(EditMessageMedia method, MsgObj msg, boolean fillReturn) {
        if (msg != null) {
            msg.unlockAttrbutes(ReturnType.Msgid);
            msg.resetAttributes(ReturnType.Msgid);
            // msg.resetAll();
        }

        threadPoolExecutor.submit(

                new Runnable() {
                    @Override
                    public void run() {
                        int round = 0;
                        while (round < 2) {
                            try {
                                Message result = (Message) telegramClient.execute(method); // Sending our message object
                                                                                           // to user
                                fillResult(result, msg, fillReturn);
                                if (msg.imgWatingCallBack.get() != null) {
                                    PhotoSize pz = result.getPhoto().get(0);

                                    msg.imgWatingCallBack.get().sendImgMsgDone(pz.getFileId(), pz.getFileUniqueId());
                                }
                                break;
                            } catch (TelegramApiException e) {
                                System.out.println(e);
                                if (e.toString().contains("wrong file")) { // 图片出错可能是图片失效了，重新发一张
                                    // 清除缓存，重新读本地文件
                                    ImgDB.clearImgChache(msg.imgName.get());
                                    ImgDB.setImg(msg, msg.imgName.get());
                                    InputMedia inputMedia = (msg.localImgFileId == null)
                                            ? new InputMediaPhoto(msg.localImgFileStream, msg.imgName.get())
                                            : new InputMediaPhoto(msg.localImgFileId);
                                    inputMedia.setCaption(msg.text);
                                    inputMedia.setParseMode("HTML");
                                    method.setMedia(inputMedia);
                                }

                            }
                            round++;
                        }

                    }

                });

    }

    // ---------------------------------------游戏选择--------------------------------------------

    private <E> List<List<String[]>> buildLayerList(List<E> opinions, boolean canBenNull) {
        int this_row_num = Config.getRow(opinions.size() + 1);//
        List<List<String[]>> buttonList = new ArrayList<>();
        for (int i = 0; i < opinions.size(); i += this_row_num) {
            List<String[]> row = new ArrayList<>();
            for (int j = i; j < i + this_row_num && j < opinions.size(); j++) {
                row.add(new String[] { opinions.get(j).toString(), String.valueOf(j + 1) });
            }
            buttonList.add(row);
        }
        List<String[]> lastRow = buttonList.get(buttonList.size() - 1);
        if (lastRow.size() < this_row_num) {
            if (canBenNull) {
                lastRow.add(new String[] { "取消(esc)", "ok" });
            } else {
                lastRow.add(new String[] { "确定(ok)", "ok" });
            }

        } else {
            List<String[]> newRow = new ArrayList<>();
            if (canBenNull) {
                newRow.add(new String[] { "取消(esc)", "ok" });
            } else {
                newRow.add(new String[] { "确定(ok)", "ok" });
            }
            buttonList.add(newRow);
        }

        // System.out.println(buttonList);
        return buttonList;
    }

    private String formatCallBackCode(long chatId, long msgId) {
        return chatId + "_" + msgId;
    }

    @Override
    public void sendAllPersonStaus(List<Person> players, CallbackEven personStatus) {
        if (players == null || players.size() == 0) {
            return;
        }
        List<List<String[]>> buttonList = new ArrayList<>();
        List<String[]> row = new ArrayList<>();
        String buttonValue = "updateStatus";
        row.add(new String[] { "更新状态", buttonValue });
        buttonList.add(row);

        for (Person p : players) {

            if (p.getUser() == null) {
                continue;
            }
            // 这个消息不计算出牌时间
            MsgObj inMsgObj = MsgObj.newMsgObj(p.getGameManager());
            inMsgObj.chatId = p.getUser().user_id;
            inMsgObj.text = personStatus.getPersonStatusExec(p);
            inMsgObj.setImg(p.toString());
            inMsgObj.replyMakup = buttonList;
            inMsgObj.isUpdateStatus = true;
            // inMsgObj.unlockAttrbutes(ReturnType.Msgid);
            // inMsgObj.resetAttributes(ReturnType.Msgid);
            sendImg(inMsgObj);
            p.setShowStatusMsg(inMsgObj);
            // sleep(500);
            threadPoolExecutor.submit(
                    new Runnable() {
                        @Override
                        public void run() {
                            try {
                                // System.out.println("准备阻塞获取 msgId "+inMsgObj );

                                long msgId = inMsgObj.getReturnMsgId(10);// 这个3秒好像有点短了
                                // 这里不处理空值，直接报错
                                // System.out.println("状态阻塞获取到了 msgId "+inMsgObj );
                                String callBackCode = formatCallBackCode(inMsgObj.chatId, msgId);

                                callBackMap.put(callBackCode, inMsgObj);
                                String returnValue;
                                while (inMsgObj.getLatchManager().isRunning() &&
                                        ((returnValue = inMsgObj.getString(ReturnType.UpdateStatus,
                                                99999999)) != null)) {

                                    inMsgObj.resetAttributes(ReturnType.UpdateStatus);
                                    if (!buttonValue.equals(returnValue)) {
                                        continue;
                                    }
                                    answerCallBack(inMsgObj, "", false);
                                    String result;
                                    if (p.getBlankPerson() != null) {
                                        result = personStatus.getPersonStatusExec(p.getBlankPerson());
                                    } else {
                                        result = personStatus.getPersonStatusExec(p);
                                    }

                                    if (result != null && !result.equals(inMsgObj.text)) {
                                        inMsgObj.text = result;
                                        editCaptionForce(inMsgObj);
                                    }
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    });

        }
    }

    @Override
    public void addUser(SanUser testUser) {
        this.joinGameCtrl.addUser(testUser);
    }

    public void removeUser(long user_id) {
        this.joinGameCtrl.removeUser(user_id);
    };

    public void removeUser(String user_id) {
        this.joinGameCtrl.removeUser(user_id);
    }

    public void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void endGame(long chatId) {
        joinGameCtrl.endGame(chatId);
    }

    // ----------------所有获取----------------------------------
    // ----------------所有获取----------------------------------
    // ----------------所有获取----------------------------------
    // ----------------所有获取----------------------------------

    @Override
    public <E> void votePublic(List<E> opinions, MsgObj publicMsgObj) {
        publicMsgObj.isVotePublic = true;
        publicMsgObj.callbackIds = new BlockingMap<>(publicMsgObj.getLatchManager());
        if (opinions == null || opinions.size() == 0) {
            return;
        }
        // publicMsgObj.unlockAttrbutes(ReturnType.Msgid);
        // publicMsgObj.resetAttributes(ReturnType.Msgid);
        List<Person> persons = publicMsgObj.forPlayers;
        // if (persons == null || persons.size() == 0) { //人为空也要装样子
        if (persons == null) {
            return;
        }

        if (publicMsgObj.forPlayersUserIdSet == null) {// 把userId单独存下来
            publicMsgObj.forPlayersUserIdSet = ConcurrentHashMap.newKeySet();
            for (Person p : persons) {
                if (p.getUser() != null) {
                    publicMsgObj.forPlayersUserIdSet.add(p.getUser().user_id);
                }
            }
        }

        int this_row_num = Config.getRow(opinions.size() + 1) + 1;//
        List<List<String[]>> buttonList = new ArrayList<>();
        for (int i = 0; i < opinions.size(); i += this_row_num) {
            List<String[]> row = new ArrayList<>();
            for (int j = i; j < i + this_row_num && j < opinions.size(); j++) {
                String[] pair = (String[]) opinions.get(j);
                row.add(new String[] { pair[0], pair[1] });// not deeplink
            }
            buttonList.add(row);
        }
        publicMsgObj.replyMakup = buttonList;
        if (publicMsgObj.localImgFileStream == null && publicMsgObj.localImgFileId == null) {
            sendMsg(publicMsgObj);
        } else {
            sendImg(publicMsgObj);
        }

        threadPoolExecutor.submit(
                new Runnable() {
                    @Override
                    public void run() {

                        try {
                            // long msgId = inMsgObj.getReturnMsgId(
                            // Config.PUBLIC_NOTICE_TOUCH_LIMIT_TIME / 1000);
                            long msgId = publicMsgObj.getReturnMsgId(10);

                            String callBackCode = formatCallBackCode(publicMsgObj.chatId, msgId);
                            callBackMap.put(callBackCode, publicMsgObj);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                });

    }

    @Override
    public <E> MsgObj chooseOnePublic(List<E> opinions, MsgObj publicMsgObj) {
        publicMsgObj.isChooseOneOpinionPublic = true;
        if (opinions == null || opinions.size() == 0) {
            publicMsgObj.setAttributes(ReturnType.ChooseOneOpinionPublic, "q");
            return publicMsgObj;
        }
        int this_row_num = Config.getRow(opinions.size() + 1) + 1;//
        List<List<String[]>> buttonList = new ArrayList<>();
        for (int i = 0; i < opinions.size(); i += this_row_num) {
            List<String[]> row = new ArrayList<>();
            for (int j = i; j < i + this_row_num && j < opinions.size(); j++) {
                String[] pair = (String[]) opinions.get(j);
                row.add(new String[] { pair[0], pair[1] });
            }
            buttonList.add(row);
        }

        List<String[]> lastRow = buttonList.get(buttonList.size() - 1);
        if (lastRow.size() < this_row_num) {
            lastRow.add(new String[] { "取消(Esc)", "q" });
        } else {
            List<String[]> newRow = new ArrayList<>();
            newRow.add(new String[] { "取消(Esc)", "q" });
            buttonList.add(newRow);
        }
        // System.out.println("buttonList="+buttonList);
        publicMsgObj.replyMakup = buttonList;
        if (publicMsgObj.localImgFileStream == null && publicMsgObj.localImgFileId == null) {
            sendMsg(publicMsgObj);
        } else {
            sendImg(publicMsgObj);
        }
        threadPoolExecutor.submit(
                new Runnable() {
                    @Override
                    public void run() {

                        try {
                            // long msgId = inMsgObj.getReturnMsgId(Config.PUBLIC_NOTICE_TOUCH_LIMIT_TIME /
                            // 1000);
                            long msgId = publicMsgObj.getReturnMsgId(10);
                            // 这里不处理空值，直接报错
                            // String deeplinkCode = inMsgObj.user_chatId + "_deeplink";
                            // callBackMap.put(deeplinkCode, inMsgObj);
                            String callBackCode = formatCallBackCode(publicMsgObj.chatId, msgId);
                            callBackMap.put(callBackCode, publicMsgObj);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                });
        return publicMsgObj;

    }

    @Override
    public <E> MsgObj chooseOneDeepLink(List<E> opinions, MsgObj inMsgObj) {
        inMsgObj.isDeeplink = true;
        if (opinions == null || opinions.size() == 0) {
            inMsgObj.setAttributes(ReturnType.Deeplink, "q");
            return inMsgObj;
        }
        // inMsgObj.unlockAttrbutes(ReturnType.Msgid);
        // inMsgObj.resetAttributes(ReturnType.Msgid);
        int this_row_num = Config.getRow(opinions.size() + 1) + 1;//
        List<List<String[]>> buttonList = new ArrayList<>();
        for (int i = 0; i < opinions.size(); i += this_row_num) {
            List<String[]> row = new ArrayList<>();
            for (int j = i; j < i + this_row_num && j < opinions.size(); j++) {
                String[] pair = (String[]) opinions.get(j);
                row.add(new String[] { pair[0], pair[1], "deeplink" });// deeplink
            }
            buttonList.add(row);
        }

        List<String[]> lastRow = buttonList.get(buttonList.size() - 1);
        if (lastRow.size() < this_row_num) {
            lastRow.add(new String[] { "取消(Esc)", "q" });
        } else {
            List<String[]> newRow = new ArrayList<>();
            newRow.add(new String[] { "取消(Esc)", "q" });
            buttonList.add(newRow);
        }
        // System.out.println("buttonList="+buttonList);
        inMsgObj.replyMakup = buttonList;
        if (inMsgObj.localImgFileStream == null && inMsgObj.localImgFileId == null) {
            sendMsg(inMsgObj);
        } else {
            sendImg(inMsgObj);
        }
        threadPoolExecutor.submit(
                new Runnable() {
                    @Override
                    public void run() {

                        try {
                            // long msgId = inMsgObj.getReturnMsgId(
                            // Config.PUBLIC_NOTICE_TOUCH_LIMIT_TIME / 1000);
                            long msgId = inMsgObj.getReturnMsgId(10);
                            // 这里不处理空值，直接报错
                            String deeplinkCode = inMsgObj.user_chatId + "_deeplink";
                            callBackMap.put(deeplinkCode, inMsgObj);
                            String callBackCode = formatCallBackCode(inMsgObj.chatId, msgId);
                            callBackMap.put(callBackCode, inMsgObj);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                });
        return inMsgObj;

    }

    @Override
    public <E> MsgObj chooseOneDeepLinkNoCallback(List<E> opinions, MsgObj inMsgObj) {
        // inMsgObj.isDeeplink = true;
        if (opinions == null || opinions.size() == 0) {
            inMsgObj.setAttributes(ReturnType.Deeplink, "q");
            return inMsgObj;
        }
        int this_row_num = Config.getRow(opinions.size() + 1) + 1;//
        List<List<String[]>> buttonList = new ArrayList<>();
        for (int i = 0; i < opinions.size(); i += this_row_num) {
            List<String[]> row = new ArrayList<>();
            for (int j = i; j < i + this_row_num && j < opinions.size(); j++) {
                String[] pair = (String[]) opinions.get(j);
                row.add(new String[] { pair[0], pair[1], "deeplink" });// deeplink
            }
            buttonList.add(row);
        }

        // System.out.println("buttonList="+buttonList);
        inMsgObj.replyMakup = buttonList;
        if (inMsgObj.localImgFileStream == null && inMsgObj.localImgFileId == null) {
            sendMsg(inMsgObj);
        } else {
            sendImg(inMsgObj);
        }

        return inMsgObj;

    }

    @Override
    public <E> MsgObj comfirmMultyDeepLink(List<E> opinions, MsgObj inMsgObj, String tag) {

        inMsgObj.isMultyDeeplink = true;
        // 这个是拿来收回调的
        inMsgObj.callbackIds = new BlockingMap<>(inMsgObj.getLatchManager());
        if (opinions == null || opinions.size() == 0) {
            inMsgObj.setAttributes(ReturnType.DeeplinkMulty, "q");
            return inMsgObj;
        }
        // inMsgObj.unlockAttrbutes(ReturnType.Msgid);
        // inMsgObj.resetAttributes(ReturnType.Msgid);
        List<Person> persons = inMsgObj.forPlayers;
        // if (persons == null || persons.size() == 0) { //人为空也要装样子
        if (persons == null) {
            return inMsgObj;
        }

        int this_row_num = Config.getRow(opinions.size() + 1) + 1;//
        List<List<String[]>> buttonList = new ArrayList<>();
        for (int i = 0; i < opinions.size(); i += this_row_num) {
            List<String[]> row = new ArrayList<>();
            for (int j = i; j < i + this_row_num && j < opinions.size(); j++) {
                String[] pair = (String[]) opinions.get(j);
                row.add(new String[] { pair[0], pair[1], "deeplink" });// deeplink
            }
            buttonList.add(row);
        }

        List<String[]> lastRow = buttonList.get(buttonList.size() - 1);
        if (lastRow.size() < this_row_num) {
            lastRow.add(new String[] { "取消(Esc)", "q" });
        } else {
            List<String[]> newRow = new ArrayList<>();
            newRow.add(new String[] { "取消(Esc)", "q" });
            buttonList.add(newRow);
        }
        // System.out.println("buttonList="+buttonList);
        inMsgObj.replyMakup = buttonList;
        if (inMsgObj.localImgFileStream == null && inMsgObj.localImgFileId == null) {
            sendMsg(inMsgObj);
        } else {
            sendImg(inMsgObj);
        }
        threadPoolExecutor.submit(
                new Runnable() {
                    @Override
                    public void run() {

                        try {
                            // long msgId = inMsgObj.getReturnMsgId(
                            // Config.PUBLIC_NOTICE_TOUCH_LIMIT_TIME / 1000);
                            // sleep(1000);//为什么不等待会取到之前的值
                            long msgId = inMsgObj.getReturnMsgId(10);

                            // System.out.println("comfirmMultyDeepLink msgid="+msgId);
                            // 这里不处理空值，直接报错
                            // 1 这里给deeplink
                            for (Person p : persons) {
                                String deeplinkCode = p.getUser().user_id + "_deeplink";
                                callBackMap.put(deeplinkCode, inMsgObj);

                            }
                            // 1 这里给取消按键
                            String callBackCode = formatCallBackCode(inMsgObj.chatId, msgId);
                            callBackMap.put(callBackCode, inMsgObj);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                });
        return inMsgObj;

    }

    @Override
    public <E> MsgObj chooseOneFromOpinion(List<E> opinions, final MsgObj inMsgObj) {
        return chooseOneFromOpinion(opinions, inMsgObj, "", "");
    }

    @Override
    public <E> MsgObj chooseOneFromOpinion(List<E> opinions, final MsgObj inMsgObj, String bottonFront,
            String bottonBehind) {
        if (opinions == null || opinions.size() == 0) {
            inMsgObj.setAttributes(ReturnType.ChooseOneCard, "q");
            return inMsgObj;
        }
        // inMsgObj.unlockAttrbutes(ReturnType.Msgid);
        // inMsgObj.resetAttributes(ReturnType.Msgid);
        inMsgObj.isChooseOneCard = true;
        int this_row_num = Config.getRow(opinions.size());//
        List<List<String[]>> buttonList = new ArrayList<>();
        for (int i = 0; i < opinions.size(); i += this_row_num) {
            List<String[]> row = new ArrayList<>();
            for (int j = i; j < i + this_row_num && j < opinions.size(); j++) {
                E item = opinions.get(j);
                if (item instanceof Person) {
                    Person p = (Person) item;

                    row.add(new String[] {
                            bottonFront + (p.getGamePlayerNo() > 0 ? Text.circleNum(p.getGamePlayerNo())
                                    : "") + p.toString() + bottonBehind,
                            String.valueOf(j + 1) });

                } else {
                    row.add(new String[] { bottonFront + opinions.get(j).toString() + bottonBehind,
                            String.valueOf(j + 1) });
                }
            }
            buttonList.add(row);
        }
        inMsgObj.replyMakup = buttonList;
        if (inMsgObj.localImgFileStream == null && inMsgObj.localImgFileId == null) {
            sendMsg(inMsgObj);
        } else {
            sendImg(inMsgObj);
        }

        threadPoolExecutor.submit(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // System.out.println("准备阻塞获取 msgId "+inMsgObj );

                            long msgId = inMsgObj.getReturnMsgId(10);
                            // 这里不处理空值，直接报错
                            // System.out.println("阻塞获取到了 msgId "+inMsgObj );
                            String callBackCode = formatCallBackCode(inMsgObj.chatId, msgId);

                            callBackMap.put(callBackCode, inMsgObj);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                });
        return inMsgObj;
    }

    @Override
    public <E> MsgObj chooseOneFromOpinionCanBeNull(List<E> opinions, MsgObj inMsgObj) {
        if (opinions == null || opinions.size() == 0) {
            inMsgObj.setAttributes(ReturnType.ChooseOneCard, "q");
            return inMsgObj;
        }
        // inMsgObj.unlockAttrbutes(ReturnType.Msgid);
        // inMsgObj.resetAttributes(ReturnType.Msgid);
        inMsgObj.isChooseOneCard = true;
        int this_row_num = Config.getRow(opinions.size() + 1);//
        List<List<String[]>> buttonList = new ArrayList<>();
        for (int i = 0; i < opinions.size(); i += this_row_num) {
            List<String[]> row = new ArrayList<>();
            for (int j = i; j < i + this_row_num && j < opinions.size(); j++) {
                E item = opinions.get(j);
                if (item instanceof Person) {
                    Person p = (Person) item;
                    row.add(new String[] {
                            (p.getGamePlayerNo() > 0 ? Text.circleNum(p.getGamePlayerNo())
                                    : "") + p.toString(),
                            String.valueOf(j + 1) });

                } else {
                    row.add(new String[] { item.toString(), String.valueOf(j + 1) });

                }
            }
            buttonList.add(row);
        }

        List<String[]> lastRow = buttonList.get(buttonList.size() - 1);
        if (lastRow.size() < this_row_num) {
            lastRow.add(new String[] { "取消(Esc)", "q" });
        } else {
            List<String[]> newRow = new ArrayList<>();
            newRow.add(new String[] { "取消(Esc)", "q" });
            buttonList.add(newRow);
        }
        // System.out.println("buttonList="+buttonList);
        inMsgObj.replyMakup = buttonList;
        if (inMsgObj.localImgFileStream == null && inMsgObj.localImgFileId == null) {
            sendMsg(inMsgObj);
        } else {
            sendImg(inMsgObj);
        }
        threadPoolExecutor.submit(
                new Runnable() {
                    @Override
                    public void run() {

                        try {
                            // System.out.println("inMsgObj.text="+inMsgObj.text);
                            long msgId = inMsgObj.getReturnMsgId(10);
                            // System.out.println(msgId+"---inMsgObj.text="+inMsgObj.text);
                            // 这里不处理空值，直接报错
                            String callBackCode = formatCallBackCode(inMsgObj.chatId, msgId);
                            callBackMap.put(callBackCode, inMsgObj);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                });
        return inMsgObj;

    }

    @Override
    public <E> MsgObj chooseSomeFromOpinion(List<E> opinions, int num, MsgObj inMsgObj) {
        return chooseSomeFromOpinion(opinions, num, false, inMsgObj);
    }

    @Override
    public <E> MsgObj chooseSomeFromOpinion(List<E> opinions, int num, boolean canBenNull, MsgObj inMsgObj) {
        if (!inMsgObj.isChooseMany) {
            inMsgObj.isChooseMany = true;
        }
        if (opinions == null || opinions.size() == 0) {
            inMsgObj.setAttributes(ReturnType.ChooseMany, "q");
            return inMsgObj;
        }
        List<List<String[]>> buttonList = buildLayerList(opinions, (num == 0 || canBenNull));

        // if (!compareButtons(buttonList, inMsgObj.replyMakup)) {// 这里要比较一下按钮如果一样就不发消息
        // inMsgObj.unlockAttrbutes(ReturnType.Msgid);
        // inMsgObj.resetAttributes(ReturnType.Msgid);
        inMsgObj.replyMakup = buttonList;
        if (inMsgObj.localImgFileStream == null && inMsgObj.localImgFileId == null) {
            sendMsg(inMsgObj);
        } else {
            sendImg(inMsgObj);
        }
        // }

        threadPoolExecutor.submit(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Set<String> selected = new HashSet<>();
                            long msgId;
                            if (inMsgObj.oldMsgId > 0) {
                                msgId = inMsgObj.oldMsgId;
                            } else {
                                msgId = inMsgObj.getReturnMsgId(10);
                            }

                            String callBackCode = formatCallBackCode(inMsgObj.chatId, msgId);
                            callBackMap.put(callBackCode, inMsgObj);
                            // 没有按下确认键
                            TimeLimit t = new TimeLimit(Config.PRIV_RND_TIME_60S);
                            while (inMsgObj.getLatchManager().isRunning()
                                    && (!("ok").equals(callBackMap.get(callBackCode)
                                            .getString(ReturnType.ChooseManyThisTime,
                                                    Config.PUBLIC_NOTICE_TIME_25S / 1000)))) {
                                if (t.isTimeout()) {
                                    return;
                                }
                                synchronized (inMsgObj) {

                                    String callBack = inMsgObj.getString(ReturnType.ChooseManyThisTime,
                                            Config.PUBLIC_NOTICE_TIME_25S / 1000);
                                    // 这里不处理空值，直接报错
                                    // 给按键打上勾
                                    int this_row_num = Config.getRow(opinions.size() + 1);
                                    int callBackNum = Integer.valueOf(callBack) - 1;
                                    // 计算商
                                    int quotient = callBackNum / this_row_num;
                                    // 计算余数
                                    int remainder = callBackNum % this_row_num;
                                    // System.err.println("callBackNum this_row_num 行 列"+callBackNum+"
                                    // "+this_row_num+" "+(quotient+1)+" "+(remainder+1));
                                    String[] pair = buttonList.get(quotient).get(remainder);
                                    if (selected.contains(callBack)) {
                                        selected.remove(callBack);
                                        pair[0] = pair[0].substring(1);// 去除 ✓
                                    } else {
                                        selected.add(callBack);
                                        pair[0] = "✓" + pair[0];
                                    }

                                    if (num == 0 || selected.size() <= num) {
                                        answerCallBack(inMsgObj, "", false);
                                    } else {
                                        answerCallBack(inMsgObj, "❌所选数量超过了限制", false);
                                        selected.remove(callBack);
                                        pair[0] = pair[0].substring(1);// 去除 ✓
                                        continue;
                                        // inMsgObj.resetAttributes(ReturnType.ChooseManyThisTime);
                                        // return;
                                    }
                                    // System.out.println(buttonList);
                                    List<String[]> lastRow = buttonList.get(buttonList.size() - 1);
                                    String[] lastPair = lastRow.get(lastRow.size() - 1);
                                    if ((num == 0 || canBenNull) && selected.size() == 0) {
                                        lastPair[0] = "取消(esc)";
                                    } else {
                                        if (num > 0 && num == selected.size()) {
                                            lastPair[0] = "✅确定(ok)";
                                        } else {
                                            lastPair[0] = "确定(ok)";
                                        }
                                    }
                                    inMsgObj.resetAttributes(ReturnType.ChooseManyThisTime);
                                    editButtons(inMsgObj);
                                }
                            }

                            // answerCallBack(inMsgObj, "", false);
                            inMsgObj.setAttributes(ReturnType.ChooseMany, new ArrayList<>(selected));
                        } catch (Exception e) {
                            // e.printStackTrace();
                        }

                    }

                });
        return inMsgObj;
    }

    @Override
    public <E> MsgObj renewSomeFromOpinion(List<E> opinions, int num, MsgObj inMsgObj) {
        if (opinions == null || opinions.size() == 0) {
            inMsgObj.setAttributes(ReturnType.ChooseMany, "q");
            return inMsgObj;
        }
        // renew不需要重置这个id，因为这个id没有变
        // inMsgObj.unlockAttrbutes(ReturnType.Msgid);
        // inMsgObj.resetAttributes(ReturnType.Msgid);
        inMsgObj.resetAttributes(ReturnType.ChooseManyThisTime);
        List<List<String[]>> buttonList = buildLayerList(opinions, num == 0);
        List<List<String[]>> oldOuttonList = inMsgObj.replyMakup;
        inMsgObj.replyMakup = buttonList;
        if (!compareButtons(buttonList, oldOuttonList)) {
            // 如果是不是一样的就更新消息
            editButtons(inMsgObj);
        }

        threadPoolExecutor.submit(
                new Runnable() {
                    @Override
                    public void run() {
                        try {

                            Set<String> selected = new HashSet<>();
                            long msgId;
                            if (inMsgObj.oldMsgId > 0) {
                                msgId = inMsgObj.oldMsgId;
                            } else {
                                msgId = inMsgObj.getReturnMsgId(10);
                            }
                            // 这里不处理空值，直接报错
                            String callBackCode = formatCallBackCode(inMsgObj.chatId, msgId);
                            callBackMap.put(callBackCode, inMsgObj);
                            // 没有按下确认键
                            TimeLimit t = new TimeLimit(Config.PRIV_RND_TIME_60S);
                            while (inMsgObj.getLatchManager().isRunning()
                                    && (!("ok").equals(callBackMap.get(callBackCode)
                                            .getString(ReturnType.ChooseManyThisTime,
                                                    Config.PUBLIC_NOTICE_TIME_25S / 1000)))) {

                                if (t.isTimeout()) {
                                    return;
                                }
                                synchronized (inMsgObj) {

                                    String callBack = inMsgObj.getString(ReturnType.ChooseManyThisTime,
                                            Config.PUBLIC_NOTICE_TIME_25S / 1000);
                                    if (callBack == null) {
                                        return;
                                    }
                                    // 这里不处理空值，直接报错
                                    // 给按键打上勾
                                    int this_row_num = Config.getRow(opinions.size() + 1);
                                    int callBackNum = Integer.valueOf(callBack) - 1;
                                    // 计算商
                                    int quotient = callBackNum / this_row_num;
                                    // 计算余数
                                    int remainder = callBackNum % this_row_num;
                                    String[] pair = buttonList.get(quotient).get(remainder);
                                    if (selected.contains(callBack)) {
                                        selected.remove(callBack);
                                        pair[0] = pair[0].substring(1);// 去除 ✓
                                    } else {
                                        selected.add(callBack);
                                        pair[0] = "✓" + pair[0];
                                    }

                                    if (num == 0 || selected.size() <= num) {
                                        answerCallBack(inMsgObj, "", false);
                                    } else {
                                        answerCallBack(inMsgObj, "❌所选数量超过了限制", false);
                                        selected.remove(callBack);
                                        pair[0] = pair[0].substring(1);// 去除 ✓
                                        continue;
                                    }
                                    // System.out.println(buttonList);
                                    List<String[]> lastRow = buttonList.get(buttonList.size() - 1);
                                    String[] lastPair = lastRow.get(lastRow.size() - 1);
                                    if (num == 0 && selected.size() == 0) {
                                        lastPair[0] = "取消(esc)";
                                    } else {
                                        if (num > 0 && num == selected.size()) {
                                            lastPair[0] = "✅确定(ok)";
                                        } else {
                                            lastPair[0] = "确定(ok)";
                                        }
                                    }
                                    inMsgObj.resetAttributes(ReturnType.ChooseManyThisTime);
                                    editButtons(inMsgObj);
                                }

                            }

                            // answerCallBack(inMsgObj, "", false);
                            inMsgObj.setAttributes(ReturnType.ChooseMany, new ArrayList<>(selected));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                });
        return inMsgObj;
    }

    @Override
    public <E> MsgObj chooseSomeFromOpinionByOrder(List<E> opinions, int num, MsgObj inMsgObj) {
        if (!inMsgObj.isChooseMany) {
            inMsgObj.isChooseMany = true;
        }
        if (opinions == null || opinions.size() == 0) {
            inMsgObj.setAttributes(ReturnType.ChooseMany, "q");
            return inMsgObj;
        }
        List<List<String[]>> buttonList = buildLayerList(opinions, num == 0);
        // inMsgObj.unlockAttrbutes(ReturnType.Msgid);
        // inMsgObj.resetAttributes(ReturnType.Msgid);
        inMsgObj.replyMakup = buttonList;
        if (inMsgObj.localImgFileStream == null && inMsgObj.localImgFileId == null) {
            sendMsg(inMsgObj);
        } else {
            sendImg(inMsgObj);
        }

        // CIRCLE_NUM
        threadPoolExecutor.submit(
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Set<String> selected = new LinkedHashSet<>();
                            long msgId;
                            if (inMsgObj.oldMsgId > 0) {
                                msgId = inMsgObj.oldMsgId;
                            } else {
                                msgId = inMsgObj.getReturnMsgId(10);
                            }
                            String callBackCode = formatCallBackCode(inMsgObj.chatId, msgId);
                            callBackMap.put(callBackCode, inMsgObj);
                            // 没有按下确认键
                            TimeLimit t = new TimeLimit(Config.PRIV_RND_TIME_60S);
                            String callBack;
                            while (inMsgObj.getLatchManager().isRunning() &&
                                    (!("ok").equals((callBack = callBackMap.get(callBackCode)
                                            .getString(ReturnType.ChooseManyThisTime,
                                                    Config.PUBLIC_NOTICE_TIME_25S / 1000))))) {

                                if (t.isTimeout()) {
                                    return;
                                }
                                synchronized (inMsgObj) {

                                    // 这里不处理空值，直接报错
                                    // 给全部去除序号
                                    for (String select : selected) {
                                        int callBackNum = Integer.valueOf(select) - 1;
                                        int this_row_num = Config.getRow(opinions.size() + 1);
                                        int quotient = callBackNum / this_row_num;
                                        int remainder = callBackNum % this_row_num;
                                        String[] pair = buttonList.get(quotient).get(remainder);
                                        pair[0] = pair[0].substring(1);// 去除 ✓
                                    }

                                    if (selected.contains(callBack)) {
                                        selected.remove(callBack);
                                    } else {
                                        selected.add(callBack);
                                    }
                                    if (num == 0 || selected.size() <= num) {
                                        answerCallBack(inMsgObj, "", false);
                                    } else {
                                        answerCallBack(inMsgObj, "❌所选数量超过了限制", false);
                                        selected.remove(callBack);
                                        continue;
                                    }
                                    List<String[]> lastRow = buttonList.get(buttonList.size() - 1);
                                    String[] lastPair = lastRow.get(lastRow.size() - 1);
                                    if (num == 0 && selected.size() == 0) {
                                        lastPair[0] = "取消(esc)";
                                    } else {
                                        if (num > 0 && num == selected.size()) {
                                            lastPair[0] = "✅确定(ok)";
                                        } else {
                                            lastPair[0] = "确定(ok)";
                                        }

                                    }
                                    System.out.println("chooseSomeFromOpinionByOrder selected=" + selected);

                                    int selectIdx = 0;
                                    // 重新排序
                                    for (String select : selected) {
                                        // 给全部去除序号
                                        int callBackNum = Integer.valueOf(select) - 1;
                                        int this_row_num = Config.getRow(opinions.size() + 1);
                                        int quotient = callBackNum / this_row_num;
                                        int remainder = callBackNum % this_row_num;
                                        String[] pair = buttonList.get(quotient).get(remainder);
                                        pair[0] = Text.CIRCLE_NUM[selectIdx] + pair[0];// 加上数字
                                        selectIdx++;
                                    }

                                    inMsgObj.resetAttributes(ReturnType.ChooseManyThisTime);
                                    editButtons(inMsgObj);
                                }

                            }

                            // answerCallBack(inMsgObj, "", false);
                            inMsgObj.setAttributes(ReturnType.ChooseMany, new ArrayList<>(selected));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                });
        return inMsgObj;
    }

    @Override
    public <E> MsgObj renewSomeFromOpinionByOrder(List<E> opinions, int num, MsgObj inMsgObj) {
        inMsgObj.resetAttributes(ReturnType.ChooseManyThisTime);
        List<List<String[]>> buttonList = buildLayerList(opinions, num == 0);
        List<List<String[]>> oldOuttonList = inMsgObj.replyMakup;
        // 更新按钮的不需要重置这个msgid，因为单更新按钮，msgid不会变
        // inMsgObj.unlockAttrbutes(ReturnType.Msgid);
        // inMsgObj.resetAttributes(ReturnType.Msgid);
        inMsgObj.replyMakup = buttonList;
        if (!compareButtons(buttonList, oldOuttonList)) {

            editButtons(inMsgObj);
        }
        // CIRCLE_NUM
        threadPoolExecutor.submit(
                new Runnable() {
                    @Override
                    public void run() {
                        try {

                            Set<String> selected = new LinkedHashSet<>();
                            long msgId;
                            if (inMsgObj.oldMsgId > 0) {
                                msgId = inMsgObj.oldMsgId;
                            } else {
                                msgId = inMsgObj.getReturnMsgId(10);
                            }
                            // 这里不处理空值，直接报错
                            String callBackCode = formatCallBackCode(inMsgObj.chatId, msgId);
                            callBackMap.put(callBackCode, inMsgObj);
                            // 没有按下确认键
                            TimeLimit t = new TimeLimit(Config.PRIV_RND_TIME_60S);
                            while (inMsgObj.getLatchManager().isRunning() &&
                                    (!("ok").equals(callBackMap.get(callBackCode)
                                            .getString(ReturnType.ChooseManyThisTime,
                                                    Config.PUBLIC_NOTICE_TIME_25S / 1000)))) {

                                if (t.isTimeout()) {
                                    return;
                                }
                                synchronized (inMsgObj) {

                                    String callBack = inMsgObj.getString(ReturnType.ChooseManyThisTime,
                                            Config.PUBLIC_NOTICE_TIME_25S / 1000);
                                    // 这里不处理空值，直接报错
                                    // 给全部去除序号
                                    for (String select : selected) {
                                        int callBackNum = Integer.valueOf(select) - 1;
                                        int this_row_num = Config.getRow(opinions.size() + 1);
                                        int quotient = callBackNum / this_row_num;
                                        int remainder = callBackNum % this_row_num;
                                        String[] pair = buttonList.get(quotient).get(remainder);
                                        pair[0] = pair[0].substring(1);// 去除 ✓
                                    }

                                    if (selected.contains(callBack)) {
                                        selected.remove(callBack);
                                    } else {
                                        selected.add(callBack);
                                    }

                                    if (num == 0 || selected.size() <= num) {
                                        answerCallBack(inMsgObj, "", false);
                                    } else {
                                        answerCallBack(inMsgObj, "❌所选数量超过了限制", false);
                                        selected.remove(callBack);
                                        continue;
                                    }

                                    List<String[]> lastRow = buttonList.get(buttonList.size() - 1);
                                    String[] lastPair = lastRow.get(lastRow.size() - 1);
                                    if (num == 0 && selected.size() == 0) {
                                        lastPair[0] = "取消(esc)";
                                    } else {
                                        if (num > 0 && num == selected.size()) {
                                            lastPair[0] = "✅确定(ok)";
                                        } else {
                                            lastPair[0] = "确定(ok)";
                                        }
                                    }

                                    System.out.println("renewSomeFromOpinionByOrder selected=" + selected);

                                    int selectIdx = 0;

                                    // 重新排序
                                    for (String select : selected) {
                                        // 给全部去除序号
                                        int callBackNum = Integer.valueOf(select) - 1;
                                        int this_row_num = Config.getRow(opinions.size() + 1);
                                        int quotient = callBackNum / this_row_num;
                                        int remainder = callBackNum % this_row_num;
                                        String[] pair = buttonList.get(quotient).get(remainder);
                                        pair[0] = Text.CIRCLE_NUM[selectIdx] + pair[0];// 加上数字
                                        selectIdx++;
                                    }

                                    inMsgObj.resetAttributes(ReturnType.ChooseManyThisTime);
                                    editButtons(inMsgObj);
                                }
                            }

                            // answerCallBack(inMsgObj, "", false);
                            inMsgObj.setAttributes(ReturnType.ChooseMany, new ArrayList<>(selected));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                });
        return inMsgObj;
    }

    public boolean noticeAndAskPublic(Person p, String noticeText, String buttonText, String buttonValue) {
        if (p.getUser() == null) {
            return false;
        }
        // 这里跳过，如果是中了什么乐不思蜀 兵粮寸断得重新判定一下是否丢牌
        MsgObj publicMsg = MsgObj.newMsgObj(p.getGameManager());
        ImgDB.setImg(publicMsg, p.toString());
        publicMsg.text = noticeText;
        publicMsg.chatId = p.getGameManager().getChatId();
        publicMsg.user_chatId = p.getUser().user_id;
        p.putTempActionMsgObj(buttonValue, publicMsg);
        List<String[]> button = new ArrayList<>();

        button.add(new String[] { "👉" + buttonText, buttonValue });
        chooseOneDeepLink(button, publicMsg);
        String resultText = publicMsg.getString(ReturnType.Deeplink, Config.PUBLIC_NOTICE_TIME_25S / 1000);
        // System.out.println("resultText="+resultText);
        // System.out.println("tag="+tag);
        // System.out.println("tag.equals(resultText)="+tag.equals(resultText));
        if (buttonValue.equals(resultText)) {
            // System.out.println("tag.equals(resultText)=" +
            // buttonValue.equals(resultText));
            delMsg(publicMsg);
            return true;
        }
        delMsg(publicMsg);
        return false;
    }

    public boolean noticeAndAskPublic(Card c, Person p, String noticeText, String buttonText, String buttonValue) {
        if (p.getUser() == null) {
            return false;
        }
        // 这里跳过，如果是中了什么乐不思蜀 兵粮寸断得重新判定一下是否丢牌
        MsgObj publicMsg = MsgObj.newMsgObj(p.getGameManager());
        ImgDB.setImg(publicMsg, c.toString());
        publicMsg.text = noticeText;
        publicMsg.chatId = p.getGameManager().getChatId();
        publicMsg.user_chatId = p.getUser().user_id;
        p.putTempActionMsgObj(buttonValue, publicMsg);
        List<String[]> button = new ArrayList<>();

        button.add(new String[] { "👉" + buttonText, buttonValue });
        chooseOneDeepLink(button, publicMsg);
        String resultText = publicMsg.getString(ReturnType.Deeplink, Config.PUBLIC_NOTICE_TIME_25S / 1000);
        // System.out.println("resultText="+resultText);
        if (buttonValue.equals(resultText)) {
            delMsg(publicMsg);
            return true;
        }
        delMsg(publicMsg);
        return false;
    }

    @Override
    public boolean noticeAndAskPublic(MsgObj publicMsg, Person p, String noticeText, String buttonText,
            String buttonValue, boolean showImg) {
        if (p.getUser() == null) {
            return false;
        }
        if (showImg) {
            ImgDB.setImg(publicMsg, p.toString());
        }
        publicMsg.text = noticeText;
        publicMsg.chatId = p.getGameManager().getChatId();
        publicMsg.user_chatId = p.getUser().user_id;
        p.putTempActionMsgObj(buttonValue, publicMsg);
        List<String[]> button = new ArrayList<>();

        button.add(new String[] { "👉" + buttonText, buttonValue });
        chooseOneDeepLink(button, publicMsg);
        String resultText = publicMsg.getString(ReturnType.Deeplink, Config.PUBLIC_NOTICE_TIME_25S / 1000);
        if (buttonValue.equals(resultText)) {
            // System.out.println("tag.equals(resultText)=" +
            // buttonValue.equals(resultText));
            // delMsg(publicMsg);
            return true;
        }
        // delMsg(publicMsg);
        return false;
    }

    @Override
    public boolean noticeAndAskPublic(MsgObj publicMsg, Card c, Person p, String noticeText, String buttonText,
            String buttonValue) {
        if (p.getUser() == null) {
            return false;
        }
        // 这里跳过，如果是中了什么乐不思蜀 兵粮寸断得重新判定一下是否丢牌

        ImgDB.setImg(publicMsg, c.toString());
        publicMsg.text = noticeText;
        publicMsg.chatId = p.getGameManager().getChatId();
        publicMsg.user_chatId = p.getUser().user_id;
        p.putTempActionMsgObj(buttonValue, publicMsg);
        List<String[]> button = new ArrayList<>();

        button.add(new String[] { "👉" + buttonText, buttonValue });
        chooseOneDeepLink(button, publicMsg);
        String resultText = publicMsg.getString(ReturnType.Deeplink, Config.PUBLIC_NOTICE_TIME_25S / 1000);
        // System.out.println("resultText="+resultText);
        if (buttonValue.equals(resultText)) {

            return true;
        }

        return false;
    }

    public boolean noticeAndAskPublicNoCallBack(MsgObj publicMsg, String buttonText,
            String buttonValue) {
        List<String[]> button = new ArrayList<>();
        button.add(new String[] { "👉" + buttonText, buttonValue });
        chooseOneDeepLinkNoCallback(button, publicMsg);
        // delMsg(publicMsg);
        return true;
    }

    /**
     * 询问众人并选出一个,可为空
     * 
     * @param publicMsg
     * @param persons
     * @param noticeText
     * @param buttonText
     * @param buttonValue
     * @return
     */
    public void noticeAndAskOneForMulty(MsgObj publicMsg, String buttonText, String buttonValue) {
        List<Person> persons = publicMsg.forPlayers;
        // if (persons == null || persons.size() == 0) {//0也要发出消息装样子
        if (persons == null) {
            return;
        }

        if (publicMsg.forPlayersUserIdSet == null) {// 把userId单独存下来
            publicMsg.forPlayersUserIdSet = ConcurrentHashMap.newKeySet();
            for (Person p : persons) {
                if (p.getUser() != null) {
                    publicMsg.forPlayersUserIdSet.add(p.getUser().user_id);
                }
            }
        }
        List<String[]> button = new ArrayList<>();
        button.add(new String[] { "👉" + buttonText, buttonValue });
        comfirmMultyDeepLink(button, publicMsg, buttonValue);

    }
}