package sanguosha.skills;

import sanguosha.people.Person;

/**
 * 注入技能，因别人而添加的技能
 */
public abstract class InjectSkill {
    private boolean hasLaunched;
    private String skillName;

    public InjectSkill(String skillName) {
        this.skillName = skillName;
        hasLaunched = false;
    }

    /**
     * 标记本局使用过
     */
    public void setLaunched() {
        hasLaunched = true;
    }

    /**
     * 是否本局使用过
     */
    public boolean isLaunched() {
        return hasLaunched;
    }

    /**
     * 新的一轮，如果每轮都是新的，就刷新
     */
    public void setNotLaunched() {
        hasLaunched = false;
    }

    public abstract boolean use( Person target);

    @Override
    public String toString() {
        return skillName;
    }
}
