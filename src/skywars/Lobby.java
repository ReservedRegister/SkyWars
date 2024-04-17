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
	
	public Lobby(SkyWars plugin, String game_name_in)
	{
		pl = plugin;
		lobby_timer = null;
		game_name = game_name_in;
		seconds = 10;
		isRunning = false;
		
		lobby_spawn = getLobbySpawn();
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
		String max_players = pl.getFileManager().readLine("arenas/" + game_name + "/", game_name + ".conf", "maximum_players");
		int max = -1;
		
		try
		{
			max = Integer.parseInt(max_players);
		}
		catch(NumberFormatException e)
		{
			return false;
		}
		
		if(getLobbyPlayerCount() >= max)
			return true;
		
		return false;
	}
	
	private void createGame()
	{
		Game game = pl.createGame(game_name);
		game.createGame(this);
	}
	
	public void stopLobbyTimer()
	{
		if(lobby_timer != null)
		{
			lobby_timer.cancel();
			seconds = 10;
			lobby_timer = null;
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
	}
	
	public void addPlayerToLobby(GamePlayer gamep)
	{
		if(gamep != null)
		{
			gamep.setPlayerState(SkyWars.GameState.LOBBY);
			gamep.setGameName(game_name);
			sendPlayerToLobby(gamep);
			
			pl.sendMessageToPlayers(GameState.LOBBY, game_name, gamep.getPlayer().getName() + " has joined the game");
			attemptToStartCountdown(false);
		}
	}
	
	public void attemptToStartCountdown(boolean force_start)
	{
		String min_players =  pl.getFileManager().readLine("arenas/" + game_name + "/", game_name + ".conf", "minimum_players");
		
		int min = -1;
		
		try
		{
			min = Integer.parseInt(min_players);
		}
		catch(NumberFormatException e)
		{
			pl.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Internal Error!");
			return;
		}
		
		if(!isRunning())
		{
			if((getLobbyPlayerCount() >= min) || force_start == true)
			{
				startCountdown();
			}
		}
	}
	
	public Location getLobbySpawn()
	{
		String file_path = "arenas/" + game_name + "/";
		String file_name = game_name + ".conf";
		
		try
		{
			String lobby_spawn = pl.getFileManager().readLine(file_path, file_name, "lobby_spawn");
			
			if(!lobby_spawn.isEmpty())
			{
				String[] split_line = lobby_spawn.split(" ");
				
				double x,y,z;
				x = Double.parseDouble(split_line[0]);
				y = Double.parseDouble(split_line[1]);
				z = Double.parseDouble(split_line[2]);
				
				return new Location(pl.getServer().getWorld(game_name), x, y, z);
			}
		}
		catch(NumberFormatException e)
		{
			System.out.println("failed to parse lobby spawn coordinates");
		}
		
		return pl.getServer().getWorld(game_name).getSpawnLocation();
	}
	
	public void deleteLobbySelection()
	{
		String file_path = "arenas/" + game_name + "/";
		String file_name = game_name + ".conf";
		
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
	
	public void sendPlayerToLobby(GamePlayer gamep)
	{
		Player player = gamep.getPlayer();
		
		player.teleport(lobby_spawn);
		player.sendMessage(ChatColor.AQUA + "Lobby Joined!");
	}
	
	public void attemptToCloseLobby()
	{	
		String min_players = pl.getFileManager().readLine("arenas/" + game_name + "/", game_name + ".conf", "minimum_players");
		
		int min = -1;
		
		try
		{
			min = Integer.parseInt(min_players);
		}
		catch(NumberFormatException e)
		{
			pl.getServer().getConsoleSender().sendMessage("Failed to parse minimum players");
			return;
		}
		
		if(getLobbyPlayerCount() >= 1 && getLobbyPlayerCount() < min)
		{
			stopLobbyTimer();
			pl.sendMessageToPlayers(GameState.LOBBY, game_name, "Game cancelled, not enough players!");
		}
		else if(getLobbyPlayerCount() <= 0)
		{
			stopLobbyTimer();
			pl.removeLobby(this);
		}
	}
}