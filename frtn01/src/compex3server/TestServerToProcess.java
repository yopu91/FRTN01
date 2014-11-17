package compex3server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class TestServerToProcess implements Runnable {
//	private BufferedReader in;
	private DataInputStream in;
	
	private DataOutputStream out;
	private TestRegul regul;
	private int port;
	private boolean alive;
	private double angle;
	private double position;

	public TestServerToProcess(TestRegul regul, int port) {
		this.regul = regul;
		this.port = port;

		this.in = null;
		this.out = null;
		this.alive = true;
	}


	public void shutDown() {
		try {
			out.writeDouble(0.0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public double getAngle(){
		return angle;
	}
	public double getPosition(){
		return position;
	}
	
	private void setInputs(String inputs){
		String[] temp;
		System.out.println(inputs);
		temp = inputs.split(" ");
		this.position = Double.parseDouble(temp[0]);
		this.angle = Double.parseDouble(temp[1]);
		
	}
	
	public void setOutput(double output){
		try {
			out.writeDouble(output);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
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
			Socket processSocket = null;
			in = null;
			synchronized (this) {
				out = null;
			}
			try {
				System.out.println("Waiting for process to connect.");
				//
				System.out.println("A");
				//
				processSocket = serverSocket.accept();
				//
				System.out.println("B");
				//
//				in = new BufferedReader(new InputStreamReader(
//						processSocket.getInputStream()));
				
				//TEST
				in = new DataInputStream(processSocket.getInputStream());
				
				synchronized (this) {
					out = new DataOutputStream(processSocket.getOutputStream());
				}
				System.out.println("Process connected.");
			} catch (IOException e) {
				System.out.println("Error: Accept failed: " + e.getMessage());
			}
			try {
				String line = null;
			//	while (alive && (line = in.readLine()) != null) {
			//		setInputs(in.readLine());
				while(alive){
					setInputs(in.readUTF());
					// dostuff
				}
			} catch (IOException e) {
				System.out.println("Error: readLine failed: " + e.getMessage());
			}
			if (processSocket != null) {
				try {
					in.close();
					synchronized (this) {
						out.close();
					}
					processSocket.close();
				} catch (IOException e) {
					System.out.println("Error: close: " + e.getMessage());
				}
			}
			System.out.println("Client disconnected.");
		}

		try {
			serverSocket.close();
		} catch (IOException e) {
			System.out
					.println("Error: serverSocket.close(): " + e.getMessage());
		}
	}
}