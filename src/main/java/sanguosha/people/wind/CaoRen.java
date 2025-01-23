package sanguosha.people.wind;

 
import config.Text;
import sanguosha.cards.Card;
import sanguosha.cards.Equipment;
import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.Skill;

public class CaoRen extends Person {
    public CaoRen() {
        super(4, Nation.WEI);
    }

    @Skill("据守")
    @Override
    public void selfEndPhase(boolean fastMode) {
        getPriviMsg().setOneTimeInfo1("据守:结束阶段，你可以翻面，若如此做，你摸4张牌，然后弃置一张牌，若为装备牌，则视为使用");
        if (launchSkillPriv("据守")) {
            drawCards(4);
            getPriviMsg().setOneTimeInfo1("据守:你已获得4张牌，请弃置1张牌并翻面");

            Card c =chooseCard(getCards());
            if(c instanceof Equipment){
                putOnEquipment(c);
            }else{
                c.setTaken(false);
                throwCard(c);
            }
            turnover();
            //sleep(1000);
            String res=Text.format("%s %s:<i>兵精粮足，守土一方</i>", 
            getPlateName(),getSkillHtmlName("据守"));
            getGameManager().getIo().printlnPublic(res, toString());
        }
    }

    @Override
    public String name() {
        return "曹仁";
    }

    @Override
    public String skillsDescription() {
        return "结束阶段，你可以翻面，若如此做，你摸四张牌，然后选择一项：1.弃置一张不为装备牌的牌；2.使用一张装备牌。";
    }
}
