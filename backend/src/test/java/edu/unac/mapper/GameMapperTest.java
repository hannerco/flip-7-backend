package edu.unac.mapper;

import edu.unac.dto.response.CreateGameResponse;
import edu.unac.dto.response.FinishedGameResponse;
import edu.unac.dto.response.GameResponse;
import edu.unac.dto.response.PlayerResponse;
import edu.unac.model.enums.GameStatus;
import edu.unac.model.enums.PlayerStatus;
import edu.unac.model.game.Game;
import edu.unac.model.game.Player;
import edu.unac.model.game.RoundPlayerScore;
import edu.unac.model.game.RoundResult;
import edu.unac.model.game.Winner;
import org.junit.jupiter.api.Test;

import java.util.List;
import edu.unac.model.enums.CardType;
import edu.unac.model.card.ActionCard;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GameMapperTest {

    @Test
    void shouldMapGameToResponse() {

        UUID playerId = UUID.randomUUID();

        Player player = Player.builder()
                .id(playerId)
                .name("Alice")
                .totalScore(100)
                .roundScore(20)
                .status(PlayerStatus.ACTIVE)
                .flippedSeven(true)
                .hand(List.of(new ActionCard(CardType.SECOND_CHANCE)))
                .build();

        Game game = Game.builder()
                .id(UUID.randomUUID())
                .status(GameStatus.IN_ROUND)
                .currentRound(3)
                .currentPlayerId(playerId)
                .dealerId(playerId)
                .players(List.of(player))
                .winner(new Winner(playerId, "Alice", 203))
                .roundHistory(List.of(
                        RoundResult.builder()
                                .id(UUID.randomUUID())
                                .roundNumber(3)
                                .scores(List.of(
                                        RoundPlayerScore.builder()
                                                .id(UUID.randomUUID())
                                                .playerId(playerId)
                                                .playerName("Alice")
                                                .score(20)
                                                .busted(false)
                                                .flippedSeven(true)
                                                .build()
                                ))
                                .build()
                ))
                .build();

        GameMapper mapper = new GameMapper();

        GameResponse response =
                mapper.toResponse(game);

        assertEquals(
                game.getId(),
                response.getId()
        );

        assertEquals(
                game.getStatus(),
                response.getStatus()
        );

        assertEquals(
                game.getCurrentRound(),
                response.getCurrentRound()
        );

        assertEquals(
                game.getDealerId(),
                response.getRoundStarterId()
        );

        assertEquals(
                1,
                response.getPlayers().size()
        );

        assertNotNull(response.getWinner());
        assertEquals(playerId, response.getWinner().getId());
        assertEquals("Alice", response.getWinner().getName());
        assertEquals(203, response.getWinner().getTotalScore());

        assertEquals(
                1,
                response.getRoundHistory().size()
        );

        assertEquals(
                3,
                response.getRoundHistory().get(0).getRoundNumber()
        );

        assertEquals(
                1,
                response.getRoundHistory().get(0).getScores().size()
        );
    }

    @Test
    void shouldMapPlayerFieldsCorrectly() {

        UUID playerId = UUID.randomUUID();

        Player player = Player.builder()
                .id(playerId)
                .name("Alice")
                .totalScore(150)
                .roundScore(40)
                .status(PlayerStatus.ACTIVE)
                .flippedSeven(true)
                .hand(List.of(new ActionCard(CardType.SECOND_CHANCE)))
                .build();

        Game game = Game.builder()
                .players(List.of(player))
                .build();

        GameMapper mapper = new GameMapper();

        GameResponse response =
                mapper.toResponse(game);

        PlayerResponse playerResponse =
                response.getPlayers().get(0);

        assertEquals(
                playerId,
                playerResponse.getId()
        );

        assertEquals(
                "Alice",
                playerResponse.getName()
        );

        assertEquals(
                150,
                playerResponse.getTotalScore()
        );

        assertEquals(
                40,
                playerResponse.getRoundScore()
        );

        assertEquals(
                PlayerStatus.ACTIVE,
                playerResponse.getStatus()
        );

        assertTrue(
                playerResponse.getCards().stream().anyMatch(c -> c.getType() == CardType.SECOND_CHANCE)
        );

        assertTrue(
                playerResponse.isFlippedSeven()
        );
    }

    @Test
    void shouldMapGameToCreateResponse() {

        UUID gameId = UUID.randomUUID();

        Game game = Game.builder()
                .id(gameId)
                .build();

        GameMapper mapper = new GameMapper();

        CreateGameResponse response =
                mapper.toCreateResponse(game);

        assertNotNull(response);
        assertEquals(gameId, response.getGameId());
        assertNotNull(response.getGameId()); // ← mata mutante de null en getId()
    }

        @Test
        void shouldMapFinishedGameSummary() {

                UUID winnerId = UUID.randomUUID();

                Game game = Game.builder()
                                .id(UUID.randomUUID())
                                .currentRound(6)
                                .players(List.of(Player.builder().id(winnerId).build()))
                                .winner(new Winner(winnerId, "Bob", 205))
                                .build();

                GameMapper mapper = new GameMapper();

                FinishedGameResponse response = mapper.toFinishedGameResponse(game);

                assertEquals(game.getId(), response.getId());
                assertEquals(6, response.getCurrentRound());
                assertEquals(1, response.getPlayersCount());
                assertNotNull(response.getWinner());
                assertEquals(winnerId, response.getWinner().getId());
                assertEquals(205, response.getWinner().getTotalScore());
        }

        @Test
        void shouldMapRoundHistoryWithPlayerScores() {

                UUID playerId = UUID.randomUUID();

                RoundResult roundResult = RoundResult.builder()
                                .id(UUID.randomUUID())
                                .roundNumber(5)
                                .scores(List.of(
                                                RoundPlayerScore.builder()
                                                                .id(UUID.randomUUID())
                                                                .playerId(playerId)
                                                                .playerName("Bob")
                                                                .score(33)
                                                                .busted(true)
                                                                .flippedSeven(false)
                                                                .build()
                                ))
                                .build();

                Game game = Game.builder()
                                .roundHistory(List.of(roundResult))
                                .build();

                GameMapper mapper = new GameMapper();

                GameResponse response = mapper.toResponse(game);

                assertEquals(1, response.getRoundHistory().size());
                assertEquals(5, response.getRoundHistory().get(0).getRoundNumber());
                assertEquals("Bob", response.getRoundHistory().get(0).getScores().get(0).getPlayerName());
                assertTrue(response.getRoundHistory().get(0).getScores().get(0).isBusted());
        }

}