package compex3server;

import compex3server.PIDParameters;

//PID class to be written by you
public class PID {
	// Current PID parameters
	private PIDParameters p;
	private double I, v, e, D, yOld;
	
	// Constructor
	public PID(String name){
		 PIDParameters p = new PIDParameters();
		  p.Beta = 1.0;
		  p.H = 0.02;
		  p.integratorOn = false;
		  p.K = -0.1;
		  p.Ti = 0.0;
		  p.Td = 2.0;
		  p.Tr = 10.0;
		  p.N = 5;
		  setParameters(p);
		  
		  this.I = 0.0;
		  this.v = 0.0;
		  this.e = 0.0;
		  this.D = 0.0;
		  this.yOld = 0.0;
	}
	
	// Calculates the control signal v.
	// Called from BallAndBeamRegul.
	public synchronized double calculateOutput(double y, double yref){
		this.e = yref - y;
		D = p.Td/(p.Td + p.N*p.H)*D - p.K * p.N * p.Td/(p.Td + p.N*p.H)*(y - yOld);
		this.v = p.K*(p.Beta*yref - y) + I + D;
		yOld = y;
		return this.v;
	}
	
	// Updates the controller state.
	// Should use tracking-based anti-windup
	// Called from BallAndBeamRegul.
	public synchronized void updateState(double u){
		  if (p.integratorOn) {
			  I = I + (p.K * p.H / p.Ti) * e + (p.H / p.Tr) * (u - v);	 
		  } else {
			  I = 0.0;
		  }
		  
	}
	
	// Returns the sampling interval expressed as a long.
	// Explicit type casting needed.
	public synchronized long getHMillis(){
		return (long) (p.H*1000.0);
	}
	
	// Sets the PIDParameters.
	// Called from PIDGUI.
	// Must clone newParameters.
	public synchronized void setParameters(PIDParameters newParameters){
		p = (PIDParameters) newParameters.clone();
		if(!p.integratorOn){
			I = 0.0;
		}
	}
	// Sets the I-part and D-part of the controller to 0.
	  // For example needed when changing controller mode.
	  public synchronized void reset(){
		  I = 0.0;
		  D = 0.0;
	  }

	  // Returns the current PIDParameters.
	  public synchronized PIDParameters getParameters(){
		  return p;
	  }
}