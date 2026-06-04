package edu.unac.service.impl;

import edu.unac.model.card.Card;
import edu.unac.model.card.ModifierCard;
import edu.unac.model.card.NumericCard;
import edu.unac.model.enums.ModifierType;
import edu.unac.model.game.Player;
import edu.unac.service.ScoreService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScoreServiceImpl implements ScoreService {

    @Override
    public int calculateScore(Player player) {

        int score = player.getHand()
                .stream()
                .filter(card -> card instanceof NumericCard)
                .map(card -> (NumericCard) card)
                .mapToInt(NumericCard::getValue)
                .sum();

        List<ModifierCard> modifiers = player.getHand()
                .stream()
                .filter(card -> card instanceof ModifierCard)
                .map(card -> (ModifierCard) card)
                .toList();

        boolean hasDouble = modifiers.stream()
                .anyMatch(m -> m.getModifier() == ModifierType.TIMES_2);

        if (hasDouble) {
            score *= 2;
        }

        for (ModifierCard modifier : modifiers) {

            switch (modifier.getModifier()) {
                case PLUS_2 -> score += 2;
                case PLUS_4 -> score += 4;
                case PLUS_6 -> score += 6;
                case PLUS_8 -> score += 8;
                case PLUS_10 -> score += 10;
                case TIMES_2 -> {
                }
            }
        }

        if (hasFlip7(player)) {
            score += 15;
        }

        return score;
    }

    @Override
    public boolean hasFlip7(Player player) {

        return player.getHand()
                .stream()
                .filter(card -> card instanceof NumericCard)
                .map(card -> ((NumericCard) card).getValue())
                .distinct()
                .count() == 7;
    }
}