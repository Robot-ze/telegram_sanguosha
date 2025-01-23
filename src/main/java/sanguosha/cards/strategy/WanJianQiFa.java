package sanguosha.cards.strategy;

import sanguosha.cards.Color;
import sanguosha.cards.EquipType;
import sanguosha.cards.Strategy;
import sanguosha.manager.GameManager;
import sanguosha.manager.Status;
import sanguosha.people.Person;

 
import java.util.List;

import config.Text;
import msg.MsgObj;

import java.util.ArrayList;

public class WanJianQiFa extends Strategy {

    public WanJianQiFa(GameManager gameManager, Color color, int number) {
        super(gameManager, color, number);
        globlStrategy=true;
    }

    @Override
    public Object use() {

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

                if (p.hasEquipment(EquipType.shield, "藤甲")) {
                    String reslut = Text.format("%s 装备藤甲,万箭齐发对其无效", p.getHtmlName());
                    getGameManager().getIo().printlnPublic(reslut,img);
               
                    continue;
                }

                if (p.hasWeiMu() && isBlack()) {
                    String reslut = Text.format("%s :帷幕,黑色锦囊万箭齐发对其无效", p.getHtmlName());
                    getGameManager().getIo().printlnPublic(reslut, img);
               
                    continue;
                }

                if (gotWuXie(p,showImg)) {
                    continue;
                }

                if (!p.requestShan(null,false,false,getSource())) {
                    int real=p.hurt(getReplaceCards(), getSource(), 1);

                    MsgObj shanMsg = p.getTempActionMsgObj("shan");
                    //MsgObj wuxieMsg = p.getTempActionMsgObj("wuxie");
                    // //sleep(1000);
                    // getGameManager().getMsgAPI().delMsg(shanMsg);
              
                    String result = Text.format(",受%s点伤%s",
                             real,p.getHPEmoji());
                    if (shanMsg != null) {
                        shanMsg.replyMakup = null;
                        shanMsg.text = shanMsg.text + result;
                        // System.out.println(" wuxieMsg.text="+ wuxieMsg.text);
                        //sleep(1000);
                        getGameManager().getMsgAPI().editCaptionForce (shanMsg);
                      
                    }
                } else {
                   
                    // MsgObj wuxieMsg = p.getTempActionMsgObj("wuxie");
                    // if (wuxieMsg != null) {
                    //     //sleep(1000);
                    //     getGameManager().getMsgAPI().delMsg(wuxieMsg);
                    
                    // }
                }

                if (getGameManager().status() == Status.end) {
                    return null;
                }
     
            } finally {
                //sleep(1000);
            }
        }

        return true;
    }

    @Override
    public String toString() {
        return "万箭齐发";
    }

    @Override
    public String details() {
        return "出牌阶段，对所有其他角色使用。每名目标角色需出[闪]，否则受到1点伤害。";
    }
}
