import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Random;

import javax.imageio.ImageIO;

public class InputGenerator {

    public static byte[] extractBytes(String ImageName) {
        try {
            byte[] imageInByte;
            BufferedImage originalImage = ImageIO.read(new File(ImageName));

            // convert BufferedImage to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(originalImage, "jpg", baos);
            baos.flush();
            imageInByte = baos.toByteArray();
            baos.close();
            return (imageInByte);
        } catch (IOException e) {
            e.printStackTrace();
            return (null);
        }
    }

    public static byte[] createImagePasscode(byte[] image) {
        try {
            System.out.println("Creating passcode");
            ScalrApplyTest b = new ScalrApplyTest();
            ScalrApplyTest.setup(image);
            BufferedImage p = b.testApply1();
            int r = p.getWidth();
            int h = p.getHeight();
            int[] imageDataBuff = p.getRGB(0, 0, r, h, (int[]) null, 0, r);
            ByteBuffer byteBuffer = ByteBuffer.allocate(imageDataBuff.length * 4);
            IntBuffer intBuffer = byteBuffer.asIntBuffer();
            intBuffer.put(imageDataBuff);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(byteBuffer.array());
            baos.flush();
            baos.close();
            System.out.println("Image Done");
            ScalrApplyTest.tearDown();
            byte[] pcodetest = new byte[128];
            int csize = imageDataBuff.length / 128;
            int ii = 0;

            for (int i1 = 0; i1 < csize * 128; i1 += csize) {
                pcodetest[ii] = (byte) (imageDataBuff[i1] % 2);
                ++ii;
            }
            return (pcodetest);
        } catch (Exception var15) {
            System.out.println("worker ended, error: " + var15.getMessage());
            return (null);
        }
    }

    public static void firstInput(String ImageName) {
        byte[] image = extractBytes(ImageName);

        byte[] passcode = new byte[128];
        new Random().nextBytes(passcode); // createImagePasscode(image);
        byte[] passcode2 = new byte[128];
        new Random().nextBytes(passcode2);

        try (FileOutputStream fos = new FileOutputStream("in_dir/example.txt")) {
            fos.write(passcode);
            fos.write(passcode); // take twice the same secret to get zero delta
            // fos.write(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // try (FileOutputStream fos = new FileOutputStream("data/public-only")) {
        // fos.write(image);
        // } catch (IOException e) {
        // e.printStackTrace();
        // }
    }

    public static void main(String[] args) {
        firstInput("/Users/yannic/image.jpg");
    }

}
