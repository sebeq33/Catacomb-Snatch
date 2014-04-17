package com.mojang.mojam.gui.components;

import java.util.ArrayList;
import java.util.List;

import com.mojang.mojam.MouseButtons;

public abstract class ClickableRightMiddleClick extends ClickableComponent{

	private List<ButtonListener> listeners;
	
	boolean rightClickIsPressed;
	boolean middleClickIsPressed;
	protected boolean performRightClick = false;
	protected boolean performMiddleClick = false;
	
	public ClickableRightMiddleClick(int x, int y, int w, int h) {
		super(x, y, w, h);
	}
	
	@Override
	public void tick(MouseButtons mouseButtons) {
		super.tick(mouseButtons);

		int mx = mouseButtons.getX();
		int my = mouseButtons.getY();
		
		rightClickIsPressed = false;
		middleClickIsPressed = false;
		
		if (enabled && mx >= getX() && my >= getY() && mx < (getX() + getWidth()) && my < (getY() + getHeight())) {
			
			if (mouseButtons.isReleased(3)) {
				postRightClick();
			} else if (mouseButtons.isDown(3)) {
				rightClickIsPressed = true;
			}
			
			if (mouseButtons.isReleased(2)) {
				postMiddleClick();
			} else if (mouseButtons.isDown(2)) {
				middleClickIsPressed = true;
			}
		}

		if (performRightClick) {
			if (listeners != null) {
				for (ButtonListener listener : listeners) {
					listener.buttonPressed(this);
				}
			}
			performRightClick = false;
		}
		
		if (performMiddleClick) {
			if (listeners != null) {
				for (ButtonListener listener : listeners) {
					listener.buttonPressed(this);
				}
			}
			performMiddleClick = false;
		}
	}
	
	private void postRightClick()
	{
		performRightClick = true;
	}
	
	private void postMiddleClick()
	{
		performMiddleClick = true;
	}
	
	/**
	 * This component is being right clicked on?
	 * @return boolean
	 */
	public boolean isRightClickPressed() {
		return rightClickIsPressed;
	}
	
	/**
	 * This component is being middle clicked on?
	 * @return boolean
	 */
	public boolean isMiddleClickPressed() {
		return middleClickIsPressed;
	}
	
	/**
	 * Adds a listener to the internal list, to get called when this component
	 * has been clicked
	 * 
	 * @param listener
	 */
	public void addListener(ButtonListener listener) {
		if (listeners == null) {
			listeners = new ArrayList<ButtonListener>();
		}
		listeners.add(listener);
	}

	@Override
	protected abstract void clicked(MouseButtons mouseButtons);

}
