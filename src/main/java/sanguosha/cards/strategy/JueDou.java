package sanguosha.cards.strategy;

import config.Config;
import config.Text;
import msg.MsgObj;
import sanguosha.cards.Card;
import sanguosha.cards.Color;
import sanguosha.cards.Strategy;
import sanguosha.manager.GameManager;
import sanguosha.people.Person;
import components.TimeLimit;

public class JueDou extends Strategy {
    public JueDou(GameManager gameManager, Color color, int number) {
        super(gameManager, color, number);
    }

    @Override
    public Object use() {
        MsgObj lastMsg;
        if (!gotWuXie(getTarget())) {

            getSource().jueDouBegin();
            getTarget().jueDouBegin();
            // String result = Text.format("\n\n%s 与 %s 的决斗开始",
            //         getSource().getPlateName(), getTarget().getPlateName());

            // 这个是判断过无懈的消息
            // MsgObj msg = lastMsg = getTarget().getTempActionMsgObj("wuxie");
            // msg.text = msg.text + result;
            // getGameManager().getMsgAPI().editCaption(msg);
            // sleep(3000);
            if (getSource().hasWuShuang()) {
                getSource().wushuangSha();
            }
            TimeLimit t = new TimeLimit(Config.WUXIE_WAIT_TIME_ROOP_60S);
            Card lastSha = null;//算伤害来源，算最后的的那张杀，没有杀才算这张格斗
            String result ;
            while (getGameManager().isRunning() && t.isNotTimeout()) {
                Card tempSha = null;
                // 吕布的格斗要两次杀才行
                if ((!getSource().hasWuShuang() && (tempSha = getTarget().requestSha(getSource(),false)) == null) ||
                        getSource().hasWuShuang() && ((tempSha = getTarget().requestSha(getSource(),false)) == null ||
                                (tempSha = getTarget().requestSha(getSource(),false)) == null)) {

                    lastMsg = getTarget().getTempActionMsgObj("sha");
                    int realHurt=getTarget().hurt(
                            lastSha == null ? getReplaceCards() : lastSha.getReplaceCards(),
                            lastSha == null ? getSource() : lastSha.getOwner(), 1);
                    result = Text.format(",败阵,受%s点伤害%s",
                           realHurt, getTarget().getHPEmoji());
                    lastMsg.text = lastMsg.text + result;
                    getGameManager().getMsgAPI().editCaptionForce(lastMsg);
                    return true;
                }
                if (tempSha != null) {
                    lastSha = tempSha;
                }

                if ((!getTarget().hasWuShuang() && (tempSha = getSource().requestSha(getTarget(),false)) == null) ||
                        getTarget().hasWuShuang() && ((tempSha = getSource().requestSha(getTarget(),false)) == null ||
                                (tempSha = getSource().requestSha(getTarget(),false)) == null)) {
                    lastMsg = getSource().getTempActionMsgObj("sha");
                    int realHurt=getSource().hurt(
                            lastSha == null ? getReplaceCards() : lastSha.getReplaceCards(),
                            lastSha == null ? getSource() : lastSha.getOwner(), 1);
                    result = Text.format(",败阵,受%s点伤害%s",
                           realHurt,getSource().getHPEmoji());
                    lastMsg.text = lastMsg.text + result;
                    getGameManager().getMsgAPI().editCaptionForce(lastMsg);
                    return true;
                }
                if (tempSha != null) {
                    lastSha = tempSha;
                }

            }
            return false;
        }
        return false;
    }
    @Override
    public boolean askTarget(Person user) {
        user.getPriviMsg().setOneTimeInfo1("\n💬请选择一名角色,由其开始，其与你轮流出[杀]，直到其中一方未打出[杀]为止。");
        return super.askTarget(user);
    }

    @Override
    public String toString() {
        return "决斗";
    }

    @Override
    public boolean needChooseTarget() {
        return true;
    }

    @Override
    public String details() {
        return "出牌阶段，对一名其他角色使用。由其开始，其与你轮流出[杀]，直到其中一方未打出[杀]为止。" +
                "未打出[杀]的一方受到另一方对其造成的1点伤害。";
    }
}
