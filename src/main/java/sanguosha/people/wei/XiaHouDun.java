package sanguosha.people.wei;

import sanguosha.cards.Card;
import sanguosha.cards.Color;

import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.Skill;

import java.util.List;

import config.Text;
import msg.CallbackEven;
import msg.MsgObj;

public class XiaHouDun extends Person {
    public XiaHouDun() {
        super(4, Nation.WEI);
    }

    @Skill("刚烈")
    @Override
    public void gotHurt(List<Card> cards, Person p, int num) {
        if (p == null) {
            return;
        }
        String code = "ganglie";
        if (launchSkillPublic(
                "刚烈",
                Text.format("是否使用 %s",
                        getSkillHtmlName("刚烈")),
                code)) {
            // if (true) {
            Card c = getGameManager().getCardsHeap().judge(this, new CallbackEven() {
                @Override
                public boolean juge(Card card) {
                    return (card.color() != Color.HEART);
                }
            });
            MsgObj publicMsgObj = getTempActionMsgObjFirstOrder("changeJudge", code);
            if (c.color() != Color.HEART) {
                String result = Text.format(",判定牌为 %s,技能生效", Card.getColorEmoji(c.color()));
                if (publicMsgObj != null) {
                    publicMsgObj.appendText(result);
                    getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
                }

                int option;
                if (p.getCards().size() < 2) {
                    option = 1;
                } else {
                    // 是对手丢牌
                    String code2 = "ganglie2";
                    if (p.launchSkillPublicDeepLink(
                            "丢两张牌",
                            Text.format("%s 你必须1.弃置两张手牌；2.受到1点伤害",
                                    p.getHtmlName()),
                            code2)) {
                        option = 1;
                    } else {
                        option = 2;
                    }
                }

                if (option == 1) {

                    p.getPriviMsg().setOneTimeInfo1("\n因 刚烈 你必须弃置两张手牌");
                    
                    p.loseCard(p.chooseManyCards(2, p.getCards()));
                    result = Text.format("\n%s 弃置两张手牌", p.getPlateName());
                    if (publicMsgObj != null) {
                        publicMsgObj.appendText(result);
                        getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
                        // sleep(3000);
                    }

                } else {
                    p.hurt((Card) null, this, 1);
                    result = Text.format(",%s 受到1点伤害%s", p.getPlateName(), p.getHPEmoji());
                    if (publicMsgObj != null) {
                        publicMsgObj.appendText(result);
                        getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
                        // sleep(3000);
                    }
                }
            } else {
                String result = Text.format(",判定牌为 %s,技能失效", Card.getColorEmoji(c.color()));
                if (publicMsgObj != null) {
                    publicMsgObj.appendText(result);
                    getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
                    // sleep(3000);
                }
            }
        }
    }

    @Override
    public String name() {
        return "夏侯惇";
    }

    @Override
    public String skillsDescription() {
        return "刚烈：当你受到伤害后，你可以进行判定，若结果不为红桃，伤害来源选择一项：1.弃置两张手牌；2.受到你造成的1点伤害。";
    }
}
