package sanguosha.people.forest;

import sanguosha.cards.Card;
import sanguosha.cards.Color;

import sanguosha.people.Identity;
import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.ForcesSkill;
import sanguosha.skills.KingSkill;
import sanguosha.skills.Skill;
import java.util.Set;

import config.Text;
import msg.CallbackEven;
import msg.MsgObj;;

public class DongZhuo extends Person {
    /**
     * 默认开局you
     */
    boolean hasAddHp = true;
    int hasMinusMaxHp = 0;

    public DongZhuo() {
        super(8, Nation.QUN);

    }

    @Skill("酒池")
    public boolean jiuChi() {
        return requestColor(Color.SPADE) != null;
    }

    @ForcesSkill("肉林")
    @Override
    public boolean hasRouLin() {
        // printlnPriv(this + " try uses 肉林");
        return isActiveSkill("肉林");
    }

    @Override
    public boolean requestJiu() {

        getPriviMsg().clearHeader2();
        getPriviMsg().setOneTimeInfo1("\n💬是否用 酒池：你可以将一张黑桃手牌当[酒]使用。");

        if (launchSkillPriv("酒池")) {
            if (jiuChi()) {
                String res = Text.format("%s %s 喝醉了:<i>呃呵，呃~再来~一壶！</i>",
                        getPlateName(), getSkillHtmlName("酒池"));
                // sleep(1000);
                getGameManager().getIo().printlnPublic(res, toString());
                return true;
            }
        }
        return super.requestJiu();
    }

    @Override
    public void usePhaseBefore() {

        if (isActiveSkill("酒池") && hasBlackHandCard() && !(isDrunk() || isDrunkShaUsed())) {
            getSkillCards().add("酒池");
        }
    }

    @Override
    public boolean useSkillInUsePhase(int orderInt) {
        // int orderInt = Integer.valueOf(order) - 1;

        if (orderInt < getSkillCards().size() && getSkillCards().get(orderInt).equals("酒池")
                && !(isDrunk() || isDrunkShaUsed())) {
            // printlnPriv(this + " uses 酒池");
            if (jiuChi()) {
                setDrunk(true);

                String res = Text.format("%s %s 喝醉了:<i>呃呵，呃~再来~一壶！</i>",
                        getPlateName(), getSkillHtmlName("酒池"));
                // sleep(1000);
                getGameManager().getIo().printlnPublic(res, toString());
            }
            return true;
        }
        return false;
    }

    // 如果没有"崩坏"要剪掉4点hp
    @Override
    public void selfBeginPhase() {
        if (hasAddHp && !isActiveSkill("崩坏")) {
            int newMaxHp = 4 + (getIdentity() == Identity.KING && getGameManager().getIntialNumPlayers() > 4 ? 1 : 0);
            setMaxHpNotSetCurrent(newMaxHp);
            hasAddHp = false;
        } else if (!hasAddHp && isActiveSkill("崩坏")) {
            int newMaxHp = 8 + (getIdentity() == Identity.KING && getGameManager().getIntialNumPlayers() > 4 ? 1 : 0);
            setMaxHpNotSetCurrent(newMaxHp - hasMinusMaxHp);
            // 只加最大生命不加实际生命
            hasAddHp = true;
        }
    }

    @ForcesSkill("崩坏")
    @Override
    public void selfEndPhase(boolean fastMode) {
        if (isActiveSkill("崩坏")) {
            boolean isLowest = true;
            for (Person p : getGameManager().getPlayersBeginFromPlayer(this)) {
                if (p.getHP() < getHP()) {
                    isLowest = false;
                    break;
                }
            }
            if (!isLowest) {

                MsgObj publicMsgObj = MsgObj.newMsgObj(getGameManager());
                if (launchSkillPublic(
                        publicMsgObj,
                        "-1点最大体力",
                        Text.format("%s %s：你不是体力值最小的角色，你-1点体力上限,否则-1点体力。",
                                getHtmlName(), getSkillHtmlName("崩坏")),
                        "benghuai1",
                        false)) {

                    setMaxHpNotSetCurrent(getMaxHP() - 1);
                    hasMinusMaxHp++;

                    String res = Text.format(",减1点体力上限%s:<i>谁有权力，谁掌生死！</i>", getHPEmoji());
                    publicMsgObj.appendText(res);
                    publicMsgObj.replyMakup = null;
                    // sleep(1000);
                    getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
                } else {
                    loseHP(1, this);

                    String res = Text.format(",失去1点体力%s:<i>什么礼制纲常，我说的，就是纲常！</i>", getHPEmoji());
                    publicMsgObj.appendText(res);
                    publicMsgObj.replyMakup = null;
                    // sleep(1000);
                    getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
                }
            }
        }

    }

    @KingSkill("暴虐")
    @Override
    public void otherPersonMakeHurt(Person source, Person target) {
        // 这个p是被攻击者，攻击者没有
        if (getHP() < getMaxHP() && getIdentity() == Identity.KING && source.getNation() == Nation.QUN) {
            String self = (source == this) ? "(你)" : "(不是你)";
            String res = Text.format("💬%s是否用 %s：主公技，当你造成伤害，可以进行判定，若结果为%s，%s %s回复1点体力",
                    source.getHtmlName(), getSkillHtmlName("暴虐"),
                    Card.getColorEmoji(Color.SPADE), getPlateName(), self);

            MsgObj publicMsgObj0 = MsgObj.newMsgObj(getGameManager());
            if (source.launchSkillPublic(
                    publicMsgObj0,
                    "暴虐",
                    res,
                    "baonve1")) {
                Card c = getGameManager().getCardsHeap().judge(source, new CallbackEven() {
                    @Override
                    public boolean juge(Card card) {
                        return (card.color() == Color.SPADE);
                    }
                });
                if (c.color() == Color.SPADE) {
                    recover(null, 1);
                    String res2 = Text.format(",%s 判定成功，%s 回复1体力:<i>酒池肉林，其乐无穷，哈哈哈哈哈哈！</i>",
                            c.getHtmlNameWithColor(),
                            getPlateName());
                    MsgObj publicMsgObj = getTempActionMsgObjFirstOrder(publicMsgObj0, "changeJudge");
                    publicMsgObj.appendText(res2);
                    // sleep(1000);
                    getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);

                } else {
                    MsgObj publicMsgObj = getTempActionMsgObjFirstOrder(publicMsgObj0, "changeJudge");
                    String res2 = Text.format(",%s 判定失败:<i>可惜,哎，我是不是该减肥了？</i>",
                            c.getHtmlNameWithColor(),
                            getPlateName());
                    publicMsgObj.appendText(res2);
                    // sleep(1000);
                    getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
                }
            } else {
                MsgObj publicMsgObj = getTempActionMsgObjFirstOrder(publicMsgObj0, "changeJudge");
                String res2 = Text.format(",放弃操作:<i>竖子，竟敢反我！</i>", getPlateName());
                publicMsgObj.appendText(res2);
                // sleep(1000);
                getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);

            }
        }
    }

    @Override
    public Set<String> getInitialSkills() {
        Set<String> skills = super.getInitialSkills();

        if (getIdentity() != Identity.KING) {
            skills.remove("暴虐");
        }
        return skills;
    }

    // @Override
    // public void clearAndAddActiveSkill(String... skills) {
    // // TODO Auto-generated method stub
    // super.clearAndAddActiveSkill(skills);
    // }

    @Override
    public String name() {
        return "董卓";
    }

    @Override
    public String skillsDescription() {
        return "酒池：你可以将一张黑桃手牌当[酒]使用。\n" +
                "肉林：锁定技，你对女性角色使用的[杀]和女性角色对你使用的[杀]均需使用两张[闪]才能抵消。\n" +
                "崩坏：锁定技，结束阶段，若你不是体力值最小的角色，你失去1点体力或减1点体力上限。\n" +
                "暴虐：主公技，当其他群势力角色造成伤害后，其可以进行判定，若结果为黑桃，你回复1点体力。";
    }
}
