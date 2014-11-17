package simprocess;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import SimEnvironment.*;

public class CommProcess implements Runnable {
	private DataInputStream in;
	private DataOutputStream out;
	private boolean alive;
	private Socket socket;
	private AnalogSource analogInAngle;
	private AnalogSource analogInPosition;
	private AnalogSink analogOut;
	private AnalogSink analogRef;

	public CommProcess(String host, int port, BallAndBeam bb) {
		analogInPosition = bb.getSource(0);
		analogInAngle = bb.getSource(1);
		analogOut = bb.getSink(0);
		analogRef = bb.getSink(1);
		this.in = null;
		this.out = null;
		this.alive = true;
		try {
			this.socket = new Socket(host, port);
			this.in = new DataInputStream(socket.getInputStream());
			this.out = new DataOutputStream(socket.getOutputStream());
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (alive) {
			try {
				analogOut.set(in.readDouble());
				out.writeUTF(Double.toString(analogInPosition.get()) + " " + Double.toString(analogInAngle.get()) + " \n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// dostuff
		}

		if (socket != null) {
			try {
				in.close();
				synchronized (this) {
					out.close();
				}
				socket.close();
			} catch (IOException e) {
				System.out.println("Error: close: " + e.getMessage());
			}
		}
		System.out.println("Client disconnected.");

	}
}