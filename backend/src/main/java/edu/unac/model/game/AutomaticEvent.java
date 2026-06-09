package edu.unac.model.game;

import edu.unac.model.card.Card;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AutomaticEvent {

    public enum Type {
        SECOND_CHANCE_CONSUMED
    }

    private Type type;

    private UUID playerId;

    private Card removedDuplicateCard; // the numeric card that would have caused bust

    private Card removedSecondChanceCard; // the SECOND_CHANCE card that was consumed
}
