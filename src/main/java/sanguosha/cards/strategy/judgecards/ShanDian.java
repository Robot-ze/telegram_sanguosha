package sanguosha.cards.strategy.judgecards;

import sanguosha.cards.Card;
import sanguosha.cards.Color;
import sanguosha.cards.JudgeCard;
import sanguosha.cards.basic.HurtType;

import sanguosha.manager.GameManager;
import sanguosha.manager.Utils;
import sanguosha.people.Person;

import java.util.ArrayList;

import config.Text;
import msg.CallbackEven;
import msg.MsgObj;

public class ShanDian extends JudgeCard {
    public ShanDian(GameManager gameManager, Color color, int number) {
        super(gameManager, color, number, 100);
    }

    @Override
    public String use() {
        if (!gotWuXie(getTarget())) {

            Card judge = getGameManager().getCardsHeap().judge(getTarget(), new CallbackEven() {
                @Override
                public boolean juge(Card card) {
                    if (card.color() == Color.SPADE && card.number() >= 2 && card.number() <= 9) {
                        return true;
                    } else {
                        return false;
                    }
                }
            });
            String result;
            MsgObj msg = getTarget().getTempActionMsgObjFirstOrder("changeJudge", "wuxie");
            if (judge.color() == Color.SPADE && judge.number() >= 2 && judge.number() <= 9) {
                boolean isPreLink = getTarget().isLinked();// 为什么要这样缓存，因为已伤害了这个值就会变
                int realNum = getTarget().hurt(getReplaceCards(), getSource(), 3, HurtType.thunder);
                result = Text.format(",判定牌为 %s ,闪电造成3点⚡️伤害%s",
                        judge.getHtmlNameWithColor(), getTarget().getHPEmoji());
                msg.text = msg.text + result;
                if (isPreLink) {
                    getGameManager().linkHurt(msg, getReplaceCards(), getSource(), getTarget(), realNum,
                            HurtType.thunder);
                    getGameManager().getMsgAPI().editCaptionForce(msg);
                    // sleep(3000);
                }

            } else {
                // getTarget().removeJudgeCard(getThisCard().get(0));
                // int numPlayers = getGameManager().getNumPlayers();
                // int index = getGameManager().getPlayers().indexOf(getTarget());
                int index = getGameManager().getPlayers().indexOf(getTarget()); // getTarget().getPos();
                Utils.assertTrue(index != -1, "shandian target not found");
                do {
                    index = (index + 1 == getGameManager().getNumPlayers()) ? 0 : index + 1;
                } while (getGameManager().isRunning()
                        && !getGameManager().getPlayers().get(index).addJudgeCard(this));

                result = Text.format(",判定牌为 %s ,躲过了闪电",
                        judge.getHtmlNameWithColor());

                setTarget(getGameManager().getPlayers().get(index));
                this.setTaken(true);

                msg.text = msg.text + result;
            }
            // 这个是判断过无懈的消息
            // System.out.println("msg.text="+msg.text);

            getGameManager().getMsgAPI().editCaptionForce(msg);
        }
        return null;
    }

    @Override
    public boolean addJudgeCard(Person p) {
        //use();
        boolean done = super.addJudgeCard(p);
        if (done) {
            // 不显示图
            // sleep(1000);
            getGameManager().getIo().printlnPublic(
                    Text.format(
                            "%s 已厌倦众生争斗,对自己用 %s",
                            p.getPlateName(),
                            getHtmlNameWithColor()));
        }

        return done;
    }

    @Override
    public String toString() {
        return "闪电";
    }

    @Override
    public String details() {
        return "出牌阶段，对你（你是第一个目标，之后可能会不断改变）使用。" +
                "若判定结果为黑桃♠️2~9，则目标角色受到3点雷电伤害。若判定不为黑桃♠️2~9，将之移动到其下家的判定区里。";
    }
}
