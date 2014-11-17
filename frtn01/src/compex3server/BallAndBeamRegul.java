package compex3server;

import compex3client.ReferenceGenerator;

import SimEnvironment.*;

//BallAndBeamRegul class to be written by you
//ONLY TO BE USED FOR SIMULATION!
public class BallAndBeamRegul extends Thread {
	private PID PIDcontroller;
	private PI PIcontroller;
	ReferenceGenerator referenceGenerator;
	private AnalogSource analogInAngle;
	private AnalogSource analogInPosition;
	private AnalogSink analogOut;
	private AnalogSink analogRef;

	// Constructor
	public BallAndBeamRegul(ReferenceGenerator refgen, BallAndBeam bb,
			int priority) {
		PIDcontroller = new PID("PID");
		PIcontroller = new PI("PI");
		referenceGenerator = refgen;
		analogInPosition = bb.getSource(0);
		analogInAngle = bb.getSource(1);
		analogOut = bb.getSink(0);
		analogRef = bb.getSink(1);

		setPriority(priority);
	}

	private double limit(double u) {
		if (u < -10.0) {
			u = -10.0;
		} else if (u > 10.0) {
			u = 10.0;
		}
		return u;
	}

	public void run() {
		long t = System.currentTimeMillis();
		while (true) {
			double yBall = analogInPosition.get();
//			double yBeam = analogInAngle.get();
			double ref = referenceGenerator.getRef();
/*			
			// ...
			synchronized (PIcontroller) {
				yBall = limit(PIcontroller.calculateOutput(yBeam, ref));
				PIcontroller.updateState(yBall);
			} */
			synchronized (PIDcontroller) {
//				double u = limit(PIDcontroller.calculateOutput(yBall, ref));
//				analogOut.set(u);
	 			double angRef = PIDcontroller.calculateOutput(yBall, ref) ;
	 			PIDcontroller.updateState(angRef); 			
	 			synchronized (PIcontroller) {
	 				double yBeam = analogInAngle.get();
					yBall = limit(PIcontroller.calculateOutput(yBeam, angRef));
					PIcontroller.updateState(yBall);
					analogOut.set(yBall);			
//					analogRef.set(angRef); // test
	 			} 
				
			}
			
		
			analogRef.set(ref);
			t = t + PIDcontroller.getHMillis();
			// ...
			long duration = t - System.currentTimeMillis();
			if (duration > 0) {
				try {
					sleep(duration);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} 
			}
		}
	}
}