package compex3client;
import javax.swing.*;

public class MainOperatorStation {
	public static void main(String[] argv) {
		final int refGenPriority = 6;
		final int plotterPriority = 7;
		
		String host = argv[0];
		int port = Integer.parseInt(argv[1]);
		
		CommClient commClient = new CommClient(host, port);
		
		ReferenceGenerator refgen = new ReferenceGenerator(refGenPriority);

		final OpCom opcom = new OpCom(plotterPriority);//Must be declared final since it is used in an inner class

		RegulProxy regul = new RegulProxy(commClient);
		
		opcom.setRegul(regul);
		commClient.setRefGen(refgen);
		
		Thread t = new Thread(commClient);
		t.start();
		
		Runnable initializeGUI = new Runnable(){
			public void run(){
			    opcom.initializeGUI();
			    opcom.start();
			}
		};
		try{
		    SwingUtilities.invokeAndWait(initializeGUI);
		}catch(Exception e){
		    return;
		}
		
		commClient.setOpCom(opcom);
		
		refgen.start();
	}
}

