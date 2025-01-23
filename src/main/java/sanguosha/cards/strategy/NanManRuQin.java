package sanguosha.cards.strategy;

import sanguosha.cards.Color;
import sanguosha.cards.EquipType;
import sanguosha.cards.Strategy;
import sanguosha.manager.GameManager;
import sanguosha.manager.Status;
import sanguosha.people.Person;

import java.util.Iterator;
import java.util.List;

import config.Text;
import msg.MsgObj;

import java.util.ArrayList;

public class NanManRuQin extends Strategy {
    public NanManRuQin(GameManager gameManager, Color color, int number) {
        super(gameManager, color, number);
        globlStrategy=true;
    }

    @Override
    public Object use() {
        Person realSource = getSource();
        for (Person p : getGameManager().getPlayersBeginFromPlayer(getSource())) {
            if (p.hasHuoShou()) {
                realSource = p;
                break;
            }
        }

        // 存出一个新数组用来规避那个结构变化
        List<Person> list = new ArrayList<>(getGameManager().getPlayersBeginFromPlayer(getSource()));
        for (int i = 0; i < list.size(); i++) {
            try {


                Person p = list.get(i);
                if (p == getSource()) {

                    continue;
                }
                boolean showImg=(i==1); //为什么是1，因为0是自己，跳过了
                String img= showImg?toString():null;
                if (p.hasHuoShou()) {
                    String reslut = Text.format("%s :祸首,南蛮入侵对其无效", p.getHtmlName());
                    getGameManager().getIo().printlnPublic(reslut, img);

                    continue;
                }
                if (p.hasJuXiang()) {
                    String reslut = Text.format("%s :巨象,南蛮入侵对其无效", p.getHtmlName());
                    getGameManager().getIo().printlnPublic(reslut, img);

                    continue;
                }
                // System.out.println("p.hasWeiMu()="+p.hasWeiMu());
                // System.out.println("isBlack()="+isBlack());
                if (p.hasWeiMu() && isBlack()) {
                    String reslut = Text.format("%s :帷幕,黑色锦囊南蛮入侵对其无效", p.getHtmlName());
                    getGameManager().getIo().printlnPublic(reslut, img);

                    continue;
                }
                if (p.hasEquipment(EquipType.shield, "藤甲")) {
                    String reslut = Text.format("%s 装备藤甲,南蛮入侵对其无效", p.getHtmlName());
                    getGameManager().getIo().printlnPublic(reslut, img);

                    continue;
                }
                if (gotWuXie(p,showImg)) {
                    continue;
                }

                if (p.requestSha(getSource(),false) == null) {
                    int real=p.hurt(this, realSource, 1);
                    MsgObj shaMsg = p.getTempActionMsgObj("sha");
                    //MsgObj wuxieMsg = p.getTempActionMsgObj("wuxie");
                    ////sleep(1000);
                    //getGameManager().getMsgAPI().delMsg(shaMsg);
                   
                    String result = Text.format(",受%s点伤害%s",
                            real, p.getHPEmoji());
                    if (shaMsg != null) {
                        shaMsg.replyMakup = null;
                        shaMsg.text = shaMsg.text + result;
                        // System.out.println(" wuxieMsg.text="+ wuxieMsg.text);
                        //sleep(1000);
                        getGameManager().getMsgAPI().editCaptionForce (shaMsg);

                    }

                } else {// 打出杀，就把原来那个技能图删了

                    // MsgObj wuxieMsg = p.getTempActionMsgObj("wuxie");
                    // //sleep(1000);
                    // getGameManager().getMsgAPI().delMsg(wuxieMsg);

                }
                if (getGameManager().status() == Status.end) {
                    return null;
                }
            } finally {
                //System.out.println(this+"sleep");
                //sleep(1000);
            }

        }

        if (this.isNotTaken()) {
            for (Person p : getGameManager().getPlayersBeginFromPlayer(getSource())) {
                if (p.hasJuXiang()) {
                    p.addCard(this);
                    this.setTaken(true);
                    break;
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return "南蛮入侵";
    }

    @Override
    public String details() {
        return "出牌阶段，对所有其他角色使用。每名目标角色需出[杀]，否则受到1点伤害。";
    }
}
