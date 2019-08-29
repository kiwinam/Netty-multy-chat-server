package server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.FirebaseMessaging;

public class FCMAdmin {
	private static FCMAdmin fcmAdmin = null;
	
    //private static final String PROJECT_ID = "t-live-3af14";
    //private static final String BASE_URL = "https://fcm.googleapis.com";
    //private static final String FCM_SEND_ENDPOINT = "/v1/projects/" + PROJECT_ID + "/messages:send";

    //private static final String MESSAGING_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
	
    public FCMAdmin() {
		try {
			FileInputStream serviceAccount = new FileInputStream("/home/ubuntu/firebase/t-live-3af14-firebase-adminsdk-8oun4-3e0219a0c5.json");

			FirebaseOptions options  = new FirebaseOptions.Builder()
				    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
				    .setDatabaseUrl("https://t-live-3af14.firebaseio.com/")
				    .build();
			
			FirebaseApp.initializeApp(options);
			fcmAdmin = this;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
    }
    
    public static FCMAdmin getInstance() {
    	if(fcmAdmin != null) {
    		return fcmAdmin;
    	}
    	return null;
    }
  
    public void sendMessage(String token) throws Exception{
    	com.google.firebase.messaging.Message message = com.google.firebase.messaging.Message.builder()
    		    .putData("score", "850")
    		    .putData("time", "2:45")
    		    .setToken(token)
    		    .setAndroidConfig(AndroidConfig.builder()
    		            .setTtl(3600 * 1000) // 1 hour in milliseconds
    		            .setPriority(AndroidConfig.Priority.HIGH)		// 서비스 화이트 리스트에 들어가도록 우선순위를 높음으로 지정한다.
//    		            .setNotification(AndroidNotification.builder()
//    		                .setTitle("$GOOG up 1.43% on the day")
//    		                .setBody("$GOOG gained 11.80 points to close at 835.67, up 1.43% on the day.")
//    		                .setIcon("stock_ticker_update")
//    		                .setColor("#f45342")
//    		                .build())
    		            .build())
    		    .build();
    	
    	String response = FirebaseMessaging.getInstance().send(message);
    	System.out.println("fcm respone : "+response);
    }
	
}
