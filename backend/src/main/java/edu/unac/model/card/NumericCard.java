package edu.unac.model.card;

import edu.unac.model.enums.CardType;

public class NumericCard extends Card {

    private final Integer value;

    public NumericCard(Integer value) {
        super(CardType.NUMBER);
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}