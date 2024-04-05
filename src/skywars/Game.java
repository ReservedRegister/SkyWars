package skywars;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import skywars.others.GzipUtil;

public class Game
{
	private SkyWars pl;
	private GamePlayer winner;
	private BukkitTask delay_before_end;
	private BukkitTask game_player_delay;
	private World arena;
	private String game_name;
	private int postgame_delayseconds;
	private int pregame_delaysecods;
	private boolean pre_game;
	private boolean player_move_allow;
	
	public Game(SkyWars plugin, World arena_in, String game_name_in, boolean allow_movement_in)
	{
		pl = plugin;
		winner = null;
		delay_before_end = null;
		game_player_delay = null;
		arena = arena_in;
		game_name = game_name_in;
		postgame_delayseconds = 5;
		pregame_delaysecods = 3;
		player_move_allow = allow_movement_in;
		pre_game = false;
	}
	
	public World getArenaWorld()
	{
		return arena;
	}
	
	public String getGameName()
	{
		return game_name;
	}
	
	public boolean isAllowedToMove()
	{
		return player_move_allow;
	}
	
	public int getGamePlayerCount()
	{
		int totalPlayers = 0;
		
		Iterator<GamePlayer> gamep_it = pl.getGamePlayersIterator();
		
		while(gamep_it.hasNext())
		{
			GamePlayer gamep = gamep_it.next();
			
			if(gamep.getPlayerState().equals(SkyWars.GameState.GAME) && gamep.isPlayerInGame(game_name))
				totalPlayers++;
		}
		
		return totalPlayers;
	}
	
	public List<String> sendPlayerToGame(Player player, List<String> spawnpoints)
	{
		String[] split_spawnpoint = spawnpoints.remove(new Random().nextInt(spawnpoints.size())).split(" ");
		
		try
		{
			double x,y,z;
			x = Double.parseDouble(split_spawnpoint[0]);
			y = Double.parseDouble(split_spawnpoint[1]);
			z = Double.parseDouble(split_spawnpoint[2]);
			
			teleportPlayerToGame(player, x, y, z);
		}
		catch(NumberFormatException e)
		{
			System.out.println("failed to parse spawnpoint");
		}
		
		return spawnpoints;
	}
	
	public void createGame(Lobby lobby)
	{
		lobby.deleteLobbySelection();
		addPlayersToGame();
		
		startGamePlayerDelay();
		setPreGameStatus(true);
		pl.removeLobby(lobby);
	}
	
	private void startGamePlayerDelay()
	{
		game_player_delay = pl.getServer().getScheduler().runTaskTimer(pl, new Runnable() {
			@Override
			public void run()
			{
				if(pregame_delaysecods <= 0)
				{
					dispatchPlayers();
					game_player_delay.cancel();
					return;
				}
				
				pl.sendMessageToPlayers(SkyWars.GameState.GAME, game_name, ChatColor.RED + "Starting in " + ChatColor.BOLD + pregame_delaysecods);
				pregame_delaysecods--;
			}
		}, 0, 20);
	}
	
	public void dispatchPlayers()
	{
		player_move_allow = true;
		freePlayersFromCages(game_name);
		
		pre_game = false;
	}
	
	private void freePlayersFromCages(String game_name)
	{
		Iterator<GamePlayer> gamep_it = pl.getGamePlayersIterator();
		
		while(gamep_it.hasNext())
		{
			GamePlayer gamep = gamep_it.next();
			
			if(gamep.getPlayerState().equals(SkyWars.GameState.GAME) && gamep.isPlayerInGame(game_name))
			{
				Player player = gamep.getPlayer();
				Location location = player.getLocation();
				
				deleteCage(location.getBlockX(), location.getBlockY(), location.getBlockZ(), 3);
			}
		}
	}
	
	private void addPlayersToGame()
	{
		String[] sections = {"spawnpoints:"};
		List<String> spawnpoints = pl.getFileManager().readSection("arenas/" + game_name + "/", game_name + ".conf", sections);
		
		Iterator<GamePlayer> gamep_it = pl.getGamePlayersIterator();
		
		while(gamep_it.hasNext())
		{
			GamePlayer gamep = gamep_it.next();
			
			if(gamep.getPlayerState().equals(SkyWars.GameState.LOBBY) && gamep.isPlayerInGame(game_name))
			{
				spawnpoints = sendPlayerToGame(gamep.getPlayer(), spawnpoints);
				gamep.setPlayerState(SkyWars.GameState.GAME);
			}
		}
	}
	
	private void deleteCage(int x_start, int y_start, int z_start, int range)
	{
		pl.resetArea(arena, x_start, y_start, z_start, x_start + range, y_start + range, z_start + range);
		pl.resetArea(arena, x_start, y_start, z_start, x_start - range, y_start - range, z_start - range);
		pl.resetArea(arena, x_start, y_start, z_start, x_start - range, y_start + range, z_start + range);
		pl.resetArea(arena, x_start, y_start, z_start, x_start - range, y_start - range, z_start + range);
		pl.resetArea(arena, x_start, y_start, z_start, x_start + range, y_start - range, z_start - range);
		pl.resetArea(arena, x_start, y_start, z_start, x_start + range, y_start + range, z_start - range);
		pl.resetArea(arena, x_start, y_start, z_start, x_start + range, y_start - range, z_start + range);
		pl.resetArea(arena, x_start, y_start, z_start, x_start - range, y_start + range, z_start - range);
	}
	
	private void teleportPlayerToGame(Player player, double x, double y, double z)
	{
		player.teleport(new Location(pl.getServer().getWorld(game_name), x, y, z));
		player.sendMessage(ChatColor.GREEN + "Game Joined!");
	}
	
	public void endGame()
	{
		if(winner != null)
		{
			winner.setCoins(winner.getCoins() + 1);
			pl.sendMessageToPlayers(SkyWars.GameState.LOBBY, game_name, winner.getPlayer().getName() + " has won the game");
			pl.sendMessageToPlayers(SkyWars.GameState.GAME, game_name, winner.getPlayer().getName() + " has won the game");
			pl.getFileManager().writeLine("", "stats.txt", winner.getPlayer().getName(), ((Integer)winner.getCoins()).toString());
		}
		else
		{
			pl.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Failed to find winning player in game: " + game_name);
		}
		
		startDelayTask();
	}
	
	private Map<Integer, String> readChunkMaps()
	{
		Map<Integer, String> maps = new HashMap<>();
		
		for(String read_line : pl.getFileManager().read("arenas/" + game_name + "/maps"))
		{
			String[] split_line = read_line.split(" ");
			
			try
			{
				Integer block_map = Integer.parseInt(split_line[0]);
				String block = split_line[1];
				
				maps.put(block_map, block);
			}
			catch(NumberFormatException e)
			{
				System.out.println("failed to parse chunk maps");
			}
		}
		
		return maps;
	}
	
	public void restoreArena()
	{
		ExecutorService arena_restore = pl.getArenaRestoreThreadpool();
		
		arena_restore.execute(new Runnable()
		{
			@Override
			public void run()
			{
				ExecutorService threadpool = Executors.newSingleThreadExecutor();
				ChunkSave chunk_save = pl.getChunkSave();
				BlockRestore block_restore = chunk_save.newBlockRestoreTask(game_name, threadpool);
				Map<Integer, String> maps = readChunkMaps();
				
				try(FileInputStream stream = new FileInputStream(pl.getFileManager().getPluginFolder() + "arenas/" + game_name + "/chunks"))
				{
					String buffer = "";
					int character_byte = -1;
					
					while((character_byte = stream.read()) != -1)
					{
						String character = new String(new byte[]{(byte) character_byte});
						
						if(character.equals("]"))
						{
							String chunk_name = buffer.substring(0, buffer.indexOf(","));
							String size = buffer.substring(buffer.indexOf(",") + 1);
							
							String chunk_x = chunk_name.substring(0, chunk_name.indexOf("."));
							String chunk_z = chunk_name.substring(chunk_name.indexOf(".") + 1);
							
							try
							{
								byte[] chunk_data = new byte[Integer.parseInt(size)];
								stream.read(chunk_data);
								threadpool.execute(chunk_save.newChunkRestoreTask(block_restore, maps, GzipUtil.unzip(chunk_data), chunk_x + " " + chunk_z));
								buffer = "";
								continue;
							}
							catch(NumberFormatException e)
							{
								System.out.println(SkyWars.PREFIX + "failed to read byte stream");
							}
						}
						buffer += character;
					}
				}
				catch(FileNotFoundException e1)
				{
					e1.printStackTrace();
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
				
				threadpool.shutdown();
				block_restore.startTask(5);
			}
		});
	}
	
	public void startDelayTask()
	{
		delay_before_end = pl.getServer().getScheduler().runTaskTimer(pl, new Runnable()
		{
			@Override
			public void run()
			{
				try
				{	
					if(getGamePlayerCount() <= 0)
					{
						restoreArena();
						delay_before_end.cancel();
					}
					
					if(postgame_delayseconds == 0)
					{
						pl.removePlayerFromGame(winner);
						winner.teleportPlayerToSpawn();
						restoreArena();
						delay_before_end.cancel();
					}
					
					postgame_delayseconds--;
				}
				catch(NullPointerException e) {}
			}
		}, 0, 20);
	}
	
	private void setWinner()
	{
		Iterator<GamePlayer> gamep_it = pl.getGamePlayersIterator();
		
		while(gamep_it.hasNext())
		{
			GamePlayer gamep = gamep_it.next();
			
			if(gamep.getPlayerState().equals(SkyWars.GameState.GAME) && gamep.isPlayerInGame(game_name))
			{
				winner = gamep;
				return;
			}
		}
	}
	
	public void attemptToEndGame()
	{
		int player_count = getGamePlayerCount();
		
		if(player_count == 1)
		{
			setWinner();
			endGame();
		}
		else if(player_count < 1)
		{
			restoreArena();
		}
	}
	
	public boolean isInPreGame()
	{
		return pre_game;
	}
	
	public void setPreGameStatus(boolean new_value)
	{
		pre_game = new_value;
	}
}