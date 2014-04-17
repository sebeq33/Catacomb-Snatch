package com.mojang.mojam.entity.building;

import com.mojang.mojam.entity.Player;
import com.mojang.mojam.entity.animation.BombExplodeAnimationSmall;
import com.mojang.mojam.entity.mob.Mob;
import com.mojang.mojam.entity.mob.Pharaoh;
import com.mojang.mojam.gui.Notifications;
import com.mojang.mojam.math.BB;
import com.mojang.mojam.network.TurnSynchronizer;
import com.mojang.mojam.screen.Art;
import com.mojang.mojam.screen.Bitmap;

/**
 * @author sebeq33
 */

public class SpawnerForPharaoh extends SpawnerEntity {

	public static final int COLOR = 0xffa50000;
	private boolean spawned = false;

	public SpawnerForPharaoh(double x, double y) {
		super(x, y);
		deathPoints = 5;
		spawnTime = -1;
		REGEN_HEALTH = false;
		minimapColor = 0xffff0000;
	}
	
	@Override
	public void tick() {
		double r = 32 * 4;
		if (level.getEntities(new BB(null, pos.x - r, pos.y - r, pos.x + r, pos.y + r), Player.class).size() > 0 && spawnTime < 0) 
			spawnTime = 150 + TurnSynchronizer.synchedRandom.nextInt(200);
		
		super.tick();
	}

	@Override
	protected Mob getMob(double x, double y) {
		return new Pharaoh(x,y);
	}
	
	@Override
	public int getColor() {
		return SpawnerForPharaoh.COLOR;
	}

	@Override
	public int getMiniMapColor() {
		return SpawnerForPharaoh.COLOR;
	}

	@Override
	public String getName() {
		return "PHARAOH";
	}
	
	@Override
	public Bitmap getBitMapForEditor() {
		Bitmap shrink = Bitmap.shrink(Art.pharaoh[1][3].copy());
		Bitmap bitmap = Art.pharaohSpawner[0][0].copy();
		bitmap.blit(shrink, (bitmap.w/2)-shrink.w/2,(bitmap.h/2)-5-shrink.h/2);
		return bitmap;
	}
	
	@Override
	public Bitmap getSprite() {
		int newIndex = (int)(3 - (3 * health) / maxHealth);
		return spawned ? Art.pharaohSpawner[4][0]: Art.pharaohSpawner[newIndex][0];
	}
	
	@Override
	public boolean spawn() {
		
		if (!spawned)
		{
			if (super.spawn())
			{
				level.addEntity(new BombExplodeAnimationSmall(pos.x, pos.y));
				Notifications.getInstance().add("A PHARAOH IS OUT OF HIS TOMB", 250);
				spawned = true;
				spawn = false;
				
				setStartHealth(1);
				deathPoints = 1;
				return true;
			}
		}
		else {
			spawn = false;
		}
		
		return false;
	}
	
}
