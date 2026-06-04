package edu.unac.service;

import edu.unac.model.card.Card;
import edu.unac.model.game.Game;

import java.util.List;
import java.util.UUID;

public interface DeckService {

    List<Card> buildDeck();

}