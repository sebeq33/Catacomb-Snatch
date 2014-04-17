package com.mojang.mojam.entity.building;

import java.util.Random;
import java.util.Set;

import com.mojang.mojam.entity.Entity;
import com.mojang.mojam.entity.Player;
import com.mojang.mojam.entity.building.Building;
import com.mojang.mojam.entity.loot.LootItem;
import com.mojang.mojam.entity.particle.Sparkle;
import com.mojang.mojam.level.IEditable;
import com.mojang.mojam.level.tile.Tile;
import com.mojang.mojam.math.BB;
import com.mojang.mojam.network.TurnSynchronizer;
import com.mojang.mojam.screen.Art;
import com.mojang.mojam.screen.Bitmap;

public class LockedDoor extends Building implements IEditable {
	
	public static final int COLOR = 0xff00CCCC;
	public static final String NAME = "LOCKED DOOR";
	private int timeBeforeChangeState = 15;
    private boolean open = false;
    private int state = 0;
    
    public LockedDoor(double x, double y, int team) {
		super(x, y, team);
		isImmortal = true;
		minimapColor = getColor();
	}
    
    public Bitmap getSprite() {
    	Bitmap bitmapForThisState = state == 4 ? Art.floorTiles[0][0] : Art.lockedDoor[open ? state : 0][0];
        return bitmapForThisState;
    }
    
    @Override
    public void tick()
    {
    	super.tick();
        if(open)
        {
        	timeBeforeChangeState--;
        	if (timeBeforeChangeState <= 0){
	        	state++;
	        	timeBeforeChangeState = 15;
	        	if (state == 1){
	        		level.addEntity(new Sparkle(pos.x, pos.y, -1, 0));
	        	}
	        	else if (state == 4){
	        		remove();
	        		
	        	}
        	}
        }
    }
    
    @Override
    public void use(Entity user)
    {
    	if (state == 0 && user instanceof Player)
    	{
    		Player player = (Player) user;
    		if (player.key > 0){
    			
    			player.dropKey();
    			
    			Random rand = TurnSynchronizer.synchedRandom = new Random();
                double dir = rand.nextDouble() * Math.PI * 2;
                LootItem lootItem = new LootItem(pos.x, pos.y, Math.cos(dir), Math.sin(dir), LootItem.PICKUP_KEY);
                lootItem.makeUntakeable();
        		level.addEntity(lootItem);
        		
    			double rx = Tile.WIDTH;
    			double ry = Tile.HEIGHT;
    			
    			Set<Entity> lockedDoors = level.getEntities(new BB(null, pos.x - rx, pos.y - ry, pos.x + rx, pos.y + ry), LockedDoor.class);
    			
    			if (lockedDoors.size() > 0)
    			{
    				for (Entity entity : lockedDoors) {
						LockedDoor lockeddoor = (LockedDoor) entity;
						lockeddoor.open = true;
					}
    			}
    		}
    	}
    }

    @Override
    public int getColor() {
        return LockedDoor.COLOR;
    }

    @Override
    public int getMiniMapColor() {
        return LockedDoor.COLOR;
    }

    @Override
    public String getName() {
        return LockedDoor.NAME;
    }

    @Override
    public Bitmap getBitMapForEditor() {
		return Art.lockedDoor[0][0];
    }
}
