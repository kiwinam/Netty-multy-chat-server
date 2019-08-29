package server;

import java.util.ArrayList;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class ChatServer {
	private final int port;
	
	public static ArrayList<User> userList = new ArrayList<>();		// 현재 소켓이 연결되어 있는 사용자의 리스트
	public static ArrayList<User> tempUserList = new ArrayList<>();	// 현재 소켓이 연결 되어 있고 중복 검사를 위해 잠시 저장되는 리스트 
	
	public static ArrayList<Message> remainMsgList = new ArrayList<>();	// 전달될 타겟 유저의 소켓이 연결되어 있지 않아 전달되지 않은 메시지의 리스트
	
	public FCMAdmin fcmAdmin = new FCMAdmin();	
	public MariaDBConnector mariadb = new MariaDBConnector();
	
	public ChatServer(int port) {
		super();
		this.port = port;
	}
	
	public static void main(String[] args) throws Exception {
		new ChatServer(7777).run();
	}
	
	public void run() throws Exception{
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workerGroup)
			.channel(NioServerSocketChannel.class)
			.handler(new LoggingHandler(LogLevel.INFO))
			.childHandler(new ChatInitailizer());
			
			bootstrap.bind(port).sync().channel().closeFuture().sync();
		}finally{
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
			
		}
	}
}
