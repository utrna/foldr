
public class PaHelicies  {
	//beginning 5' nucleotide number
	private int pre_5;
	//end 3' nucleotide number
	private int end_3;
	
	//constructor
	//arguments (beg 5', end 3')
	public PaHelicies(int a, int b) {
		pre_5 = a-1;
		end_3 = b-1;
	}
	
	public int getPre5() {
		return pre_5;
	}
	public int getEnd3() {
		return end_3;
	}
	
}
