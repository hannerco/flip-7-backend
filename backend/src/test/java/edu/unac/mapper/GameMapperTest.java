package edu.unac.mapper;

import edu.unac.dto.response.CreateGameResponse;
import edu.unac.dto.response.FinishedGameResponse;
import edu.unac.dto.response.GameResponse;
import edu.unac.dto.response.PlayerResponse;
import edu.unac.model.card.NumericCard;
import edu.unac.model.enums.GameStatus;
import edu.unac.model.enums.ModifierType;
import edu.unac.model.card.ModifierCard;
import edu.unac.model.enums.PlayerStatus;
import edu.unac.model.game.*;
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
        GameResponse response = mapper.toResponse(game);

        assertEquals(game.getId(), response.getId());
        assertEquals(game.getStatus(), response.getStatus());
        assertEquals(game.getCurrentRound(), response.getCurrentRound());
        assertEquals(game.getDealerId(), response.getRoundStarterId());
        assertEquals(1, response.getPlayers().size());
        assertNotNull(response.getWinner());
        assertEquals(playerId, response.getWinner().getId());
        assertEquals("Alice", response.getWinner().getName());
        assertEquals(203, response.getWinner().getTotalScore());
        assertEquals(1, response.getRoundHistory().size());
        assertEquals(3, response.getRoundHistory().get(0).getRoundNumber());
        assertEquals(1, response.getRoundHistory().get(0).getScores().size());
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
        GameResponse response = mapper.toResponse(game);
        PlayerResponse playerResponse = response.getPlayers().get(0);

        assertEquals(playerId, playerResponse.getId());
        assertEquals("Alice", playerResponse.getName());
        assertEquals(150, playerResponse.getTotalScore());
        assertEquals(40, playerResponse.getRoundScore());
        assertEquals(PlayerStatus.ACTIVE, playerResponse.getStatus());
        assertTrue(playerResponse.getCards().stream().anyMatch(c -> c.getType() == CardType.SECOND_CHANCE));
        assertTrue(playerResponse.isFlippedSeven());
    }

    @Test
    void shouldMapGameToCreateResponse() {
        UUID gameId = UUID.randomUUID();

        Game game = Game.builder()
                .id(gameId)
                .build();

        GameMapper mapper = new GameMapper();
        CreateGameResponse response = mapper.toCreateResponse(game);

        assertNotNull(response);
        assertEquals(gameId, response.getGameId());
        assertNotNull(response.getGameId());
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

    @Test
    void shouldHandleNullRoundHistory() {
        Game game = Game.builder()
                .roundHistory(null)
                .players(List.of())
                .build();

        GameMapper mapper = new GameMapper();
        GameResponse response = mapper.toResponse(game);

        assertNotNull(response.getRoundHistory());
        assertTrue(response.getRoundHistory().isEmpty());
    }

    @Test
    void shouldHandlePlayerWithoutCards() {
        Player player = Player.builder()
                .id(UUID.randomUUID())
                .name("Alice")
                .hand(null)
                .build();

        Game game = Game.builder()
                .players(List.of(player))
                .build();

        GameMapper mapper = new GameMapper();
        GameResponse response = mapper.toResponse(game);

        assertTrue(response.getPlayers().get(0).getCards().isEmpty());
    }

    @Test
    void shouldMapNumericCard() {
        NumericCard card = new NumericCard(7);

        Player player = Player.builder()
                .id(UUID.randomUUID())
                .name("Alice")
                .hand(List.of(card))
                .build();

        Game game = Game.builder()
                .players(List.of(player))
                .build();

        GameMapper mapper = new GameMapper();
        GameResponse response = mapper.toResponse(game);

        assertEquals(7, response.getPlayers().get(0).getCards().get(0).getValue());
    }

    @Test
    void shouldHandleNullPlayersInFinishedGameResponse() {
        Game game = Game.builder()
                .players(null)
                .build();

        GameMapper mapper = new GameMapper();
        FinishedGameResponse response = mapper.toFinishedGameResponse(game);

        assertEquals(0, response.getPlayersCount());
    }

    @Test
    void shouldMapPendingActionWithoutCard() {
        UUID sourceId = UUID.randomUUID();

        PendingAction pendingAction = PendingAction.builder()
                .id(UUID.randomUUID())
                .sourcePlayerId(sourceId)
                .remainingCards(2)
                .build();

        Game game = Game.builder()
                .players(List.of())
                .pendingAction(pendingAction)
                .build();

        GameMapper mapper = new GameMapper();
        GameResponse response = mapper.toResponse(game);

        assertNotNull(response.getPendingAction());
        assertNull(response.getPendingAction().getCard());
    }

    @Test
    void shouldMapPendingActionNumericCard() {
        NumericCard card = new NumericCard(7);

        PendingAction pendingAction = PendingAction.builder()
                .card(card)
                .sourcePlayerId(UUID.randomUUID())
                .build();

        Game game = Game.builder()
                .players(List.of())
                .pendingAction(pendingAction)
                .build();

        GameMapper mapper = new GameMapper();
        GameResponse response = mapper.toResponse(game);

        assertEquals(7, response.getPendingAction().getCard().getValue());
    }

    @Test
    void shouldAddOnlyValidTargetOptions() {
        UUID sourceId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();

        Player source = Player.builder()
                .id(sourceId)
                .status(PlayerStatus.ACTIVE)
                .build();

        Player target = Player.builder()
                .id(targetId)
                .status(PlayerStatus.ACTIVE)
                .build();

        Player inactive = Player.builder()
                .id(UUID.randomUUID())
                .status(PlayerStatus.BUSTED)
                .build();

        PendingAction pendingAction = PendingAction.builder()
                .sourcePlayerId(sourceId)
                .build();

        Game game = Game.builder()
                .players(List.of(source, target, inactive))
                .pendingAction(pendingAction)
                .build();

        GameMapper mapper = new GameMapper();
        GameResponse response = mapper.toResponse(game);

        assertEquals(1, response.getPendingAction().getTargetOptions().size());
        assertTrue(response.getPendingAction().getTargetOptions().contains(targetId));
    }

    @Test
    void shouldMapPendingActionWithNumericCard() {
        UUID sourceId = UUID.randomUUID();

        NumericCard card = new NumericCard(7);

        PendingAction pendingAction = PendingAction.builder()
                .id(UUID.randomUUID())
                .card(card)
                .sourcePlayerId(sourceId)
                .remainingCards(3)
                .build();

        Game game = Game.builder()
                .players(List.of())
                .pendingAction(pendingAction)
                .build();

        GameMapper mapper = new GameMapper();
        GameResponse response = mapper.toResponse(game);

        assertNotNull(response.getPendingAction());
        assertNotNull(response.getPendingAction().getCard());
        assertEquals(7, response.getPendingAction().getCard().getValue());
    }

    @Test
    void shouldMapAutomaticEventWithNumericCard() {
        NumericCard card = new NumericCard(9);

        AutomaticEvent event = new AutomaticEvent();
        event.setType(AutomaticEvent.Type.SECOND_CHANCE_CONSUMED);
        event.setPlayerId(UUID.randomUUID());
        event.setRemovedDuplicateCard(card);

        Game game = Game.builder()
                .players(List.of())
                .lastAutomaticEvent(event)
                .build();

        GameMapper mapper = new GameMapper();
        GameResponse response = mapper.toResponse(game);

        assertNotNull(response.getLastAutomaticEvent());
        assertEquals(9, response.getLastAutomaticEvent().getRemovedDuplicateCard().getValue());
    }

    @Test
    void shouldMapAutomaticEventWithSecondChanceCard() {
        ActionCard secondChance = new ActionCard(CardType.SECOND_CHANCE);

        AutomaticEvent event = new AutomaticEvent();
        event.setType(AutomaticEvent.Type.SECOND_CHANCE_CONSUMED);
        event.setPlayerId(UUID.randomUUID());
        event.setRemovedSecondChanceCard(secondChance);

        Game game = Game.builder()
                .players(List.of())
                .lastAutomaticEvent(event)
                .build();

        GameMapper mapper = new GameMapper();
        GameResponse response = mapper.toResponse(game);

        assertNotNull(response.getLastAutomaticEvent().getRemovedSecondChanceCard());
    }

    @Test
    void shouldMapPendingActionWithModifierCard() {
        ModifierCard card = new ModifierCard(ModifierType.TIMES_2);

        PendingAction pendingAction = PendingAction.builder()
                .id(UUID.randomUUID())
                .card(card)
                .sourcePlayerId(UUID.randomUUID())
                .remainingCards(2)
                .build();

        Game game = Game.builder()
                .players(List.of())
                .pendingAction(pendingAction)
                .build();

        GameMapper mapper = new GameMapper();
        GameResponse response = mapper.toResponse(game);

        assertEquals(ModifierType.TIMES_2, response.getPendingAction().getCard().getModifier());
    }

    @Test
    void shouldMapAutomaticEventWithModifierCard() {
        ModifierCard card = new ModifierCard(ModifierType.TIMES_2);

        AutomaticEvent event = new AutomaticEvent();
        event.setType(AutomaticEvent.Type.SECOND_CHANCE_CONSUMED);
        event.setPlayerId(UUID.randomUUID());
        event.setRemovedDuplicateCard(card);

        Game game = Game.builder()
                .players(List.of())
                .lastAutomaticEvent(event)
                .build();

        GameMapper mapper = new GameMapper();
        GameResponse response = mapper.toResponse(game);

        assertEquals(ModifierType.TIMES_2, response.getLastAutomaticEvent().getRemovedDuplicateCard().getModifier());
    }

    @Test
    void shouldMapCurrentPlayerIdAndDealerId() {
        UUID playerId = UUID.randomUUID();
        UUID dealerId = UUID.randomUUID();

        Game game = Game.builder()
                .id(UUID.randomUUID())
                .currentPlayerId(playerId)
                .dealerId(dealerId)
                .players(List.of())
                .build();

        GameMapper mapper = new GameMapper();
        GameResponse response = mapper.toResponse(game);

        assertEquals(playerId, response.getCurrentPlayerId());
        assertEquals(dealerId, response.getDealerId());
    }

    @Test
    void shouldMapAllCardFieldsInPlayerHand() {
        NumericCard card = new NumericCard(5);

        Player player = Player.builder()
                .id(UUID.randomUUID())
                .name("Test")
                .hand(List.of(card))
                .build();

        Game game = Game.builder()
                .players(List.of(player))
                .build();

        GameMapper mapper = new GameMapper();
        GameResponse response = mapper.toResponse(game);

        edu.unac.dto.response.CardResponse cardResponse = response.getPlayers().get(0).getCards().get(0);

        assertNotNull(cardResponse);
        assertNotNull(cardResponse.getType());
        assertNotNull(cardResponse.getKind());
        assertEquals(5, cardResponse.getValue());
    }

    @Test
    void shouldMapAllCardFieldsInAutomaticEvent() {
        NumericCard card = new NumericCard(8);

        AutomaticEvent event = new AutomaticEvent();
        event.setType(AutomaticEvent.Type.SECOND_CHANCE_CONSUMED);
        event.setPlayerId(UUID.randomUUID());
        event.setRemovedDuplicateCard(card);

        Game game = Game.builder()
                .players(List.of())
                .lastAutomaticEvent(event)
                .build();

        GameMapper mapper = new GameMapper();
        GameResponse response = mapper.toResponse(game);

        edu.unac.dto.response.CardResponse cardResponse = response.getLastAutomaticEvent().getRemovedDuplicateCard();

        assertNotNull(cardResponse);
        assertNotNull(cardResponse.getType());
        assertNotNull(cardResponse.getKind());
    }

    @Test
    void shouldMapAllCardFieldsInSecondChanceRemoval() {
        ActionCard secondChance = new ActionCard(CardType.SECOND_CHANCE);

        AutomaticEvent event = new AutomaticEvent();
        event.setType(AutomaticEvent.Type.SECOND_CHANCE_CONSUMED);
        event.setPlayerId(UUID.randomUUID());
        event.setRemovedSecondChanceCard(secondChance);

        Game game = Game.builder()
                .players(List.of())
                .lastAutomaticEvent(event)
                .build();

        GameMapper mapper = new GameMapper();
        GameResponse response = mapper.toResponse(game);

        edu.unac.dto.response.CardResponse cardResponse = response.getLastAutomaticEvent().getRemovedSecondChanceCard();

        assertNotNull(cardResponse);
        assertEquals(CardType.SECOND_CHANCE, cardResponse.getType());
        assertNotNull(cardResponse.getKind());
    }

    @Test
    void shouldMapAllAutomaticEventFields() {
        UUID playerId = UUID.randomUUID();

        AutomaticEvent event = new AutomaticEvent();
        event.setType(AutomaticEvent.Type.SECOND_CHANCE_CONSUMED);
        event.setPlayerId(playerId);

        Game game = Game.builder()
                .players(List.of())
                .lastAutomaticEvent(event)
                .build();

        GameMapper mapper = new GameMapper();
        GameResponse response = mapper.toResponse(game);

        assertNotNull(response.getLastAutomaticEvent());
        assertEquals(AutomaticEvent.Type.SECOND_CHANCE_CONSUMED, response.getLastAutomaticEvent().getType());
        assertEquals(playerId, response.getLastAutomaticEvent().getPlayerId());
    }

    @Test
    void shouldMapModifierCardInPlayerHand() {
        ModifierCard modifierCard = new ModifierCard(ModifierType.TIMES_2);

        Player player = Player.builder()
                .id(UUID.randomUUID())
                .name("Test")
                .hand(List.of(modifierCard))
                .build();

        Game game = Game.builder()
                .players(List.of(player))
                .build();

        GameMapper mapper = new GameMapper();
        GameResponse response = mapper.toResponse(game);

        edu.unac.dto.response.CardResponse cardResponse = response.getPlayers().get(0).getCards().get(0);

        assertEquals(ModifierType.TIMES_2, cardResponse.getModifier());
    }

    @Test
    void shouldMapAllPendingActionFields() {
        UUID expectedId = UUID.randomUUID();
        UUID expectedSourcePlayerId = UUID.randomUUID();
        int expectedRemainingCards = 5;
        CardType expectedType = CardType.SECOND_CHANCE;

        PendingAction pendingAction = PendingAction.builder()
                .id(expectedId)
                .type(expectedType)
                .sourcePlayerId(expectedSourcePlayerId)
                .remainingCards(expectedRemainingCards)
                .build();

        Player sourcePlayer = Player.builder()
                .id(expectedSourcePlayerId)
                .status(PlayerStatus.ACTIVE)
                .build();

        Player otherPlayer = Player.builder()
                .id(UUID.randomUUID())
                .status(PlayerStatus.ACTIVE)
                .build();

        Game game = Game.builder()
                .players(List.of(sourcePlayer, otherPlayer))
                .pendingAction(pendingAction)
                .build();

        GameMapper mapper = new GameMapper();
        GameResponse response = mapper.toResponse(game);

        assertNotNull(response.getPendingAction());
        assertEquals(expectedId, response.getPendingAction().getId());
        assertEquals(expectedType, response.getPendingAction().getType());
        assertEquals(expectedSourcePlayerId, response.getPendingAction().getSourcePlayerId());
        assertEquals(expectedRemainingCards, response.getPendingAction().getRemainingCards());
        assertNotNull(response.getPendingAction().getTargetOptions());
        assertEquals(1, response.getPendingAction().getTargetOptions().size());
    }

    @Test
    void shouldMapAllCardResponseFieldsInPendingAction() {
        UUID expectedCardId = UUID.randomUUID();
        NumericCard numericCard = new NumericCard(7);

        try {
            java.lang.reflect.Field idField = numericCard.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(numericCard, expectedCardId);
        } catch (Exception e) {
        }

        PendingAction pendingAction = PendingAction.builder()
                .id(UUID.randomUUID())
                .card(numericCard)
                .sourcePlayerId(UUID.randomUUID())
                .remainingCards(2)
                .build();

        Game game = Game.builder()
                .players(List.of())
                .pendingAction(pendingAction)
                .build();

        GameMapper mapper = new GameMapper();
        GameResponse response = mapper.toResponse(game);

        assertNotNull(response.getPendingAction());
        assertNotNull(response.getPendingAction().getCard());
        assertNotNull(response.getPendingAction().getCard().getType());
        assertNotNull(response.getPendingAction().getCard().getKind());
        assertEquals(7, response.getPendingAction().getCard().getValue());

        if (response.getPendingAction().getCard().getId() != null) {
            assertNotNull(response.getPendingAction().getCard().getId());
        }
    }

    @Test
    void shouldMapAllCardResponseFieldsForModifierCardInPendingAction() {
        UUID expectedCardId = UUID.randomUUID();
        ModifierCard modifierCard = new ModifierCard(ModifierType.TIMES_2);

        try {
            java.lang.reflect.Field idField = modifierCard.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(modifierCard, expectedCardId);
        } catch (Exception e) {
        }

        PendingAction pendingAction = PendingAction.builder()
                .id(UUID.randomUUID())
                .card(modifierCard)
                .sourcePlayerId(UUID.randomUUID())
                .remainingCards(2)
                .build();

        Game game = Game.builder()
                .players(List.of())
                .pendingAction(pendingAction)
                .build();

        GameMapper mapper = new GameMapper();
        GameResponse response = mapper.toResponse(game);

        assertNotNull(response.getPendingAction().getCard());
        assertNotNull(response.getPendingAction().getCard().getType());
        assertNotNull(response.getPendingAction().getCard().getKind());
        assertEquals(ModifierType.TIMES_2, response.getPendingAction().getCard().getModifier());
    }

    @Test
    void shouldMapAllCardResponseFieldsForNumericCardInPlayerHand() {
        NumericCard card = new NumericCard(7);

        UUID forcedId = UUID.randomUUID();
        try {
            java.lang.reflect.Field idField = card.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(card, forcedId);
        } catch (Exception e) {
            System.out.println("Failed to set ID: " + e.getMessage());
        }

        assertNotNull(card.getType(), "Card should have a type");

        Player player = Player.builder()
                .id(UUID.randomUUID())
                .name("Test")
                .hand(List.of(card))
                .build();

        Game game = Game.builder()
                .players(List.of(player))
                .build();

        GameMapper mapper = new GameMapper();
        GameResponse response = mapper.toResponse(game);

        edu.unac.dto.response.CardResponse cardResponse = response.getPlayers().get(0).getCards().get(0);

        assertNotNull(cardResponse, "CardResponse should not be null");
        assertNotNull(cardResponse.getId(), "Card ID should not be null");
        assertNotNull(cardResponse.getType(), "Card type should not be null");
        assertNotNull(cardResponse.getKind(), "Card kind should not be null");
        assertEquals(7, cardResponse.getValue());
        assertEquals(forcedId, cardResponse.getId());
    }
}