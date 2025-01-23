package sanguosha.cards;

import sanguosha.manager.GameManager;

public abstract class BasicCard extends Card {

    public BasicCard(GameManager gameManager,Color color, int number) {
        super(gameManager,color, number);
    }
}
