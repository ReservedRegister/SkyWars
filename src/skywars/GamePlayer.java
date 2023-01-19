package skywars;

import org.bukkit.entity.Player;

public class GamePlayer
{
	private SkyWars pl;
	private String player_name;
	private String game_name;
	private int coins;
	private boolean in_lobby;
	private boolean in_game;
	
	public GamePlayer(SkyWars plugin, String player_name_in)
	{
		pl = plugin;
		player_name = player_name_in;
		game_name = null;
		coins = 0;
		in_lobby = false;
		in_game = false;
		
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
		catch(NumberFormatException e) {}
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
	
	public boolean getLobbyStatus()
	{
		return in_lobby;
	}
	
	public boolean getGameStatus()
	{
		return in_game;
	}
	
	public boolean isPlayerInGame(String game_name_in)
	{
		try
		{
			if(game_name.equals(game_name_in))
				return true;
		}
		catch(NullPointerException e) {}
		
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
	
	public void setLobbyStatus(boolean new_lobby_status)
	{
		in_lobby = new_lobby_status;
	}
	
	public void setGameStatus(boolean new_game_status)
	{
		in_game = new_game_status;
	}
}