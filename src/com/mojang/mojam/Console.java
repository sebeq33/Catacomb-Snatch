package com.mojang.mojam;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

import com.mojang.mojam.entity.Entity;
import com.mojang.mojam.entity.Player;
import com.mojang.mojam.entity.building.SpawnerEntity;
import com.mojang.mojam.entity.mob.HostileMob;
import com.mojang.mojam.entity.weapon.Cannon;
import com.mojang.mojam.entity.weapon.ElephantGun;
import com.mojang.mojam.entity.weapon.Flamethrower;
import com.mojang.mojam.entity.weapon.Machete;
import com.mojang.mojam.entity.weapon.Melee;
import com.mojang.mojam.entity.weapon.Raygun;
import com.mojang.mojam.entity.weapon.Rifle;
import com.mojang.mojam.entity.weapon.Shotgun;
import com.mojang.mojam.entity.weapon.VenomShooter;
import com.mojang.mojam.gui.TitleMenu;
import com.mojang.mojam.gui.components.Font;
import com.mojang.mojam.level.Level;
import com.mojang.mojam.level.LevelInformation;
import com.mojang.mojam.level.LevelList;
import com.mojang.mojam.network.packet.ChatCommand;
import com.mojang.mojam.screen.Screen;

public class Console implements KeyListener {

	/***
	 * Maximum amount of verbose data kept in the console
	 * also the number of lines of data displayed
	 */
	public static final int MAX_LINES = 20;

	/***
	 * Maximum number of characters allowed to input into the console
	 */
	public static final int MAX_INPUT_LENGTH = 60;

	private ArrayList<String> verboseData = new ArrayList<String>(MAX_LINES);

	private String typing = "";
	private String lastTyping = "";
	private String input = null;
	private boolean completedInput;

	private boolean open;

	/***
	 * Left padding size when drawing console text
	 */
	public static final int xOffset = 5;

	/***
	 * Top padding size when drawing console text. affects console height
	 */
	public static final int yOffset = 5;

	public Console()
	{
		log("------------------------------------------------------------");//Deep magic lining it up
		log("|Catacomb Snatch Console v1.1                           |");
		log("|Type commands with a slash in front, like /this        |");
		log("|If in doubt, type /help                                  |");
		log("------------------------------------------------------------");
		log("");
	}


	/***
	 * Logs the verbose info into the console
	 * 
	 * @param s information to display in console
	 */
	public void log(String s) {
		if(s == null) return;

		if(verboseData.size() + 1 > MAX_LINES)
			verboseData.remove(verboseData.size() - 1);

		verboseData.add(0,s);
	}

	/***
	 * Closes the console and cancels current input
	 */
	public void close() {
		typing = "";
		input = null;
		completedInput = false;
		open = false;
	}

	/***
	 * Opens the console
	 */
	public void open() {
		open = true;
	}

	/***
	 * Toggles between open and close.
	 */
	public void toggle() {
		if(open)
			close();
		else
			open();
	}

	/***
	 * Tells if the console is open or not
	 * @return the answer
	 */
	public boolean isOpen() {
		return open;
	}

	/***
	 * renders the console on the screen if it is open
	 * screen space it takes up is (MAX_LINES+1) * Font.FONT_WHITE_SMALL + yOffset
	 * 
	 * @param s screen to render to
	 */
	public void render(Screen s) {
		if(open) {
			int fontHeight = Font.FONT_WHITE_SMALL.getFontHeight();
			int consoleHeight = (MAX_LINES + 1) * fontHeight + yOffset; //+1 for the input line

			s.alphaFill(0, 0, s.w, consoleHeight, 0xff000000, 0x80); //50% black,fixed from 0x50 (31.25%)

			Font.FONT_WHITE_SMALL.draw(s, typing + (((((int)(System.currentTimeMillis()/500))&1)==1)?"|":""), xOffset,(consoleHeight -= fontHeight)); //draws bottom up starting with typing

			for(int i = 0; i < verboseData.size(); i++) {
				Font.FONT_WHITE_SMALL.draw(s, verboseData.get(i), xOffset, (consoleHeight -= fontHeight) ); // and then the verbose data in order of newest first
			}
		}
	}

	/***
	 * checks if the user has inputed anything
	 * unnecessary if the console is closed
	 */
	public void tick() {
		if(completedInput) {
			processInput(input);
		}
	}

	private void processInput(String input) {
		log(">" + input);
		String command = getCommand(input);

		if(command.startsWith("/")) {
			doCommand(command, input);
		} else {
			chat.doCommand(new String[]{input});
		}

		completedInput = false;
	}

	private String getCommand(String input) {
		if(!input.contains(" ")) {
			return input;
		} else {
			return input.substring(0, input.indexOf(' '));
		}
	}

	/***
	 * Execute a console command
	 * if no command has that name nothing will be done
	 * 
	 * @param command command name
	 * @param input arguments for the command separated by spaces
	 */
	public void doCommand(String command, String input) {
		if(command.charAt(0) == '/')
			command = command.substring(1); //remove forward slash

		for(Command c : Command.commands) {

			if(c != null && c.name.equals(command)) {

				String[] args = getArgs(input,c.numberOfArgs);
				c.doCommand(args);
				return;
			}
		}
		
		log("command not found");
	}

	private String[] getArgs(String input, int numberOfArgs) {
		if(numberOfArgs == -1) { //see Command NumberOfArgs for reason
			if(!input.contains(" ")) {
				return new String[]{""};
			} else {
				return new String[]{removeCommand(input)};
			}
		}

		if(numberOfArgs <= 0) return null;

		String[] args = new String[numberOfArgs];
		input = removeCommand(input);
		if(numberOfArgs == 1) return new String[]{input};

		for(int i = 0; i < numberOfArgs; i++) {
			int index = input.indexOf(' ');

			if(index > 0) {
				args[i] = input.substring(0, index);
				input = input.substring(index+1);
			}
		}
		return args;
	}

	private String removeCommand(String input) {
		if(input.charAt(0) != '/') {
			return input;
		}
		if(!input.contains(" ")) {
			return input;
		}
		return input.substring(input.indexOf(' ') + 1);
	}

	public void keyTyped(KeyEvent e) {
		
	}

	public void keyPressed(KeyEvent e) {
		if(open) {
			switch(e.getKeyCode()) {
			case KeyEvent.VK_ESCAPE:
				//close();//handled by keys in MojamComponent
				break;
			case KeyEvent.VK_ENTER:
				typing = typing.trim();
				if(!typing.equals("")) {
					input = typing;
					lastTyping = typing;
					completedInput = true;
				}
				typing = "";
				break;
			case KeyEvent.VK_UP:
				typing = lastTyping;
				break;
			case KeyEvent.VK_BACK_SPACE:
				if(typing.length() > 0)
					typing = typing.substring(0, typing.length()-1);
				break;
			default:
				if(typing.length() < MAX_INPUT_LENGTH)
					typing += e.getKeyChar();	
				break;
			}
		}
	}

	public void keyReleased(KeyEvent e) {
		
	}

	/***
	 * List of possible commands
	 */
	@SuppressWarnings("unused")
	private Command help = new Command("help", 0, "Displays all possible commands") {
		public void doCommand(String[] args) {
			log("All Commands");
			log("--------------");
			for(int i = 0; i < Command.commands.size(); i++) {
				Command c = Command.commands.get(i);
				if(c != null)
					log("/"+c.name + " : " + c.helpMessage);
			}
		}
	};

	public Command pause = new Command("pause", 0, "Pauses the game") {
		public void doCommand(String[] args) {
			close();
			MojamComponent.instance.synchronizer.addCommand(new com.mojang.mojam.network.PauseCommand(true));
		}
	};

	
	public Command exit = new Command("exit", 1, "exits the game. 0 force exit, 1 regular game exit") {
		public void doCommand(String[] args) {
			if(args.length > 0 && args[0].equals("0"))
				System.exit(0);
			else
				MojamComponent.instance.stop(false);
		}
	};

	public Command chat = new Command("chat", -1, "Does the same as pressing T and typing in the after /chat and pressing enter") {
		public void doCommand(String[] args) {
			String msg = "";
			for(int i = 0; i < args.length-1; i++) {
				msg += args[i] + " ";
			}
			msg += args[args.length-1];
			MojamComponent.instance.synchronizer.addCommand(new ChatCommand(msg));
		}
	};

	public Command load = new Command("load", 1, "Loads a map by name")
	{
		@Override
		public void doCommand(String[] args)
		{
			if (args[0].equals("list") || args[0].equals("help"))
			{
				for (LevelInformation level : LevelList.getLevels()) {
					if (level.vanilla) log("--> " +level.levelName.toLowerCase());
					else log("--> " +level.levelName.replaceAll("\\+ ", "").toLowerCase());
				}
			}
			else
			{
				for (LevelInformation level : LevelList.getLevels()) {
					if ((level.vanilla && level.levelName.toLowerCase().equals(args[0])) || (!level.vanilla && level.levelName.toLowerCase().equals(("+ " +args[0]))))
					{
						TitleMenu.level = level;
						MojamComponent.instance.handleAction(TitleMenu.START_GAME_ID);
						log("Loading map " + args[0]);
						close();
						return;
					}
				}
				
				log("Map not found");
			}
		}
	};
	
	public Command lang = new Command("lang", 1, "Sets the language")
	{
		@Override
		public void doCommand(String[] args)
		{
			if(args[0].equals("help"))
			{
				log("Enter your two letter language code, e.g. /lang af -> Afrikaans, /lang it -> Italiano");
			}				
			else 
			{
				MojamComponent.instance.setLocale(args[0]);
			}
		}
	};
	
	public Command menu = new Command("menu", 0, "Return to menu")
	{
		@Override
		public void doCommand(String[] args)
		{
			MojamComponent.instance.handleAction(TitleMenu.RETURN_TO_TITLESCREEN);
		}
	};

	public Command allweapons = new Command("allweapons", 1, "Gives all weapons")
	{
		@Override
		public void doCommand(String[] args)
		{
			Player player = MojamComponent.instance.player;
		
			log("Giving player a shotgun");
			if(!player.weaponInventory.add(new Shotgun(player))) {
	        	log("You already have this item.");
	    	}
	
			log("Giving player a rifle");
			if(!player.weaponInventory.add(new Rifle(player))) {
	        	log("You already have this item.");
	    	}
	
			log("Giving player a veonomshooter");
			if(!player.weaponInventory.add(new VenomShooter(player))) {
	        	log("You already have this item.");
	    	}
	
			log("Giving player an elephant gun");
			if(!player.weaponInventory.add(new ElephantGun(player))) {
	        	log("You already have this item.");
	    	}
	
			log("Giving player a lesson in boxing");
			if(!player.weaponInventory.add(new Melee(player))) {
	        	log("You already have this item.");
	    	}
	
			log("Giving player a raygun");
			if(!player.weaponInventory.add(new Raygun(player))) {
	        	log("You already have this item.");
	    	}
	
			log("Giving player a machete");
			if(!player.weaponInventory.add(new Machete(player))) {
	        	log("You already have this item.");
	    	}
	
			log("Giving player a cannon!");
			if(!player.weaponInventory.add(new Cannon(player))) {
	        	log("You already have this item.");
	    	}
			
			log("Giving player a flamethrower!");
			if(!player.weaponInventory.add(new Flamethrower(player))) {
	        	log("You already have this item.");
	    	}
		}
	};

	
	public Command give = new Command("give", 1, "Gives a weapon")
	{
		@Override
		public void doCommand(String[] args)
		{
			args[0] = args[0].trim().toLowerCase();
			Player player = MojamComponent.instance.player;
			
			if(args[0].equals("shotgun"))
			{
				log("Giving player a shotgun");
				if(!player.weaponInventory.add(new Shotgun(player))) {
		        	log("You already have this item.");
		    	}
			}
			else if(args[0].equals("rifle"))
			{
				log("Giving player a rifle");
				if(!player.weaponInventory.add(new Rifle(player))) {
		        	log("You already have this item.");
		    	}
			}
			else if(args[0].equals("venom"))
			{
				log("Giving player a veonomshooter");
				if(!player.weaponInventory.add(new VenomShooter(player))) {
		        	log("You already have this item.");
		    	}
			}
			else if(args[0].equals("elephant"))
			{
				log("Giving player an elephant gun");
				if(!player.weaponInventory.add(new ElephantGun(player))) {
		        	log("You already have this item.");
		    	}
			}
			else if(args[0].equals("fist"))
			{
				log("Giving player a lesson in boxing");
				if(!player.weaponInventory.add(new Melee(player))) {
		        	log("You already have this item.");
		    	}
			}
			else if(args[0].equals("raygun"))
			{
				log("Giving player a raygun");
				if(!player.weaponInventory.add(new Raygun(player))) {
		        	log("You already have this item.");
		    	}
			}
			else if(args[0].equals("machete"))
			{
				log("Giving player a machete");
				if(!player.weaponInventory.add(new Machete(player))) {
		        	log("You already have this item.");
		    	}
			}
			else if(args[0].equals("cannon"))
			{
				log("Giving player a cannon!");
				if(!player.weaponInventory.add(new Cannon(player))) {
		        	log("You already have this item.");
		    	}
			}
			else if(args[0].equals("flamethrower"))
			{
				log("Giving player a flamethrower!");
				if(!player.weaponInventory.add(new Flamethrower(player))) {
		        	log("You already have this item.");
		    	}
			}
			else if(args[0].equals("help"))
			{
				log("Options:");
				log(">rifle (Rifle)");
				log(">shotgun (Shotgun)");
				log(">venom (VenomShooter)");
				log(">elephant (Elephant Gun)");
				log(">fist (Melee)");
				log(">raygun (Raygun)");
				log(">machete (Machete)");
				log(">cannon (Cannon)");
				log("Or you can use a numerical value to receive money.");
			}
			try{
				player.score+=Integer.parseInt(args[0]);
			}catch (NumberFormatException e)
			{

			}
		}
	};
	
	public Command time = new Command("time", 0, "Show the current time"){
		@Override
		public void doCommand(String[] s)
		{
			log(new Date(System.currentTimeMillis()).toString());
		}
	};
	
	public Command cooldown = new Command("cool", 1, "Cools the currently held weapon to a certain value"){
		@Override
		public void doCommand(String[] s)
		{
			try	{
				int i = Integer.parseInt(s[0].trim());
				log("Cooling weapon from " + i + " centispecks.");
				for(;i>0;i--)
				{
					MojamComponent.instance.player.weapon.weapontick();
				}
			} catch (NumberFormatException e)
			{
				log("Cooling weapon");
				int i = 600;
				for(;i>0;i--)
				{
					MojamComponent.instance.player.weapon.weapontick();
				}
			}
		}
	};
	
	public Command SetSpawn = new Command("setspawn", 1, "enable/disable spawn of mobs. /setspawn help"){
		@Override
		public void doCommand(String[] args)
		{
			
			if(args[0].equals("help"))
			{
				log("0 => toggle allow/block hostile mob spawning (current : "+ MojamComponent.instance.level.spawn + ")");
				log("1 => toggle allow/block mobSpawner spawning (current : "+ MojamComponent.instance.level.spawnSpawner + ")");
				log("reset => set both spawn parameter to default ");
			}
			else if (args[0].equals("0"))
			{
				MojamComponent.instance.level.spawn = !MojamComponent.instance.level.spawn;
				log("spawnMob : "+ MojamComponent.instance.level.spawn);
			}
			else if (args[0].equals("1"))
			{
				MojamComponent.instance.level.spawnSpawner = !MojamComponent.instance.level.spawnSpawner;
				log("spawnSpawner : "+ MojamComponent.instance.level.spawnSpawner);
			}
			else if (args[0].equals("reset"))
			{
				MojamComponent.instance.level.spawnSpawner = true;
				MojamComponent.instance.level.spawn = true;
			}
			else {	
				log("ERROR. invalid args");
			}
		}
	};
	
	public Command killEntity = new Command("killentity", 1, "kill all entity of a type"){
		@Override
		public void doCommand(String[] args)
		{
			if(args[0].equals("help"))
			{
				log("/killentity par1");
				log("mob => all hostile mob");
				log("spawner => all mob spawner");
				log("both => all mob & spawner");
				log("all => all entity");
			}
			else 			
			{
				Level level = MojamComponent.instance.level;
				Set<Entity> entities = null;
				
				if (args[0].equals("mob")) entities = level.getAllEntities(HostileMob.class);
				else if (args[0].equals("spawner")) entities = level.getAllEntities(SpawnerEntity.class);
				else if (args[0].equals("both"))
				{
					entities = level.getAllEntities(HostileMob.class);
					if (entities == null) entities = level.getAllEntities(SpawnerEntity.class);
					else entities.addAll(level.getAllEntities(SpawnerEntity.class));
				}
				else if (args[0].equals("all"))
				{
					entities = level.getAllEntities(null);
				}
				
				if (entities != null && entities.size() > 0)
				{
					for (Entity entity : entities) {
						if (!(entity instanceof Player)) entity.remove(); 
					}
					log("SUCCES.");
				}
				else
				{
					log("ERROR. invalid args");
				}
			}
		}
	};
	
	public Command giveMoney = new Command("givemoney", 1, "give the selected amount (integer) to the player"){
		@Override
		public void doCommand(String[] args)
		{
			try{
				int amount = Integer.valueOf(args[0]);
				MojamComponent.instance.player.addScore(amount);
			}
			catch(Exception e)
			{
				log("ERROR. invalid args");
			}
		}
	};
	
	public Command reveal = new Command("reveal", 0, "reveal all the map (radius = 100)"){
		@Override
		public void doCommand(String[] args)
		{
			Level level = MojamComponent.instance.level;
			if (level == null) return;
			level.reveal(level.width / 2, level.height / 2, 100, true);
		}
	};
	
	public Command creative = new Command("creative", 0, "toggle on/off creative mode of the player"){
		@Override
		public void doCommand(String[] args)
		{
			Player p = MojamComponent.instance.player;
			p.creative = !p.creative;
			p.setRailPricesAndImmortality();
			p.updateWeapons();
		}
	};

	public abstract static class Command {

		public String name;
		public String helpMessage;
		public int numberOfArgs; //-1 args means return raw input data minus the command
		public static ArrayList<Command> commands = new ArrayList<Command>();

		public Command(String name, int numberOfArgs, String helpMessage) {
			this.name = name;
			this.numberOfArgs = numberOfArgs;
			this.helpMessage = helpMessage;
			commands.add(this);
		}

		public abstract void doCommand(String[] args);
	}

}