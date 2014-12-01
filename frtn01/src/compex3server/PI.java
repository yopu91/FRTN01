package compex3server;

import compex3server.PIParameters;

// PI class to be written by you
public class PI {
	// Current PI parameters
	private PIParameters p;
	
	private double I; // Integrator state
	
	private double v; // Desired control signal
	private double e; // Current control error
	
	// Constructor
	public PI(String name) {
		PIParameters p = new PIParameters();
		p.Beta = 1;
		p.H = 0.05;
		p.integratorOn = false;
		p.K = 1.0;
		p.Ti = 0.0;
		p.Tr = 1.0;
		setParameters(p);
		
		this.I = 0.0;
		this.v = 0.0;
		this.e = 0.0;
	}
	
	// Calculates the control signal v.
	// Called from BeamRegul.
	public synchronized double calculateOutput(double y, double yref) {
		this.e = yref - y;
		this.v = p.K * (p.Beta * yref - y) + I;
		return this.v;
	}
	
	// Updates the controller state.
	// Should use tracking-based anti-windup
	// Called from BeamRegul.
	public synchronized void updateState(double u) {
		if (this.p.integratorOn) {
			this.I = this.I + (p.K * p.H / p.Ti) * this.e + (p.H / p.Tr) * (u - this.v);
		} else {
			this.I = 0.0;
		}
	}
	
	// Returns the sampling interval expressed as a long.
	// Note: Explicit type casting needed
	public synchronized long getHMillis() {
		return (long)(this.p.H * 1000.0);
	}
	
	// Sets the PIParameters.
	// Called from PIGUI.
	// Must clone newParameters.
	public synchronized void setParameters(PIParameters newParameters) {
		this.p = (PIParameters)newParameters.clone();
	}
	
	// Sets the I-part of the controller to 0.
	// For example needed when changing controller mode.
	public synchronized void reset() {
		this.I = 0.0;
	}
	
	// Returns the current PIParameters.
	public synchronized PIParameters getParameters() {
		return (PIParameters)p.clone();
	}
}
