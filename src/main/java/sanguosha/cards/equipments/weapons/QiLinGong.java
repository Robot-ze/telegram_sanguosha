package sanguosha.cards.equipments.weapons;

import sanguosha.cards.Card;
import sanguosha.cards.Color;
import sanguosha.cards.equipments.Weapon;
import sanguosha.manager.GameManager;
import sanguosha.people.AI;

import static sanguosha.cards.EquipType.minusOneHorse;
import static sanguosha.cards.EquipType.plusOneHorse;

import config.Text;
import msg.MsgObj;

public class QiLinGong extends Weapon {

    public QiLinGong(GameManager gameManager, Color color, int number) {
        super(gameManager, color, number, 5);
    }

    @Override
    public Object use(Card sourceCard) {
        if (getTarget().hasEquipment(plusOneHorse, null) &&
                getTarget().hasEquipment(minusOneHorse, null)) {

            boolean active = false;
            if (getOwner() .isAI()) {
                return false;
            } else {

                active = getGameManager().getMsgAPI().noticeAndAskPublic(
                        this,
                        getOwner(),
                        Text.format("%s 是否用 %s",
                                getOwner().getHtmlName(), getHtmlName()),
                        "使用" + toString(), "qilinggong");

            }

            if (!active) {
                return null;
            }

            getSource().getPriviMsg().setOneTimeInfo1("💬你可以弃置其装备区里的一张坐骑牌。");
            int choice = getSource().chooseNoNull("射下提速马",
                    "射下减速马", "取消(Esc)");
            if (choice == 2) {
                // getTarget().getEquipments().put(minusOneHorse, null);
                getTarget().getEquipments().remove(minusOneHorse);
            } else if (choice == 1) {
                // getTarget().getEquipments().put(plusOneHorse, null);
                getTarget().getEquipments().remove(plusOneHorse);
            }
        } else if (getTarget().hasEquipment(plusOneHorse, null) ||
                getTarget().hasEquipment(minusOneHorse, null)) {

            getSource().getPriviMsg().clearHeader2();
            getSource().getPriviMsg().setOneTimeInfo1("\n💬是否用 麒麟弓：每当你使用[杀]对目标角色造成伤害时，你可以弃置其装备区里的一张坐骑牌。");

            int choice = getSource().chooseNoNull("射下马匹", "取消(Esc)");
            if (choice == 1) {
                Card horse;
                if (getTarget().hasEquipment(plusOneHorse, null)) {
                    // getTarget().getEquipments().put(plusOneHorse, null);
                    horse=getTarget().getEquipments().get(plusOneHorse);
                    getTarget().getEquipments().remove(plusOneHorse);
                   

                } else {
                    // getTarget().getEquipments().put(minusOneHorse, null);
                    horse=getTarget().getEquipments().get(minusOneHorse);
                    getTarget().getEquipments().remove(minusOneHorse);
                }

                MsgObj msg = getTarget().getTempActionMsgObj("sha");
                if (msg != null) {
                    String result = Text.format("\n%s 的 %s 被%s射下",
                            getTarget().getPlateName(),horse.getHtmlNameWithColor(),toString() );
                    msg.text = msg.text + result;
                    getGameManager().getMsgAPI().editCaptionForce(msg);
                }
            }
        } else {
            // getSource().printlnToIO("target has no horse");
        }
        return null;
    }

    @Override
    public String toString() {
        return "麒麟弓";
    }

    @Override
    public String details() {
        return "每当你使用[杀]对目标角色造成伤害时，你可以弃置其装备区里的一张坐骑牌。";
    }
}
