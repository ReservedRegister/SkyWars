package skywars;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

public class AsyncFileWrite
{
	private SkyWars pl;
	private OutputStream write_stream;
	private Queue<byte[]> write_data_buffer;
	private boolean closeBuffer;
	
	public AsyncFileWrite(SkyWars plugin, String file_path, String file_name, boolean append)
	{
		pl = plugin;
		write_data_buffer = new LinkedList<>();
		closeBuffer = false;
		
		createWriteTask(file_path, file_name, append);
	}
	
	public synchronized boolean isWriteFinished()
	{
		if(closeBuffer)
		{
			if(write_data_buffer.isEmpty())
			{
				return true;
			}
		}
		
		return false;
	}
	
	public synchronized void writeBytes(byte[] label, byte[] write_lines)
	{
		if(closeBuffer != true)
		{
			write_data_buffer.add(label);
			write_data_buffer.add(write_lines);
		}
	}
	
	public synchronized void closeBuffering()
	{
		closeBuffer = true;
	}
	
	private void createWriteTask(String file_path, String file_name, boolean append)
	{
		pl.getFileManager().createFile(file_path, file_name, false);
		
		try
		{
			write_stream = new FileOutputStream(pl.getFileManager().getPluginFolder() + file_path + "/" + file_name, append);
		}
		catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}
		
		Timer timer = new Timer();
		timer.schedule(new TimerTask()
		{
		    @Override
		    public void run()
		    {
		    	synchronized(this)
		    	{
			    	if(!write_data_buffer.isEmpty())
			    	{
			    		try
			    		{
			    			write_stream.write(write_data_buffer.remove());
			    		}
			    		catch(IOException e)
			    		{
			    			e.printStackTrace();
			    		}
			    	}
			    	else if(closeBuffer && write_data_buffer.isEmpty())
			    	{
			    		try
			    		{
							write_stream.close();
						}
			    		catch(IOException e)
			    		{
							e.printStackTrace();
						}
			    		
			    		timer.cancel();
			    	}
		    	}
		    }
		}, 0, 1);
	}
}
