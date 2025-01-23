package sanguosha.people.god;

import config.DescUrl;
import config.Text;
import sanguosha.manager.Utils;
import sanguosha.people.Identity;
import sanguosha.people.Nation;
import sanguosha.people.Person;

public abstract class God extends Person {
    public God(int maxHP, Nation nation) {
        super(maxHP, nation);
    }

    @Override
    public void initialize(Identity identity,int uerPos) {
       
        int nationIdx = -1;
        // printlnToIOPriv("you are GOD! select a nation");
        String res = Text.format("💬%s 你的身份是%s,位置是%s号位,你神力非凡，请选择你想归属的势力", toString(),
                DescUrl.getDescHtml(Person.getIdentityString(identity)),
                uerPos);
        getPriviMsg().appendOneTimeInfo1(res);
        Nation[] nationEnum = new Nation[] { Nation.WEI, Nation.SHU, Nation.WU, Nation.QUN };
        nationIdx = chooseNoNull("魏国", "蜀国", "吴国", "群英");
        if (nationIdx < 1) {
            nationIdx = Utils.randint(1, nationEnum.length);
        }
        setNation(nationEnum[nationIdx - 1]);
    }

    // @Override
    // public void run(boolean isNotHuashen ) {
    // //printlnPriv("========GOD IS COMING!========");
    // super.run( isNotHuashen);
    // }
}
