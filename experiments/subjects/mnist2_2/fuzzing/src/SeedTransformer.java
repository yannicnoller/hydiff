import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

public class SeedTransformer {

    public static void main(String[] args) {

        if (args.length < 2) {
            System.err.println("Please specify the input folder and output folder!");
            return;
        }

        String inputFolderPath = args[0];
        String outputFolderPath = args[1];

        File inputFolder = new File(inputFolderPath);
        File outputFolder = new File(outputFolderPath);
        outputFolder.mkdirs();

        for (File file : inputFolder.listFiles()) {
            byte[] bytes = new byte[28 * 28 * 1];

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                int i = 0;
                while ((line = br.readLine()) != null) {
                    bytes[i++] = (byte) (Integer.valueOf(line.trim())-128);
                }
                
                File outputFile = new File(outputFolderPath + "/" + file.getName());
                outputFile.createNewFile();
                
                try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                    fos.write(bytes);
                }
                
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        
        System.out.println("Done.");

    }
}
