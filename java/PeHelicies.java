import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class PeHelicies {
	//name of the helix
	private String name;
	//beginning 5' nucleotide number
	private int pre_5;
	//end 5' nucleotide number
	private int end_5;
	//beginning 3' nucleotide number
	private int pre_3;
	//end 3' nucleotide number
	private int end_3;
	//letter of helix, ie. p2b, p2c, p2d
	private char letter;
	//RNA sequence
	private String seq;
	//simple distance
	private int simpleDist;
	//helix length
	private int helixLength;
	private double helixEnergy;
	private double helixME;
	private int helixCD;
	//ArrayList containing all the base pairs
	ArrayList<BasePair> bpList;
	//distance between the previous helix and this helix on the 5' end and the 3' end
	private int dist5;
	private int dist3;

	
	//constructor
	//arguments (beg 5', end 5', beg 3', end 3')
	public PeHelicies(int a, int b, int c, int d,String rnaSeq) {
		pre_5 = a;
		end_5 = b;
		pre_3 = c;
		end_3 = d;
		helixLength =  end_5-pre_5 + 1;
		seq = rnaSeq;
		
		bpList = new ArrayList<BasePair>();
		int count3 = end_3;
		for(int i=pre_5; i<= end_5; i++) {
			bpList.add(new BasePair(i,count3));
			count3--;
		}

	}
	
	//adds the name of the helix to the helix object
	public void setName(String name1) {
		name = name1.trim();
	}
	//gets the name of the helix
	public String getName() {
		return name;
	}
	public int getPre5() {
		return pre_5 ;
	}
	public int getEnd5() {
		return end_5;
	}
	public int getPre3() {
		return pre_3;
	}
	public int getEnd3() {
		return end_3;
	}
	public char getLetter() {
		return letter;
	}
	public int getSimpleDist() {
		return (pre_3 - end_5);
	}
	public int getHelixLength() {
		return helixLength;
	}
	
	
	public void printSequence() {
		//System.out.println(helixME);
		for(int i = pre_5; i <= end_5; i++) {
			System.out.print((i) + ":" + seq.charAt(i-1) + " ");
		}
		
		System.out.println("");
		
		for(int i = end_3; i >= pre_3; i--) {
			System.out.print((i) + ":" + seq.charAt(i-1) + " ");
		}
		System.out.println("\n");
	}
	
	//augments helix
	public void augmentHelix() {
		pre_5 += 1;
		end_5 += 1;
		pre_3 += 1;
		end_3 += 1;
	}
	
	//unaugments helix
	public void unAugmentHelix() {
		pre_5 -= 1;
		end_5 -= 1;
		pre_3 -= 1;
		end_3 -= 1;
	}
	public boolean isEqual(PeHelicies helixB) {
		if(helixB.getPre5() != this.pre_5)
			return false;
		if(helixB.getEnd5() != this.end_5)
			return false;
		if(helixB.getPre3() != this.pre_3)
			return false;
		if(helixB.getEnd3() != this.end_3)
			return false;
		return true;
	}
	//given a count of the number of helicies in a compound helix, returns the letter of the helix, ie. the b in the P2b
	public char generateLetter(int numHelicies) {
		//ascii vals 97-122, a-z
		numHelicies +=97;
		char val = (char) numHelicies;
		return val;
	}
	//generates the full name of the helix given the compound helix name and the number of the helix
	public String generateName(String compHelixName, int numHelicies) {
		return ("" + compHelixName + generateLetter(numHelicies));
	}
	
	public double getHelixEnergy() {
		Map<String, Double> energeticsDict = new HashMap<String, Double>();
		energeticsDict.put("AU:AU", -.9);
		energeticsDict.put("AU:CG", -2.2);
		energeticsDict.put("AU:GC", -2.1);
		energeticsDict.put("AU:GU", -0.6);
		energeticsDict.put("AU:UA", -1.1);
		energeticsDict.put("AU:UG", -1.4);
		energeticsDict.put("CG:AU", -2.1);
		energeticsDict.put("CG:CG", -3.3);
		energeticsDict.put("CG:GC", -2.4);
		energeticsDict.put("CG:GU", -1.4);
		energeticsDict.put("CG:UA", -2.1);
		energeticsDict.put("CG:UG", -2.1);
		energeticsDict.put("GC:AU", -2.4);
		energeticsDict.put("GC:CG", -3.4);
		energeticsDict.put("GC:GC", -3.3);
		energeticsDict.put("GC:GU", -1.5);
		energeticsDict.put("GC:UA", -2.2);
		energeticsDict.put("GC:UG", -2.5);
		energeticsDict.put("GU:AU", -1.3);
		energeticsDict.put("GU:CG", -2.5);
		energeticsDict.put("GU:GC", -2.1);
		energeticsDict.put("GU:GU", -0.5);
		energeticsDict.put("GU:UA", -1.4);
		energeticsDict.put("GU:UG", 1.3);
		energeticsDict.put("UA:AU", -1.3);
		energeticsDict.put("UA:CG", -2.4);
		energeticsDict.put("UA:GC", -2.1);
		energeticsDict.put("UA:GU", -1.0);
		energeticsDict.put("UA:UA", -0.9);
		energeticsDict.put("UA:UG", -1.3);
		energeticsDict.put("UG:AU", -1.0);
		energeticsDict.put("UG:CG", -1.5);
		energeticsDict.put("UG:GC", -1.4);
		energeticsDict.put("UG:GU", 0.3);
		energeticsDict.put("UG:UA", -0.6);
		energeticsDict.put("UG:UG", -0.5);
				
		
		//gets the 5' sequence of the helix
		String seq5 = "";
		for(int i=pre_5-1; i<end_5; i++) {
			seq5 += seq.charAt(i);
		}
		//gets the 3' sequence of the helix
		String seq3 = "";
		for(int i=end_3-1; i>pre_3-2; i--) {
			seq3 += seq.charAt(i);
		}
		double energy = 0.0;
		if(seq3.length() > 0 && seq5.length() > 0) {
			//gets the energetics value
			for(int i=0; i<helixLength-1; i++) {
			char base1 = seq5.charAt(i);
			char base2 = seq3.charAt(i);
			char base3 = seq5.charAt(i+1);
			char base4 = seq3.charAt(i+1);
			String key = "" + base1+base2+":"+base3+base4;
			energy += energeticsDict.get(key);
			}
		}
		helixEnergy = energy;
		return energy;
		
	}
	public void setHelixME(double me) {
		helixME = me;
	}
	public double getHelixME() {
		return helixME;
	}
	public void setCD(int cd) {
		helixCD = cd;
	}
	public int getCD() {
		return helixCD;
	}
	public ArrayList<BasePair> getBasePairsList() {
		return bpList;
	}
	//get the total number of paired nucleotides in a helix
	public int getTotalPairedNuc() {
		return ((end_5 - pre_5) * 2) + 2;
	}
	
	//pass in a helix as a argument, if the there are conflicting base pairs, remove the base pairs in this helix, if all the base pairs are removed or the 
	// helix is now to short in length, return true,
	//if part of the base pairs are removed, return false
	public boolean removeBasePairs(PeHelicies helix) {
		//go through the base pairs of the helix passed in, if any of the positions are oonflicting in the current basepair, replace with a 0
		for(BasePair bp: helix.getBasePairsList()) {
			for(BasePair currentBP: this.bpList) {
				if(bp.getBase1Pos()==currentBP.getBase1Pos())
					currentBP.setBase1Pos(0);
				if(bp.getBase1Pos()==currentBP.getBase2Pos())
					currentBP.setBase2Pos(0);
				if(bp.getBase2Pos()==currentBP.getBase1Pos())
					currentBP.setBase1Pos(0);
				if(bp.getBase2Pos()==currentBP.getBase2Pos())
					currentBP.setBase2Pos(0);
			}
		}
		//remove all the base pairs that have conflicting bases, ie. base pairs that contain 0s in either position
		for(Iterator<BasePair> iterator = this.bpList.iterator(); iterator.hasNext();) {
			BasePair bp = iterator.next();
			if(bp.getBase1Pos()==0 || bp.getBase2Pos()==0)
				iterator.remove();
			
		}
		
		//if all the base pairs have been removed or if the helix size is now less than 2, the helix needs to be removed, return true
		if(this.bpList.size() < 2)
			return true;
		//otherwise need to update the indicies of the helix and the size of the helix
		else {
			
			int newPre5 = this.bpList.get(0).getBase1Pos();
			int newEnd3 = this.bpList.get(0).getBase2Pos();
			int newEnd5 = this.bpList.get(bpList.size()-1).getBase1Pos();
			int newPre3 = this.bpList.get(bpList.size()-1).getBase2Pos();
			helixLength = this.bpList.size();
			
			return false;
		}
		
	}
	
	//returns true if the helix is an initial helix, false otherwise
	public boolean isInitialHelix() {
		if(name.contains("a"))
			return true;
		else
			return false;
	}
	//set the distances from the previous helix to the current helix on the 5' end and the 3' end
	public void setDistances(int distance5, int distance3) {
		dist5 = distance5;
		dist3 = distance3;
	}
	public int getDistance5() {
		return dist5;
	}
	public int getDistance3() {
		return dist3;
	}
	//compares the two helicies and determines which of the two is more stable based on the conflicting base pairs
	//if this helix is more stable return true, if the competing helix is more stable return false
	public boolean competingBasePairs(PeHelicies competingHelix) {
		int competingHelixLen = competingHelix.getHelixLength();
		int thisHelixLen = this.helixLength;
		//arraylist containing the conflicting base pairs of this helix
		ArrayList<BasePair> thisBPList = new ArrayList<BasePair>();
		//arraylist containing the conflicting base pairs of the competing helix
		ArrayList<BasePair> compBPList = new ArrayList<BasePair>();
		
		for(BasePair thisBP: this.bpList) {
			for(BasePair compBP: competingHelix.getBasePairsList()) {
				if(thisBP.getBase1Pos() == compBP.getBase1Pos() || thisBP.getBase1Pos() == compBP.getBase2Pos()) {
					thisBPList.add(thisBP);
				}
				if(thisBP.getBase2Pos() == compBP.getBase1Pos() || thisBP.getBase2Pos() == compBP.getBase2Pos()) {
					thisBPList.add(thisBP);
				}

			}
		}
		for(BasePair compBP: competingHelix.getBasePairsList()) {
			for(BasePair thisBP: this.bpList) {
				if(compBP.getBase1Pos() == thisBP.getBase1Pos() || compBP.getBase1Pos() == thisBP.getBase2Pos()) {
					compBPList.add(compBP);
				}
				if(compBP.getBase2Pos() == thisBP.getBase1Pos() || compBP.getBase2Pos() == thisBP.getBase2Pos()) {
					compBPList.add(compBP);
				}

			}
		}
		//test code
		/*
		System.out.println("This bp list: ");
		for(BasePair bp: thisBPList) {
			System.out.print(bp.getBase1Pos() + " ");
		}
		System.out.println();
		for(BasePair bp: thisBPList) {
			System.out.print(bp.getBase2Pos() + " ");
		}
		System.out.println();
		System.out.println("Competing bp list: ");
		for(BasePair bp: compBPList) {
			System.out.print(bp.getBase1Pos() + " ");
		}
		System.out.println();
		for(BasePair bp: compBPList) {
			System.out.print(bp.getBase2Pos() + " ");
		}
		*/
		
		String thisHelix5Seq = "";
		String thisHelix3Seq = "";
		String compHelix5Seq = "";
		String compHelix3Seq = "";
		//get the sequences for this helix
		for(BasePair bp: thisBPList) {
			thisHelix5Seq+= seq.charAt(bp.getBase1Pos()-1);
			thisHelix3Seq+= seq.charAt(bp.getBase2Pos()-1);
		}
		//gets the sequences for the competing helix
		for(BasePair bp: compBPList) {
			compHelix5Seq+= seq.charAt(bp.getBase1Pos()-1);
			compHelix3Seq+= seq.charAt(bp.getBase2Pos()-1);
		}
		
		double thisBPEnergy = 0.0;
		double compBPEnergy = 0.0;
		
		Map<String, Double> energeticsDict = new HashMap<String, Double>();
		energeticsDict.put("AU:AU", -.9);
		energeticsDict.put("AU:CG", -2.2);
		energeticsDict.put("AU:GC", -2.1);
		energeticsDict.put("AU:GU", -0.6);
		energeticsDict.put("AU:UA", -1.1);
		energeticsDict.put("AU:UG", -1.4);
		energeticsDict.put("CG:AU", -2.1);
		energeticsDict.put("CG:CG", -3.3);
		energeticsDict.put("CG:GC", -2.4);
		energeticsDict.put("CG:GU", -1.4);
		energeticsDict.put("CG:UA", -2.1);
		energeticsDict.put("CG:UG", -2.1);
		energeticsDict.put("GC:AU", -2.4);
		energeticsDict.put("GC:CG", -3.4);
		energeticsDict.put("GC:GC", -3.3);
		energeticsDict.put("GC:GU", -1.5);
		energeticsDict.put("GC:UA", -2.2);
		energeticsDict.put("GC:UG", -2.5);
		energeticsDict.put("GU:AU", -1.3);
		energeticsDict.put("GU:CG", -2.5);
		energeticsDict.put("GU:GC", -2.1);
		energeticsDict.put("GU:GU", -0.5);
		energeticsDict.put("GU:UA", -1.4);
		energeticsDict.put("GU:UG", 1.3);
		energeticsDict.put("UA:AU", -1.3);
		energeticsDict.put("UA:CG", -2.4);
		energeticsDict.put("UA:GC", -2.1);
		energeticsDict.put("UA:GU", -1.0);
		energeticsDict.put("UA:UA", -0.9);
		energeticsDict.put("UA:UG", -1.3);
		energeticsDict.put("UG:AU", -1.0);
		energeticsDict.put("UG:CG", -1.5);
		energeticsDict.put("UG:GC", -1.4);
		energeticsDict.put("UG:GU", 0.3);
		energeticsDict.put("UG:UA", -0.6);
		energeticsDict.put("UG:UG", -0.5);
		
		if(thisHelix3Seq.length() > 0 && thisHelix5Seq.length() > 0) {
			//gets the energetics value
			for(int i=0; i<thisHelix3Seq.length()-1; i++) {
			char base1 = thisHelix5Seq.charAt(i);
			char base2 = thisHelix3Seq.charAt(i);
			char base3 = thisHelix5Seq.charAt(i+1);
			char base4 = thisHelix3Seq.charAt(i+1);
			String key = "" + base1+base2+":"+base3+base4;
			thisBPEnergy += energeticsDict.get(key);
			}
		}
		
		if(compHelix3Seq.length() > 0 && compHelix5Seq.length() > 0) {
			//gets the energetics value
			for(int i=0; i<compHelix3Seq.length()-1; i++) {
			char base1 = compHelix5Seq.charAt(i);
			char base2 = compHelix3Seq.charAt(i);
			char base3 = compHelix5Seq.charAt(i+1);
			char base4 = compHelix3Seq.charAt(i+1);
			String key = "" + base1+base2+":"+base3+base4;
			compBPEnergy += energeticsDict.get(key);
			}
		}
		
		thisBPEnergy = thisBPEnergy/thisHelixLen;
		compBPEnergy = compBPEnergy/competingHelixLen;
		
		System.out.println("Current bp energy: " + thisBPEnergy);
		System.out.println("Comp bp energy: " + compBPEnergy);
		
		//test code
		/*
		System.out.println(thisHelix5Seq);
		System.out.println(thisHelix3Seq);
		System.out.println();
		System.out.println(compHelix5Seq);
		System.out.println(compHelix3Seq);
		*/
		if(thisBPEnergy <= compBPEnergy)
			return true;
		else 
			return false;

		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
}
