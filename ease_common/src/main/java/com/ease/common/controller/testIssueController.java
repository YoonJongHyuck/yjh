package com.ease.common.controller;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class testIssueController {	
	
	//get 테스트
	@SuppressWarnings("unchecked")
	@RequestMapping(value="/issue/{isid}",method=RequestMethod.GET)
	public @ResponseBody ResponseEntity<Object> getIssueTest(@PathVariable String isid, @RequestParam(value="id", required=false, defaultValue="nothing") String id) {
	
		HttpStatus resultHttpStatus = HttpStatus.OK;
		JSONObject resultData = new JSONObject();
		resultData.put("isid", isid);
		resultData.put("contents", "get 테스트 성공!");
		ResponseEntity<Object> resultEntity = new ResponseEntity<Object>(resultData, resultHttpStatus);
		
		return resultEntity;
		
	}
	
	
	//SoftManager에 요청 등록 시 사용할 TEST API 
	//요청서버에서 구현할 시 파라미터 셋팅 후 requestSoftManager 호출하여 구현  
	@SuppressWarnings("unchecked")
	@RequestMapping(value="/issue",method=RequestMethod.POST)
	public @ResponseBody ResponseEntity<Object> putIssue(){
		HttpStatus resultHttpStatus = HttpStatus.OK;
		
		JSONObject resultData = new JSONObject();
		Map<String, Object> returnMap = new HashMap<String, Object>();
		String resultString = "";
		String param = "";
		JSONObject requestParamJson = new JSONObject();

		//파라미터 셋팅
		requestParamJson.put("INTFACEID", "ITSM_01");
		requestParamJson.put("SYSCODE", "S001");		
		requestParamJson.put("JOBCODE", "J001");
		requestParamJson.put("RELCODE", "ITSM-001");		
		requestParamJson.put("TITLE", "요청등록 테스트");
		requestParamJson.put("DETAIL", "해당 업무 등록 및 처리 부탁드립니다.");		
		requestParamJson.put("REQUSERID", "user");
		requestParamJson.put("REQUSERNAME", "박현업");
		requestParamJson.put("REQDATE", "2019-12-25T06:00:00.000+09:00");
		requestParamJson.put("REGUSID", "smdev");
		requestParamJson.put("REGUSENAME", "개발자");		
		
		param = requestParamJson.toString();
		
		try {
			returnMap = requestSoftManager(param);
			
			resultHttpStatus = (HttpStatus) returnMap.get("RESPONSE_STATUS");
			resultString = (String) returnMap.get("RESPONSE_DATA");
			
			if(resultHttpStatus == HttpStatus.OK) {	//정상일 경우
				JSONParser parser = new JSONParser();				
				resultData = (JSONObject) parser.parse(resultString);
			}else {
				resultData.put("RESCODE", "10");
				resultData.put("RESMSG", "서버에서 서비스 처리 에러 발생1.");	
			}
			
		} catch (Exception e) {
			resultHttpStatus = HttpStatus.INTERNAL_SERVER_ERROR;			
			resultData.put("RESCODE", "10");
			resultData.put("RESMSG", "서버에서 서비스 처리 에러 발생2.");			
		}
		
		ResponseEntity<Object> resultEntity = new ResponseEntity<Object>(resultData, resultHttpStatus);
		
		return resultEntity;	
	}
	
	
	//HTTP 요청
	private Map<String, Object> requestSoftManager(String param) {
		String softManagerUrl ="http://192.168.0.12:8063/API";
		Map<String, Object> returnMap = new HashMap<String, Object>();
		String sendData 	  = param;
		HttpURLConnection con = null;
		StringBuffer buf 	  = new StringBuffer();
		String returnStr 	  = "";
		int resCode			  = HttpsURLConnection.HTTP_INTERNAL_ERROR;
		
		try {
			URL url 			  = new URL(softManagerUrl);
			con = (HttpURLConnection)url.openConnection();
			con.setConnectTimeout(15000);
			con.setReadTimeout(15000);
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		    con.setDoOutput(true);
		    con.setRequestMethod("POST");
		    con.connect();
		    
		    DataOutputStream dos = new DataOutputStream(con.getOutputStream());
		    
		    dos.write(sendData.getBytes("UTF-8"));
		    dos.flush();
		    dos.close();
		    
		    resCode = con.getResponseCode();
		    
		    if (resCode == HttpsURLConnection.HTTP_OK) {
		    	BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
				int c;
			    
			    while ((c = br.read()) != -1) {
			    	buf.append((char)c);
			    }
			    returnStr = buf.toString();
			    br.close();
			    
		    } else {		 		    	
		    	returnStr = "";		    	
		    }
		    
		} catch (IOException e) {
			resCode		  = HttpsURLConnection.HTTP_INTERNAL_ERROR;
			returnStr 	  = "";
		}
		returnMap.put("RESPONSE_STATUS",  HttpStatus.valueOf(resCode));
		returnMap.put("RESPONSE_DATA", returnStr);		
		
		return returnMap;
	}


	//SoftManager에서 호출할 API
	@SuppressWarnings("unchecked")
	@RequestMapping(value="/result",method=RequestMethod.POST, produces = "application/json; charset=utf-8")
	public @ResponseBody ResponseEntity<Object> getIssueResult(@RequestBody String param, HttpServletRequest request) throws UnsupportedEncodingException{
		
		HttpStatus resultHttpStatus = HttpStatus.OK;
		JSONParser parser = new JSONParser();
		JSONObject requestData = new JSONObject();
		JSONObject resultData = new JSONObject();
		
		try {
			System.out.println("param (before decode) : "+param);
			
			param = URLDecoder.decode(param, "UTF-8"); 
			
			System.out.println("param : "+param);
			
			requestData = (JSONObject) parser.parse(param);
			
			resultData.put("RESCODE", "00");
			resultData.put("RESMSG", "수신 성공");
			resultData.put("ISID", requestData.get("ISID"));
			resultData.put("RELCODE", requestData.get("RELCODE"));
			
		} catch (Exception e) {
//			resultHttpStatus = HttpStatus.BAD_REQUEST;		
			resultData.put("RESCODE", "10");
			resultData.put("RESMSG", "파라미터 확인 요망");
			
			System.out.println("ERROR 발생! 파라미터 확인 요망");
		}
		
		ResponseEntity<Object> resultEntity = new ResponseEntity<Object>(resultData, resultHttpStatus);
		
		return resultEntity;		
	}
	

}
