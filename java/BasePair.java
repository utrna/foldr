
public class BasePair {
	private int nucleotide1;
	private int nucleotide2;
	
	//pass in the position of the nucleotides that are paired
	public BasePair(int base1, int base2) {
		nucleotide1 = base1;
		nucleotide2 = base2;
	}
	public int getBase1Pos() {
		return nucleotide1;
	}
	public int getBase2Pos() {
		return nucleotide2;
	}
	public void setBase1Pos(int pos1) {
		nucleotide1 = pos1;
	}
	public void setBase2Pos(int pos2) {
		nucleotide2 = pos2;
	}
}
