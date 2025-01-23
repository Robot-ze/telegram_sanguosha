package sanguosha.people.fire;

import java.util.ArrayList;
import java.util.List;

import config.Text;
import msg.MsgObj;
import sanguosha.cards.Card;
import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.Skill;

public class XunYu extends Person {
    public XunYu() {
        super(3, Nation.WEI);
        //super(1, Nation.WEI);// test
    }

    @Override
    public void usePhaseBefore() {

        if (isActiveSkill("驱虎") && hasNotUsedSkill1()) {
            getSkillCards().add("驱虎");
        }
    }

    @Skill("驱虎")
    @Override
    public boolean useSkillInUsePhase(int orderInt) {
        // int orderInt = Integer.valueOf(order) - 1;

        if (orderInt < getSkillCards().size() && getSkillCards().get(orderInt).equals("驱虎") && hasNotUsedSkill1()) {

            getPriviMsg().setOneTimeInfo1("驱虎：出牌阶段限一次，你可以与体力值大于你的一名角色拼点：" +
                    "若你赢，你令该角色对其攻击范围内的另一名角色造成1点伤害；若你没赢，其对你造成1点伤害。\n");

            List<Person> lifeMaxThanMe = new ArrayList<>();
            for (Person p : getGameManager().getPlayersBeginFromPlayer(this)) {
                if (p == this) {
                    continue;
                }
                if (p.getHP() > getHP() && p.getCards().size() > 0) {
                    lifeMaxThanMe.add(p);
                }
            }

            if(lifeMaxThanMe.size()<=0){
                getGameManager().getIo().delaySendAndDelete(this, "没有体力值大于你并且有手牌的玩家");
                return true;
            }

            Person p = selectPlayer(lifeMaxThanMe);
            if (p == null) {
                return true;
            }


            List<Person> reachablePeople = getGameManager().reachablePeople(p, p.getShaDistance());
            if (reachablePeople.size() == 0) {
                 getGameManager().getIo().delaySendAndDelete(this,Text.format("\n💬%s 无法攻击到任何人", p.getPlateName()));
                return true;
            }

            getPriviMsg().setOneTimeInfo1(Text.format("\n💬请选择 %s 的攻击对象", p.getPlateName()));
            Person p2 = selectPlayerExept(reachablePeople, p);//可以打自己，不排除自己，相当于黄盖了
            if (p2 == null) {
                return true;
            }

            boolean win = getGameManager().pinDian(this, p);

            int n = win ? p2.hurt((Card) null, p, 1) : hurt((Card) null, p, 1);

            setHasUsedSkill1(true);
            MsgObj pindianMsg = getTempActionMsgObj("pindian");

            if (pindianMsg != null) {

                if (win) {
                    // System.out.println("pindianMsg=" + pindianMsg);
                    String result = Text.format(",%s成功,%s 令 %s 对 %s 造成 %s伤害%s:<i>此乃驱虎吞狼之计。</i>",
                            getSkillHtmlName("驱虎"),
                            getPlateName(),
                            p.getHtmlName(),
                            p2.getHtmlName(),
                            n + "",
                            p2.getHPEmoji());
                    pindianMsg.text = pindianMsg.text + result;
                } else {
                    String result = Text.format(",%s失败,%s 自己受 %s伤害%s:<i>养虎为患矣！</i>",
                            getSkillHtmlName("驱虎"),
                            getPlateName(),
                            n + "",
                            getHPEmoji());
                    pindianMsg.text = pindianMsg.text + result;
                }

                getGameManager().getMsgAPI().editCaptionForce(pindianMsg);
                // sleep(3000);

            }

            return true;
        }
        return false;
    }

    @Skill("节命")
    @Override
    public void gotHurt(List<Card> cs, Person source, int num) {
        if (launchSkillPublicDeepLink("节命",
                "当你受到1点伤害后，你可以令一名角色将手牌摸至X张(X为其体力上限且最多为5)",
                "jieming")) {

            getPriviMsg().setOneTimeInfo1("你可以令一名角色将手牌摸至X张(X为其体力上限且最多为5) ");
            Person p = selectPlayer(true);
            int n = p.getMaxHP() - p.getCards().size();
            n = Math.min(5, n);
            n = Math.max(0, n);
            if (n > 0) {
                p.drawCards(n);
            }

            String result = Text.format("%s,%s摸牌%s张:<i>我，永不背弃。</i>",
                    getSkillHtmlName("节命"),
                    p.getHtmlName(),
                    n + "");
            getGameManager().getIo().printlnPublic(result, toString());
            // sleep(3000);

        }
    }

    @Override
    public String name() {
        return "荀彧";
    }

    @Override
    public String skillsDescription() {
        return "驱虎：出牌阶段限一次，你可以与体力值大于你的一名角色拼点：" +
                "若你赢，你令该角色对其攻击范围内的另一名角色造成1点伤害；若你没赢，其对你造成1点伤害。\n" +
                "节命：当你受到1点伤害后，你可以令一名角色将手牌摸至X张（X为其体力上限且最多为5）。";
    }
}
