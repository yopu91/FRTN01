package compex3client;

public class RegulProxy {
	public static final int OFF = 0;
	public static final int BEAM = 1;
	public static final int BALL = 2;
	
	private CommClient commClient;
	
	public RegulProxy(CommClient commClient) {
		this.commClient = commClient;
	}
	
	public PIParameters getInnerParameters() {
		return commClient.getInnerParameters();
	}
	
	public void setInnerParameters(PIParameters p) {
		commClient.setInnerParameters(p);
	}
	
	public PIDParameters getOuterParameters() {
		return commClient.getOuterParameters();
	}
	
	public void setOuterParameters(PIDParameters p) {
		commClient.setOuterParameters(p);
	}
	
	public int getMode() {
		return commClient.getMode();
	}
	
	public void setBALLMode() {
		commClient.setBALLMode();
	}
	
	public void setBEAMMode() {
		commClient.setBEAMMode();
	}
	
	public void setOFFMode() {
		commClient.setOFFMode();
	}
	
	public void shutDown() {
		commClient.shutDown();
	}
}
