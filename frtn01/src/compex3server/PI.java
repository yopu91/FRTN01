package compex3server;

import compex3server.PIParameters;

// PI class to be written by you
public class PI {
	  private PIParameters p;

	  private double I; // Integrator state
	  
	  private double v; // Desired control signal
	  private double e; // Current control error
	  
	  //Constructor
	  public PI(String name) {
		  PIParameters p = new PIParameters();
		  p.Beta = 1.0;
		  p.H = 0.02;
		  p.integratorOn = false;
		  p.K = 1.0;
		  p.Ti = 0.0;
		  p.Tr = 10.0;
		  setParameters(p);
		  
		  this.I = 0.0;
		  this.v = 0.0;
		  this.e = 0.0;
	  }

	  public synchronized double calculateOutput(double y, double yref) {
		  this.e = yref - y;
		  this.v = p.K * (p.Beta * yref - y) + I; // I is 0.0 if integratorOn is false
		  return this.v;
	  }

	  public synchronized void updateState(double u) {
		  if (p.integratorOn) {
			  I = I + (p.K * p.H / p.Ti) * e + (p.H / p.Tr) * (u - v);
		  } else {
			  I = 0.0;
		  }
	  }
		
	  public synchronized long getHMillis() {
	  	return (long)(p.H * 1000.0);
	  }

	  public synchronized void setParameters(PIParameters newParameters) {
	  	p = (PIParameters)newParameters.clone();
		if (!p.integratorOn) {
			I = 0.0;
		}
	  }

	  // Sets the I-part of the controller to 0.
	  // For example needed when changing controller mode.
	  public synchronized void reset(){
		  I = 0.0;
	  }
	  
	  // Returns the current PIParameters.
	  public synchronized PIParameters getParameters(){
		  return p;
	  }
	}
	   