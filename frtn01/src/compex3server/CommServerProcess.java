package compex3server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class CommServerProcess implements Runnable {
	private DataInputStream in;
	
	private DataOutputStream out;
	private int port;
	private boolean alive;
	private double angle;
	private double position;
	byte[] indata;

	public CommServerProcess(int port) {
		this.port = port;
		this.in = null;
		this.out = null;
		this.alive = true;
		this.indata = new byte[4];
	}


	public void shutDown() {
		try {
			out.writeDouble(0.0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public double getAngle(){
		return angle;
	}
	public double getPosition(){
		return position;
	}
	
//	private void setInputs(String inputs){
//		String[] temp;
//		System.out.println(inputs);
//		temp = inputs.split(" ");
//		this.position = Double.parseDouble(temp[0]);
//		this.angle = Double.parseDouble(temp[1]);
//		
//	}
	
	private void setInputs(byte[] input){
		byte tempByte = input[0];
		input[0] = (byte) (input[0] & 0x3);
		if((tempByte & 0xC0) == 0xC0){
			this.position = ByteBuffer.wrap(input).getDouble();
		}else{
			this.angle = ByteBuffer.wrap(input).getDouble();
		}
	}
	
	public void setOutput(double output){
		try {
			out.writeDouble(output);
		} catch (IOException e) {
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
				processSocket = serverSocket.accept();
				in = new DataInputStream(processSocket.getInputStream());
				
				synchronized (this) {
					out = new DataOutputStream(processSocket.getOutputStream());
				}
				System.out.println("Process connected.");
			} catch (IOException e) {
				System.out.println("Error: Accept failed: " + e.getMessage());
			}
			try {
				while(alive){
//					setInputs(in.readUTF());
					in.read(indata);
					setInputs(indata);
				}
			} catch (IOException e) {
				System.out.println("Error: read failed: " + e.getMessage());
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
			System.out.println("Process disconnected.");
		}

		try {
			serverSocket.close();
		} catch (IOException e) {
			System.out
					.println("Error: serverSocket.close(): " + e.getMessage());
		}
	}
}