package edu.unac.service.impl;

import edu.unac.model.game.Game;
import edu.unac.model.game.Player;
import edu.unac.repository.GameRepository;
import edu.unac.service.GameService;
import org.springframework.stereotype.Service;
import java.util.UUID;

import java.util.List;

@Service
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;

    public GameServiceImpl(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    @Override
    public Game createGame(List<String> playerNames) {

        if (playerNames == null || playerNames.size() < 2) {
            throw new IllegalArgumentException("Need at least 2 players");
        }

        List<Player> players = playerNames.stream()
                .map(name -> Player.builder()
                        .name(name)
                        .build())
                .toList();

        Game game = Game.builder()
                .players(players)
                .build();

        return gameRepository.save(game);
    }

    @Override
    public Game getGameById(UUID gameId) {

        return gameRepository.findById(gameId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Game not found"
                        )
                );
    }
}