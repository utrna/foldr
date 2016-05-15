import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Scanner;


public class csvReader {
	
    public static void main(String[] args) throws IOException {
    	
    	String path = "C:/Users/Vishal/Desktop/16s Pi predictions/ 16sPotentialPiHelices.pi";
    	File piFile = new File(path);
    	piFile.createNewFile();
    	    	
    	
    	FileOutputStream fos = new FileOutputStream(piFile);
    	BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

    	
        Scanner scanner = new Scanner(new File("C:/Users/Vishal/Desktop/16s Pi predictions/Hairpin Loops le 15 - 2014-01-02-A.csv "));
        scanner.useDelimiter(",");
        //skip first line of file
        scanner.nextLine();
       while(scanner.hasNextLine()) {
        // while(scanner.hasNext()){
        	//System.out.print(scanner.next()+"|");
    	   
    	   //get 5' start
    	   int beg5 = scanner.nextInt();
    	   //get 5' end
    	   int end5 = scanner.nextInt();
    	   //get 3' start
    	   int beg3 = scanner.nextInt();
    	   //get 3' end
    	   int end3 = scanner.nextInt();
    	   //get energy
    	   scanner.nextInt();
    	   scanner.nextInt();
    	   double energy = scanner.nextDouble();
    	   bw.write("" + beg5 + "   " + end5 + "   " + beg3 + "   " + end3);
    	   bw.newLine();
    	   scanner.nextLine();
        }
        scanner.close();
        bw.close();
        
    }

}
