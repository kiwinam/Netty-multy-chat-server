package server;

public class Message {
	private String targetEmail;
	private String msg;
	
	public Message(String targetEmail, String msg) {
		this.targetEmail = targetEmail;
		this.msg = msg;
	}
	
	public String getTargetEmail() {
		return targetEmail;
	}
	
	public String getMsg() {
		return msg;
	}

}
