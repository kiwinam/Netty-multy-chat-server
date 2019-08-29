package server;

import java.util.ArrayList;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

public class ChatHandler extends ChannelInboundHandlerAdapter {
	//private ArrayList<User> userList = new ArrayList<>();
	private ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	private JSONParser jsonParser = new JSONParser();
	
	//private MariaDBConnector mariadb = new MariaDBConnector();
	//private FCMAdmin fcmAdmin = new FCMAdmin();		// 핸들러는 채널 한 개당 하나씩 생김. FCMAdmin 클래스와 MariaDBConnector 클래스를 ChatServer 로 이전하는 방법이 있음.
	
	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		//super.handlerAdded(ctx);
		Channel newChannel = ctx.channel();
		
		System.out.println("new Channel local address : "+newChannel.localAddress().toString());
//		boolean isNew = true;
//		
//		for(User user : ChatServer.userList) {
//			if(user.getUserAddr().equals(newChannel.remoteAddress().toString())) {
//				isNew = false;
//			}
//		}
//		if(isNew) {
//			System.out.println("user added in userList :: ("+newChannel.remoteAddress().toString()+")");
//			ChatServer.userList.add(new User(newChannel, newChannel.remoteAddress().toString()));
//			System.out.println("user list size : "+ChatServer.userList.size());
//		}else {
//			System.out.println("already added user");
//		}
		// 유저가 중첩되서 접속되는 현상을 방지하기 위해 주석 처리함. 서버에 아직 처리 x 18.08.07 18:00 - 박천명
		
		ChatServer.tempUserList.add(new User(newChannel, newChannel.remoteAddress().toString()));
		System.out.println("temp list add");
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {	
		System.out.println("user income");
	}
	

	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		try {
			String message = (String) msg;
			System.out.println("channel Read :" +message);
			JSONObject jsonObject = (JSONObject) jsonParser.parse(message);
			String type = jsonObject.get("type").toString();
			
			// 처음 유저가 접속 하고 난 후 "set" 타입을 보내 정보를 입력한다.
			if(type.equals("set")) {
				String email = jsonObject.get("email").toString();
				String name = jsonObject.get("name").toString();
					
				boolean isExistEmail = false;
				for(User user: ChatServer.userList) {
					// 현재 메시지를 보낸 유저의 어드레스와 일치하면 setUserInfo 를 호출하여 정보를 설정한다.
					
					// 현재 메시지에서 설정 요청한 이메일과 유저 리스트에 있는 이메일이 일치하면 새로운 채널을 추가하지 않는다.
					if(user.getUserEmail().equals(email)) {
						isExistEmail = true;
						System.out.println("이미 존재하는 이메일입니다.");
						break;
					}
				}
				
				if(isExistEmail) {	// 이미 유저 리스트에 추가된 이메일이라면.
					for(User tempUser : ChatServer.tempUserList) {
						if(tempUser.getRemoteAddress().equals(ctx.channel().remoteAddress().toString())) {
							ChatServer.tempUserList.remove(tempUser);
							ctx.close();
							break;
						}
					}
				}else {	//유저 리스트에 없는 이메일이라면.
					for(User tempUser : ChatServer.tempUserList) {
						if(tempUser.getRemoteAddress().equals(ctx.channel().remoteAddress().toString())) {
							tempUser.setUserInfo(name, email);	// 이름과 이메일을 임시 유저에 설정한다.
							ChatServer.userList.add(tempUser);	// 임시 유저를 유저 리스트에 추가한다.
							ChatServer.tempUserList.remove(tempUser);	// 임시 유저 리스트에서 현재 유저를 삭제한다.
							System.out.println("user info set ("+name+"/"+email+")");
							checkRemainMsg(email);		// 현재 설정된 이메일을 가진 유저에게 남겨진 메시지가 있는지 확인한다.
							break;
						}
					}
				}
			}else { 	// 메시지를 전달 받은 경우, 전달해야하는 user 를 찾고 메시지를 전달한다.
				String targetEmail = jsonObject.get("targetEmail").toString();
				boolean isFindUser = false;
			
				for(User user : ChatServer.userList) {
					
					if(user.getUserEmail().equals(targetEmail)) {	// 메시지를 전달해야하는 target email 과 일치하는 유저를 찾는다.
						// 타겟 유저에게 메시지를 전달한다.
						System.out.println("user find");
						isFindUser = true;
						user.getUserChannel().writeAndFlush(message);
						System.out.println(user.getUserEmail()+" >> "+message);
					}
				}
				
				// 메시지를 타겟 유저에게 전달하지 못한 경우 처리 
				if(!isFindUser) {
					// 전달하지 못한 리스트에 임시 저장한다.
					ChatServer.remainMsgList.add(new Message(targetEmail, message));
					
					// MariaDB 에서 전달받을 상대방의 파이어베이스 토큰을 확인한다.
					String targetToken = MariaDBConnector.getInstance().getToken(targetEmail);	// 토큰을 가져온다.
					System.out.println(targetEmail+" :: token >> "+targetToken);
					
					// FCM Admin 를 이용하여 Google Firebase 서버에 다운 스트림 메시징을 요청한다.					
					if(!"logout".equals(targetToken)) {	// 상대방이 TLive 앱에서 로그아웃 한 상태가 아니라면
						// 파이어베이스 메시지 전송 요청
						System.out.println("fcm gogo");
						FCMAdmin.getInstance().sendMessage(targetToken);
						//fcmAdmin.sendMessage(targetToken);
					}else {
						System.out.println("token is logout :: "+targetToken);
					}
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	// 소켓 연결 해제시 유저 목록에서 해제된 유저의 정보를 삭제한다. 
	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception{
		System.out.println("channelUnregistered");
		Channel unRegisterChannel = ctx.channel();
		for(User user : ChatServer.userList) {
			if(user.getRemoteAddress().equals(unRegisterChannel.remoteAddress().toString())){
				System.out.println("remove user : "+user.getUserEmail());
				ChatServer.userList.remove(user);
				return;
			}
		}
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception{
		System.out.println("channelInactive");
		Channel unRegisterChannel = ctx.channel();
		for(User user : ChatServer.userList) {
			if(user.getRemoteAddress().equals(unRegisterChannel.remoteAddress().toString())){
				System.out.println("remove user : "+user.getUserEmail());
				ChatServer.userList.remove(user);
				return;
			}
		}
	}
	
	// 유저 리스트에서 파라미터로 전달받은 이메일을 가지고 있는 유저를 리턴한다.
	private User findUser(String userEmail) {
		for(User user : ChatServer.userList) {
			if(userEmail.equals(user.getUserEmail())) {
				return user;
			}
		}
		return null;
	}
	
	// 소켓에 연결 되었을 때. 소켓 연결이 끊어진 동안 나에게 온 메시지가 있는지 확인하는 메소드
	// 받지 못한 메시지가 존재한다면 다시 전송해준다.
	private void checkRemainMsg(String connectedEmail) {
		ArrayList<Message> remainList = new ArrayList<>();	// 남겨진 메시지를 저장할 리스트
		
		for(Message message : ChatServer.remainMsgList) {	// 전체 받지 못한 리스트를 검사한다. 
			if(connectedEmail.equals(message.getTargetEmail())) {	// 현재 연결된 이메일과 전달 되지 않은 메시지의 타겟 이메일이 같다면 
				remainList.add(message);	// 리스트에 메시지를 추가한다.
				System.out.println("remain msg added");
				
			}
		}
		
		if (remainList.size() != 0) {	// 받지 못한 메시지가 존재한다면
			User user = findUser(connectedEmail);
			if(user != null) {
				for(Message msg : remainList) {
					user.getUserChannel().writeAndFlush(msg.getMsg());
					System.out.println(user.getUserEmail()+" >> "+msg.getMsg());
					ChatServer.remainMsgList.remove(msg);	// 기존에 남아있던 리스트에서 전송한 메시지를 삭제한다.
				}
			}
		}else {
			System.out.println("No remain msg for "+connectedEmail);
		}
	}
}
