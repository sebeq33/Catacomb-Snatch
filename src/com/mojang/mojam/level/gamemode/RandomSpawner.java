package com.mojang.mojam.level.gamemode;

import java.util.Random;

import com.mojang.mojam.entity.building.SpawnerEntity;
import com.mojang.mojam.level.Level;
import com.mojang.mojam.level.tile.Tile;
import com.mojang.mojam.network.TurnSynchronizer;

public class RandomSpawner implements ILevelTickItem {

	@Override
	public void tick(Level level) {
		Random random = TurnSynchronizer.synchedRandom;
		int width = level.width;
		int height = level.height;

		double x = (random.nextInt(width - 16) + 8) * Tile.WIDTH
				+ Tile.WIDTH / 2;
		double y = (random.nextInt(height - 16) + 8) * Tile.HEIGHT
				+ Tile.HEIGHT / 2 - 4;
		
		if (level.checkSpawnableEntityEmplacement(x,y)){
			level.addEntity(SpawnerEntity.getRandomSpawner(x, y));
		}
	}

}
