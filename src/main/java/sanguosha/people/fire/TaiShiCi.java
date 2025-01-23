package sanguosha.people.fire;

import config.Config;
import config.Text;
import msg.MsgObj;
import sanguosha.cards.Card;
import sanguosha.cards.basic.Sha;
import sanguosha.manager.GameManager;
import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.Skill;
import components.TimeLimit;

public class TaiShiCi extends Person {
    private boolean isNB;

    public TaiShiCi() {
        super(4, Nation.WU);
        //super(2, Nation.WU);
    }

    @Override
    public void usePhaseBefore() {

        if (isActiveSkill("天义") && hasNotUsedSkill1()) {
            getSkillCards().add("天义");
        }
    }

    @Skill("天义")
    @Override
    public boolean useSkillInUsePhase( int orderInt) {

        //int orderInt = Integer.valueOf(order) - 1;

        if (orderInt < getSkillCards().size() && getSkillCards().get(orderInt).equals("天义") && hasNotUsedSkill1()) {
            Person p = null;
            TimeLimit t = new TimeLimit(Config.PRIV_RND_TIME_60S);
            do {
                if (t.isTimeout()) {
                    break;
                }
                //printlnToIOPriv("choose a person with hand cards to 拼点");
                getPriviMsg().setOneTimeInfo1(Text.format("\n💬你使用 %s,请选择1个目标 进行拼点:",  getSkillHtmlName("天义") ));
                p = selectPlayer();
                if (p == null) {
                    return false;
                }
            } while (getGameManager().isRunning() && p.getCards().isEmpty());
            if (p == null) {
                return false;
            }
            if (getGameManager().pinDian(this, p)) {
                isNB = true;
                setMaxShaCount(2);
             
                MsgObj publicMsg=getTempActionMsgObj("pindian");
                String result=Text.format(",%s可多用一张[杀]、无距离限制且多选一个目标:<i>北海酬恩，神亭酣战！</i>",  getHtmlName() );
                publicMsg.text=publicMsg.text+result;
                getGameManager().getIo().delaySendAndDelete(this, "你本回合可多用一张[杀]、无距离限制且多选一个目标");
                //sleep(1000);
                getGameManager().getMsgAPI().editCaptionForce(publicMsg);
        
                //sleep(3000);
            } else {
                setMaxShaCount(0);
           
                MsgObj publicMsg=getTempActionMsgObj("pindian");
                String result=Text.format(",本回合 %s 不能使用[杀]",  getHtmlName() );
                publicMsg.text=publicMsg.text+result;
                getGameManager().getIo().delaySendAndDelete(this, "你本回合不能使用[杀]");
                //sleep(1000);
                getGameManager().getMsgAPI().editCaptionForce(publicMsg);
                //sleep(3000);
            }
            setHasUsedSkill1(true);
        }
        return false;
    }

    @Override
    public void setShaMulti(Card card) {
        if(isNB){
            card.setMultiSha(card.getMultiSha()+1); 
        }
        super.setShaMulti(card);
    }
 
     

    @Override
    public boolean getIsNB() {
        return this.isNB;
    }

    @Override
    public int getShaDistance() {
        if (isNB) {
            return 10000;
        }
        return super.getShaDistance();
    }

    @Override
    public void selfEndPhase(boolean fastMode) {
        setMaxShaCount(1);
        isNB = false;
    }

    @Override
    public String name() {
        return "太史慈";
    }

    @Override
    public String skillsDescription() {
        return "天义：出牌阶段限一次，你可以与一名角色拼点：若你赢，本回合你可以多使用一张[杀]、" +
                "使用[杀]无距离限制且可以多选择一个目标；若你没赢，本回合你不能使用[杀]。";
    }
}
