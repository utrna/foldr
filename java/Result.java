
public class Result {
	
	private boolean a;
	private PeHelicies helix;
	private int cd;
	
	public Result(boolean value, PeHelicies helixToAdd, int conditionalDistance) {
		a = value;
		helix = helixToAdd;
		cd = conditionalDistance;
	}
	
	public boolean getBoolean() {
		return a;
	}
	
	public PeHelicies getHelix() {
		return helix;
	}
	public int getCondDist() {
		return cd;
	}
}
