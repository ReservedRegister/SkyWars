package skywars;

import org.bukkit.ChunkSnapshot;

public abstract class ChunkRestoreTask implements Runnable
{
	private SkyWars pl;
	private ChunkSnapshot snap;
	private ArenaCache arena_cache;
	private String chunk_data;
	private String world_name;
	private int block_height;
	private int chunk_x;
	private int chunk_z;
	
	public ChunkRestoreTask(SkyWars plugin, ChunkSnapshot snap_in, String world_name_in, String chunk_data_in)
	{
		pl = plugin;
		snap = snap_in;
		world_name = world_name_in;
		chunk_data = chunk_data_in;
		arena_cache = pl.getFileManager().getCachedArenas().get(world_name);
		block_height = pl.getServer().getWorld(world_name).getMaxHeight();
		chunk_x = snap_in.getX();
		chunk_z = snap_in.getZ();
	}
	
	public abstract void restoreChunk(String block, int y_start, int x_start, int z_start, int x_end, int z_end);
	
	public int getChunkX()
	{
		return chunk_x;
	}
	
	public int getChunkZ()
	{
		return chunk_z;
	}
	
	public ChunkSnapshot getChunkSnap()
	{
		return snap;
	}
	
	public int getMaxHeight()
	{
		return block_height;
	}
	
	private void iterateChunk(String block, String start_end)
	{
		try
		{
			String[] coords_start_end = start_end.split(";");
			
			String start = coords_start_end[0];
			String end = coords_start_end[1];
			
			String[] start_coords = start.split(" ");
			String[] end_coords = end.split(" ");
			
			int y_start = Integer.parseInt(start_coords[0]);
			int x_start = Integer.parseInt(start_coords[1]);
			int z_start = Integer.parseInt(start_coords[2]);
			
			int y_end = Integer.parseInt(end_coords[0]);
			int x_end = Integer.parseInt(end_coords[1]);
			int z_end = Integer.parseInt(end_coords[2]);
			
			for(int y = y_start+1; y < y_end; y++)
			{
				restoreChunk(block, y, 0, 0, 16, 16);
			}
			
			if(y_start != y_end)
			{
				restoreChunk(block, y_start, x_start, z_start, 16, 16);
				restoreChunk(block, y_end, 0, 0, x_end, z_end);
			}
			else
				restoreChunk(block, y_start, x_start, z_start, x_end, z_end);
		}
		catch(NumberFormatException e)
		{
			pl.getServer().getConsoleSender().sendMessage(SkyWars.PREFIX + "failed to parse chunk data");
			pl.getServer().getConsoleSender().sendMessage(SkyWars.PREFIX + "at: " + start_end);
		}
	}
	
	@Override
	public void run()
	{	
		for(String line : chunk_data.split("\n"))
		{
			String[] line_split = line.split(" ");
			
			try
			{
				String block = arena_cache.getBlockFromMap(Integer.parseInt(line_split[0]));
				
				for(int i = 1; i < line_split.length; i++)
				{
					String coords_data = line_split[i];
					
					if(coords_data.contains(";"))
					{
						String[] split_coords = coords_data.split(";");
						
						try
						{
							int start = Integer.parseInt(split_coords[0]);
							int end = start + Integer.parseInt(split_coords[1]);
							
							int y_start = start / 256;
							int x_start = start % 256 / 16;
							int z_start = start % 256 % 16;
							
							int y_end = end / 256;
							int x_end = end % 256 / 16;
							int z_end = end % 256 % 16;
							
							iterateChunk(block, y_start + " " + x_start + " " + z_start + ";" + y_end + " " + x_end + " " + z_end);
						}
						catch(NumberFormatException e)
						{
							pl.getServer().getConsoleSender().sendMessage(SkyWars.PREFIX + "failed to parse chunk data");
							pl.getServer().getConsoleSender().sendMessage(SkyWars.PREFIX + "at: " + block + " " + coords_data);
						}
					}
					else
					{
						try
						{
							int coords = Integer.parseInt(coords_data);
							
							int y = coords / 256;
							int x = coords % 256 / 16;
							int z = coords % 256 % 16;
							
							iterateChunk(block, y + " " + x + " " + z + ";" + y + " " + x + " " + z);
						}
						catch(NumberFormatException e)
						{
							pl.getServer().getConsoleSender().sendMessage(SkyWars.PREFIX + "failed to parse chunk data");
							pl.getServer().getConsoleSender().sendMessage(SkyWars.PREFIX + "at: " + block + " " + coords_data);
						}
					}
				}
			}
			catch(NumberFormatException e)
			{
				System.out.println("READ: failed to parse chunk maps");
			}
		}
	}
}
