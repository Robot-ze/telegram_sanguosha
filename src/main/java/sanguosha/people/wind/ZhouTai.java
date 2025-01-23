package sanguosha.people.wind;

import sanguosha.cards.Card;

import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.Skill;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;

import config.Text;
import msg.MsgObj;

public class ZhouTai extends Person {
    private Deque<Card> buQuCards =  new LinkedList<>();
    private Deque<Integer> buQuNumbers = new LinkedList<>();
    private ArrayList<Card> extCards = new ArrayList<> ();

    public ZhouTai() {
        super(4, Nation.WU);
        //super(1, Nation.WU);//test
    }

    public boolean buQuDuplicated() {
        return new HashSet<>(buQuNumbers).size() != buQuNumbers.size();
    }

    @Skill("不屈")
    @Override
    public void loseHP(int num,Person source) {
        int round;
        if (getHP() > 0) {// 只算致死的那部分数值
            if ((getHP() - num <= 0)) {
                round = num - getHP() + 1;
            } else {
                round = 0;
            }

        } else {
            round = num;
        }
        subCurrentHP(num);
        // printlnPriv(this + " lost " + num + "HP, current HP: " + getHP() + "/" +
        // getMaxHP());

        if (getHP() <= 0) {
            MsgObj publicMsgObj=MsgObj.newMsgObj(getGameManager());
            String oldPoint="";
            for(int point:buQuNumbers){
                oldPoint+=Text.pokerNum(point);
            }
            if (launchSkillPublic(
                    publicMsgObj,
                    "不屈",
                    Text.format("%s :是否使用 %s:现在的不屈牌点数[%s]",
                            getHtmlName(), getSkillHtmlName("不屈"),oldPoint),
                    "buqu1")) {
                // printlnPriv(this + " now has " + num + " 不屈 cards");
             
                String newCard="";

                for (int i = 0; i < round; i++) {
                    Card c = getGameManager().getCardsHeap().draw();
                    buQuNumbers.add(c.number());
                    buQuCards.add(c);
                    newCard+=c.getHtmlNameWithColor()+" ";
                    // getGameManager().getIo().printCardsPublic(buQuCards);
                }
                // printlnPriv(this + " now has " + buQuCards.size() + " 不屈 cards");
                // TimeLimit t = new TimeLimit(Config.PRIV_RND_TIME_30S);
                // while (getGameManager().isRunning() && buQuDuplicated()) {
                String res=Text.format(",抽牌[%s],",newCard);
                if (buQuDuplicated()) {
                    // if (t.isTimeout()) {
                    // return;
                    // }
                    res+="可惜<i>敌众我寡，无力回天……</i>";
                     
                    dying(source);
                }else{
                    res+="成功<i>我绝不会倒下！</i>";
                }
                publicMsgObj.appendText(res);
                //sleep(1000);
                getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
               
            } else {
                dying(source);
            }
        }
    }

    // @Override
    // public void throwPhase(boolean fastMode) {
    //     int num = getCards().size() - Math.max(getHP(), 0);
    //     if (num > 0) {
    //         // printlnToIOPriv(Text.format("You need to throw %d cards", num));
    //         ArrayList<Card> cs;
    //         if (fastMode) {// 快速模式直接丢掉后面的牌
    //             cs = new ArrayList<>();//
    //             for (int i = 0; i < num; i++) {
    //                 cs.add(getCards().get(getCards().size() - 1 - i));
    //             }
    //         } else {
    //             cs = chooseCards(num, getCards());
    //         }

    //         if (cs.size() < num) {// 如果选不够，强制从前面丢起
    //             for (Card c : getCards()) {
    //                 if (cs.indexOf(c) >= 0) {
    //                     continue;
    //                 }
    //                 cs.add(c);
    //                 if (cs.size() >= num) {
    //                     break;
    //                 }
    //             }
    //         }
    //         loseCard(cs);
    //         for (Person p : getGameManager().getPlayersBeginFromPlayer(this)) {
    //             p.otherPersonThrowPhase(this, cs);
    //         }
    //     }
    // }

    @Override
    public void recover(MsgObj publicMsgObj, int num) {
        String dropCardString=null;
        if (getHP() <= 0   ) {
            // printlnToIOPriv("choose 不屈 cards that you want to remove");
            //ArrayList<Card> cs = chooseCards(Math.max(num, buQuCards.size()), buQuCards);
            dropCardString="";
            for(int i=0;i<num;i++){
                if(!buQuCards.isEmpty()){
                    Card c=buQuCards.pollFirst();
                    getGameManager().getCardsHeap().discard(c);
                    buQuNumbers.pollFirst();
                    dropCardString+=c.getHtmlNameWithColor()+" ";
                }
            }
            
            // printlnPriv(this + " now has " + buQuCards.size() + " 不屈 cards");
        }
        super.recover(publicMsgObj,num);
        //不单止tao回加生命，还有其他技能也加生命
        
        if(dropCardString!=null && publicMsgObj!=null){
            String res=Text.format(",丢弃不屈牌[%s]", dropCardString);
            //sleep(1000);
            publicMsgObj.appendText(res);
            getGameManager().getMsgAPI().editCaptionForce(publicMsgObj);
        }
        
    }

    public int numOfBuQu() {
        return buQuCards.size();
    }

    @Override
    public ArrayList<Card> getExtraCards() {
        extCards.clear();
        extCards.addAll(buQuCards);
        return extCards;
    }

    @Override
    public String getExtraInfo() {
        String oldPoint="";
        for(int point:buQuNumbers){
            oldPoint+=Text.pokerNum(point);
        }
        return  Text.format("不屈牌%s", oldPoint)  ;
    }

    @Override
    public String name() {
        return "周泰";
    }

    @Override
    public String skillsDescription() {
        return "不屈：每当你扣减1点体力后，若你体力值为0，你可以从牌堆亮出一张牌置于你的武将牌上。" +
                "若此牌的点数与你武将牌上已有的任何一张牌都不同，你不会死亡。" +
                "若出现相同点数的牌，你进入濒死状态。\n";
    }
}
