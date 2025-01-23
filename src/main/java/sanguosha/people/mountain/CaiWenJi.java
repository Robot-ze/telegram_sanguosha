package sanguosha.people.mountain;

import java.util.List;

import config.Text;
import msg.CallbackEven;
import msg.MsgObj;
import sanguosha.cards.Card;
import sanguosha.manager.Utils;
import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.ForcesSkill;
import sanguosha.skills.Skill;

public class CaiWenJi extends Person {
    public CaiWenJi() {
        super(3, "female", Nation.QUN);
        // super(1, "female", Nation.QUN);// test
    }

    @Skill("悲歌")
    @Override
    public void otherPersonHurtBySha(Person source, Person target) {

        MsgObj publMsgObj0 = MsgObj.newMsgObj(getGameManager());
        if (source == this) {// 伤害来源是自己就不管
            return;
        }

        if (launchSkillPublicDeepLink(
                publMsgObj0,
                "悲歌",
                Text.format("%s 是否使用 %s",
                        getHtmlName(), getSkillHtmlName("悲歌")),
                "beige1"

        )) {
            String info = Text.format("悲歌:请弃置一张牌，然后令 %s 进行判定，若结果为：" +
                    "红桃，其回复1点体力；方块，其摸两张牌；梅花，伤害来源弃置两张牌；黑桃，伤害来源翻面。\n",
                    target.getHtmlName());
            getPriviMsg().setOneTimeInfo1(info);
            Card c = chooseCard(getCardsAndEquipments(), true);
            if (c == null) {
                return;
            }
            loseCard(c);
            Card d = getGameManager().getCardsHeap().judge(target, new CallbackEven() {
                @Override
                public boolean juge(Card card) {
                    return true;// 总是真，因为所有花色都能对应
                }
            });
            String result = ",判定牌为 " + Card.getColorEmoji(c.color());
            switch (d.color()) {
                case HEART:
                    target.recover(null, 1);
                    result += Text.format(",%s 回复1点体力%s",
                            target.getHtmlName(), target.getHPEmoji());
                    break;
                case DIAMOND:
                    target.drawCards(2);
                    result += Text.format(",%s 摸2张新牌",
                            target.getHtmlName());
                    break;
                case CLUB:
                    int num;
                    if (source.getCardsAndEquipments().size() == 0) {
                        num = 0;
                    } else if (source.getCardsAndEquipments().size() <= 2) {
                        num = getCardsAndEquipments().size();
                        source.loseCard(source.getCardsAndEquipments());
                    } else {
                        num = 2;
                        // 随机选吧，不要让别人点了
                        for (int i = 0; i < num; i++) {
                            List<Card> cards = source.getCardsAndEquipments();
                            Card cc = cards.get(Utils.randint(0, cards.size() - 1));
                            source.loseCard(cc);
                        }

                    }
                    result += Text.format(",%s 随机弃置%s张牌",
                            source.getHtmlName(), num);
                    break;
                case SPADE:
                    source.turnover();
                    result += Text.format(",%s 翻面,跳过下一回合",
                            source.getHtmlName());
                    break;
                default:
                    getGameManager().panic("Unexpected value: " + d.color());
            }
            result += ":<i>悲歌可以当泣，远望可以当归。</i>";
            MsgObj publMsgObj =  getTempActionMsgObjFirstOrder(publMsgObj0,"changeJudge" );
            publMsgObj.appendText(result);
            String privText = "你的悲歌:" + result;
            getGameManager().getIo().delaySendAndDelete(this, privText);
            //sleep(1000);
            getGameManager().getMsgAPI().editCaptionForce(publMsgObj);

        }
    }

    @ForcesSkill("断肠")
    @Override
    public void selfDeadAction(Person source) {
        // 这个技能够难写的，要涉及很多循环内的元素更换，不知道还有没有bug
        boolean dead;// 如果是AI要做以下操作
        if (getAi() != null && getAi().checkDead()) {
            dead = true;
        } else if (checkDead()) {
            dead = true;
        } else {
            dead = false;
        }
        if (dead && source != null && isActiveSkill("断肠")) {
            // printlnPriv(this + " uses 断肠");
            // 变成一个白板人
            getGameManager().addblankPerson(source);

            String res = Text.format("%s 啊，你杀死 %s ,失去所有技能:<i>日东月西兮徒相望，不得相随兮空断肠。</i>",
                    //getSkillHtmlName("断肠"),
                    source.getHtmlName(), getPlateName());
            //sleep(1000);
            getGameManager().getIo().printlnPublic(res, toString());

        }
        return;

    }

    @Override
    public String name() {
        return "蔡文姬";
    }

    @Override
    public String skillsDescription() {
        return "悲歌：当一名角色受到[杀]造成的伤害后，你可以弃置一张牌，然后令其进行判定，若结果为：" +
                "红桃，其回复1点体力；方块，其摸两张牌；梅花，伤害来源弃置两张牌；黑桃，伤害来源翻面。\n" +
                "断肠：锁定技，当你死亡时，杀死你的角色失去所有武将技能。";
    }
}
