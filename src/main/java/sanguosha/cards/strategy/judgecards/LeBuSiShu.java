package sanguosha.cards.strategy.judgecards;

import config.Text;
import msg.CallbackEven;
import msg.MsgObj;
import sanguosha.cards.Card;
import sanguosha.cards.Color;
import sanguosha.cards.JudgeCard;

import sanguosha.manager.GameManager;
import sanguosha.people.Person;

public class LeBuSiShu extends JudgeCard {
    public LeBuSiShu(GameManager gameManager, Color color, int number) {
        super(gameManager, color, number);
    }

    @Override
    public String use() {
        if (!gotWuXie(getTarget())) {

            Card c = getGameManager().getCardsHeap().judge(getTarget(), new CallbackEven() {
                @Override
                public boolean juge(Card card) {
                    if (card.color() != Color.HEART) {
                        return true;
                    } else {
                        return false;
                    }
                }
            });
            String result;
            if (c.color() != Color.HEART) {
                result = Text.format(
                        ",判定牌为 %s,%s 本回合不能出牌",
                        Card.getColorEmoji(c.color()),
                        getTarget().getHtmlName());

                getTarget().getPriviMsg()
                        .setOneTimeInfo1(Text.format(",💬乐不思蜀 判定结果不为红桃♥️，你本回合不能出牌", toString(), details()));
                // 这个是判断过无懈的消息
                MsgObj msg = getTarget().getTempActionMsgObj("wuxie");
                msg.text = msg.text + result;
                getGameManager().getMsgAPI().editCaptionForce(msg);
                return "skip use"; // 这个skip use不能动，是用来给其他地方标记的
            } else {
                result = Text.format(
                        ",判定牌为 %s,%s 无效",
                        c.getHtmlNameWithColor(),
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
                            getHtmlNameWithColor())

                     );
            //sleep(3000);
        }

        return done;
    }

    @Override
    public String toString() {
        return "乐不思蜀";
    }

    @Override
    public boolean needChooseTarget() {
        return true;
    }

    @Override
    public boolean asktargetAddition(Person user, Person p) {
        if (p.hasQianXun()) {
            //getSource().printlnToIOPriv("不能对有 谦逊 技能的角色使用");
            return false;
        }
        return true;
    }

    @Override
    public String details() {
        return "出牌阶段，对一名其他角色使用。若判定结果不为红桃，跳过其出牌阶段。";
    }
}
