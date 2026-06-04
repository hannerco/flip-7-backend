package edu.unac.model.game;

import edu.unac.model.card.Card;
import edu.unac.model.enums.CardType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PendingAction {

    private CardType type;

    private Card card;

    private UUID sourcePlayerId;
}