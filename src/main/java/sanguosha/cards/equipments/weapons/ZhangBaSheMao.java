package sanguosha.cards.equipments.weapons;

import sanguosha.cards.Card;
import sanguosha.cards.Color;
import sanguosha.cards.basic.Sha;
import sanguosha.cards.equipments.Weapon;
import sanguosha.manager.GameManager;

import java.util.ArrayList;

import config.DescUrl;
import config.Text;

public class ZhangBaSheMao extends Weapon {

    public ZhangBaSheMao(GameManager gameManager, Color color, int number) {
        super(gameManager, color, number, 3);
    }

    @Override
    public Object use(Card sourceCard) {
        getSource().getPriviMsg().clearHeader2();
        getSource().getPriviMsg().setOneTimeInfo1("\n💬你使用了丈八蛇矛,选择2张牌当作一个 杀：你可以将两张手牌当[杀]使用或打出。\n" +
                "如2张牌为红色，则视为红色的[杀]；如2张牌为黑色，视为黑色的[杀]；如2张牌为1红1黑，视为无色的[杀]。\n" +
                "发动〖丈八蛇矛〗使用或打出的杀视为无点数性质。");
        Card card;
        ArrayList<Card> cs = getSource().chooseManyFromProvided (2, getSource().getCards(),true);
        if (cs.size() != 2) {
            return null;
        }
        getSource().loseCard(cs);
        if (cs.get(0).isRed() && cs.get(1).isRed()) {
            card = new Sha(getGameManager(), Color.DIAMOND, 0);
        } else if (cs.get(1).isBlack() && cs.get(1).isBlack()) {
            card = new Sha(getGameManager(), Color.CLUB, 0);
        } else {
            card = new Sha(getGameManager(), Color.NOCOLOR, 0);
        }
        card.setIsFake(true);
        card.setReplaceCards(cs);
        String res = Text.format("%s 使用 %s 打出杀",
                getSource().getPlateName(), DescUrl.getDescHtml(toString()));
        //sleep(1000);
        getGameManager().getIo().printlnPublic(res, toString());
        return card;
    }

    @Override
    public String toString() {
        return "丈八蛇矛";
    }

    @Override
    public String details() {
        return "你可以将两张手牌当[杀]使用或打出。\n" +
                "如2张牌为红色，则视为红色的[杀]；如2张牌为黑色，视为黑色的[杀]；如2张牌为1红1黑，视为无色的[杀]。\n" +
                "发动〖丈八蛇矛〗使用或打出的杀视为无点数性质。";
    }
}
