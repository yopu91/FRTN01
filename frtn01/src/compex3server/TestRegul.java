package compex3server;

import se.lth.control.DoublePoint;
import se.lth.control.realtime.AnalogIn;
import se.lth.control.realtime.AnalogOut;
import se.lth.control.realtime.IOChannelException;
import se.lth.control.realtime.Semaphore;
import simprocess.BallAndBeam;

public class TestRegul extends Regul {
	private TestServerToProcess process;

	public static final int OFF = 0;
	public static final int BEAM = 1;
	public static final int BALL = 2;

	private PI inner = new PI("PI");
	private PID outer = new PID("PID");

	// private AnalogIn analogInAngle;
	// private AnalogIn analogInPosition;
	// private AnalogOut analogOut;

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

	public TestRegul(int pri) {
		this.process = null;
		priority = pri;
		mutex = new Semaphore(1);
		modeMon = new ModeMonitor();
	}

	public void setProcess(TestServerToProcess process){
		this.process = process;
	}
	public void setOpCom(OpComProxy opcom) {
		this.opcom = opcom;
	}

	public void setRefGen(ReferenceGeneratorProxy referenceGenerator) {
		this.referenceGenerator = referenceGenerator;
	}

	// Called in every sample in order to send plot data to OpCom
	private void sendDataToOpCom(double yref, double y, double u) {
		double x = (double) (System.currentTimeMillis() - starttime) / 1000.0;
		DoublePoint dp = new DoublePoint(x, u);
		PlotData pd = new PlotData(x, yref, y);
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

	public synchronized PIDParameters getOuterParameters() {
		return outer.getParameters();
	}

	public void setOFFMode() {
		modeMon.setMode(OFF);
	}

	public void setBEAMMode() {
		modeMon.setMode(BEAM);
	}

	public void setBALLMode() {
		modeMon.setMode(BALL);
	}

	public int getMode() {
		return modeMon.getMode();
	}

	// Called from OpCom when shutting down
	public synchronized void shutDown() {
		WeShouldRun = false;
		mutex.take();
		process.shutDown();
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
			switch (modeMon.getMode()) {
			case OFF: {
				// Code for the OFF mode.
				// Written by you.
				// Should include resetting the controllers
				sendDataToOpCom(0, 0, 0);
				inner.reset();
				outer.reset();

				// Should include a call to sendDataToOpCom
				break;
			}
			case BEAM: {
				// Code for the BEAM mode
				// Written by you.
				// Should include a call to sendDataToOpCom
				double yBeam = 0;
				double ref = referenceGenerator.getRef();
				synchronized (inner) {
					yBeam = process.getAngle();
					double u = limit(inner.calculateOutput(yBeam, ref), -10.0,
							10.0);
					// analogOut.set(u);
					process.setOutput(u);
					inner.updateState(u);
					sendDataToOpCom(ref, yBeam, u);
				}

				break;
			}
			case BALL: {
				// Code for the BALL mode
				// Written by you.
				// Should include a call to sendDataToOpCom
				double yBeam = 0;
				double yBall = 0;
				double ref = referenceGenerator.getRef();
				synchronized (outer) {
					yBall = process.getPosition();
					double angRef = outer.calculateOutput(yBall, ref);
					synchronized (inner) {
						yBeam = process.getAngle();
						double u = inner.calculateOutput(yBeam, angRef);
						// analogOut.set(u);
						process.setOutput(u);
						inner.updateState(u);
						sendDataToOpCom(ref, yBall, u);
					}
					outer.updateState(angRef);

				}

				break;
			}
			default: {
				System.out.println("Error: Illegal mode.");
				break;
			}
			}

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
}