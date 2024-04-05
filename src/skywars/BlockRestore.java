package skywars;

import org.bukkit.World;

public interface BlockRestore
{
	public void add(int x, int y, int z, int chunk_x, int chunk_z, String block);
	public World getRestoreWorld();
	public void startTask(long period);
}
