import java.util.ArrayList;

//checks the correctness of the predicted helicies with the actual helicies
public class checkPredictedHelicies {
	
	//ArrayList of the prediced compound helicies
	private ArrayList<CompoundHelix> predictedCompoundHeliciesList;
	//ArrayList of the correct helicies
	private ArrayList<CompoundHelix> correctCompoundHeliciesList;
	private String rnaSeq;
	
	//constructor
	public checkPredictedHelicies(ArrayList<CompoundHelix> predCompHelicies, ArrayList<CompoundHelix> correctCompHelicies, String seq) {
		predictedCompoundHeliciesList = predCompHelicies;
		correctCompoundHeliciesList = correctCompHelicies;
		rnaSeq = seq;
	}
	
	//prints the correctness of the helicies
	public void printCorrectness() {
		
		double totalHelicies = 0;
		double correctHelicies = 0;
		int numPredictedHelicies = 0;
		int totalPredictedPiHelicies = 0;
		int correctPiHelicies = 0;
		
		for(int i=0; i<predictedCompoundHeliciesList.size(); i++) {
			System.out.println(predictedCompoundHeliciesList.get(i).getName());
			numPredictedHelicies++;
			if(predictedCompoundHeliciesList.get(i).getSize() == correctCompoundHeliciesList.get(i).getSize()) {
				System.out.println("Sizes of compound helicies: CORRECT");
			}
			else{ 
				System.out.println("Sizes of compound helicies: INCORRECT, size of predicted: " + predictedCompoundHeliciesList.get(i).getSize() + " size of actual: " +  correctCompoundHeliciesList.get(i).getSize());
			}
			
			
			//checks to see correctness of each helix within the compound helix, does not count initial Pa helicies since it is a given
			//messed up here to need to change variable j to a name
			int minLen = Math.min(predictedCompoundHeliciesList.get(i).getSize(), correctCompoundHeliciesList.get(i).getSize());
			for(int j=1; j<minLen; j++) {
				totalHelicies++;
				if(predictedCompoundHeliciesList.get(i).getHelix(j).getName().contains("P"))
					totalPredictedPiHelicies++;
				if(predictedCompoundHeliciesList.get(i).getHelix(j).isEqual(correctCompoundHeliciesList.get(i).getHelix(j))) {
					System.out.print(predictedCompoundHeliciesList.get(i).getHelix(j).getName() + ": CORRECT ");
					correctHelicies++;
					if(predictedCompoundHeliciesList.get(i).getHelix(j).getName().contains("P"))
						correctPiHelicies++;
				}
				else {
					System.out.print(predictedCompoundHeliciesList.get(i).getHelix(j).getName() + ": INCORRECT ");

				}
			}
			
			System.out.println("\n");
			
		}
		double percentCorrect = (Math.round((correctHelicies/totalHelicies)*100));
		
		System.out.println("Number of predicited helicies: " + numPredictedHelicies);
		System.out.println("Correct Helicies: " + correctHelicies);
		System.out.println("Percent of the helicies are present that are correctly predicited: " + percentCorrect);
		System.out.println("Total Predicted Pi Helicies: " + totalPredictedPiHelicies);
		System.out.println("Correct PiHelicies: " + correctPiHelicies);
	}
	//generates the corrected on a per nucleotide basis and returns it
	public double generateNucleotideCorrectness() {
		
		double numCorrect = 0.0;
		double numTotal = 0.0;
		
		int[] predPairedBases = new int[rnaSeq.length()];
		int[] correctPairedBases = new int[rnaSeq.length()];
		
		for(CompoundHelix predCompHelix: predictedCompoundHeliciesList) {
			for(PeHelicies predHelix: predCompHelix.getAllHelicies()) {
				int count = 0;
				//adds in pairs for 5' end
				for(int i = predHelix.getPre5() ; i <= predHelix.getEnd5(); i++) {
					predPairedBases[i] = predHelix.getEnd3()  - count;
					count++;
				}
				//adds in pairs for 3' end
				count = 0;
				for(int i = predHelix.getPre3() ; i <= predHelix.getEnd3(); i++) {
					predPairedBases[i] = predHelix.getEnd5() - count;
					count++;
				}
				
			}
		}
		
		for(CompoundHelix correctCompHelix: correctCompoundHeliciesList) {
			for(PeHelicies correctHelix: correctCompHelix.getAllHelicies()) {
				int count = 0;
				//adds in pairs for 5' end
				for(int i = correctHelix.getPre5() ; i <= correctHelix.getEnd5(); i++) {
					correctPairedBases[i] = correctHelix.getEnd3()  - count;
					count++;
				}
				//adds in pairs for 3' end
				count = 0;
				for(int i = correctHelix.getPre3() ; i <= correctHelix.getEnd3(); i++) {
					correctPairedBases[i] = correctHelix.getEnd5() - count;
					count++;
				}
				
			}
		}
		
		
		for(int i=0; i<predPairedBases.length; i++) {
			if(predPairedBases[i] > 0) {
				numTotal++;
				if(predPairedBases[i] == correctPairedBases[i])
					numCorrect++;
			}
			
		}
		
		//numTotal = numTotal/2;
		//numCorrect = numCorrect/2;
		
		System.out.println("Total Number of Paired Bases: " + numTotal);
		System.out.println("Number of Correct Paired Bases: " + numCorrect);
		
		return (Math.round(numCorrect/numTotal * 100));
		
		
		
		

		
	}
	
	
	
	
	
}
