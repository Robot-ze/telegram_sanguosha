package sanguosha.cards.basic;

import config.Text;
import sanguosha.cards.BasicCard;
import sanguosha.cards.Color;
import sanguosha.manager.GameManager;

public class Tao extends BasicCard {

    public Tao(GameManager gameManager,Color color, int number) {
        super( gameManager,color, number);
    }

    @Override
    public Object use() {
        getTarget().recover(null,1);
        //直接吃的不显示图
        getGameManager().getIo().printlnPublic(
            Text.format(
                "%s 吃 %s,+1点体力:%s", 
                getSource().getPlateName(),
                getHtmlNameWithColor(),
                getSource().getHPEmoji()
                ) );

        return true;
    }

    @Override
    public String toString() {
        return "桃";
    }

    @Override
    public String help() {
        return "使用时机：出牌阶段或一名角色濒死时。\n" +
                "使用目标：你（出牌阶段）或处于濒死状态的角色。\n" +
                "作用效果：目标角色回复1 点体力。";
    }
 
}
