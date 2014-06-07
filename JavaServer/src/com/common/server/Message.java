package com.common.server;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Message 
{
	public String MessageType;
	public String TimeStamp;
	public String RequestID;
	public String StudentName;
	public String StudentID;
	private String ResponseDelay;
	public String ClientIP;
	public String ClientPort;
	public String SocketNumber;
	public String ServerIP;
	public String ServerPort;
	public String Message;
	public String Scenario;
	
	public Message()
	{
		SimpleDateFormat ft = new SimpleDateFormat("hhmmssSSSS");
		MessageType = "RSP";
		TimeStamp = ft.format(new Date());
		ResponseDelay = "0";
		ServerPort = "6500";
	}
	
	@Override
	public String toString()
	{
		String tmp = MessageType + "|" +
					  TimeStamp + "|" +
					  RequestID + "|" +
					  StudentName + "|" +
					  StudentID + "|" +
					  ResponseDelay + "|" +
					  ClientIP + "|" +
					  ClientPort + "|" +
					  SocketNumber + "|" +
					  ServerIP + "|" +
					  ServerPort + "|" +
					  Message + "|" +
					  Scenario;
		//char header = (char)tmp.length();
		byte[] message = tmp.getBytes();
		byte[] messageLength = ByteBuffer.allocate(4).putInt(message.length).array();
		String data = new String(messageLength, 2, 2) +  new String(message);
		return data;
	}
}
