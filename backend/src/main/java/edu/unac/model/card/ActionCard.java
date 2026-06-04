package edu.unac.model.card;

import edu.unac.model.enums.CardType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@DiscriminatorValue("ACTION")
public class ActionCard extends Card {

    public ActionCard(CardType type) {

        super(type);

        if (type != CardType.FREEZE
                && type != CardType.FLIP_THREE
                && type != CardType.SECOND_CHANCE) {

            throw new IllegalArgumentException(
                    "ActionCard only supports action card types"
            );
        }
    }
}