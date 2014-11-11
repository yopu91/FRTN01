package compex3server;

public class MainController {
	public static void main(String[] argv) {
		final int regulPriority = 8; 
		
		int port = 4444;
		
		CommServer commServer = new CommServer(port);
		
		ReferenceGeneratorProxy refgen = new ReferenceGeneratorProxy(commServer); 
		Regul regul = new Regul(regulPriority); 
		OpComProxy opcom = new OpComProxy(commServer); 
		
		commServer.setRegul(regul);
		regul.setOpCom(opcom); 
		regul.setRefGen(refgen);
		
		Thread t = new Thread(commServer);
		t.start();
		
		regul.start();
	}
}
