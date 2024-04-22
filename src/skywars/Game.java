package skywars;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bukkit.ChatColor;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

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
	private boolean game_ended;
	private ArenaCache cached_arena_data;
	
	public Game(SkyWars plugin, World arena_in, String game_name_in)
	{
		pl = plugin;
		winner = null;
		delay_before_end = null;
		game_player_delay = null;
		arena = arena_in;
		game_name = game_name_in;
		postgame_delayseconds = 5;
		pregame_delaysecods = 3;
		pre_game = false;
		cached_arena_data = pl.getFileManager().getArenaCache(game_name);
		player_move_allow = cached_arena_data.isAllowedToMove();
		game_ended = false;
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
		
		Iterator<GamePlayer> gamep_it = pl.getGamePlayers().iterator();
		
		while(gamep_it.hasNext())
		{
			GamePlayer gamep = gamep_it.next();
			
			if(gamep.getPlayerState().equals(SkyWars.GameState.GAME) && gamep.isPlayerInGame(game_name))
				totalPlayers++;
		}
		
		return totalPlayers;
	}
	
	public void sendPlayerToGame(Player player, List<double[]> spawnpoints)
	{
		double[] spawnpoint = spawnpoints.remove(new Random().nextInt(spawnpoints.size()));
		
		try
		{
			double x,y,z;
			x = spawnpoint[0];
			y = spawnpoint[1];
			z = spawnpoint[2];
			
			teleportPlayerToGame(player, x, y, z);
		}
		catch(NumberFormatException e)
		{
			System.out.println("failed to parse spawnpoint");
		}
	}
	
	public void switchToGame()
	{
		addPlayersToGame();
		
		startGamePlayerDelay();
		pre_game = true;
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
		
		attemptToEndGame();
	}
	
	private void freePlayersFromCages(String game_name)
	{
		Iterator<GamePlayer> gamep_it = pl.getGamePlayers().iterator();
		
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
		List<double[]> spawnpoints_copy = new ArrayList<>(cached_arena_data.getSpawnpoints());
		Iterator<GamePlayer> gamep_it = pl.getGamePlayers().iterator();
		
		while(gamep_it.hasNext())
		{
			GamePlayer gamep = gamep_it.next();
			
			if(gamep.getPlayerState().equals(SkyWars.GameState.LOBBY) && gamep.isPlayerInGame(game_name))
			{
				sendPlayerToGame(gamep.getPlayer(), spawnpoints_copy);
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
	
	public void restoreArena()
	{
		cached_arena_data.updateChunksnaps();
		ExecutorService arena_restore = pl.getArenaRestoreThreadpool();
		
		arena_restore.execute(new Runnable()
		{
			@Override
			public void run()
			{
				ExecutorService threadpool = Executors.newSingleThreadExecutor();
				ChunkSave chunk_save = pl.getChunkSave();
				BlockRestore block_restore = chunk_save.newBlockRestoreTask(game_name, threadpool);
				
				Map<int[], String> chunks_ready = cached_arena_data.getUnzipedRestoreData();
				
				for(int[] key : chunks_ready.keySet())
				{
					String chunk = chunks_ready.get(key);
					ChunkSnapshot chunk_snap = cached_arena_data.getChunkSnap(key);
					threadpool.execute(chunk_save.newChunkRestoreTask(block_restore, chunk_snap, chunk));
				}
				
				threadpool.shutdown();
				block_restore.startTask(6);
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
		}, 0, 20);
	}
	
	private void setWinner()
	{
		Iterator<GamePlayer> gamep_it = pl.getGamePlayers().iterator();
		
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
		if(game_ended)
			return;
		
		int player_count = getGamePlayerCount();
		
		if(player_count == 1)
		{
			game_ended = true;
			
			setWinner();
			endGame();
		}
		else if(player_count < 1)
		{
			game_ended = true;
			
			restoreArena();
		}
	}
	
	public boolean isInPreGame()
	{
		return pre_game;
	}
}