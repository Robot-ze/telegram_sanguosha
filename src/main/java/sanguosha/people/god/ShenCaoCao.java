package sanguosha.people.god;

import java.util.List;

import config.Text;
import msg.MsgObj;
import sanguosha.cards.Card;
import sanguosha.people.Person;
import sanguosha.skills.Skill;

public class ShenCaoCao extends God {
    public ShenCaoCao() {
        super(3, null);
    }

    @Skill("归心")
    @Override
    public void gotHurt(List<Card> cards, Person p, int num) {
        MsgObj publicMsgObj =MsgObj.newMsgObj(getGameManager());
        if (launchSkillPublic(
            publicMsgObj,
            "归心",
            Text.format("是否使用 %s", getSkillHtmlName("归心")),
            "scc_guixin"
            )) {
            int count=0;
            for (Person p2: getGameManager().getPlayersBeginFromPlayer( this)) {
                if (p2 != this &&
                        !(p2.getCardsAndEquipments().isEmpty() && p2.getJudgeCards().isEmpty())) {
                    Card c = randomChooseTargetCards(p2);
                    p2.loseCard(c, false);
                    addCard(c);
                    count++;
                }
            }
            turnover();

            String res =Text.format(",获得%s位角色的各1张手牌,翻面", count);
            publicMsgObj.appendText(res);
            //sleep(1000);
            getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
        }
    }

    @Skill("飞影")
    @Override
    public boolean hasFeiYing() {
        return true;
    }

    @Override
    public String name() {
        return "神曹操";
    }

    @Override
    public String skillsDescription() {
        return "归心：当你受到1点伤害后，你可以获得每名其他角色区域里的一张牌，然后你翻面。\n" +
                "飞影：锁定技，其他角色计算与你的距离+1。";
    }
}
