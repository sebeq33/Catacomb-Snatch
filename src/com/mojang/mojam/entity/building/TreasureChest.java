package com.mojang.mojam.entity.building;

import java.util.Random;

import com.mojang.mojam.entity.Entity;
import com.mojang.mojam.entity.loot.Loot;
import com.mojang.mojam.entity.mob.Mob;
import com.mojang.mojam.level.IEditable;
import com.mojang.mojam.network.TurnSynchronizer;
import com.mojang.mojam.screen.Art;
import com.mojang.mojam.screen.Bitmap;

public class TreasureChest extends Building implements IEditable {

	public static final int COLOR = 0xff0f2300;
    private int value = 0, status = 0, disappearTime = 0;
    private boolean empty = false;
    
    public TreasureChest(double x, double y, int team, int value) {
        super(x, y, team);
        
        this.value = value;
        minimapColor = getColor();
    }
    
    public Bitmap getSprite() {
        return Art.small_chest[status][0];
    }
    
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

    
    public void use(Entity user)
    {
        status = 1;
        empty = true;
        disappearTime = 100;
        
        Random rand = TurnSynchronizer.synchedRandom = new Random();
        
        while (value > 0) {
            double dir = rand.nextDouble() * Math.PI * 2;
            Loot loot = new Loot(pos.x, pos.y, Math.cos(dir), Math.sin(dir), value / 15);
            level.addEntity(loot);

            value -= loot.getScoreValue();
        }
        value = 0;
    }

    @Override
    public int getColor() {
        return TreasureChest.COLOR;
    }

    @Override
    public int getMiniMapColor() {
        return TreasureChest.COLOR;
    }

    @Override
    public String getName() {
        return "TREASURE CHEST";
    }

    @Override
    public Bitmap getBitMapForEditor() {
    	Bitmap coin = Art.pickupCoinGold[1][0].copy();
		Bitmap chest = Art.small_chest[0][0].copy();
		chest.blit(coin, (chest.w/2),(chest.h/2 + 10));
		return chest;
    }
} 