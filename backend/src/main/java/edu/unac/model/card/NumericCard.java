package edu.unac.model.card;

import edu.unac.model.enums.CardType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@DiscriminatorValue("NUMBER")
public class NumericCard extends Card {

    @Column(name = "card_value")
    private Integer value;

    public NumericCard(Integer value) {
        super(CardType.NUMBER);
        this.value = value;
    }
}