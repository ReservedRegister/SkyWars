package skywars;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

public class FileHandle
{
	private ArenaFileManager files;
	private Queue<byte[]> write_data_buffer;
	private OutputStream output_stream;
	private boolean deleteHandle;
	
	public FileHandle(SkyWars pl, String file_path, String file_name, boolean append)
	{
		files = pl.getFileManager();
		write_data_buffer = new LinkedList<>();
		deleteHandle = false;
		
		createHandle(file_path, file_name, append);
	}
	
	public boolean isHandleClosed()
	{
		return output_stream == null;
	}
	
	public synchronized void writeBytes(byte[] label, byte[] write_lines)
	{
		if(deleteHandle != true)
		{
			write_data_buffer.add(label);
			write_data_buffer.add(write_lines);
		}
	}
	
	public synchronized void closeHandle()
	{
		deleteHandle = true;
	}
	
	private void createHandle(String file_path, String file_name, boolean append)
	{
		try
		{
			files.createFile(file_path, file_name, false);
			output_stream = new FileOutputStream(files.getPluginFolder() + file_path + file_name, append);
			
			Timer timer = new Timer();
			timer.schedule(new TimerTask()
			{
			    @Override
			    public void run()
			    {
			    	if(!write_data_buffer.isEmpty())
			    	{
			    		try
			    		{
			    			output_stream.write(write_data_buffer.remove());
			    		}
			    		catch(IOException e)
			    		{
			    			e.printStackTrace();
			    		}
			    	}
			    	else if(deleteHandle && write_data_buffer.isEmpty())
			    	{
			    		try
			    		{
			    			output_stream.close();
			    			output_stream = null;
			    		}
			    		catch(IOException e)
			    		{
			    			e.printStackTrace();
			    		}
			    		
			    		timer.cancel();
			    	}
			    }
			}, 0, 1);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}
