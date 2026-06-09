package edu.unac.dto.response;

import edu.unac.model.game.AutomaticEvent;
import lombok.Data;

import java.util.UUID;

@Data
public class AutomaticEventResponse {
    private AutomaticEvent.Type type;
    private UUID playerId;
    private CardResponse removedDuplicateCard;
    private CardResponse removedSecondChanceCard;
}
