package edu.unac.service.impl;

import edu.unac.model.card.ActionCard;
import edu.unac.model.card.NumericCard;
import edu.unac.model.enums.CardType;
import edu.unac.model.enums.GameStatus;
import edu.unac.model.enums.PlayerStatus;
import edu.unac.model.game.*;
import edu.unac.model.game.RoundResult;
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
    void shouldNotStartRoundWhenRoundIsAlreadyInProgress() {

        Game game = Game.builder()
                .status(GameStatus.IN_ROUND)
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

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> gameService.startRound(UUID.randomUUID())
        );

        assertEquals("Round already in progress", exception.getMessage());

        verify(deckService, never()).buildDeck();
        verify(gameRepository, never()).save(any());
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
    void shouldPauseInitialDealWhenAnActionCardAppears() {

        Player alice = Player.builder()
                .id(UUID.randomUUID())
                .name("Alice")
                .build();

        Player bob = Player.builder()
                .id(UUID.randomUUID())
                .name("Bob")
                .build();

        Game game = Game.builder()
                .players(List.of(alice, bob))
                .build();

        when(gameRepository.findById(any()))
                .thenReturn(Optional.of(game));

        when(deckService.buildDeck())
                .thenReturn(new ArrayList<>(List.of(
                        new ActionCard(CardType.FREEZE),
                        new NumericCard(8)
                )));

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Game result = gameService.startRound(UUID.randomUUID());

        assertNotNull(result.getPendingAction());
        assertEquals(CardType.FREEZE, result.getPendingAction().getType());
        assertEquals(alice.getId(), result.getPendingAction().getSourcePlayerId());
        assertEquals(1, result.getInitialDealCardsDealtCount());
        assertTrue(result.isInitialDealPaused());
        assertEquals(0, result.getPlayers().get(0).getHand().size());
        assertEquals(0, result.getPlayers().get(1).getHand().size());
    }

    @Test
    void shouldResumeInitialDealAfterResolvingActionCard() {

        Player alice = Player.builder()
                .id(UUID.randomUUID())
                .name("Alice")
                .status(PlayerStatus.ACTIVE)
                .build();

        Player bob = Player.builder()
                .id(UUID.randomUUID())
                .name("Bob")
                .status(PlayerStatus.ACTIVE)
                .build();

        Game game = Game.builder()
                .currentRound(1)
                .currentPlayerId(alice.getId())
                .dealerId(alice.getId())
                .players(new ArrayList<>(List.of(alice, bob)))
                .deck(new ArrayList<>(List.of(
                        new NumericCard(7),
                        new NumericCard(8)
                )))
                .status(GameStatus.IN_ROUND)
                .initialDealCardsDealtCount(1)
                .initialDealPaused(true)
                .pendingAction(new PendingAction())
                .build();

        game.getPendingAction().setId(null);
        game.getPendingAction().setType(CardType.FREEZE);
        game.getPendingAction().setCard(new ActionCard(CardType.FREEZE));
        game.getPendingAction().setSourcePlayerId(alice.getId());

        when(gameRepository.findById(any()))
                .thenReturn(Optional.of(game));

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Game result = gameService.applyAction(UUID.randomUUID(), bob.getId());

        assertNull(result.getPendingAction());
        assertFalse(result.isInitialDealPaused());
        assertNull(result.getInitialDealCardsDealtCount());
                assertEquals(0, result.getPlayers().get(0).getHand().size());
                assertEquals(1, result.getPlayers().get(1).getHand().size());
        assertEquals(alice.getId(), result.getCurrentPlayerId());
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
        void shouldRebuildDeckWhenStartingNewRoundRunsOutOfCards() {

        Player alice = Player.builder()
                .id(UUID.randomUUID())
                .name("Alice")
                .hand(new ArrayList<>(List.of(new NumericCard(9))))
                .status(PlayerStatus.STAYED)
                .build();

        Player bob = Player.builder()
                .id(UUID.randomUUID())
                .name("Bob")
                .hand(new ArrayList<>(List.of(new NumericCard(8))))
                .status(PlayerStatus.BUSTED)
                .build();

        Game game = Game.builder()
                .currentRound(1)
                .deck(new ArrayList<>(List.of(new NumericCard(7))))
                .discardPile(new ArrayList<>(List.of(new NumericCard(6))))
                .players(new ArrayList<>(List.of(alice, bob)))
                .build();

        when(gameRepository.findById(any()))
                .thenReturn(Optional.of(game));

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(deckService.buildDeck())
                .thenReturn(new ArrayList<>(List.of(
                        new NumericCard(1),
                        new NumericCard(2),
                        new NumericCard(3)
                )));

        Game result = gameService.startRound(UUID.randomUUID());

        verify(deckService).buildDeck();
        assertEquals(2, result.getDeck().size());
        assertEquals(1, result.getPlayers().get(0).getHand().size());
        assertEquals(1, result.getPlayers().get(1).getHand().size());
        assertEquals(PlayerStatus.ACTIVE, result.getPlayers().get(0).getStatus());
        assertEquals(PlayerStatus.ACTIVE, result.getPlayers().get(1).getStatus());
    }

    @Test
    void shouldRefillDeckFromDiscardWhenDeckIsEmpty() {

        UUID playerId = UUID.randomUUID();

        Player player = Player.builder()
                .id(playerId)
                .name("Alice")
                .build();

        Game game = Game.builder()
                .currentPlayerId(playerId)
                .players(new ArrayList<>(List.of(player)))
                .deck(new ArrayList<>())
                .discardPile(new ArrayList<>(List.of(
                        new NumericCard(7),
                        new NumericCard(8)
                )))
                .build();

        when(gameRepository.findById(any()))
                .thenReturn(Optional.of(game));

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(deckService.buildDeck())
                .thenReturn(new ArrayList<>(List.of(
                        new NumericCard(1),
                        new NumericCard(2),
                        new NumericCard(3)
                )));

        when(scoreService.calculateScore(any()))
                .thenReturn(7);

        Game result = gameService.drawCard(UUID.randomUUID(), playerId);

        assertEquals(1, result.getPlayers().get(0).getHand().size());
        assertEquals(2, result.getDeck().size());
        assertEquals(0, result.getDiscardPile().size());

        verify(deckService).buildDeck();
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

    @Test
    void shouldPersistRoundHistoryWhenRoundEnds() {

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

        Game result = gameService.stay(UUID.randomUUID(), playerId);

        assertEquals(1, result.getRoundHistory().size());
        RoundResult rr = result.getRoundHistory().get(0);
        assertEquals(game.getCurrentRound(), rr.getRoundNumber());
        assertEquals(1, rr.getScores().size());
        assertEquals(25, rr.getScores().get(0).getScore());
    }

    @Test
    void shouldDeclareWinnerWhenSomePlayerReachesTwoHundredPoints() {

        UUID currentPlayerId = UUID.randomUUID();
        UUID winnerId = UUID.randomUUID();

        Player currentPlayer = Player.builder()
                .id(currentPlayerId)
                .name("Alice")
                .roundScore(15)
                .totalScore(190)
                .status(PlayerStatus.ACTIVE)
                .build();

        Player winnerPlayer = Player.builder()
                .id(winnerId)
                .name("Bob")
                .roundScore(0)
                .totalScore(210)
                .status(PlayerStatus.STAYED)
                .build();

        Game game = Game.builder()
                .currentPlayerId(currentPlayerId)
                .players(new ArrayList<>(List.of(currentPlayer, winnerPlayer)))
                .build();

        when(gameRepository.findById(any()))
                .thenReturn(Optional.of(game));

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Game result = gameService.stay(UUID.randomUUID(), currentPlayerId);

        assertEquals(GameStatus.GAME_OVER, result.getStatus());
        assertNotNull(result.getWinner());
        assertEquals(winnerId, result.getWinner().getId());
        assertEquals("Bob", result.getWinner().getName());
        assertEquals(210, result.getWinner().getTotalScore());
    }

    @Test
    void shouldReturnOnlyFinishedGamesInFinishedList() {

        Game finishedGame = Game.builder()
                .id(UUID.randomUUID())
                .status(GameStatus.GAME_OVER)
                .build();

        when(gameRepository.findAllByStatus(GameStatus.GAME_OVER))
                .thenReturn(List.of(finishedGame));

        List<Game> result = gameService.getFinishedGames();

        assertEquals(1, result.size());
        assertEquals(GameStatus.GAME_OVER, result.get(0).getStatus());
    }

    @Test
    void shouldReturnFinishedGameByIdOnlyWhenGameIsOver() {

        UUID gameId = UUID.randomUUID();

        Game finishedGame = Game.builder()
                .id(gameId)
                .status(GameStatus.GAME_OVER)
                .build();

        when(gameRepository.findById(gameId))
                .thenReturn(Optional.of(finishedGame));

        Game result = gameService.getFinishedGameById(gameId);

        assertEquals(gameId, result.getId());
        assertEquals(GameStatus.GAME_OVER, result.getStatus());
    }

    @Test
    void shouldThrowWhenRequestingFinishedDetailForActiveGame() {

        UUID gameId = UUID.randomUUID();

        Game activeGame = Game.builder()
                .id(gameId)
                .status(GameStatus.IN_ROUND)
                .build();

        when(gameRepository.findById(gameId))
                .thenReturn(Optional.of(activeGame));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> gameService.getFinishedGameById(gameId)
        );

        assertEquals("Game is not finished", exception.getMessage());
    }

    @Test
    void shouldNotAllowStartingRoundForFinishedGame() {

        UUID gameId = UUID.randomUUID();

        Game finishedGame = Game.builder()
                .id(gameId)
                .status(GameStatus.GAME_OVER)
                .players(new ArrayList<>(List.of(
                        Player.builder().id(UUID.randomUUID()).name("Alice").build(),
                        Player.builder().id(UUID.randomUUID()).name("Bob").build()
                )))
                .build();

        when(gameRepository.findById(gameId))
                .thenReturn(Optional.of(finishedGame));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> gameService.startRound(gameId)
        );

        assertEquals("Game is already finished", exception.getMessage());
    }

    @Test
    void shouldActivateSecondChanceWhenDrawingCard() {

        UUID playerId = UUID.randomUUID();

        Player player = Player.builder()
                .id(playerId)
                .name("Alice")
                .build();

        Game game = Game.builder()
                .currentPlayerId(playerId)
                .players(new ArrayList<>(List.of(player)))
                .deck(new ArrayList<>(List.of(
                        new ActionCard(CardType.SECOND_CHANCE)
                )))
                .build();

        when(gameRepository.findById(any()))
                .thenReturn(Optional.of(game));

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(scoreService.calculateScore(any()))
                .thenReturn(0);

        gameService.drawCard(
                UUID.randomUUID(),
                playerId
        );

        boolean hasSecond = player.getHand().stream().anyMatch(c -> c instanceof ActionCard ac && ac.getType() == CardType.SECOND_CHANCE);
        assertTrue(hasSecond);
    }

    @Test
    void shouldConsumeSecondChanceOnBust() {

        UUID playerId = UUID.randomUUID();

        Player player = Player.builder()
                .id(playerId)
                .name("Alice")
                .hand(new ArrayList<>(List.of(
                        new NumericCard(5),
                        new ActionCard(CardType.SECOND_CHANCE)
                )))
                .build();

        Game game = Game.builder()
                .currentPlayerId(playerId)
                .players(new ArrayList<>(List.of(player)))
                .deck(new ArrayList<>(List.of(
                        new NumericCard(5)
                )))
                .build();

        when(gameRepository.findById(any()))
                .thenReturn(Optional.of(game));

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(scoreService.calculateScore(any()))
                .thenReturn(5);

        gameService.drawCard(
                UUID.randomUUID(),
                playerId
        );

        boolean hasSecondAfter = player.getHand().stream().anyMatch(c -> c instanceof ActionCard ac && ac.getType() == CardType.SECOND_CHANCE);
        assertFalse(hasSecondAfter);
    }

    @Test
    void shouldNotBecomeBustedWhenSecondChanceExists() {

        UUID playerId = UUID.randomUUID();

        Player player = Player.builder()
                .id(playerId)
                .hand(new ArrayList<>(List.of(
                        new NumericCard(5),
                        new ActionCard(CardType.SECOND_CHANCE)
                )))
                .build();

        Game game = Game.builder()
                .currentPlayerId(playerId)
                .players(new ArrayList<>(List.of(player)))
                .deck(new ArrayList<>(List.of(
                        new NumericCard(5)
                )))
                .build();

        when(gameRepository.findById(any()))
                .thenReturn(Optional.of(game));

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(scoreService.calculateScore(any()))
                .thenReturn(5);

        gameService.drawCard(
                UUID.randomUUID(),
                playerId
        );

        assertNotEquals(
                PlayerStatus.BUSTED,
                player.getStatus()
        );
    }

    @Test
    void shouldRemoveRepeatedCardAfterUsingSecondChance() {

        UUID playerId = UUID.randomUUID();

        Player player = Player.builder()
                .id(playerId)
                .hand(new ArrayList<>(List.of(
                        new NumericCard(5),
                        new ActionCard(CardType.SECOND_CHANCE)
                )))
                .build();

        int sizeBefore = player.getHand().size();

        Game game = Game.builder()
                .currentPlayerId(playerId)
                .players(new ArrayList<>(List.of(player)))
                .deck(new ArrayList<>(List.of(
                        new NumericCard(5)
                )))
                .build();

        when(gameRepository.findById(any()))
                .thenReturn(Optional.of(game));

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(scoreService.calculateScore(any()))
                .thenReturn(5);

        gameService.drawCard(
                UUID.randomUUID(),
                playerId
        );

        assertEquals(
                sizeBefore - 1,
                player.getHand().size()
        );
    }

    @Test
    void shouldRemoveSecondChanceCardAfterUsingIt() {

        UUID playerId = UUID.randomUUID();

        Player player = Player.builder()
                .id(playerId)
                .hand(new ArrayList<>(List.of(
                        new NumericCard(5),
                        new ActionCard(CardType.SECOND_CHANCE)
                )))
                .build();

        Game game = Game.builder()
                .currentPlayerId(playerId)
                .players(new ArrayList<>(List.of(player)))
                .deck(new ArrayList<>(List.of(
                        new NumericCard(5)
                )))
                .build();

        when(gameRepository.findById(any()))
                .thenReturn(Optional.of(game));

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(scoreService.calculateScore(any()))
                .thenReturn(5);

        gameService.drawCard(
                UUID.randomUUID(),
                playerId
        );

        boolean hasSecondChance =
                player.getHand()
                        .stream()
                        .anyMatch(card ->
                                card instanceof ActionCard actionCard
                                        && actionCard.getType()
                                        == CardType.SECOND_CHANCE
                        );

        assertFalse(hasSecondChance);
    }

    @Test
    void shouldBecomeBustedWithoutSecondChance() {

        UUID playerId = UUID.randomUUID();

        Player player = Player.builder()
                .id(playerId)
                .hand(new ArrayList<>(List.of(
                        new NumericCard(5)
                )))
                .build();

        Game game = Game.builder()
                .currentPlayerId(playerId)
                .players(new ArrayList<>(List.of(player)))
                .deck(new ArrayList<>(List.of(
                        new NumericCard(5)
                )))
                .build();

        when(gameRepository.findById(any()))
                .thenReturn(Optional.of(game));

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(scoreService.calculateScore(any()))
                .thenReturn(10);

        gameService.drawCard(
                UUID.randomUUID(),
                playerId
        );

        assertEquals(
                PlayerStatus.BUSTED,
                player.getStatus()
        );
    }

    @Test
    void shouldRecalculateScoreAfterUsingSecondChance() {

        UUID playerId = UUID.randomUUID();

        Player player = Player.builder()
                .id(playerId)
                .hand(new ArrayList<>(List.of(
                        new NumericCard(5),
                        new ActionCard(CardType.SECOND_CHANCE)
                )))
                .build();

        Game game = Game.builder()
                .currentPlayerId(playerId)
                .players(new ArrayList<>(List.of(player)))
                .deck(new ArrayList<>(List.of(
                        new NumericCard(5)
                )))
                .build();

        when(gameRepository.findById(any()))
                .thenReturn(Optional.of(game));

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(scoreService.calculateScore(any()))
                .thenReturn(5);

        gameService.drawCard(
                UUID.randomUUID(),
                playerId
        );

        verify(scoreService, atLeast(2))
                .calculateScore(any());
    }

    @Test
    void shouldTransferSecondChanceToAnotherActivePlayer() {

        UUID currentPlayerId = UUID.randomUUID();
        UUID otherPlayerId = UUID.randomUUID();

        Player currentPlayer = Player.builder()
                .id(currentPlayerId)
                .name("Alice")
                .hand(new ArrayList<>(List.of(
                        new ActionCard(CardType.SECOND_CHANCE)
                )))
                .build();

        Player otherPlayer = Player.builder()
                .id(otherPlayerId)
                .name("Bob")
                .build();

        Game game = Game.builder()
                .currentPlayerId(currentPlayerId)
                .players(new ArrayList<>(List.of(currentPlayer, otherPlayer)))
                .deck(new ArrayList<>(List.of(
                        new ActionCard(CardType.SECOND_CHANCE)
                )))
                .build();

        when(gameRepository.findById(any()))
                .thenReturn(Optional.of(game));

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(scoreService.calculateScore(any()))
                .thenReturn(0);

        gameService.drawCard(
                UUID.randomUUID(),
                currentPlayerId
        );

        boolean currentHasSecondChance = currentPlayer.getHand().stream()
                .anyMatch(card -> card instanceof ActionCard actionCard
                        && actionCard.getType() == CardType.SECOND_CHANCE);

        boolean otherHasSecondChance = otherPlayer.getHand().stream()
                .anyMatch(card -> card instanceof ActionCard actionCard
                        && actionCard.getType() == CardType.SECOND_CHANCE);

        assertTrue(currentHasSecondChance);
        assertTrue(otherHasSecondChance);
    }

    @Test
    void shouldThrowWhenPlayerDrawsOutOfTurn() {

        UUID currentPlayerId = UUID.randomUUID();

        UUID otherPlayerId = UUID.randomUUID();

        Game game = Game.builder()
                .currentPlayerId(currentPlayerId)
                .players(new ArrayList<>())
                .deck(new ArrayList<>())
                .build();

        when(gameRepository.findById(any()))
                .thenReturn(Optional.of(game));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> gameService.drawCard(
                                UUID.randomUUID(),
                                otherPlayerId
                        )
                );

        assertEquals(
                "Not your turn",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowWhenPlayerStaysOutOfTurn() {

        UUID currentPlayerId = UUID.randomUUID();

        UUID otherPlayerId = UUID.randomUUID();

        Game game = Game.builder()
                .currentPlayerId(currentPlayerId)
                .players(new ArrayList<>())
                .build();

        when(gameRepository.findById(any()))
                .thenReturn(Optional.of(game));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> gameService.stay(
                                UUID.randomUUID(),
                                otherPlayerId
                        )
                );

        assertEquals(
                "Not your turn",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowWhenGameNotFound() {

        UUID gameId = UUID.randomUUID();

        when(gameRepository.findById(any()))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> gameService.stay(
                                gameId,
                                UUID.randomUUID()
                        )
                );

        assertEquals(
                "Game not found",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowExceptionWhenPlayerListIsNull() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> gameService.createGame(null)
                );

        assertEquals(
                "Need at least 2 players",
                exception.getMessage()
        );

        verify(gameRepository, never())
                .save(any());
    }

    @Test
    void shouldThrowWhenGameNotFoundOnStartRound() {

        UUID gameId = UUID.randomUUID();

        when(gameRepository.findById(any()))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> gameService.startRound(gameId)
                );

        assertEquals(
                "Game not found",
                exception.getMessage()
        );
    }

    @Test
    void shouldThrowWhenGameNotFoundOnDrawCard() {

        UUID gameId = UUID.randomUUID();

        when(gameRepository.findById(any()))
                .thenReturn(Optional.empty());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> gameService.drawCard(
                                gameId,
                                UUID.randomUUID()
                        )
                );

        assertEquals(
                "Game not found",
                exception.getMessage()
        );
    }

    @Test
    void shouldCreatePendingFreezeAction() {

        UUID playerId = UUID.randomUUID();

        Player player = Player.builder()
                .id(playerId)
                .name("Alice")
                .build();

        Game game = Game.builder()
                .currentPlayerId(playerId)
                .players(new ArrayList<>(List.of(player)))
                .deck(new ArrayList<>(List.of(
                        new ActionCard(CardType.FREEZE)
                )))
                .build();

        when(gameRepository.findById(any()))
                .thenReturn(Optional.of(game));

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Game result = gameService.drawCard(
                UUID.randomUUID(),
                playerId
        );

        assertNotNull(result.getPendingAction());

        assertEquals(
                CardType.FREEZE,
                result.getPendingAction().getType()
        );

        assertEquals(
                playerId,
                result.getPendingAction().getSourcePlayerId()
        );
    }

    @Test
    void shouldFreezeTargetPlayer() {

        UUID targetId = UUID.randomUUID();

        Player target = Player.builder()
                .id(targetId)
                .status(PlayerStatus.ACTIVE)
                .build();

        PendingAction pendingAction =
                new PendingAction();

        pendingAction.setId(null);
        pendingAction.setType(CardType.FREEZE);
        pendingAction.setCard(null);
        pendingAction.setSourcePlayerId(UUID.randomUUID());



        Game game = Game.builder()
                .players(new ArrayList<>(List.of(target)))
                .pendingAction(pendingAction)
                .build();

        when(gameRepository.findById(any()))
                .thenReturn(Optional.of(game));

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Game result = gameService.applyAction(
                UUID.randomUUID(),
                targetId
        );

        assertEquals(
                PlayerStatus.STAYED,
                result.getPlayers().get(0).getStatus()
        );
    }

    @Test
    void shouldClearPendingActionAfterApplyingFreeze() {

        UUID targetId = UUID.randomUUID();

        Player target = Player.builder()
                .id(targetId)
                .status(PlayerStatus.ACTIVE)
                .build();

        PendingAction pendingAction =
                new PendingAction();

        pendingAction.setId(null);
        pendingAction.setType(CardType.FREEZE);
        pendingAction.setCard(null);
        pendingAction.setSourcePlayerId(UUID.randomUUID());

        Game game = Game.builder()
                .players(new ArrayList<>(List.of(target)))
                .pendingAction(pendingAction)
                .build();

        when(gameRepository.findById(any()))
                .thenReturn(Optional.of(game));

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Game result = gameService.applyAction(
                UUID.randomUUID(),
                targetId
        );

        assertNull(result.getPendingAction());
    }

    @Test
    void shouldThrowWhenNoPendingActionExists() {

        Game game = Game.builder()
                .players(new ArrayList<>())
                .build();

        when(gameRepository.findById(any()))
                .thenReturn(Optional.of(game));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> gameService.applyAction(
                                UUID.randomUUID(),
                                UUID.randomUUID()
                        )
                );

        assertEquals(
                "No pending action",
                exception.getMessage()
        );
    }

    @Test
    void shouldEndRoundWhenFreezeRemovesLastActivePlayer() {

        UUID playerId = UUID.randomUUID();

        Player player = Player.builder()
                .id(playerId)
                .status(PlayerStatus.ACTIVE)
                .roundScore(20)
                .build();

        PendingAction pendingAction =
                new PendingAction();

        pendingAction.setId(null);
        pendingAction.setType(CardType.FREEZE);
        pendingAction.setCard(null);
        pendingAction.setSourcePlayerId(UUID.randomUUID());

        Game game = Game.builder()
                .players(new ArrayList<>(List.of(player)))
                .pendingAction(pendingAction)
                .status(GameStatus.IN_ROUND)
                .build();

        when(gameRepository.findById(any()))
                .thenReturn(Optional.of(game));

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Game result = gameService.applyAction(
                UUID.randomUUID(),
                playerId
        );

        assertEquals(
                GameStatus.ROUND_END,
                result.getStatus()
        );
    }

    @Test
    void shouldThrowWhenTargetPlayerIsNotActive() {

        UUID playerId = UUID.randomUUID();

        Player player = Player.builder()
                .id(playerId)
                .status(PlayerStatus.STAYED)
                .build();

        PendingAction pendingAction =
                new PendingAction();

        pendingAction.setId(null);
        pendingAction.setType(CardType.FREEZE);
        pendingAction.setCard(null);
        pendingAction.setSourcePlayerId(UUID.randomUUID());

        Game game = Game.builder()
                .players(new ArrayList<>(List.of(player)))
                .pendingAction(pendingAction)
                .build();

        when(gameRepository.findById(any()))
                .thenReturn(Optional.of(game));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> gameService.applyAction(
                                UUID.randomUUID(),
                                playerId
                        )
                );

        assertEquals(
                "Target player is not active",
                exception.getMessage()
        );
    }

    @Test
    void shouldCreatePendingFlipThreeAction() {

        UUID playerId = UUID.randomUUID();

        Player player = Player.builder()
                .id(playerId)
                .name("Alice")
                .build();

        Game game = Game.builder()
                .currentPlayerId(playerId)
                .players(new ArrayList<>(List.of(player)))
                .deck(new ArrayList<>(List.of(
                        new ActionCard(CardType.FLIP_THREE)
                )))
                .build();

        when(gameRepository.findById(any()))
                .thenReturn(Optional.of(game));

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Game result = gameService.drawCard(
                UUID.randomUUID(),
                playerId
        );

        assertNotNull(result.getPendingAction());
        assertEquals(CardType.FLIP_THREE, result.getPendingAction().getType());
        assertEquals(playerId, result.getPendingAction().getSourcePlayerId());
        assertEquals(3, result.getPendingAction().getRemainingCards());
    }

    @Test
    void shouldApplyFlipThreeAndDrawThreeCards() {

        UUID playerId = UUID.randomUUID();

        Player player = Player.builder()
                .id(playerId)
                .name("Alice")
                .status(PlayerStatus.ACTIVE)
                .build();

        PendingAction pendingAction = new PendingAction();
        pendingAction.setId(null);
        pendingAction.setType(CardType.FLIP_THREE);
        pendingAction.setCard(new ActionCard(CardType.FLIP_THREE));
        pendingAction.setSourcePlayerId(playerId);
        pendingAction.setRemainingCards(3);

        Game game = Game.builder()
                .currentPlayerId(playerId)
                .players(new ArrayList<>(List.of(player)))
                .deck(new ArrayList<>(List.of(
                        new NumericCard(1),
                        new NumericCard(2),
                        new NumericCard(3)
                )))
                .pendingAction(pendingAction)
                .build();

        when(gameRepository.findById(any()))
                .thenReturn(Optional.of(game));

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(scoreService.calculateScore(any()))
                .thenReturn(1, 3, 6);

        Game result = gameService.applyAction(
                UUID.randomUUID(),
                playerId
        );

        assertNull(result.getPendingAction());
        assertEquals(3, result.getPlayers().get(0).getHand().size());
        assertEquals(6, result.getPlayers().get(0).getRoundScore());
    }

    @Test
    void shouldBankPointsWhenFreezeIsApplied() {

        UUID sourceId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();

        Player source = Player.builder()
                .id(sourceId)
                .name("Alice")
                .status(PlayerStatus.ACTIVE)
                .build();

        Player target = Player.builder()
                .id(targetId)
                .name("Bob")
                .status(PlayerStatus.ACTIVE)
                .roundScore(20)
                .totalScore(100)
                .build();

        PendingAction pendingAction = new PendingAction();
        pendingAction.setId(null);
        pendingAction.setType(CardType.FREEZE);
        pendingAction.setCard(new ActionCard(CardType.FREEZE));
        pendingAction.setSourcePlayerId(sourceId);

        Game game = Game.builder()
                .currentPlayerId(sourceId)
                .players(new ArrayList<>(List.of(source, target)))
                .pendingAction(pendingAction)
                .build();

        when(gameRepository.findById(any()))
                .thenReturn(Optional.of(game));

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Game result = gameService.applyAction(
                UUID.randomUUID(),
                targetId
        );

        assertEquals(PlayerStatus.STAYED, result.getPlayers().get(1).getStatus());
        assertEquals(120, result.getPlayers().get(1).getTotalScore());
        assertEquals(0, result.getPlayers().get(1).getRoundScore());
    }

    @Test
    void shouldEndRoundImmediatelyWhenFlipSevenIsReached() {

        UUID playerId = UUID.randomUUID();

        Player player = Player.builder()
                .id(playerId)
                .name("Alice")
                .status(PlayerStatus.ACTIVE)
                .hand(new ArrayList<>(List.of(
                        new NumericCard(0),
                        new NumericCard(1),
                        new NumericCard(2),
                        new NumericCard(3),
                        new NumericCard(4),
                        new NumericCard(5)
                )))
                .build();

        Game game = Game.builder()
                .currentPlayerId(playerId)
                .players(new ArrayList<>(List.of(player)))
                .deck(new ArrayList<>(List.of(
                        new NumericCard(6)
                )))
                .status(GameStatus.IN_ROUND)
                .build();

        when(gameRepository.findById(any()))
                .thenReturn(Optional.of(game));

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(scoreService.calculateScore(any()))
                .thenReturn(21);

        when(scoreService.hasFlip7(any()))
                .thenReturn(true);

        Game result = gameService.drawCard(
                UUID.randomUUID(),
                playerId
        );

        assertEquals(GameStatus.ROUND_END, result.getStatus());
        assertTrue(result.getPlayers().get(0).isFlippedSeven());
    }

    @Test
    void shouldChainPendingActionWhenFlipThreeDrawsFreeze() {

        UUID playerId = UUID.randomUUID();

        Player player = Player.builder()
                .id(playerId)
                .name("Alice")
                .status(PlayerStatus.ACTIVE)
                .build();

        PendingAction pendingAction = new PendingAction();
        pendingAction.setId(null);
        pendingAction.setType(CardType.FLIP_THREE);
        pendingAction.setCard(new ActionCard(CardType.FLIP_THREE));
        pendingAction.setSourcePlayerId(playerId);
        pendingAction.setRemainingCards(3);

        Game game = Game.builder()
                .currentPlayerId(playerId)
                .players(new ArrayList<>(List.of(player)))
                .deck(new ArrayList<>(List.of(
                        new ActionCard(CardType.FREEZE),
                        new NumericCard(2),
                        new NumericCard(3)
                )))
                .pendingAction(pendingAction)
                .build();

        when(gameRepository.findById(any()))
                .thenReturn(Optional.of(game));

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Game result = gameService.applyAction(
                UUID.randomUUID(),
                playerId
        );

        assertNotNull(result.getPendingAction());
        assertEquals(CardType.FREEZE, result.getPendingAction().getType());
        assertEquals(playerId, result.getPendingAction().getSourcePlayerId());
    }

    @Test
    void shouldChainPendingActionWhenFlipThreeDrawsAnotherFlipThree() {

        UUID playerId = UUID.randomUUID();

        Player player = Player.builder()
                .id(playerId)
                .name("Alice")
                .status(PlayerStatus.ACTIVE)
                .build();

        PendingAction pendingAction = new PendingAction();
        pendingAction.setId(null);
        pendingAction.setType(CardType.FLIP_THREE);
        pendingAction.setCard(new ActionCard(CardType.FLIP_THREE));
        pendingAction.setSourcePlayerId(playerId);
        pendingAction.setRemainingCards(3);

        Game game = Game.builder()
                .currentPlayerId(playerId)
                .players(new ArrayList<>(List.of(player)))
                .deck(new ArrayList<>(List.of(
                        new ActionCard(CardType.FLIP_THREE),
                        new NumericCard(2),
                        new NumericCard(3)
                )))
                .pendingAction(pendingAction)
                .build();

        when(gameRepository.findById(any()))
                .thenReturn(Optional.of(game));

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Game result = gameService.applyAction(
                UUID.randomUUID(),
                playerId
        );

        assertNotNull(result.getPendingAction());
        assertEquals(CardType.FLIP_THREE, result.getPendingAction().getType());
        assertEquals(playerId, result.getPendingAction().getSourcePlayerId());
        assertEquals(3, result.getPendingAction().getRemainingCards());
    }

    @Test
    void shouldHandleFlipThreeWhenDeckRunsOut() {

        UUID playerId = UUID.randomUUID();

        Player player = Player.builder()
                .id(playerId)
                .name("Alice")
                .status(PlayerStatus.ACTIVE)
                .build();

        PendingAction pendingAction = new PendingAction();
        pendingAction.setId(null);
        pendingAction.setType(CardType.FLIP_THREE);
        pendingAction.setCard(new ActionCard(CardType.FLIP_THREE));
        pendingAction.setSourcePlayerId(playerId);
        pendingAction.setRemainingCards(3);

        Game game = Game.builder()
                .currentPlayerId(playerId)
                .players(new ArrayList<>(List.of(player)))
                .deck(new ArrayList<>(List.of(
                        new NumericCard(9)
                )))
                .pendingAction(pendingAction)
                .build();

        when(gameRepository.findById(any()))
                .thenReturn(Optional.of(game));

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(scoreService.calculateScore(any()))
                .thenReturn(9);

        Game result = gameService.applyAction(
                UUID.randomUUID(),
                playerId
        );

        assertNull(result.getPendingAction());
        assertEquals(1, result.getPlayers().get(0).getHand().size());
        assertEquals(9, result.getPlayers().get(0).getRoundScore());
    }

    @Test
    void shouldUseSecondChanceDuringFlipThreeSequence() {

        UUID playerId = UUID.randomUUID();

        Player player = Player.builder()
                .id(playerId)
                .name("Alice")
                .status(PlayerStatus.ACTIVE)
                .hand(new ArrayList<>(List.of(
                        new NumericCard(5)
                )))
                .build();

        PendingAction pendingAction = new PendingAction();
        pendingAction.setId(null);
        pendingAction.setType(CardType.FLIP_THREE);
        pendingAction.setCard(new ActionCard(CardType.FLIP_THREE));
        pendingAction.setSourcePlayerId(playerId);
        pendingAction.setRemainingCards(3);

        Game game = Game.builder()
                .currentPlayerId(playerId)
                .players(new ArrayList<>(List.of(player)))
                .deck(new ArrayList<>(List.of(
                        new ActionCard(CardType.SECOND_CHANCE),
                        new NumericCard(5),
                        new NumericCard(8)
                )))
                .pendingAction(pendingAction)
                .build();

        when(gameRepository.findById(any()))
                .thenReturn(Optional.of(game));

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(scoreService.calculateScore(any()))
                .thenReturn(5, 10, 5, 13);

        Game result = gameService.applyAction(
                UUID.randomUUID(),
                playerId
        );

        assertNull(result.getPendingAction());
        assertEquals(2, result.getPlayers().get(0).getHand().size());
        assertEquals(13, result.getPlayers().get(0).getRoundScore());
        boolean hasSecondAfterFlipThree = result.getPlayers().get(0).getHand().stream().anyMatch(c -> c instanceof ActionCard ac && ac.getType() == CardType.SECOND_CHANCE);
        assertFalse(hasSecondAfterFlipThree);
        assertEquals(PlayerStatus.ACTIVE, result.getPlayers().get(0).getStatus());
    }

    @Test
    void shouldDeferFlipThreeSpecialCardsUntilAllThreeCardsAreDrawn() {

        UUID sourcePlayerId = UUID.randomUUID();
        UUID targetPlayerId = UUID.randomUUID();
        UUID otherActivePlayerId = UUID.randomUUID();

        Player sourcePlayer = Player.builder()
                .id(sourcePlayerId)
                .name("Alice")
                .status(PlayerStatus.ACTIVE)
                .build();

        Player targetPlayer = Player.builder()
                .id(targetPlayerId)
                .name("Bob")
                .status(PlayerStatus.ACTIVE)
                .build();

        Player otherActivePlayer = Player.builder()
                .id(otherActivePlayerId)
                .name("Carol")
                .status(PlayerStatus.ACTIVE)
                .build();

        PendingAction pendingAction = new PendingAction();
        pendingAction.setId(null);
        pendingAction.setType(CardType.FLIP_THREE);
        pendingAction.setCard(new ActionCard(CardType.FLIP_THREE));
        pendingAction.setSourcePlayerId(sourcePlayerId);
        pendingAction.setRemainingCards(3);

        Game game = Game.builder()
                .currentPlayerId(sourcePlayerId)
                .players(new ArrayList<>(List.of(sourcePlayer, targetPlayer, otherActivePlayer)))
                .deck(new ArrayList<>(List.of(
                        new ActionCard(CardType.FREEZE),
                        new ActionCard(CardType.FLIP_THREE),
                        new NumericCard(8)
                )))
                .pendingAction(pendingAction)
                .build();

        when(gameRepository.findById(any()))
                .thenReturn(Optional.of(game));

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(scoreService.calculateScore(any()))
                .thenReturn(8);

        Game result = gameService.applyAction(
                UUID.randomUUID(),
                targetPlayerId
        );

        assertNotNull(result.getPendingAction());
        assertEquals(CardType.FREEZE, result.getPendingAction().getType());
        assertEquals(1, result.getPlayers().get(1).getHand().size());
        assertEquals(8, result.getPlayers().get(1).getHand().get(0).getType() == CardType.NUMBER ? ((NumericCard) result.getPlayers().get(1).getHand().get(0)).getValue() : -1);

        Game afterFreeze = gameService.applyAction(
                UUID.randomUUID(),
                sourcePlayerId
        );

        assertNotNull(afterFreeze.getPendingAction());
        assertEquals(CardType.FLIP_THREE, afterFreeze.getPendingAction().getType());
        assertEquals(PlayerStatus.STAYED, afterFreeze.getPlayers().get(0).getStatus());
    }

}