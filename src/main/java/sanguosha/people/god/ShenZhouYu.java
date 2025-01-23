package sanguosha.people.god;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import config.Text;
import msg.MsgObj;
import sanguosha.cards.Card;
import sanguosha.cards.Color;
import sanguosha.cards.basic.HurtType;
import sanguosha.people.Person;
import sanguosha.skills.RestrictedSkill;
import sanguosha.skills.Skill;

public class ShenZhouYu extends God {

    public ShenZhouYu() {
        super(4, null);
    }

    // @Override
    // public void drawCards(int num) {// test
    //     // TODO Auto-generated method stub
    //     super.drawCards(num + 8);
    // }

    @Skill("琴音")
    @Override
    public int throwPhase(boolean fastMode) {
        int num = getCards().size() - getHP();
        if (num > 0) {
            getPriviMsg().setOneTimeInfo1(Text.format("💬你需要舍弃 %s 张牌\n", num));

            ArrayList<Card> cs;
            if (fastMode) {// 快速模式直接丢掉后面的牌
                cs = new ArrayList<>();//
                for (int i = 0; i < num; i++) {
                    cs.add(getCards().get(getCards().size() - 1 - i));
                }
            } else {
                cs = chooseManyFromProvided(num, getCards(),true);
            }

            if (cs.size() < num) {// 如果选不够，强制从前面丢起
                for (Card c : getCards()) {
                    if (cs.indexOf(c) >= 0) {
                        continue;
                    }
                    cs.add(c);
                    if (cs.size() >= num) {
                        break;
                    }
                }
            }
            loseCard(cs);
            for (Person p : getGameManager().getPlayersBeginFromPlayer(this)) {
                p.otherPersonThrowPhase(this, cs);
            }
        }
        if (num >= 2) {
            MsgObj publicMsgObj = MsgObj.newMsgObj(getGameManager());
            // getPriviMsg().setOneTimeInfo1("💬琴音:弃牌超两张,你可以令所有人受伤或恢复");

            if (launchSkillPublicDeepLink(
                    publicMsgObj,
                    "琴音",
                    "💬琴音:弃牌超两张,你可以令所有人受伤或恢复",
                    "qinyin1")) {
                if (chooseNoNull("急促的琴声", "柔和的琴声") == 1) {
                    String res = Text.format(",%s 所有人受1点伤:<i>急促的琴声</i>\n",
                            getSkillHtmlName("琴音"));
                    for (Person p : getGameManager().getPlayersBeginFromPlayer(this)) {
                        res += p.getHtmlName() + ":" + p.getHPEmojiMinus(1) + " ";
                    }
                    publicMsgObj.appendText(res);
                    getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);

                    List<Person>  temp=new ArrayList<>(getGameManager().getPlayersBeginFromPlayer(this));
                    for (Person p : temp) {
                        p.hurt((Card) null, this, 1);
                    }
                } else {
                    String res = Text.format(",%s 所有人恢复1点:<i>柔和的琴声</i>\n",
                            getSkillHtmlName("琴音"));
                    publicMsgObj.appendText(res);
                    List<Person>  temp=new ArrayList<>(getGameManager().getPlayersBeginFromPlayer(this));
                    for (Person p : temp) {
                        res += p.getHtmlName() + ":" + p.getHPEmojiMinus(-1) + " ";
                    }
                    getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);

                    for (Person p : getGameManager().getPlayersBeginFromPlayer(this)) {
                        p.recover(null, 1);
                    }
                }
            }
        }
        return num;
    }

    @Override
    public void usePhaseBefore() {

        if (isActiveSkill("业炎") && hasNotUsedSkill1() && check4color(getCards())) {
            getSkillCards().add("业炎");
        }
    }

    @RestrictedSkill("业炎")
    @Override
    public boolean useSkillInUsePhase(int orderInt) {
        if (orderInt < getSkillCards().size() && getSkillCards().get(orderInt).equals("业炎") && hasNotUsedSkill1()) {

            String res = "💬你可以选择至多三名角色，对这些角色造成共计至多3点火焰伤害。若一名角色分配2点或更多伤害,你须先弃置四张花色各不相同的手牌并失去3点体力";
            getPriviMsg().setOneTimeInfo1(res);
            List<Person> persons = new ArrayList<>();
            for (Person p : getGameManager().getPlayersBeginFromPlayer(this)) {
                if (p != this) {
                    persons.add(p);
                }
            }

            List<Person> plist = chooseManyFromProvidedByOrder(0, persons);
            if (plist.size() == 0 || plist.size() > 3) {
                getGameManager().getIo().delaySendAndDelete(this, "💬人数错误");

                return false;
            }

            int[][] hurtN = new int[][] {
                    { 3 }, { 2, 1 }, { 1, 1, 1 },
            };
            int[] hurts = hurtN[plist.size() - 1];

            if (plist.size() < 3) {
                String res2 = Text.format("💬%s 将受超过2点🔥伤,你须先弃置4张花色各不相同的手牌,并失去3点体力,",
                        plist.get(0).getPlateName());
                getPriviMsg().setOneTimeInfo1(res2);
                List<Card> chooses = chooseManyFromProvided(4, getCards(), true);
                if (chooses.size() != 4 || !check4color(chooses)) {
                    getGameManager().getIo().delaySendAndDelete(this, "💬牌数不对或没有选足够花色的牌");
                    return false;
                }
                loseCard(chooses);
                loseHP(3, null);
            }
            MsgObj publicMsgObj = MsgObj.newMsgObj(getGameManager());
            publicMsgObj.setImg(toString());
            publicMsgObj.chatId = getGameManager().getChatId();
            publicMsgObj.text = Text.format("%s:<i>（燃烧声）聆听吧，这献给你的镇魂曲！</i>\n", getSkillHtmlName("业炎"));
            if (plist.size() < 3) {
                publicMsgObj.appendText(Text.format("%s 受3点伤:%s\n", getHtmlName(), getHPEmoji()));
            }
            for (int i = 0; i < plist.size(); i++) {
                Person p = plist.get(i);
                boolean isPreLink = p.isLinked();// 为什么要这样缓存，因为已伤害了这个值就会变

                int realNum = p.hurt((Card) null, this, hurts[i], HurtType.fire);
                publicMsgObj.appendText(Text.format("%s 受%s点🔥伤:%s\n", p.getHtmlName(), realNum, p.getHPEmoji()));
                if (isPreLink) {
                    getGameManager().linkHurt(publicMsgObj, null, this, p, realNum, HurtType.fire);
                }
            }

            setHasUsedSkill1(true);
            getGameManager().getMsgAPI().sendImg(publicMsgObj);
            return true;
        }
        return false;
    }

    private boolean check4color(List<Card> cards) {
        Set<Color> colors = new HashSet<>();
        for (Card c : cards) {
            colors.add(c.color());
        }
        return colors.size() >= 4;
    }

    @Override
    public String name() {
        return "神周瑜";
    }

    @Override
    public String skillsDescription() {
        return "琴音：弃牌阶段结束时，若你于此阶段内弃置过你的至少两张手牌，则你可以选择一项：" +
                "1.令所有角色各回复1点体力；2.令所有角色各失去1点体力。\n" +
                "业炎：限定技，出牌阶段，你可以选择至多三名角色，对这些角色造成共计至多3点火焰伤害。\n" +
                "若你将对一名角色分配2点或更多火焰伤害，你须先弃置四张花色各不相同的手牌并失去3点体力。";
    }
}
