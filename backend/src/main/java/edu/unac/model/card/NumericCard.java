package edu.unac.model.card;

import edu.unac.model.enums.CardType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@DiscriminatorValue("NUMBER")
public class NumericCard extends Card {

    private Integer value;

    public NumericCard(Integer value) {
        super(CardType.NUMBER);
        this.value = value;
    }
}