package sanguosha.cards.equipments.horses;

import sanguosha.cards.Color;
import sanguosha.manager.GameManager;

public class ZiXing extends MinusOneHorse {

    public ZiXing(GameManager gameManager,Color color, int number) {
        super(  gameManager,color, number);
    }

    @Override
    public String toString() {
        return "紫骍";
    }
 
}
