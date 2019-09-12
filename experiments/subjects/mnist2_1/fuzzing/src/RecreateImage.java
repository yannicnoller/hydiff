import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import javax.imageio.ImageIO;

public class RecreateImage {
    
    static final int IMG_HEIGHT = 28; /* 28 */
    static final int IMG_WIDTH = 28; /* 28 */
    static final int NUMBER_OF_PIXEL_CHANGE = 7;

    public static void main(String[] args) {

//        String fileName = "/Users/yannic/Downloads/hydiff-experiment-results/mnist2_1/fuzzing/in_dir/input";
        String fileName ="/Users/yannic/Downloads/hydiff-experiment-results/mnist2_1/fuzzer-out-1/afl/queue/id:000003,src:000000,op:havoc,rep:32,+odiff,+ddiff";
        BufferedImage bf_orig = new BufferedImage(IMG_HEIGHT, IMG_HEIGHT, BufferedImage.TYPE_BYTE_GRAY);
        BufferedImage bf_changed = new BufferedImage(IMG_HEIGHT, IMG_HEIGHT, BufferedImage.TYPE_BYTE_GRAY);
        
        int[][] src_orig = new int[IMG_HEIGHT][IMG_HEIGHT];
        int[][] src_changed = new int[IMG_HEIGHT][IMG_HEIGHT];
        
        byte[] bytes = new byte[1];
        try (FileInputStream fis = new FileInputStream(fileName)) {
            for (int i = 0; i < IMG_HEIGHT; i++) {
                for (int j = 0; j < IMG_WIDTH; j++) {
                    if (fis.read(bytes) == -1) {
                        throw new RuntimeException("Not enough data to read input!");
                    }
                    src_orig[i][j] = bytes[0] + 128;
                    src_changed[i][j] = bytes[0] + 128;
                    bf_orig.setRGB(i, j, bytes[0]);
                    bf_changed.setRGB(i, j, bytes[0]);
                }
            }
            
            for (int i = 0; i < NUMBER_OF_PIXEL_CHANGE; i++) {
                if (fis.read(bytes) == -1) {
                    throw new RuntimeException("Not enough data to read input!");
                }
                int i_pos = Math.floorMod(bytes[0], IMG_HEIGHT);
                if (fis.read(bytes) == -1) {
                    throw new RuntimeException("Not enough data to read input!");
                }
                int j_pos = Math.floorMod(bytes[0], IMG_WIDTH);
                if (fis.read(bytes) == -1) {
                    throw new RuntimeException("Not enough data to read input!");
                }
                int value = bytes[0] + 128;
                bf_changed.setRGB(i_pos, j_pos, value);
                System.out.println("changed txt [" + i_pos + ", " + j_pos + "]: " + src_changed[i_pos][j_pos] + " -> " + value);
                src_changed[i_pos][j_pos] = value;
            }

            File f_orig = new File("/Users/yannic/image_orig.jpg");
            f_orig.createNewFile();
            ImageIO.write(bf_orig, "jpg", f_orig);
            
            File f_changed = new File("/Users/yannic/image_changed.jpg");
            f_changed.createNewFile();
            ImageIO.write(bf_changed, "jpg", f_changed);
            
            File f_src_orig = new File("/Users/yannic/image_src_orig.txt");
            FileWriter fw_src_orig = new FileWriter(f_src_orig);
            BufferedWriter bw = new BufferedWriter(fw_src_orig);
            StringBuffer src_orig_content = new StringBuffer();
            for (int i=0; i<IMG_HEIGHT; i++) {
                for (int j=0; j<IMG_WIDTH; j++) {
                    src_orig_content.append(src_orig[i][j]);
                    src_orig_content.append(" ");
                } 
            }
            bw.write(src_orig_content.toString());
            bw.close();
            
            File f_src_changed = new File("/Users/yannic/image_src_changed.txt");
            FileWriter fw_src_changed = new FileWriter(f_src_changed);
            bw = new BufferedWriter(fw_src_changed);
            StringBuffer src_changed_content = new StringBuffer();
            for (int i=0; i<IMG_HEIGHT; i++) {
                for (int j=0; j<IMG_WIDTH; j++) {
                    src_changed_content.append(src_changed[i][j]);
                    src_changed_content.append(" ");
                } 
            }
            bw.write(src_changed_content.toString());
            bw.close();

            System.out.println("Files were written succesfully.");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
