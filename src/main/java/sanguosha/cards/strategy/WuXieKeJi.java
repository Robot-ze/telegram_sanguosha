package sanguosha.cards.strategy;

import sanguosha.cards.Color;
import sanguosha.cards.Strategy;
import sanguosha.manager.GameManager;

public class WuXieKeJi extends Strategy {
    public WuXieKeJi(GameManager gameManager,Color color, int number) {
        super(  gameManager,color, number);
    }

    @Override
    public Object use() {
        return true;
    }

    @Override
    public String toString() {
        return "无懈可击";
    }

    @Override
    public String details() {
        return "一张锦囊牌生效前，抵消此牌对一名角色产生的效果，或抵消另一张[无懈可击]产生的效果。";
    }
}
