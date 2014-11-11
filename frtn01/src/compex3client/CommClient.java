package compex3client;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import se.lth.control.DoublePoint;

public class CommClient implements Runnable {
	private final String cmdGetInnerParameters = "GetInnerParameters";
	private final String cmdSetInnerParameters = "SetInnerParameters";
	private final String cmdGetOuterParameters = "GetOuterParameters";
	private final String cmdSetOuterParameters = "SetOuterParameters";
	private final String cmdGetMode = "GetMode";
	private final String cmdSetOFFMode = "SetOFFMode";
	private final String cmdSetBEAMMode = "SetBEAMMode";
	private final String cmdSetBALLMode = "SetBALLMode";
	private final String cmdShutdown = "Shutdown";
	
	private final String cmdPutControlDataPoint = "PutControlDataPoint";
	private final String cmdPutMeasurementDataPoint = "PutMeasurementDataPoint";
	private final String cmdGetRef = "GetRef";
	
	private ReferenceGenerator refGen;
	private OpCom opCom;
	
	private Socket socket;
	private BufferedReader in;
	private BufferedWriter out;
	private List<String> replies;
	
	public CommClient(String host, int port) {
		this.refGen = null;
		this.opCom = null;
		try {
			this.socket = new Socket(host, port);
			this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
		} catch (IOException e) {
			System.out.println("Unable to connect to " + host + ":" + port);
			System.exit(1);
		}
		this.replies = new LinkedList<String>();
	}
	
	public void setRefGen(ReferenceGenerator refGen) {
		this.refGen = refGen;
	}
	
	public void setOpCom(OpCom opCom) {
		this.opCom = opCom;
	}
	
	public PIParameters getInnerParameters() {
		write(cmdGetInnerParameters);
		String reply = waitForReply(cmdGetInnerParameters);
		return Serialize.unserializePIParameters(reply);
	}
	
	public void setInnerParameters(PIParameters p) {
		write(cmdSetInnerParameters + " " + Serialize.serialize(p));
	}
	
	public PIDParameters getOuterParameters() {
		write(cmdGetOuterParameters);
		String reply = waitForReply(cmdGetOuterParameters);
		return Serialize.unserializePIDParameters(reply);
	}
	
	public void setOuterParameters(PIDParameters p) {
		write(cmdSetOuterParameters + " " + Serialize.serialize(p));
	}
	
	public int getMode() {
		write(cmdGetMode);
		String reply = waitForReply(cmdGetMode);
		return Integer.parseInt(reply);
	}
	
	public void setOFFMode() {
		write(cmdSetOFFMode);
	}
	
	public void setBEAMMode() {
		write(cmdSetBEAMMode);
	}
	
	public void setBALLMode() {
		write(cmdSetBALLMode);
	}
	
	public void shutDown() {
		write(cmdShutdown);
		try {
			in.close();
			out.close();
			socket.close();
		} catch (IOException e) {
			System.out.println("IOException: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private synchronized void write(String message) {
		try {
			out.write(message + "\n");
			DebugPrint("Sending: " + message);
			out.flush();
		} catch (IOException e) {
			System.out.println("IOException: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private String waitForReply(String kind) {
		String reply = null;
		while (reply == null) {
			synchronized (replies) {
				Iterator<String> it = replies.iterator();
				while ((reply == null) && it.hasNext()) {
					String s = it.next();
					if (s.startsWith(kind)) {
						reply = s;
						replies.remove(s);
					}
				}
				if (reply == null) {
					try {
						replies.wait();
					} catch (InterruptedException e) {
					}
				}
			}
		}
		reply = reply.substring(kind.length() + 1);
		DebugPrint("Reply of kind " + kind + " found: " + reply);
		return reply;
	}
	
	public void run() {
		try {
			String line;
			while ((line = in.readLine()) != null) {
				DebugPrint("Command received: \"" + line + "\"");
				// Commands
				if (line.equals(cmdGetRef)) {
					write(cmdGetRef + " " + refGen.getRef());
				} else if (line.startsWith(cmdPutControlDataPoint)) {
					if (opCom != null) {
						String newDoublePoint = line.substring(cmdPutControlDataPoint.length() + 1);
						DoublePoint dp = Serialize.unserializeDoublePoint(newDoublePoint);
						if (dp != null) {
							opCom.putControlDataPoint(dp);
						}
					} else {
						DebugPrint("Note: Ignoring control data point. GUI not yet initialized.");
					}
				} else if (line.startsWith(cmdPutMeasurementDataPoint)) {
					if (opCom != null) {
						String newPlotData = line.substring(cmdPutMeasurementDataPoint.length() + 1);
						PlotData pd = Serialize.unserializePlotData(newPlotData);
						if (pd != null) {
							opCom.putMeasurementDataPoint(pd);
						}
					} else {
						DebugPrint("Note: Ignoring measurement data point. GUI not yet initialized.");
					}
				} else if (
						line.startsWith(cmdGetInnerParameters) ||
						line.startsWith(cmdGetOuterParameters) ||
						line.startsWith(cmdGetMode)) {
					// Replies
					DebugPrint("Reply received: " + line);
					synchronized (replies) {
						replies.add(line);
						replies.notifyAll();
					}
				} else {
					System.out.println("Error: Unknown command: " + line);
				}
			}
		} catch (IOException e) {
			System.out.println("Error: readLine failed: " + e.getMessage());
		}
		System.out.println("CommClient stopped.");
	}
	
	private void DebugPrint(String message) {
		//System.out.println(message);
	}
}

