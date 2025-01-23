package sanguosha.cards.strategy;

import sanguosha.cards.Card;
import sanguosha.cards.Color;
import sanguosha.cards.Strategy;
import sanguosha.manager.GameManager;
import sanguosha.manager.Utils;
import sanguosha.people.AI;
import sanguosha.people.Person;

import java.util.ArrayList;
import java.util.List;

import config.Text;
import msg.MsgObj;

public class WuGuFengDeng extends Strategy {
    public WuGuFengDeng(GameManager gameManager, Color color, int number) {
        super(gameManager, color, number);
        globlStrategy = true;
    }

    @Override
    public Object use() {
        ArrayList<Card> cards = getGameManager().getCardsHeap().draw(getGameManager().getNumPlayers());
        int userNo = 0;
        //存个缓存避免有人挂了改变了结构
        List<Person> list = new ArrayList<>(getGameManager().getPlayersBeginFromPlayer(getSource()));

        for (Person p : list) {
            try {
                boolean showImg = (userNo == 0); // 为什么是1，因为0是自己，跳过了
                // String img = showImg ? toString() : null;

                if (!gotWuXie(p, showImg)) {
                    Card c;

                    MsgObj publicMsgObj = MsgObj.newMsgObj(getGameManager());
                    if (cards.size() == 1) {
                        c = cards.get(0);
                        publicMsgObj.text = Text.format("%s 你获得最后一张牌",
                                p.getHtmlName());
                        publicMsgObj.chatId = getGameManager().getChatId();
                        getGameManager().getMsgAPI().sendMsg(publicMsgObj);
                    } else {
                        if (p .isAI()) {
                            c = cards.get(Utils.randint(0, cards.size() - 1));
                            publicMsgObj.text = Text.format("%s 你可选一张牌",
                                    p.getHtmlName());
                            publicMsgObj.chatId = getGameManager().getChatId();
                            getGameManager().getMsgAPI().sendMsg(publicMsgObj);
                            sleep(3000);
                        } else {

                            publicMsgObj.text = Text.format("%s 你可一张牌",
                                    p.getHtmlName());
                            publicMsgObj.chatId = getGameManager().getChatId();
                            publicMsgObj.user_chatId = p.getUser().user_id;
                            publicMsgObj.isChooseOneCardPublic = true;
                            // ImgDB.setImg(publicMsgObj, img);

                            // p.getPriviMsg().clearHeader2();
                            // p.getPriviMsg().setOneTimeInfo1(Text.format("\n💬%s 使用了 五谷丰登
                            // 你可获得这些牌中（剩余的）的任意一张", getSource()));
                            c = p.chooseCardPublic(cards, false, publicMsgObj);
                            //System.out.println("c===="+c);
                            // getGameManager().getMsgAPI().clearButtons(publicMsgObj);

                        }

                    }
                    p.addCard(c);
                    cards.remove(c);
                    // sleep(3000);
                    // MsgObj wuxie = p.getTempActionMsgObj("wuxie");
                    String result = Text.format(",%s", c.getHtmlNameWithColor());
                    publicMsgObj.appendText(result);
                    //sleep(1000);
                    publicMsgObj.replyMakup = null;
                    getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
                    // sleep(3000);
                }
            } finally {
                userNo++;
                //sleep(1000);
            }

        }
        if (!cards.isEmpty()) {
            getGameManager().getCardsHeap().discard(cards);
        }
        return true;
    }

    @Override
    public String toString() {
        return "五谷丰登";
    }

    @Override
    public String details() {
        return "出牌阶段，对所有角色使用。" +
                "（选择目标后）你从牌堆顶亮出等同于角色数量的牌，每名目标角色获得这些牌中（剩余的）的任意一张。";
    }
}
