package edu.unac.model.card;

import edu.unac.model.enums.CardType;
import edu.unac.model.enums.ModifierType;

public class ModifierCard extends Card {

    private final ModifierType modifier;

    public ModifierCard(ModifierType modifier) {
        super(CardType.MODIFIER);
        this.modifier = modifier;
    }

    public ModifierType getModifier() {
        return modifier;
    }
}
