package com.mojang.mojam.gui;

import java.awt.event.KeyEvent;

import com.mojang.mojam.MojamComponent;
import com.mojang.mojam.gui.components.Button;
import com.mojang.mojam.gui.components.ClickableComponent;
import com.mojang.mojam.gui.components.Font;
import com.mojang.mojam.screen.Art;
import com.mojang.mojam.screen.Screen;

public class ExitMenu extends GuiMenu {
	private final int gameWidth;
    private final int gameHeight;
    private final boolean ingame;
    
    private Button menuButton = null;
	private Button exit_game = null;
	private Button resumeButton = null;

	public void changeLocale() {
		menuButton.setLabel(MojamComponent.texts.getStatic("pausemenu.backtomain"));
		exit_game.setLabel(MojamComponent.texts.getStatic("pausemenu.exit"));
		if (resumeButton != null) resumeButton.setLabel(MojamComponent.texts.getStatic("back"));
	}

	public ExitMenu(int gameWidth, int gameHeight, boolean ingame) {
		super();
		this.gameWidth = gameWidth;
		this.gameHeight = gameHeight;
		this.ingame = ingame;
		
		if (ingame)
		{
			menuButton = (Button) addButton(new Button(TitleMenu.RETURN_TO_TITLESCREEN, MojamComponent.texts.getStatic("pausemenu.backtomain"), (gameWidth - 128) / 2, 170));
			exit_game = (Button) addButton(new Button(TitleMenu.REALLY_EXIT_GAME_ID, MojamComponent.texts.getStatic("pausemenu.exit"), (gameWidth - 128) / 2, 200));
			resumeButton = (Button) addButton(new Button(TitleMenu.BACK_ID, MojamComponent.texts.getStatic("back"), (gameWidth - 128) / 2, 270));
		}
		else
		{
			menuButton = (Button) addButton(new Button(TitleMenu.BACK_ID, MojamComponent.texts.getStatic("pausemenu.backtomain"), (gameWidth - 128) / 2, 170));
			exit_game = (Button) addButton(new Button(TitleMenu.REALLY_EXIT_GAME_ID, MojamComponent.texts.getStatic("pausemenu.exit"), (gameWidth - 128) / 2, 200));
		}
	}

	public void render(Screen screen) {

		if (!ingame) {
			screen.alphaFill(0, 0, gameWidth, gameHeight, 0xff000000, 0x01); 
		}
		else {
			screen.alphaFill(0, 0, gameWidth, gameHeight, 0xff000000, 0x30);
		}
		screen.blit(Art.emptyBackgroundWindow, (gameWidth - 128) / 2 - 118, (gameHeight - 128) / 2 - 64);

		super.render(screen);
		
		int yOffset = (gameHeight - (CharacterButton.HEIGHT * 2 + 20)) / 2 - 20;
		Font.defaultFont().draw(screen, MojamComponent.texts.getStatic("exitmenu.headline"),
				MojamComponent.GAME_WIDTH / 2, yOffset, Font.Align.CENTERED);
		screen.blit(Art.getLocalPlayerArt()[0][6], (gameWidth - 128) / 2 - 40,
				160 + selectedItem * (selectedItem == 2 ? 50 : 30));
	}
	
	
	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			if (resumeButton == null) menuButton.postClick(); else resumeButton.postClick();
		} else {
			super.keyPressed(e);
		}		
	}

	@Override
	public void buttonPressed(ClickableComponent button) {}

	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyReleased(KeyEvent e) {}
}
