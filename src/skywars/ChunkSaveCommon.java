package skywars;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

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
	
	private class IntegerByRef
	{
		public Integer x;
		
		public IntegerByRef()
		{
			x = 0;
		}
	}
	
	public void saveChests(String world_name, int x, int y, int z, int chunk_x, int chunk_z)
	{
		List<ArenaChestItem> chest_items = getChestItems(world_name, chunk_x, chunk_z, x, y, z);
		
		if(!chest_items.isEmpty())
		{
			String file_path = "arenas/" + world_name + "/";
			String file_name = "chests.txt";
			
			String chest_items_ready = "";
			
			for(ArenaChestItem chest_item : chest_items)
			{
				chest_items_ready += chest_item.getItemLocation() + " " + chest_item.getItemCount() + " " + chest_item.getItemMaterial() + " ";
			}
			
			chest_items_ready = chest_items_ready.trim();
			
			getPlugin().getFileManager().write(file_path, file_name, chunk_x + " " + chunk_z + " " + x + " " + y + " " + z + " " + chest_items_ready, true);
		}
	}
	
	private List<ArenaChestItem> getChestItems(String world_name, int chunk_x, int chunk_z, int x, int y, int z)
	{
		Future<List<ArenaChestItem>> future = getPlugin().getServer().getScheduler().callSyncMethod(getPlugin(), new Callable<List<ArenaChestItem>>()
		{
			@Override
			public List<ArenaChestItem> call() throws Exception
			{
				List<ArenaChestItem> chest_items = new ArrayList<>();
				
				Block block = pl.getServer().getWorld(world_name).getChunkAt(chunk_x, chunk_z).getBlock(x, y, z);
				BlockState block_state = block.getState();
				
				if(block_state instanceof Chest)
				{
			        Chest chest = (Chest) block_state;
			        Inventory chest_inv = chest.getBlockInventory();
			        
			        for(int item_location = 0; item_location < chest_inv.getContents().length; item_location++)
			        {
			        	ItemStack item = chest_inv.getContents()[item_location];
			        	
			        	if(item != null)
			        	{
			        		String item_material = new String(item.getType().name());
			        		Integer item_count = item.getAmount();
			        		
			        		chest_items.add(new ArenaChestItem(item_location , item_count, item_material));
			        	}
			        }
				}
				
				return chest_items;
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
	
	public void saveArenaChunks(String world_name, CommandSender sender, int range)
	{
		String file_path = "arenas/" + world_name;
		String file_name = "chunks.txt";
		
		ExecutorService threadpool = Executors.newFixedThreadPool(64);
		ChunkWriter file_handle = new ChunkWriter(pl, file_path, file_name, true);
		World world = pl.getServer().getWorld(world_name);
		
		Map<Integer, String> maps = new HashMap<>();
		IntegerByRef block_maps = new IntegerByRef();
		
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
	
	private void waitForTasks(ExecutorService threadpool, ChunkWriter file_handle, CommandSender sender, String world_name, Map<Integer, String> maps)
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
		    		
		    		file_handle.closeWriter();
		    		file_handle.writeChunkSize();
		    		
	    			boolean chunk_maps_ready = pl.getFileManager().readChunkMaps(world_name);
	    			boolean chunks = pl.getFileManager().readChunks(world_name);
	    			boolean chests = pl.getFileManager().readChests(world_name);
	    			
	    			if(chunk_maps_ready && chunks && chests)
	    			{
		    			pl.getLoadingArenas().remove(world_name);
		    			pl.getLoadedArenas().add(world_name);
		    			
		    			pl.getFileManager().getCachedArenas().get(world_name).parseRestoreBytes();
		    			sender.sendMessage(ChatColor.DARK_PURPLE + "Arena loaded!");
	    			}
	    			else
	    			{
	    				sender.sendMessage(ChatColor.DARK_PURPLE + "Failed to load arena world data!");
	    			}
	    			
	    			task.cancel();
		    	}
		    }
		}, 0, 1);
	}
	
	private void saveChunkMaps(String world_name, Map<Integer, String> maps)
	{
		String write_line = "";
		String file_path = "arenas/" + world_name;
		String maps_file = "maps.txt";
		
		for(Integer block_map : maps.keySet())
		{
			write_line += block_map + " " + maps.get(block_map) + "\n";
		}
		
		getPlugin().getFileManager().write(file_path, maps_file, write_line, false);
	}
	
	public void saveChunk(ChunkSnapshot snap, ChunkWriter file_handle, Map<Integer, String> maps, IntegerByRef block_maps)
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
						int block_count = (coords_data-1) - start;
						String last_block_coords = "";
						
						if(block_count != 0)
							last_block_coords = " " + start + ";" + block_count;
						else
							last_block_coords = " " + start;
						
						
						String saved_blocks = saved_block_data.get(last_block);
						
						if(saved_blocks != null)
						{
							String updated_blocks = saved_blocks + last_block_coords;
							saved_block_data.put(last_block, updated_blocks);
						}
						else
						{
							saved_block_data.put(last_block, last_block_coords);
						}
						
						start = coords_data;
					}
					
					if(current_block.startsWith(Integer.toString(54)) || current_block.startsWith("CHEST"))
					{
						//System.out.println(current_block);
						saveChests(snap.getWorldName(), x, y, z, snap.getX(), snap.getZ());
					}
					
					last_block = current_block;
					coords_data++;
				}
			}
		}
		
		int top_block = (block_height * 16 * 16) - 1;
		int block_count = top_block - start;
		String last_block_layer  = "";
		
		if(block_count != 0)
			last_block_layer = " " + start + ";" + block_count;
		else
			last_block_layer = " " + start;
		
		String saved_blocks = saved_block_data.get(last_block);
		
		if(saved_blocks != null)
		{
			String updated_blocks = saved_blocks + last_block_layer;
			saved_block_data.put(last_block, updated_blocks);
		}
		else
		{
			saved_block_data.put(last_block, last_block_layer);
		}
		
		saveChunkBytes(saved_block_data, snap, file_handle, maps, block_maps);
	}
	
	private synchronized void saveChunkBytes(Map<String, String> saved_block_data, ChunkSnapshot snap, ChunkWriter file_handle, Map<Integer, String> maps, IntegerByRef block_maps)
	{
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
		String label = snap.getX() + " " + snap.getZ() + " " + chunk_data.length + " ";
		
		file_handle.writeBytes(label.getBytes(), chunk_data);
	}
	
	public abstract BlockRestore newBlockRestoreTask(String game_name, ExecutorService threadpool);
	
	public abstract ChunkRestore newChunkRestoreTask(BlockRestore block_restore, ChunkSnapshot snap_in, String unzip);
	
	public abstract String getCurrentBlock(ChunkSnapshot snap, int x, int y, int z);
}
