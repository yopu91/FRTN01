package compex3server;

public class MainController {
	public static void main(String[] argv) {
		final int regulPriority = 8; 
		
		//int port = Integer.parseInt(argv[0]);
		int port = 3010;
		
		CommServer commServer = new CommServer(port);
		
		ReferenceGeneratorProxy refgen = new ReferenceGeneratorProxy(commServer);
		
		
		
//		Regul regul = new Regul(regulPriority);
		
		
		// Sim tests
		//int testPort = Integer.parseInt(argv[1]);
		int testPort = 3000;
		TestRegul regul = new TestRegul(regulPriority);
		TestServerToProcess process = new TestServerToProcess(regul, testPort);
		regul.setProcess(process);
		Thread p = new Thread(process);
		p.start();
		
		
		
		OpComProxy opcom = new OpComProxy(commServer); 
		
		commServer.setRegul(regul);
		regul.setOpCom(opcom); 
		regul.setRefGen(refgen);
		
		Thread t = new Thread(commServer);
		t.start();
		
		regul.start();
	}
}
