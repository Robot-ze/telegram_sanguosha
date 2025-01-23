package sanguosha.cards.strategy;

import sanguosha.cards.Card;
import sanguosha.cards.Color;
import sanguosha.cards.EquipType;
import sanguosha.cards.Strategy;
import sanguosha.cards.basic.Sha;
import sanguosha.manager.GameManager;
import sanguosha.manager.Utils;
import sanguosha.people.AI;
import sanguosha.people.Person;

import static sanguosha.cards.EquipType.weapon;

import config.Text;
import msg.MsgObj;

public class JieDaoShaRen extends Strategy {
    private Person target2;

    public JieDaoShaRen(GameManager gameManager, Color color, int number) {
        super(gameManager, color, number);
    }

    @Override
    public Object use() {
        if (getTarget() == null || getTarget2() == null) {
            return false;
        }
        if (!gotWuXie(getTarget())) {
            Utils.assertTrue(getTarget().hasEquipment(EquipType.weapon, null),
                    "\n💬你选择的目标没有武器");
            Sha sha = getTarget().requestSha(getTarget2());
            if (sha != null) {
                String result = Text.format("%s 对 %s 打出杀",
                        getTarget().getHtmlName(), getTarget2().getHtmlName());

                // 这个是判断过无懈的消息
                MsgObj msg = getTarget().getTempActionMsgObj("sha");
                msg.text = msg.text + result;
                getGameManager().getMsgAPI().editCaptionForce(msg);

                sha.setSource(getTarget());
                sha.setTarget(getTarget2());
                sha.use();
            } else {
                Card w = getTarget().getEquipments().get(EquipType.weapon);
                getSource().addCard(w);
                // getTarget().getEquipments().put(EquipType.weapon, null);
                getTarget().getEquipments().remove(EquipType.weapon);
                String result = Text.format("%s 未出杀,%s 获得其武器 %s",
                        getTarget().getPlateName(), getSource().getPlateName(), w.getHtmlNameWithColor());

                // 这个是判断过无懈的消息
                MsgObj msg = getTarget().getTempActionMsgObj("sha");
                msg.text = msg.text + result;
                getGameManager().getMsgAPI().editCaptionForce(msg);

            }
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "借刀杀人";
    }

    public void setTarget2(Person target2) {
        this.target2 = target2;
    }

    public Person getTarget2() {
        return target2;
    }

    @Override
    public boolean needChooseTarget() {
        return true;
    }
 
    @Override
    public boolean askTarget(Person user) {
        user.getPriviMsg().setOneTimeInfo1(Text.format("\n💬你使用 %s,请选择第1个目标:对装备区里有武器牌且其攻击范围内有使用[杀]的目标的一名其他角色A使用。" +
                "（选择目标时）你选择A攻击范围内的一名角色B（与A不同）。" +
                "A需对B使用一张[杀]，否则将其装备区里的武器牌交给你。",  getHtmlName() ));
        return super.askTarget(user);
    }

    @Override
    public boolean asktargetAddition(Person user, Person p) {
        if(!p.hasEquipment(EquipType.weapon, null)){
            user.getPriviMsg().setOneTimeInfo1(Text.format("\n💬 %s 没有武器，请重新选择", p.getPlateName()));
            return false;
        }

        if (user .isAI()){//AI无脑按顺序选一个
            for(Person pp:getGameManager().getPlayersBeginFromPlayer(p)){
                if(pp==p){
                    continue;
                }
                setTarget(p);
                setTarget2(pp);
                return true;
            }
            return false;
        }

       
        user.getPriviMsg().setOneTimeInfo1(Text.format("\n💬你使用 %s,请选择第2个目标:", getHtmlName()));
        Person p2 = getSource().selectPlayerExept(
            getGameManager().getPlayersBeginFromPlayer(p),getSource(),p);
        if (p2 == null) {
            return false;
        }

        setTarget(p);
        setTarget2(p2);
        return true;
    }

    @Override
    public String details() {
        return "出牌阶段，对装备区里有武器牌且其攻击范围内有使用[杀]的目标的一名其他角色A使用。" +
                "（选择目标时）你选择A攻击范围内的一名角色B（与A不同）。" +
                "A需对B使用一张[杀]，否则将其装备区里的武器牌交给你。";
    }
}
