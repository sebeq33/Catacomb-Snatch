package com.mojang.mojam.level.gamemode;

import java.util.Random;

import com.mojang.mojam.entity.building.SpawnerForBat;
import com.mojang.mojam.level.Level;
import com.mojang.mojam.level.tile.Tile;
import com.mojang.mojam.network.TurnSynchronizer;

public class GameModeVanilla extends GameMode implements IVictoryConditions {
	
	private boolean bVictoryAchieved;
	private int winner;
	
	@Override
	protected void setupPlayerSpawnArea() {
		super.setupPlayerSpawnArea();
		
		Random random = TurnSynchronizer.synchedRandom;		
		for (int i = 0; i < 11; i++) {
			double x = (random.nextInt(newLevel.width - 16) + 8) * Tile.WIDTH
					+ Tile.WIDTH / 2;
			double y = (random.nextInt(newLevel.height - 16) + 8) * Tile.HEIGHT
					+ Tile.HEIGHT / 2 - 4;
			if (newLevel.checkSpawnableEntityEmplacement(x, y)) {
				newLevel.addEntity(new SpawnerForBat(x, y));
			}
		}
	}
	
	@Override
	protected void loadColorTile(int color, int x, int y) {
		super.loadColorTile(color, x, y);
	}
	
	@Override
	protected void setTickItems() {
		newLevel.tickItems.add(new RandomSpawner());
	}

	@Override
	public void updateVictoryConditions(Level level) {
		if (level != null) {
            if (level.player1Score >= level.TARGET_SCORE) {
            	bVictoryAchieved = true;
            	winner = 1;
            }
            if (level.player2Score >= level.TARGET_SCORE) {
            	bVictoryAchieved = true;
            	winner = 2;
            }
        }
	}

	@Override
	public boolean isVictoryConditionAchieved() {
		return bVictoryAchieved;
	}

	@Override
	public int playerVictorious() {
		return winner;
	}
	
	@Override
	protected void setTargetScore() {
		newLevel.TARGET_SCORE = 100;
	}
}
