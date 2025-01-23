package sanguosha.cards.equipments.horses;

import sanguosha.cards.Color;
import sanguosha.manager.GameManager;

public class ZhuaHuangFeiDian extends PlusOneHorse {

    public ZhuaHuangFeiDian(GameManager gameManager,Color color, int number) {
        super(  gameManager,color, number);
    }

    @Override
    public String toString() {
        return "爪黄飞电";
    }

   
}
