package sanguosha.people.mountain;

import sanguosha.cards.Card;
import sanguosha.cards.basic.Sha;
import sanguosha.manager.Utils;
import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.AfterWakeSkill;
import sanguosha.skills.Skill;
import sanguosha.skills.WakeUpSkill;
import components.TimeLimit;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import config.Config;
import config.Text;
import msg.MsgObj;

public class JiangWei extends Person {
 

    public JiangWei() {
        super(4, Nation.SHU);
    }

    @Override
    public void usePhaseBefore() {

        if (isActiveSkill("挑衅") && hasNotUsedSkill1()) {
            getSkillCards().add("挑衅");
        }
    }

    @Skill("挑衅")
    @Override
    public boolean useSkillInUsePhase(int orderInt) {
        // int orderInt = Integer.valueOf(order) - 1;
        if (orderInt < getSkillCards().size() && getSkillCards().get(orderInt).equals("挑衅") && hasNotUsedSkill1()) {
            // println(this + " uses 挑衅");
            List<Person> personCanHitSelf = new ArrayList<>();
            for (Person p : getGameManager().getPlayers()) {
                if (p == this) {
                    continue;
                }
                if (getGameManager().calDistance(p, this) <= p.getShaDistance()) {
                    personCanHitSelf.add(p);
                }
            }
            getPriviMsg().clearHeader2();
            getPriviMsg().setOneTimeInfo1(Text.format("\n💬挑衅：出牌阶段限一次，你可以选择一名能攻击你的角色，然后除非该角色对你使用一张[杀]，否则TA弃置一张牌。\n"));

            Person p = selectPlayer(personCanHitSelf);
            if (p == null) {
                return false;
            }

            setHasUsedSkill1(true);

            String info = Text.format("%s %s %s:<i>汝等小儿，可敢杀我？</i>",
                    getPlateName(), p.getSkillHtmlName("挑衅"), p.getHtmlName());

            getGameManager().getIo().printlnPublic(info, toString());

            Sha sha = p.requestSha(this);
            if (sha == null) {
                if (p.getCardsAndEqSize() <= 0) { // 别人打出无懈导致手牌没有了，也视作使用成功
                    return true;
                }
                List<Card> cardsAndEquips = p.getCardsAndEquipments();
                Card c = cardsAndEquips.get(Utils.randint(0, cardsAndEquips.size() - 1));
                p.loseCard(c);// 是姜维让别人丢牌
                try {
                    return true;
                } finally {
                    String res = Text.format(",未出战，弃牌1张");
                    MsgObj publicMsgObj = p.getTempActionMsgObj("sha");
                    publicMsgObj.appendText(res);
                    //sleep(1000);
                    getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
                }

            }
            getGameManager().getCardsHeap().retrieve(sha);
            sha.setTarget(this);
            sha.setSource(p);
            p.useCard(sha);
            if (sha.isNotTaken()) {
                throwCard(sha);
            } else {
                sha.setTaken(false);
            }
            return true;
        }
        return false;
    }

    @AfterWakeSkill("观星")
    public void guanXing() {
        MsgObj publicMsgObj = MsgObj.newMsgObj(getGameManager());
        int num = Math.min(getGameManager().getNumPlayers(), 5);
        if (launchSkillPublicDeepLink(
                publicMsgObj,
                "观星",
                Text.format("%s %s你可以观看%s张牌,放回牌堆顶或牌堆底",
                        getHtmlName(), getSkillHtmlName("观星"), num),
                "guanxing2")) {
                    setskipNoticeUsePublic(true);

            Deque<Card> heap = getGameManager().getCardsHeap().getDrawCards(num);

            ArrayList<Card> view = new ArrayList<>();
            for (int i = 0; i < num; i++) {
                view.add(heap.pop());
            }
            // heap = new ArrayList<>(heap.subList(num, heap.size()));

            // getPriviMsg().clearHeader2();
            getPriviMsg().setOneTimeInfo1(Text.format("\n💬观星：请选择放回牌堆顶部的牌,其余将直接放回牌底"));

            ArrayList<Card> top = chooseManyFromProvidedByOrder(0, view);
            if (top != null) {
                view.removeAll(top);
                for (int i = top.size() - 1; i >= 0; i--) {// 要倒序插进去
                    heap.addFirst(top.get(i));
                }
                // heap.addAll( top);
            }
            // if (!view.isEmpty()) {
            //     // getPriviMsg().clearHeader2();
            //     getPriviMsg().setOneTimeInfo1(Text.format("\n💬观星：请再次选择放回牌堆底部的牌"));
            //     ArrayList<Card> bottom = chooseManyFromProvidedByOrder(view.size(), view);
            //     heap.addAll(bottom);
            // }
            if (!view.isEmpty()) {
                heap.addAll(view);
            }
            Utils.checkCardsNum(getGameManager());
            getGameManager().getCardsHeap().setDrawCards(heap);

            String res = Text.format(",已窥天命:<i>丞相，請示吾當何以為計？</i>", getPlateName(), getSkillHtmlName("观星"));
            publicMsgObj.appendText(res);
            //sleep(1000);
            getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
            // sleep(3000);

        }

    }

 

    @Override
    public void selfEndPhase(boolean fastMode) {
        if(!isZuoCi()){
            setskipNoticeUsePublic(false); 
        }
    }

    @WakeUpSkill("志继")
    public void zhiJi(MsgObj publiMsgObj) {
        getPriviMsg().clearHeader2();
        getPriviMsg().setOneTimeInfo1(Text.format("\n💬志继：觉醒技，准备阶段，若你没有手牌，你回复1点体力或摸两张牌，然后减1点体力上限，获得“观星”。\n"));

        if (getHP() == getMaxHP() || chooseNoNull("恢复1点体力", "摸两张牌") == 2) {
            drawCards(2);
            String res =",抽2张牌,已觉醒!<i>末将，定不负丞相嘱托。</i>";
            publiMsgObj.appendText(res);
        } else {
            recover(null, 1);
            String res = Text.format(",恢复1体力,已觉醒!<i>末将，定不负丞相嘱托。</i>",getHPEmoji());
            publiMsgObj.appendText(res);
        }
        setMaxHpNotSetCurrent(getMaxHP() - 1);
        wakeUp();
        addActiveSkill("观星");

        //sleep(1000);
        getGameManager().getMsgAPI().editCaptionForce(publiMsgObj);
    }

    @Override
    public void selfBeginPhase() {

        MsgObj publicMsgObj = MsgObj.newMsgObj(getGameManager());
        if (!isWakenUp() && getCards().isEmpty()) {
            if (launchSkillPublicDeepLink(
                    publicMsgObj,
                    "志继",
                    Text.format("%s 是否用 %s", getHtmlName(), getSkillHtmlName("志继")),
                    "zhiji3")) {
                zhiJi(publicMsgObj);
            }
        }
 
        if (isWakenUp()) {
            guanXing();
        }
    }

    @Override
    public String name() {
        return "姜维";
    }

    @Override
    public String skillsDescription() {
        return "挑衅：出牌阶段限一次，你可以选择一名攻击范围内含有你的角色，然后除非该角色对你使用一张[杀]，否则你弃置其一张牌。\n" +
                "志继：觉醒技，准备阶段，若你没有手牌，你回复1点体力或摸两张牌，然后减1点体力上限，获得“观星”。\n" +
                (isWakenUp() ? "观星：准备阶段，你可以观看牌堆顶的X张牌（X为全场角色数且最多为5），然后以任意顺序放回牌堆顶或牌堆底。" : "");
    }
}
