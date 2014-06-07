package com.common.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PrintWriter;
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
				if (server.isAlive())
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
			
			while (numOfClients < 2) {
				try {
					Socket client = serverSocket.accept();
					ReceivingThread receiver = new ReceivingThread(numOfClients + 1, client);
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
		private int ClientID;
		private Socket socket;
		private Message response;
		private PrintWriter writer;
		private DataInputStream input;
		private DataOutputStream output;		
		
		public ReceivingThread(int ClientID, Socket socket) {
			this.cnt = 0;
			this.ClientID = ClientID;
			this.socket = socket;
			try {
				writer = new PrintWriter("Lab3.Scenario2.NguyenK317_Client_" + ClientID + ".txt");
				input = new DataInputStream(socket.getInputStream());
				output = new DataOutputStream(socket.getOutputStream());
				response = new Message();
				response.ClientPort = Integer.toString(socket.getPort());
				response.SocketNumber = Integer.toString(ClientID + 1);
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
			int bytesRead = 0;
			writer.println("Client: " + ClientID);
			while(cnt < 10000) {
				try {
					byte[] buffer = new byte[1024];
					while((input.read(buffer, 0, (bytesRead = input.available()))) == 0);
					String receive = new String(buffer);
					System.out.println(receive);
					writer.write(receive, 0, bytesRead);
					writer.println();
					ProcessInput(receive);
					++cnt;
				}
				catch(Exception error) {
					System.out.println(error.getMessage());
				}
			}
			
			try {
				socket.shutdownInput();
				socket.shutdownOutput();
				socket.close();
				writer.close();
			}
			catch(Exception error) {
				System.out.println(error.getMessage());
			}
			
			long reqEndTime = new Date().getTime();
			System.out.println("Req. run duration (ms): " + (reqEndTime - reqBeginTime));
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
				writer.write(response.toString(),0, response.toString().length());
				writer.println();
			}
			catch(Exception error) {
				System.out.println(error.getMessage());
			}
		}
	}
}
