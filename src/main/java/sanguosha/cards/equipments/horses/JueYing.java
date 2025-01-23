package sanguosha.cards.equipments.horses;

import sanguosha.cards.Color;
import sanguosha.manager.GameManager;

public class JueYing extends PlusOneHorse {

    public JueYing(GameManager gameManager,Color color, int number) {
        super(  gameManager,color, number);
    }

    @Override
    public String toString() {
        return "绝影";
    }
}
