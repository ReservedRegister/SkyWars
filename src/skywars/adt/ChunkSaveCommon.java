package skywars.adt;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.bukkit.ChatColor;
import org.bukkit.ChunkSnapshot;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;

import skywars.FileHandle;
import skywars.SkyWars;
import skywars.interfaces.BlockRestore;
import skywars.interfaces.ChunkRestore;
import skywars.others.GzipUtil;

public abstract class ChunkSaveCommon
{
	private SkyWars pl;
	
	public ChunkSaveCommon(SkyWars plugin)
	{
		pl = plugin;
	}
	
	public SkyWars getPlugin()
	{
		return pl;
	}
	
	public void saveChests(String world_name, Object material, int x, int y, int z, int chunk_x, int chunk_z)
	{
		if(material instanceof String)
		{
			if(material.equals("CHEST"))
			{
				String file_path = "arenas/" + world_name + "/";
				String file_name = "chests.txt";
				int[] coords = getCoords(world_name, chunk_x, chunk_z, x, y, z);
				
				getPlugin().getFileManager().write(file_path, file_name, new String[] {coords[0] + " " + coords[1] + " " + coords[2] + ":"}, true);
			}
		}
		else if(material instanceof Integer)
		{
			if(material.equals(54))
			{
				String file_path = "arenas/" + world_name + "/";
				String file_name = "chests.txt";
				int[] coords = getCoords(world_name, chunk_x, chunk_z, x, y, z);
				
				getPlugin().getFileManager().write(file_path, file_name, new String[] {coords[0] + " " + coords[1] + " " + coords[2] + ":"}, true);
			}
		}
	}
	
	private int[] getCoords(String world_name, int chunk_x, int chunk_z, int x, int y, int z)
	{
		Future<int[]> future = getPlugin().getServer().getScheduler().callSyncMethod(getPlugin(), new Callable<int[]>()
		{
			@Override
			public int[] call() throws Exception
			{
				Block block = pl.getServer().getWorld(world_name).getChunkAt(chunk_x, chunk_z).getBlock(x, y, z);
				return new int[] {block.getX(), block.getY(), block.getZ()};
			}
		});
		
		try
		{
			return future.get();
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
		}
		catch(ExecutionException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	private class IntegerByRef
	{
		public Integer x;
		
		public IntegerByRef(Integer x_in)
		{
			x = x_in;
		}
	}
	
	public void saveArenaChunks(CommandSender sender, String world_name, int range)
	{
		ExecutorService threadpool = Executors.newSingleThreadExecutor();
		FileHandle file_handle = new FileHandle(pl, "arenas/" + world_name + "/", "chunks", true);
		World world = pl.getServer().getWorld(world_name);
		
		Map<Integer, String> maps = new HashMap<>();
		IntegerByRef block_maps = new IntegerByRef(0);
		
		for(int[] coords : pl.getArenaChunks(world_name, range))
		{
			ChunkSnapshot snap = world.getChunkAt(coords[0], coords[1]).getChunkSnapshot();
			
			threadpool.execute(new Runnable() {
				@Override
				public void run()
				{
					saveChunk(snap, file_handle, maps, block_maps);
				}
			});
		}
		
		waitForTasks(threadpool, file_handle, sender, world_name, maps);
	}
	
	private void waitForTasks(ExecutorService threadpool, FileHandle file_handle, CommandSender sender, String world_name, Map<Integer, String> maps)
	{
		threadpool.shutdown();
		
		Timer task = new Timer();
		task.schedule(new TimerTask()
		{
		    @Override
		    public void run()
		    {
		    	if(threadpool.isTerminated())
		    	{
		    		saveChunkMaps(world_name, maps);
		    		file_handle.closeHandle();
		    		
		    		if(file_handle.isHandleClosed())
		    		{
		    			pl.removeArenaFromQueue(world_name);
		    			pl.addArenaToSet(world_name);
		    			sender.sendMessage(ChatColor.DARK_PURPLE + "Arena loaded!");
		    			task.cancel();
		    		}
		    	}
		    }
		}, 0, 1);
	}
	
	private void saveChunkMaps(String world_name, Map<Integer, String> maps)
	{
		String write_line = "";
		
		for(Integer block_map : maps.keySet())
		{
			write_line += block_map + " " + maps.get(block_map) + "\n";
		}
		
		getPlugin().getFileManager().write("arenas/" + world_name + "/", "maps", write_line, false);
	}
	
	public void saveChunk(ChunkSnapshot snap, FileHandle file_handle, Map<Integer, String> maps, IntegerByRef block_maps)
	{
		Map<String, String> saved_block_data = new HashMap<>();
		String last_block = getCurrentBlock(snap, 0, 0, 0);
		int block_height = pl.getServer().getWorld(snap.getWorldName()).getMaxHeight();
		int coords_data = 0;
		int start = 0;
		
		for(int y = 0; y < block_height; y++)
		{
			for(int x = 0; x < 16; x++)
			{
				for(int z = 0; z < 16; z++)
				{
					String current_block = getCurrentBlock(snap, x, y, z);
					
					if(!current_block.equals(last_block))
					{
						int block_count = coords_data - start - 1;
						String last_block_coords = "";
						
						if(block_count != 0)
							last_block_coords = " " + start + ";" + block_count;
						else
							last_block_coords = " " + start;
						
						
						if(saved_block_data.containsKey(last_block))
							saved_block_data.merge(last_block, last_block_coords, (coords, new_coords) -> coords.concat(new_coords));
						else
							saved_block_data.put(last_block, last_block_coords);
						
						start = coords_data;
					}
					
					last_block = current_block;
					coords_data++;
				}
			}
		}
		
		String last_block_layer = " " + start + ";" + (block_height * 16 * 16 - start - 1);
		
		if(saved_block_data.containsKey(last_block))
			saved_block_data.merge(last_block, last_block_layer, (coords, new_coords) -> coords.concat(new_coords));
		else
			saved_block_data.put(last_block, last_block_layer);
		
		saveChunkBytes(saved_block_data, snap, file_handle, maps, block_maps);
	}
	
	private synchronized void saveChunkBytes(Map<String, String> saved_block_data, ChunkSnapshot snap, FileHandle file_handle, Map<Integer, String> maps, IntegerByRef block_maps)
	{
		String chunk_name = snap.getX() + "." + snap.getZ();
    	String write_line = "";
    	
		for(String block : saved_block_data.keySet())
		{
			boolean found = false;
			
			for(Integer block_map : maps.keySet())
			{
				String block_map_value = maps.get(block_map);
				
				if(block_map_value.equals(block))
				{
					write_line += block_map + saved_block_data.get(block) + "\n";
					found = true;
					break;
				}
			}
			
			if(found)
				continue;
			
			maps.put(block_maps.x, block);
			write_line += block_maps.x + saved_block_data.get(block) + "\n";
			block_maps.x++;
		}
		
		byte[] chunk_data = GzipUtil.zip(write_line.trim());
		String label = chunk_name + "," + chunk_data.length + "]";
		
		file_handle.writeBytes(label.getBytes(), chunk_data);
	}
	
	public abstract BlockRestore newBlockRestoreTask(String game_name, ExecutorService threadpool);
	
	public abstract ChunkRestore newChunkRestoreTask(BlockRestore block_restore, Map<Integer, String> maps, String unzip, String chunk_name);
	
	public abstract String getCurrentBlock(ChunkSnapshot snap, int x, int y, int z);
}
