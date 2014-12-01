package compex3server;

import se.lth.control.DoublePoint;
import se.lth.control.realtime.AnalogIn;
import se.lth.control.realtime.AnalogOut;
import se.lth.control.realtime.IOChannelException;
import se.lth.control.realtime.Semaphore;

public class Regul extends Thread {
	public static final int OFF = 0;
	public static final int BEAM = 1;
	public static final int BALL = 2;
	
	private PI inner = new PI("PI");
	private PID outer = new PID("PID");
	
//	private AnalogIn analogInAngle; 
//	private AnalogIn analogInPosition; 
//	private AnalogOut analogOut;
	private CommServerProcess process;
	
	private ReferenceGeneratorProxy referenceGenerator;
	private OpComProxy opcom;
	
	private int priority;
	private boolean WeShouldRun = true;
	private long starttime;
	private Semaphore mutex; // used for synchronization at shut-down
	
	private ModeMonitor modeMon;
	
	// Inner monitor class
	class ModeMonitor {
		private int mode;
		
		// Synchronized access methods
		public synchronized void setMode(int newMode) {
			mode = newMode;
			inner.reset();
			outer.reset();
		}
		
		public synchronized int getMode() {
			return mode;
		}
	}
	
	public Regul(int pri) {
		priority = pri;
		mutex = new Semaphore(1);
		modeMon = new ModeMonitor();
	}
	
	public void setProcess(CommServerProcess process){
		this.process = process;
	}
	
	public void setOpCom(OpComProxy opcom) {
		this.opcom = opcom;
	}
	
	public void setRefGen(ReferenceGeneratorProxy referenceGenerator){
		this.referenceGenerator = referenceGenerator;
	}
	
	// Called in every sample in order to send plot data to OpCom
	private void sendDataToOpCom(double yref, double y, double u) {
		double x = (double)(System.currentTimeMillis() - starttime) / 1000.0;
		DoublePoint dp = new DoublePoint(x,u);
		PlotData pd = new PlotData(x,yref,y);
		opcom.putControlDataPoint(dp);
		opcom.putMeasurementDataPoint(pd);
	}
	
	public synchronized void setInnerParameters(PIParameters p) {
		inner.setParameters(p);
	}
	
	public synchronized PIParameters getInnerParameters() {
		return inner.getParameters();
	}
	
	public synchronized void setOuterParameters(PIDParameters p) {
		outer.setParameters(p);
	}
	
	public synchronized PIDParameters getOuterParameters(){
		return outer.getParameters();
	}
	
	public void setOFFMode(){
		modeMon.setMode(OFF);
	}
	
	public void setBEAMMode(){
		modeMon.setMode(BEAM);
	}
	
	public void setBALLMode(){
		modeMon.setMode(BALL);
	}
	
	public int getMode(){
		return modeMon.getMode();
	}
	
	// Called from OpCom when shutting down
	public synchronized void shutDown() {
		WeShouldRun = false;
		mutex.take();
		process.shutDown();
	}
	
	private double limit(double v) {
		return limit(v, -10, 10);
	}
	
	private double limit(double v, double min, double max) {
		if (v < min) {
			v = min;
		} else if (v > max) {
			v = max;
		}
		return v;
	}
	
	public void run() {
		long duration;
		long t = System.currentTimeMillis();
		starttime = t;
		
		setPriority(priority);
		mutex.take();
		while (WeShouldRun) {
			double u = 0.0;
			double y = 0.0;
			double yRef = 0.0;
			
			switch (modeMon.getMode()) {
			case OFF: {
				setOut(u);
				break;
			}
			case BEAM: {
				yRef = referenceGenerator.getRef();
				synchronized (inner) {
					y = process.getAngle();
					u = limit(inner.calculateOutput(y, yRef));
					setOut(u);
					inner.updateState(u);
				}
				break;
			}
			case BALL: {
				yRef = referenceGenerator.getRef();
				synchronized (outer) {
					y = process.getPosition();
					double angleRef = outer.calculateOutput(y, yRef);
					double angleRefSat = limit(angleRef);
					double angle;
					synchronized (inner) {
						angle = process.getAngle();
						u = limit(inner.calculateOutput(angle, angleRefSat));
						setOut(u);
						inner.updateState(u);
					}
					if (angleRef == angleRefSat) {
						outer.updateState(angleRef);
					} else {
						outer.updateState(angle);
					}
				}
				break;
			}
			default: {
				System.out.println("Error: Illegal mode.");
				break;
			}
			}
			
			sendDataToOpCom(yRef, y, u);
			
			// sleep
			t = t + inner.getHMillis();
			duration = t - System.currentTimeMillis();
			if (duration > 0) {
				try {
					sleep(duration);
				} catch (InterruptedException x) {
				}
			}
		}
		mutex.give();
	}
	
	
	private void setOut(double u) {
		process.setOutput(u);
	}
}
