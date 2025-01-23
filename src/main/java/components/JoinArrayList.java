package components;

import java.util.List;

import sanguosha.cards.Card;
import sanguosha.skills.InjectSkill;

import java.util.AbstractList;

/**
 * 两个表拼成一个表,挺傻的方法，好在参数少
 */
public class JoinArrayList extends AbstractList<String> {
    private List<InjectSkill> listA;
    private List<String> listB;
    private List<Card> listC;

    public JoinArrayList(List<InjectSkill> listA, List<String> listB, List<Card> listC) {
        this.listA = listA;
        this.listB = listB;
        this.listC = listC;
    }

    @Override
    public int size() {
        return listA.size() + listB.size()+ listC.size();
    }

    @Override
    public String get(int index) {
        if (index < listA.size()) {
            return listA.get(index).toString();
        } else if (index <( listA.size()+listB.size()) ) {
            return listB.get(index - listA.size());
        } else {
            Card c = listC.get(index - listA.size() - listB.size());
            return c.info() + c.toString();
        }
    }
}
