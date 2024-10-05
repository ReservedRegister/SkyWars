package skywars;

import java.util.Iterator;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import skywars.SkyWars.GameState;

public class Lobby
{
	private SkyWars pl;
	private BukkitTask lobby_timer;
	private Location lobby_spawn;
	private String game_name;
	private int seconds;
	private boolean isRunning;
	private ArenaCache cached_arena_data;
	
	public Lobby(SkyWars plugin, String game_name_in)
	{
		pl = plugin;
		lobby_timer = null;
		game_name = game_name_in;
		seconds = 10;
		isRunning = false;
		cached_arena_data = pl.getFileManager().getCachedArenas().get(game_name);
		
		setLobbySpawn();
	}
	
	public boolean isRunning()
	{
		return isRunning;
	}
	
	public String getGameName()
	{
		return game_name;
	}
	
	public void setGameName(String new_game_name)
	{
		game_name = new_game_name;
	}
	
	public void setRunStatus(boolean isRunning_in)
	{
		isRunning = isRunning_in;
	}
	
	public int getLobbyPlayerCount()
	{
		int totalPlayers = 0;
		
		Iterator<GamePlayer> gamep_it = pl.getGamePlayers().iterator();
		
		while(gamep_it.hasNext())
		{
			GamePlayer gamep = gamep_it.next();
			
			if(gamep.getPlayerState().equals(SkyWars.GameState.LOBBY) && gamep.isPlayerInGame(game_name))
			{
				totalPlayers++;
			}
		}
		
		return totalPlayers;
	}
	
	public boolean isLobbyFull()
	{
		int max = cached_arena_data.getMaximumPlayers();
		
		if(getLobbyPlayerCount() >= max)
			return true;
		
		return false;
	}
	
	private void createGame()
	{
		Game game = pl.createGame(game_name);
		
		deleteLobbySelection();
		game.switchToGame();
		
		pl.getLobbys().remove(this);
	}
	
	public void stopLobbyTimer()
	{
		if(lobby_timer != null)
		{
			lobby_timer.cancel();
			lobby_timer = null;
			
			seconds = 10;
			isRunning = false;
		}
	}
	
	public void startCountdown()
	{
		lobby_timer = pl.getServer().getScheduler().runTaskTimer(pl, new Runnable()
		{
			@Override
			public void run()
			{
				if(seconds == 0)
				{
					stopLobbyTimer();
					createGame();
					return;
				}
				
				pl.sendMessageToPlayers(SkyWars.GameState.LOBBY, game_name, ChatColor.YELLOW + "Seconds left: " + seconds);
				seconds--;
			}
		}, 0, 20);
		
		isRunning = true;
	}
	
	public void addPlayerToLobby(GamePlayer gamep)
	{
		if(gamep != null)
		{
			teleportPlayerToLobby(gamep);
			
			gamep.setPlayerState(SkyWars.GameState.LOBBY);
			gamep.setGameName(game_name);
			
			pl.sendMessageToPlayers(GameState.LOBBY, game_name, gamep.getPlayer().getName() + " has joined the game");
			attemptToStartCountdown(false);
		}
	}
	
	public void attemptToStartCountdown(boolean force_start)
	{
		int min = cached_arena_data.getMinimumPlayers();
		
		if(!isRunning())
		{
			if((getLobbyPlayerCount() >= min) || force_start == true)
			{
				startCountdown();
			}
		}
	}
	
	private void setLobbySpawn()
	{
		double[] lobby_spawn_coords = cached_arena_data.getLobbySpawn();
		double x,y,z;
		
		x = lobby_spawn_coords[0];
		y = lobby_spawn_coords[1];
		z = lobby_spawn_coords[2];
		
		lobby_spawn = new Location(pl.getServer().getWorld(game_name), x, y, z);
	}
	
	public void deleteLobbySelection()
	{
		String file_path = "arenas/" + game_name;
		String file_name = game_name + ".txt";
		
		String one = pl.getFileManager().readLine(file_path, file_name, "lobby_location_one");
		String two = pl.getFileManager().readLine(file_path, file_name, "lobby_location_two");
		
		if(one.isEmpty() || two.isEmpty())
			return;
		
		World world = pl.getServer().getWorld(game_name);
		
		try
		{
			int x_start,y_start,z_start,x_end,y_end,z_end;
			
			String[] one_split = one.split(" ");
			String[] two_split = two.split(" ");
			
			x_start = Integer.parseInt(one_split[0]);
			y_start = Integer.parseInt(one_split[1]);
			z_start = Integer.parseInt(one_split[2]);
			
			x_end = Integer.parseInt(two_split[0]);
			y_end = Integer.parseInt(two_split[1]);
			z_end = Integer.parseInt(two_split[2]);
			
			pl.resetArea(world, x_start, y_start, z_start, x_end, y_end, z_end);
		}
		catch(NumberFormatException e)
		{
			System.out.println("failed to parse coordinates - lobby selection delete");
		}
	}
	
	public void teleportPlayerToLobby(GamePlayer gamep)
	{
		Player player = gamep.getPlayer();
		
		player.teleport(lobby_spawn);
		player.sendMessage(ChatColor.AQUA + "Lobby Joined!");
	}
	
	public void attemptToCloseLobby()
	{	
		int min = cached_arena_data.getMinimumPlayers();
		
		if(getLobbyPlayerCount() >= 1 && getLobbyPlayerCount() < min)
		{
			stopLobbyTimer();
			pl.sendMessageToPlayers(GameState.LOBBY, game_name, "Game cancelled, not enough players!");
		}
		else if(getLobbyPlayerCount() <= 0)
		{
			stopLobbyTimer();
			pl.getLobbys().remove(this);
		}
	}
}