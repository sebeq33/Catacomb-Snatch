package com.mojang.mojam.entity.loot;

public interface LootItemCollector {
	public boolean canTake();

	public void take(LootItem lootItem);

	public double getSuckPower();

	public void notifySucking();

	public int getScore();

	public void flash();

	boolean canTakeItem(LootItem lootItem);
}
