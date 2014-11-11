package compex3server;

public class ReferenceGeneratorProxy {
	private CommServer commServer;
	
	public ReferenceGeneratorProxy(CommServer commServer) {
		this.commServer = commServer;
	}
	
	public double getRef() {
		return commServer.getRef();
	}
}
