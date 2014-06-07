package com.common.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;

public class Main 
{	
	public static void main(String[] args) {
			ListeningClientThread server = new ListeningClientThread();
			server.start();
			try {
				server.join();
			}
			catch(Exception error) {
				System.out.println(error.getMessage());
			}
	}
	
	/*
	 * This thread wait for client to connect
	 */
	protected static class ListeningClientThread extends Thread
	{
		private ServerSocket serverSocket;
		private ArrayList<Thread> clientsThread;
		
		public ListeningClientThread() {
			try {
				serverSocket = new ServerSocket(6500);
				serverSocket.setSoTimeout(10000);
				clientsThread = new ArrayList<Thread>();
			}
			catch (Exception error) {
				System.out.println(error.getMessage());
			}
		}
		
		@Override
		public void run() {
			int numOfClients = 0;
			
			while (numOfClients < 1) {
				try {
					Socket client = serverSocket.accept();
					ReceivingThread receiver = new ReceivingThread(client);
					receiver.start();
					clientsThread.add(receiver);
					++numOfClients;
				}
				catch(Exception error) {
					System.out.println(error.getMessage());
				}
			}
			
			for(int i = 0; i < clientsThread.size(); ++i) {
				try {
					clientsThread.get(i).join();
					if (!clientsThread.get(i).isAlive())
						System.out.println("Client " + i + " done...");
					else
						System.out.println("Client " + i + " not done...");
				}
				catch (Exception error) {
					System.out.println(error.getMessage());
				}
			}
			
			try {
				serverSocket.close();
			}
			catch (Exception error) {
				System.out.println(error.getMessage());
			}
		}
	}
	
	/*
	 * this thread listening for incoming requests and sending response back to client
	 */
	protected static class ReceivingThread extends Thread
	{
		private int cnt;
		private Socket socket;
		private DataInputStream input;
		private DataOutputStream output;
		private Message response;
		
		public ReceivingThread(Socket socket) {
			cnt = 0;
			this.socket = socket;
			try {
				input = new DataInputStream(socket.getInputStream());
				output = new DataOutputStream(socket.getOutputStream());
				response = new Message();
				response.ClientPort = Integer.toString(socket.getPort());
				response.SocketNumber = "1";
				response.ServerIP = socket.getLocalAddress().toString().substring(1);
				response.ServerPort = Integer.toString(socket.getLocalPort());
				response.Message = "OIT-Good-Req";
				response.Scenario = "1";				
			}
			catch(Exception error) {
				System.out.println(error.getMessage());
			}
		}
		
		@Override
		public void run() {
			long reqBeginTime = new Date().getTime();
			
			while(cnt < 10000) {
				try {
					byte[] buffer = new byte[1024];
					while((input.read(buffer, 0, input.available())) == 0);
					String receive = new String(buffer);
					System.out.println(receive);
					ProcessInput(receive);
					++cnt;
				}
				catch(Exception error) {
					System.out.println(error.getMessage());
				}
			}
			
			cancel();
			long reqEndTime = new Date().getTime();
			System.out.println("Req. run duration (ms): " + (reqEndTime - reqBeginTime));
		}
		
		public void cancel() {
			try {
				socket.shutdownInput();
				socket.shutdownOutput();
				socket.close();
			}
			catch(Exception error) {
				System.out.println(error.getMessage());
			}
		}
		
		private void ProcessInput(String data) {
			try {
				String[] parts = data.split("\\|");				
				response.TimeStamp = parts[1];
				response.RequestID = parts[2];
				response.StudentName = parts[3];
				response.StudentID = parts[4];
				response.ClientIP = parts[6];								
				output.write(response.toString().getBytes());
			}
			catch(Exception error) {
				System.out.println(error.getMessage());
			}
		}
	}
}
