package edu.unac.service.impl;

import edu.unac.model.card.Card;
import edu.unac.model.card.NumericCard;
import edu.unac.model.enums.GameStatus;
import edu.unac.model.enums.PlayerStatus;
import edu.unac.model.game.Game;
import edu.unac.model.game.Player;
import edu.unac.repository.GameRepository;
import edu.unac.service.DeckService;
import edu.unac.service.GameService;
import edu.unac.service.ScoreService;
import org.springframework.stereotype.Service;
import java.util.UUID;

import java.util.List;

@Service
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;

    private final DeckService deckService;
    private final ScoreService scoreService;

    public GameServiceImpl(
            GameRepository gameRepository,
            DeckService deckService,
            ScoreService scoreService
    ) {
        this.gameRepository = gameRepository;
        this.deckService = deckService;
        this.scoreService = scoreService;
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

    @Override
    public Game startRound(UUID gameId) {

        Game game = gameRepository.findById(gameId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Game not found"
                        )
                );

        game.setCurrentRound(
                game.getCurrentRound() + 1
        );

        game.setDeck(
                deckService.buildDeck()
        );

        dealInitialCards(game);

        for (Player player : game.getPlayers()) {

            player.setRoundScore(0);
            player.setSecondChance(false);
            player.setFlippedSeven(false);
        }

        int startingPlayerIndex =
                (game.getCurrentRound() - 1)
                        % game.getPlayers().size();

        Player startingPlayer =
                game.getPlayers().get(startingPlayerIndex);

        game.setCurrentPlayerId(
                startingPlayer.getId()
        );

        game.setDealerId(
                startingPlayer.getId()
        );

        game.setStatus(
                GameStatus.IN_ROUND
        );

        return gameRepository.save(game);
    }

    private void dealInitialCards(Game game) {

        int startIndex = getStartingPlayerIndex(game);

        int totalPlayers = game.getPlayers().size();

        for (int i = 0; i < totalPlayers; i++) {

            int playerIndex =
                    (startIndex + i) % totalPlayers;

            Player player =
                    game.getPlayers().get(playerIndex);

            Card card =
                    game.getDeck().remove(0);

            player.getHand().add(card);
        }
    }

    private int getStartingPlayerIndex(Game game) {

        return (game.getCurrentRound() - 1)
                % game.getPlayers().size();
    }

    @Override
    public Game drawCard(
            UUID gameId,
            UUID playerId
    ) {

        Game game = gameRepository.findById(gameId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Game not found"
                        )
                );

        if (!playerId.equals(
                game.getCurrentPlayerId()
        )) {

            throw new IllegalArgumentException(
                    "Not your turn"
            );
        }

        Player player =
                game.getPlayers()
                        .stream()
                        .filter(p ->
                                p.getId()
                                        .equals(playerId))
                        .findFirst()
                        .orElseThrow();

        Card card =
                game.getDeck().remove(0);

        player.getHand().add(card);

        player.setRoundScore(
                scoreService.calculateScore(player)
        );


        if (isBusted(player)) {

            player.setStatus(
                    PlayerStatus.BUSTED
            );
        }

        advanceTurn(game);

        checkRoundEnd(game);

        return gameRepository.save(game);
    }


    private boolean isBusted(Player player) {

        List<Integer> values = player.getHand()
                .stream()
                .filter(card -> card instanceof NumericCard)
                .map(card -> ((NumericCard) card).getValue())
                .toList();

        return values.size() != values.stream()
                .distinct()
                .count();
    }

    private void advanceTurn(Game game) {

        int currentIndex = game.getPlayers()
                .stream()
                .map(Player::getId)
                .toList()
                .indexOf(game.getCurrentPlayerId());

        int totalPlayers = game.getPlayers().size();

        for (int i = 1; i <= totalPlayers; i++) {

            Player candidate =
                    game.getPlayers()
                            .get((currentIndex + i)
                                    % totalPlayers);

            if (candidate.getStatus()
                    == PlayerStatus.ACTIVE) {

                game.setCurrentPlayerId(
                        candidate.getId()
                );

                return;
            }
        }

        game.setCurrentPlayerId(null);
    }

    @Override
    public Game stay(UUID gameId, UUID playerId) {

        Game game = getGameById(gameId);

        if (!playerId.equals(game.getCurrentPlayerId())) {
            throw new IllegalArgumentException("Not your turn");
        }

        Player player = findPlayer(game, playerId);

        player.setStatus(PlayerStatus.STAYED);

        advanceTurn(game);

        checkRoundEnd(game);

        return gameRepository.save(game);
    }

    private Player findPlayer(
            Game game,
            UUID playerId
    ) {

        return game.getPlayers()
                .stream()
                .filter(p -> p.getId().equals(playerId))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Player not found"
                        )
                );
    }

    private void checkRoundEnd(Game game) {

        boolean activePlayers =
                game.getPlayers()
                        .stream()
                        .anyMatch(
                                p -> p.getStatus()
                                        == PlayerStatus.ACTIVE
                        );

        if (!activePlayers) {

            game.setStatus(
                    GameStatus.ROUND_END
            );

            calculateRoundScores(game);
        }
    }

    private void calculateRoundScores(Game game) {

        for (Player player : game.getPlayers()) {

            if (player.getStatus()
                    != PlayerStatus.BUSTED) {

                player.setTotalScore(
                        player.getTotalScore()
                                + player.getRoundScore()
                );
            }
        }
    }



}