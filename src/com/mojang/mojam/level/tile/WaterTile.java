package com.mojang.mojam.level.tile;

import com.mojang.mojam.entity.Bullet;
import com.mojang.mojam.entity.Entity;
import com.mojang.mojam.entity.Player;
import com.mojang.mojam.entity.mob.Mob;
import com.mojang.mojam.entity.mob.Mummy;
import com.mojang.mojam.entity.mob.Pharaoh;
import com.mojang.mojam.entity.mob.Scarab;
import com.mojang.mojam.level.Level;
import com.mojang.mojam.screen.Art;
import com.mojang.mojam.screen.Bitmap;
import com.mojang.mojam.screen.Screen;

public class WaterTile extends Tile {
	public static final int COLOR0 = 0xff00CCff;
	public static final int COLOR1 = 0xff00CDff;
	private static final String NAME = "WATER";
	private boolean deep;

	public WaterTile(boolean depth) {
		this.deep = depth;
		img = deep ? 18 : 16;
		minimapColor = Art.floorTileColors[img & 7][img / 8];
	}
	
	public void init(Level level, int x, int y) {
		super.init(level, x, y);
	}
	
	public boolean isDeep(){
		return deep;
	}

	public void render(Screen screen) {
		super.render(screen);
	}

	public int getColor() {
		return deep ? WaterTile.COLOR1 : WaterTile.COLOR0;
	}

	public String getName() {
		return NAME;
	}

	@Override
	public boolean isBuildable() {
		return false;
	}
	
	@Override
	public boolean canPass(Entity e) {
		if (deep) {
			return (e instanceof Bullet || e instanceof Player || (e instanceof Mob && ((Mob) e).flying));
		}
		else {
			return !((e instanceof Mummy) || (e instanceof Pharaoh || (e instanceof Scarab)));
		}
	}
	
	public Bitmap getBitMapForEditor() {
		return Art.floorTiles[img & 7][img / 8];
	}
	
	@Override
	public int getMiniMapColor() {
		return minimapColor;
	}
}
