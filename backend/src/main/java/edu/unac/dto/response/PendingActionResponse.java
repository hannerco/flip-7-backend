package edu.unac.dto.response;

import edu.unac.model.enums.CardType;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class PendingActionResponse {
    private UUID id;
    private CardType type;
    private CardResponse card;
    private UUID sourcePlayerId;
    private Integer remainingCards;
    private List<UUID> targetOptions; // player ids that can be targeted
}
