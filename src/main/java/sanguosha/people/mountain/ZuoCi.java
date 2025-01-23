package sanguosha.people.mountain;

import config.Text;
import msg.MsgObj;
import msg.ReturnType;
import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.ForcesSkill;

public class ZuoCi extends Person {

    public ZuoCi() {
        super(3, Nation.QUN);
        setZuoCi(true);
    }

  

    @ForcesSkill("新生")

    @Override
    public void zuoCiInitialize() {
  
        Person p1 = getGameManager().getPeoplePool().allocOnePerson();
        addHuaShenToList(p1);
        // printlnPriv(this + " got 化身 " + p1);
        p1.setZuoCi(true);
        p1.setGameManager(getGameManager());
        Person p2 = getGameManager().getPeoplePool().allocOnePerson();
        addHuaShenToList(p2);
        // printlnPriv(this + " got 化身 " + p2);
        p2.setZuoCi(true);
        p2.setGameManager(getGameManager());
        // selectHuaShen(p1);
        // while (!huaShen(false)) {
        // printlnToIO("you must choose a 化身");
        // }
    }
 
    

    @Override
    public String name() {
        return "左慈";
    }

    @Override
    public boolean getSkipNoticeUsePublic() {
        return true;
    }

    /**
     * 获取带html注释url的名字
     * 
     * @return
     */
    // public String getHtmlName() {
    // return "<b><a href=\"" + getUrl() + "\">" + toString() + "</a></b>(<a
    // href=\"tg://user?id=" + user.user_id
    // + "\">" + user.full_name + "</a>)";
    // }

    // @Override
    // public String getPlateName() {
    // return "<b>" + toString() + "</b>(" + user.full_name + ")";
    // }

    @Override
    public String skillsDescription() {
        return "化身：游戏开始时，你随机获得两张武将牌作为\"化身\"牌，然后亮出其中一张，获得该\"化身\"牌的除了主公技外的1个技能。" +
                "回合开始时或结束后，你可以更改亮出的\"化身\"牌。\n" +
                "新生：当你受到1点伤害后，你可以获得一张新的\"化身\"牌。";
    }
}
