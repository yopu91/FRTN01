package simprocess;

public class MainSimProcess {
	public static void main (String args[]){
		BallAndBeam bb = new BallAndBeam();
		Thread c = new Thread(new CommProcess("localhost", 3000, bb));
		c.start();
	}
	
}
