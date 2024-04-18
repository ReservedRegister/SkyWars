package skywars.legacy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;

import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;

import skywars.BlockRestore;
import skywars.ChunkRestore;
import skywars.ChunkSave;
import skywars.ChunkSaveCommon;
import skywars.SkyWars;

public class ChunkSaveMaterialData extends ChunkSaveCommon implements ChunkSave
{
	public ChunkSaveMaterialData(SkyWars plugin)
	{
		super(plugin);
	}
	
	@Override
	public BlockRestore newBlockRestoreTask(String game_name, ExecutorService threadpool)
	{
		return new BlockRestoreMaterialData(super.getPlugin(), game_name, threadpool);
	}
	
	@Override
	public ChunkRestore newChunkRestoreTask(BlockRestore block_restore, ChunkSnapshot snap_in, String unzip)
	{
		return new ChunkRestoreMaterialData(super.getPlugin(), snap_in, block_restore, unzip);
	}
	
	@Override
	public String getCurrentBlock(ChunkSnapshot snap, int x, int y, int z)
	{
		String world_name = snap.getWorldName();
		Method blocktypemethod = null;
		Method blockdatamagic = null;
		boolean legacy = false;
		
		try
		{
			blockdatamagic = snap.getClass().getMethod("getData", int.class, int.class, int.class);
		}
		catch(SecurityException e) {}
		catch(NoSuchMethodException e)
		{
			  try
			  {
				  blockdatamagic = snap.getClass().getMethod("getBlockData", int.class, int.class, int.class);
				  blocktypemethod = snap.getClass().getMethod("getBlockType", int.class, int.class, int.class);
			  }
			  catch(SecurityException e2) {}
			  catch(NoSuchMethodException e1)
			  {
				  try
				  {
					  blocktypemethod = snap.getClass().getMethod("getBlockTypeId", int.class, int.class, int.class);
					  legacy = true;
				  }
				  catch(SecurityException e2) {}
				  catch(NoSuchMethodException e2)
				  {
					  System.out.println("failed to find a method to save chunks");
				  }
			  }
		}
		
		String current_materialdata = null;
		
		try
		{
			if(!legacy)
			{
				Material material = (Material) blocktypemethod.invoke(snap, x, y, z);
				int magic_value = (int) blockdatamagic.invoke(snap, x, y, z);
				current_materialdata = material.name() + "(" + magic_value + ")";
				saveChests(world_name, material.name(), x, y, z, snap.getX(), snap.getZ());
			}
			else
			{
				int material = (int) blocktypemethod.invoke(snap, x, y, z);
				int magic_value = (int) blockdatamagic.invoke(snap, x, y, z);
				current_materialdata = material + "(" + magic_value + ")";
				saveChests(world_name, (Integer)material, x, y, z, snap.getX(), snap.getZ());
			}
		}
		catch(IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch(IllegalAccessException e)
		{
			e.printStackTrace();
		}
		catch(InvocationTargetException e)
		{
			e.printStackTrace();
		}
		
		return current_materialdata;
	}
}
