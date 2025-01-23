package sanguosha.cards.equipments.horses;

import sanguosha.cards.Color;
import sanguosha.cards.EquipType;
import sanguosha.cards.Equipment;
import sanguosha.manager.GameManager;

public abstract class PlusOneHorse extends Equipment {
    public PlusOneHorse(GameManager gameManager,Color color, int number) {
        super(  gameManager,color, number, EquipType.plusOneHorse);
    }

    @Override
    public Object use() {
        return null;
    }

    @Override
    public String help() {
        return "锁定技，其他角色与你的距离+1。（可以理解为一种防御上的优势）" +
                "不同名称的+1坐骑，其效果是相同的。\n\n" + super.help();
    }
}
