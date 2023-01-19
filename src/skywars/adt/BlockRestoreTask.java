package skywars.adt;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ExecutorService;

import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import skywars.BlockQueue;
import skywars.SkyWars;
import skywars.interfaces.BlockRestore;

public abstract class BlockRestoreTask extends BukkitRunnable implements BlockRestore
{
	private SkyWars pl;
	private ExecutorService threadpool;
	private World world;
	private Queue<BlockQueue> blocks;
	private int block_counter;
	private int limit_block_counter;
	
	public BlockRestoreTask(SkyWars plugin, String world_name_in, ExecutorService threadpool_in)
	{
		pl = plugin;
		threadpool = threadpool_in;
		world = pl.getServer().getWorld(world_name_in);
		blocks = new PriorityQueue<>();
		block_counter = 0;
		limit_block_counter = 0;
	}
	
	public abstract void restoreBlocks();
	
	public void startTask(long period)
	{
		runTaskTimer(pl, 0, period);
	}
	
	public SkyWars getPlugin()
	{
		return pl;
	}
	
	public boolean isThreadPoolDone()
	{
		return threadpool.isTerminated() && blocks.isEmpty();
	}
	
	public World getRestoreWorld()
	{
		return world;
	}
	
	public int getBlocks()
	{
		return block_counter;
	}
	
	public synchronized void add(int x, int y, int z, int chunk_x, int chunk_z, String block)
	{
		blocks.add(new BlockQueue(x, y, z, chunk_x, chunk_z, block));
	}
	
	public BlockQueue removeWithLimit()
	{
		if(limit_block_counter <= 250)
		{
			limit_block_counter++;
			return remove();
		}
		
		limit_block_counter = 0;
		return null;
	}
	
	public synchronized BlockQueue remove()
	{
		if(blocks.isEmpty())
			return null;
		
		return blocks.remove();
	}
	
	public void incrementBlocksBy(int blocks)
	{
		block_counter += blocks;
	}
	
	@Override
	public void run()
	{
		restoreBlocks();
	}
}
