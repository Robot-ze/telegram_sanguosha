package sanguosha.cards.equipments;

import sanguosha.cards.Card;
import sanguosha.cards.Color;
import sanguosha.cards.EquipType;
import sanguosha.cards.Equipment;
import sanguosha.manager.GameManager;

public abstract class Weapon extends Equipment {
    private final int distance;

    public Weapon(GameManager gameManager,Color color, int number, int distance) {
        super(  gameManager,color, number, EquipType.weapon);
        this.distance = distance;
    }

    public int getDistance() {
        return distance;
    }

    @Override
    public Object use() {
        return  use(null);
    }

    /**
     * 给武器单独实现的use
     * @param sourceCard
     * @return
     */
    public abstract Object use(Card sourceCard);


    public abstract String details();

    @Override
    public String help() {
        return "攻击距离：" + distance + "\n" + details() +
                "\n\n武器：武器牌能增强攻击能力，装备区里只能摆放一张武器牌。\n\n" + super.help();
    }
}
