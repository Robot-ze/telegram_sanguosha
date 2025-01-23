package sanguosha.cards.strategy;

import java.util.ArrayList;
import java.util.List;

import config.Text;
import msg.MsgObj;
import sanguosha.cards.Color;
import sanguosha.cards.Strategy;
import sanguosha.manager.GameManager;
import sanguosha.people.AI;
import sanguosha.people.Person;

public class TaoYuanJieYi extends Strategy {
    public TaoYuanJieYi(GameManager gameManager, Color color, int number) {
        super(gameManager, color, number);
        globlStrategy = true;
    }

    @Override
    public Object use() {
        //int userNo = 0;
        boolean showImg=true;
        // 存个缓存避免有人挂了改变了结构
        List<Person> list = new ArrayList<>(getGameManager().getPlayersBeginFromPlayer(getSource()));
        for (Person p : list) {
            if (p.getHP() == p.getMaxHP()) {
                continue;
            }
            try {
                //boolean showImg = (userNo == 0); // 为什么是1，因为0是自己，跳过了
                // String img = showImg ? toString() : null;
                if (!gotWuXie(p, showImg)) {
                    if(showImg){//只在第一次展示图片
                        showImg=false; 
                    }
                    // MsgObj publicMsg = p.getTempActionMsgObj("wuxie");
                    MsgObj publicMsg = MsgObj.newMsgObj(getGameManager());

                    publicMsg.chatId = getGameManager().getChatId();
                    p.recover(publicMsg, 1);

                    String result = Text.format("%s +1体力:%s",
                            p.getPlateName(), p.getHPEmoji());
                    // 这个是判断过无懈的消息

                    // if (p != getSource()) {
                    // getGameManager().getMsgIO().delMsg(msgIn);
                    // }

                    // MsgObj msgOut = p.getTempActionMsgObj("wuxie");
                    publicMsg.text = result;
                    sleep(1000);
                    getGameManager().getMsgAPI().sendImg(publicMsg);
                    sleep(1000);
                    if (p.isAI()) {
                        sleep(3000);
                    }

                }
            } finally {
                //userNo++;
                // sleep(1000);
            }
        }

        return true;
    }

    @Override
    public String toString() {
        return "桃园结义";
    }

    @Override
    public String details() {
        return "出牌阶段，对所有角色使用。每名目标角色回复1点体力。";
    }
}
