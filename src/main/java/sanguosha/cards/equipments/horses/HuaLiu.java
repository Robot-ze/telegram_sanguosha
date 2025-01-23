package sanguosha.cards.equipments.horses;

import sanguosha.cards.Color;
import sanguosha.manager.GameManager;

public class HuaLiu extends PlusOneHorse {

    public HuaLiu(GameManager gameManager,Color color, int number) {
        super(  gameManager,color, number);
    }

    @Override
    public String toString() {
        return "骅骝";
    }
}
