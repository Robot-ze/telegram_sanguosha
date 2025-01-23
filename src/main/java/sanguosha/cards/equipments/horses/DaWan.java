package sanguosha.cards.equipments.horses;

import sanguosha.cards.Color;
import sanguosha.manager.GameManager;

public class DaWan extends MinusOneHorse {

    public DaWan(GameManager gameManager,Color color, int number) {
        super(  gameManager,color, number);
    }

    @Override
    public String toString() {
        return "大宛";
    }

  
}
