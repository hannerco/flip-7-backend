package edu.unac.service.impl;

import edu.unac.model.card.ActionCard;
import edu.unac.model.card.Card;
import edu.unac.model.card.NumericCard;
import edu.unac.model.enums.CardType;
import edu.unac.model.enums.GameStatus;
import edu.unac.model.enums.PlayerStatus;
import edu.unac.model.game.Game;
import edu.unac.model.game.PendingAction;
import edu.unac.model.game.Player;
import edu.unac.repository.GameRepository;
import edu.unac.service.DeckService;
import edu.unac.service.GameService;
import edu.unac.service.ScoreService;
import org.springframework.stereotype.Service;
import java.util.UUID;

import java.util.ArrayList;
import java.util.List;
import edu.unac.model.game.RoundResult;
import edu.unac.model.game.RoundPlayerScore;
import edu.unac.model.game.Winner;
import edu.unac.model.game.AutomaticEvent;

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

        List<Player> players = new ArrayList<>(playerNames.stream()
                .map(name -> Player.builder()
                        .name(name)
                        .build())
                .toList());

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
        public List<Game> getFinishedGames() {

                return gameRepository.findAllByStatus(GameStatus.GAME_OVER);
        }

        @Override
        public Game getFinishedGameById(UUID gameId) {

                Game game = getGameById(gameId);

                if (game.getStatus() != GameStatus.GAME_OVER) {
                        throw new IllegalArgumentException("Game is not finished");
                }

                return game;
        }

    @Override
    public Game startRound(UUID gameId) {

        Game game = gameRepository.findById(gameId)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Game not found"
                        )
                );

        ensureGameIsNotFinished(game);

        if (game.getStatus() == GameStatus.IN_ROUND) {
            throw new IllegalStateException("Round already in progress");
        }

        game.setCurrentRound(game.getCurrentRound() + 1);

        resetRoundState(game);

        int startingPlayerIndex = getStartingPlayerIndex(game);

        game.setCurrentPlayerId(
                game.getPlayers().get(startingPlayerIndex).getId()
        );

        game.setDealerId(
                game.getPlayers().get(startingPlayerIndex).getId()
        );

        game.setStatus(GameStatus.IN_ROUND);
        game.setInitialDealCardsDealtCount(0);
        game.setInitialDealPaused(false);

                if (game.getDeck() == null) {
                        game.setDeck(new ArrayList<>());
                }

                if (game.getDeck().isEmpty()) {
                        replenishDeckIfNeeded(game);

                        if (game.getDeck().isEmpty()) {
                                // addAll into the managed collection instead of replacing the reference
                                game.getDeck().addAll(deckService.buildDeck());
                        }
                }

        dealInitialCards(game);

        return gameRepository.save(game);
    }

    private void dealInitialCards(Game game) {

        int totalPlayers = game.getPlayers().size();

        int dealtCount = game.getInitialDealCardsDealtCount() == null
                ? 0
                : game.getInitialDealCardsDealtCount();

        if (dealtCount >= totalPlayers) {
            game.setInitialDealCardsDealtCount(null);
            game.setInitialDealPaused(false);
            return;
        }

        while (dealtCount < totalPlayers) {

            int playerIndex =
                    (getStartingPlayerIndex(game) + dealtCount) % totalPlayers;

            Player player =
                    game.getPlayers().get(playerIndex);

            Card card = drawTopCard(game);

            player.getHand().add(card);

            if (card instanceof ActionCard actionCard
                    && (actionCard.getType() == CardType.FREEZE
                    || actionCard.getType() == CardType.FLIP_THREE)) {

                player.getHand().remove(player.getHand().size() - 1);

                createPendingAction(
                        game,
                        card,
                        player.getId(),
                        actionCard.getType() == CardType.FLIP_THREE ? 3 : 0
                );

                game.setInitialDealCardsDealtCount(dealtCount + 1);
                game.setInitialDealPaused(true);

                return;
            }

            player.setRoundScore(
                    scoreService.calculateScore(player)
            );

            dealtCount++;

            game.setInitialDealCardsDealtCount(dealtCount);
        }

        game.setInitialDealCardsDealtCount(null);
                game.setInitialDealPaused(false);
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

        ensureGameIsNotFinished(game);

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

        Card card = drawTopCard(game);

        if (card instanceof ActionCard actionCard
                && (actionCard.getType() == CardType.FREEZE
                || actionCard.getType() == CardType.FLIP_THREE)) {

            createPendingAction(
                    game,
                    card,
                    playerId,
                    actionCard.getType() == CardType.FLIP_THREE ? 3 : 0
            );

            return gameRepository.save(game);
        }

        applyDrawnCard(game, player, card);

        advanceTurn(game);

        checkRoundEnd(game);

        return gameRepository.save(game);
    }

    @Override
    public Game applyAction(
            UUID gameId,
            UUID targetPlayerId
    ) {

        Game game = getGameById(gameId);

        ensureGameIsNotFinished(game);

        PendingAction pendingAction =
                game.getPendingAction();

        if (pendingAction == null) {

            throw new IllegalArgumentException(
                    "No pending action"
            );
        }

        PendingAction resolvedPendingAction = pendingAction;

        switch (pendingAction.getType()) {

            case FREEZE -> applyFreeze(
                    game,
                    targetPlayerId
            );

            case FLIP_THREE -> applyFlipThree(game, targetPlayerId);

            default -> throw new IllegalArgumentException(
                    "Unsupported action"
            );
        }

        if (resolvedPendingAction.getCard() != null) {
                discardCard(game, resolvedPendingAction.getCard());
        }

        if (game.getPendingAction() != resolvedPendingAction) {

                if (game.getPendingAction() != null) {
                        return gameRepository.save(game);
                }

                if (game.getDeferredPendingActions() != null
                                && !game.getDeferredPendingActions().isEmpty()) {
                        game.setPendingAction(
                                        game.getDeferredPendingActions().remove(0)
                        );

                        return gameRepository.save(game);
                }
        }

        if (game.getPendingAction() == resolvedPendingAction) {
            game.setPendingAction(null);

                        if (game.getDeferredPendingActions() != null
                                        && !game.getDeferredPendingActions().isEmpty()) {
                                game.setPendingAction(
                                                game.getDeferredPendingActions().remove(0)
                                );

                                return gameRepository.save(game);
                        }

                        if (game.isInitialDealPaused()) {
                                dealInitialCards(game);

                                if (game.getPendingAction() != null) {
                                        return gameRepository.save(game);
                                }

                                checkRoundEnd(game);

                                return gameRepository.save(game);
                        }

                        advanceTurn(game);
        }

        checkRoundEnd(game);

        return gameRepository.save(game);
    }

    private void applyFreeze(
            Game game,
            UUID targetPlayerId
    ) {

        Player target =
                findPlayer(game, targetPlayerId);

        if (target.getStatus() != PlayerStatus.ACTIVE) {

            throw new IllegalArgumentException(
                    "Target player is not active"
            );
        }

        target.setStatus(
                PlayerStatus.STAYED
        );

        target.setTotalScore(
                target.getTotalScore() + target.getRoundScore()
        );

        target.setRoundScore(0);
    }

    private void applyFlipThree(
            Game game,
            UUID targetPlayerId
    ) {

        Player target =
                findPlayer(game, targetPlayerId);

        if (target.getStatus() != PlayerStatus.ACTIVE) {

            throw new IllegalArgumentException(
                    "Target player is not active"
            );
        }

        PendingAction currentPendingAction = game.getPendingAction();

        int remainingCards = currentPendingAction.getRemainingCards() == null
                ? 3
                : currentPendingAction.getRemainingCards();

        if (game.getDeferredPendingActions() == null) {
                game.setDeferredPendingActions(new ArrayList<>());
        }

        PendingAction firstDeferredPendingAction = null;

        while (remainingCards > 0
                && target.getStatus() == PlayerStatus.ACTIVE
                && game.getStatus() != GameStatus.ROUND_END
                && !game.getDeck().isEmpty()) {

                        Card card = drawTopCard(game);

            if (card instanceof ActionCard actionCard
                    && (actionCard.getType() == CardType.FREEZE
                    || actionCard.getType() == CardType.FLIP_THREE)) {

                                queueDeferredPendingAction(
                                                game,
                                                card,
                                                target.getId(),
                                                actionCard.getType() == CardType.FLIP_THREE ? 3 : 0
                                );

                                if (firstDeferredPendingAction == null) {
                                        firstDeferredPendingAction = game.getDeferredPendingActions().remove(0);
                                }

                                remainingCards--;

                                if (game.getStatus() == GameStatus.ROUND_END
                                                || target.getStatus() != PlayerStatus.ACTIVE) {
                                        break;
                                }

                                continue;
            }

            applyDrawnCard(game, target, card);

            remainingCards--;

            if (game.getStatus() == GameStatus.ROUND_END
                    || target.getStatus() != PlayerStatus.ACTIVE) {
                break;
            }

            checkRoundEnd(game);

            if (game.getStatus() == GameStatus.ROUND_END
                    || target.getStatus() != PlayerStatus.ACTIVE) {
                break;
            }
        }

        if (target.getStatus() != PlayerStatus.ACTIVE
                || game.getStatus() == GameStatus.ROUND_END) {
                if (game.getDeferredPendingActions() != null) {
                        game.getDeferredPendingActions().clear();
                }
                return;
        }

        if (game.getPendingAction() == currentPendingAction) {
                currentPendingAction.setRemainingCards(remainingCards);

                if (firstDeferredPendingAction != null) {
                        game.setPendingAction(firstDeferredPendingAction);
                }
        }
    }

        private void queueDeferredPendingAction(
                        Game game,
                        Card card,
                        UUID sourcePlayerId,
                        int remainingCards
        ) {

                PendingAction deferredPendingAction = new PendingAction();
                deferredPendingAction.setId(null);
                deferredPendingAction.setType(card.getType());
                deferredPendingAction.setCard(card);
                deferredPendingAction.setSourcePlayerId(sourcePlayerId);
                deferredPendingAction.setRemainingCards(remainingCards);

                game.getDeferredPendingActions().add(deferredPendingAction);
        }

        private void createPendingAction(
                        Game game,
                        Card card,
                        UUID sourcePlayerId,
                        int remainingCards
        ) {

                PendingAction pendingAction = new PendingAction();
                pendingAction.setId(null);
                pendingAction.setType(card.getType());
                pendingAction.setCard(card);
                pendingAction.setSourcePlayerId(sourcePlayerId);
                pendingAction.setRemainingCards(remainingCards);

                game.setPendingAction(pendingAction);
        }

        private void resetRoundState(Game game) {

                discardHands(game);

                game.setPendingAction(null);

                for (Player player : game.getPlayers()) {

                        player.setRoundScore(0);
                        player.setFlippedSeven(false);
                        player.setStatus(PlayerStatus.ACTIVE);
                }
        }

        private void discardHands(Game game) {

                ensureDiscardPile(game);

                for (Player player : game.getPlayers()) {

                        if (!player.getHand().isEmpty()) {
                                game.getDiscardPile().addAll(player.getHand());
                                player.getHand().clear();
                        }
                }

                if (game.getPendingAction() != null && game.getPendingAction().getCard() != null) {
                        discardCard(game, game.getPendingAction().getCard());
                }
        }

        private void replenishDeckIfNeeded(Game game) {

                List<Card> rebuiltDeck = new ArrayList<>(deckService.buildDeck());

                if (game.getDeck() == null) {
                        game.setDeck(new ArrayList<>());
                }

                game.getDeck().clear();
                game.getDeck().addAll(rebuiltDeck);

                if (game.getDiscardPile() != null) {
                        game.getDiscardPile().clear();
                }
        }

        private Card drawTopCard(Game game) {

                if (game.getDeck() == null || game.getDeck().isEmpty()) {
                        replenishDeckIfNeeded(game);
                }

                if (game.getDeck() == null || game.getDeck().isEmpty()) {
                        throw new IllegalStateException("No cards available");
                }

                return game.getDeck().remove(0);
        }

        private void discardCard(Game game, Card card) {

                if (card == null) {
                        return;
                }

                ensureDiscardPile(game);
                game.getDiscardPile().add(card);
        }

        private void ensureDiscardPile(Game game) {

                if (game.getDiscardPile() == null) {
                        game.setDiscardPile(new ArrayList<>());
                }
        }

    private void applyDrawnCard(
            Game game,
            Player player,
            Card card
    ) {

                if (card instanceof ActionCard actionCard
                                && actionCard.getType() == CardType.SECOND_CHANCE) {

                        boolean hasSecondChance = player.getHand().stream().anyMatch(c ->
                                        c instanceof ActionCard ac && ac.getType() == CardType.SECOND_CHANCE
                        );

                        if (!hasSecondChance) {
                                // player receives their single allowed second chance
                                player.getHand().add(card);
                        } else {
                                // player already has one: transfer to next eligible player
                                Player recipient = findRecipientForSecondChance(game, player);

                                if (recipient != null) {
                                        recipient.getHand().add(card);
                                } else {
                                        // no eligible recipient found, discard the card
                                        discardCard(game, card);
                                }
                        }

                } else {
                        player.getHand().add(card);
                }

        player.setRoundScore(
                scoreService.calculateScore(player)
        );

        if (isBusted(player)) {

                        if (consumeSecondChanceIfNeeded(player)) {

                                Card removedCard = removeLastCard(player);
                                Card removedSecondChanceCard = removeSecondChanceCard(player);

                                // record automatic event for API so frontend can show what happened
                                game.setLastAutomaticEvent(new AutomaticEvent(
                                        AutomaticEvent.Type.SECOND_CHANCE_CONSUMED,
                                        player.getId(),
                                        removedCard,
                                        removedSecondChanceCard
                                ));

                                discardCard(game, removedCard);
                                discardCard(game, removedSecondChanceCard);

                                player.setRoundScore(
                                        scoreService.calculateScore(player)
                                );

                        } else {

                                player.setStatus(
                                                PlayerStatus.BUSTED
                                );
                        }
        }
    }

        private Card removeSecondChanceCard(Player player) {

        for (int i = 0; i < player.getHand().size(); i++) {

            Card card = player.getHand().get(i);

            if (card instanceof ActionCard actionCard
                    && actionCard.getType() == CardType.SECOND_CHANCE) {

                                return player.getHand().remove(i);
            }
        }

                return null;
    }

    private boolean consumeSecondChanceIfNeeded(Player player) {
                // Consume second chance based solely on the presence of the SECOND_CHANCE card
                boolean hasCard = player.getHand().stream().anyMatch(c ->
                                c instanceof ActionCard ac && ac.getType() == CardType.SECOND_CHANCE
                );

                return hasCard;
    }

        private Player findRecipientForSecondChance(Game game, Player from) {

                int idx = game.getPlayers().indexOf(from);
                if (idx == -1) {
                        return null;
                }

                int total = game.getPlayers().size();

                for (int i = 1; i < total; i++) {
                        Player candidate = game.getPlayers().get((idx + i) % total);

                        // only give to ACTIVE players who don't already have a second chance card
                        boolean hasSecond = candidate.getHand().stream().anyMatch(c ->
                                        c instanceof ActionCard ac && ac.getType() == CardType.SECOND_CHANCE
                        );

                        if (candidate.getStatus() == PlayerStatus.ACTIVE && !hasSecond) {
                                return candidate;
                        }
                }

                return null;
        }

        private Card removeLastCard(Player player) {

        if (!player.getHand().isEmpty()) {
                        return player.getHand().remove(player.getHand().size() - 1);
        }

                return null;
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

                ensureGameIsNotFinished(game);

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

        boolean flipSevenReached =
                game.getPlayers()
                        .stream()
                        .anyMatch(scoreService::hasFlip7);

        if (flipSevenReached) {

            game.getPlayers()
                    .stream()
                    .filter(scoreService::hasFlip7)
                    .forEach(player -> player.setFlippedSeven(true));

            game.setStatus(
                    GameStatus.ROUND_END
            );

            calculateRoundScores(game);

            return;
        }

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
                // Persist round scores in roundHistory
                RoundResult roundResult = new RoundResult();
                roundResult.setRoundNumber(game.getCurrentRound());
                roundResult.setGame(game);

                for (Player player : game.getPlayers()) {

                        if (player.getStatus() != PlayerStatus.BUSTED) {
                                player.setTotalScore(
                                                player.getTotalScore() + player.getRoundScore()
                                );
                        }

                        RoundPlayerScore rps = RoundPlayerScore.builder()
                                        .playerId(player.getId())
                                        .playerName(player.getName())
                                        .score(player.getRoundScore())
                                        .busted(player.getStatus() == PlayerStatus.BUSTED)
                                        .flippedSeven(player.isFlippedSeven())
                                        .build();

                        roundResult.getScores().add(rps);
                }

                if (game.getRoundHistory() == null) {
                        game.setRoundHistory(new java.util.ArrayList<>());
                }

                game.getRoundHistory().add(roundResult);

                if (hasWinner(game)) {
                        game.setWinner(determineWinner(game));
                        game.setStatus(GameStatus.GAME_OVER);
                }
    }

        private void ensureGameIsNotFinished(Game game) {

                if (game.getStatus() == GameStatus.GAME_OVER) {
                        throw new IllegalStateException("Game is already finished");
                }
        }


        private boolean hasWinner(Game game) {

                return game.getPlayers().stream()
                        .anyMatch(player -> player.getTotalScore() >= 200);
        }

        private Winner determineWinner(Game game) {

                Player winnerPlayer = game.getPlayers().stream()
                        .max(java.util.Comparator.comparingInt(Player::getTotalScore))
                        .orElseThrow(() -> new IllegalStateException("No players in game"));

                return new Winner(
                        winnerPlayer.getId(),
                        winnerPlayer.getName(),
                        winnerPlayer.getTotalScore()
                );
        }

}