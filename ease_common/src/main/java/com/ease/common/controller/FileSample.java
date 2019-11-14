package com.ease.common.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;

public class FileSample {
	
	//파일 읽기 예제1 : info.properties 파일을 읽어 데이터를 반환한다.
	//호출예제 : int DBTIMEOUT =  Integer.valueOf(retrieveInfo("DBCONN_INFO", "LOGINTIMEOUT"));
	public static String retrieveInfo(String groupName, String targetField) {
		BufferedReader reader = null;
		try {
			String infoFileName = ".\\\\info.properties";
			
			reader = new BufferedReader(new FileReader(new File(infoFileName)));		
			
		} catch (Exception e) {
			System.out.println("info.properties 파일을 찾을 수 없습니다.");
			System.exit(1);
		}
		String strValue = null;
		String strCurrentLine = null;
		boolean bGroupFound = false;

		try {
			while ((strCurrentLine = reader.readLine()) != null) {
				if (strCurrentLine.startsWith("[")) {
					bGroupFound = false;
					if (strCurrentLine.indexOf(groupName) > -1) {
						bGroupFound = true;
					}
				}

				if (bGroupFound) {
					if (strCurrentLine.startsWith(";"))
						continue;

					String[] tokens = strCurrentLine.split("=");

					if (tokens.length < 2 || tokens.length > 2)
						continue;
					if (tokens[0].equals(targetField)) {
						strValue = tokens[1];
						break;
					}
				}
			}
			reader.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
			
		}

		return strValue;
		
		
//		info.properties
//		[DBCONN_INFO]
//				LOGINTIMEOUT=5
//				QUERYTIMEOUT=50
//				DB_HOST1=10.1.21.115
//				DB_HOST2=10.1.21.116
//				DB_PORT=3521
//				DB_DBNAME=KWX
//				DB_USER=itsm
//				DB_PW=itsmlive2019
//
//		[SCM_INFO]
//		#SCMHOST=itsm.kyowon.co.kr
//		SCMHOST=10.1.1.33
//		SCMPORT=8063
//		SCM_USER_ID=admin4
//		#FILE_PATH=Z:\\\\tempattAchFiles\\upfile\\
//		FILE_PATH=C:\\\\upfile\\
		
	}
	
	//text 문서 읽어오기
	public void fileSample2() throws IOException, ClassNotFoundException {
		//lastnum.txt는 최종 작업번호를 업데이트 하는 문서이다.
		File lastnumFile = new File("lastnum.txt");
		String lastSEQ = "";
		
		if(!lastnumFile.exists()) {
			lastSEQ = "0";
			System.out.println("lastSEQ이 존재하지 않습니다. 처음부터 시작합니다.");
			
		}else{
			FileInputStream fis = new FileInputStream(lastnumFile);
			ObjectInputStream ois = new ObjectInputStream(fis);
			lastSEQ = (String) ois.readObject();
			System.out.println("lastSEQ이 존재합니다. lastSEQ : "+lastSEQ);
		}
		
	}
	
	
	
	
	
}
