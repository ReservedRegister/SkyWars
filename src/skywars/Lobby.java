package skywars;

import java.util.Iterator;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class Lobby
{
	private SkyWars pl;
	private BukkitTask lobby_timer;
	private Location lobby_spawn;
	private String game_name;
	private int seconds;
	private boolean has_started;
	
	public Lobby(SkyWars plugin, String game_name_in)
	{
		pl = plugin;
		lobby_timer = null;
		game_name = game_name_in;
		seconds = 10;
		has_started = false;
		
		lobby_spawn = getLobbySpawn();
	}
	
	public String getGameName()
	{
		return game_name;
	}
	
	public void setGameName(String new_game_name)
	{
		game_name = new_game_name;
	}
	
	public boolean hasStarted()
	{
		return has_started;
	}
	
	public int getLobbyPlayerCount()
	{
		int totalPlayers = 0;
		
		Iterator<GamePlayer> gamep_it = pl.getGamePlayersIterator();
		
		while(gamep_it.hasNext())
		{
			GamePlayer gamep = gamep_it.next();
			
			if(gamep.getLobbyStatus() == true && gamep.isPlayerInGame(game_name))
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
	
	public void startGame()
	{
		stopLobbyTimer();
		createGame();
	}
	
	private void createGame()
	{
		String movement_line = pl.getFileManager().readLine("arenas/" + game_name + "/", game_name + ".conf", "allow_movement");
		boolean movement = pl.getBooleanFromString(movement_line);
		
		Game game = pl.createGame(game_name, movement);
		game.createGame(this);
	}
	
	public void stopLobbyTimer()
	{
		if(lobby_timer != null)
		{
			lobby_timer.cancel();
			has_started = false;
			seconds = 10;
		}
	}
	
	public void startCountdown(String game_name)
	{
		lobby_timer = pl.getServer().getScheduler().runTaskTimer(pl, new Runnable()
		{
			@Override
			public void run()
			{
				if(seconds == 0)
				{
					startGame();
					return;
				}
				
				pl.sendMessageToPlayers("lobby", game_name, ChatColor.YELLOW + "Seconds left: " + seconds);
				seconds--;
			}
		}, 0, 20);
		
		has_started = true;
	}
	
	public void setHasStarted(boolean new_start_value)
	{
		has_started = new_start_value;
	}
	
	public void addPlayerToLobby(GamePlayer gamep)
	{
		try
		{
			gamep.setLobbyStatus(true);
			gamep.setGameName(game_name);
			sendPlayerToLobby(gamep);
		}
		catch(NullPointerException e) {}
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
}