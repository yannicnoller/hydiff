import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;

public class InternalData {

  public double[][][][] weights0;
  public double[][][][] weights2;
  public double[][] weights6;
  public double[][] weights8;
  
  public double[] biases0;
  public double[] biases2;
  public double[] biases6;
  public double[] biases8;
  
  private InternalData(){}
  
  public static void main(String[] args) {
    // testing
    
    InternalData internal = createFromDataFiles("./data");
    
    System.out.println("weights2");
    System.out.println(internal.weights2[2][2][0][3]);
    
    System.out.println("Done.");
  }
  
  public static InternalData createFromDataFiles(String path) {
    InternalData data = new InternalData();
    
    // Read biases0
    File fileBiases0 = new File(path + "/biases0.txt");
    data.biases0 = new double[2];
    try (BufferedReader br = new BufferedReader(new FileReader(fileBiases0))) {
      String line;
      int i=0;
      while ((line = br.readLine()) != null) {
        data.biases0[i] = Double.valueOf(line.trim());
        i++;
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    
    // Read biases2
    File fileBiases2 = new File(path + "/biases2.txt");
    data.biases2 = new double[4];
    try (BufferedReader br = new BufferedReader(new FileReader(fileBiases2))) {
      String line;
      int i=0;
      while ((line = br.readLine()) != null) {
        data.biases2[i] = Double.valueOf(line.trim());
        i++;
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    
    // Read biases6
    File fileBiases6 = new File(path + "/biases6.txt");
    data.biases6 = new double[128];
    try (BufferedReader br = new BufferedReader(new FileReader(fileBiases6))) {
      String line;
      int i=0;
      while ((line = br.readLine()) != null) {
        data.biases6[i] = Double.valueOf(line.trim());
        i++;
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    
    // Read biases8
    File fileBiases8 = new File(path + "/biases8.txt");
    data.biases8 = new double[10];
    try (BufferedReader br = new BufferedReader(new FileReader(fileBiases8))) {
      String line;
      int i=0;
      while ((line = br.readLine()) != null) {
        data.biases8[i] = Double.valueOf(line.trim());
        i++;
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    
    // Read weights0
    File fileWeights0 = new File(path + "/weights0.txt");
    data.weights0 = new double[3][3][1][2];
    try (BufferedReader br = new BufferedReader(new FileReader(fileWeights0))) {
      String line;
      for (int i=0; i<3; i++) {
        for (int j=0; j<3; j++) {
          for (int k=0; k<1; k++) {
            line = br.readLine();
            String[] items = line.split(",");
            for (int l=0; l<2; l++) {
              data.weights0[i][j][k][l] = Double.valueOf(items[l].trim());
            }
          }
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    
    // Read weights2
    File fileWeights2 = new File(path + "/weights2.txt");
    data.weights2 = new double[3][3][2][4];
    try (BufferedReader br = new BufferedReader(new FileReader(fileWeights2))) {
      String line;
      for (int i=0; i<3; i++) {
        for (int j=0; j<3; j++) {
          for (int k=0; k<2; k++) {
            line = br.readLine();
            String[] items = line.split(",");
            for (int l=0; l<4; l++) {
              data.weights2[i][j][k][l] = Double.valueOf(items[l].trim());
            }
          }
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    
    // Read weights6
    File fileWeights6 = new File(path + "/weights6.txt");
    data.weights6 = new double[576][128];
    try (BufferedReader br = new BufferedReader(new FileReader(fileWeights6))) {
      String line;
      int i=0;
      while ((line = br.readLine()) != null) {
        String[] items = line.split(",");
        for (int j=0; j<items.length; j++) {
          data.weights6[i][j] = Double.valueOf(items[j].trim());
        }
        i++;
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    
    // Read weights8
    File fileWeights8 = new File(path + "/weights8.txt");
    data.weights8 = new double[128][10];
    try (BufferedReader br = new BufferedReader(new FileReader(fileWeights8))) {
      String line;
      int i=0;
      while ((line = br.readLine()) != null) {
        String[] items = line.split(",");
        for (int j=0; j<items.length; j++) {
          data.weights8[i][j] = Double.valueOf(items[j].trim());
        }
        i++;
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    
    
    return data;
  }
}
