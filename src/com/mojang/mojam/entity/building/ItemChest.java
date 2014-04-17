package com.mojang.mojam.entity.building;

import java.util.Random;

import com.mojang.mojam.entity.Entity;
import com.mojang.mojam.entity.loot.LootItem;
import com.mojang.mojam.entity.mob.Mob;
import com.mojang.mojam.level.IEditable;
import com.mojang.mojam.network.TurnSynchronizer;
import com.mojang.mojam.screen.Art;
import com.mojang.mojam.screen.Bitmap;

public class ItemChest extends Building implements IEditable {

	public static final int keyChest_COLOR = 0xff0f2301;
	public static final int heartChest_COLOR = 0xff0f2302;
	public static final int mapChest_COLOR = 0xff0f2303;
	public static final int[] COLORS = {keyChest_COLOR, heartChest_COLOR, mapChest_COLOR};
	
    private int disappearTime = 0;
    private boolean empty = false;
    private int item;
    
    public ItemChest(double x, double y, int team, int item) {
        super(x, y, team);
        minimapColor = getColor();
        this.item = item;
    }
    
    @Override
	public Bitmap getSprite() {
        return Art.small_chest[empty ? 1 : 0][0];
    }
    
    @Override
	public void tick()
    {
        if(disappearTime > 0)
        {
            if(--disappearTime == 0 && empty)
            {
                die(); 
                remove();
            }
        }
    }

	@Override
	public boolean isNotFriendOf(Mob m) {
	    return false;
	}

    
    @Override
	public void use(Entity user)
    {
    	if (!empty)
    	{
	        empty = true;
	        disappearTime = 100;
	        
	        Random rand = TurnSynchronizer.synchedRandom = new Random();
	        
	        int quantity = item == LootItem.PICKUP_HEART ? 2 : 1;
	        for (int i = 0; i < quantity; i++) {
	        	double dir = rand.nextDouble() * Math.PI * 2;
	        	level.addEntity(new LootItem(pos.x, pos.y, Math.cos(dir), Math.sin(dir), item, (item == LootItem.PICKUP_HEART)));
			}
    	}
    }

    @Override
    public int getColor() {
        return ItemChest.COLORS[item];
    }

    @Override
    public int getMiniMapColor() {
    	return ItemChest.COLORS[item];
    }

    @Override
    public String getName() {
        return "ITEM CHEST";
    }

    @Override
    public Bitmap getBitMapForEditor() {
    	Bitmap itemIcon = LootItem.animationArt[item][3][0];
		Bitmap chest = Art.small_chest[0][0].copy();
		chest.blit(itemIcon, (chest.w/2),(chest.h/2 + 10));
		return chest;
    }
} 