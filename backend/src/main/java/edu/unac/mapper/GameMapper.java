package edu.unac.mapper;

import edu.unac.dto.response.CreateGameResponse;
import edu.unac.dto.response.FinishedGameResponse;
import edu.unac.dto.response.GameResponse;
import edu.unac.dto.response.RoundPlayerScoreResponse;
import edu.unac.dto.response.RoundResultResponse;
import edu.unac.dto.response.PlayerResponse;
import edu.unac.dto.response.WinnerResponse;
import edu.unac.model.game.Game;
import edu.unac.model.game.Player;
import edu.unac.model.game.RoundPlayerScore;
import edu.unac.model.game.RoundResult;
import edu.unac.model.game.Winner;
import org.springframework.stereotype.Component;
import edu.unac.dto.response.PendingActionResponse;
import edu.unac.dto.response.AutomaticEventResponse;
import edu.unac.model.enums.CardType;
import edu.unac.model.enums.PlayerStatus;

@Component
public class GameMapper {
    public GameResponse toResponse(Game game) {

        GameResponse gameResponse = new GameResponse();
        gameResponse.setId(game.getId());
        gameResponse.setStatus(game.getStatus());
        gameResponse.setCurrentRound(game.getCurrentRound());
        gameResponse.setCurrentPlayerId(game.getCurrentPlayerId());
        gameResponse.setDealerId(game.getDealerId());
        gameResponse.setRoundStarterId(game.getDealerId());
        gameResponse.setPlayers(game.getPlayers().stream().map(this::toPlayerResponse).toList());
        gameResponse.setRoundHistory(
            game.getRoundHistory() == null
                ? java.util.List.of()
                : game.getRoundHistory().stream().map(this::toRoundResultResponse).toList()
        );
        gameResponse.setWinner(
            game.getWinner() == null ? null : toWinnerResponse(game.getWinner())
        );

        // map pending action
        if (game.getPendingAction() != null) {
            PendingActionResponse par = new PendingActionResponse();
            par.setId(game.getPendingAction().getId());
            par.setType(game.getPendingAction().getType());
            par.setSourcePlayerId(game.getPendingAction().getSourcePlayerId());
            par.setRemainingCards(game.getPendingAction().getRemainingCards());

            if (game.getPendingAction().getCard() != null) {
                edu.unac.dto.response.CardResponse cr = new edu.unac.dto.response.CardResponse();
                cr.setId(game.getPendingAction().getCard().getId());
                cr.setType(game.getPendingAction().getCard().getType());
                cr.setKind(game.getPendingAction().getCard().getClass().getSimpleName().replace("Card", "").toUpperCase());

                if (game.getPendingAction().getCard() instanceof edu.unac.model.card.NumericCard nc) {
                    cr.setValue(nc.getValue());
                } else if (game.getPendingAction().getCard() instanceof edu.unac.model.card.ModifierCard mc) {
                    cr.setModifier(mc.getModifier());
                }

                par.setCard(cr);
            }

            // compute target options: active players excluding source
            java.util.List<java.util.UUID> targets = new java.util.ArrayList<>();
            for (edu.unac.model.game.Player p : game.getPlayers()) {
                if (p.getStatus() == edu.unac.model.enums.PlayerStatus.ACTIVE
                        && !p.getId().equals(game.getPendingAction().getSourcePlayerId())) {
                    targets.add(p.getId());
                }
            }

            par.setTargetOptions(targets);
            gameResponse.setPendingAction(par);
        }

        // map last automatic event
        if (game.getLastAutomaticEvent() != null) {
            AutomaticEventResponse aer = new AutomaticEventResponse();
            aer.setType(game.getLastAutomaticEvent().getType());
            aer.setPlayerId(game.getLastAutomaticEvent().getPlayerId());

            if (game.getLastAutomaticEvent().getRemovedDuplicateCard() != null) {
                edu.unac.dto.response.CardResponse cr = new edu.unac.dto.response.CardResponse();
                cr.setId(game.getLastAutomaticEvent().getRemovedDuplicateCard().getId());
                cr.setType(game.getLastAutomaticEvent().getRemovedDuplicateCard().getType());
                cr.setKind(game.getLastAutomaticEvent().getRemovedDuplicateCard().getClass().getSimpleName().replace("Card", "").toUpperCase());

                if (game.getLastAutomaticEvent().getRemovedDuplicateCard() instanceof edu.unac.model.card.NumericCard nc) {
                    cr.setValue(nc.getValue());
                } else if (game.getLastAutomaticEvent().getRemovedDuplicateCard() instanceof edu.unac.model.card.ModifierCard mc) {
                    cr.setModifier(mc.getModifier());
                }

                aer.setRemovedDuplicateCard(cr);
            }

            if (game.getLastAutomaticEvent().getRemovedSecondChanceCard() != null) {
                edu.unac.dto.response.CardResponse cr2 = new edu.unac.dto.response.CardResponse();
                cr2.setId(game.getLastAutomaticEvent().getRemovedSecondChanceCard().getId());
                cr2.setType(game.getLastAutomaticEvent().getRemovedSecondChanceCard().getType());
                cr2.setKind(game.getLastAutomaticEvent().getRemovedSecondChanceCard().getClass().getSimpleName().replace("Card", "").toUpperCase());
                aer.setRemovedSecondChanceCard(cr2);
            }

            gameResponse.setLastAutomaticEvent(aer);
        }

        return gameResponse;
    }

    public CreateGameResponse toCreateResponse(Game game) {
        CreateGameResponse createGameResponse = new CreateGameResponse();
        createGameResponse.setGameId(game.getId());

        return createGameResponse;
    }

    public FinishedGameResponse toFinishedGameResponse(Game game) {
        FinishedGameResponse response = new FinishedGameResponse();
        response.setId(game.getId());
        response.setCurrentRound(game.getCurrentRound());
        response.setPlayersCount(game.getPlayers() == null ? 0 : game.getPlayers().size());
        response.setWinner(game.getWinner() == null ? null : toWinnerResponse(game.getWinner()));

        return response;
    }

    private PlayerResponse toPlayerResponse(
            Player player
    ) {
        PlayerResponse playerResponse = new PlayerResponse();
        playerResponse.setId(player.getId());
        playerResponse.setName(player.getName());
        playerResponse.setTotalScore(player.getTotalScore());
        playerResponse.setRoundScore(player.getRoundScore());
        playerResponse.setStatus(player.getStatus());
        // secondChance is determined by presence of a SECOND_CHANCE card in `cards`
        playerResponse.setFlippedSeven(player.isFlippedSeven());
        // map player's hand to CardResponse list
        if (player.getHand() == null || player.getHand().isEmpty()) {
            playerResponse.setCards(java.util.List.of());
        } else {
            java.util.List<edu.unac.dto.response.CardResponse> cards = new java.util.ArrayList<>();
            for (edu.unac.model.card.Card card : player.getHand()) {
                edu.unac.dto.response.CardResponse cr = new edu.unac.dto.response.CardResponse();
                cr.setId(card.getId());
                cr.setType(card.getType());
                cr.setKind(card.getClass().getSimpleName().replace("Card", "").toUpperCase());

                if (card instanceof edu.unac.model.card.NumericCard nc) {
                    cr.setValue(nc.getValue());
                } else if (card instanceof edu.unac.model.card.ModifierCard mc) {
                    cr.setModifier(mc.getModifier());
                }

                cards.add(cr);
            }
            playerResponse.setCards(cards);
        }

        return playerResponse;
    }

    // removed helper toCardResponse to avoid type inference issues in some JDKs

    private RoundResultResponse toRoundResultResponse(RoundResult roundResult) {
        RoundResultResponse response = new RoundResultResponse();
        response.setId(roundResult.getId());
        response.setRoundNumber(roundResult.getRoundNumber());
        response.setScores(roundResult.getScores().stream().map(this::toRoundPlayerScoreResponse).toList());

        return response;
    }

    private RoundPlayerScoreResponse toRoundPlayerScoreResponse(RoundPlayerScore roundPlayerScore) {
        RoundPlayerScoreResponse response = new RoundPlayerScoreResponse();
        response.setId(roundPlayerScore.getId());
        response.setPlayerId(roundPlayerScore.getPlayerId());
        response.setPlayerName(roundPlayerScore.getPlayerName());
        response.setScore(roundPlayerScore.getScore());
        response.setBusted(roundPlayerScore.isBusted());
        response.setFlippedSeven(roundPlayerScore.isFlippedSeven());

        return response;
    }

    private WinnerResponse toWinnerResponse(Winner winner) {
        WinnerResponse response = new WinnerResponse();
        response.setId(winner.getId());
        response.setName(winner.getName());
        response.setTotalScore(winner.getTotalScore());

        return response;
    }
}
