package sanguosha.people.wei;

import config.Config;
import config.Text;
import sanguosha.cards.Card;

import sanguosha.people.Nation;
import sanguosha.people.Person;
import sanguosha.skills.Skill;
import components.TimeLimit;

public class ZhangLiao extends Person {
    public ZhangLiao() {
        super(4, Nation.WEI);
    }

    @Skill("突袭")
    @Override
    public void drawPhase(boolean fastMode) {
        getPriviMsg().setOneTimeInfo1("突袭：摸牌阶段，你可以改为获得最多两名角色的各一张手牌。");
        if (! fastMode&&launchSkillPriv("突袭")) {
            TimeLimit t = new TimeLimit(Config.PRIV_RND_TIME_60S);
            String showText = "请选择第1个角色";
            Person p1 = null;
            while (getGameManager().isRunning() && t.isNotTimeout()) {
                getPriviMsg().setOneTimeInfo1(showText);
                p1 = selectPlayer();
                if (p1 == null) {
                    break;
                }
                if (p1.getCards().isEmpty()) {
                    showText = "此角色已无手牌";
                    // printlnToIOPriv("target has no hand cards");
                    continue;
                }
                showText = "\n请选择TA的一张牌";
                getPriviMsg().setOneTimeInfo1(showText);
                Card c1 = chooseAnonymousCard(p1.getCards());
                p1.loseCard(c1, false);
                addCard(c1);
                break;
            }
            showText = "请选择第2个角色";
            Person p2 = null;
            while (getGameManager().isRunning() && t.isNotTimeout()) {
                getPriviMsg().setOneTimeInfo1(showText);
                p2 = selectPlayerExept(getGameManager().getPlayers(), this, p1);
                if (p2 == null) {
                    break;
                }
                if (p2.getCards().isEmpty()) {
                    showText = "此角色已无手牌";
                    // printlnToIOPriv("target has no hand cards");
                    continue;
                }
                showText = "\n请选择TA的一张牌";
                getPriviMsg().setOneTimeInfo1(showText);
                Card c2 = chooseAnonymousCard(p2.getCards());
                p2.loseCard(c2, false);
                addCard(c2);
                break;
            }

            String result = "";
            if (p1 != null) {
                result += p1.getHtmlName();
            }
            if (p2 != null) {
                result += " " + p2.getHtmlName();
            }
            if (p1 != null || p2 != null) {
             
                result = Text.format("%s使用 %s 抽取 %s 一张手牌:<i>没有防备我吧。</i>", 
                getPlateName(), 
                getSkillHtmlName("突袭"),
                result);
                getGameManager().getIo().printlnPublic(result, toString());
                //sleep(3000);
            }
            return;
        }
        super.drawPhase(  fastMode);
    }

    @Override
    public String name() {
        return "张辽";
    }

    @Override
    public String skillsDescription() {
        return "突袭：摸牌阶段，你可以改为获得最多两名角色的各一张手牌。";
    }
}
