package skywars;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
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

import skywars.events.EventMethods;
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
import skywars.legacy.ChunkSaveMaterialData;



public class SkyWars extends JavaPlugin
{
	public static String PREFIX = ChatColor.translateAlternateColorCodes('&', "&3[&5SkyWars&3]&r ");
	public static String SUCCESS = String.valueOf(ChatColor.GREEN);
	public static String ERROR = String.valueOf(ChatColor.RED);
	public enum GameState {INACTIVE, LOBBY, GAME};
	
	private ExecutorService arena_restore;
	private EventMethods event_methods;
	private ArenaFileManager files;
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
		files = new ArenaFileManager(this);
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
	
	public World getGlobalSpawn()
	{
		return spawn;
	}
	
	public Set<GamePlayer> getGamePlayers()
	{
		return players;
	}
	
	public Set<String> getLoadedArenas()
	{
		return arenas;
	}
	
	public Set<String> getLoadingArenas()
	{
		return loading_arenas;
	}
	
	public EventMethods getEventMethods()
	{
		return event_methods;
	}
	
	public ArenaFileManager getFileManager()
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
	
	public boolean isPluginEnabled()
	{
		return enabled;
	}
	
	public void createArena(String arena)
	{
		String[] world_names = {arena};
		new CreateWorldsTask(world_names).runTaskTimer(this, 0, 15);
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
	
	public boolean enablePlugin()
	{
		if(enabled)
		{
			return false;
		}
		
		registerEvents();
		setupGamePlayers();
		
		return true;
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
	
	public GamePlayer getOrCreateGamePlayer(Player player)
	{
		for(GamePlayer gamep : players)
		{
			if(gamep.getPlayer().equals(player))
			{
				return gamep;
			}
		}
		
		GamePlayer new_gamep = new GamePlayer(this, player.getName());
		players.add(new_gamep);
		return new_gamep;
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
		if(arenas.isEmpty())
		{
			return null;
		}
		
		return arenas.iterator().next();
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
		
		if(!files.loadArenaData(arena))
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
	
	public void removeGamePlayer(GamePlayer gamep)
	{
		if(gamep != null)
		{
			players.remove(gamep);
		}
	}
	
	public void removeGame(Game game)
	{
		if(game != null)
		{
			games.remove(game);
		}
	}
	
	public void removeLobby(Lobby lobby)
	{
		if(lobby != null)
		{
			lobbys.remove(lobby);
		}
	}
	
	public void managePlayerQuit(GamePlayer gamep)
	{
		if(gamep == null)
			return;
		
		if(gamep.getPlayerState().equals(SkyWars.GameState.LOBBY))
		{
			Lobby player_lobby = getLobby(gamep.getGameName());
			player_lobby.attemptToCloseLobby();
		}
		else if(gamep.getPlayerState().equals(SkyWars.GameState.GAME))
		{
			Game player_game = getGame(gamep.getGameName());
			player_game.attemptToEndGame();
			
		}
	}
	
	public void removePlayerFromList(GamePlayer gamep)
	{
		managePlayerQuit(gamep);
		removeGamePlayer(gamep);
	}
	
	public boolean removePlayerFromGame(GamePlayer gamep)
	{
		if(gamep != null)
		{
			if(!gamep.getPlayerState().equals(SkyWars.GameState.INACTIVE))
			{
				managePlayerQuit(gamep);
				
				gamep.setPlayerState(GameState.INACTIVE);
				gamep.setGameName(null);
				
				return true;
			}
		}
		
		return false;
	}
	
	public void sendMessageToPlayers(SkyWars.GameState player_state, String game_name, String msg)
	{
		if(player_state.equals(SkyWars.GameState.LOBBY))
		{
			for(GamePlayer gamep : players)
			{
				if(gamep.getPlayerState().equals(SkyWars.GameState.LOBBY) && gamep.isPlayerInGame(game_name))
				{
					Player player = gamep.getPlayer();
					player.sendMessage(msg);
				}
			}
		}
		else if(player_state.equals(SkyWars.GameState.GAME))
		{
			for(GamePlayer gamep : players)
			{
				if(gamep.getPlayerState().equals(SkyWars.GameState.GAME) && gamep.isPlayerInGame(game_name))
				{
					Player player = gamep.getPlayer();
					player.sendMessage(msg);
				}
			}
		}
		else if(player_state.equals(SkyWars.GameState.INACTIVE))
		{
			for(GamePlayer gamep : players)
			{
				if(gamep.getPlayerState().equals(SkyWars.GameState.INACTIVE))
				{
					Player player = gamep.getPlayer();
					player.sendMessage(msg);
				}
			}
		}
	}
	
	public void forceStartLobby(Player player)
	{
		GamePlayer gamep = getGamePlayer(player);
		
		try
		{
			Lobby lobby = getLobby(gamep.getGameName());
			
			if(!gamep.getPlayerState().equals(GameState.LOBBY))
			{
				player.sendMessage(ChatColor.RED + "You have not joined a lobby!");
				return;
			}
			else if(!lobby.isRunning())
			{
				player.sendMessage(ChatColor.DARK_GREEN + "Game force started");
				lobby.attemptToStartCountdown(true);
				return;
			}
		}
		catch(NullPointerException e)
		{
			getServer().getConsoleSender().sendMessage(ChatColor.RED + "Failed to force lobby!");
		}
	}
	
	public void playerLeave(Player player)
	{
		GamePlayer gamep = getGamePlayer(player);
		
		if(gamep != null)
		{
			String game_before_leave = gamep.getGameName();
			GameState player_state_before_leave = gamep.getPlayerState();
			boolean left = removePlayerFromGame(gamep);
			
			if(left && player_state_before_leave.equals(GameState.LOBBY))
			{
				player.sendMessage(ChatColor.RED + "You have left the lobby.");
				sendMessageToPlayers(GameState.GAME, game_before_leave, player.getName() + " has left the lobby");
			}
			else if(left && player_state_before_leave.equals(GameState.GAME))
			{
				player.sendMessage(ChatColor.RED + "You have left the game!");
				sendMessageToPlayers(GameState.GAME, game_before_leave, player.getName() + " has quit the game");
			}
			else
			{
				player.sendMessage(ChatColor.RED + "You have not joined a game.");
			}
		}
	}
	
	public void playerJoin(Player player, String arena_name)
	{
		if(arenas.isEmpty())
		{
			player.sendMessage(ChatColor.RED + "There are no loaded arenas!");
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
		
		GamePlayer gamep = getOrCreateGamePlayer(player);
		Lobby lobby = getOrCreateLobby(arena_name);
		
		if(gamep.getPlayerState().equals(GameState.LOBBY) && gamep.isPlayerInGame(arena_name))
		{
			player.sendMessage(ChatColor.RED + "You have already joined the lobby.");
			return;
		}
		else if(lobby.isLobbyFull())
		{
			player.sendMessage(ChatColor.RED + "Lobby full!");
			return;
		}
		
		removePlayerFromGame(gamep);
		lobby.addPlayerToLobby(gamep);
	}
	
	public boolean isGameRunning(String game_name)
	{
		Game game = getGame(game_name);
		
		if(game != null)
			return true;
		
		return false;
	}
	
	public Lobby getOrCreateLobby(String arena_name)
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
	
	public Game createGame(String arena_name)
	{
		Game game = getGame(arena_name);
		
		if(game == null)
		{
			game = new Game(this, getServer().getWorld(arena_name), arena_name);
			games.add(game);
			return game;
		}
		else
			return game;
	}
}