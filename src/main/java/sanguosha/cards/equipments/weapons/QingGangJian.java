package sanguosha.cards.equipments.weapons;

import sanguosha.cards.Card;
import sanguosha.cards.Color;
import sanguosha.cards.equipments.Shield;
import sanguosha.cards.equipments.Weapon;
import sanguosha.manager.GameManager;

import static sanguosha.cards.EquipType.shield;

import config.DescUrl;
import config.Text;

public class QingGangJian extends Weapon {
    public QingGangJian(GameManager gameManager, Color color, int number) {
        super(gameManager, color, number, 2);
    }

    @Override
   public Object use(Card sourceCard) {

        getSource().getPriviMsg().clearHeader2();
        getSource().getPriviMsg().setOneTimeInfo1("\n💬是否用 青釭剑：每当你使用[杀]指定一名目标角色后，你无视其防具。");

        if (!getSource().launchSkillPriv("青釭剑")) {
            return null;
        }

        if (getTarget().hasEquipment(shield, null)) {
            ((Shield) getTarget().getEquipments().get(shield)).setValid(false);
        } else if (getTarget().hasBaZhen()) {
            getTarget().setBaZhen(false);
        }
        String res = Text.format("%s 使用 %s 无视防具！", 
        getSource().getPlateName(), DescUrl.getDescHtml(toString()));
        //sleep(1000);
        getGameManager().getIo().printlnPublic(res, toString());
        return null;
    }

    @Override
    public String toString() {
        return "青釭剑";
    }

    @Override
    public String details() {
        return "锁定技，每当你使用[杀]指定一名目标角色后，你无视其防具。";
    }
}
