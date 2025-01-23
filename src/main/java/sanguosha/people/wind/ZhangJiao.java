package sanguosha.people.wind;

import sanguosha.cards.Card;
import sanguosha.cards.Color;
import sanguosha.cards.basic.HurtType;
import sanguosha.cards.basic.Shan;
import sanguosha.cards.strategy.judgecards.ShanDian;

import sanguosha.people.Identity;
import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.InjectSkill;
import sanguosha.skills.KingSkill;
import sanguosha.skills.Skill;
import components.TimeLimit;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import config.Config;
import config.Text;
import msg.CallbackEven;
import msg.MsgObj;

public class ZhangJiao extends Person {

    private Map<Person, InjectSkill> injectSkillInstMap = new HashMap<>();

    public ZhangJiao() {
        super(3, Nation.QUN);
    }

    @Skill("雷击")
    @Override
    public boolean requestShan(MsgObj publicMsg, boolean sendSingleMsg, Person source) {
        MsgObj publicMsgObj0 = MsgObj.newMsgObj(getGameManager());
        if (super.requestShan(publicMsg, sendSingleMsg, source)) {
            if (launchSkillPublicDeepLink(
                    publicMsgObj0,
                    "雷击",
                    "当你使用或打出[闪]时，你可以令一名角色进行判定，若结果为黑桃，你对其造成2点雷电伤害。",
                    "leiji1")) {
                getPriviMsg().setOneTimeInfo1("雷击:你想雷击哪位玩家");
                Person p = selectPlayer();
                if (p != null) {
                    Card c = getGameManager().getCardsHeap().judge(this, new CallbackEven() {
                        @Override
                        public boolean juge(Card card) {
                            return (card.color() == Color.SPADE);
                        }
                    });

                    MsgObj publicMsgObj = getTempActionMsgObjFirstOrder(publicMsgObj0, "changeJudge");

                    if (c.color() == Color.SPADE) {
                        boolean isPreLink = p.isLinked();
                        int realNum = p.hurt((Card) null, this, 2, HurtType.thunder);
                        String res = Text.format("\n%s 因 %s 受到%s⚡️伤害",
                                p.getHtmlName(),
                                getSkillHtmlName("雷击"), realNum);
                        publicMsgObj.appendText(res);
                        if (isPreLink) {
                            getGameManager().linkHurt(publicMsgObj, null, this, p, realNum, HurtType.thunder);
                        }
                        publicMsgObj.appendText(":<i>雷公电母，听我号令！</i>");
                        // sleep(1000);
                        getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);

                    } else {
                        publicMsgObj.appendText("判定失败:<i>怪哉，雷公电母何在！</i>");
                        // sleep(1000);
                        getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Skill("鬼道")
    @Override
    public Card changeJudge(Person target, Card d) {
        // System.out.println(" changeJudge(Person target, Card d)" + target);
        MsgObj publicMsgObj = MsgObj.newMsgObj(getGameManager());
        // 伤害和那些什么写在改判牌这里
        target.putTempActionMsgObj("changeJudge", publicMsgObj);
        if (launchSkillPublicDeepLink(
                publicMsgObj,
                "鬼道",
                Text.format("%s,%s 的判定牌是 %s",
                        getHtmlName(),
                        target.getPlateName(), d.getHtmlNameWithColor()),
                "guidao"

        )) {
            getPriviMsg().appendOneTimeInfo1("💬请出一张黑色牌");
            Card c = requestRedBlack("black", true);
            if (c != null) {

                getGameManager().getCardsHeap().retrieve(c);
                addCard(d);
                String res = Text.format(",改判 %s:<i>道势所向，皆由我控！</i>",
                        
                        c.getHtmlNameWithColor());
                publicMsgObj.appendText(res);
                // sleep(1000);
                getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
                
                return c;
            }
        }
        return null;
    }

    // 黄天插技能
    @KingSkill("黄天")
    @Override
    public void otherPersonUsePhaseBefore(Person thatPerson) {
        if (thatPerson == this) {// 自己就不插了
            return;
        }
        if (getIdentity() != Identity.KING) {// 不是主公没这技能
            return;
        }
        if (thatPerson.getNation() != Nation.QUN) {// 不是群势力角色
            return;
        }
        InjectSkill skill = injectSkillInstMap.get(thatPerson);
        Person personThis = this;
        if (skill == null) {
            skill = new InjectSkill("黄天(" + toString() + ")") {

                @Override
                public boolean use(Person target) {
                    target.getPriviMsg().clearHeader2();
                    target.getPriviMsg().setOneTimeInfo1(
                            Text.format("\n💬是否用 黄天:主公技，群势力角色的出牌阶段限一次，可以将一张[闪]或[闪电]交给%s", personThis.getHtmlName()));
                    Card c = null;
                    TimeLimit t = new TimeLimit(Config.PRIV_RND_TIME_60S);
                    while (getGameManager().isRunning() &&
                            ((c = target.requestCard(null)) != null && !(c instanceof Shan || c instanceof ShanDian))) {
                        if (c != null) {// 牌不对得加回去给别人
                            getGameManager().getCardsHeap().retrieve(c);
                            target.addCard(c);
                        }
                        if (t.isTimeout()) {
                            return false;
                        }
                        target.getPriviMsg().setOneTimeInfo1(Text.format("\n💬所选卡牌种类不匹配，你可以将一张[闪]或[闪电]交给他 ", this));
                    }
                    if (c != null) {
                        getGameManager().getCardsHeap().retrieve(c);
                        addCard(c);
                        setLaunched();
                        try {
                            return true;
                        } finally {
                            String res = Text.format("%s 将 一个 %s 献给 %s<i>成为黄天之世的祭品吧！</i>",
                                    thatPerson.getPlateName(),
                                    c.getHtmlNameWithColor(), getHtmlName());
                            // sleep(1000);
                            getGameManager().getIo().printlnPublic(res, thatPerson.toString());
                        }

                    }
                    return false;
                }

            };
            injectSkillInstMap.put(thatPerson, skill);
        }

        if (!skill.isLaunched()) {
            thatPerson.getInjectSkills().add(skill);
        }

    }

    // 黄天清技能
    @Override
    public void otherPersonEndPhase(Person thatPerson, boolean fastMode) {
        InjectSkill skill = injectSkillInstMap.get(thatPerson);
        if (skill != null) {
            skill.setNotLaunched();
        }
    }

    @Override
    public Set<String> getInitialSkills() {
        Set<String> skills = super.getInitialSkills();

        if (getIdentity() != Identity.KING) {
            skills.remove("黄天");
        }
        return skills;
    }

    @Override
    public String name() {
        return "张角";
    }

    @Override
    public String skillsDescription() {
        return "雷击：当你使用或打出[闪]时，你可以令一名角色进行判定，若结果为黑桃，你对其造成2点雷电伤害。\n" +
                "鬼道：当一名角色的判定牌生效前，你可以出黑色牌替换之。\n" +
                "黄天：主公技，其他群势力角色的出牌阶段限一次，该角色可以将一张[闪]或[闪电]交给你。";
    }
}