package skywars.legacy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;

import org.bukkit.Material;
import org.bukkit.block.BlockState;

import skywars.BlockQueue;
import skywars.BlockRestoreTask;
import skywars.Lobby;
import skywars.SkyWars;

public class BlockRestoreMaterialData extends BlockRestoreTask
{
	private Method settypeid;
	private Method setrawdata;
	
	public BlockRestoreMaterialData(SkyWars plugin, String world_name_in, ExecutorService threadpool_in)
	{
		super(plugin, world_name_in, threadpool_in);
		
		settypeid = null;
		setrawdata = null;
	}
	
	@Override
	public void restoreBlocks()
	{
		BlockQueue block_queue = null;
		
		while((block_queue = super.removeWithLimit()) != null)
		{
			String materialdata = block_queue.getBlock();
			String material = null;
			String bytedata = null;
			int x = block_queue.getX();
			int y = block_queue.getY();
			int z = block_queue.getZ();
			int chunk_x = block_queue.getChunkX();
			int chunk_z = block_queue.getChunkZ();
			
			try
			{
				material = materialdata.substring(0, materialdata.indexOf("("));
			}
			catch(StringIndexOutOfBoundsException e)
			{
				getPlugin().getServer().getConsoleSender().sendMessage("failed to obtain material");
				return;
			}
			
			try
			{
				bytedata = materialdata.substring(materialdata.indexOf("(") + 1, materialdata.indexOf(")"));
			}
			catch(StringIndexOutOfBoundsException e)
			{
				getPlugin().getServer().getConsoleSender().sendMessage("failed to obtain bytedata");
				return;
			}
			
			BlockState current_state = getRestoreWorld().getChunkAt(chunk_x, chunk_z).getBlock(x, y, z).getState();
			
			try
			{
				current_state.setType(Material.valueOf(material));
			}
			catch(IllegalArgumentException e)
			{	
				try
				{
					settypeid = current_state.getClass().getMethod("setTypeId", int.class);
				}
				catch(SecurityException e1) {}
				catch(NoSuchMethodException e2)
				{
					getPlugin().getServer().getConsoleSender().sendMessage("failed to find a method to restore chunks");
				}
				
				try
				{
					settypeid.invoke(current_state, Integer.parseInt(material));
				}
				catch(IllegalArgumentException e3) {}
				catch(IllegalAccessException e4) {}
				catch(InvocationTargetException e5) {}
			}
			
			try
			{
				setrawdata = current_state.getClass().getMethod("setRawData", byte.class);
			}
			catch(SecurityException e1) {}
			catch(NoSuchMethodException e2)
			{
				getPlugin().getServer().getConsoleSender().sendMessage("failed to find a method to restore chunks");
			}
			
			try
			{
				setrawdata.invoke(current_state, Byte.parseByte(bytedata));
			}
			catch(IllegalArgumentException e3) {}
			catch(IllegalAccessException e4) {}
			catch(InvocationTargetException e5) {}
			
			current_state.update(true, false);
			incrementBlocksBy(1);
		}
		
		if(super.isThreadPoolDone())
		{
			super.getPlugin().getServer().getConsoleSender().sendMessage("[" + getBlocks() + "] blocks restored in [" + getRestoreWorld().getName() + "]");
			super.getPlugin().removeGame(getPlugin().getGame(getRestoreWorld().getName()));
			
			Lobby game_lobby = super.getPlugin().getLobby(getRestoreWorld().getName());
			
			if(game_lobby != null)
			{
				game_lobby.attemptToStartCountdown(false);
			}
			
			super.cancel();
		}
	}
}
