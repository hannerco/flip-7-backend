package edu.unac.service;

import edu.unac.model.game.Player;

public interface ScoreService {

    int calculateScore(Player player);

    boolean hasFlip7(Player player);
}