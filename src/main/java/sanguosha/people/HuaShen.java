package sanguosha.people;

import sanguosha.cards.Card;
import sanguosha.cards.Equipment;
import sanguosha.skills.Skill;
import components.TimeLimit;

import java.util.List;

import config.Config;
import config.DescUrl;
import config.Text;
import msg.MsgObj;
import msg.ReturnType;

import java.util.ArrayList;

public interface HuaShen extends PlayerIO, SkillLauncher {

    void setZuoCi(boolean zuoCi);

    void addHuaShenToList(Person p);

    /**
     * 目前可变换的化身列表
     * 
     * @return
     */
    ArrayList<Person> getHuaShenList();

    void setActiveHuaShen(Person p);

    Person getActiveHuaShen();

    int getShaCount();

    ArrayList<Card> getExtraCards();

    void loseCard(ArrayList<Card> cs, boolean throwaway, boolean print);

    @Override
    boolean hasWuShuang();

    default void zuoCiInitialize() {
    }

    @Skill("化身")
    default boolean huaShen(boolean beginPhase) {

        if (isZuoCi()) {
            if (beginPhase) {
                if (getHuaShenList().size() <= 0) {
                    //初始化放在这里，免得每次都拿到没有打乱的顺序
                    zuoCiInitialize();
                }

                // 提醒两次
                MsgObj noticeMsg = MsgObj.newMsgObj(getGameManager());
                // 提醒两次
                // System.out.println( Text.format("%s 的回合,请点击按键出牌", getHtmlName()));
                boolean fastMode = !(getGameManager().getMsgAPI().noticeAndAskPublic(
                        noticeMsg,
                        (Person) this,
                        Text.format("%s 的回合,请点击按键出牌", getHtmlName()),
                        "点击出牌", "startRound", true));

                String callBackValue = (String) noticeMsg.getReturnAttr(ReturnType.Deeplink, 0);

                if (callBackValue == null) {// 如果用户还没有点击，删掉原再提醒一次
                    getGameManager().getMsgAPI().delMsg(noticeMsg);

                    //sleep(1000);
                    // 再取一次
                    callBackValue = (String) noticeMsg.getReturnAttr(ReturnType.Deeplink, 0);
                    if (callBackValue == null) {
                        noticeMsg.isDeleted = false;
                        fastMode = !(getGameManager().getMsgAPI().noticeAndAskPublic(
                                noticeMsg,
                                (Person) this,
                                Text.format("❗️请注意，%s 请点击按键出牌,如未出牌则跳过此回合", getHtmlName()),
                                "点击出牌", "startRound", true));

                    }
                }

                // 只删除按钮，让别人知道到谁出牌
                setFastMode(fastMode);
                getGameManager().getMsgAPI().clearButtons(noticeMsg);
            }
            // ----------------------------------------------------------------------------------------
            TimeLimit t = new TimeLimit(Config.PRIV_RND_TIME_60S);
            while (getGameManager().isRunning() && t.isNotTimeout()) {

                String res = "";
                for (Person p : getHuaShenList()) {
                    res += "\n<b>" + DescUrl.getDescHtml(p.toString()) + "</b>\n" + p.skillsDescription() + "\n";
                }
                res += "\n💬化身：你随机获得两张武将牌作为\"化身\"牌，然后亮出其中一张，获得该\"化身\"牌的除了主公技外的一个技能。" +
                        "回合开始时或结束后，你可以更改亮出的\"化身\"牌。\n";

                getPriviMsg().clearHeader2();
                getPriviMsg()
                        .setOneTimeInfo1(res);
                // 为什么要这步多余的操作，只是因为要消除按键那里显示处了一个不协调的emoji
                int pos = this.getGamePlayerNo();
                for (Person p : getHuaShenList()) {
                    p.setGamePlayerNo(0);
                }
                Person choice = selectPlayer(getHuaShenList(), true);
                setGamePlayerNo(pos);// 然后再设回来，只是因为要消除按键那里显示处了一个不协调的emoji

                if (choice != null) {
                    ArrayList<String> skillList = new ArrayList<>(choice.getInitialSkills());
                    res = "";
                    res += DescUrl.getDescHtml(choice.toString()) + "\n" + choice.skillsDescription() + "\n";
                    res += Text.format("\n💬化身：请选择除了主公技外的一个技能。");

                    getPriviMsg().setOneTimeInfo1(res);
                    int skillId = chooseFromProvided(null, false, skillList);

                    if (skillId >= 0) {
                        choice.clearAndAddActiveSkill(skillList.get(skillId), "新生");
                    } else {
                        choice.clearAndAddActiveSkill("新生");
                    }
                    System.out.println("choice.getActiveSkills()=" + choice.getActiveSkills());
                    choice.setGameManager(getGameManager());
                    // printlnPriv(this + " selects 化身 " + choice);
                    setActiveHuaShen(choice);
                    choice.setActiveHuaShen(choice);
                    changePersonAndPos(choice);
                    if (beginPhase) {
                        choice.setskipNoticeUsePublic(true);
                        choice.run(false);
                    }
                    return true;
                } else {
                    if (getActiveHuaShen() == null) {
                        continue;
                    } else {
                        if (beginPhase) {
                            getActiveHuaShen().run(false);
                        }
                        return true;
                    }

                }
            }
        }
        // printlnToIO("you can't use 化身 because I don't want to implement it");
        return false;
    }

    @Skill("新生")
    default void xinSheng() {// 这些技能为什么放这里，因为左慈的角色换了也要继承新生
        Person p = getGameManager().getPeoplePool().allocOnePerson();// 为什么这里没随机
        if (p == null) {
            // printlnPriv("无牌可用");
            return;
        }
        addHuaShenToList(p);
        p.setZuoCi(true);
        p.setGameManager(getGameManager());

        String res = "<b><a href=\"" + DescUrl.getDescUrl("左慈") + "\">新生</a></b>:<i>幻化无穷，生生不息。</i>";
        //sleep(1000);
        getGameManager().getIo().printlnPublic(res, "左慈");
        // printlnPriv(this + " got new 化身 " + p);
        // printlnPriv(this + " now has " + getHuaShenList().size() + " 化身");
    }

    /**
     * 调换人物和取代之前的位置
     * 
     * @param p
     */
    default void changePersonAndPos(Person p) {
        // 如果碰到和自己就是p，而只是技能的不同
        // System.out.println("this 换白板=" + this);
        // 如果碰到和自己就是p，而只是技能的不同

        if (this.equals(p)) {
            return;
        }
        p.setGameManager(getGameManager());
        p.setUser(getUser());
        p.setIdentityOnly(getIdentity());
        if (getIdentity() == Identity.KING && getGameManager().getIntialNumPlayers() > 4) {
            p.initMaxHP(p.getMaxHP() + 1);
        }
        p.setCurrentHP(Math.min(getHP(), p.getMaxHP()));
        p.setDaWu(isDaWu());
        p.setKuangFeng(isKuangFeng());

        p.setDrunk(isDrunk());
        p.setDrunkShaUsed(isDrunkShaUsed());
        p.setShaCount(getShaCount());
        p.setGamePlayerNo(getGamePlayerNo());
        if (isLinked() != p.isLinked()) {
            p.switchLink();
        }
        if (isTurnedOver() != p.isTurnedOver()) {
            p.turnover();
        }
        // ----------------------------------------
        p.getCards().clear();
        p.getCards().addAll(getCards());
        for (Card c : p.getCards()) {
            c.setOwner(p);
        }
        // ----------------------------------------
        for (Equipment equipment : getEquipments().values()) {
            p.getEquipments().put(equipment.getEquipType(), equipment);
        }

        for (Card c : p.getEquipments().values()) {
            c.setOwner(p);
        }
        // ----------------------------------------
        p.getJudgeCards().clear();
        p.getJudgeCards().addAll(getJudgeCards());

        for (Card c : p.getJudgeCards()) {
            c.setOwner(p);
        }

        // ----------------------------------------
        if (getExtraCards() != null) {// 之前的收集的集气，集田之类的牌，全部丢进废牌
            getGameManager().getCardsHeap().discard(getExtraCards());
            getExtraCards().clear();
        }

        // loseCard(p.getCards(), false, false);
        // loseCard(new ArrayList<>(p.getRealJudgeCards()), false, false);
        // loseCard(new ArrayList<>(p.getEquipments().values()), false, false);
        getCards().clear();
        getRealJudgeCards().clear();
        getEquipments().clear();

        p.setHuaShenList(getHuaShenList());
        // if(hasWakenUp()){
        // p.wakeUp();
        // }
        // selectHuaShen(p);
        p.setZuoCi(isZuoCi());

        int index = getGameManager().getPlayers().indexOf((Person) this);
        getGameManager().getPlayers().set(index, p);
        getGameManager().removeIndentity(getIdentity(), (Person) this);
        getGameManager().addIndentity(getIdentity(), (Person) p);

        for (Person pp : getGameManager().getPlayers()) {
            if (pp.isAI()) {// 有可能AI之前已经把他当成目标
                AI ai = (AI) pp;

                if (this.equals(ai.getPrimaryEnemy())) {
                    ai.setPrimaryEnemy(p);
                }
            }
        }

    }
}
