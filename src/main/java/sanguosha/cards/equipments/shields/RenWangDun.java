package sanguosha.cards.equipments.shields;

import sanguosha.cards.Color;
import sanguosha.cards.equipments.Shield;
import sanguosha.manager.GameManager;

public class RenWangDun extends Shield {
    public RenWangDun(GameManager gameManager,Color color, int number) {
        super(  gameManager,color, number);
    }

    @Override
    public Object use() {
        return null;
    }

    @Override
    public String toString() {
        return "仁王盾";
    }

    @Override
    public String details() {
        return "锁定技，黑色的[杀]对你无效。";
    }
}
