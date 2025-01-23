package sanguosha.people.shu;

import config.Text;
import sanguosha.cards.Card;
import sanguosha.cards.basic.Sha;
import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.ForcesSkill;

public class ZhangFei extends Person {
    public ZhangFei() {
        super(4, Nation.SHU);
         
    }

    @Override
    public void usePhaseBefore(){
        if(isActiveSkill("咆哮")){
            paoXiao();
        }
       
    }

    @Override
    public boolean useCard(Card card) {
        if(card instanceof Sha){
            String res=Text.format("<i>燕人张飞在此！！！</i>");   
            getGameManager().getIo().printlnPublic(res, toString());
            //sleep(3000);
        }
        return super.useCard(card);
    }

    @ForcesSkill("咆哮")
    public void paoXiao() {
        setMaxShaCount(10000);
    }

    @Override
    public String name() {
        return "张飞";
    }

    @Override
    public String skillsDescription() {
        return "咆哮：锁定技，你使用[杀]无次数限制。";
    }
}
