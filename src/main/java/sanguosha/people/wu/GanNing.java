package sanguosha.people.wu;

import config.Text;
import sanguosha.cards.Card;
import sanguosha.cards.strategy.GuoHeChaiQiao;
import sanguosha.cardsheap.CardsHeap;
import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.Skill;

public class GanNing extends Person {
    public GanNing() {
        super(4, Nation.WU);
    }

    @Override
    public void usePhaseBefore(){

        if (isActiveSkill("奇袭")&& getCards().size()>0  ){
            getSkillCards().add("奇袭");           
        } 
    }
 


    @Skill("奇袭")
    @Override
    public boolean useSkillInUsePhase( int orderInt) {
        //int orderInt=Integer.valueOf(order)-1 ;
        
        if ( orderInt<getSkillCards().size()&& getSkillCards().get(orderInt).equals("奇袭")) {
            getPriviMsg().setOneTimeInfo1("奇袭：你可以将一张黑色牌当[过河拆桥]使用");
            Card c = requestRedBlack("black", true);
            if (c == null) {
                return true;
            }
            GuoHeChaiQiao chai = new GuoHeChaiQiao(getGameManager(),c.color(), c.number());
            getPriviMsg().setOneTimeInfo1("奇袭：你可以将一张黑色牌当[过河拆桥]使用。请选择角色");
            if (chai.askTarget(this)) {
                useCard(chai);
            } else {
                addCard(getGameManager().getCardsHeap().retrieve(c), false);
            }
            String res=Text.format("%s %s:<i>你的牌太多啦！</i>", getPlateName(),getSkillHtmlName("奇袭"));   
            getGameManager().getIo().printlnPublic(res, toString());
            //sleep(3000);

            return true;
        }
        return false;
    }

    @Override
    public String name() {
        return "甘宁";
    }

    @Override
    public String skillsDescription() {
        return "奇袭：你可以将一张黑色牌当[过河拆桥]使用。";
    }
}
