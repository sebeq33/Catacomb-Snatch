package com.mojang.mojam.gui;

import java.awt.event.KeyEvent;

import com.mojang.mojam.MojamComponent;
import com.mojang.mojam.MouseButtons;
import com.mojang.mojam.Options;
import com.mojang.mojam.gui.components.Button;
import com.mojang.mojam.gui.components.ClickableComponent;
import com.mojang.mojam.gui.components.Font;
import com.mojang.mojam.gui.components.Panel;
import com.mojang.mojam.level.LevelList;
import com.mojang.mojam.screen.Art;
import com.mojang.mojam.screen.Screen;

public class LevelSelect extends GuiMenu {
    
    private final int LEVELS_PER_PAGE = 9;

    private int currentPage = 0;
	private LevelButton[] levelButtons = null;
	private Button[] deleteButtons = null;
	
	private final int xButtons = (MojamComponent.GAME_WIDTH / LevelButton.WIDTH);
	private final int xSpacing = LevelButton.WIDTH + 8;
	private final int ySpacing = LevelButton.HEIGHT + 8;
	private final int xStart = (MojamComponent.GAME_WIDTH - (xSpacing * xButtons) + 8) / 2;
	private final int yStart = 50;

	private LevelButton activeButton;
	
	private Button startGameButton;
	private Button cancelButton;
    private Button previousPageButton;
    private Button nextPageButton;
    
    private Button confirmeDeleteButton = new Button(-1, MojamComponent.texts.getStatic("levelselect.confirmDelete"), 125, 190);
    private Button cancelDeleteButton = new Button(-1, MojamComponent.texts.getStatic("cancel"), 265, 190);
    private Panel deletePanel = new Panel(110, 120, 298, 105) {

    	@Override
        public void render(Screen screen) {
            super.render(screen);
            Font.defaultFont().draw(screen, "You will delete the map : "+ LevelList.get(activeButton.getId()).levelName +".",
                    screen.w / 2 , getY() + 20, Font.Align.CENTERED);
            Font.defaultFont().draw(screen, "ARE YOU SURE TO DELETE IT ?",
            		screen.w / 2 , getY() + 40, Font.Align.CENTERED);
        }
    };
    
    private int nextActiveButtonId = 0;
    private boolean outdatedLevelButtons = false;
    private boolean deleteVisible = false;
    private boolean updateButtons = false;
	
	public int action;
	
	public LevelSelect(int source) {
		super();		
		
		// Get all levels
		TitleMenu.level = LevelList.get(0);

		// Add main buttons
		if (source == TitleMenu.SELECT_HOST_LEVEL_ID)
		{
			this.action = TitleMenu.SELECT_DIFFICULTY_HOSTING_ID;
			startGameButton = (Button) addButton(new Button(-1 , MojamComponent.texts.getStatic("levelselect.start"), 
				MojamComponent.GAME_WIDTH - 256 - 30, MojamComponent.GAME_HEIGHT - 24 - 25));
			cancelButton = (Button) addButton(new Button(TitleMenu.CANCEL_JOIN_ID, MojamComponent.texts.getStatic("cancel"), 
				MojamComponent.GAME_WIDTH - 128 - 20, MojamComponent.GAME_HEIGHT - 24 - 25));
		}
		else if (source == TitleMenu.SELECT_LEVEL_ID)
		{
			this.action = TitleMenu.SELECT_DIFFICULTY_ID;
			startGameButton = (Button) addButton(new Button(-1 , MojamComponent.texts.getStatic("levelselect.start"), 
				MojamComponent.GAME_WIDTH - 256 - 30, MojamComponent.GAME_HEIGHT - 24 - 25));
			cancelButton = (Button) addButton(new Button(TitleMenu.BACK_ID, MojamComponent.texts.getStatic("cancel"), 
				MojamComponent.GAME_WIDTH - 128 - 20, MojamComponent.GAME_HEIGHT - 24 - 25));
		}
		else if (source == TitleMenu.SELECT_LEVEL_EDITOR)
		{
			this.action = TitleMenu.RETURN_TO_EDITOR;
			startGameButton = (Button) addButton(new Button(-1 , MojamComponent.texts.getStatic("levelselect.start"), 
					MojamComponent.GAME_WIDTH - 256 - 30, MojamComponent.GAME_HEIGHT - 24 - 25));
			cancelButton = (Button) addButton(new Button(TitleMenu.BACK_ID, MojamComponent.texts.getStatic("cancel"), 
					MojamComponent.GAME_WIDTH - 128 - 20, MojamComponent.GAME_HEIGHT - 24 - 25));
		}
		
		// Add page buttons
		if (LevelList.size() > LEVELS_PER_PAGE) {
	        previousPageButton = (Button) addButton(new Button(TitleMenu.LEVELS_PREVIOUS_PAGE_ID, "(", 
	                xStart, MojamComponent.GAME_HEIGHT - 24 - 25, 30, Button.BUTTON_HEIGHT));
	        nextPageButton = (Button) addButton(new Button(TitleMenu.LEVELS_PREVIOUS_PAGE_ID, ")", 
	                xStart + 40, MojamComponent.GAME_HEIGHT - 24 - 25, 30, Button.BUTTON_HEIGHT));
		}
        
        // Create level
		goToPage(0);
	}

	private void goToPage(int page) {
        currentPage = page;
        nextActiveButtonId = 0;
        outdatedLevelButtons = true;
    }

    private void updateLevelButtons() {
    	int y = 0;
    	
    	// Remove previous buttons
    	if (levelButtons != null) {
            for (int i = 0; i < levelButtons.length; i++) {
                if (levelButtons[i] != null) {
                    removeButton(levelButtons[i]);
                    removeButton(deleteButtons[i]);
                }
            }
    	}
    	
    	// Create level buttons
        levelButtons = new LevelButton[Math.min(LEVELS_PER_PAGE,
        		LevelList.size() - currentPage * LEVELS_PER_PAGE)];
        deleteButtons = new Button[Math.min(LEVELS_PER_PAGE,
        		LevelList.size() - currentPage * LEVELS_PER_PAGE)];
        
    	for (int i = currentPage * LEVELS_PER_PAGE;
    	         i < Math.min((currentPage + 1) * LEVELS_PER_PAGE, LevelList.size());
    	         i++) {
    		int x = i % xButtons;
    		int buttonIndex = i % LEVELS_PER_PAGE;
    		
    		levelButtons[buttonIndex] = (LevelButton) addButton(new LevelButton(i, LevelList.get(i), 
    		        xStart + x * xSpacing, yStart + ySpacing * y));
    		
    		if (!LevelList.get(buttonIndex + (currentPage * LEVELS_PER_PAGE)).vanilla) {
    			deleteButtons[buttonIndex] = (Button) addButton(new Button(-1 , "", xStart + x * xSpacing , yStart + ySpacing * y, 24, 24, Art.trashIcon));
    		}
    		
    		if (buttonIndex == nextActiveButtonId) {
    			activeButton = levelButtons[nextActiveButtonId];
    			activeButton.setActive(true);
    		}
    
    		if (x == (xButtons - 1)) {
    			y++;
    		}
    	}
    }

    private boolean hasPreviousPage() {
	    return currentPage > 0;
	}
	
    private boolean hasNextPage() {
        return (currentPage + 1) * LEVELS_PER_PAGE < LevelList.size();
    }
	
    @Override
    public void tick(MouseButtons mouseButtons) {
        super.tick(mouseButtons);
        
        if (updateButtons) {
            updateDeleteButtons();
            updateButtons = false;
        }
        
        if (outdatedLevelButtons) {
            updateLevelButtons();
            outdatedLevelButtons = false;
        }
    }
    
    @Override
    public void render(Screen screen) {
    	screen.blit(Art.emptyBackground, 0, 0);
    	
    	// Draw disabled page buttons
    	if (LevelList.size() > LEVELS_PER_PAGE) {
	    	if (!hasPreviousPage()) {
	    	    previousPageButton.render(screen);
	    	    screen.fill(previousPageButton.getX() + 4, previousPageButton.getY() + 4,
	    	            previousPageButton.getWidth() - 8, previousPageButton.getHeight() - 8, 0x75401f);
	    	}
	        if (!hasNextPage()) {
	            nextPageButton.render(screen);
	            screen.fill(nextPageButton.getX() + 4, nextPageButton.getY() + 4,
	                    nextPageButton.getWidth() - 8, nextPageButton.getHeight() - 8, 0x75401f);
	        }
	        previousPageButton.enabled = hasPreviousPage();
	        nextPageButton.enabled = hasNextPage();
    	}

    	super.render(screen);
    	Font.defaultFont().draw(screen, MojamComponent.texts.getStatic("levelselect.title"), 20, 20);
    }
    
    private void updateDeleteButtons() {
        if (deleteVisible) {
            addButton(deletePanel);
            addButton(confirmeDeleteButton);
            addButton(cancelDeleteButton);
        }
        else {
            removeButton(deletePanel);
            removeButton(confirmeDeleteButton);
            removeButton(cancelDeleteButton);
        }
    }
    @Override
    public void buttonPressed(ClickableComponent button) {
    
    	if (deleteVisible) {
            if (button == confirmeDeleteButton) {
            	if (LevelList.checkLevel(LevelList.get(activeButton.getId()).levelName)) 
            	{
            		if (LevelList.deleteLevel(activeButton.getId())) {
            			deleteVisible = false;
 	                    updateButtons = true;
 	                    outdatedLevelButtons = true;
 	                }
            	}
            }
            
            if (button == confirmeDeleteButton || button == cancelDeleteButton) {
            	deleteVisible = false; 
            	updateButtons = true;
            }
            return;
    	}
            
    	if (button instanceof LevelButton) {
    
    		LevelButton lb = (LevelButton) button;
    
    		if (activeButton != null && activeButton != lb) {
    			activeButton.setActive(false);
    		}
    
    		activeButton = lb;
    	}
    	
    	for (int i = 0; i < deleteButtons.length; i++) {
			if (button == deleteButtons[i])
			{
				activeButton = levelButtons[i];
				if (!LevelList.get(activeButton.getId()).vanilla){
	    			updateButtons = true;
	    			deleteVisible = true;
	    		}
				continue;
			}
		}
    	
    	if (button == previousPageButton && hasPreviousPage()) {
    	    goToPage(currentPage - 1);
    	}
        else if (button == nextPageButton && hasNextPage()) {
            goToPage(currentPage + 1);
        }
        else if (button == startGameButton)
    	{
    		if (action == TitleMenu.RETURN_TO_EDITOR) LevelEditorMenu.selectedLevel = activeButton.getId();
    		else TitleMenu.level = LevelList.get(activeButton.getId());
    		
    		MojamComponent.instance.handleAction(action);
    	}
    }
    

    @Override
    public void keyPressed(KeyEvent e) {

    	if (deleteVisible) {
    		if (e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == Options.getAsInteger("key_use")) {
        		confirmeDeleteButton.postClick();
        	}
    		else if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
    		{
    			cancelDeleteButton.postClick();
    		}
    		return;
    	}
    	
    	if (e.getKeyCode() == KeyEvent.VK_PAGE_UP && hasPreviousPage()) {
    		goToPage(currentPage - 1);
    	}
    	else if (e.getKeyCode() == KeyEvent.VK_PAGE_DOWN && hasNextPage()) {
    		goToPage(currentPage + 1);
    	}
    	else if (e.getKeyCode() == KeyEvent.VK_DELETE) {
    		if (!LevelList.get(activeButton.getId()).vanilla){
    			updateButtons = true;
    			deleteVisible = true;
    		}
    	}
    	else {
    		
	        // Compute new id
	        int activeButtonId = activeButton.getId();
	    	nextActiveButtonId = -1;
	    	if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == Options.getAsInteger("key_left")) {
	    		if (activeButtonId == (currentPage * LEVELS_PER_PAGE) && hasPreviousPage()) 
	    		{
	    			currentPage--;
	    			nextActiveButtonId = (currentPage * LEVELS_PER_PAGE) + LEVELS_PER_PAGE - 1;;
	    			outdatedLevelButtons = true;
	    		}
	    		else nextActiveButtonId = activeButtonId - 1 - (currentPage * LEVELS_PER_PAGE);
	    	}
	    	else if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == Options.getAsInteger("key_right")) {
	    		if (activeButtonId == (currentPage * LEVELS_PER_PAGE) + LEVELS_PER_PAGE -1 && hasNextPage()) 
	    		{
	    			currentPage++;
	    			nextActiveButtonId = (currentPage * LEVELS_PER_PAGE) - LEVELS_PER_PAGE;
	    			outdatedLevelButtons = true;
	    			
	    		}
	    		else nextActiveButtonId = activeButtonId + 1 - (currentPage * LEVELS_PER_PAGE);
	    	}
	    	else if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == Options.getAsInteger("key_up")) {
	    		nextActiveButtonId = activeButtonId - 3 - (currentPage * LEVELS_PER_PAGE);
	    	}
	    	else if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == Options.getAsInteger("key_down")) {
	    		nextActiveButtonId = activeButtonId + 3 - (currentPage * LEVELS_PER_PAGE);
	    	}
	    
	    	// Update active button
	    	if (nextActiveButtonId >= 0 && nextActiveButtonId < levelButtons.length) {
	    		activeButton.setActive(false);
	    		activeButton = levelButtons[nextActiveButtonId];
	    		activeButton.setActive(true);
	    		activeButton.postClick();
	    	}
	    
	    	// Start on Enter, Cancel on Escape
	    	if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == Options.getAsInteger("key_use")) {
	    		startGameButton.postClick();	    	
	    	}
	    	if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
	    		cancelButton.postClick();
	    	}
    	
    	}
    	
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

}
