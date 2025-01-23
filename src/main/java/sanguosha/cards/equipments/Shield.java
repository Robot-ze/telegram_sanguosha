package sanguosha.cards.equipments;

import sanguosha.cards.Color;
import sanguosha.cards.EquipType;
import sanguosha.cards.Equipment;
import sanguosha.manager.GameManager;

public abstract class Shield extends Equipment {
    private boolean valid = true;

    public Shield(GameManager gameManager,Color color, int number) {
        super(  gameManager,color, number, EquipType.shield);
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public abstract String details();

    @Override
    public String help() {
        return details() + "\n\n防具：防具是可以增强防御力的装备，装备区里始终只能放有一张防具牌。\n\n" + super.help();
    }
}
