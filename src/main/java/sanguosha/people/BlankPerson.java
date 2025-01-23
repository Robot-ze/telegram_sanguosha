package sanguosha.people;

public class BlankPerson extends Person {
    private String name;

    public BlankPerson() {
        super(4,  "male", Nation.QUN);
    }

    public BlankPerson(int maxHP) {
        super(maxHP,  "male", Nation.QUN);
    }

    @Override
    public void setName(String name){
        System.out.println("name="+name);
        this.name=name;
    }

    @Override
    public String name() {
        return  name;
    }

    @Override
    public String skillsDescription() {
        return "我是快乐的小白板";
    }
}
