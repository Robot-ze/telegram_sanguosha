package sanguosha.cards;

import sanguosha.manager.GameManager;

public abstract class JudgeCard extends Strategy {

    public JudgeCard(GameManager gameManager,Color color, int number, int distance) {
        super(  gameManager,color, number, distance);
    }

    public JudgeCard(GameManager gameManager,Color color, int number) {
        super(  gameManager,color, number);
    }

    @Override
    public abstract String use();

}
