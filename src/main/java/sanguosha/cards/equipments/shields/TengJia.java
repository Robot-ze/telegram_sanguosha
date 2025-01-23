package sanguosha.cards.equipments.shields;

import sanguosha.cards.Color;
import sanguosha.cards.equipments.Shield;
import sanguosha.manager.GameManager;

public class TengJia extends Shield {
    public TengJia(GameManager gameManager,Color color, int number) {
        super(  gameManager,color, number);
    }

    @Override
    public Object use() {
        return null;
    }

    @Override
    public String toString() {
        return "藤甲";
    }

    @Override
    public String details() {
        return "锁定技，[南蛮入侵]、[万箭齐发]和普通[杀]对你无效；锁定技，每当你受到火焰伤害时，此伤害+1。";
    }
}
