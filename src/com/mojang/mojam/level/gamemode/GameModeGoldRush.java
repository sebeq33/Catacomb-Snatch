package com.mojang.mojam.level.gamemode;

import java.util.Random;

import com.mojang.mojam.MojamComponent;
import com.mojang.mojam.entity.building.SpawnerForBat;
import com.mojang.mojam.level.Level;
import com.mojang.mojam.level.tile.Tile;
import com.mojang.mojam.network.TurnSynchronizer;

public class GameModeGoldRush extends GameMode{
	protected boolean bVictoryAchieved;
	protected int winner;
	
	@Override
	protected void setupPlayerSpawnArea() {
		super.setupPlayerSpawnArea();
		
		Random random = TurnSynchronizer.synchedRandom;		
		for (int i = 0; i < 15; i++) {
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
	protected void setTickItems() {
		newLevel.tickItems.add(new RandomSpawner());
	}
	
	@Override
	protected void setVictoryCondition() {
		newLevel.victoryConditions = this;
	}
	
	@Override
	protected void setTargetScore() {
		TARGET_SCORE = 5000;
	}

	@Override
	public void updateVictoryConditions(Level level) {
		if(MojamComponent.instance.players[0] != null)
			level.player1Score = MojamComponent.instance.players[0].score;
		if(MojamComponent.instance.players[1] != null)
			level.player2Score = MojamComponent.instance.players[1].score;
		
		if (level.player1Score >= TARGET_SCORE) {
        	bVictoryAchieved = true;
        	winner = 1;
        }
        if (level.player2Score >= TARGET_SCORE) {
        	bVictoryAchieved = true;
        	winner = 2;
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
}
