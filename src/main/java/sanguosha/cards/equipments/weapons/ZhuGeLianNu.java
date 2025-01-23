package sanguosha.cards.equipments.weapons;

import config.DescUrl;
import config.Text;
import sanguosha.cards.Card;
import sanguosha.cards.Color;
import sanguosha.cards.equipments.Weapon;
import sanguosha.manager.GameManager;

public class ZhuGeLianNu extends Weapon {
    private int gameRound=-1;
    public ZhuGeLianNu(GameManager gameManager,Color color, int number) {
        super(  gameManager,color, number, 1);
    }

    @Override
  public Object use(Card sourceCard) {
        System.out.println("zhugeliannu----------");
        if(getGameManager().getPersonRound()>gameRound){//只在每回合第一次显示一次
            gameRound=getGameManager().getPersonRound();
            String res=Text.format("%s %s !", 
            getSource().getPlateName(),DescUrl.getDescHtml(toString()));
            //sleep(1000);
            getGameManager().getIo().printlnPublic(res, toString());
        }
        return null;
    }

    @Override
    public String toString() {
        return "诸葛连弩";
    }

    @Override
    public String details() {
        return "锁定技，你于出牌阶段内使用[杀]无次数限制。";
    }
}
