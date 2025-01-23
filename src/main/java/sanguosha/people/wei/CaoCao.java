package sanguosha.people.wei;

import sanguosha.cards.Card;
import sanguosha.manager.Utils;
import sanguosha.people.Identity;
import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.people.PlayerIO;
import sanguosha.skills.KingSkill;
import sanguosha.skills.Skill;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import config.Text;
import msg.MsgObj;

import java.util.ArrayList;
import java.util.List;

public class CaoCao extends Person {
    public CaoCao() {
        super(4, Nation.WEI);
    }

    @Skill("奸雄")
    @Override
    public void gotHurt(List<Card> cs, Person p, int num) {

        if (cs != null) {
            // String cardString = "";
            // System.out.println("奸雄 cs=" + cs);
            for (Card c : cs) {
                if (c == null) {
                    continue;
                }

                // if (c.isNotTaken()) {// 全部牌拿走
                addCard(c);
                c.setTaken(true);
                // cardString += c.getHtmlNameWithColor() + " ";
                getJudgeCards().remove(c);
                getGameManager().getCardsHeap().retrieve(c);
                // }
            }

            String result = Text.format("%s,%s 获得%s张牌:<i>宁教我负天下人，休教天下人负我！</i>",
                    getSkillHtmlName("奸雄"), getPlateName(),
                    cs.size());
            //sleep(1000);
            getGameManager().getIo().printlnPublic(result, toString());

        }

    }

    @KingSkill("护驾")
    private Card hujia() {

        // if(getShaCount()<=0){ //这几行不能加，加了被动的出杀时候也触发不了
        // return null;
        // }
        ArrayList<Person> weiPeople = getGameManager().peoplefromNation(Nation.WEI);
        weiPeople.remove(this);

        MsgObj publicMsgObj = MsgObj.newMsgObj(getGameManager());
        publicMsgObj.forPlayers = new ArrayList<>();
        AtomicReference<PlayerIO> showCardPerson = new AtomicReference<>(null);
        AtomicReference<Card> showCard = new AtomicReference<>(null);
        for (Person p : weiPeople) {
            if (p.getUser() != null && p.existsSha()) {// 不是AI
                publicMsgObj.forPlayers.add(p);
            }
        }
        if (publicMsgObj.forPlayers.isEmpty()) {

            String reult = Text.format("%s %s :魏将何在？来人，护驾！",
                    this.getPlateName(), getSkillHtmlName("护驾"));
            publicMsgObj.text = reult;
            publicMsgObj.setImg(toString());
            publicMsgObj.chatId = getGameManager().getChatId();
            getGameManager().getMsgAPI().sendImg(publicMsgObj);
            // sleep(3000);
            reult = Text.format("环顾四野,已无魏将护驾");
            publicMsgObj.text = publicMsgObj.text + reult;
            getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
            // sleep(3000);
            return null;
        }

        requestCardForPublic(publicMsgObj, showCardPerson, showCard, "闪",
                "帮TA出闪", "hujia1");

        // System.out.println("showCardPerson.get()=" + showCardPerson.get());
        if (showCardPerson.get() == null) {
            String reult = Text.format("环顾四野,已无魏将护驾");
            publicMsgObj.text = publicMsgObj.text + reult;
            publicMsgObj.replyMakup = null;
            getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
            return null;
        } else {
            String reult = Text.format("%s 已护驾打出一闪");
            publicMsgObj.text = publicMsgObj.text + reult;
            publicMsgObj.replyMakup = null;
            getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
            return showCard.get();
        }
    }

    @Override
    public boolean skillShan(Person sourse) {

        if (getIdentity() == Identity.KING) {
            getPriviMsg().setOneTimeInfo1("你可以令其他魏势力角色选择是否出“闪”");
            if (launchSkillPriv("护驾")) {
                return hujia() != null;
            }
        }
        return false;
    }

    @Override
    public Set<String> getInitialSkills() {
        Set<String> skills = super.getInitialSkills();

        if (getIdentity() != Identity.KING) {
            skills.remove("护驾");
        }
        return skills;
    }

    // @Override
    // public void clearAndAddActiveSkill(String... skills) {
    // // TODO Auto-generated method stub
    // super.clearAndAddActiveSkill(skills);
    // }

    @Override
    public String name() {
        return "曹操";
    }

    @Override
    public String skillsDescription() {
        return "奸雄：当你受到伤害后，你可以获得造成此伤害的牌。\n" +
                "护驾：主公技，当你需要使用或打出“闪”时，你可以令其他魏势力角色选择是否出“闪”（视为由你使用或打出）。";
    }
}
