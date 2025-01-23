package sanguosha.people.wind;

import sanguosha.cards.Card;
import sanguosha.cards.Color;
import sanguosha.cards.basic.HurtType;
import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.Skill;

import java.util.ArrayList;
import java.util.List;

import config.Text;
import msg.MsgObj;

public class XiaoQiao extends Person {
    public XiaoQiao() {
        super(3, "female", Nation.QUN);
    }

    @Skill("天香")
    @Override
    public int hurt(List<Card> card, Person source, int num, HurtType type) {
        MsgObj publicMsgObj = MsgObj.newMsgObj(getGameManager());
        if (this.getUser() == null) { // AI
            return super.hurt(card, source, num, type);
        } else if (launchSkillPublicDeepLink(
                publicMsgObj,
                "天香",
                Text.format("是否使用 %s", getSkillHtmlName("天香")),
                "tianxiang"

        )) {//

            getPriviMsg().setOneTimeInfo1(Text.format("使用天香:请弃置一张%s牌", Card.getColorEmoji(Color.HEART)));
            Card c = requestColor(Color.HEART);
            if (c != null) {
                getPriviMsg().setOneTimeInfo1(Text.format("使用天香:请选择一名转移角色"));
                Person p = selectPlayer();
                if (p != null) {
                    int value = p.hurt(card, source, num, type);
                    int cardNum = p.getMaxHP() - p.getHP();
                    p.drawCards(cardNum);

                    String res = Text.format(",%s摸%s张牌, 并受%s转移伤害%s:<i>替我挡着！奴家会好好待你</i>",
                            p.getHtmlName(),
                            cardNum,
                            value,
                            p.getHPEmoji());
                    publicMsgObj.text = publicMsgObj.text + res;
                    // System.out.println("publicMsgObj.text ="+publicMsgObj.text );
                    //sleep(1000);
                    getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
                    // sleep(3000);
                    // getGameManager().getIo().printlnPublic(res, toString());
                    return 0;
                } else {
                    addCard(c);
                }
            }

        }
        return super.hurt(card, source, num, type);
    }

    @Override
    public Card requestColor(Color color) {
        getPriviMsg().setOneTimeInfo2(
                Text.format("你的%s牌已被当成%s牌",
                        Card.getColorEmoji(Color.SPADE),
                        Card.getColorEmoji(Color.HEART)));
        return super.requestColor(color);
    }

    @Skill("红颜")
    @Override
    public boolean hasHongYan() {
        return isActiveSkill("红颜");
    }

    @Override
    public String name() {
        return "小乔";
    }

    @Override
    public String skillsDescription() {
        return "天香：当你受到伤害时，你可以弃置一张红桃手牌并选择一名其他角色。若如此做，你将此伤害转移给该角色，然后其摸X张牌（X为该角色已损失的体力值）。\n" +
                "红颜：锁定技，你的黑桃牌视为红桃牌。";
    }
}
