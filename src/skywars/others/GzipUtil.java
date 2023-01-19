package skywars.others;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
  
public class GzipUtil {
  
  public static byte[] zip(final String str) {
    if ((str == null) || (str.length() == 0)) {
      throw new IllegalArgumentException("Cannot zip null or empty string");
    }
  
    try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
      try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
        gzipOutputStream.write(str.getBytes(StandardCharsets.UTF_8));
      }
      return byteArrayOutputStream.toByteArray();
    } catch(IOException e) {
      throw new RuntimeException("Failed to zip content", e);
    }
  }
  
  public static String unzip(final byte[] compressed) {
    if ((compressed == null) || (compressed.length == 0)) {
      throw new IllegalArgumentException("Cannot unzip null or empty bytes");
    }
    if (!isZipped(compressed)) {
      return new String(compressed);
    }
  
    try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(compressed)) {
      try (GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream)) {
        try (InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream, StandardCharsets.UTF_8)) {
          try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            String output = "";
            String line;
            while((line = bufferedReader.readLine()) != null){
            	output+= "\n" + line;
            }
            return output.trim();
          }
        }
      }
    } catch(IOException e) {
      throw new RuntimeException("Failed to unzip content", e);
    }
  }
  
  public static boolean isZipped(final byte[] compressed) {
    return (compressed[0] == (byte) (GZIPInputStream.GZIP_MAGIC)) 
           && (compressed[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8));
  }
}