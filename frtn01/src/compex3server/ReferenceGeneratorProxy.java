package compex3server;

public class ReferenceGeneratorProxy {
	private CommServerClient commServer;
	
	public ReferenceGeneratorProxy(CommServerClient commServer) {
		this.commServer = commServer;
	}
	
	public double getRef() {
		return commServer.getRef();
	}
}
