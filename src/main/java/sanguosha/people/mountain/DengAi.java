package sanguosha.people.mountain;

import java.util.ArrayList;

import config.Text;
import msg.CallbackEven;
import msg.MsgObj;
import sanguosha.cards.Card;
import sanguosha.cards.Color;
import sanguosha.cards.strategy.ShunShouQianYang;
import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.AfterWakeSkill;
import sanguosha.skills.Skill;
import sanguosha.skills.WakeUpSkill;

public class DengAi extends Person {
    private final ArrayList<Card> tian = new ArrayList<>();

    private final static int TIAN_LIMIT = 3;
    // private final static int TIAN_LIMIT = 1;// test

    public DengAi() {
        super(4, Nation.WEI);
    }

    @Skill("屯田")
    public void tunTian() {
        // getPriviMsg().clearHeader2();
        // getPriviMsg().setOneTimeInfo1(Text.format("\n💬是否用
        // 屯田,：当你于回合外失去牌后，你可以进行判定，若结果不为红桃，将判定牌置于你的武将牌上，称为\"田\"；" +
        // "你计算与其他角色的距离-X（X为\"田\"的数量）。\n"));
        // 这些明显都是点固定选项的，且不用进私聊操作的操作，全换成自动触发
        if (!isMyRound()) {
            Card c = getGameManager().getCardsHeap().judge(this, new CallbackEven() {
                @Override
                public boolean juge(Card card) {
                    if (card.color() != Color.HEART) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }, false);
            if (c.color() != Color.HEART) {
                getGameManager().getCardsHeap().getJudgeCard();
                tian.add(c);
                String res = Text.format("%s:%s获得1“田”",
                        getSkillHtmlName("屯田"), getPlateName() );
                // //sleep(1000);这里不要sleep了，会导致按键按不下去
                getGameManager().getIo().printlnPublic(res);
                // printlnPriv(this + " now has " + tian.size() + " 田");
            }
        }
    }

    @Override
    public void lostHandCardAction() {
        tunTian();
    }

    @Override
    public void lostEquipment() {
        tunTian();
    }

    @Override
    public int numOfTian() {
        return tian.size();
    }

    @WakeUpSkill("凿险")
    @Override
    public void selfBeginPhase() {

        MsgObj publicMsgObj = MsgObj.newMsgObj(getGameManager());

        if (!isWakenUp() && tian.size() >= TIAN_LIMIT && launchSkillPublicDeepLink(
                publicMsgObj,
                "凿险",
                Text.format("%s 是否 %s：觉醒技，若“田”大于等于3，减1点体力上限，获得“%s”",
                        getHtmlName(), getSkillHtmlName("凿险"), getSkillHtmlName("急袭")),
                "zaoxian1"

        )) {
            // printlnPriv(this + " uses 凿险");
            setMaxHpNotSetCurrent(getMaxHP() - 1);
            wakeUp();
            addActiveSkill("急袭");
            setskipNoticeUsePublic(true);

            String res = Text.format(",%s 已觉醒!<i>开辟险路，奇袭敌军！</i>", getPlateName());
            publicMsgObj.appendText(res);
            //sleep(1000);
            getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);

        }
    }

    @Override
    public void selfEndPhase(boolean fastMode) {

        if (!isZuoCi()) {
            setskipNoticeUsePublic(false);
        }
    }

    @Override
    public void usePhaseBefore() {

        if (isActiveSkill("急袭") && isWakenUp()) {
            getSkillCards().add("急袭");
        }
    }

    @AfterWakeSkill("急袭")
    @Override
    public boolean useSkillInUsePhase(int orderInt) {
        // int orderInt = Integer.valueOf(order) - 1;

        if (orderInt < getSkillCards().size() && getSkillCards().get(orderInt).equals("急袭") && isWakenUp()) {
            getPriviMsg().clearHeader2();
            getPriviMsg().setOneTimeInfo1(Text.format("\n💬急袭：你可以将一张“田”当[顺手牵羊]使用。"));
            Card c = chooseCard(tian, true);
            if (c == null) {
                return true;
            }
            ShunShouQianYang shun = new ShunShouQianYang(getGameManager(), c.color(), c.number());
            if (shun.askTarget(this)) {
                tian.remove(c);
                getGameManager().getCardsHeap().discard(c);
                // printlnPriv(this + " now has " + tian.size() + " 田");
                useCard(shun);

                String res = Text.format("%s %s:<i>偷渡阴平，直取蜀汉！</i>", getPlateName(), getSkillHtmlName("急袭"));
                //sleep(1000);
                getGameManager().getIo().printlnPublic(res, toString());
            }
            return true;
        }
        return false;
    }

    @Override
    public ArrayList<Card> getExtraCards() {
        return tian;
    }

    @Override
    public String getExtraInfo() {
        return "田×"+tian.size() ;
    }

    @Override
    public String name() {
        return "邓艾";
    }

    @Override
    public String skillsDescription() {
        return "屯田：当你于回合外失去牌后，你可以进行判定，若结果不为红桃，将判定牌置于你的武将牌上，称为\"田\"；" +
                "你计算与其他角色的距离-X（X为\"田\"的数量）。\n" +
                "凿险：觉醒技，准备阶段，若“田”的数量大于等于3，你减1点体力上限，然后获得“急袭”。\n" +
                (isWakenUp() ? "急袭——你可以将一张“田”当[顺手牵羊]使用。" : "");
    }
}
