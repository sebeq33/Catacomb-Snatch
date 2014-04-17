package com.mojang.mojam.entity.building;

import com.mojang.mojam.entity.Entity;
import com.mojang.mojam.entity.mob.Mob;
import com.mojang.mojam.entity.mob.Team;
import com.mojang.mojam.gui.TitleMenu;
import com.mojang.mojam.level.IEditable;
import com.mojang.mojam.level.tile.Tile;
import com.mojang.mojam.network.TurnSynchronizer;
import com.mojang.mojam.screen.Art;
import com.mojang.mojam.screen.Bitmap;
import com.mojang.mojam.screen.Screen;

/**
 * Spawner entity. A sarcophage which spawns enemies of a given type onto the field.
 */
public abstract class SpawnerEntity extends Building implements IEditable {
	/** Spawn interval in frames*/
	public static final int SPAWN_INTERVAL = 60 * 4;

	public int spawnTime = 0;
	public boolean spawn = true;

	private int lastIndex = 0;

	/**
	 * Constructor
	 * 
	 * @param x Initial X coordinate
	 * @param y Initial Y coordinate
	 * @param type Mob type
	 */
	public SpawnerEntity(double x, double y) {
		super(x, y, Team.Neutral);

		setStartHealth(20);
		freezeTime = 10;
		spawnTime = TurnSynchronizer.synchedRandom.nextInt(SPAWN_INTERVAL);
		minimapIcon = 4;
		healthBarOffset = 22;
		deathPoints = 0 * 5 + 5;
		yOffs = 0;
	}

	@Override
	public void tick() {
		super.tick();
		if (freezeTime > 0)
			return;
		
		if (!spawn) return;

		if (spawnTime == 0) {
			spawn();
			spawnTime = TitleMenu.difficulty.calculateSpawntime(SPAWN_INTERVAL);
		} else if (spawnTime > 0) spawnTime --;
	}

	/**
	 * Spawn a new enemy of the given type onto the field.
	 * PUBLIC because SpawnerForPharaoh rewrite it 
	 */
	public boolean spawn() {
		double x = pos.x + (TurnSynchronizer.synchedRandom.nextFloat() - 0.5)
				* 5;
		double y = pos.y + (TurnSynchronizer.synchedRandom.nextFloat() - 0.5)
				* 5;
		x=Math.max(Math.min(x, level.width*Tile.WIDTH), 0);//spawn only inside the level!
		y=Math.max(Math.min(y, level.height*Tile.HEIGHT), 0);
		int xin=(int)x/ Tile.WIDTH;
		int yin=(int)y/ Tile.HEIGHT;
		Tile spawntile = level.getTile(xin, yin);
		Mob te = getMob(x,y);
		
		if (level.countEntities(Mob.class) < level.maxMonsters && level.getEntities(te.getBB().grow(8), te.getClass()).size() == 0 && spawntile.canPass(te))
		{
			return level.addMob(te,xin,yin) ? true : false;
		}	
		else return false;
	}
	
	protected abstract Mob getMob(double x, double y);

	@Override
	public Bitmap getSprite() {
		int newIndex = (int)(3 - (3 * health) / maxHealth);
		if (newIndex != lastIndex) {
			// if (newIndex > lastIndex) // means more hurt
			// level.addEntity(new SmokeAnimation(pos.x - 12, pos.y - 20,
			// Art.fxSteam24, 40));
			lastIndex = newIndex;
		}
		return Art.mobSpawner[newIndex][0];
	}

	public static Entity getRandomSpawner(double x, double y) {
		
		// more spawner mean more chance to be choose
		final SpawnerEntity[] spawnMobList = {
				new SpawnerForBat(x,y),	new SpawnerForBat(x,y),
				new SpawnerForSnake(x,y), new SpawnerForSnake(x,y),
				new SpawnerForMummy(x,y), new SpawnerForMummy(x,y),
				new SpawnerForScarab(x,y), new SpawnerForScarab(x,y),
				new SpawnerForPharaoh(x,y)
		};
		
		int nextInt =  TurnSynchronizer.synchedRandom.nextInt(spawnMobList.length);
		
		return spawnMobList[nextInt];
	}
	
	@Override
	public Bitmap getBitMapForEditor() {
		return Art.mobSpawner[0][0];
	}
	
	@Override
	public boolean isHighlightable() {
		return false;
	}

	@Override
    public void render(Screen screen) {
	    super.render(screen);
	    screen.blit(Art.mobSpawnerShadow, pos.x - Art.mobSpawnerShadow.w / 2 - 1, pos.y - Art.mobSpawnerShadow.h / 2 + 7);
    }
	
	protected Bitmap blitMobOnTop(Bitmap mobicon) {
		Bitmap bitmap = Art.mobSpawner[0][0].copy();
		bitmap.blit(mobicon, (bitmap.w/2)-mobicon.w/2,(bitmap.h/2)-5-mobicon.h/2);
		return bitmap;
	}
	
}
