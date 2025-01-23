package sanguosha.people.shu;

import config.Text;
import msg.CallbackEven;
import msg.MsgObj;
import sanguosha.cards.Card;
import sanguosha.cards.Color;
import sanguosha.cardsheap.CardsHeap;

import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.ForcesSkill;
import sanguosha.skills.Skill;

public class MaChao extends Person {
    public MaChao() {
        super(4, Nation.SHU);
    }

    @ForcesSkill("马术")
    @Override
    public boolean hasMaShu() {
        // println(this + " uses 马术");
        return isActiveSkill("马术");
    }

    @Skill("铁骑")
    @Override
    public boolean shaCanBeShan(Person p) {
        String code = "mashu";

        if (launchSkillPublic("铁骑",
                Text.format("%s %s:<i>全军突击！</i>",
                        getPlateName(),
                        getSkillHtmlName("铁骑")),
                code)) {
            MsgObj puMsgObj0 = getTempActionMsgObj(code);
            Card c = getGameManager().getCardsHeap().judge(this, new CallbackEven() {
                @Override
                public boolean juge(Card card) {
                    return (card.isBlack());
                }
            });
            // //sleep(3000);
            MsgObj puMsgObj = getTempActionMsgObjFirstOrder(puMsgObj0, "changeJudge");
            if (c.isBlack()) {
                String result = Text.format(",%s 此次的杀不能被躲闪",
                        // c.getHtmlNameWithColor(),
                        getPlateName());
                puMsgObj.text = puMsgObj.text + result;

            } else {
                String result = Text.format(",%s 此次的杀可以被躲闪",
                        // c.getHtmlNameWithColor(),
                        getPlateName());
                puMsgObj.text = puMsgObj.text + result;

            }

            puMsgObj.isDeleted = false;
            getGameManager().getMsgAPI().editCaptionForce(puMsgObj);
            puMsgObj.isDeleted = true;
            return !c.isBlack();
        } else {
            // //sleep(3000);
            MsgObj puMsgObj = getTempActionMsgObj(code);
            if (puMsgObj == null) {
                return true;
            }
            String result = Text.format("\n%s 放弃使用 铁骑",
                    getPlateName());
            puMsgObj.text = puMsgObj.text + result;
            puMsgObj.isDeleted = false;
            getGameManager().getMsgAPI().editCaptionForce(puMsgObj);
            puMsgObj.isDeleted = true;
        }
        return true;
    }

    @Override
    public String name() {
        return "马超";
    }

    @Override
    public String skillsDescription() {
        return "马术：锁定技，你计算与其他角色的距离-1。\n" +
                "铁骑：当你使用[杀]指定目标后，你可以进行判定，若结果为红色，该角色不能使用[闪]。";
    }
}
