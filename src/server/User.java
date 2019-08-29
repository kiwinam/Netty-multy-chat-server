package server;

import io.netty.channel.Channel;

public class User {
	private int userId;
	private String userName;
	private String userEmail;
	private Channel channel;	
	private String remoteAddr;
	
	public User(Channel channel, String remoteAddr) {
		this.channel = channel;
		this.remoteAddr = remoteAddr;
	}
	
	public void setUserInfo(String userName, String userEmail) {
		this.userName = userName;
		this.userEmail = userEmail;
	}
	
	public User(int userId, String userName) {
		this.userId = userId;
		this.userName = userName;
	}
	
	public int getUserId() {
		return userId;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public String getUserAddr() {
		return remoteAddr;
	}
	
	public String getUserEmail() {
		return userEmail;
	}
	
	public Channel getUserChannel() {
		return channel;
	}
	
	public String getRemoteAddress() {
		return remoteAddr;
	}
}
