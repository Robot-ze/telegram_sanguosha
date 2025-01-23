package sanguosha.people.mountain;

import sanguosha.cards.Card;
import sanguosha.cards.Equipment;
import sanguosha.cards.equipments.horses.MinusOneHorse;
import sanguosha.cards.equipments.horses.PlusOneHorse;
import sanguosha.manager.Utils;
import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.Skill;
import components.TimeLimit;

import java.util.ArrayList;
import java.util.List;

import config.Config;
import config.Text;
import msg.MsgObj;

public class ZhangZhaoZhangHong extends Person {
    public ZhangZhaoZhangHong() {
        super(3, Nation.WU);
    }

    @Override
    public void usePhaseBefore() {

        if (isActiveSkill("直谏") && getCards().size() > 0) {
            getSkillCards().add("直谏");
        }
    }

    @Skill("直谏")
    @Override
    public boolean useSkillInUsePhase(int orderInt) {
        // int orderInt = Integer.valueOf(order) - 1;

        if (orderInt < getSkillCards().size() && getSkillCards().get(orderInt).equals("直谏")) {
            getPriviMsg().clearHeader2();
            getPriviMsg().setOneTimeInfo1(Text.format("\n💬直谏：出牌阶段，你可以将手牌中的一张装备牌置于其他角色的装备区里，然后摸一张牌。\n"));

            List<Card> equipmentInHands = new ArrayList<>();
            for (Card c : getCards()) {
                if (c instanceof Equipment) {
                    equipmentInHands.add(c);
                }
            }

            if (equipmentInHands.size() <= 0) {
                String res = "直谏失败:你没有装备牌";
                getGameManager().getIo().delaySendAndDelete(this, res);
                return false;
            }
            getPriviMsg().setOneTimeInfo1("直谏:请你选择一张装备牌");
            Card c = chooseCard(equipmentInHands, true);

            if (c == null) {
                return true;
            }
            getPriviMsg().setOneTimeInfo1("直谏:请你选择一个玩家");

            Person p = selectPlayer(false);

            if (p == null) {
                return true;
            }
            // printlnPriv(p + " puts on " + c);
            // p.getEquipments().put(((Equipment) c).getEquipType(), (Equipment) c);
            loseCard(c, false);
            p.putOnEquipment(c);
            drawCard();

            String res = Text.format("%s 把 %s 置于 %s:<i>老臣是为你好。</i>",
                    getPlateName(),
                    c.getHtmlNameWithColor(),
                    p.getHtmlName());
            //sleep(1000);
            getGameManager().getIo().printlnPublic(res, toString());
        }
        return false;
    }

    @Skill("固政")
    @Override
    public void otherPersonThrowPhase(Person p, ArrayList<Card> cards) {
        Utils.assertTrue(!cards.isEmpty(), "throw cards are empty");
        if (p == this) {
            return;
        }
        getPriviMsg().clearHeader2();
        getPriviMsg().setOneTimeInfo1(Text.format("\n💬是否用 固政：其他角色的弃牌阶段结束时，你可以将此阶段中的一张弃牌返还给该角色，然后你获得其余的弃牌。"));
        MsgObj publicMsgObj = MsgObj.newMsgObj(getGameManager());
        if (cards == null || cards.size() == 0) {
            return;
        }
        if (launchSkillPublicDeepLink(
                publicMsgObj,
                "固政",
                Text.format("%s 是否用 %s", getHtmlName(), getSkillHtmlName("固政")),
                "guzheng1"

        )) {

            getPriviMsg().setOneTimeInfo1(Text.format("\n💬固政：选择一张弃牌返还给该角色，然后你获得其余的%s张牌。", cards.size() - 1));

            Card c = chooseCard(cards, true);
            if (c == null) {
                return;
            }
            getGameManager().getCardsHeap().retrieve(cards);
            p.addCard(c);
            cards.remove(c);
            addCard(cards);
            String res = ",<i>今当稳固内政，以御外患。</i>";
            publicMsgObj.appendText(res);
            //sleep(1000);
            getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
        }
    }

    @Override
    public String name() {
        return "张昭张纮";
    }

    @Override
    public String skillsDescription() {
        return "直谏：出牌阶段，你可以将手牌中的一张装备牌置于其他角色的装备区里，然后摸一张牌。\n" +
                "固政：其他角色的弃牌阶段结束时，你可以将此阶段中的一张弃牌返还给该角色，然后你获得其余的弃牌。";
    }
}
