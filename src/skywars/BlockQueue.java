package skywars;

public class BlockQueue implements Comparable<BlockQueue>
{
	private String block_name;
	private int x;
	private int y;
	private int z;
	private int chunk_x;
	private int chunk_z;
	
	public BlockQueue(int x_in, int y_in, int z_in, int chunk_x_in, int chunk_z_in, String block_name_in)
	{
		block_name = block_name_in;
		x = x_in;
		y = y_in;
		z = z_in;
		chunk_x = chunk_x_in;
		chunk_z = chunk_z_in;
	}
	
	public String getBlock()
	{
		return block_name;
	}
	
	public int getX()
	{
		return x;
	}
	
	public int getY()
	{
		return y;
	}
	
	public int getZ()
	{
		return z;
	}
	
	public int getChunkX()
	{
		return chunk_x;
	}
	
	public int getChunkZ()
	{
		return chunk_z;
	}
	
    @Override
    public int compareTo(BlockQueue block_restore)
    {
    	if(getY() > block_restore.getY())
        	return 1;
        else if(getY() < block_restore.getY())
        	return -1;
        else
        	return 0;
    }
}
