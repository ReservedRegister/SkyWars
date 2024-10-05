package skywars;

import java.util.ArrayList;
import java.util.List;

public class ArenaChest
{
	private int chunk_x;
	private int chunk_z;
	private int block_x;
	private int block_y;
	private int block_z;
	private List<ArenaChestItem> chest_items;
	
	public ArenaChest(int chunk_x_in, int chunk_z_in, int block_x_in, int block_y_in, int block_z_in)
	{
		chunk_x = chunk_x_in;
		chunk_z = chunk_z_in;
		block_x = block_x_in;
		block_y = block_y_in;
		block_z = block_z_in;
		chest_items = new ArrayList<>();
	}
	
	public int getChunkX()
	{
		return chunk_x;
	}
	
	public int getChunkZ()
	{
		return chunk_z;
	}
	
	public int getBlockX()
	{
		return block_x;
	}
	
	public int getBlockY()
	{
		return block_y;
	}
	
	public int getBlockZ()
	{
		return block_z;
	}
	
	public void addChestItem(ArenaChestItem new_item)
	{
		chest_items.add(new_item);
	}
	
	public List<ArenaChestItem> getChestItems()
	{
		return chest_items;
	}
}
