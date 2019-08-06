package com.ease.common.controller;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * common java utility
 * v0.1
 * @author ease
 *
 */
public class EaseCommonUtil {
	
	@SuppressWarnings("unchecked")
	private Map<String,Object> jsonStringToMap(String jsonString){
		try {
			return new ObjectMapper().readValue(new JSONObject(jsonString).toString(), HashMap.class);
		} catch (Exception e) {
			return new HashMap<String,Object>();
		}
	}
	
	
	/**
	 * http connection
	 * @type Content-Type : application/json
	 * @param apiUrl
	 * @param arrayObj
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> httpConnectionforJson(String apiUrl, String arrayObj) throws Exception{
		
		//�ش��Լ� ȣ�� ��
		/*
		try {
			ObjectMapper mapper = new ObjectMapper();
			Map<String, Object> returnMap = getConnection(POINT_SERVER_URL+useCouponUrl, mapper.writeValueAsString(setUseCpnParam(paramMap)));
			
			logger.debug("RES_CD : "+returnMap.get("RES_CD"));
			logger.debug("RES_MSG : "+returnMap.get("RES_MSG"));
			    
		}catch (Exception e) {
			logger.error("[useCpn Exception error] "+e.getMessage(),e);
		}
		*/
		
//		logger.info("apiUrl : " + apiUrl);
		
		URL url 			  = new URL(apiUrl); 	// ��û�� ���� URL
		String sendData 	  = arrayObj;
		HttpURLConnection con = null;
		StringBuffer buf 	  = new StringBuffer();
		Map<String, Object> returnMap = new HashMap<String, Object>();
		String returnStr 	  = "";
		
		try {
			con = (HttpURLConnection)url.openConnection();
			
			con.setConnectTimeout(15000);		//������� timeout ����. 15��
			con.setReadTimeout(15000);			//��Ʈ���б� timeout ����. 15��
			
			con.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
		    con.setDoOutput(true);
		    con.setRequestMethod("POST");
		    con.connect();
		    
		    // �۽��� ������ ����.
		    DataOutputStream dos = new DataOutputStream(con.getOutputStream());
		    dos.write(sendData.getBytes("UTF-8"));
		    dos.flush();
		    dos.close();
		    
		    int resCode = con.getResponseCode();
//		    logger.info("resCode : " + resCode );
		    
		    if (resCode == HttpsURLConnection.HTTP_OK) {
		    	BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
				int c;
			    
			    while ((c = br.read()) != -1) {
			    	buf.append((char)c);
			    }
			    returnStr = buf.toString();
			    br.close();
			    
			    returnMap = jsonStringToMap(returnStr);
			    
		    } else {
		    	returnMap.put("RES_CD", "9999");
		    	returnMap.put("RES_MSG", "HTTP ��� �� ������ �߻��Ͽ����ϴ�.");
		    }
		    
		} catch (IOException e) {			
			e.printStackTrace();
	    	returnMap.put("RES_CD", "9999");
	    	returnMap.put("RES_MSG", "�ý��� ������ �߻��Ͽ����ϴ�.");
		} finally {
		    con.disconnect();
		}
		
		return returnMap;
	}
	
}
