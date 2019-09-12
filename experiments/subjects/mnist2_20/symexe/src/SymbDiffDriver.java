import java.io.FileInputStream;
import java.io.IOException;
import gov.nasa.jpf.symbc.Debug;

import static gov.nasa.jpf.symbc.ChangeAnnotation.*;

public class SymbDiffDriver {

    static final int IMG_HEIGHT = 28; /* 28 */
    static final int IMG_WIDTH = 28; /* 28 */

     static final int NUMBER_OF_PIXEL_CHANGE = 156; // 20%
//    static final int NUMBER_OF_PIXEL_CHANGE = 784; // 100%

    public static void main(String[] args) {

        double[][][] a = new double[IMG_HEIGHT][IMG_WIDTH][1];
        
        int[] i_pos_changes = new int[NUMBER_OF_PIXEL_CHANGE];
        int[] j_pos_changes = new int[NUMBER_OF_PIXEL_CHANGE];
        double[] value_changes = new double[NUMBER_OF_PIXEL_CHANGE];

        if (args.length == 1) {

            String fileName = args[0].replace("#", ",");

            // Reading input from fuzzed file.
            try (FileInputStream fis = new FileInputStream(fileName)) {
                /* Read pixel values from [0, 255] and normalize them to [0, 1] */
                byte[] bytes = new byte[1];

                for (int i = 0; i < IMG_HEIGHT; i++) {
                    for (int j = 0; j < IMG_WIDTH; j++) {
                        for (int k = 0; k < 1; k++) {

                            if (fis.read(bytes) == -1) {
                                throw new RuntimeException("Not enough data to read input!");
                            }

                            // /* Add symbolic byte. */
                            // byte byteValue = Debug.addSymbolicByte(bytes[0], "sym_" + i + "_" + j + "_" + k);
                            //
                            // /* Normalize value from [-128,127] to be in range [0, 1] */
                            // double value = (byteValue + 128) / 255.0;
                            //
                            // /* Add double Value */
                            // a[i][j][k] = value;

                            double value = (bytes[0] + 128) / 255.0;
                            a[i][j][k] = Debug.addSymbolicDouble(value, "sym_" + i + "_" + j + "_" + k);

                        }
                    }
                }

                for (int i = 0; i < NUMBER_OF_PIXEL_CHANGE; i++) {
                    if (fis.read(bytes) == -1) {
                        throw new RuntimeException("Not enough data to read input!");
                    }
                    i_pos_changes[i] = Debug.addConstrainedSymbolicInt(Math.floorMod(bytes[0], 28), "sym_ipos_" + i, 0, 27);
                    if (fis.read(bytes) == -1) {
                        throw new RuntimeException("Not enough data to read input!");
                    }
                    j_pos_changes[i] = Debug.addConstrainedSymbolicInt(Math.floorMod(bytes[0], 28), "sym_jpos_" + i, 0, 27);
                    if (fis.read(bytes) == -1) {
                        throw new RuntimeException("Not enough data to read input!");
                    }
                    value_changes[i] = Debug.addSymbolicDouble((bytes[0] + 128) / 255.0, "sym_change_" + i);
                }

            } catch (IOException e) {
                System.err.println("Error reading input");
                e.printStackTrace();
                return;
            }

        } else {
            for (int i = 0; i < IMG_HEIGHT; i++) {
                for (int j = 0; j < IMG_WIDTH; j++) {
                    for (int k = 0; k < 1; k++) {

                        // /* Add pure symbolic byte. */
                        // byte byteValue = Debug.makeSymbolicByte("sym_" + i + "_" + j + "_" + k);
                        //
                        // /* Normalize value from [-128,127] to be in range [0, 1] */
                        // double value = (byteValue + 128) / 255.0;
                        //
                        // /* Add double Value */
                        // a[i][j][k] = value;

                        a[i][j][k] = Debug.makeSymbolicDouble("sym_" + i + "_" + j + "_" + k);

                    }
                }
            }

            for (int i = 0; i < NUMBER_OF_PIXEL_CHANGE; i++) {
                i_pos_changes[i] = Debug.makeConstrainedSymbolicInteger("sym_ipos_" + i, 0, 27);
                j_pos_changes[i] = Debug.makeConstrainedSymbolicInteger("sym_jpos_" + i, 0, 27);
                value_changes[i] = Debug.makeSymbolicDouble("sym_change_" + i);
            }
        }

        /* Read internal data. */
        InternalData internalData = new InternalData();
        internalData.biases0 = Debug.getBiases0();
        internalData.biases2 = Debug.getBiases2();
        internalData.biases6 = Debug.getBiases6();
        internalData.biases8 = Debug.getBiases8();
        internalData.weights0 = Debug.getWeights0();
        internalData.weights2 = Debug.getWeights2();
        internalData.weights6 = Debug.getWeights6();
        internalData.weights8 = Debug.getWeights8();
        
        
        
        /* dummy pc update */
        if (a[0][0][0] > 0) {
            int x = 0;
        }
        
        // Insert changes.
        for (int i = 0; i < NUMBER_OF_PIXEL_CHANGE; i++) {
            int i_pos = i_pos_changes[i];
            int j_pos = j_pos_changes[i];
            double value = value_changes[i];
            a[i_pos][j_pos][0] = change(a[i_pos][j_pos][0], value);
        }

//        Debug.printPC("1 >>");
        
//        DNNt dnn = new DNNt(internalData);
//        int res = dnn.run(a);
        run(internalData, a);

    }
    
    public static int run(InternalData internal, double[][][] input) {
        
//        Debug.printPC("2 >>");

        // layer 0: conv2d_1
        double[][][] layer0 = new double[26][26][2];
        for (int i = 0; i < 26; i++)
            for (int j = 0; j < 26; j++)
                for (int k = 0; k < 2; k++) {
                    layer0[i][j][k] = internal.biases0[k];
                    for (int I = 0; I < 3; I++)
                        for (int J = 0; J < 3; J++)
                            for (int K = 0; K < 1; K++)
                                layer0[i][j][k] += internal.weights0[I][J][K][k] * input[i + I][j + J][K];
                }

        // layer 1: activation_1
        double[][][] layer1 = new double[26][26][2];
        for (int i = 0; i < 26; i++)
            for (int j = 0; j < 26; j++)
                for (int k = 0; k < 2; k++)
                    if (layer0[i][j][k] > 0)
                        layer1[i][j][k] = layer0[i][j][k];
                    else
                        layer1[i][j][k] = 0;
        
        // layer 2: conv2d_2
        double[][][] layer2 = new double[24][24][4];
        for (int i = 0; i < 24; i++)
            for (int j = 0; j < 24; j++)
                for (int k = 0; k < 4; k++) {
                    layer2[i][j][k] = internal.biases2[k];
                    for (int I = 0; I < 3; I++)
                        for (int J = 0; J < 3; J++)
                            for (int K = 0; K < 2; K++)
                                layer2[i][j][k] += internal.weights2[I][J][K][k] * layer1[i + I][j + J][K];
                }

        // layer 3: activation_2
        double[][][] layer3 = new double[24][24][4];
        for (int i = 0; i < 24; i++)
            for (int j = 0; j < 24; j++)
                for (int k = 0; k < 4; k++)
                    if (layer2[i][j][k] > 0)
                        layer3[i][j][k] = layer2[i][j][k];
                    else
                        layer3[i][j][k] = 0;

        // layer 4: max_pooling2d_1
        double[][][] layer4 = new double[12][12][4];
        for (int i = 0; i < 12; i++)
            for (int j = 0; j < 12; j++)
                for (int k = 0; k < 4; k++) {
                    layer4[i][j][k] = 0;
                    for (int I = i * 2; I < (i + 1) * 2; I++)
                        for (int J = i * 2; J < (i + 1) * 2; J++)
                            if (layer3[I][J][k] > layer4[i][j][k])
                                layer4[i][j][k] = layer3[I][J][k];
                }

        // layer 5: flatten_1
        double[] layer5 = new double[576];
        for (int i = 0; i < 576; i++) {
            int d0 = i / 48;
            int d1 = (i % 48) / 4;
            int d2 = i - d0 * 48 - d1 * 4;
            layer5[i] = layer4[d0][d1][d2];
        }

        // layer 6: dense_1
        double[] layer6 = new double[128];
        for (int i = 0; i < 128; i++) {
            layer6[i] = internal.biases6[i];
            for (int I = 0; I < 576; I++)
                layer6[i] += internal.weights6[I][i] * layer5[I];
        }

        // layer 7: activation_3
        double[] layer7 = new double[128];
        for (int i = 0; i < 128; i++)
            if (layer6[i] > 0)
                layer7[i] = layer6[i];
            else
                layer7[i] = 0;

        // layer 8: dense_2
        double[] layer8 = new double[10];
        for (int i = 0; i < 10; i++) {
            layer8[i] = internal.biases8[i];
            for (int I = 0; I < 128; I++)
                layer8[i] += internal.weights8[I][i] * layer7[I];
        }

        // layer 9: activation_4
        int ret = 0;
        double res = -100000;
        for (int i = 0; i < 10; i++) {
            if (layer8[i] > res) {
                res = layer8[i];
                ret = i;
            }
        }
        return ret;
    }
}
