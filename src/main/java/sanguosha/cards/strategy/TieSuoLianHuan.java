package sanguosha.cards.strategy;

import sanguosha.cards.Color;
import sanguosha.cards.Strategy;
import sanguosha.manager.GameManager;
import sanguosha.people.Person;
import components.TimeLimit;

import java.util.ArrayList;

import config.Config;
import config.Text;
import msg.MsgObj;

public class TieSuoLianHuan extends Strategy {
    private Person target2;

    public TieSuoLianHuan(GameManager gameManager, Color color, int number) {
        super(gameManager, color, number);
    }

    @Override
    public Object use() {
        if (!gotWuXie(getTarget())) {
            getTarget().switchLink();
        }
        // sleep(3000);

        if (!gotWuXie(getTarget2())) {
            getTarget2().switchLink();
        }
        // sleep(3000);

        String result = "\n";
        if (getTarget().isLinked()) {
            result += Text.format("\n%s 处于铁索连环状态",
                    getTarget().getHtmlName());
        } else {
            result += Text.format("\n%s 铁索连环状态解除",
                    getTarget().getHtmlName());
        }

        if (getTarget2().isLinked()) {
            result += Text.format("\n%s 处于铁索连环状态",
                    getTarget2().getHtmlName());
        } else {
            result += Text.format("\n%s 铁索连环状态解除",
                    getTarget2().getHtmlName());
        }

        // 这个是判断过无懈的消息
        MsgObj msg = getTarget2().getTempActionMsgObj("wuxie");
        msg.text = msg.text + result;
        getGameManager().getMsgAPI().editCaptionForce(msg);
        // sleep(3000);
        return true;
    }

    @Override
    public String toString() {
        return "铁索连环";
    }

    public Person getTarget2() {
        return target2;
    }

    public void setTarget2(Person target2) {
        this.target2 = target2;
    }

    @Override
    public boolean needChooseTarget() {
        return true;
    }

    @Override
    public boolean askTarget(Person user) {
        setSource(user);

        TimeLimit t = new TimeLimit(Config.PRIV_RND_TIME_60S);
        while (getGameManager().isRunning() && t.isNotTimeout()) {
            user.getPriviMsg().setOneTimeInfo1("\n💬请选择两个目标:目标角色分别横置或重置其武将牌。当一名处于横置状态的角色受到属性伤害，即使其死亡，" +
                "也会令其它处于连环状态的角色受到同来源、同属性、同程度的伤害。经由连环传导的伤害不能再次被传导。" );
            ArrayList<Person> plist = user.chooseManyFromProvided(2, getGameManager().getPlayers(),true);
            if (plist.size() == 0) {
                return false;
            }
            if (plist.size() == 1) {
                return false;
            }
            Person p1 = plist.get(0);
            Person p2 = plist.get(1);
            if (p1 == null || p2 == null) {
                return false;
            }

            if (this.isBlack() && p1.hasWeiMu()) {
                user.getPriviMsg().setOneTimeInfo1(Text.format("\n💬黑色[锦囊]不能作用于有 帷幕 的角色，请重新选择"));
                continue;
            }
            if (this.isBlack() && p2.hasWeiMu()) {
                user.getPriviMsg().setOneTimeInfo1(Text.format("\n💬黑色[锦囊]不能作用于有 帷幕 的角色，请重新选择"));
                continue;
            }
            if (p1 == p2) {
                user.getPriviMsg().setOneTimeInfo1("\n💬不能选择同一个目标");
                continue;
            }
            setTarget(p1);
            setTarget2(p2);
            return true;
        }
        return false;
    }

    @Override
    public String details() {
        return "目标角色分别横置或重置其武将牌。当一名处于横置状态的角色受到属性伤害，即使其死亡，" +
                "也会令其它处于连环状态的角色受到同来源、同属性、同程度的伤害。经由连环传导的伤害不能再次被传导。" +
                "重铸：你可以从手里弃掉这张牌，然后摸一张牌。";
    }
}
