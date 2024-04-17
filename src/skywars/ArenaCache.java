package skywars;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import skywars.others.GzipUtil;

public class ArenaCache
{
	private List<double[]> spawnpoints;
	private int min_players;
	private int max_players;
	private boolean move;
	private double[] centre_chunk;
	private double[] lobby_spawn;
	private byte[] restore_bytes;
	private Map<Integer, String> chunk_maps;
	private Map<int[], String> processed_chunks;
	
	public ArenaCache(int min_in, int max_in, boolean move_in, byte[] restore_bytes_in)
	{
		spawnpoints = new ArrayList<>();
		min_players = min_in;
		max_players = max_in;
		move = move_in;
		centre_chunk = new double[2];
		lobby_spawn = new double[3];
		restore_bytes = restore_bytes_in;
		chunk_maps = new HashMap<>();
		processed_chunks = new HashMap<>();
	}
	
	public ArenaCache()
	{
		spawnpoints = new ArrayList<>();
		min_players = -1;
		max_players = -1;
		move = true;
		centre_chunk = null;
		lobby_spawn = null;
		restore_bytes = null;
	}
	
	public List<double[]> getSpawnpoints()
	{
		return spawnpoints;
	}
	
	public int getMinimumPlayers()
	{
		return min_players;
	}
	
	public int getMaximumPlayers()
	{
		return max_players;
	}
	
	public boolean isAllowedToMove()
	{
		return move;
	}
	
	public double[] getCentre()
	{
		return centre_chunk;
	}
	
	public double[] getLobbySpawn()
	{
		return lobby_spawn;
	}
	
	public byte[] getRestoreBytes()
	{
		return restore_bytes;
	}
	
	public Map<Integer, String> getChunkMaps()
	{
		return chunk_maps;
	}
	
	public Map<int[], String> getUnzipedRestoreData()
	{
		return processed_chunks;
	}
	
	public void setSpawnpoints(List<double[]> spawns)
	{
		spawnpoints = spawns;
	}
	
	public void addSpawnPoint(double[] spawn_in)
	{
		spawnpoints.add(spawn_in);
	}
	
	public void setMinimumPlayers(int min_in)
	{
		min_players = min_in;
	}
	
	public void setMaximumPlayers(int max_in)
	{
		max_players = max_in;
	}
	
	public void setMoveSetting(boolean move_in)
	{
		move = move_in;
	}
	
	public void setCentre(double[] chunk_in)
	{
		centre_chunk = chunk_in;
	}
	
	public void setLobbySpawn(double[] spawn_in)
	{
		lobby_spawn = spawn_in;
	}
	
	public void setRestoreBytes(byte[] bytes_in)
	{
		restore_bytes = bytes_in;
	}
	
	public void setChunkMaps(Map<Integer, String> chunk_maps_in)
	{
		chunk_maps = chunk_maps_in;
	}
	
	public boolean parseRestoreBytes()
	{
		int chunk_data_counter = 0;
		String buffer = "";
		int character_byte = -1;
		List<String> buffer_data = new ArrayList<>();
		
		while(restore_bytes[chunk_data_counter] < restore_bytes.length)
		{
			String character = new String(new byte[]{(byte) character_byte});
			
			if(character.equals(" "))
			{
				buffer_data.add(buffer);
				
				if(buffer_data.size() == 3)
				{
					try
					{
						int chunk_x = Integer.parseInt(buffer_data.get(0));
						int chunk_z = Integer.parseInt(buffer_data.get(1));
						int chunk_size = Integer.parseInt(buffer_data.get(2));
						
						byte[] actual_chunk = new byte[chunk_size];
						int actual_chunk_counter = 0;
						
						chunk_data_counter++;
						
						for(int i = 0; i < chunk_size; i++)
						{
							actual_chunk[actual_chunk_counter] = restore_bytes[chunk_data_counter];
							
							actual_chunk_counter++;
							chunk_data_counter++;
						}
						
						processed_chunks.put(new int[] {chunk_x, chunk_z}, GzipUtil.unzip(actual_chunk));
						
						buffer_data.clear();
						buffer = "";
						continue;
					}
					catch(NumberFormatException e)
					{
						e.printStackTrace();
						return false;
					}
				}
				
				buffer = "";
				chunk_data_counter++;
				continue;
			}
			
			buffer += character;
			chunk_data_counter++;
		}
		
		return true;
	}
}
