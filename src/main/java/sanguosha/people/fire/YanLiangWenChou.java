package sanguosha.people.fire;

import config.Text;
import msg.CallbackEven;
import sanguosha.cards.Card;
import sanguosha.cards.strategy.JueDou;
import sanguosha.cardsheap.CardsHeap;
import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.Skill;

public class YanLiangWenChou extends Person {
    private Card judgeCard;

    public YanLiangWenChou() {
        super(4, Nation.QUN);
    }

    @Override
    public void drawPhase(boolean fastMode) {
        getPriviMsg().setOneTimeInfo1("双雄：摸牌阶段，你可以改为进行判定，你获得生效后的判定牌，" +
                "然后本回合你可以将与判定结果颜色不同的一张手牌当[决斗]使用。");
        if (!  fastMode&&launchSkillPriv("[双雄]抽1张判定牌")) {
            Card c = getGameManager().getCardsHeap().judge(this, new CallbackEven() {
                @Override
                public boolean juge(Card card) {
                    return true;
                }
            });
            addCard(getGameManager().getCardsHeap().getJudgeCard());
            judgeCard = c;
            // setHasUsedSkill1(true);
            // if (isActiveSkill("双雄") && judgeCard != null && hasNotUsedSkill1()) {
            // getSkillCards().add("双雄");
            // }
            // //sleep(3000);
            String colorString = judgeCard.isRed() ? "黑色" : "红色";
            String result = Text.format("%s,本回合可以将一张%s手牌当[决斗]使用",

                    getSkillHtmlName("双雄"),
                    colorString);
            getGameManager().getIo().printlnPublic(result, toString());
            getGameManager().getIo().delaySendAndDelete(this, result);
            return;
        }
        super.drawPhase(  fastMode);
    }

    @Override
    public void usePhaseBefore() {

        if (isActiveSkill("双雄") && judgeCard != null
                && ((judgeCard.isRed() && hasBlackHandCard()) ||
                        (judgeCard.isBlack() && hasRedHandCard()))
                && hasNotUsedSkill1()) {
            getSkillCards().add("双雄");
        }
    }

    @Skill("双雄")
    @Override
    public boolean useSkillInUsePhase(int orderInt) {
        // int orderInt = Integer.valueOf(order) - 1;
        // System.out.println("getSkillCards()="+getSkillCards());
        if (orderInt < getSkillCards().size() && getSkillCards().get(orderInt).equals("双雄") && hasNotUsedSkill1()) {

            String colorString = judgeCard.isRed() ? "黑色" : "红色";
            String result = Text.format("请选择一张%s手牌当[决斗]使用",
                    colorString);
            getPriviMsg().setOneTimeInfo1(result);
            Card c = requestRedBlack(judgeCard.isRed() ? "black" : "red");
            if (c == null) {
                return false;
            }
            JueDou jd = new JueDou(getGameManager(), c.color(), c.number());
            jd.setIsFake(true);
            jd.addReplaceCard(c);
            if (jd.askTarget(this)) {
                String res = Text.format("<i>快来与我等决一死战！</i>");
                getGameManager().getIo().printlnPublic(res, toString());

                useCard(jd);
                setHasUsedSkill1(true);
            } else {
                addCard(getGameManager().getCardsHeap().retrieve(c), false);
            }
            return true;
        }
        return false;
    }

    @Override
    public void selfEndPhase(boolean fastMode) {
        judgeCard = null;
    }

    @Override
    public String name() {
        return "颜良文丑";
    }

    @Override
    public String skillsDescription() {
        return "双雄：摸牌阶段，你可以改为进行判定，你获得生效后的判定牌，" +
                "然后本回合你可以将与判定结果颜色不同的一张手牌当[决斗]使用。";
    }
}
