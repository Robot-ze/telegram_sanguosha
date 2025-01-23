package sanguosha.people.shu;

import sanguosha.cards.Card;

import sanguosha.manager.Utils;
import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.ForcesSkill;
import sanguosha.skills.Skill;

import java.util.ArrayList;
import java.util.Deque;

import config.Text;
import msg.MsgObj;

public class ZhuGeLiang extends Person {

    public ZhuGeLiang() {
        super(3, Nation.SHU);
    }

    @Skill("观星")
    @Override
    public void selfBeginPhase() {
        MsgObj publicMsgObj = MsgObj.newMsgObj(getGameManager());
        int num = Math.min(getGameManager().getNumPlayers(), 5);
        if (launchSkillPublicDeepLink(
                publicMsgObj,
                "观星",
                Text.format("%s %s你可以观看%s张牌,放回牌堆顶或牌堆底",
                        getHtmlName(), getSkillHtmlName("观星"), num),
                "guanxing")) {
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
            // // getPriviMsg().clearHeader2();
            // getPriviMsg().setOneTimeInfo1(Text.format("\n💬观星：请再次选择放回牌堆底部的牌"));
            // ArrayList<Card> bottom = chooseManyFromProvidedByOrder(view.size(), view);
            // heap.addAll(bottom);
            // }
            if (!view.isEmpty()) {
                heap.addAll(view);
            }
            Utils.checkCardsNum(getGameManager());
            getGameManager().getCardsHeap().setDrawCards(heap);

            String res = Text.format(",已窥天命:<i>知天易，逆天难。</i>", getPlateName(), getSkillHtmlName("观星"));
            publicMsgObj.appendText(res);
            //sleep(1000);
            getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
            // sleep(3000);

        }
    }

    @Override
    public void selfEndPhase(boolean fastMode) {
        if (!isZuoCi()) {
            setskipNoticeUsePublic(false);
        }

    }

    @ForcesSkill("空城")
    @Override
    public boolean hasKongCheng() {
        // 这个没测过，不好测，要双人测
        return isActiveSkill("空城");
    }

    @Override
    public String name() {
        return "诸葛亮";
    }

    @Override
    public String skillsDescription() {
        return "观星：准备阶段，你可以观看牌堆顶的X张牌（X为存活角色数且最多为5），然后以任意顺序放回牌堆顶或牌堆底。\n" +
                "空城：锁定技，若你没有手牌，你不能成为[杀]或[决斗]的目标。";
    }
}
