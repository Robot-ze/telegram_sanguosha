package sanguosha.people.wu;

import config.Text;
import db.ImgDB;
import msg.MsgObj;
import sanguosha.cards.Card;
import sanguosha.cards.Color;
import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.Skill;

public class ZhouYu extends Person {
    public ZhouYu() {
        super(3, Nation.WU);
    }

    @Skill("英姿")
    @Override
    public void drawPhase(boolean fastMode) {
        String res = Text.format("英姿：摸牌阶段，你可以多摸一张牌。");
        getPriviMsg().setOneTimeInfo1(res);
        if (  fastMode&&launchSkillPriv("英姿")) {
            drawCards(3);

            res = Text.format("%s %s 多摸一张牌:<i>哈哈哈哈！</i>", getPlateName(), getSkillHtmlName("英姿"));

            getGameManager().getIo().printlnPublic(res, toString());
            // sleep(3000);
        } else {
            super.drawPhase(  fastMode);
        }
    }

    @Override
    public void usePhaseBefore() {

        if (isActiveSkill("反间") && getCards().size() >= 0 && hasNotUsedSkill1()) {
            getSkillCards().add("反间");
        }
    }

    @Skill("反间")
    @Override
    public boolean useSkillInUsePhase( int orderInt) {
        //int orderInt = Integer.valueOf(order) - 1;

        if (orderInt < getSkillCards().size() && getSkillCards().get(orderInt).equals("反间") && hasNotUsedSkill1()) {
            // printlnPriv(this + " uses 反间");
            String res = "反间：出牌阶段限一次，你可以赠一张牌与其他角色，并令其猜颜色，若猜错，则你对其造成1点伤害。请选择一个角色";
            getPriviMsg().setOneTimeInfo1(res);

            Person p = selectPlayer();
            if (p == null) {
                return false;
            }
            res = "反间：请继续选择一张牌";
            getPriviMsg().setOneTimeInfo1(res);
            Card c = chooseCard(getCards(), false);

            setHasUsedSkill1(true);
            Color[] clrList = new Color[] { Color.SPADE, Color.CLUB, Color.HEART, Color.DIAMOND };
            boolean active = false;
            MsgObj publicMsg = MsgObj.newMsgObj(getGameManager());

            if (p .isAI()) {
                active = true;
                publicMsg.chatId = getGameManager().getChatId();
                ImgDB.setImg(publicMsg, toString());
                publicMsg.text = Text.format("%s,%s 对你使用 %s: 赠一张牌，你猜颜色，若猜错，则受1点伤害:<i>挣扎吧！在血和暗的深渊里！</i>",
                        p.getHtmlName(),
                        getPlateName(),
                        getSkillHtmlName("反间"));
                getGameManager().getMsgAPI().sendImg(publicMsg);
            } else {
                active = getGameManager().getMsgAPI().noticeAndAskPublic(
                        publicMsg, p,
                        Text.format("%s,%s 对你使用 %s: 赠一张牌，你猜颜色，若猜错，则受1点伤害",
                                p.getHtmlName(),
                                getPlateName(),
                                getSkillHtmlName("反间")),
                        "猜颜色", "fanjian",true);
                // 去除按键
                getGameManager().getMsgAPI().clearButtons(publicMsg);
            }
            p.addCard(c);

            if (active) {
                p.getPriviMsg().setOneTimeInfo1("请猜牌的颜色");
                int clr = p.chooseNoNull("♠️", "♣️", "♥️", "♦️");

                if (c.color() != clrList[clr - 1]) {
                    int real = p.hurt((Card) null, this, 1);

                    res = Text.format("\n%s 猜是 %s,牌为 %s,受%s点伤害%s",
                            p.getPlateName(),
                            Card.getColorEmoji(clrList[clr - 1]),
                            real,
                            c.getHtmlNameWithColor(), p.getHPEmoji()

                    );

                    publicMsg.appendText(res);
                    getGameManager().getMsgAPI().editCaptionForce(publicMsg);
                    // sleep(3000);

                    return true;
                } else {
                    res = Text.format("\n%s 猜是 %s,牌为 %s,猜对了",
                            p.getPlateName(),
                            Card.getColorEmoji(clrList[clr - 1]),
                            c.getHtmlNameWithColor());

                    publicMsg.appendText(res);
                    getGameManager().getMsgAPI().editCaptionForce(publicMsg);
                    // sleep(3000);
                }
            } else {
                int real = p.hurt((Card) null, this, 1);

                res = Text.format("\n%s 放弃猜牌,受%s点伤害%s",
                        p.getPlateName(),
                        real,
                        p.getHPEmoji());

                publicMsg.appendText(res);
                getGameManager().getMsgAPI().editCaptionForce(publicMsg);
                // sleep(3000);
            }

            return false;
        }
        return false;
    }

    @Override
    public String name() {
        return "周瑜";
    }

    @Override
    public String skillsDescription() {
        return "英姿：摸牌阶段，你可以多摸一张牌。\n" +
                "反间：出牌阶段限一次，你可以令一名其他角色选择一种花色，然后该角色获得你的一张手牌并展示之，若此牌的花色与其所选的花色不同，则你对其造成1点伤害。";
    }
}
