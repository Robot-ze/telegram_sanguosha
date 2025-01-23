package sanguosha.people.forest;

import sanguosha.cards.Card;
import sanguosha.cards.Color;

import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.ForcesSkill;
import sanguosha.skills.Skill;

import java.util.ArrayList;
import java.util.Deque;

import config.Text;

public class MengHuo extends Person {
    public MengHuo() {
        super(4, Nation.SHU);
    }

    @ForcesSkill("祸首")
    @Override
    public boolean hasHuoShou() {
        // println(this + " uses 祸首");
        return isActiveSkill("祸首");
    }

    @Skill("再起")
    @Override
    public void drawPhase(boolean fastMode) {
        int num = getMaxHP() - getHP();

        if (num <= 0) {
            super.drawPhase(  fastMode);
            return;
        }
        getPriviMsg().clearHeader2();
        getPriviMsg().setOneTimeInfo1(Text.format("💬是否用 再起:摸牌时你可以改为亮出牌堆顶的%s张牌 然后回复红桃牌数量的体力，获得其余的牌。", num));
        if (! fastMode&&launchSkillPriv("再起")) {
            Deque<Card> heap = getGameManager().getCardsHeap().getDrawCards(getMaxHP() - getHP());
            ArrayList<Card> cs = new ArrayList<>() {
            };
            for (int i = 0; i < (getMaxHP() - getHP()); i++) {
                cs.add(heap.pop());
            }
            getGameManager().getCardsHeap().getDrawCards(0).removeAll(cs);
            // printlnPriv("再起 cards:");
            // getGameManager().getIo().printCardsPublic(cs);
    
            int r_num = 0;
            int g_num = 0;
            for (Card c : cs) {
                //cardString += "[" + c.getHtmlNameWithColor() + "]";
                if (c.color() == Color.HEART) {
                    r_num++;
                    recover(null, 1);
                    getGameManager().getCardsHeap().discard(c);
                } else {
                    g_num++;
                    addCard(c);
                }
            }

            String res = Text.format("获得%s张牌并恢复体力%s点%s:<i>我怎么会打不过只会纸上谈兵的书生？再来！</i>",
 
                    g_num,
                    r_num,
                    getHPEmoji());
            getGameManager().getIo().delaySendAndDelete(this, res);
            //sleep(1000);
            getGameManager().getIo().printlnPublic(res, toString());
        }

        else {
            super.drawPhase(  fastMode);
        }
    }

    @Override
    public String name() {
        return "孟获";
    }

    @Override
    public String skillsDescription() {
        return "祸首：锁定技，[南蛮入侵]对你无效；当其他角色使用[南蛮入侵]指定目标后，你代替其成为此牌造成的伤害的来源。\n" +
                "再起：摸牌阶段，若你已受伤，你可以改为亮出牌堆顶的X张牌（X为你已损失的体力值+1），" +
                "然后回复等同于其中红桃牌数量的体力，并获得其余的牌。";
    }
}
