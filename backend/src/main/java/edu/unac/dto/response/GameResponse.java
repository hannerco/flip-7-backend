package edu.unac.dto.response;

import edu.unac.model.enums.GameStatus;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class GameResponse {
    private UUID id;
    private GameStatus status;
    private Integer currentRound;
    private UUID currentPlayerId;
    private UUID dealerId;
    private UUID roundStarterId;
    private List<PlayerResponse> players;
    private List<RoundResultResponse> roundHistory;
    private WinnerResponse winner;
    private PendingActionResponse pendingAction;
    private AutomaticEventResponse lastAutomaticEvent;
}
