package sanguosha.cards.equipments.weapons;

import config.Text;
import sanguosha.cards.Card;
import sanguosha.cards.Color;
import sanguosha.cards.basic.Sha;
import sanguosha.cards.equipments.Weapon;
import sanguosha.manager.GameManager;

public class QingLongYanYueDao extends Weapon {
    public QingLongYanYueDao(GameManager gameManager, Color color, int number) {
        super(gameManager, color, number, 3);
    }

    @Override
    public Object use(Card sourceCard) {
        // 如果连续闪的话，这里会连续嵌套调用，要点好多点，不过懒得改了
        boolean active = getSource().launchSkillPublic(
                "青龙偃月刀",
                Text.format("%s 是否用 %s",
                        getSource().getHtmlName(), getHtmlName()),
                "qlyyd");
        if (!active) {
            return null;
        }
        getSource().getPriviMsg().setOneTimeInfo1("💬如果有足够的[杀]，可以一直追杀下去，直到目标角色不使用[闪]或使用者无[杀]为止。");
        Sha s = getSource().requestSha(getTarget(), false);
        if (s != null) {
            s.setTarget(getTarget());
            s.setSource(getSource());
            s.use();
        }
        return null;
    }

    @Override
    public String toString() {
        return "青龙偃月刀";
    }

    @Override
    public String details() {
        return "每当你使用的[杀]被目标角色使用的[闪]抵消时，你可以对其使用一张[杀]（无距离限制）。\n" +
                "如果有足够的[杀]，可以一直追杀下去，直到目标角色不使用[闪]或使用者无[杀]为止。";
    }
}
