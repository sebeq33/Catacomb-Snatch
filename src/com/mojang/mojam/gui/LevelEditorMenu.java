package com.mojang.mojam.gui;

import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;

import com.mojang.mojam.MojamComponent;
import com.mojang.mojam.MouseButtons;
import com.mojang.mojam.Options;
import com.mojang.mojam.entity.Entity;
import com.mojang.mojam.entity.building.ItemChest;
import com.mojang.mojam.entity.building.LockedDoor;
import com.mojang.mojam.entity.building.SpawnerForBat;
import com.mojang.mojam.entity.building.SpawnerForMummy;
import com.mojang.mojam.entity.building.SpawnerForPharaoh;
import com.mojang.mojam.entity.building.SpawnerForScarab;
import com.mojang.mojam.entity.building.SpawnerForSnake;
import com.mojang.mojam.entity.building.TreasureChest;
import com.mojang.mojam.entity.building.TreasurePile;
import com.mojang.mojam.entity.building.Turret;
import com.mojang.mojam.entity.building.TurretTeamOne;
import com.mojang.mojam.entity.building.TurretTeamTwo;
import com.mojang.mojam.entity.loot.LootItem;
import com.mojang.mojam.entity.mob.Bat;
import com.mojang.mojam.entity.mob.Mummy;
import com.mojang.mojam.entity.mob.Pharaoh;
import com.mojang.mojam.entity.mob.Scarab;
import com.mojang.mojam.entity.mob.Snake;
import com.mojang.mojam.entity.mob.SpikeTrap;
import com.mojang.mojam.gui.components.Button;
import com.mojang.mojam.gui.components.ClickableComponent;
import com.mojang.mojam.gui.components.ClickableRightMiddleClick;
import com.mojang.mojam.gui.components.Font;
import com.mojang.mojam.gui.components.Panel;
import com.mojang.mojam.gui.components.Slider;
import com.mojang.mojam.gui.components.Text;
import com.mojang.mojam.level.IEditable;
import com.mojang.mojam.level.LevelInformation;
import com.mojang.mojam.level.LevelList;
import com.mojang.mojam.level.LevelUtils;
import com.mojang.mojam.level.tile.DestroyableWallTile;
import com.mojang.mojam.level.tile.DropTrap;
import com.mojang.mojam.level.tile.FloorTile;
import com.mojang.mojam.level.tile.HoleTile;
import com.mojang.mojam.level.tile.SandTile;
import com.mojang.mojam.level.tile.Tile;
import com.mojang.mojam.level.tile.UnbreakableRailTile;
import com.mojang.mojam.level.tile.UnpassableSandTile;
import com.mojang.mojam.level.tile.WallTile;
import com.mojang.mojam.level.tile.WaterTile;
import com.mojang.mojam.screen.Art;
import com.mojang.mojam.screen.Bitmap;
import com.mojang.mojam.screen.Screen;

public class LevelEditorMenu extends GuiMenu {

    private int LEVEL_WIDTH = 48;//48
    private int LEVEL_HEIGHT = 48; //48
    private final int TILE_WIDTH = Tile.WIDTH; //32
    private final int TILE_HEIGHT = Tile.HEIGHT; //32
    private final int MENU_WIDTH = 142;
    
    private int mapW = LEVEL_WIDTH * TILE_WIDTH;
    private int mapH = LEVEL_HEIGHT * TILE_HEIGHT;
    private int mapX = -450; // -450
    private int mapY = -1200; // -1200
    
    private int[][] mapTile = new int[LEVEL_HEIGHT][LEVEL_WIDTH];
    private Bitmap[][] map = new Bitmap[LEVEL_HEIGHT][LEVEL_WIDTH];
    private Bitmap mapFloor = new Bitmap(mapW, mapH);
    private Bitmap minimap = new Bitmap(LEVEL_HEIGHT, LEVEL_WIDTH);
    
    private Bitmap displaymap = new Bitmap(48, 48);
        
    private Bitmap pencil = new Bitmap(TILE_WIDTH, TILE_HEIGHT);
    private int pencilX = 280;//280;
    private int pencilY = LEVEL_HEIGHT * (TILE_HEIGHT - 1);
    private int pencilColor = 0xffcfac02;
    private boolean drawing;
    
    private final IEditable[] editableTiles = {
        new FloorTile(), // always first for erase
        new SandTile(),
        new UnpassableSandTile(),
        new WallTile(),
        new DestroyableWallTile(),
        new LockedDoor(0, 0, 0),
        new WaterTile(false),
        new WaterTile(true),
        new HoleTile(),
        new DropTrap(),
        new SpikeTrap(0, 0),
        new TreasurePile(0, 0),
        new UnbreakableRailTile(new FloorTile()),
        new TreasureChest(0, 0, 0, 0),
        new ItemChest(0, 0, 0, LootItem.PICKUP_KEY),
        new ItemChest(0, 0, 0, LootItem.PICKUP_MAP),
        new ItemChest(0, 0, 0, LootItem.PICKUP_HEART),
        new Turret(0, 0, 0),
        new TurretTeamOne(0, 0),
        new TurretTeamTwo(0, 0),
        new SpawnerForBat(0, 0),
        new SpawnerForSnake(0, 0),
        new SpawnerForMummy(0, 0),
        new SpawnerForScarab(0, 0),
        new SpawnerForPharaoh(0, 0),
        new Bat(0,0),
        new Snake(0,0),
        new Mummy(0,0),
        new Scarab(0,0),
        new Pharaoh(0,0)
    };
    
    private int buttonsPerPage = 12;
    private final int totalPages = (int) Math.ceil(editableTiles.length / (float) buttonsPerPage);
    private int currentPage = 0;
    private final int buttonsCols = 3;
    private final int buttonMargin = 1;
    private final int buttonsX = 7;
    private final int buttonsY = 20;
    
    private LevelEditorButton[] tileButtons;
    private LevelEditorButton selectedButton;
    
    private Button prevPageButton;
    private Button nextPageButton;
    private Button newButton;
    private Button openButton;
    private Button saveButton;
    private Button cancelButton;
    private Button confirmeSaveButton;
    private Button cancelSaveButton;
    private ClickableComponent sliderNewMapX;
    private ClickableComponent sliderNewMapY;
    private Button confirmeNew;
    private Button cancelNew;
    
    private Panel savePanel;
    private Panel confirmSavePanel;
    private Panel menuNewPanel;
    private ClickableRightMiddleClick editorComponent;
    private Text levelName;
    
    private boolean clicked;
    private boolean updateButtons;
    private boolean updateTileButtons;
    private boolean saveMenuVisible;
    private boolean confirmSaveVisible;
    private boolean newMenuVisible;
    private int timeForMoveWithMouseOnLeft = 10;
    
    public static int selectedLevel = 0;
    
    private String saveLevelName = "";
    private Random random = new Random();
    
    public LevelEditorMenu() {
        super();
    	
        createGUI();
        setCurrentPage(0);
        
        // loads first level on the list
        if (selectedLevel < 0 || selectedLevel > LevelList.size()) selectedLevel = 0;
        
        openLevel(LevelList.get(selectedLevel));

        addButtonListener(this);
    }
    
    private void createGUI() {
        
        levelName = new Text(1,"", 120, 5);
        
        // map clickable component
        editorComponent = (ClickableRightMiddleClick) addButton(new ClickableRightMiddleClick(MENU_WIDTH, 0, MojamComponent.GAME_WIDTH - MENU_WIDTH, MojamComponent.GAME_HEIGHT) {

            @Override
            protected void clicked(MouseButtons mouseButtons) {
                // do nothing, handled by button listeners
            }
        });
        
        // menu panel
        addButton(new Panel(0, 0, MENU_WIDTH, MojamComponent.GAME_HEIGHT));
        
        // minimap panel
        addButton(new Panel(MojamComponent.GAME_WIDTH - minimap.w - 11, 1, minimap.w + 10, minimap.w + 10));
        
        // save menu panel
        savePanel = new Panel(180, 120, 298, 105) {
            @Override
            public void render(Screen screen) {
                super.render(screen);
                Font.defaultFont().draw(screen, MojamComponent.texts.getStatic("leveleditor.enterLevelName"),
                        getX() + getWidth() / 2, getY() + 20, Font.Align.CENTERED);
                Font.defaultFont().draw(screen, saveLevelName + "_",
                        getX() + getWidth() / 2, getY() + 40, Font.Align.CENTERED);
            }
        };
        
        confirmSavePanel = new Panel(180, 120, 298, 105) {
            @Override
            public void render(Screen screen) {
                super.render(screen);
                Font.defaultFont().draw(screen, "A file with this name already exist.",
                        getX() + getWidth() / 2, getY() + 20, Font.Align.CENTERED);
                Font.defaultFont().draw(screen, "ARE YOU SURE TO FILE OVERWRITE IT ?",
                        getX() + getWidth() / 2, getY() + 40, Font.Align.CENTERED);
            }
        };
        
        menuNewPanel = new Panel(180, 120, 298, 105) {
            @Override
            public void render(Screen screen) {
                super.render(screen);
                Font.defaultFont().draw(screen, "Select height and width for the new level.",
                        getX() + getWidth() / 2, getY() + 20, Font.Align.CENTERED);
            }
        };

        // save menu buttons
        confirmeSaveButton = new Button(-1, MojamComponent.texts.getStatic("leveleditor.save"), 195, 190);
        cancelSaveButton = new Button(-1, MojamComponent.texts.getStatic("cancel"), 335, 190);
        
        //new level buttons
        sliderNewMapX = new Slider(-1, "X", 195, 155, 0.48f);
        sliderNewMapY = new Slider(-1, "Y", 335, 155, 0.48f);
        confirmeNew = new Button(-1, MojamComponent.texts.getStatic("leveleditor.new"), 195, 190);
        cancelNew = new Button(-1, MojamComponent.texts.getStatic("cancel"), 335, 190);

        // actions buttons
        int startY = (MojamComponent.GAME_HEIGHT - 5) - 26 * 5;
        prevPageButton = (Button) addButton(new Button(-1, "(", 7, startY, 30, Button.BUTTON_HEIGHT));
        nextPageButton = (Button) addButton(new Button(-1, ")", MENU_WIDTH - 37, startY, 30, Button.BUTTON_HEIGHT));
        newButton = (Button) addButton(new Button(-1, MojamComponent.texts.getStatic("leveleditor.new"), 7, startY += 26));
        openButton = (Button) addButton(new Button(-1, MojamComponent.texts.getStatic("leveleditor.open"), 7, startY += 26));
        saveButton = (Button) addButton(new Button(-1, MojamComponent.texts.getStatic("leveleditor.save"), 7, startY += 26));
        cancelButton = (Button) addButton(new Button(TitleMenu.BACK_ID, MojamComponent.texts.getStatic("back"), 7, startY += 26));
    }
    
    private void getCurrentTileButtons(int x, int y) {
        
        if (x > LEVEL_WIDTH  - 1 || x < 0 || y > LEVEL_HEIGHT - 1 || y < 0 ) return;
        
    	for (int i = 0; i < editableTiles.length; i++) {
			if (editableTiles[i].getColor() == mapTile[x][y]){
				updateTileButtons(i);
				break;
			}
		}
    }
    
    private void updateTileButtons(int selected) {
    	int y = 0;
    	
        // Remove previous buttons
        if (tileButtons != null) {
            for (int i = 0; i < tileButtons.length; i++) {
                if (tileButtons[i] != null) {
                    removeButton(tileButtons[i]);
                }
            }
        }
        
        if (!updateTileButtons){
	        if (selected > buttonsPerPage - 1) {
	        	currentPage = selected / buttonsPerPage;
	        	selected = selected - (currentPage * buttonsPerPage);
	        }
	        else { currentPage = 0;}
        }
        
        tileButtons = new LevelEditorButton[Math.min(buttonsPerPage, editableTiles.length - currentPage * buttonsPerPage)];

        for (int i = currentPage * buttonsPerPage; i < Math.min((currentPage + 1) * buttonsPerPage, editableTiles.length); i++) {
            int x = i % buttonsCols;
            int id = i % buttonsPerPage;
            
            tileButtons[id] = (LevelEditorButton) addButton(new LevelEditorButton(i, editableTiles[i], buttonsX + x * (LevelEditorButton.WIDTH + buttonMargin), buttonsY + y));
            
            if (id == selected) {
                selectedButton = tileButtons[id];
                selectedButton.setActive(true);
            }

            if (x == (buttonsCols - 1)) {
                y += LevelEditorButton.HEIGHT + buttonMargin;
            }
        }
        
        if (!hasPreviousPage()) prevPageButton.enabled = false; else prevPageButton.enabled = true;
        if (!hasNextPage()) nextPageButton.enabled = false; else nextPageButton.enabled = true;
    }
    
    private boolean hasPreviousPage() {
	    return currentPage > 0;
	}
	
    private boolean hasNextPage() {
        return (currentPage + 1) * buttonsPerPage < editableTiles.length;
    }
    
    private void setCurrentPage(int page) {
        currentPage = page;
        updateTileButtons = true;
    }
    
    private void updateSaveButtons() {
        if (saveMenuVisible) {
        	saveLevelName = selectedLevel == -1 ? "New Level" : LevelList.get(selectedLevel).levelName;
        	if (saveLevelName.contains("+ ")) saveLevelName = saveLevelName.replaceAll("\\+ ", "");
            addButton(savePanel);
            addButton(confirmeSaveButton);
            addButton(cancelSaveButton);
        }
        else {
            removeButton(savePanel);
            removeButton(confirmeSaveButton);
            removeButton(cancelSaveButton);
            
        	if (confirmSaveVisible)
            {
            	addButton(confirmSavePanel);
            	addButton(confirmeSaveButton);
                addButton(cancelSaveButton);
            }
        	else {
        		removeButton(confirmSavePanel);
                removeButton(confirmeSaveButton);
                removeButton(cancelSaveButton);
        	}
        	
        	if (newMenuVisible)
        	{
        		addButton(menuNewPanel);
        		addButton(sliderNewMapX);
            	addButton(sliderNewMapY);
                addButton(confirmeNew);
                addButton(cancelNew);
        	}
        	else {
        		removeButton(menuNewPanel);
        		removeButton(sliderNewMapX);
                removeButton(sliderNewMapY);
                removeButton(confirmeNew);
                removeButton(cancelNew);
        	}
        }
    }

	private void openLevel(LevelInformation li) {
		BufferedImage bufferedImage = null;

		try {
			if (li.vanilla) {
				bufferedImage = ImageIO.read(MojamComponent.class
						.getResource(li.getPath()));
			} else {
				bufferedImage = ImageIO.read(new File(li.getPath()));
			}
			
			updateMapConstants(bufferedImage.getWidth(), bufferedImage.getHeight());

			int[] rgbs = new int[LEVEL_WIDTH * LEVEL_HEIGHT];
			bufferedImage.getRGB(0, 0, LEVEL_WIDTH, LEVEL_HEIGHT, rgbs, 0, LEVEL_WIDTH);
			
			//change map name
			removeText(levelName);
			levelName = new Text(1, li.levelName, 120, 5);
			addText(levelName);

			for (int y = 0; y < LEVEL_HEIGHT; y++) {
				for (int x = 0; x < LEVEL_WIDTH; x++) {
					mapFloor.blit(Art.floorTiles[random.nextInt(3)][0], TILE_WIDTH * x, TILE_HEIGHT * y);
					
					int col = rgbs[x + y * LEVEL_WIDTH] & 0xffffffff;

					IEditable tile = LevelUtils.getNewTileFromColor(col);
					draw(tile, x, y);

					if (tile instanceof FloorTile) {
						Entity entity = LevelUtils.getNewEntityFromColor(col,
								x, y);
						if (entity instanceof IEditable) {
							draw((IEditable) entity, x, y);
						}
					}
				}
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	private void updateMapConstants(int levelwidth, int levelheight){
		//setup map height and width
		LEVEL_WIDTH = levelwidth;
		LEVEL_HEIGHT = levelheight;
		
		mapW = LEVEL_WIDTH * TILE_WIDTH;
	    mapH = LEVEL_HEIGHT * TILE_HEIGHT;
	    mapX = -((mapW)/ 2) + 11 * TILE_WIDTH;//(int) -(Math.floor((LEVEL_WIDTH - 1) / 2) * TILE_WIDTH) + (7 * TILE_WIDTH); 
	    mapY = -mapH + 11 * TILE_HEIGHT;//(int) -(mapH - (0.1 * mapH)) + (6 * TILE_HEIGHT); 
	    
	    mapTile = new int[LEVEL_WIDTH][LEVEL_HEIGHT];
	    map = new Bitmap[LEVEL_WIDTH][LEVEL_HEIGHT];
	    mapFloor = new Bitmap(mapW, mapH);
	    minimap = new Bitmap(LEVEL_WIDTH, LEVEL_HEIGHT);
	    
	    displaymap = new Bitmap(Math.min(minimap.w, 48), Math.min(minimap.h, 48));
	    
	    // setup pencil
        pencil = new Bitmap(TILE_WIDTH, TILE_HEIGHT);
	    pencilX = TILE_WIDTH * ((LEVEL_WIDTH - 1) / 2);//280;
	    pencilY = LEVEL_HEIGHT * (TILE_HEIGHT - 1);
	}
	
	private void draw(IEditable tileOrEntity, int x, int y) {

        if (x < 0 || x > LEVEL_WIDTH - 1) return;
        if (y < 0 || y > LEVEL_HEIGHT - 1) return;
        if (mapTile[x][y] == tileOrEntity.getColor()) return;
        
        if (tileOrEntity.getColor() != FloorTile.COLOR) {
            map[x][y] = tileOrEntity.getBitMapForEditor();
        } else {
            map[x][y] = null;
        }
        
        mapTile[x][y] = tileOrEntity.getColor();
        minimap.fill(x, y, 1, 1, tileOrEntity.getMiniMapColor() );
    }

	private void newLevel(int width, int height) {
		updateMapConstants(width, height);
		int widthCenter = (int)Math.floor((LEVEL_WIDTH - 1) / 2);
		int heightCenter = (int)Math.floor((LEVEL_HEIGHT - 1) / 2);
		
		minimap.fill(0, 0, minimap.w, minimap.h, editableTiles[0].getMiniMapColor());
		
		//create a new level with wall in 4 side, 2 rail by player, and a treasure in center of the map
		for (int x = 0; x < LEVEL_WIDTH; x++) {
			for (int y = 0; y < LEVEL_HEIGHT; y++) {
				mapFloor.blit(Art.floorTiles[random.nextInt(3)][0], TILE_WIDTH * x, TILE_HEIGHT * y);
				mapTile[x][y] = editableTiles[0].getMiniMapColor();
				
				if (y == 0 || y == LEVEL_HEIGHT - 1 || x == 0 || x == LEVEL_WIDTH - 1)
				{
					if (x == widthCenter - 1 || x == widthCenter + 1)
					{
						UnbreakableRailTile rail = new UnbreakableRailTile(new FloorTile());
						mapTile[x][y] = UnbreakableRailTile.COLOR;
						map[x][y] = rail.getBitMapForEditor();
						minimap.pixels[x + y * minimap.w] = rail.getMiniMapColor();
					}
					else if (x != widthCenter)
					{
						WallTile wall = new WallTile();
						mapTile[x][y] = WallTile.COLOR;
						map[x][y] = wall.getBitMapForEditor();
						minimap.pixels[x + y * minimap.w] = wall.getMiniMapColor();
					}
				}
				else if (y == heightCenter && x == widthCenter){
					TreasurePile treasure = new TreasurePile(0,0);
					mapTile[x][y] = TreasurePile.COLOR;
					map[x][y] = treasure.getBitMapForEditor();
					minimap.pixels[x + y * minimap.w] = treasure.getMiniMapColor();
				}
			}
		}
		selectedLevel = -1;
		
		removeText(levelName);
		levelName = new Text(1, "New Level", 120, 5);
		addText(levelName);
	}
	
	@Override
    public void render(Screen screen) {
        screen.clear(0);

        // level floor
        screen.blit(mapFloor, mapX, mapY);
        
        // level tiles
        for (int x = 0; x < LEVEL_WIDTH; x++) {
            for (int y = 0; y < LEVEL_HEIGHT; y++) {
            	
                if (map[x][y] == null) continue; // = floortile
   
                Bitmap tile = map[x][y];

                // change tiles that requires some sort of drawing modification
                switch (mapTile[x][y]) {
                    case HoleTile.COLOR:
                        if (y > 0 && (mapTile[x][y - 1] == HoleTile.COLOR)) {
                            tile = null;
                        } else if (y > 0 && (mapTile[x][y - 1] == SandTile.COLOR || mapTile[x][y - 1] == UnpassableSandTile.COLOR)) {
                            tile = Art.floorTiles[7][0];
                        }
                        else if (y > 0 && (mapTile[x][y - 1] == WaterTile.COLOR0)) {
                            tile = Art.floorTiles[1][2];
                        }
                        else if (y > 0 && (mapTile[x][y - 1] == WaterTile.COLOR1)) {
                            tile = Art.floorTiles[3][2];
                        }
                        break;
                    case UnbreakableRailTile.COLOR:
                        boolean n = y > 0 && mapTile[x][y - 1] == UnbreakableRailTile.COLOR;
                        boolean s = y < LEVEL_HEIGHT - 1 && mapTile[x][y + 1] == UnbreakableRailTile.COLOR;
                        boolean w = x > 0 && mapTile[x - 1][y] == UnbreakableRailTile.COLOR;
                        boolean e = x < LEVEL_WIDTH - 1 && mapTile[x + 1][y] == UnbreakableRailTile.COLOR;

                        int c = (n ? 1 : 0) + (s ? 1 : 0) + (w ? 1 : 0) + (e ? 1 : 0);
                        int img;

                        if (c <= 1) {
                            img = (n || s) ? 1 : 0;     // default is horizontal
                        } else if (c == 2) {
                            if (n && s) {
                                img = 1;                // vertical
                            } else if (w && e) {
                                img = 0;                // horizontal
                            } else {
                                img = n ? 4 : 2;        // north turn
                                img += e ? 0 : 1;       // south turn
                            }
                        } else {                        // 3 or more turning disk
                            img = 6;
                        }

                        map[x][y] = Art.rails[img][0];
                        break;
                }
                  
                // draw the tile or fill with black if it's null
                if (tile != null) {
                    screen.blit(tile,
                            x * TILE_WIDTH - (tile.w - TILE_WIDTH) / 2 + mapX,
                            y * TILE_HEIGHT - (tile.h - TILE_HEIGHT) + mapY);
                } else {
                    screen.fill(x * TILE_WIDTH + mapX, y * TILE_HEIGHT + mapY, TILE_WIDTH, TILE_HEIGHT, 0);
                }
            }
        }
        
        pencil.fill(0, 0, pencil.w, pencil.h, pencilColor);
        pencil.fill(1, 1, pencil.w - 2, pencil.h - 2, 0);
        
        // pencil position indicator
        for (int x = 0; x < LEVEL_WIDTH; x++) {
            for (int y = 0; y < LEVEL_HEIGHT; y++) {
                if (x == (((pencilX + TILE_WIDTH / 2) - mapX) / TILE_WIDTH) && y == (((pencilY + TILE_HEIGHT / 2) - mapY) / TILE_HEIGHT)) {
                	screen.blit(pencil, TILE_HEIGHT * x + mapX, TILE_HEIGHT * y + mapY);
                    break;
                }
            }
        }

        super.render(screen);
        
        // minimap
        screen.blit(displaymap, screen.w - displaymap.w - 6 - ((48 - displaymap.w) / 2), 6 + ((48 - displaymap.h) / 2));
        
        // selected tile name
        Font.defaultFont().draw(screen, selectedButton != null ? selectedButton.getTile().getName() : "",
        		MENU_WIDTH / 2, 13, Font.Align.CENTERED);
        
        // current page and total pages
        Font.defaultFont().draw(screen, (currentPage + 1) + "/" + totalPages,
        		MENU_WIDTH / 2, 261, Font.Align.CENTERED);
    }
    
    @Override
    public void tick(MouseButtons mouseButtons) {
        super.tick(mouseButtons);
        
        // show/hide save menu buttons
        if (updateButtons) {
            updateSaveButtons();
            updateButtons = false;
        }
        
        // update tile buttons
        if (updateTileButtons){
            updateTileButtons(0);
            updateTileButtons = false;
        }
        
        // lock buttons when save menu is visible
        if (saveMenuVisible || confirmSaveVisible || newMenuVisible) return;

        if (!mouseButtons.mouseHidden)
        {
        	// update pencil location
        	pencilX = mouseButtons.getX() - (TILE_WIDTH / 2);
        	pencilY = mouseButtons.getY() - (TILE_HEIGHT / 2);
        
	        // move level x with mouse
	        if (mouseButtons.getX() > MENU_WIDTH) {
	            if (pencilX + TILE_WIDTH > MojamComponent.GAME_WIDTH
	                    && -(mapX - MENU_WIDTH) < mapW - (MojamComponent.GAME_WIDTH - MENU_WIDTH) + TILE_HEIGHT) {
	                mapX -= TILE_WIDTH / 2;
	                
	            } else if (pencilX + 32 > MENU_WIDTH && pencilX < MENU_WIDTH + 16 && mapX < MENU_WIDTH + 32) {
	            	//added a delay for moving on left
	            	if (timeForMoveWithMouseOnLeft == 0) mapX += TILE_WIDTH / 2; 
	            	else timeForMoveWithMouseOnLeft--;
	            }
	            else timeForMoveWithMouseOnLeft = 10;
	        }
	        
	        // move level y with mouse
	        if (pencilY + TILE_HEIGHT > MojamComponent.GAME_HEIGHT
	                && -mapY < mapH - MojamComponent.GAME_HEIGHT + TILE_HEIGHT) {
	            mapY -= TILE_HEIGHT / 2;
	        } else if (pencilY < 0 && mapY < TILE_HEIGHT) {
	            mapY += TILE_HEIGHT / 2;
	        }
        }
               
        // draw
        int hoverX = (((pencilX + TILE_WIDTH / 2) - mapX) / TILE_WIDTH);
        int hoverY = (((pencilY + TILE_HEIGHT / 2) - mapY) / TILE_HEIGHT);
        
        int xScroll = minimap.w > 48 ? MojamComponent.clampi(-(mapX / TILE_WIDTH), 0, (minimap.w - displaymap.w)) : 0;
    	int yScroll = minimap.h > 48 ? MojamComponent.clampi(-(mapY / TILE_HEIGHT), 0 , (minimap.h - displaymap.h)) : 0;
        
		for (int y = 0; y < displaymap.h; y++) {
			for (int x = 0; x < displaymap.w; x++) {
				if  (((x + xScroll) + (y + yScroll) * minimap.w) == (hoverX + hoverY * LEVEL_WIDTH))
				{
					displaymap.pixels[x + y * displaymap.w] = pencil.pixels[0];
				}
				else displaymap.pixels[x + y * displaymap.w] = minimap.pixels[(x + xScroll) + (y + yScroll) * minimap.w];
			}
    	}
        
        if (editorComponent.isPressed()) {
            draw(selectedButton.getTile(), hoverX, hoverY);
            drawing = false;
            pencilColor = 0xffffdd00;
    	}
        else if (editorComponent.isRightClickPressed()) {
        	draw(editableTiles[0], hoverX, hoverY);
        	drawing = false;
        	pencilColor = 0xffff0000;
        }
        else if (editorComponent.isMiddleClickPressed()) {
        	getCurrentTileButtons(hoverX, hoverY);
        	drawing = false;
        	pencilColor = 0xff00CCCC;
        }
        else if (drawing){
        	draw(selectedButton.getTile(), hoverX, hoverY);
        	pencilColor = 0xff00ff00;
        }
        else {
        	pencilColor = 0xffcfac02;
        }
    }
    
    @Override
    public void buttonPressed(ClickableComponent button) {
              
        // save menu buttons
        if (saveMenuVisible) {
            if (button == confirmeSaveButton) {
            	if (LevelList.checkLevel(saveLevelName)) 
            	{
            		confirmSaveVisible = true; 
            		saveMenuVisible = false;
            		updateButtons = true;
            		return;
            	}
            	else{
	                if (LevelList.saveLevel(LEVEL_WIDTH, LEVEL_HEIGHT, mapTile, saveLevelName)) {
	                    removeText(levelName);
	                    levelName = new Text(1, "+ " + saveLevelName, 120, 5);
	                    addText(levelName);
	                    
	                    saveMenuVisible = false;
	                    updateButtons = true;
	                    saveLevelName = "";
	                }
            	}
            }
            
            if (button == cancelSaveButton) {
            	saveMenuVisible = false;
                updateButtons = true;
                saveLevelName = "";
            }

            return;
        }
        
        //confirm save if levelfile already exist
        if (confirmSaveVisible) {
        	if (button == confirmeSaveButton) {
        		if (LevelList.saveLevel(LEVEL_WIDTH, LEVEL_HEIGHT, mapTile, saveLevelName)) {
                    removeText(levelName);
                    levelName = new Text(1, "+ " + saveLevelName, 120, 5);
                    addText(levelName);
                }
        	}
        	
        	if (button == confirmeSaveButton || button == cancelSaveButton) {
        		confirmSaveVisible = false;
                updateButtons = true;
                saveLevelName = "";
            }
        	
        	return;
        }
        
        if (newMenuVisible) {
        	if (button == confirmeNew) {
        		boolean valid = true;
        		if (((Slider) sliderNewMapX).getIntValue() < 25){
        			((Slider) sliderNewMapX).setValue(0.25f);
        			valid = false;
                }
        		if (((Slider) sliderNewMapY).getIntValue() < 25){
        			((Slider) sliderNewMapY).setValue(0.25f);
        			valid = false;
                }
        		if (valid){
        			newLevel(((Slider) sliderNewMapX).getIntValue(), ((Slider) sliderNewMapY).getIntValue());
        			newMenuVisible = false;
                    updateButtons = true;
        		}
        	}
        	
        	if (button == cancelNew) {
        		newMenuVisible = false;
                updateButtons = true;
            }
        	
        	return;
        }
        
        // tile buttons
        if (button instanceof LevelEditorButton) {
            LevelEditorButton lb = (LevelEditorButton) button;

            if (selectedButton != null && selectedButton != lb) {
                selectedButton.setActive(false);
                selectedButton = lb;
                selectedButton.setActive(true);
            }

            return;
        }

        // menu buttons
        if (!clicked) {
            if (button == newButton) {
                newMenuVisible = true;
                updateButtons = true;
            } 
            else if (button == openButton)
            {
            	MojamComponent.instance.handleAction(TitleMenu.SELECT_LEVEL_EDITOR);
            } 
            else if (button == saveButton) 
            {
                saveMenuVisible = true;
                updateButtons = true;
            }
            else if (button == prevPageButton && hasPreviousPage()) 
            {
                setCurrentPage(currentPage - 1);
            }
            else if (button == nextPageButton && hasNextPage()) {
                setCurrentPage(currentPage + 1);
            }
            
            clicked = true;
        }
        else clicked = false;
    }
    

    @Override
    public void keyPressed(KeyEvent e) {
        
        // cancel/goback
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (saveMenuVisible || confirmSaveVisible) {
                cancelSaveButton.postClick();
            } else {
                cancelButton.postClick();
            }
            return;
        }

        // confirme
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (saveMenuVisible || confirmSaveVisible) {
                confirmeSaveButton.postClick();
            }
            else MojamComponent.instance.handleAction(TitleMenu.SELECT_LEVEL_EDITOR);
        }
        
        // disable keys if save menu is visible
        if (saveMenuVisible || confirmSaveVisible) {
            return;
        }

        // start/toggle drawing
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            drawing = true;
        } else if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
            drawing = !drawing;
        }
        
        int x = ((pencilX + TILE_WIDTH / 2) - mapX) / TILE_WIDTH;
        int y = ((pencilY + TILE_HEIGHT / 2) - mapY) / TILE_HEIGHT;

        // move level with keys
        if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == Options.getAsInteger("key_left")) 
        {
        	if (mapX < MENU_WIDTH + 24 && x < LEVEL_WIDTH - 4) mapX += TILE_WIDTH;
        	else if (x > 0) pencilX -= 32;
        } 
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == Options.getAsInteger("key_right")) 
        {
        	if (-(mapX - MENU_WIDTH) < mapW - (MojamComponent.GAME_WIDTH - MENU_WIDTH) + TILE_HEIGHT && x > 3) mapX -= 32;
        	else if (x < LEVEL_WIDTH - 1) pencilX += TILE_WIDTH;
        } 
        else if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == Options.getAsInteger("key_up")) 
        {
            if (mapY < TILE_HEIGHT && y < LEVEL_HEIGHT - 4) mapY += TILE_HEIGHT;
            else if (y > 0) pencilY -= 32;
        } 
        else if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == Options.getAsInteger("key_down")) 
        {
            if (-mapY < mapH - MojamComponent.GAME_HEIGHT + TILE_HEIGHT && y > 3) mapY -= TILE_HEIGHT;
            else if (y < LEVEL_HEIGHT -1) pencilY += 32;
        } 
        else if(e.getKeyCode() == KeyEvent.VK_DELETE)
        {
        	draw(editableTiles[0], x, y);
    	}
        else if (e.getKeyCode() == Options.getAsInteger("key_use"))
        {
        	getCurrentTileButtons(x, y);
        }
        else if (e.getKeyCode() == KeyEvent.VK_TAB) { //tab to scroll through tiles
            int id = (selectedButton.getId() - (buttonsPerPage * currentPage));
            
            if (selectedButton.getId() + 1 == editableTiles.length) {
                setCurrentPage(0);
            } else if (id == buttonsPerPage - 1 && hasNextPage()) {
                setCurrentPage(currentPage + 1);
            } else if (selectedButton.getId() + 1 < editableTiles.length) {
                tileButtons[id + 1].postClick();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // stop drawing
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            drawing = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // read input for new level name
        if (saveMenuVisible) {
            if (e.getKeyChar() == KeyEvent.VK_BACK_SPACE && saveLevelName.length() > 0) {
                saveLevelName = saveLevelName.substring(0, saveLevelName.length() - 1);
            } else {
                if (e.getKeyChar() == KeyEvent.VK_ENTER) return;
                
                saveLevelName += e.getKeyChar();
            }
        }
    }
}
