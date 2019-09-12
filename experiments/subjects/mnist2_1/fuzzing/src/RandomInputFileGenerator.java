import java.util.Random;
import java.nio.file.Files;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class RandomInputFileGenerator {
  
  public static void main(String[] args) {
    
    Random r = new Random();
    
    byte[] data = new byte[28*28*1*Double.BYTES];
    r.nextBytes(data);
    
    File newFile = new File("./in_dir/input");
    try{
      if (!newFile.exists()) {
        newFile.createNewFile();
      }
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
    
    try (FileOutputStream fos = new FileOutputStream(newFile)) {
      
      fos.write(data);
    } catch (IOException ioe) {
      ioe.printStackTrace();
    }
    
    System.out.println("Done.");
  }
}
