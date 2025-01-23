package sanguosha.cards.equipments.weapons;

import config.DescUrl;
import config.Text;
import sanguosha.cards.Card;
import sanguosha.cards.Color;
import sanguosha.cards.equipments.Weapon;
import sanguosha.manager.GameManager;

public class ZhuQueYuShan extends Weapon {
    public ZhuQueYuShan(GameManager gameManager, Color color, int number) {
        super(gameManager, color, number, 4);
    }

    @Override
  public Object use(Card sourceCard) {
        getSource().getPriviMsg().clearHeader2();
        getSource().getPriviMsg().setOneTimeInfo1("\n💬你是否用 朱雀羽扇:你可以将一张普通[杀]当火[杀]使用。");
        if (!getSource().launchSkillPriv(toString())) {
            return false;
        }
        String res = Text.format("%s %s,杀变火杀",
                getSource().getPlateName(), DescUrl.getDescHtml(toString()));
        //sleep(1000);
        getGameManager().getIo().printlnPublic(res, toString());
        //sleep(1000);
        return true;
    }

    @Override
    public String toString() {
        return "朱雀羽扇";
    }

    @Override
    public String details() {
        return "你可以将一张普通[杀]当火[杀]使用。";
    }
}
