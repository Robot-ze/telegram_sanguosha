package sanguosha.people.god;

import sanguosha.cards.Card;
import sanguosha.cards.EquipType;
import sanguosha.cards.equipments.Shield;
import sanguosha.manager.GameManager;
import sanguosha.manager.Utils;
import sanguosha.people.Person;
import sanguosha.skills.ForcesSkill;
import sanguosha.skills.Skill;

import java.util.ArrayList;
import java.util.List;

import config.Text;

public class ShenLvBu extends God {
    private int baoNuMark = 2;
    private boolean isWuQian = false;
    private int thisRound = -1;

    public ShenLvBu() {
        super(5, null);
    }

    @ForcesSkill("暴怒")
    @Override
    public void gotHurt(List<Card> cards, Person p, int num) {
        // printlnPriv(this + " got " + num + " 暴怒mark");
        baoNuMark += num;
        sendNotice(num);
        // printlnPriv(this + " now has " + baoNuMark + " 暴怒 marks");
    }

    @Override
    public void hurtOther(Person p, int num) {
        // printlnPriv(this + " got " + num + " 暴怒mark");
        baoNuMark += num;
        sendNotice(num);
    }

    private void sendNotice(int num) {
        boolean showImg;
        if (getGameManager().getPersonRound() > thisRound) {
            thisRound = getGameManager().getPersonRound();
            showImg = true;
        } else {
            showImg = false;
        }

        String res = Text.format("%s %s+%s:<i>嗯~~~~~</i>",
               getPlateName(), getSkillHtmlName("暴怒"), num);
        //sleep(1000);
        getGameManager().getIo().printlnPublic(res, showImg ? toString() : null);
    }

    @ForcesSkill("无谋")
    @Override
    public void useStrategy() {
        getPriviMsg().setOneTimeInfo1("当你使用普通锦囊牌时，你弃1枚“暴怒”标记或失去1点体力。");
        String res;
        if (baoNuMark == 0 || chooseNoNull("暴怒-1", "体力-1") == 2) {
            loseHP(1, this);
            res = Text.format("-1体力%s", getHPEmoji());
        } else {
            // printlnPriv(this + " lost 1 暴怒 mark");
            // printlnPriv(this + " now has " + baoNuMark + " 暴怒 marks");
            baoNuMark--;
            res = Text.format("-1暴怒");
        }

        String res2 = Text.format("%s:<i>哪个说我有勇无谋？</i>", res);
        //sleep(1000);
        getGameManager().getIo().printlnPublic(res2, toString());
    }

    @ForcesSkill("无双")
    @Override
    public boolean hasWuShuang() {
        return isWuQian;
    }

    @Skill("无前")
    public void wuQian() {
        getPriviMsg().setOneTimeInfo1("出牌阶段，你可以弃2枚“暴怒”标记并选择一名其他角色，然后直到回合结束，你获得“无双”且该角色的防具失效");

        Person p = selectPlayer();
        if (p == null) {
            return;
        }
        if (p.hasEquipment(EquipType.shield, null)) {
            ((Shield) p.getEquipments().get(EquipType.shield)).setValid(false);
        }
        isWuQian = true;
        // printlnPriv(this + " lost 2 暴怒 mark");
        baoNuMark -= 2;
        // printlnPriv(this + " now has " + baoNuMark + " 暴怒 marks");
        // printlnPriv(this + " uses 无前 towards " + p);
        String res2 = Text.format("%s 护甲失效:<i>天王老子也保不住你！</i>", p.getHtmlName());
        //sleep(1000);
        getGameManager().getIo().printlnPublic(res2, toString());
    }

    @Skill("神愤")
    public void shenFen() {
        // printlnPriv(this + " uses 神愤");
        // printlnPriv(this + " lost 6 暴怒 mark");
        // printlnPriv(this + " now has " + baoNuMark + " 暴怒 marks");
        baoNuMark -= 6;
        String hurtText="";
        for (Person p : getGameManager().getPlayersBeginFromPlayer(this)) {
            if (p == this) {
                continue;
            }
            p.hurt((Card) null, this, 1);
            hurtText+="\n"+p.toString()+p.getHPEmoji();
            if (p.checkDead()) {
                continue;
            }
            p.loseCard(new ArrayList<>(p.getEquipments().values()));
            if (p.getCards().size() <= 4) {
                p.loseCard(p.getCards());
            } else {
                for (int i = 0; i < 4; i++) {
                    p.loseCard(randomChooseTargetCards(p));
                }

            }
        }
        hurtText+="\n";
        turnover();

        String res2 = Text.format("%s!%s全体受1点伤,弃武器,丢4张手牌:<i>凡人们，颤抖吧！这是神之怒火！</i>",
                getSkillHtmlName("神愤"),hurtText);
        //sleep(1000);
        getGameManager().getIo().printlnPublic(res2, toString());
    }

    @Override
    public void usePhaseBefore() {

        if (isActiveSkill("神愤") && baoNuMark >= 6 && hasNotUsedSkill1()) {
            getSkillCards().add("神愤");
        }

        if (isActiveSkill("无前") && baoNuMark >= 2) {
            getSkillCards().add("无前");
        }

    }

    @Override
    public boolean useSkillInUsePhase(int orderInt) {
        // TODO
        if (orderInt < getSkillCards().size() && getSkillCards().get(orderInt).equals("无前")) {
            wuQian();
            return true;
        }
        if (orderInt < getSkillCards().size() && getSkillCards().get(orderInt).equals("神愤") && hasNotUsedSkill1()) {
            shenFen();
            setHasUsedSkill1(true);
            return true;
        }
        return false;
    }

    @Override
    public void selfEndPhase(boolean fastMode) {
        Utils.assertTrue(baoNuMark >= 0, "invalid 暴怒 mark: " + baoNuMark);
        isWuQian = false;
        for (Person p : getGameManager().getPlayersBeginFromPlayer(this)) {
            if (p.hasEquipment(EquipType.shield, null)) {
                ((Shield) p.getEquipments().get(EquipType.shield)).setValid(true);
            }
        }

    }

    @Override
    public String getExtraInfo() {
        return "暴怒×" + baoNuMark;
    }

    @Override
    public String name() {
        return "神吕布";
    }

    @Override
    public String skillsDescription() {
        return "狂暴：锁定技，游戏开始时，你获得2枚“暴怒”标记；当你造成或受到1点伤害后，你获得1枚“暴怒”标记。\n" +
                "无谋：锁定技，当你使用普通锦囊牌时，你弃1枚“暴怒”标记或失去1点体力。\n" +
                "无前：出牌阶段，你可以弃2枚“暴怒”标记并选择一名其他角色，然后直到回合结束，你获得“无双”且该角色的防具失效。\n" +
                "无双：锁定技，你使用的[杀]需两张[闪]才能抵消；与你进行[决斗]的角色每次需打出两张[杀]。\n" +
                "神愤：出牌阶段限一次，你可以弃6枚“暴怒”标记，然后对所有其他角色各造成1点伤害，这些角色先各弃置装备区里的所有牌，再弃置四张手牌，最后你翻面。";
    }
}
