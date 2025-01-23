package sanguosha.cards.equipments.weapons;

import config.Text;
import sanguosha.cards.Card;
import sanguosha.cards.Color;
import sanguosha.cards.equipments.Weapon;
import sanguosha.manager.GameManager;

public class FangTianHuaJi extends Weapon {
    private Card sha;

    public FangTianHuaJi(GameManager gameManager, Color color, int number) {
        super(gameManager, color, number, 4);
    }

    @Override
    public Object use(Card sourceCard) {
        this.sha = sourceCard;
        if (getSource().getCards().size() == 1) {
            // if (true) {
            getSource().getPriviMsg().setOneTimeInfo1(
                    Text.format("\n💬是否用 %s,[杀]若是你最后的手牌，你可以额外选择至多两个目标。", toString()));

            if (getSource().launchSkillPriv("方天画戟")) {
                sha.setMultiSha(sha.getMultiSha() + 2);
            }
        }

        return null;
    }

    public void setsha(Card sha) {
        this.sha = sha;
    }

    @Override
    public String toString() {
        return "方天画戟";
    }

    @Override
    public String details() {
        return "你使用的[杀]若是你最后的手牌，你可以额外选择至多两个目标。\n";
    }
}
