package skywars.legacy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import org.bukkit.ChunkSnapshot;

import skywars.SkyWars;
import skywars.adt.ChunkRestoreTask;
import skywars.interfaces.BlockRestore;
import skywars.interfaces.ChunkRestore;

public class ChunkRestoreMaterialData extends ChunkRestoreTask implements ChunkRestore
{
	private BlockRestore blockrestore_task;
	private ChunkSnapshot snap;
	private Method get_type;
	private Method get_data;
	private String current_materialdata;
	private int chunk_x;
	private int chunk_z;
	
	public ChunkRestoreMaterialData(SkyWars plugin, Map<Integer, String> maps_in, BlockRestore blockrestore_task_in, String chunk_data_in, String chunk_name_in)
	{
		super(plugin, maps_in, blockrestore_task_in.getRestoreWorld().getName(), chunk_data_in, chunk_name_in);
		
		blockrestore_task = blockrestore_task_in;
		snap = getChunkSnap();
		get_type = null;
		get_data = null;
		current_materialdata = null;
		chunk_x = getChunkX();
		chunk_z = getChunkZ();
		
		setMethods();
	}
	
	private void setMethods()
	{
		try
		{
			get_data = snap.getClass().getMethod("getData", int.class, int.class, int.class);
		}
		catch(SecurityException e) {}
		catch(NoSuchMethodException e)
		{
			  try
			  {
				  get_data = snap.getClass().getMethod("getBlockData", int.class, int.class, int.class);
				  get_type = snap.getClass().getMethod("getBlockType", int.class, int.class, int.class);
			  }
			  catch(SecurityException e2) {}
			  catch(NoSuchMethodException e1)
			  {
				  try
				  {
					  get_type = snap.getClass().getMethod("getBlockTypeId", int.class, int.class, int.class);
				  }
				  catch(SecurityException e2) {}
				  catch(NoSuchMethodException e2)
				  {
					  System.out.println("failed to find a method to restore chunk");
				  }
			  }
		}
	}
	
	@Override
	public void restoreChunk(String block, int y_start, int x_start, int z_start, int x_end, int z_end)
	{
		boolean iterate = true;
		
		int y = y_start;
		int x = x_start;
		int z = z_start;
		
		while(iterate)
		{
			if(x == x_end && z == z_end)
				iterate = false;
			
			try
			{
				current_materialdata = get_type.invoke(snap, x, y, z) + "(" + get_data.invoke(snap, x, y, z) + ")";
			}
			catch(IllegalArgumentException e3) {}
			catch(IllegalAccessException e4) {}
			catch(InvocationTargetException e5) {}
			
			if(!block.equals(current_materialdata))
				blockrestore_task.add(x, y, z, chunk_x, chunk_z, block);
			
			z++;
			if(z == 16)
			{
				z = 0;
				x++;
				
				if(x == 16)
					return;
			}
		}
	}
}