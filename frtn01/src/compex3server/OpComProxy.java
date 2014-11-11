package compex3server;

import se.lth.control.DoublePoint;

public class OpComProxy {
	private CommServer commServer;
	
	public OpComProxy(CommServer commServer) {
		this.commServer = commServer;
	}
	
	public void putControlDataPoint(DoublePoint dp) {
		commServer.putControlDataPoint(dp);
	}
	
	public void putMeasurementDataPoint(PlotData pd) {
		commServer.putMeasurementDataPoint(pd);
	}
}
