
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class generateCsv {

	public static void main(String[] args, ArrayList<CompoundHelix> predCompHelices) {
		   generateCsvFile("C:/Users/Vishal/Desktop/16s Pi predictions/ E coli 16s predicted compound helices.csv", predCompHelices); 
	}
	
	private static void generateCsvFile(String sFileName, ArrayList<CompoundHelix> predCompHelices) {
		try	{
			FileWriter writer = new FileWriter(sFileName);
			
			writer.append("Compound Helix Name");
		    writer.append(',');
		    writer.append("Hairpin Loop Seq");
		    writer.append(',');
		    writer.append("Hairpin Loop Length");
		    writer.append(',');
		    writer.append("5_end 4 bp");
		    writer.append(',');
		    writer.append("3_end 4 bp");
		    writer.append(',');
		    writer.append("Total Compound Helix ME");
		    writer.append(',');
		    writer.append("Total Compound Helix Length");
    		writer.append(',');
    		writer.append("Total Compound Paired Nucleotides");
    		writer.append(',');
    		writer.append("Ratio of Paired/Total Compound Helix Length");
		    writer.append(',');
    		writer.append("Total Unpaired Nucleotides 5' ");
    		writer.append(',');
    		writer.append("Total Unpaired Nucleotides 3' ");
    		writer.append(',');
		    writer.append("Total Conditional Distance");
		    writer.append(',');
		    writer.append("Helix Name");
		    writer.append(',');
		    writer.append("5_Begin");
		    writer.append(',');
		    writer.append("5_End");
		    writer.append(',');
		    writer.append("3_Begin");
		    writer.append(',');
		    writer.append("3_End");
		    writer.append(',');
		    writer.append("Helix Modifed Energy");
		    writer.append(',');
		    writer.append("Helix Conditional Distance");
		    writer.append('\n');
		    
		    for(CompoundHelix compHelix: predCompHelices) {
		    	for(PeHelicies helix: compHelix.getAllHelicies()) {
		    		writer.append(compHelix.getName());
		    		writer.append(',');
		    		writer.append(compHelix.getHairpinSeq());
		    		writer.append(',');
		    		writer.append(Integer.toString(compHelix.getHairpinSeq().length()));
		    		writer.append(',');
		    		writer.append(compHelix.get5End4BPSeq());
				    writer.append(',');
				    writer.append(compHelix.get3End4BPSeq());
				    writer.append(',');
		    		writer.append(Double.toString(compHelix.getCompHelixEnergy()));
		    		writer.append(',');
		    		writer.append(Integer.toString(compHelix.getTotalHelixLength()));
		    		writer.append(',');
		    		writer.append(Integer.toString(compHelix.getTotalNumPairedNucleotides()));
		    		writer.append(',');
		    		writer.append(Double.toString((1.0*compHelix.getTotalNumPairedNucleotides())/(1.0*compHelix.getTotalHelixLength())));
		    		writer.append(',');
		    		writer.append(Integer.toString(compHelix.getUnpairedDist5()));
		    		writer.append(',');
		    		writer.append(Integer.toString(compHelix.getUnpairedDist3()));
		    		writer.append(',');
				    writer.append(Integer.toString(compHelix.getTotalCD()));
				    writer.append(',');
		    		writer.append(helix.getName());
				    writer.append(',');
				    writer.append(Integer.toString(helix.getPre5()));
				    writer.append(',');
				    writer.append(Integer.toString(helix.getEnd5()));
				    writer.append(',');
				    writer.append(Integer.toString(helix.getPre3()));
				    writer.append(',');
				    writer.append(Integer.toString(helix.getEnd3()));
				    writer.append(',');
				    writer.append(Double.toString(helix.getHelixME()));
				    writer.append(',');
				    writer.append(Integer.toString(helix.getCD()));
				    writer.append('\n');
		    	}

		    }
		    				
		    writer.flush();
		    writer.close();
			}
		catch(IOException e)	{
		     e.printStackTrace();
			} 
	    }	

}
