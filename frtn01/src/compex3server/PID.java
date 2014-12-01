package compex3server;

import compex3server.PIDParameters;

public class PID {
	// Current PID parameters
	private PIDParameters p;
	private double ad;
	private double bd;
	
	private double I; // Integrator state
	private double D; // Derivative state
	
	private double v; // Desired control signal
	private double e; // Current control error
	
	private double y;    // Current measurement value
	private double yOld; // Previous measurement value
	
	// Constructor
	public PID(String name) {
		PIDParameters p = new PIDParameters();
		p.Beta = 1.0;
		p.H = 0.05;
		p.integratorOn = false;
		p.K = -0.25;
		p.N = 10.0;
		p.Td = 1.0;
		p.Ti = 8.0;
		p.Tr = 10.0;
		setParameters(p);
		
		this.I = 0.0;
		this.D = 0.0;
		this.v = 0.0;
		this.e = 0.0;
		this.y = 0.0;
		this.yOld = 0.0;
	}
	
	// Calculates the control signal v.
	// Called from BallAndBeamRegul.
	public synchronized double calculateOutput(double y, double yref) {
		this.y = y;
		e = yref - y;
		D = ad*D - bd*(y - yOld);
		v = p.K * (p.Beta * yref - y) + I + D; // I is 0.0 if integratorOn is false
		return v;
	}
	
	// Updates the controller state.
	// Should use tracking-based anti-windup
	// Called from BallAndBeamRegul.
	public synchronized void updateState(double u) {
		if (p.integratorOn) {
			I = I + (p.K * p.H / p.Ti) * e + (p.H / p.Tr) * (u - v);
		} else {
			I = 0.0;
		}
		yOld = y;
	}
	
	// Returns the sampling interval expressed as a long.
	// Explicit type casting needed.
	public synchronized long getHMillis() {
		return (long)(p.H * 1000.0);
	}
	
	// Sets the PIDParameters.
	// Called from PIDGUI.
	// Must clone newParameters.
	public synchronized void setParameters(PIDParameters newParameters) {
		p = (PIDParameters)newParameters.clone();
		ad = p.Td / (p.Td + p.N*p.H);
		bd = p.K * ad * p.N;
		if (!p.integratorOn) {
			this.I = 0.0;
		}
	}
	
	// Sets the I-part of the controller to 0.
	// For example needed when changing controller mode.	
	public synchronized void reset() {
		this.I = 0.0;
		this.D = 0.0;
		this.yOld = 0.0;
	}
	
	// Returns the current PIDParameters.
	public synchronized PIDParameters getParameters() {
		return (PIDParameters)p.clone();
	}
}
