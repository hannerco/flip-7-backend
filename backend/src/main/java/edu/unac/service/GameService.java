package edu.unac.service;

import edu.unac.model.game.Game;

import java.util.List;
import java.util.UUID;

public interface GameService {

    Game createGame(List<String> playerNames);

    Game getGameById(UUID gameId);

    Game startRound(UUID gameId);

    Game drawCard(UUID gameId, UUID playerId);

    Game stay(UUID gameId, UUID playerId);
}