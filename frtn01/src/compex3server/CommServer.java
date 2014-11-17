package compex3server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.io.InputStreamReader;
import se.lth.control.DoublePoint;


public class CommServer implements Runnable {
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
	
	private Regul regul;
	
	private boolean alive;
	private int port;
	private BufferedReader in;
	private BufferedWriter out;
	private List<String> replies;
	
	public CommServer(int port) {
		this.regul = null;
		
		this.alive = true;
		this.port = port;
		
		this.in = null;
		this.out = null;
		
		this.replies = new LinkedList<String>();
	}
	
	public void setRegul(Regul regul) {
		this.regul = regul;
	}
	
	public void putControlDataPoint(DoublePoint dp) {
		write(cmdPutControlDataPoint + " " + Serialize.serialize(dp));
	}
	
	public void putMeasurementDataPoint(PlotData pd) {
		write(cmdPutMeasurementDataPoint + " " + Serialize.serialize(pd));
	}
	
	public double getRef() {
		synchronized (this) {
			if (out == null) {
				return 0.0;
			}
		}
		double ref = 0.0;
		write(cmdGetRef);
		String reply = waitForReply(cmdGetRef);
		try {
			ref = Double.parseDouble(reply);
		} catch (Exception e) {
			System.out.println("Error: Illegal reference: " + reply);
		}
		return ref;
	}
	
	private synchronized void write(String message) {
		if (out != null) {
			try {
				out.write(message + "\n");
				DebugPrint("Sending: " + message);
				out.flush();
			} catch (IOException e) {
				System.out.println("IOException: " + e.getMessage());
				e.printStackTrace();
			}
		} else {
			DebugPrint("Not connected. Ignoring: " + message);
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
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(port);
			System.out.println("Listening to port: " + port);
		} catch (IOException e) {
			System.out.println("Error: Could not listen on port: " + port);
			System.exit(1);
		}
		
		while (alive) {
			Socket clientSocket = null;
			in = null;
			synchronized (this) {
				out = null;
			}
			try {
				System.out.println("Waiting for client to connect.");
				clientSocket = serverSocket.accept();
				in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				synchronized (this) {
					out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
				}
				System.out.println("Client connected.");
			} catch (IOException e) {
				System.out.println("Error: Accept failed: " + e.getMessage());
			}
			
			try {
				String line = null;
				out.write("hej");
				while (alive && (line = in.readLine()) != null) {
					DebugPrint("Command received: \"" + line + "\"");
					// Commands
					if (line.equals(cmdGetInnerParameters)) {
						write(cmdGetInnerParameters + " " + Serialize.serialize(regul.getInnerParameters()));
					} else 	if (line.startsWith(cmdSetInnerParameters)) {
						String newParameters = line.substring(cmdSetInnerParameters.length() + 1);
						PIParameters p = Serialize.unserializePIParameters(newParameters);
						if (p != null) {
							regul.setInnerParameters(p);
						}
					} else if (line.equals(cmdGetOuterParameters)) {
						write(cmdGetOuterParameters + " " + Serialize.serialize(regul.getOuterParameters()));
					} else if (line.startsWith(cmdSetOuterParameters)) {
						String newParameters = line.substring(cmdSetOuterParameters.length() + 1);
						PIDParameters p = Serialize.unserializePIDParameters(newParameters);
						if (p != null) {
							regul.setOuterParameters(p);
						}
					} else if (line.equals(cmdGetMode)) {
						write(cmdGetMode + " " + regul.getMode());
					} else if (line.equals(cmdSetOFFMode)) {
						regul.setOFFMode();
					} else if (line.equals(cmdSetBEAMMode)) {
						regul.setBEAMMode();
					} else if (line.equals(cmdSetBALLMode)) {
						regul.setBALLMode();
					} else if (line.equals(cmdShutdown)) {
						alive = false;
						regul.shutDown();
					} else if (
							line.startsWith(cmdGetRef)) {
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
			if (clientSocket != null) {
				try {
					in.close();
					synchronized (this) {
						out.close();
					}
					clientSocket.close();
				} catch (IOException e) {
					System.out.println("Error: close: " + e.getMessage());
				}
			}
			System.out.println("Client disconnected.");
		}
		
		try {
			serverSocket.close();
		} catch (IOException e) {
			System.out.println("Error: serverSocket.close(): " + e.getMessage());
		}
	}
	
	private void DebugPrint(String message) {
		//System.out.println(message);
	}
}
