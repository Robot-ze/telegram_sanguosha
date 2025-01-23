package sanguosha.people.fire;

import java.util.List;

import config.Config;
import config.Text;
import sanguosha.cards.Card;
import sanguosha.cards.equipments.Weapon;
import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.Skill;
import components.TimeLimit;

public class DianWei extends Person {
    public DianWei() {
        super(4, Nation.WEI);
        //super(1, Nation.WEI);// test
    }

    @Override
    public void usePhaseBefore() {

        if (isActiveSkill("强袭") && hasNotUsedSkill1()) {
            getSkillCards().add("强袭");
        }
    }

    @Skill("强袭")
    @Override
    public boolean useSkillInUsePhase(int orderInt) {

        // int orderInt = Integer.valueOf(order) - 1;

        if (orderInt < getSkillCards().size() && getSkillCards().get(orderInt).equals("强袭") && hasNotUsedSkill1()) {
            // printlnPriv(this + " uses 强袭");
            getPriviMsg().setOneTimeInfo1("\n💬请选择你的 强袭 对象：你可以失去1点体力或弃置一张武器牌，并对TA造成1点伤害。");
            List<Person> canReachPerson= getGameManager().reachablePeople(this, getShaDistance());

            Person p = selectPlayer(canReachPerson);
            if (p == null) {
                return true;
            }
  

            getPriviMsg().setOneTimeInfo1("\n💬请选择你的 强袭 方式");
            if (chooseNoNull("丢出武器", "受1伤害") == 2) {
                loseHP(1,this);
                int realNum;
                if (checkDead()) {
                    realNum = p.hurt((Card) null, null, 1);
                } else {
                    realNum = p.hurt((Card) null, this, 1);
                }
                showHurtText(false, p, realNum, null);
            } else {
                Card c = null;
                TimeLimit t = new TimeLimit(Config.PRIV_RND_TIME_60S);
                do {
                    if (t.isTimeout()) {
                        break;
                    }
                    getPriviMsg().setOneTimeInfo1("\n💬请选择一把武器");
                    c = chooseCard(getCardsAndEquipments(), true);
                    // System.out.println("chooseCard(getCardsAndEquipments()=" + c);
                } while (getGameManager().isRunning() && (!(c instanceof Weapon) && c != null));
                if (c == null) {
                    return false;
                }
                
                loseCard(c);

                int realNum = p.hurt((Card) null, this, 1);

                showHurtText(true, p, realNum, c);

            }
            setHasUsedSkill1(true);
            return true;
        }
        return false;
    }

    private void showHurtText(boolean useWepon, Person p, int realNum, Card weapon) {

        String function = useWepon ? "扔出武器" : "自损";
        String result = Text.format("%s%s对%s造成%s点伤害%s:<i>小儿吃我一戟！</i>",
                this.getPlateName(),
                function, p.getHtmlName(), realNum, p.getHPEmoji());
        getGameManager().getIo().printlnPublic(result, toString());
        // sleep(3000);
    }

    // @Override
    // public Set<String> getSkills() {

    // return super.getSkills();
    // }

    // @Override
    // public void clearAndAddActiveSkill(String... skills) {
    // // TODO Auto-generated method stub
    // super.clearAndAddActiveSkill(skills);
    // }

    @Override
    public String name() {
        return "典韦";
    }

    @Override
    public String skillsDescription() {
        return "强袭：出牌阶段限一次，你可以失去1点体力或弃置一张武器牌，并对你攻击范围内的一名其他角色造成1点伤害。";
    }
}
