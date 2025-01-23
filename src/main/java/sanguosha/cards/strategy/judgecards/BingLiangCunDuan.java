package sanguosha.cards.strategy.judgecards;

import config.Text;
import msg.CallbackEven;
import msg.MsgObj;
import sanguosha.cards.Card;
import sanguosha.cards.Color;
import sanguosha.cards.JudgeCard;

import sanguosha.manager.GameManager;
import sanguosha.people.Person;

public class BingLiangCunDuan extends JudgeCard {
    public BingLiangCunDuan(GameManager gameManager, Color color, int number) {
        super(gameManager, color, number, 1);
    }

    @Override
    public String use() {
        if (!gotWuXie(getTarget())) {
            Card c = getGameManager().getCardsHeap().judge(getTarget(), new CallbackEven() {
                @Override
                public boolean juge(Card card) {

                    if (card.color() != Color.CLUB) {
                        return true;
                    } else {
                        return false;
                    }
                }

            });
            String result;
            if (c.color() != Color.CLUB) {

                result = Text.format(
                        ",判定牌为 %s ,%s 本回合不能摸牌",
                        Card.getColorEmoji(c.color()),
                        getTarget().getHtmlName());

                getTarget().getPriviMsg()
                        .setOneTimeInfo1(Text.format(",💬兵粮寸断 判定结果不为梅花♣️，你本回合不能摸牌", toString(), details()));
                // 这个是判断过无懈的消息
                MsgObj msg = getTarget().getTempActionMsgObjFirstOrder("changeJudge","wuxie");
                msg.text = msg.text + result;
                getGameManager().getMsgAPI().editCaptionForce (msg);
                return "skip draw";// 这个skip draw不能动，是给其他地方的标记，不要翻译他
            } else {
                result = Text.format(
                        ",判定牌为 %s,%s 无效",
                        Card.getColorEmoji(c.color()),
                        getHtmlName());
                // 这个是判断过无懈的消息
                MsgObj msg = getTarget().getTempActionMsgObjFirstOrder("changeJudge","wuxie");
                msg.text = msg.text + result;
                getGameManager().getMsgAPI().editCaptionForce(msg);
            }

        }
        return null;
    }

    @Override
    public boolean addJudgeCard(Person p) {
        boolean done = super.addJudgeCard(p);
        if (done) {
            //不显示图
            //sleep(1000);
            getGameManager().getIo().printlnPublic(
                    Text.format(
                            "%s 对 %s 使用 %s",
                            getSource().getPlateName(),
                            p.getHtmlName(),
                            getHtmlNameWithColor()) );
        }

        return done;
    }

    @Override
    public String toString() {
        return "兵粮寸断";
    }

    @Override
    public boolean needChooseTarget() {
        return true;
    }

    @Override
    public int getDistance() {
        if (getSource().hasDuanLiang()) {
            return 2;
        }
        return super.getDistance();
    }

    @Override
    public String details() {
        return "出牌阶段，对对距离为1的其他角色使用。若判定的结果不为梅花，跳过其摸牌阶段。";
    }
}
