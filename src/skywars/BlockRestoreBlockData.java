package skywars;

import java.util.concurrent.ExecutorService;

import org.bukkit.block.BlockState;

public class BlockRestoreBlockData extends BlockRestoreTask
{
	public BlockRestoreBlockData(SkyWars plugin, String world_name_in, ExecutorService threadpool_in)
	{
		super(plugin, world_name_in, threadpool_in);
	}
	
	@Override
	public void restoreBlocks()
	{
		BlockQueue block_queue = null;
		
		while((block_queue = super.removeWithLimit()) != null)
		{
			String block = block_queue.getBlock();
			int x = block_queue.getX();
			int y = block_queue.getY();
			int z = block_queue.getZ();
			int chunk_x = block_queue.getChunkX();
			int chunk_z = block_queue.getChunkZ();
			
			BlockState current_state = getRestoreWorld().getChunkAt(chunk_x, chunk_z).getBlock(x, y, z).getState();
			current_state.setBlockData(getPlugin().getServer().createBlockData(block));
			current_state.update(true, false);
			incrementBlocksBy(1);
		}
		
		if(super.isThreadPoolDone())
		{
			super.getPlugin().getServer().getConsoleSender().sendMessage("[" + getBlocks() + "] blocks restored in [" + getRestoreWorld().getName() + "]");
			super.getPlugin().getGame(getRestoreWorld().getName()).restoreArenaChests();
			super.getPlugin().getGames().remove(super.getPlugin().getGame(getRestoreWorld().getName()));
			
			Lobby game_lobby = super.getPlugin().getLobby(getRestoreWorld().getName());
			
			if(game_lobby != null)
			{
				game_lobby.attemptToStartCountdown(false);
			}
			
			super.cancel();
		}
	}


}
