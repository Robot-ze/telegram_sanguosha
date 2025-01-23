package sanguosha.people.mountain;

import sanguosha.cards.BasicCard;
import sanguosha.cards.Card;
import sanguosha.cards.basic.Sha;
import sanguosha.people.Identity;
import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.people.PlayerIO;
import sanguosha.skills.AfterWakeSkill;
import sanguosha.skills.ForcesSkill;
import sanguosha.skills.KingSkill;
import sanguosha.skills.Skill;
import sanguosha.skills.WakeUpSkill;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import config.Text;
import msg.MsgObj;

import java.util.ArrayList;
import java.util.List;

public class LiuChan extends Person {
    private boolean fangQuan = false;

    public LiuChan() {
        super(3, Nation.SHU);
    }

    @ForcesSkill("享乐")
    @Override
    public boolean canNotBeSha(Sha sha, Person p) {
        if (super.canNotBeSha(sha, p)) {
            return true;
        }

        if (!isActiveSkill("享乐")) {
            return false;
        }

        MsgObj publicMsg = MsgObj.newMsgObj(getGameManager());

        boolean active = p.launchSkillPublicDeepLink(
                publicMsg,
                "附加基本牌",
                Text.format("%s: %s 弃置一张基本牌，否则此[杀]对 %s 无效",

                        getSkillHtmlName("享乐"),
                        p.getHtmlName(),
                        getPlateName()),
                "xiangle1", false);

        if (!active) {

            try {
                return true;
            } finally {
                String res = ",放弃附加牌，此[杀]失效:<i>退朝，退朝~</i>";
                publicMsg.appendText(res);
                //sleep(1000);
                getGameManager().getMsgAPI().editCaptionForce(publicMsg);
            }

        }

        List<Card> basicCards = new ArrayList<>();
        for (Card c : p.getCards()) {
            if (c instanceof BasicCard) {
                basicCards.add(c);
            }
        }
        p.getPriviMsg().clearHeader2();
        p.getPriviMsg().setOneTimeInfo1(Text.format("%s: %s 弃置一张基本牌，否则此[杀]对 %s 无效",

                getSkillHtmlName("享乐"),
                p.getHtmlName(),
                getPlateName()));

        Card c = p.chooseCard(basicCards, true);

        if (c == null) {
            try {
                return true;
            } finally {
                String res = ",放弃附加牌，此[杀]失效:<i>退朝，退朝~</i>";
                publicMsg.appendText(res);
                //sleep(1000);
                getGameManager().getMsgAPI().editCaptionForce(publicMsg);
            }
        }

        return false;// 没选出牌，杀也无效
    }

    @Skill("放权")
    @Override
    public void usePhase(boolean fastMode) {
        getPriviMsg().clearHeader2();
        getPriviMsg().setOneTimeInfo1("\n💬是否用 放权：你可以跳过出牌阶段，然后此回合结束时，你可以弃置一张手牌并令一名其他角色获得一个额外的回合。");
        if (launchSkillPriv("放权")) {
            fangQuan = true;
            return;
        }
        super.usePhase(fastMode);
    }

    @Override
    public void selfEndPhase(boolean fastMode) {

        if (!isZuoCi()) {
            setskipNoticeUsePublic(false);
        }
        if (fangQuan) {
            getPriviMsg().clearHeader2();
            getPriviMsg().setOneTimeInfo1("\n💬放权：请选择一名其他角色获得一个额外的回合。");

            Person p = selectPlayer();
            if (p == null) {
                return;
            }
            getPriviMsg().clearHeader2();
            getPriviMsg().setOneTimeInfo1("\n💬放权：请选择一张牌并令TA获得一个额外的回合。");

            if (requestCard(null) == null) {
                return;
            }
            fangQuan = false;

            String res = Text.format("%s:<i>诶嘿嘿，有劳爱卿了~</i>", getSkillHtmlName("放权"));
            //sleep(1000);
            getGameManager().getIo().printlnPublic(res, toString());

            p.run(true);
        }
    }

    @KingSkill("若愚")
    @WakeUpSkill("若愚")
    @Override
    public void selfBeginPhase() {
        if (getIdentity() == Identity.KING && !isWakenUp()) {
            boolean isLowest = true;
            for (Person p : getGameManager().getPlayersBeginFromPlayer(this)) {
                if (p.getHP() < getHP()) {
                    isLowest = false;
                    break;
                }
            }
            if (isLowest) {
                MsgObj publicMsgObj = MsgObj.newMsgObj(getGameManager());
                if (launchSkillPublicDeepLink(
                        publicMsgObj,
                        "若愚",
                        Text.format("%s 是否用 %s",
                                getHtmlName(), getSkillHtmlName("若愚")),
                        "ruoyu1")) {
                    // println(this + " uses 若愚");
                    setMaxHpNotSetCurrent(getMaxHP() + 1);
                    recover(null, 1);
                    wakeUp();
                    addActiveSkill("激将");
                    setskipNoticeUsePublic(true);

                    String res = Text.format(",加1点体力上限，回复1点体力，获得\"激将\":<i>世人皆错看我，唉！</i>");
                    publicMsgObj.appendText(res);
                    //sleep(1000);
                    getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);

                }
            }
        }
    }

    @AfterWakeSkill("激将")
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
                    this.getPlateName(), getSkillHtmlName("激将"));
            publicMsgObj.text = reult;
            publicMsgObj.setImg(toString());
            publicMsgObj.chatId = getGameManager().getChatId();
            getGameManager().getMsgAPI().sendImg(publicMsgObj);
            reult = Text.format("\n环顾四周,已无蜀将可助一臂之力");
            publicMsgObj.text = publicMsgObj.text + reult;
            getGameManager().getIo().delaySendAndDelete(this, reult);
            //sleep(1000);
            getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);

            return null;
        }

        requestCardForPublic(publicMsgObj, showCardPerson, showCard, "杀",
                "帮TA出杀", "jijiang");

        // System.out.println("showCardPerson.get()=" + showCardPerson.get());
        if (showCardPerson.get() == null) {
            String reult = Text.format(",环顾四周,已无蜀将可助一臂之力");
            publicMsgObj.text = publicMsgObj.text + reult;
            publicMsgObj.replyMakup = null;
            getGameManager().getIo().delaySendAndDelete(this, reult);
            //sleep(1000);
            getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
            return null;
        } else {
            String reult = Text.format(",%s 已助一臂之力打出一杀", showCardPerson.get());
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
    public Set<String> getInitialSkills() {
        Set<String> skills = super.getInitialSkills();

        if (getIdentity() != Identity.KING) {
            skills.remove("若愚");
        }
        return skills;
    }

    @Override
    public String name() {
        return "刘禅";
    }

    @Override
    public String skillsDescription() {
        return "享乐：锁定技，当你成为一名角色使用[杀]的目标后，除非该角色弃置一张基本牌，否则此[杀]对你无效。\n" +
                "放权：你可以跳过出牌阶段，然后此回合结束时，你可以弃置一张手牌并令一名其他角色获得一个额外的回合。\n" +
                "若愚：主公技，觉醒技，准备阶段，若你是体力值最小的角色，你加1点体力上限，回复1点体力，然后获得\"激将\"。\n" +
                (isWakenUp() ? "激将：主公技，其他蜀势力角色可以在你需要时代替你使用或打出[杀]。" : "");
    }
}
