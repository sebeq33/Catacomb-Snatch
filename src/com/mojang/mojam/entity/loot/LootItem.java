package com.mojang.mojam.entity.loot;

import com.mojang.mojam.MojamComponent;
import com.mojang.mojam.entity.Entity;
import com.mojang.mojam.entity.Player;
import com.mojang.mojam.level.tile.HoleTile;
import com.mojang.mojam.network.TurnSynchronizer;
import com.mojang.mojam.screen.Art;
import com.mojang.mojam.screen.Bitmap;
import com.mojang.mojam.screen.Screen;


public class LootItem extends Entity {
	public double xMovement, yMovement, accelerationDirectionDelta;
	public double accelerationDirection;
	public Entity owner;
	public int life;
	public int item = 0;
	public int animationTime = 0;
	public boolean fake = false;
	private boolean isTakeable;
	private boolean disappears = true; 

	public static Bitmap[][][] animationArt = {Art.pickupKey, Art.pickupHeart, Art.pickupMap};
	public final static int PICKUP_KEY = 0;
	public final static int PICKUP_HEART = 1;
	public final static int PICKUP_MAP = 2;

	public LootItem(double x, double y, double xDirection, double yDirection, int lootValue, boolean disappears) {
		setup(x, y, xDirection, yDirection, lootValue, disappears);
	}

	public LootItem(double x, double y, double xDirection, double yDirection, int lootValue) {
		setup(x, y, xDirection, yDirection, lootValue, true);
	}
	
	public void setup(double x, double y, double xDirection, double yDirection, int lootValue, boolean disappears){
		pos.set(x, y);
		isTakeable = true;

		this.item = lootValue;
		
		double power = TurnSynchronizer.synchedRandom.nextDouble() * 1 + 1;
		this.xMovement = xDirection * power;
		this.yMovement = yDirection * power;
		this.accelerationDirectionDelta = TurnSynchronizer.synchedRandom.nextDouble() * 2 + 1.0;
		this.setSize(2, 2);
		this.disappears=disappears;
		physicsSlide = false;
		life = TurnSynchronizer.synchedRandom.nextInt(100) + 600;

		animationTime = TurnSynchronizer.synchedRandom.nextInt(animationArt[item].length * 3);
	}
	
	public void makeUntakeable() {
		isTakeable = false;
		life = 100 - TurnSynchronizer.synchedRandom.nextInt(40);
	}

	@Override
	public void tick() {
		animationTime++;
		if(coinWillFallenInHole()) {
			collide(this, xMovement, yMovement);
		}
		move(xMovement, yMovement);
		accelerationDirection += accelerationDirectionDelta;
		if (accelerationDirection < 0) {
			accelerationDirection = 0;
			xMovement *= 0.8;
			yMovement *= 0.8;
		} else {
			xMovement *= 0.98;
			yMovement *= 0.98;
		}
		
		accelerationDirectionDelta -= 0.2;
		if (this.disappears){
			if (--life < 0)
				remove();
		}
		
		if (isTakeable) {
			double fixDistance = 100;
			int absorbDistance = 16;
			for (Entity entity : level.getEntities(getBB().grow(fixDistance))) {
				if (!(entity instanceof LootItemCollector))
					continue;
				LootItemCollector collector = (LootItemCollector) entity;
				double xDelta = entity.pos.x - pos.x;
				double yDelta = entity.pos.y - pos.y;
				double distance = Math.sqrt(xDelta * xDelta + yDelta * yDelta);
				if (!collector.canTakeItem(this)) {
					double localDistance = 80;
					if (xDelta * xDelta + yDelta * yDelta < localDistance * localDistance) {
						xDelta /= distance;
						yDelta /= distance;
						double power = (1 - (distance / localDistance)) * 0.1;
						if (accelerationDirection <= 0) {
							xMovement -= xDelta * power;
							yMovement -= yDelta * power;
						}
					}
				}
				else {
					double suckPower = collector.getSuckPower();
					double suckDistance = 0;
					
					suckDistance = (fixDistance - 40) * suckPower + 40;
					
					if (distance < suckDistance) {
						collector.notifySucking();
						if (distance < absorbDistance) {
							onTake(collector);
							return;
						}
						xDelta /= distance;
						yDelta /= distance;
						double power = (1 - (distance / suckDistance)) * 1.6 * (suckPower * 0.5 + 0.5);
						if (accelerationDirection <= 0) {
							xMovement += xDelta * power;
							yMovement += yDelta * power;
						}
					}
				}
			}
		}
	}


	private boolean coinWillFallenInHole(){
	    return (Math.abs(xMovement) + Math.abs(yMovement)) < 0.1 && level.getTile(pos) instanceof HoleTile;
	}
	
	public void forceTake(LootItemCollector taker) {
		onTake(taker);
	}

	protected void onTake(LootItemCollector taker) {
		remove();
		taker.flash();
		taker.take(this);
		
		MojamComponent.soundPlayer.playSound("/sound/Gem.wav",(float) pos.x, (float) pos.y);
	}

	@Override
	protected boolean shouldBlock(Entity e) {
		return false;
	}

	@Override
	public void handleCollision(Entity entity, double xa, double ya) {
		if (isTakeable && entity instanceof Player) {
			((Player) entity).take(this);
		}
	}

	@Override
	public void render(Screen screen) {
		Bitmap[][] lootAnimation = animationArt[item];
		if (life > 60 * 3 || life / 2 % 2 == 0) {
			int frame = animationTime / 3 % lootAnimation.length;
			Bitmap currentFrame = lootAnimation[frame][0];
			if (accelerationDirection > 0) {
				screen.blit(Art.shadow, pos.x - 2, pos.y);
			}
			screen.blit(currentFrame, pos.x - currentFrame.w / 2, pos.y - currentFrame.h / 2 - 2 - accelerationDirection);
		}
	}
	
	public boolean isThisItem(int item){
		return item == this.item ? true : false;
	}
}