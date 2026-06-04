package edu.unac.model.card;

import edu.unac.model.enums.CardType;

public abstract class Card {

    private final CardType type;

    protected Card(CardType type) {
        this.type = type;
    }

    public CardType getType() {
        return type;
    }
}