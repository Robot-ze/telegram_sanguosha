package sanguosha.people.shu;

import sanguosha.cards.Card;
import sanguosha.cards.basic.Sha;

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

public class LiuBei extends Person {
    private int rendeCount;
    private boolean hasRecovered;

    public LiuBei() {
        super(4, Nation.SHU);
        //super(1, Nation.SHU);//fortest
    }

    @Override
    public void selfBeginPhase() {
        rendeCount = 0;
        hasRecovered = false;
    }

    @Skill("仁德")
    public void renDe() {
        getPriviMsg().clearHeader2();
        String res="仁德：出牌阶段，你可以将任意张手牌交给其他角色，给出第二张手牌时，你回复1点体力。\n请选择一个人";
        getPriviMsg().setOneTimeInfo1(res);
        Person p = selectPlayer();
        if (p == null) {
            return;
        }
        res="仁德：请选择你要给与的牌";
        getPriviMsg().setOneTimeInfo1(res);
        ArrayList<Card> cards = chooseManyFromProvided(0, getCards());
        if (cards == null || cards.isEmpty()) {
            return;
        }
        // println(this + " gives " + cards.size() + " cards to " + p);
        loseCard(cards, false);
        p.addCard(cards);

        rendeCount += cards.size();
        if (!hasRecovered && rendeCount >= 2) {
            recover(null,1);
            hasRecovered = true;
        }

        getGameManager().getIo().printlnPublic(
                Text.format("赐予 %s %s张牌%s:<i>惟贤惟德，能服于人</i>",
                        //getPlateName(),
                        p.getHtmlName(),
                        rendeCount + "",
                        (!hasRecovered && rendeCount >= 2) ? ",并恢复了1体力" : ""),
                toString());
        //sleep(3000);
    }

    @KingSkill("激将")
    public Card jiJiang() {
        // if(getShaCount()<=0){ //这几行不能加，加了被动的出杀时候也触发不了
        // return null;
        // }
        ArrayList<Person> shuPeople = getGameManager().peoplefromNation(Nation.SHU);
        shuPeople.remove(this);

        MsgObj publicMsgObj = MsgObj.newMsgObj(getGameManager());
        publicMsgObj.forPlayers = new ArrayList<>();
        AtomicReference<PlayerIO> showCardPerson = new AtomicReference<>(null);
        AtomicReference<Card> showCard = new AtomicReference<>(null);
        for (Person p : shuPeople) {
            if (p.getUser() != null && p.existsSha()) {// 不是AI
                publicMsgObj.forPlayers.add(p);
            }
        }
        if (publicMsgObj.forPlayers.isEmpty()) {

            String reult = Text.format("%s %s:<i>蜀将何在？</i>",
             this.getPlateName(),getSkillHtmlName("激将"));
            publicMsgObj.text = reult;
            publicMsgObj.setImg(toString());
            publicMsgObj.chatId = getGameManager().getChatId();
            getGameManager().getMsgAPI().sendImg(publicMsgObj);
            //sleep(3000);
            reult = Text.format(",环顾四周,已无蜀将可助一臂之力");
            publicMsgObj.text = publicMsgObj.text + reult;
            getGameManager().getIo().delaySendAndDelete(this, reult);
            //sleep(1000);
            getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
            //sleep(3000);
            return null;
        }

        requestCardForPublic(publicMsgObj, showCardPerson,showCard, "杀",
                "帮TA出杀", "jijiang");

        //System.out.println("showCardPerson.get()=" + showCardPerson.get());
        if (showCardPerson.get() == null) {
            String reult = Text.format(",环顾四周,已无蜀将可助一臂之力");
            publicMsgObj.text = publicMsgObj.text + reult;
            publicMsgObj.replyMakup = null;
            getGameManager().getIo().delaySendAndDelete(this, reult);
            //sleep(1000);
            getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
            return null;
        } else {
            String reult = Text.format(",%s 已助一臂之力打出一杀",showCardPerson.get());
            publicMsgObj.text = publicMsgObj.text + reult;
            publicMsgObj.replyMakup = null;
            getGameManager().getIo().delaySendAndDelete(this, reult);
            //sleep(1000);
            getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
            return showCard.get();
        }

       
    }

    @Override
    public boolean skillSha(Person sourse) {
        getPriviMsg().setOneTimeInfo1(Text.format("是否使用 %s：主公技，其他蜀势力角色可以在你需要时代替你使用或打出[杀]。（视为由你使用或打出）",
        getSkillHtmlName("激将")));
        if (getIdentity() == Identity.KING && launchSkillPriv("激将")) {
            return jiJiang() != null;
        }
        return false;
    }

    @Override
    public void usePhaseBefore() {

        if (isActiveSkill("仁德") && getCards().size() >= 1) {
            getSkillCards().add("仁德");
        }
        // ArrayList<Person> shuPeople = getGameManager().peoplefromNation(Nation.SHU);
        // shuPeople.remove(this);
        // if (isActiveSkill("激将") && shuPeople != null && shuPeople.size() > 0 && getShaCount() < getMaxShaCount()) {
        //     getSkillCards().add("激将");
        // }

    }

    @Override
    public boolean useSkillInUsePhase( int orderInt) {

        //int orderInt = Integer.valueOf(order) - 1;

        if (orderInt < getSkillCards().size() && getSkillCards().get(orderInt).equals("仁德")) {
            renDe();
            return true;
        } 
        //不能主动发出
        // else if (orderInt < getSkillCards().size() && getSkillCards().get(orderInt).equals("激将")
        //         && getIdentity() == Identity.KING) {
        //     // -------选人
        //     Person target = selectPlayer(false);
        //     if (target == null) {
        //         return false;
        //     }
        //     Card sha = jiJiang();
        //     if (sha == null) {
        //         return false;
        //     }

        //     sha.setSource(this);
        //     sha.setTarget(target);
        //     return useCard(sha);
        // }
        return false;
    }

    @Override
    public Set<String> getInitialSkills() {
        Set<String> skills = super.getInitialSkills();

        if (getIdentity() != Identity.KING) {
            //("不是主公" + getIdentity());
            skills.remove("激将");
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
        return "刘备";
    }

    @Override
    public String skillsDescription() {
        return "仁德：出牌阶段，你可以将任意张手牌交给其他角色，然后你于此阶段内给出第二张手牌时，你回复1点体力。\n" +
                "激将：主公技，其他蜀势力角色可以在你需要时代替你使用或打出[杀]。（视为由你使用或打出）。";
    }
}
