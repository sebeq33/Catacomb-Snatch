package com.mojang.mojam.entity.mob;

import com.mojang.mojam.MojamComponent;
import com.mojang.mojam.entity.Player;
import com.mojang.mojam.entity.animation.EnemyDieAnimation;
import com.mojang.mojam.entity.loot.Loot;
import com.mojang.mojam.entity.loot.LootItem;
import com.mojang.mojam.level.tile.Tile;
import com.mojang.mojam.network.TurnSynchronizer;
import com.mojang.mojam.screen.Art;
import com.mojang.mojam.screen.Bitmap;

public class Pharaoh extends HostileMob {

    private int tick = 0;
    public static double ATTACK_RADIUS = 5 * Tile.WIDTH;

	public static final int COLOR = 0xffffdd00;
	
    public Pharaoh(double x, double y) {
        super(x, y, Team.Neutral);
        setPos(x, y);
        dir = TurnSynchronizer.synchedRandom.nextDouble() * Math.PI * 2;
        yOffs = 10;
        facing = TurnSynchronizer.synchedRandom.nextInt(4);
        minimapColor = 0xffff0000;
        REGEN_HEALTH = false;
        dropHeart = 0;
    }

    public void tick() {
        super.tick();
        if (freezeTime > 0) {
            return;
        }
        tick++;
        if (tick >= 20) {
            tick = 0;
            facing = faceEntity(pos.x, pos.y, ATTACK_RADIUS, Player.class, facing);
        }
        walk();
    }

    public void die() {
    	int particles = 8;

		if (getDeathPoints() > 0) {
			int loots = 8;
			for (int i = 0; i < loots; i++) {
				double dir = i * Math.PI * 2 / particles;

				level.addEntity(new Loot(pos.x, pos.y, Math.cos(dir), Math.sin(dir), getDeathPoints()));
			}
		}

		level.addEntity(new EnemyDieAnimation(pos.x, pos.y, 150));
		
		if (TurnSynchronizer.synchedRandom.nextInt(3) == 0) // 1/3 chance to create a key
			level.addEntity(new LootItem(pos.x, pos.y, Math.cos(dir), Math.sin(dir), 1));

		MojamComponent.soundPlayer.playSound(getDeathSound(), (float) pos.x, (float) pos.y);
    }

    public Bitmap getSprite() {
        return Art.pharaoh[((stepTime / 6) & 3)][(facing + 1) & 3];
    }

    @Override
    public String getDeathSound() {
        return "/sound/pharao_dies.wav";
    }

	@Override
	public int getColor() {
		return COLOR;
	}

	@Override
	public int getMiniMapColor() {
		return COLOR;
	}

	@Override
	public String getName() {
		return "PHARAOH";
	}

	@Override
	public Bitmap getBitMapForEditor() {
		return Art.pharaoh[0][0];
	}
}