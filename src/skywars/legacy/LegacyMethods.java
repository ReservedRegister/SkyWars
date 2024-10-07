package skywars.legacy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.ChunkSnapshot;
import org.bukkit.block.BlockState;

import skywars.SkyWars;

public class LegacyMethods
{
	private SkyWars pl;
	private Method settypeid;
	private Method setrawdata;
	private Method gettype;
	private Method getdata;
	
	public LegacyMethods(SkyWars plugin)
	{
		pl = plugin;
		
		settypeid = null;
		setrawdata = null;
		gettype = null;
		getdata = null;
		
		try
		{
			getdata = ChunkSnapshot.class.getMethod("getBlockData", int.class, int.class, int.class);
		}
		catch(NoSuchMethodException e)
		{
			pl.getServer().getConsoleSender().sendMessage("Failed to find a method to restore chunks getBlockData()");
		}
		catch(SecurityException e)
		{
			e.printStackTrace();
		}
		
		try
		{
			gettype = ChunkSnapshot.class.getMethod("getBlockTypeId", int.class, int.class, int.class);
		}
		catch(NoSuchMethodException e)
		{
			pl.getServer().getConsoleSender().sendMessage("Failed to find a method to restore chunks getBlockTypeId()");
		}
		catch(SecurityException e)
		{
			e.printStackTrace();
		}
		
		try
		{
			settypeid = BlockState.class.getMethod("setTypeId", int.class);
		}
		catch(NoSuchMethodException e)
		{
			pl.getServer().getConsoleSender().sendMessage("Failed to find a method to restore chunks setTypeId()");
		}
		catch(SecurityException e)
		{
			e.printStackTrace();
		}
		
		try
		{
			setrawdata = BlockState.class.getMethod("setRawData", byte.class);
		}
		catch(NoSuchMethodException e)
		{
			pl.getServer().getConsoleSender().sendMessage("Failed to find a method to restore chunks setRawData()");
		}
		catch(SecurityException e)
		{
			e.printStackTrace();
		}
	}
	
	public Object getBlockData(ChunkSnapshot snap, int x, int y, int z)
	{
		try
		{
			return getdata.invoke(snap, x, y, z);
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch (InvocationTargetException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public Object getBlockTypeId(ChunkSnapshot snap, int x, int y, int z)
	{		
		try
		{
			return gettype.invoke(snap, x, y, z);
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch (InvocationTargetException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public Object setTypeId(BlockState current_state, int material)
	{		
		try
		{
			return settypeid.invoke(current_state, material);
		}
		catch (NumberFormatException e)
		{
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch (InvocationTargetException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public Object setRawData(BlockState current_state, byte bytedata)
	{		
		try
		{
			return setrawdata.invoke(current_state, bytedata);
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch (InvocationTargetException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
}
