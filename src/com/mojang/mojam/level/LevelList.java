package com.mojang.mojam.level;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import com.mojang.mojam.MojamComponent;

public class LevelList {

	private static ArrayList<LevelInformation> levels;

	public static void createLevelList() {
		levels = new ArrayList<LevelInformation>();
		levels.add(new LevelInformation("Mojam", "/levels/level1.bmp",true));
		levels.add(new LevelInformation("AsymeTrical","/levels/AsymeTrical.bmp",true));
		levels.add(new LevelInformation("CataBOMB", "/levels/CataBOMB.bmp",true));
		levels.add(new LevelInformation("Siege","/levels/Siege.bmp",true));
		levels.add(new LevelInformation("TheMaze", "/levels/TheMaze.bmp",true));
		levels.add(new LevelInformation("Circular_Shapes", "/levels/Circular Shapes.bmp",true));
		levels.add(new LevelInformation("BlackHole", "/levels/BlackHole.bmp",true));
		levels.add(new LevelInformation("Railroads", "/levels/RailRoads.bmp",true));
		levels.add(new LevelInformation("DevMap", "/levels/DevMap.bmp",true));
		
		File levels = getBaseDir();
		if(!levels.exists()) levels.mkdirs();
		System.out.println("Looking for levels: "+levels.getPath());
		loadDir(levels);
	}
	
	public static LevelInformation get(int id){
		return levels.get(id);
	}
	
	public static int size(){
		return levels.size();
	}
	
	public static File getBaseDir(){
		return new File(MojamComponent.getMojamDir(), "levels");
	}
	
	public static void loadDir(File file){
		File[] children = file.listFiles();
	    if (children != null) {
	        for (File child : children) {
	            if(child.isDirectory()){
	            	loadDir(child);
	            	continue;
	            }
	            String fileName = child.getName();
	            String fname="";
	            String ext="";
	            int mid= fileName.lastIndexOf(".");
	            fname=fileName.substring(0,mid);
	            ext=fileName.substring(mid+1);
	            System.out.println("   Found level: "+fname+"."+ext);
	            if(ext.toLowerCase().equals("bmp")){
	        		levels.add(new LevelInformation("+ "+fname, child.getPath(),false));
	            }
	        }
	        System.out.println();// just pass at the next line for console display
	    }
	}

	public static ArrayList<LevelInformation> getLevels() {
		if (levels == null) {
			createLevelList();
		}
		return levels;
	}
	
	public static void resetLevels(){
		levels = null;
	}
	
	public static void updateLevels(){
		 LevelList.resetLevels();
	     levels = LevelList.getLevels();
	}
	
	public static boolean checkLevel(String name) {
    	if (name.contains("+ ")) name = name.replaceAll("\\+ ", "");
		boolean check = new File(LevelList.getBaseDir(), name.trim() + ".bmp").exists();
		return check;
	}
	
	public static boolean saveLevel(int width, int height, int[][] tiles, String name) {
    	name = name.trim();
    	
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, tiles[x][y]);
            }
        }

        try {
            File newLevel = new File(LevelList.getBaseDir(), name + ".bmp");
            newLevel.createNewFile();
            ImageIO.write(image, "BMP", newLevel);
            levels.add(new LevelInformation("+ "+name, newLevel.getPath(),false));
        } 
        catch (FileNotFoundException ex) {
            System.out.println("FileNotFoundException : " + ex);
            return true;
        } 
        catch (IOException ioe) {
            System.out.println("IOException : " + ioe);
            return false;
        }
        
        return true;
    }

	public static boolean deleteLevel(int id) {
		if (!levels.get(id).vanilla){
			File levelToDelete = new File(levels.get(id).getPath());
			
			boolean success = levelToDelete.delete();
			if (success) levels.remove(id);
			
			System.out.println("Delete level : "+levelToDelete.getPath() + " ////// " + (success ? "success" : "error") + "\n");
			return success;
		}
		return false;
	}
}
