package skywars;

import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class GamePlayer
{
	private SkyWars pl;
	private SkyWars.GameState player_state;
	private String player_name;
	private String game_name;
	private int coins;
	
	public GamePlayer(SkyWars plugin, String player_name_in)
	{
		pl = plugin;
		player_state = SkyWars.GameState.INACTIVE;
		player_name = player_name_in;
		game_name = null;
		coins = 0;
		
		loadStats();
	}
	
	private void loadStats()
	{
		String player_stats = pl.getFileManager().readLine("", "stats.txt", player_name);
		
		if(player_stats.isEmpty())
			return;
		
		try
		{
			coins = Integer.parseInt(player_stats);
			return;
		}
		catch(NumberFormatException e)
		{
			pl.getServer().getConsoleSender().sendMessage(ChatColor.RED + "Failed to set stats for player: " + player_name);
		}
	}
	
	public String getGameName()
	{
		return game_name;
	}
	
	public Player getPlayer()
	{
		return pl.getServer().getPlayerExact(player_name);
	}
	
	public int getCoins()
	{
		return coins;
	}
	
	public SkyWars.GameState getPlayerState()
	{
		return player_state;
	}
	
	public boolean isPlayerInGame(String game_name_in)
	{
		if(game_name.equals(game_name_in))
			return true;
		
		return false;
	}
	
	public void setGameName(String new_game_name)
	{
		game_name = new_game_name;
	}
	
	public void setCoins(int new_coins)
	{
		coins = new_coins;
	}
	
	public void setPlayerState(SkyWars.GameState newGameState)
	{
		player_state = newGameState;
	}
	
	public void teleportPlayerToSpawn()
	{
		if(!getPlayer().getWorld().getName().equals(pl.getGlobalSpawn().getName()))
			getPlayer().teleport(pl.getGlobalSpawn().getSpawnLocation());
	}
}