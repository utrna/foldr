import java.util.ArrayList;


public class CompoundHelix {


	//name of the compound helix
	private String name;
	//array list of the helicies that make up the compound helix
	private ArrayList<PeHelicies> heliciesList;
	//number of helicies that make up the compound helix
	private int numHelicies;
	//the average distance between helicies on the 5' end and the 3' end
	double dist5Avg = 0;
	double dist3Avg = 0;
	//average conditional distance
	double avgCondDist = 0;
	String rnaSeq = "";
	//average percent composition of nucleotides for the 5' and 3' seq
	private double avgCompA5 = 0;
	private double avgCompC5 = 0;
	private double avgCompG5 = 0;
	private double avgCompU5 = 0;
	private double avgCompA3 = 0;
	private double avgCompC3 = 0;
	private double avgCompG3 = 0;
	private double avgCompU3 = 0;
	//max distance between helicies on the 5' end and the 3'end in a compound helix
	private int maxDist5 = 0;
	private int maxDist3 = 0;
	private double compHelixEnergy;

	
	//constructor
	public CompoundHelix(String seq) {
		heliciesList = new ArrayList<PeHelicies>();
		rnaSeq = seq;
	}
	
	//sets the name of the compound helix
	public void setName(String nameOfCompHelix) {
		name = nameOfCompHelix;
	}
	
	//add helix to the compound helix
	public void addHelix(PeHelicies helixToAdd) {
		heliciesList.add(helixToAdd);
		numHelicies = heliciesList.size();
		//compHelixEnergy += helixToAdd.getHelixEnergy();
	}
	//gets the size of the compound helix
	public int getSize() {
		return numHelicies;
	}
	//gets the name of the compound helix
	public String getName() {
		return name;
	}
	//gets the number of helicies that make up the compound helix
	public int getNumHelicies() {
		return numHelicies;
	}
	
	//statistical methods for a compound helix, prints to stdout
	public void genDistances() {
		System.out.println("Compound Helix: " + name);
		
		//distance between the helicies on the 5' end and 3' end
		int dist5 = 0;
		int dist3 = 0;
		
		//if there is only 1 helix in the compound helix the distance is 0
		if(numHelicies ==1 )
			System.out.println("Distance = 0");
		else {
			//get the distance between helicies on the 5' side and the 3' side
			for(int i = 1; i < numHelicies; i++) {
				
				dist5 = distBetweenHelicies5(heliciesList.get(i-1), heliciesList.get(i));
				dist5Avg += dist5;
				dist3 = distBetweenHelicies3(heliciesList.get(i-1), heliciesList.get(i));
				dist3Avg += dist3;
				avgCondDist += (dist3 + dist5);
				
				if(dist5 > maxDist5)
					maxDist5 = dist5;
				if(dist3 > maxDist3)
					maxDist3 = dist3;
				
				System.out.print("" + heliciesList.get(i-1).getName() + "-" + heliciesList.get(i).getName() + " 5' distance: " + dist5 + " || 3' distance: " + dist3 + " || CD: " + (dist3 + dist5) + "  ||  " );
				String seq5 = get5EndSeq(heliciesList.get(i-1),heliciesList.get(i));
				String seq3 = get3EndSeq(heliciesList.get(i-1),heliciesList.get(i));
				String seq5Comp = ("A%: " + getComposition(seq5)[0] + " C%: " + getComposition(seq5)[1]  + " G%: " + getComposition(seq5)[2]  + " U%: " + getComposition(seq5)[3]);
				String seq3Comp = ("A%: " + getComposition(seq3)[0] + " C%: " + getComposition(seq3)[1]  + " G%: " + getComposition(seq3)[2]  + " U%: " + getComposition(seq3)[3]);
				System.out.printf("%-12s%-12s%-6s%-12s%-16s%-16s%-16s%-16s%s\n","5' end RNA sequence: ",seq5,"3' end RNA sequence: ",seq3, " || ", "5' sequence composition: ", seq5Comp, "  3' sequence composition: ", seq3Comp);
				
				avgCompA5 += getComposition(seq5)[0];
				avgCompC5 += getComposition(seq5)[1];
				avgCompG5 += getComposition(seq5)[2];
				avgCompU5 += getComposition(seq5)[3];
				avgCompA3 += getComposition(seq3)[0];
				avgCompC3 += getComposition(seq3)[1];
				avgCompG3 += getComposition(seq3)[2];
				avgCompU3 += getComposition(seq3)[3];
				
			}
			if(numHelicies > 1) {
				dist5Avg = dist5Avg/(numHelicies-1);
				dist5Avg = Math.round(dist5Avg*100);
				dist5Avg = dist5Avg/100 ;
				dist3Avg = dist3Avg/(numHelicies-1);
				dist3Avg = Math.round(dist3Avg*100);
				dist3Avg = dist3Avg/100 ;
				avgCondDist = avgCondDist/(numHelicies-1);
				avgCondDist = Math.round(avgCondDist*100);
				avgCondDist = avgCondDist/100 ;
				
				avgCompA5 = avgCompA5/(numHelicies-1);
				avgCompA5 = Math.round(avgCompA5*100);
				avgCompA5 = avgCompA5/100 ;
				avgCompC5 = avgCompC5/(numHelicies-1);
				avgCompC5 = Math.round(avgCompC5*100);
				avgCompC5 = avgCompC5/100 ;
				avgCompG5 = avgCompG5/(numHelicies-1);
				avgCompG5 = Math.round(avgCompG5*100);
				avgCompG5 = avgCompG5/100 ;
				avgCompU5 = avgCompU5/(numHelicies-1);
				avgCompU5 = Math.round(avgCompU5*100);
				avgCompU5 = avgCompU5/100 ;
				
				avgCompA3 = avgCompA3/(numHelicies-1);
				avgCompA3 = Math.round(avgCompA3*100);
				avgCompA3 = avgCompA3/100 ;
				avgCompC3 = avgCompC3/(numHelicies-1);
				avgCompC3 = Math.round(avgCompC3*100);
				avgCompC3 = avgCompC3/100 ;
				avgCompG3 = avgCompG3/(numHelicies-1);
				avgCompG3 = Math.round(avgCompG3*100);
				avgCompG3 = avgCompG3/100 ;
				avgCompU3 = avgCompU3/(numHelicies-1);
				avgCompU3 = Math.round(avgCompU3*100);
				avgCompU3 = avgCompU3/100 ;
				
			}
			
			System.out.println("Average:  5' distance: " + dist5Avg + " | 3' distance: " + dist3Avg + " | CD: " + avgCondDist);
			System.out.println("Average:  5' composition: A: " + avgCompA5 + "% C: " + avgCompC5 + "% G: " + avgCompG5 + "% U: " + avgCompU5 + "  ||  " + "3' composition: A: " + avgCompA3 + "% C: " + avgCompC3 + "% G: " + avgCompG3 + "% U: " + avgCompU3);
			System.out.println("Average:     composition: A: " + ((avgCompA5 + avgCompA3)/2) + "% C: " + ((avgCompC5 + avgCompC3)/2) + "% G: " + ((avgCompG5 + avgCompG3)/2) + "% U: " + ((avgCompU5 + avgCompU3)/2) + "%");
		}
		
		
		
	}
	
	//generates the distance between 2 helicies on the 5' end
	public static int distBetweenHelicies5(PeHelicies helixA, PeHelicies helixB) {
		return Math.abs((helixA.getPre5() - helixB.getEnd5())-1);
	}
	//generates the distance between 2 helicies on the 3' end
	public static int distBetweenHelicies3(PeHelicies helixA, PeHelicies helixB) {
		return Math.abs((helixB.getPre3() - helixA.getEnd3())-1);
	}
	//gets the average distance between helicies on the 5' end
	public double getAvgDist5() {
		return dist5Avg;
	}
	//gets the average distance between helicies on the 3' end
	public double getAvgDist3() {
		return dist3Avg;
	}
	//gets the average conditional distance 
	public double getAvgCondDist() {
		return avgCondDist;
	}
	
	//gets the 5' end RNA sequence between 2 helicies
	public String get5EndSeq(PeHelicies helixA, PeHelicies helixB) {
		//get the sequence between helixA and helixB on the 5' end 
		String seq5 = rnaSeq.substring(Math.min(helixB.getEnd5(), helixA.getPre5()-1), Math.max(helixB.getEnd5(), helixA.getPre5()-1));
		return seq5;
	}
	
	
	
	//gets the 3' RNA sequence between 2 helicies
	public String get3EndSeq(PeHelicies helixA, PeHelicies helixB) {
		//get the sequence between helixA and helixB on the 5' end 
		String seq3 = rnaSeq.substring(Math.min(helixA.getEnd3(), helixB.getPre3()-1), Math.max(helixA.getEnd3(), helixB.getPre3()-1));
		return seq3;
	}
	
	
	//generate the percent composition of nucleotides (A,C,U,G) given a sequence 
	public double[] getComposition(String seq) {
		
		double countA = 0;
		double countC = 0;
		double countU = 0;
		double countG = 0;
		
		for(int i=0; i<seq.length(); i++) {
			if(seq.charAt(i) == 'A')
				countA++;
			if(seq.charAt(i) == 'C')
				countC++;
			if(seq.charAt(i) == 'G')
				countG++;
			if(seq.charAt(i) == 'U')
				countU++;
		}
		
		countA = countA/seq.length();
		countA = Math.round(countA*100);
		countC = countC/seq.length();
		countC = Math.round(countC*100);
		countG = countG/seq.length();
		countG = Math.round(countG*100);
		countU = countU/seq.length();
		countU = Math.round(countU*100);

		//array containing the percentage compositions of A,C,G,U, in pos 0,1,2,3, respectivley, pos 4 contains the printable version of the compositions
		double comp[] = {countA,countC,countG,countU};
		
		//return ("A%: " + countA + " C%: " + countC + " G%: " + countG + " U%: " + countU);
		return comp;
	}
	
	
	//gets the average percent compositions for the entire compound helix for the 5' end and the 3' end 
	//returns an array with the compositions A,C,G,U, at pos 0,1,2,3
	public double[] getAvgComp5() {
		double avgComp5[] = {avgCompA5, avgCompC5, avgCompG5, avgCompU5} ;
		return avgComp5;
	}	
	public double[] getAvgComp3() {
		double avgComp3[] = {avgCompA3, avgCompC3, avgCompG3, avgCompU3} ;
		return avgComp3;
	}
	
	//gets the max distance between helicies on 5' end and the 3' end
	public int getMaxDist5() {
		return maxDist5;
	}
	public int getMaxDist3() {
		return maxDist3;
	}
	//gets a helix given a number
	public PeHelicies getHelix(int n) {
		return heliciesList.get(n);
	}
	//gets a helix given a name
	public PeHelicies getHelix(String name) {
		for(PeHelicies helix: heliciesList) {
			if(helix.getName()==name)
				return helix;
		}
		return null;
	}
	//prints all the helicies in the compound helix
	public void printCompHelix() {
		System.out.println("Ratio: "+ ((double)(this.getTotalNumPairedNucleotides())/(double)(this.getTotalHelixLength())));
		for(PeHelicies helix: heliciesList) {
			System.out.println(helix.getName());
			System.out.println(helix.getHelixME());
			helix.printSequence();
		}
	}
	//returns the energy of the entire compound helix
	public double getCompHelixEnergy() {
		double totalME = 0.0;
		for(PeHelicies helix: heliciesList) {
			totalME += helix.getHelixME();
		}
		return totalME;
	}
	//replaces a helix in the compound helix with one passed in as an argument
	public void replaceHelix(int n , PeHelicies helix) {
		heliciesList.set(n, helix);
	}
	//removes a helix
	public void removeHelix(int n) {
		heliciesList.remove(n);
		numHelicies --;
	}
	//returns all the helicies as an ArrayList
	public ArrayList<PeHelicies> getAllHelicies() {
		return heliciesList;
	}
	//decreases the size of the helix by one
	public void decreaseSizeByOne() {
		numHelicies--;
	}
	//returns total conditional distance of the helix, all the CDs' added together
	public int getTotalCD() {
		int totalCD = 0;
		for(PeHelicies helix: heliciesList) {
			totalCD += helix.getCD();
		}
		return totalCD;
	}
	//returns the total length of the helix in basepairs
	public int getTotalHelixLength() {
		//5' sequence length
		int length5 = 0;
		//3' sequence length
		int length3 = 0;		
		length5 = heliciesList.get(0).getEnd5() - heliciesList.get(heliciesList.size()-1).getPre5();
		length3 =  heliciesList.get(heliciesList.size()-1).getEnd3() - heliciesList.get(0).getPre3();
		return length5+length3+2;

	}
	//get total number of paired nucleotides
	public int getTotalNumPairedNucleotides() {
		int totalPairedNucleotides = 0;
		for(PeHelicies helix: heliciesList) {
			totalPairedNucleotides += helix.getTotalPairedNuc();
		}
		return totalPairedNucleotides;
	}
	//get hairpin loop sequence {
	public String getHairpinSeq() {
		return rnaSeq.substring(heliciesList.get(0).getEnd5(), heliciesList.get(0).getPre3()-1);
	}
	//get the total distance between helices for the 5' sequence
	public int getUnpairedDist5() {
		int dist5 = 0;
		for(int i = 1; i < numHelicies; i++) {
			dist5 = distBetweenHelicies5(heliciesList.get(i-1), heliciesList.get(i));
		}
		return dist5;
	}
	//get the total distance between helices for the 5' sequence
	public int getUnpairedDist3() {
		int dist3 = 0;
		for(int i = 1; i < numHelicies; i++) {
			dist3 = distBetweenHelicies3(heliciesList.get(i-1), heliciesList.get(i));
		}
		return dist3;
	}
 	//get the four base pair on the 5' end of the sequence, 2 from the first part of the Pa helix, and 2 from the first part of the hairpin loop sequence
	public String get5End4BPSeq() {
		int start5 = heliciesList.get(0).getEnd5()-2;
		int end5 = heliciesList.get(0).getEnd5()+2;
		//if the start5 is less than 0
		/*
		if(start5 < 0) {
			return "-1" + rnaSeq.substring(0,end5);
		}
		*/
		return rnaSeq.substring(start5, end5);
	}
 	//get the four base pair on the 3' end of the sequence, 2 from the first part of the Pa helix, and 2 from the first part of the hairpin loop sequence
	//reverses seq so that first 2 basepairs in sequence are from helix and next two base pairs are from hairpin loop
	public String get3End4BPSeq() {
		int start3 = heliciesList.get(0).getPre3()-3;
		int end3 = heliciesList.get(0).getPre3() + 1;
		String input = rnaSeq.substring(start3, end3);
		StringBuilder input1 = new StringBuilder();
		input1.append(input);
		input1=input1.reverse(); 
		return input1.toString();
	}
	
	
}
