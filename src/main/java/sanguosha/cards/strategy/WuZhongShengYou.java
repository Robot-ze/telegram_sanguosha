package sanguosha.cards.strategy;

import config.Text;
import msg.MsgObj;
import sanguosha.cards.Color;
import sanguosha.cards.Strategy;
import sanguosha.manager.GameManager;

public class WuZhongShengYou extends Strategy {

    public WuZhongShengYou(GameManager gameManager, Color color, int number) {
        super(gameManager, color, number);
    }

    @Override
    public Object use() {
        if (!gotWuXie(getTarget())) {
            getTarget().drawCards(2);

            MsgObj wuxie = getTarget().getTempActionMsgObj("wuxie");
            String result = Text.format(",抽取2张卡牌");
            wuxie.text = wuxie.text + result;
            // sleep(1000);
            getGameManager().getMsgAPI().editCaptionForce (wuxie);
            // sleep(3000);
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "无中生有";
    }

    @Override
    public String details() {
        return "出牌阶段，对你使用。你摸两张牌。";
    }
}
