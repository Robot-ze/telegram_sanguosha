package sanguosha.cards.equipments.shields;

import config.Text;
import msg.CallbackEven;
import sanguosha.cards.Card;
import sanguosha.cards.Color;
import sanguosha.cards.equipments.Shield;
import sanguosha.cardsheap.CardsHeap;
import sanguosha.manager.GameManager;

public class BaGuaZhen extends Shield {
    public BaGuaZhen(GameManager gameManager, Color color, int number) {
        super(gameManager, color, number);
    }

    @Override
    public Object use() {
        Card c = getGameManager().getCardsHeap().judge(getSource(), new CallbackEven() {
            @Override
            public boolean juge(Card card) {
                if (card.isRed()) {
                    return true;
                } else {
                    return false;
                }
            }
        });

        if (c.isRed()) {
            getGameManager().getIo().printlnPublic(
                    Text.format("%s 装备%s,判定牌 %s 为红色,视为出闪",
                            getOwner().getPlateName(),
                            getHtmlName(),
                            c.getHtmlNameWithColor()),
                    toString());
            String res = Text.format("你装备%s,判定牌 %s 为红色,视为出闪",
                    getHtmlName(),
                    c.getHtmlNameWithColor());
            getGameManager().getIo().delaySendAndDelete(getOwner(), res);
        } else {
            String res = Text.format("判定牌 %s 为黑色,%s无效",
                    c.getHtmlNameWithColor(), getHtmlName());
            getGameManager().getIo().delaySendAndDelete(getOwner(), res);
        }

        return c.isRed();
    }

    @Override
    public String toString() {
        return "八卦阵";
    }

    @Override
    public String details() {
        return "每当你需要使用或出[闪]时，你可以进行一次判定，若判定结果为红色，视为你使用或打出了一张[闪]。";
    }

    
}
