package edu.unac.model.card;

import edu.unac.model.enums.CardType;
import edu.unac.model.enums.ModifierType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@DiscriminatorValue("MODIFIER")
public class ModifierCard extends Card {

    private ModifierType modifier;

    public ModifierCard(ModifierType modifier) {
        super(CardType.MODIFIER);
        this.modifier = modifier;
    }
}