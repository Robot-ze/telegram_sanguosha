package sanguosha.people.forest;

import java.util.List;

import config.Text;
import msg.MsgObj;
import sanguosha.cards.Card;
import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.Skill;

public class SunJian extends Person {
 

    public SunJian() {
        super(4, Nation.WU);

    }

    @Skill("英魂")
    @Override
    public void selfBeginPhase() {

        if (getHP() < getMaxHP()) {
            // if (true) {// test
            int x = getMaxHP() - getHP();
            MsgObj publicMsgObj = MsgObj.newMsgObj(getGameManager());
            if (launchSkillPublicDeepLink(
                    publicMsgObj,
                    "英魂",
                    Text.format("%s 是否用 %s,:你现在可以选择一名其他角色：\n" +
                            "1.令其摸%s张牌，弃1张牌\n2.令其摸1张牌，弃%s张牌",
                            getHtmlName(), getSkillHtmlName("英魂"), x, x),
                    "yinghun1")) {

                        setskipNoticeUsePublic(true);
                String op1 = "摸" + x + "张牌,扔1张牌";
                String op2 = "摸1张牌,扔" + x+"张牌";

                getPriviMsg().clearHeader2();
                getPriviMsg().setOneTimeInfo1(Text.format("英魂:你现在可以选择一名其他角色：\n" +
                        "1.令其摸%s张牌，弃1张牌；\n2.令其摸1张牌，弃%s张牌", x, x));

                Person p = selectPlayer(false);
                if (p != null) {
                    getPriviMsg().setOneTimeInfo1(Text.format(
                            "英魂:\n1.令其摸%s张牌，然后弃1张牌\n2.令其摸1张牌，然后弃%s张牌", x, x));
                    if (chooseNoNull(op1, op2) == 1) {
                        p.drawCards(x);

                        MsgObj sonPublicMsg = MsgObj.newMsgObj(getGameManager());
                        boolean sonActive = p.launchSkillPublicDeepLink(
                                sonPublicMsg,
                                op1,
                                Text.format(
                                        "%s 你现在必须摸%s张牌，然后弃1张牌", p.getHtmlName(), x),
                                "yinghun2", false);
                        Card c;
                        List<Card> allCard = p.getCardsAndEquipments();
                        if (sonActive) {// 可以选装备牌
                            c = p.chooseCard(allCard);

                        } else {// 强制丢最后一张手牌
                            c = allCard.get(allCard.size() - 1);
                        }

                        p.loseCard(c);
                        String res = Text.format(
                                ",%s 谨遵遗志!<i>以吾魂魄，保佑吾儿之基业</i>", p.getPlateName());
                        sonPublicMsg.appendText(res);
                        getGameManager().getIo().delaySendAndDelete(this, p.getPlateName() + " 已照办");
                        //sleep(1000);
                        getGameManager().getMsgAPI().editCaptionForce(sonPublicMsg);
                    } else {
                        p.drawCard();
                        MsgObj sonPublicMsg = MsgObj.newMsgObj(getGameManager());
                        List<Card> allCard = p.getCardsAndEquipments();
                        int num = allCard.size();
                        if (num <= x) {
                            p.loseCard(allCard);
                            sonPublicMsg.text = Text.format(
                                    "%s 你现在必须摸1张牌，然后弃%s张牌", p.getHtmlName(), x);
                            sonPublicMsg.chatId = getGameManager().getChatId();
                            //sleep(1000);
                            getGameManager().getMsgAPI().sendMsg(sonPublicMsg);
                        } else {
                            boolean sonActive = p.launchSkillPublicDeepLink(
                                    sonPublicMsg,
                                    op2,
                                    Text.format(
                                            "%s 你现在必须摸1张牌，然后弃%s张牌", p.getHtmlName(), x),
                                    "yinghun2", false);

                            List<Card> cs;
                            if (sonActive) {// 可以选装备牌
                                cs = p.chooseManyFromProvided(x, allCard);
                            } else {// 强制丢最后x张手牌
                                cs = allCard.subList(allCard.size() - x, allCard.size() - 1);
                            }
                            p.loseCard(cs);
                        }

                        String res = Text.format(
                                ",%s 冤魂不散!<i>不诛此贼三族！则吾死不瞑目！</i>", p.getPlateName());
                        sonPublicMsg.appendText(res);
                        getGameManager().getIo().delaySendAndDelete(this, p.getPlateName() + " 已照办");
                        //sleep(1000);
                        getGameManager().getMsgAPI().editCaptionForce(sonPublicMsg);
                    }
                }
            }
        }
    }

  

    @Override
    public void selfEndPhase(boolean fastMode) {
        if(!isZuoCi()){
            setskipNoticeUsePublic(false); 
        }
    }

    @Override
    public String name() {
        return "孙坚";
    }

    @Override
    public String skillsDescription() {
        return "英魂：准备阶段，若你已受伤，你可以选择一名其他角色并选择一项：" +
                "1.令其摸X张牌，然后弃置一张牌；2.令其摸一张牌，然后弃置X张牌。（X为你已损失的体力值）";
    }
}
