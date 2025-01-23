package sanguosha.cardsheap;

import static sanguosha.people.Identity.KING;
import static sanguosha.people.Identity.MINISTER;
import static sanguosha.people.Identity.REBEL;
import static sanguosha.people.Identity.TRAITOR;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;

import sanguosha.manager.GameManager;
import sanguosha.manager.Utils;
import sanguosha.people.Identity;
import sanguosha.people.Person;
import sanguosha.people.fire.DianWei;
import sanguosha.people.fire.PangDe;
import sanguosha.people.fire.PangTong;
import sanguosha.people.fire.TaiShiCi;
import sanguosha.people.fire.WoLong;
import sanguosha.people.fire.XunYu;
import sanguosha.people.fire.YanLiangWenChou;
import sanguosha.people.fire.YuanShao;
import sanguosha.people.forest.CaoPi;
import sanguosha.people.forest.DongZhuo;
import sanguosha.people.forest.JiaXu;
import sanguosha.people.forest.LuSu;
import sanguosha.people.forest.MengHuo;
import sanguosha.people.forest.SunJian;
import sanguosha.people.forest.XuHuang;
import sanguosha.people.forest.ZhuRong;
import sanguosha.people.god.ShenCaoCao;
import sanguosha.people.god.ShenGuanYu;
import sanguosha.people.god.ShenLvBu;
import sanguosha.people.god.ShenLvMeng;
import sanguosha.people.god.ShenSiMaYi;
import sanguosha.people.god.ShenZhaoYun;
import sanguosha.people.god.ShenZhouYu;
import sanguosha.people.god.ShenZhuGeLiang;
import sanguosha.people.mountain.CaiWenJi;
import sanguosha.people.mountain.DengAi;
import sanguosha.people.mountain.JiangWei;
import sanguosha.people.mountain.LiuChan;
import sanguosha.people.mountain.SunCe;
import sanguosha.people.mountain.ZhangHe;
import sanguosha.people.mountain.ZhangZhaoZhangHong;
import sanguosha.people.mountain.ZuoCi;
import sanguosha.people.qun.DiaoChan;
import sanguosha.people.qun.HuaTuo;
import sanguosha.people.qun.LvBu;
import sanguosha.people.qun.YuanShu;
import sanguosha.people.shu.GuanYu;
import sanguosha.people.shu.HuangYueYing;
import sanguosha.people.shu.LiuBei;
import sanguosha.people.shu.MaChao;
import sanguosha.people.shu.ZhangFei;
import sanguosha.people.shu.ZhaoYun;
import sanguosha.people.shu.ZhuGeLiang;
import sanguosha.people.wei.CaoCao;
import sanguosha.people.wei.GuoJia;
import sanguosha.people.wei.SiMaYi;
import sanguosha.people.wei.XiaHouDun;
import sanguosha.people.wei.ZhangLiao;
import sanguosha.people.wei.ZhenJi;
import sanguosha.people.wind.CaoRen;
import sanguosha.people.wind.HuangZhong;
import sanguosha.people.wind.WeiYan;
import sanguosha.people.wind.XiaHouYuan;
import sanguosha.people.wind.XiaoQiao;
import sanguosha.people.wind.YuJi;
import sanguosha.people.wind.ZhangJiao;
import sanguosha.people.wind.ZhouTai;
import sanguosha.people.wu.DaQiao;
import sanguosha.people.wu.GanNing;
import sanguosha.people.wu.HuangGai;
import sanguosha.people.wu.LuXun;
import sanguosha.people.wu.LvMeng;
import sanguosha.people.wu.SunQuan;
import sanguosha.people.wu.SunShangXiang;
import sanguosha.people.wu.ZhouYu;

public class PeoplePool {
    private final Deque<Person> people = new ConcurrentLinkedDeque<>();
    private final Map<Class<? extends Person>, Person> peopleIdex = new ConcurrentHashMap<>();
    // private final Set<Person> allPeople = new CopyOnWriteArraySet<>();
    private final List<Identity> identities = new CopyOnWriteArrayList<>();
    private int optionsPerPerson = 3;
    private int numPlayers;
    private GameManager gameManager;

    public PeoplePool(int optionsPerPerson) {
        this.optionsPerPerson = optionsPerPerson;
    }

    /**
     * 添加标准角色
     */ 
    public void addStandard() {
        // if (allPeople.isEmpty()) {
        // addFeng();
        // addHuo();
        // addLin();
        // addShan();
        // addGod();
        // allPeople.addAll(people);
        // addStandard();
        // allPeople.addAll(people);
        // people.clear();
        // }
        // 蜀国
        people.add(new GuanYu());
        people.add(new HuangYueYing());
        people.add(new LiuBei());
        people.add(new MaChao());
        people.add(new ZhangFei());
        people.add(new ZhaoYun());
        people.add(new ZhuGeLiang());

        // 魏国
        people.add(new CaoCao());
        people.add(new GuoJia());
        people.add(new SiMaYi());
        people.add(new XiaHouDun());
        people.add(new XunYu());
        people.add(new ZhangLiao());
        people.add(new ZhenJi());

        // 吴国
        people.add(new DaQiao());
        people.add(new GanNing());
        people.add(new HuangGai());
        people.add(new LuXun());
        people.add(new LvMeng());
        people.add(new SunQuan());
        people.add(new SunShangXiang());
        people.add(new ZhouYu());

        // 群雄
        people.add(new HuaTuo());
        people.add(new LvBu());
        people.add(new DiaoChan());
        people.add(new YuanShu());
    }

    public void addFeng() {
        people.add(new CaoRen());
        people.add(new HuangZhong());
        people.add(new WeiYan());
        people.add(new XiaHouYuan());
        people.add(new XiaoQiao());
        people.add(new YuJi());
        people.add(new ZhangJiao());
        people.add(new ZhouTai());
    }

    /**
     * 添加火包角色
     */
    public void addHuo() {
        people.add(new DianWei());
        people.add(new PangDe());
        people.add(new PangTong());
        people.add(new TaiShiCi());
        people.add(new WoLong());
        people.add(new XunYu());
        people.add(new YanLiangWenChou());
        people.add(new YuanShao());
    }

    /**
     * 添加林包角色
     */
    public void addLin() {
        people.add(new CaoPi());
        people.add(new DongZhuo());
        people.add(new JiaXu());
        people.add(new LuSu());
        people.add(new MengHuo());
        people.add(new SunJian());
        people.add(new XuHuang());
        people.add(new ZhuRong());
    }

    /**
     * 添加山包角色
     */
    public void addShan() {
        people.add(new CaiWenJi());
        people.add(new DengAi());
        people.add(new JiangWei());
        people.add(new LiuChan());
        people.add(new SunCe());
        people.add(new ZhangHe());
        people.add(new ZhangZhaoZhangHong());
        people.add(new ZuoCi());
    }

    /**
     * 添加神包角色
     */
    public void addGod() {
        people.add(new ShenCaoCao());
        people.add(new ShenGuanYu());
        people.add(new ShenLvBu());
        people.add(new ShenLvMeng());
        people.add(new ShenSiMaYi());
        people.add(new ShenZhaoYun());
        people.add(new ShenZhouYu());
        people.add(new ShenZhuGeLiang());
    }

    /**
     * 添加身份
     * @param id 身份
     * @param num 数量
     */
    public void addIdentity(Identity id, int num) {
        for (int i = 0; i < num; i++) {
            identities.add(id);
        }
    }

    /**
     * 默认主公数量
     * @param total 总人数
     * @return 主公数量
     */
    public int defaultMinister(int total) {
        return total == 10 ? 3 : total >= 6 ? 2 : total >= 4 ? 1 : 0;
    }

    /**
     * 默认内奸数量
     * @param total 总人数
     * @return 内奸数量
     */
    public int defaultTraitor(int total) {
        return total >= 9 ? 2 : total >= 3 ? 1 : 0;
    }

    /**
     * 默认反贼数量
     * @param total 总人数
     * @return 反贼数量
     */
    public int defaultRebel(int total) {
        return total >= 8 ? 4 : total >= 7 ? 3 : total >= 5 ? 2 : 1;
    }

    /**
     * 初始化
     * @param gameManager 游戏管理器
     */
    public void init(GameManager gameManager) {
        this.gameManager = gameManager;
        numPlayers = this.gameManager.getNumPlayers();
        addStandard();
        addFeng();
        addHuo();
        addLin();
        addShan();
        addGod();
        for (Person p : people) {
            peopleIdex.put(p.getClass(), p);
        }

        switch (this.gameManager.getNumPlayers()) {
            case 10:
                addIdentity(MINISTER, 1);
                // fallthrough
            case 9:
                addIdentity(TRAITOR, 1);
                // fallthrough
            case 8:
                addIdentity(REBEL, 1);
                // fallthrough
            case 7:
                addIdentity(REBEL, 1);
                // fallthrough
            case 6:
                addIdentity(MINISTER, 1);
                // fallthrough
            case 5:
                addIdentity(REBEL, 1);
                // fallthrough
            case 4:
                addIdentity(MINISTER, 1);
                // fallthrough
            case 3:
                addIdentity(TRAITOR, 1);
                // fallthrough
            case 2:
                addIdentity(KING, 1);
                addIdentity(REBEL, 1);
                break;
            default:
                gameManager.getIo().panic("invalid players: " + gameManager.getNumPlayers());
        }

        //混淆身份
        Collections.shuffle(identities);
    }

    /**
     * 设置玩家数量
     * @param numPlayers 玩家数量
     */
    public void setNumPlayers(int numPlayers) {
        gameManager.getPeoplePool().numPlayers = numPlayers;
    }

    /**
     * 检查身份数量是否合法
     * @return 是否合法
     */
    public boolean illegalIdentityNumber() {
        return identities.size() != gameManager.getNumPlayers();
    }

    /**
     * 检查选项数量是否合法
     * @return 是否合法
     */
    public boolean illegalOptionsPerPerson() {
        System.out.println(
                "people.size()  numPlayers * optionsPerPerson " + people.size() + " " + numPlayers * optionsPerPerson);
        return people.size() < numPlayers * optionsPerPerson;
    }

    /**
     * 重新开始
     */
    public void restart() {
        people.clear();
        identities.clear();
    }

    /**
     * 添加角色到列表
     * @param cls 角色类
     * @param kings 角色列表
     */
    public void addPersonToList(Class<? extends Person> cls, ArrayList<Person> kings) {
        Person p = peopleIdex.get(cls);
        kings.add(p);
        people.remove(p);
    }

    /**
     * 分配主公
     * @param roleNum 角色数量
     * @return 角色列表
     */
    public ArrayList<Person> allocPeopleForKing(int roleNum) {
        ArrayList<Person> options = new ArrayList<>();
        // if (optionsPerPerson > 3) {
        // addPersonToList(LiuBei.class, options);
        // addPersonToList(CaoCao.class, options);
        // addPersonToList(SunQuan.class, options);
        // }
        // if (optionsPerPerson > 10) {
        // addPersonToList(ZhangJiao.class, options);
        // addPersonToList(YuanShao.class, options);
        // addPersonToList(DongZhuo.class, options);
        // addPersonToList(CaoPi.class, options);
        // addPersonToList(LiuChan.class, options);
        // addPersonToList(SunCe.class, options);
        // }
        addPersonToList(LiuBei.class, options);
        addPersonToList(CaoCao.class, options);
        addPersonToList(SunQuan.class, options);
        addPersonToList(ZhangJiao.class, options);
        addPersonToList(YuanShao.class, options);
        addPersonToList(DongZhuo.class, options);
        addPersonToList(CaoPi.class, options);
        addPersonToList(LiuChan.class, options);
        addPersonToList(SunCe.class, options);

        Collections.shuffle(options);

        while (options.size() > roleNum) {
            //移除并加回玩家可选列表中
            Person kingRole = options.get(options.size() - 1);
            options.remove(options.size() - 1);
            people.add(kingRole);
        }
 

        Utils.assertTrue(people.size() >= optionsPerPerson, "No people available");
        // options.addAll(people.subList(0, optionsPerPerson - options.size()));

        // options.addAll(people.subList(0, 9 - options.size()));//这句话意思不是主公的的不能玩主公
        // for(int i=0;i<(9 - options.size());i++){
        // options.add(people.pollFirst());
        // }
        // people.removeAll(options);
        return options;
    }

    /**
     * 洗人物牌
     */
    public void shufflePeople() {
        //System.out.println("执行shufflePeople");
        //System.out.println("1people="+people);
        List<Person> peopleList = new ArrayList<>(people);
        Collections.shuffle(peopleList);
        people.clear();
        people.addAll(peopleList);
        //System.out.println("2people="+people);
    }

    /** 这是给选人的, 是个多线程安全方法，因为people队列是线程安全的 */
    public ArrayList<Person> allocPeople() {
        Utils.assertTrue(people.size() >= optionsPerPerson, "No people available");
        ArrayList<Person> ans = new ArrayList<>();

        // ("optionsPerPerson="+optionsPerPerson);
        for (int i = 0; i < optionsPerPerson; i++) {
            ans.add(people.pollFirst());
        }
        // people.removeAll(ans);
        return ans;
    }

    /**
     * 只是给左慈用的
     * 
     * @return
     */
    public Person allocOnePerson() {
        Utils.assertTrue(people.size() >= 1, "No people available");
        if (people.size() == 0) {
            return null;
        }
        Person ans = people.pollFirst();
        // people.remove(people.size()-1);
        if (ans instanceof ZuoCi) {
            if (people.size() == 0) {
                return null;
            }
            ans = people.pollFirst();
            // people.remove(people.size()-1);
        }
        return ans;
    }

    /**
     * 分配主公身份
     * @return 主公身份
     */
    public Identity allocIdentityForKing() {
        Utils.assertTrue(identities.contains(KING), "KING not in identities");
        identities.remove(KING);
        return KING;
    }

    /**
     * 分配身份
     * @return 身份
     */
    public Identity allocIdentity() {
        Utils.assertTrue(identities.size() >= 1, "No identity available");
        Identity ans = identities.get(0);
        identities.remove(ans);
        return ans;
    }

    /**
     * 获取人物牌
     * @return 人物牌
     */
    public Deque<Person> getPeople() {
        return people;
    }

    /**
     * 重新丢进池子
     * @param p
     */
    public void rePut(Person p){
        people.addLast(p);
    }

    // public Set <Person> getAllPeople() {
    // return allPeople;
    // }

    // public void setOptionsPerPerson(int optionsPerPerson) {
    // gameManager.getPeoplePool().optionsPerPerson = optionsPerPerson;
    // }
}
