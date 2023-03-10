package skywars;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import skywars.events.GameAsyncPlayerChatEvent;
import skywars.events.GameBlockBreakEvent;
import skywars.events.GameBlockPlaceEvent;
import skywars.events.GameDeathEvent;
import skywars.events.GamePlayerHitEvent;
import skywars.events.GamePlayerInteractEvent;
import skywars.events.GamePlayerJoinEvent;
import skywars.events.GamePlayerLeaveEvent;
import skywars.events.GamePlayerMoveEvent;
import skywars.events.GameWorldChangeEvent;
import skywars.interfaces.ChunkSave;
import skywars.legacy.ChunkSaveMaterialData;

public class SkyWars extends JavaPlugin
{
	public static String PREFIX = ChatColor.translateAlternateColorCodes('&', "&3[&5SkyWars&3]&r ");
	
	private ExecutorService arena_restore;
	private EventMethods event_methods;
	private FileManager files;
	private ChunkSave chunk_save;
	private World spawn;
	private boolean enabled;
	
	private Set<GamePlayer> players;
	private Set<Lobby> lobbys;
	private Set<Game> games;
	private Set<String> arenas;
	private Set<String> loading_arenas;
	
	@Override
	public void onEnable()
	{
		new SkyWarsCommand(this);
		
		initFields();
		createFiles();
		loadArenas();
	}
	
	private void loadArenas()
	{
		List<String> allowed_arenas = files.read("arenas.txt");
		Set<String> loaded_arenas = new HashSet<>();
		
		for(String arena_name : allowed_arenas)
		{
			String created_new_arena = PREFIX + "&eArena: &6" + arena_name + " &ewas not found: NEW ARENA CREATED!";
			String loaded_existing_arena = PREFIX + "&eArena: &6" + arena_name + " &ewas loaded!";
			
			new WorldCreator(arena_name).createWorld();
			
			if(!files.isDirCreated(arena_name, true))
				loaded_arenas.add(created_new_arena);
			else
				loaded_arenas.add(loaded_existing_arena);
		}
		
		for(String loaded_arena : loaded_arenas)
		{
			getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', loaded_arena));
		}
	}
	
	private void initFields()
	{
		arena_restore = Executors.newSingleThreadExecutor();
		event_methods = new EventMethods(this);
		files = new FileManager(this);
		spawn = getServer().getWorld("world");
		enabled = false;
		
		players = new HashSet<>();
		lobbys = new HashSet<>();
		games = new HashSet<>();
		arenas = new HashSet<>();
		loading_arenas = new HashSet<>();
		
		setChunkSaveMethod();
	}
	
	private void createFiles()
	{
		files.createFile("stats.txt", false);
		files.createFile("arenas.txt", false);
		files.createDir("arenas", false);
	}
	
	private void registerEvents()
	{
		PluginManager pm = getServer().getPluginManager();
		
		pm.registerEvents(new GamePlayerJoinEvent(this), this);
		pm.registerEvents(new GameWorldChangeEvent(this), this);
		pm.registerEvents(new GamePlayerLeaveEvent(this), this);
		pm.registerEvents(new GameDeathEvent(this), this);
		pm.registerEvents(new GameAsyncPlayerChatEvent(this), this);
		pm.registerEvents(new GamePlayerHitEvent(this), this);
		pm.registerEvents(new GameBlockBreakEvent(this), this);
		pm.registerEvents(new GameBlockPlaceEvent(this), this);
		pm.registerEvents(new GamePlayerMoveEvent(this), this);
		pm.registerEvents(new GamePlayerInteractEvent(this), this);
	}
	
	public void setChunkSaveMethod()
	{
		try
		{
			getServer().getClass().getMethod("createBlockData", Material.class);
			chunk_save = new ChunkSaveBlockData(this);
		}
		catch(SecurityException e)
		{
			e.printStackTrace();
		}
		catch(NoSuchMethodException e)
		{
			chunk_save = new ChunkSaveMaterialData(this);
		}
	}
	
	public ItemStack getCoordsSelectorItem()
	{
		ItemStack item = new ItemStack(Material.BEDROCK, 1);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Selector");
		item.setItemMeta(meta);
		return item;
	}
	
	public Iterator<GamePlayer> getGamePlayersIterator()
	{
		return players.iterator();
	}
	
	public Iterator<Lobby> getLobbyIterator()
	{
		return lobbys.iterator();
	}
	
	public Iterator<Game> getGameIterator()
	{
		return games.iterator();
	}
	
	public void addGame(Game new_game)
	{
		games.add(new_game);
	}
	
	public void addLobby(Lobby new_lobby)
	{
		lobbys.add(new_lobby);
	}
	
	public void addArenaToSet(String arena_name)
	{
		arenas.add(arena_name);
	}
	
	public EventMethods getEventMethods()
	{
		return event_methods;
	}
	
	public FileManager getFileManager()
	{
		return files;
	}
	
	public ExecutorService getArenaRestoreThreadpool()
	{
		return arena_restore;
	}
	
	public ChunkSave getChunkSave()
	{
		return chunk_save;
	}
	
	public void removeArenaFromQueue(String arena_name)
	{
		loading_arenas.remove(arena_name);
	}
	
	public String setMaxPlayers(String arena, int max)
	{
		List<String> allowed_arenas = files.read("arenas.txt");
		
		if(!allowed_arenas.contains(arena) || arenas.contains(arena))
			return "You are not allowed to set max players for the chosen arena name!";
		
		if(max < 1 || max > 16)
			return "Number out of bounds. Try again!";
		
		files.writeLine("arenas/" + arena + "/", arena + ".conf", "maximum_players", "" + max);
		return "Successfully updated max players for the chosen arena name!";
	}
	
	public String setMinPlayers(String arena, int min)
	{
		List<String> allowed_arenas = files.read("arenas.txt");
		
		if(!allowed_arenas.contains(arena) || arenas.contains(arena))
			return "You are not allowed to set min players for the chosen arena name!";
		
		files.writeLine("arenas/" + arena + "/", arena + ".conf", "minimum_players", "" + min);
		return "Successfully updated min players for the chosen arena name!";
	}
	
	public String setMovement(String arena, boolean movement)
	{
		List<String> allowed_arenas = files.read("arenas.txt");
		
		if(!allowed_arenas.contains(arena) || arenas.contains(arena))
			return "You are not allowed to set movement settings for the chosen arena";
		
		files.writeLine("arenas/" + arena + "/", arena + ".conf", "allow_movement", "" + movement);
		return "Movement settings set!";
	}
	
	public String setCentre(String arena, int chunk_x, int chunk_z)
	{
		List<String> allowed_arenas = files.read("arenas.txt");
		
		if(!allowed_arenas.contains(arena) || arenas.contains(arena))
			return "You are not allowed to set the centre for the chosen arena";
		
		files.writeLine("arenas/" + arena + "/", arena + ".conf", "centre", chunk_x + " " + chunk_z);
		return "Centre for this arena has been set!";
	}
	
	public void createArena(CommandSender sender, String arena)
	{
		sender.sendMessage(ChatColor.GREEN + "Working...");
		
		String[] world_names = {arena};
		new CreateWorldsTask(sender, world_names, "&bArena Loaded!").runTaskTimer(this, 0, 15);
		files.writeLine("", "arenas.txt", "", arena);
	}
	
	public void teleportPlayerToArena(Player player, String arena)
	{
		List<String> allowed_arenas = files.read("arenas.txt");
		
		if(!allowed_arenas.contains(arena))
		{
			player.sendMessage(ChatColor.RED + "The arena world that you are in is not allowed to be used! Add arena name to arenas.txt");
			return;
		}
		
		if(getServer().getWorld(arena) == null)
		{
			player.sendMessage(ChatColor.RED + "Could not find arena!");
			return;
		}
		
		player.teleport(getServer().getWorld(arena).getSpawnLocation());
		player.sendMessage(ChatColor.GREEN + "Successfully teleported!");
	}
	
	public String setLobbySpawn(String arena, Location location)
	{
		List<String> allowed_arenas = files.read("arenas.txt");
		
		if(!allowed_arenas.contains(arena) || arenas.contains(arena))
		{
			return "You are not allowed to set spawnpoints for this arena!";
		}
		
		String file_path = "arenas/" + arena + "/";
		String file_name = arena + ".conf";
		
		String lobby_spawn = files.readLine(file_path, file_name, "lobby_spawn");
		
		if(lobby_spawn.isEmpty())
		{
			String write_line = location.getX() + " " + location.getY() + " " + location.getZ();
			files.writeLine(file_path, file_name, "lobby_spawn", write_line);
			
			return "Successfully set lobby spawn for arena";
		}
		
		return "Value already set";
	}
	
	public String addSpawnpoint(String arena, Location location)
	{
		List<String> allowed_arenas = files.read("arenas.txt");
		
		if(!allowed_arenas.contains(arena) || arenas.contains(arena))
		{
			return "You are not allowed to set spawnpoints for this arena!";
		}
		
		String file_path = "arenas/" + arena + "/";
		String file_name = arena + ".conf";
		String[] sections = {"spawnpoints:"};
		
		List<String> spawnpoints = files.readSection(file_path, file_name, sections);
		
		String maximum_players = files.readLine(file_path, file_name, "maximum_players");
		
		if(!maximum_players.isEmpty())
		{
			try
			{
				int max_players = Integer.parseInt(maximum_players);
				
				if(spawnpoints.size() >= max_players)
					return "Maximum number of spawnpoints reached";
			}
			catch(NumberFormatException e) 
			{
				getServer().getConsoleSender().sendMessage("Failed to parse max players");
			}
		}
		else
			return "Maximum number of players was not found";
		
		String write_line = location.getX() + " " + location.getY() + " " + location.getZ();
		files.writeToSection("arenas/" + arena + "/", arena + ".conf", sections, new String[]{write_line});
		return "Successfully updated spawnpoint for the chosen arena!";
	}
	
	public boolean isPluginEnabled()
	{
		return enabled;
	}
	
	public void enablePlugin(CommandSender player)
	{
		if(enabled)
		{
			player.sendMessage(PREFIX + ChatColor.RED + "Plugin already enabled!");
			return;
		}
		
		registerEvents();
		setupGamePlayers();
		
		player.sendMessage(PREFIX + ChatColor.RED + "Plugin enabled!");
	}
	
	private void iterateXAndZ(World world, int y, int x_start, int x_end, int z_start, int z_end)
	{
		if(x_start <= x_end)
		{
			for(int x = x_start; x <= x_end; x++)
			{
				if(z_start <= z_end)
				{
					for(int z = z_start; z <= z_end; z++)
					{
						Block block = world.getBlockAt(x, y, z);
						
						if(!block.getType().name().equals("AIR"))
							block.setType(Material.AIR);
					}
				}
				else
				{
					for(int z = z_start; z >= z_end; z--)
					{
						Block block = world.getBlockAt(x, y, z);
						
						if(!block.getType().name().equals("AIR"))
							block.setType(Material.AIR);
					}
				}
			}
		}
		else
		{
			for(int x = x_start; x >= x_end; x--)
			{
				if(z_start <= z_end)
				{
					for(int z = z_start; z <= z_end; z++)
					{
						Block block = world.getBlockAt(x, y, z);
						
						if(!block.getType().name().equals("AIR"))
							block.setType(Material.AIR);
					}
				}
				else
				{
					for(int z = z_start; z >= z_end; z--)
					{
						Block block = world.getBlockAt(x, y, z);
						
						if(!block.getType().name().equals("AIR"))
							block.setType(Material.AIR);
					}
				}
			}
		}
	}
	
	public GamePlayer getGamePlayer(Player player)
	{
		for(GamePlayer gamep : players)
		{
			if(gamep.getPlayer().equals(player))
			{
				return gamep;
			}
		}
		
		return null;
	}
	
	public Game getGame(String game_name)
	{
		for(Game game : games)
		{
			if(game.getGameName().equals(game_name))
			{
				return game;
			}
		}
		
		return null;
	}
	
	public Lobby getLobby(String game_name)
	{
		for(Lobby lobby : lobbys)
		{
			if(lobby.getGameName().equals(game_name))
			{
				return lobby;
			}
		}
		
		return null;
	}
	
	public void setupGamePlayers()
	{
		enabled = true;
		
		for(Player player : getServer().getOnlinePlayers())
		{
			players.add(new GamePlayer(this, player.getName()));
		}
	}
	
	public String getFirstArena()
	{
		if(!arenas.isEmpty())
			return arenas.iterator().next();
		
		return "";
	}
	
	public void unloadArena(CommandSender sender, String arena)
	{
		if(arenas.remove(arena))
		{
			sender.sendMessage(ChatColor.GREEN + "Arena unloaded!");
			return;
		}
		
		sender.sendMessage(ChatColor.YELLOW + "Arena is not loaded!");
	}
	
	public void resetArea(World world, int x_start, int y_start, int z_start, int x_end, int y_end, int z_end)
	{
		if(y_start <= y_end)
		{
			for(int y = y_start; y <= y_end; y++)
			{
				iterateXAndZ(world, y, x_start, x_end, z_start, z_end);
			}
		}
		else
		{
			for(int y = y_start; y >= y_end; y--)
			{
				iterateXAndZ(world, y, x_start, x_end, z_start, z_end);
			}
		}
	}
	
	public void loadChunks(List<int[]> chunks, String world_name)
	{
		for(int[] chunk_coords : chunks)
		{
			Chunk chunk = getServer().getWorld(world_name).getChunkAt(chunk_coords[0], chunk_coords[1]);
			
			if(!chunk.isLoaded())
				chunk.load();
		}
	}
	
	public List<int[]> getArenaChunks(String world_name, int range)
	{
		Set<String> chunks = new HashSet<>();
		List<int[]> arena_chunks = new ArrayList<>();
		
		String arena_centre = files.readLine("arenas/" + world_name + "/", world_name + ".conf", "centre");
		String[] centre = arena_centre.split(" ");
		
		int main_chunk_x = Integer.parseInt(centre[0]);
		int main_chunk_z = Integer.parseInt(centre[1]);
		
		int chunk_x = main_chunk_x - range;
		int chunk_x_limit = main_chunk_x + range;
		
		while(chunk_x <= chunk_x_limit)
		{
			for(int chunk_z_loop = main_chunk_z - range; chunk_z_loop <= main_chunk_z + range; chunk_z_loop++)
			{
				arena_chunks.add(new int[]{chunk_x, chunk_z_loop});
				chunks.add(chunk_x + " " + chunk_z_loop);
			}
			
			chunk_x++;
		}
		
		int chunk_z = main_chunk_z - range;
		int chunk_z_limit = main_chunk_z + range;
		
		while(chunk_z <= chunk_z_limit)
		{
			for(int chunk_x_loop = main_chunk_x - range; chunk_x_loop <= main_chunk_x + range; chunk_x_loop++)
			{
				if(chunks.contains(chunk_x_loop + " " + chunk_z))
					continue;
				
				arena_chunks.add(new int[]{chunk_x_loop, chunk_z});
			}
			
			chunk_z++;
		}
		
		getServer().getConsoleSender().sendMessage("---------- Allocated: " + arena_chunks.size() + " chunks ----------");
		getServer().getConsoleSender().sendMessage("---------- Arena: " + world_name + " ----------");
		return arena_chunks;
	}
	
	public int getChunkCount(int range)
	{
		Set<String> chunks = new HashSet<>();
		int chunk_count = 0;
		
		int main_chunk_x = 0;
		int main_chunk_z = 0;
		
		int chunk_x = main_chunk_x - range;
		int chunk_x_limit = main_chunk_x + range;
		
		while(chunk_x <= chunk_x_limit)
		{
			for(int chunk_z_loop = main_chunk_z - range; chunk_z_loop <= main_chunk_z + range; chunk_z_loop++)
			{
				chunk_count++;
				chunks.add(chunk_x + " " + chunk_z_loop);
			}
			
			chunk_x++;
		}
		
		int chunk_z = main_chunk_z - range;
		int chunk_z_limit = main_chunk_z + range;
		
		while(chunk_z <= chunk_z_limit)
		{
			for(int chunk_x_loop = main_chunk_x - range; chunk_x_loop <= main_chunk_x + range; chunk_x_loop++)
			{
				if(chunks.contains(chunk_x_loop + " " + chunk_z))
					continue;
				
				chunk_count++;
			}
			
			chunk_z++;
		}
		
		return chunk_count;
	}
	
	public void loadArena(CommandSender sender, String arena)
	{	
		sender.sendMessage(ChatColor.GREEN + "Working...");
		
		if(loading_arenas.contains(arena))
		{
			sender.sendMessage(ChatColor.YELLOW + "Arena is currently loading!");
			return;
		}
		
		List<String> allowed_arenas = files.read("arenas.txt");
		
		if(arenas.contains(arena))
		{
			sender.sendMessage(ChatColor.RED + "Arena already loaded!");
			return;
		}
		
		if(!allowed_arenas.contains(arena))
		{
			sender.sendMessage(ChatColor.RED + "Arena has not been added to the arenas text file!");
			return;
		}
		
		sender.sendMessage(ChatColor.YELLOW + "Checking arena data...");
		
		if(!checkArenaData(arena))
		{
			sender.sendMessage(ChatColor.RED + "Arena data is invalid or does not exist");
			return;
		}
		
		if(!files.isFileCreated("arenas/" + arena + "/chunks", false))
		{
			sender.sendMessage(ChatColor.YELLOW + "Creating chunk data!");
			
			try
			{
				loading_arenas.add(arena);
				chunk_save.saveArenaChunks(sender, arena, 8);
				return;
			}
			catch(NumberFormatException e)
			{
				getServer().getConsoleSender().sendMessage("Failed to parse centre of arena");
			}
		}
		
		arenas.add(arena);
		sender.sendMessage(ChatColor.DARK_PURPLE + "Arena loaded!");
	}
	
	private boolean checkArenaData(String arena)
	{
		String file_path = "arenas/" + arena + "/";
		String file_name = arena + ".conf";
		String[] sections = {"spawnpoints:"};
		
		List<String> spawnpoints = files.readSection(file_path, file_name, sections);
		
		String minimum_players = files.readLine(file_path, file_name, "minimum_players");
		String maximum_players = files.readLine(file_path, file_name, "maximum_players");
		String allow_movement = files.readLine(file_path, file_name, "allow_movement");
		String arena_centre = files.readLine(file_path, file_name, "centre");
		String lobby_spawn = files.readLine(file_path, file_name, "lobby_spawn");
		
		if(minimum_players.isEmpty() ||
		maximum_players.isEmpty() ||
		allow_movement.isEmpty() ||
		arena_centre.isEmpty() ||
		lobby_spawn.isEmpty())
			return false;
		
		try
		{
			int max_players = Integer.parseInt(maximum_players);
			
			if(spawnpoints.size() != max_players)
				return false;
		}
		catch(NumberFormatException e)
		{
			getServer().getConsoleSender().sendMessage("Failed to parse max players");
		}
		
		return true;
	}
	
	public void removeGamePlayer(GamePlayer gamep)
	{
		try
		{
			players.remove(gamep);
		}
		catch(NullPointerException e) {}
	}
	
	public void removeGame(Game game)
	{
		try
		{
			games.remove(game);
		}
		catch(NullPointerException e) {}
	}
	
	public void removeLobby(Lobby lobby)
	{
		try
		{
			lobbys.remove(lobby);
		}
		catch(NullPointerException e) {}
	}
	
	private void sendMessageToGamePlayers(Player player, Player killer, String game_name, boolean force_quit)
	{
		if(force_quit == true)
		{
			player.sendMessage(ChatColor.RED + "You have left the game.");
			return;
		}
		
		if(killer != null)
			sendMessageToPlayers("game", game_name, player.getName() + " has been eliminated from the game by " + killer.getName());
		else
			sendMessageToPlayers("game", game_name, player.getName() + " has been eliminated from the game");
	}
	
	public void teleportPlayerToSpawn(Player player)
	{
		if(!player.getWorld().getName().equals(spawn.getName()))
			player.teleport(spawn.getSpawnLocation());
	}
	
	public void removePlayerFromList(GamePlayer gamep)
	{
		try
		{
			gamep.setLobbyStatus(false);
			attemptToCloseLobby(gamep.getGameName());
		}
		catch(NullPointerException e) {}
		
		removeGamePlayer(gamep);
	}
	
	public boolean removePlayerFromLobby(GamePlayer gamep, String game, boolean teleport)
	{
		try
		{
			Player player = gamep.getPlayer();
			boolean is_ingame = gamep.isPlayerInGame(game);
			
			if(gamep.getLobbyStatus() == true && (is_ingame || game == null))
			{
				gamep.setLobbyStatus(false);
				
				if(teleport)
					player.teleport(spawn.getSpawnLocation());
				
				player.sendMessage(ChatColor.GREEN + "You have left the lobby");
				
				sendMessageToPlayers("lobby", gamep.getGameName(), gamep.getPlayer().getName() + " has left the lobby.");
				attemptToCloseLobby(gamep.getGameName());
				
				gamep.setGameName(null);
				return true;
			}
		}
		catch(NullPointerException e) {}
		
		return false;
	}
	
	public boolean removePlayerFromGame(GamePlayer gamep, Player killer, String game_name, boolean force_quit, boolean teleport)
	{
		try
		{
			Player player = gamep.getPlayer();
			Game game = getGame(game_name);
			boolean is_ingame = gamep.isPlayerInGame(game_name);
			
			if(gamep.getGameStatus() == true && (is_ingame || game == null))
			{
				gamep.setGameStatus(false);
				
				try
				{
					if(game.getGamePlayerCount() <= 1)
						game.setHasStarted(false);
				}
				catch(NullPointerException e) {}
				
				if(teleport == true)
					player.teleport(spawn.getSpawnLocation());
				
				sendMessageToGamePlayers(player, killer, game_name, force_quit);
				gamep.setGameName(null);
				return true;
			}
		}
		catch(NullPointerException e) {}
		
		return false;
	}
	
	public void sendMessageToPlayers(String player_type, String game_name, String msg)
	{
		if(player_type.equalsIgnoreCase("lobby"))
		{
			for(GamePlayer gamep : players)
			{
				if(gamep.getLobbyStatus() == true && gamep.isPlayerInGame(game_name))
				{
					Player player = gamep.getPlayer();
					player.sendMessage(msg);
				}
			}
		}
		else if(player_type.equalsIgnoreCase("game"))
		{
			for(GamePlayer gamep : players)
			{
				if(gamep.getGameStatus() == true && gamep.isPlayerInGame(game_name))
				{
					Player player = gamep.getPlayer();
					player.sendMessage(msg);
				}
			}
		}
		else if(player_type.equalsIgnoreCase("all"))
		{
			for(GamePlayer gamep : players)
			{
				Player player = gamep.getPlayer();
				player.sendMessage(msg);
			}
		}
	}
	
	public void forceStartLobby(Player player)
	{
		GamePlayer gamep = getGamePlayer(player);
		
		try
		{
			Lobby lobby = getLobby(gamep.getGameName());
			
			if(gamep.getLobbyStatus() == false || gamep.getGameStatus() == true)
			{
				player.sendMessage(ChatColor.RED + "You have not joined a lobby!");
				return;
			}
			else if(lobby.hasStarted() != true)
			{
				player.sendMessage(ChatColor.DARK_GREEN + "Game force started");
				attemptToStartCountdown(gamep.getGameName(), player, true);
				return;
			}
		}
		catch(NullPointerException e) {}
	}
	
	public void attemptToStartCountdown(String game_name, Player player, boolean force_start)
	{
		Lobby lobby = getLobby(game_name);
		String min_players = files.readLine("arenas/" + game_name + "/", game_name + ".conf", "minimum_players");
		
		int min = -1;
		
		try
		{
			min = Integer.parseInt(min_players);
		}
		catch(NumberFormatException e)
		{
			player.sendMessage(ChatColor.RED + "Internal Error!");
			return;
		}
		
		try
		{
			if(!lobby.hasStarted())
			{
				if((lobby.getLobbyPlayerCount() >= min) || force_start == true)
				{
					lobby.startCountdown(game_name);
				}
			}
		}
		catch(NullPointerException e) {}
	}
	
	public void attemptToCloseLobby(String game_name)
	{
		if(game_name == null || game_name == "")
			return;
		
		Lobby lobby = getLobby(game_name);
		String min_players = files.readLine("arenas/" + game_name + "/", game_name + ".conf", "minimum_players");
		
		int min = -1;
		
		try
		{
			min = Integer.parseInt(min_players);
		}
		catch(NumberFormatException e)
		{
			getServer().getConsoleSender().sendMessage("Failed to parse minimum players");
			return;
		}
		
		if(lobby.getLobbyPlayerCount() > 0 && lobby.getLobbyPlayerCount() < min)
		{
			lobby.stopLobbyTimer();
			sendMessageToPlayers("lobby", game_name, "Game cancelled, not enough players!");
		}
		else if(lobby.getLobbyPlayerCount() < 1)
		{
			lobby.stopLobbyTimer();
			lobbys.remove(lobby);
		}
	}
	
	public void playerLeave(Player player)
	{
		GamePlayer gamep = getGamePlayer(player);
		
		if(removePlayerFromLobby(gamep, null, true))
			return;
		if(removePlayerFromGame(gamep, null, null, true, true))
			return;
		else
			player.sendMessage(ChatColor.RED + "You have not joined a lobby/game.");
	}
	
	public void playerJoin(Player player, String arena_name)
	{
		if(arena_name.isEmpty())
		{
			player.sendMessage(ChatColor.RED + "There are not any loaded arenas!");
			return;
		}
		
		if(!arenas.contains(arena_name))
		{
			player.sendMessage(ChatColor.RED + "Arena not loaded!");
			return;
		}
		
		if(isGameRunning(arena_name))
		{
			player.sendMessage(ChatColor.RED + "Game already started.");
			return;
		}
		
		GamePlayer gamep = getGamePlayer(player);
		Lobby lobby = createLobby(arena_name);
		
		if(gamep == null)
			players.add(new GamePlayer(this, player.getName()));
		else if(gamep.getLobbyStatus() == true && gamep.isPlayerInGame(arena_name))
		{
			player.sendMessage(ChatColor.RED + "You have already joined the lobby.");
			return;
		}
		else if(lobby.isLobbyFull())
		{
			player.sendMessage(ChatColor.RED + "Lobby full!");
			return;
		}
		
		removePlayerFromLobby(gamep, null, false);
		removePlayerFromGame(gamep, null, null, false, false);
		
		lobby.addPlayerToLobby(getGamePlayer(player));
		sendMessageToPlayers("lobby", arena_name, player.getName() + " has joined the game");
		attemptToStartCountdown(arena_name, player, false);
	}
	
	public boolean isGameRunning(String game_name)
	{
		Game game = getGame(game_name);
		
		if(game != null)
			return true;
		
		return false;
	}
	
	public Lobby createLobby(String arena_name)
	{
		Lobby lobby = getLobby(arena_name);
		
		if(lobby == null)
		{
			lobby = new Lobby(this, arena_name);
			lobbys.add(lobby);
			return lobby;
		}
		else
			return lobby;
	}
	
	public Game createGame(String arena_name, boolean movement)
	{
		Game game = getGame(arena_name);
		
		if(game == null)
		{
			game = new Game(this, getServer().getWorld(arena_name), arena_name, movement);
			games.add(game);
			return game;
		}
		else
			return game;
	}
	
	public Boolean getBooleanFromString(String boolean_value)
	{
		if(boolean_value.equalsIgnoreCase("true"))
			return true;
		else if(boolean_value.equalsIgnoreCase("false"))
			return false;
		
		return null;
	}
}