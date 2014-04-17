package com.mojang.mojam;

public class MouseWheel {

	
	public int[] currentState = new int[4];
	public int[] nextState = new int[4];

	public void setNextState(int button, int value) {
		nextState[button] = value;
	}

	public boolean isDown(int button) {
		return currentState[button] < 0;
	}
	
	public boolean isUp(int button) {
		return currentState[button] > 0;
	}
	
	public void tick() {
		for (int i = 0; i < currentState.length; i++) {
			currentState[i] = nextState[i];
		}
	}

	public void releaseAll() {
		for (int i = 0; i < nextState.length; i++) {
			nextState[i] = 0;
		}
	}

}
