package sanguosha.cards.equipments.weapons;

import sanguosha.cards.Card;
import sanguosha.cards.Color;
import sanguosha.cards.equipments.Weapon;
import sanguosha.manager.GameManager;
import sanguosha.people.AI;
import sanguosha.people.Person;

import java.util.ArrayList;

import config.Text;
import msg.MsgObj;

public class SanJianLiangRenDao extends Weapon {
    public SanJianLiangRenDao(GameManager gameManager, Color color, int number) {
        super(gameManager, color, number, 3);
    }

    @Override
    public Object use(Card sourceCard) {
        ArrayList<Person> nearbyPerson = getGameManager().reachablePeople(getSource(), 1);
        if (!nearbyPerson.isEmpty()) {
            MsgObj publicMsgObj = MsgObj.newMsgObj(getGameManager());
            boolean active = false;
            if (getOwner().isAI()) {
                return false;
            } else {

                active = getGameManager().getMsgAPI().noticeAndAskPublic(
                        publicMsgObj,
                        this,
                        getOwner(),
                        Text.format("%s 是否用 %s",
                                getOwner().getHtmlName(), getHtmlName()),
                        "使用" + toString(), "shanren");

            }

            if (!active) {
                return false;
            }

            Person p = getSource().selectPlayer(nearbyPerson);

            if (p == null) {
                return null;
            }
            getSource().getPriviMsg().setOneTimeInfo1("\n💬请弃置一张手牌并对该角色距离1的另一名角色造成1点伤害");

            Card c = getSource().requestCard(null);
            if (c == null) {
                return null;
            }

            int real = p.hurt((Card) null, getSource(), 1);
            //sleep(1000);
            String res = Text.format(",%s 受%s点伤%s",
                    p.getHtmlName(), real, p.getHPEmoji());
            publicMsgObj.appendText(res);
            //sleep(1000);
            getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
        }
        return null;
    }

    @Override
    public String toString() {
        return "三尖两刃刀";
    }

    @Override
    public String details() {
        return "你使用[杀]对目标角色造成伤害后，可弃置一张手牌并对该角色距离1的另一名角色造成1点伤害。";
    }
}
