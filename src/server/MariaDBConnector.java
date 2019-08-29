package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MariaDBConnector {
	
	private static MariaDBConnector mariadb = null;
	
	private Connection conn = null;
	private Statement state = null;
	private PreparedStatement preState = null;
	private ResultSet resultSet = null;
	
	private String driver = "org.mariadb.jdbc.Driver";
	private String url = "jdbc:mariadb://localhost:3306/tlive";
	private String user = "charlie";
	private String password = "akfcjs!2aud";
	
	public MariaDBConnector() {
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(url, user, password);
			if(conn != null) {
				System.out.println("데이터베이스 접속 성공");
				mariadb = this;
			}
		}catch(ClassNotFoundException e) {
			System.out.println("드라이버 로드 실패");
		}catch(SQLException e) {
			System.out.println("데이터베이스 접속 실패");
		}
	}
	
	public static MariaDBConnector getInstance() {
		if(mariadb != null) {
			return mariadb;
		}
		return null; 
	}
	
	// 파라터로 전달받은 이메일의 파이어베이스 토큰을 리턴하는 메소드
	public String getToken(String email) throws Exception {
		String token = null;
		preState = conn.prepareStatement("SELECT firebaseToken FROM user WHERE email = ?");
		preState.setString(1, email);
		
		resultSet = preState.executeQuery();
		
		if(resultSet.next()) {
			token = resultSet.getString("firebaseToken");
		}else {
			token = "error";
		}
		return token;
	}
}
