package edu.unac.service.impl;

import edu.unac.model.card.NumericCard;
import edu.unac.model.enums.GameStatus;
import edu.unac.model.enums.PlayerStatus;
import edu.unac.model.game.*;
import edu.unac.repository.GameRepository;
import edu.unac.service.DeckService;
import edu.unac.service.ScoreService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceImplTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private DeckService deckService;

    @Mock
    private ScoreService scoreService;

    @InjectMocks
    private GameServiceImpl gameService;
    @Test
    void shouldCreateGameWithTwoPlayers() {

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Game game = gameService.createGame(
                List.of("Alice", "Bob")
        );

        assertNotNull(game);

        assertEquals(2, game.getPlayers().size());

        assertEquals(
                GameStatus.WAITING,
                game.getStatus()
        );

        verify(gameRepository, times(1))
                .save(any(Game.class));
    }

    @Test
    void shouldThrowExceptionWhenLessThanTwoPlayers() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> gameService.createGame(
                                List.of("Alice")
                        )
                );

        assertEquals(
                "Need at least 2 players",
                exception.getMessage()
        );

        verify(gameRepository, never())
                .save(any());
    }

    @Test
    void shouldReturnGameWhenGameExists() {

        Game game = Game.builder().build();

        when(gameRepository.findById(any()))
                .thenReturn(java.util.Optional.of(game));

        Game result =
                gameService.getGameById(
                        java.util.UUID.randomUUID()
                );

        assertNotNull(result);

        verify(gameRepository, times(1))
                .findById(any());
    }

    @Test
    void shouldThrowExceptionWhenGameDoesNotExist() {

        when(gameRepository.findById(any()))
                .thenReturn(java.util.Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> gameService.getGameById(
                                java.util.UUID.randomUUID()
                        )
                );

        assertEquals(
                "Game not found",
                exception.getMessage()
        );

        verify(gameRepository, times(1))
                .findById(any());
    }

    @Test
    void shouldStartRoundSuccessfully() {

        Game game = Game.builder()
                .players(List.of(
                        Player.builder()
                                .id(UUID.randomUUID())
                                .name("Alice")
                                .build(),
                        Player.builder()
                                .id(UUID.randomUUID())
                                .name("Bob")
                                .build()
                ))
                .build();

        when(gameRepository.findById(any()))
                .thenReturn(java.util.Optional.of(game));

        when(deckService.buildDeck())
                .thenReturn(new ArrayList<>(List.of(
                        new NumericCard(7),
                        new NumericCard(8),
                        new NumericCard(9)
                )));

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Game result =
                gameService.startRound(
                        UUID.randomUUID()
                );

        assertEquals(
                1,
                result.getCurrentRound()
        );

        assertEquals(
                GameStatus.IN_ROUND,
                result.getStatus()
        );

        assertNotNull(
                result.getCurrentPlayerId()
        );

        assertNotNull(
                result.getDealerId()
        );

        assertFalse(
                result.getDeck().isEmpty()
        );
    }

    @Test
    void shouldDealOneCardToEachPlayerWhenRoundStarts() {

        Game game = Game.builder()
                .players(List.of(
                        Player.builder()
                                .id(UUID.randomUUID())
                                .name("Alice")
                                .build(),
                        Player.builder()
                                .id(UUID.randomUUID())
                                .name("Bob")
                                .build()
                ))
                .build();

        when(gameRepository.findById(any()))
                .thenReturn(Optional.of(game));

        when(deckService.buildDeck())
                .thenReturn(new ArrayList<>(List.of(
                        new NumericCard(7),
                        new NumericCard(8),
                        new NumericCard(9)
                )));

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Game result =
                gameService.startRound(UUID.randomUUID());

        assertEquals(
                1,
                result.getPlayers().get(0).getHand().size()
        );

        assertEquals(
                1,
                result.getPlayers().get(1).getHand().size()
        );

        assertEquals(
                1,
                result.getDeck().size()
        );
    }

    @Test
    void shouldDealStartingWithNextPlayerEachRound() {

        Player alice = Player.builder()
                .id(UUID.randomUUID())
                .name("Alice")
                .build();

        Player bob = Player.builder()
                .id(UUID.randomUUID())
                .name("Bob")
                .build();

        Game game = Game.builder()
                .currentRound(1)
                .players(List.of(alice, bob))
                .build();

        when(gameRepository.findById(any()))
                .thenReturn(Optional.of(game));

        when(deckService.buildDeck())
                .thenReturn(new ArrayList<>(List.of(
                        new NumericCard(100),
                        new NumericCard(200),
                        new NumericCard(300)
                )));

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Game result =
                gameService.startRound(UUID.randomUUID());

        NumericCard bobCard =
                (NumericCard) result.getPlayers().get(1)
                        .getHand()
                        .get(0);

        NumericCard aliceCard =
                (NumericCard) result.getPlayers().get(0)
                        .getHand()
                        .get(0);

        assertEquals(
                100,
                bobCard.getValue()
        );

        assertEquals(
                200,
                aliceCard.getValue()
        );
    }

    @Test
    void shouldDrawCardSuccessfully() {

        UUID playerId = UUID.randomUUID();

        Player player = Player.builder()
                .id(playerId)
                .name("Alice")
                .build();

        Game game = Game.builder()
                .currentPlayerId(playerId)
                .players(List.of(player))
                .deck(new ArrayList<>(List.of(
                        new NumericCard(7),
                        new NumericCard(8)
                )))
                .build();

        when(gameRepository.findById(any()))
                .thenReturn(Optional.of(game));

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(scoreService.calculateScore(any()))
                .thenReturn(7);


        Game result =
                gameService.drawCard(
                        UUID.randomUUID(),
                        playerId
                );

        assertEquals(
                1,
                result.getPlayers()
                        .get(0)
                        .getHand()
                        .size()
        );

        assertEquals(
                1,
                result.getDeck()
                        .size()
        );
    }

    @Test
    void shouldUpdateRoundScoreAfterDrawingCard() {

        UUID playerId = UUID.randomUUID();

        Player player = Player.builder()
                .id(playerId)
                .name("Alice")
                .build();

        Game game = Game.builder()
                .currentPlayerId(playerId)
                .players(List.of(player))
                .deck(new ArrayList<>(List.of(
                        new NumericCard(7)
                )))
                .build();

        when(gameRepository.findById(any()))
                .thenReturn(Optional.of(game));

        when(scoreService.calculateScore(any()))
                .thenReturn(7);

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Game result =
                gameService.drawCard(
                        UUID.randomUUID(),
                        playerId
                );

        assertEquals(
                7,
                result.getPlayers()
                        .get(0)
                        .getRoundScore()
        );
    }

    @Test
    void shouldBustPlayerWhenDrawingDuplicateNumber() {

        UUID playerId = UUID.randomUUID();

        Player player = Player.builder()
                .id(playerId)
                .name("Alice")
                .hand(new ArrayList<>(List.of(
                        new NumericCard(7)
                )))
                .build();

        Game game = Game.builder()
                .currentPlayerId(playerId)
                .players(List.of(player))
                .deck(new ArrayList<>(List.of(
                        new NumericCard(7)
                )))
                .build();

        when(gameRepository.findById(any()))
                .thenReturn(Optional.of(game));

        when(scoreService.calculateScore(any()))
                .thenReturn(14);

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Game result = gameService.drawCard(
                UUID.randomUUID(),
                playerId
        );

        assertEquals(
                PlayerStatus.BUSTED,
                result.getPlayers()
                        .get(0)
                        .getStatus()
        );
    }

    @Test
    void shouldAdvanceTurnAfterDrawingCard() {

        UUID aliceId = UUID.randomUUID();
        UUID bobId = UUID.randomUUID();

        Player alice = Player.builder()
                .id(aliceId)
                .name("Alice")
                .build();

        Player bob = Player.builder()
                .id(bobId)
                .name("Bob")
                .build();

        Game game = Game.builder()
                .currentPlayerId(aliceId)
                .players(List.of(
                        alice,
                        bob
                ))
                .deck(new ArrayList<>(List.of(
                        new NumericCard(7)
                )))
                .build();

        when(gameRepository.findById(any()))
                .thenReturn(Optional.of(game));

        when(scoreService.calculateScore(any()))
                .thenReturn(7);

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Game result = gameService.drawCard(
                UUID.randomUUID(),
                aliceId
        );

        assertEquals(
                bobId,
                result.getCurrentPlayerId()
        );
    }

    @Test
    void shouldStaySuccessfully() {

        UUID playerId = UUID.randomUUID();

        Player player = Player.builder()
                .id(playerId)
                .name("Alice")
                .build();

        Game game = Game.builder()
                .currentPlayerId(playerId)
                .players(List.of(player))
                .build();

        when(gameRepository.findById(any()))
                .thenReturn(Optional.of(game));

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Game result = gameService.stay(
                UUID.randomUUID(),
                playerId
        );

        assertEquals(
                PlayerStatus.STAYED,
                result.getPlayers()
                        .get(0)
                        .getStatus()
        );
    }

    @Test
    void shouldSkipStayedPlayers() {

        UUID aliceId = UUID.randomUUID();
        UUID bobId = UUID.randomUUID();
        UUID charlieId = UUID.randomUUID();

        Player alice = Player.builder()
                .id(aliceId)
                .name("Alice")
                .status(PlayerStatus.ACTIVE)
                .build();

        Player bob = Player.builder()
                .id(bobId)
                .name("Bob")
                .status(PlayerStatus.STAYED)
                .build();

        Player charlie = Player.builder()
                .id(charlieId)
                .name("Charlie")
                .status(PlayerStatus.ACTIVE)
                .build();

        Game game = Game.builder()
                .currentPlayerId(aliceId)
                .players(List.of(
                        alice,
                        bob,
                        charlie
                ))
                .deck(new ArrayList<>(List.of(
                        new NumericCard(7)
                )))
                .build();

        when(gameRepository.findById(any()))
                .thenReturn(Optional.of(game));

        when(scoreService.calculateScore(any()))
                .thenReturn(7);

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Game result = gameService.drawCard(
                UUID.randomUUID(),
                aliceId
        );

        assertEquals(
                charlieId,
                result.getCurrentPlayerId()
        );
    }

    @Test
    void shouldSkipBustedPlayers() {

        UUID aliceId = UUID.randomUUID();
        UUID bobId = UUID.randomUUID();
        UUID charlieId = UUID.randomUUID();

        Player alice = Player.builder()
                .id(aliceId)
                .name("Alice")
                .status(PlayerStatus.ACTIVE)
                .build();

        Player bob = Player.builder()
                .id(bobId)
                .name("Bob")
                .status(PlayerStatus.BUSTED)
                .build();

        Player charlie = Player.builder()
                .id(charlieId)
                .name("Charlie")
                .status(PlayerStatus.ACTIVE)
                .build();

        Game game = Game.builder()
                .currentPlayerId(aliceId)
                .players(List.of(
                        alice,
                        bob,
                        charlie
                ))
                .deck(new ArrayList<>(List.of(
                        new NumericCard(7)
                )))
                .build();

        when(gameRepository.findById(any()))
                .thenReturn(Optional.of(game));

        when(scoreService.calculateScore(any()))
                .thenReturn(7);

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Game result = gameService.drawCard(
                UUID.randomUUID(),
                aliceId
        );

        assertEquals(
                charlieId,
                result.getCurrentPlayerId()
        );
    }

    @Test
    void shouldEndRoundWhenNoActivePlayersRemain() {

        UUID playerId = UUID.randomUUID();

        Player player = Player.builder()
                .id(playerId)
                .name("Alice")
                .status(PlayerStatus.ACTIVE)
                .roundScore(15)
                .build();

        Game game = Game.builder()
                .currentPlayerId(playerId)
                .players(List.of(player))
                .build();

        when(gameRepository.findById(any()))
                .thenReturn(Optional.of(game));

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Game result = gameService.stay(
                UUID.randomUUID(),
                playerId
        );

        assertEquals(
                GameStatus.ROUND_END,
                result.getStatus()
        );
    }

    @Test
    void shouldAddRoundScoreToTotalScoreAtRoundEnd() {

        UUID playerId = UUID.randomUUID();

        Player player = Player.builder()
                .id(playerId)
                .name("Alice")
                .roundScore(25)
                .totalScore(100)
                .status(PlayerStatus.ACTIVE)
                .build();

        Game game = Game.builder()
                .currentPlayerId(playerId)
                .players(List.of(player))
                .build();

        when(gameRepository.findById(any()))
                .thenReturn(Optional.of(game));

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Game result = gameService.stay(
                UUID.randomUUID(),
                playerId
        );

        assertEquals(
                125,
                result.getPlayers()
                        .get(0)
                        .getTotalScore()
        );
    }
}