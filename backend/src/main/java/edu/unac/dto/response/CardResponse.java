package edu.unac.dto.response;

import edu.unac.model.enums.CardType;
import edu.unac.model.enums.ModifierType;
import lombok.Data;

import java.util.UUID;

@Data
public class CardResponse {
    private UUID id;
    private String kind; // NUMBER, ACTION, MODIFIER
    private CardType type;
    private Integer value;
    private ModifierType modifier;
}
