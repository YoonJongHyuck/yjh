package com.ease.common;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.apache.catalina.manager.util.SessionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import net.sf.ehcache.CacheManager;

/** 
 * SAP - ���굥���� SAP ���� ���� API
 * 
 * sapjco3.dll �Ǵ� sapjco3.so ������ SAP �ý��� �����ڿ��� �����޾ƾ� �Ѵ�.
 * 
 * sapjco3 ���� ���� ��� : lib�� �ִ� sapjco3.dll ������ System32�� �����Ѵ�.
 * �Ǵ� ��Ĺ ȯ�溯�� �� -Djava.library.path="C:\yjh\workspace\maeil-backoffice\WebContent\WEB-INF\lib �� ���� dll������ �ִ� ��ġ�� �����Ѵ�.
 * ������ ������ ���, sapjco3.so(64bit)�� ���̺귯���� ������ �� LD_LIBRARY_PATH �� ����ش�. ( ex: LD_LIBRARY_PATH=/apps/stage/MAEIL_ADMIN/lib/ )
 */

public class EaseCommonUtil_SAP {
	
	


//	@Autowired
//	private PRCRDMapper prCrdMapper;
	
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	//JCO Ŀ�ؼ� ����
	String sapAshost = "";	
	String sapSysnr	 = "";
	String sapClient = "";
	String sapUser	 = "";
	String sapPasswd = "";
	String sapLang	 = "";
	
	String coopCoCd  = "";	
	String abapName	 = "";			//�������ϸ����� ����
	String eReturnType	 	= "";	//SAP���۰�� TYPE
	String eReturnMessage	= "";	//SAP���۰�� MESSAGE	
	String functionName	 	= "";	//SAP �Լ���Ī
	String tableName		= "";	//SAP table ��Ī
	String userId 	 = "";	
	String callUser  = "";			//ȣ��ý���, 1:BATCH, 2:BACKOFFICE
	String resCd	 = "";			//�����ڵ� (00000:����, ��Ÿ) 	
	String resMsg	 = "";
	
	SimpleDateFormat ymdFormat = new SimpleDateFormat("yyyyMMdd");
	SimpleDateFormat hmsFormat = new SimpleDateFormat("HHmmss");
	String trmsYmd = "";	//��������
	String trmsHms = "";	//���۽ð�
	
	Map<String, String> resultMap = new HashMap<String, String>();	//�����ڵ�, �޽����� ���� ��
	Map<String, String> dateMap = new HashMap<String, String>();	//�������� �� �ð��� ���� ��
			
	/** 
	 * ����Ʈī�� ����/������� ���� �ش� ����翡 ����
	 * @param REQ_DT ��������(YYYYMMDD)	 
	 * @param COOPCO_CD ����� �ڵ� 	  
	 * @param CALL_USER 1:BATCH, 2:ADMIN
	 * @return RES_CD �����ڵ� (00000:success, 00010:no data, 00020:sap transmission failure, 00030:sap process failure, 00040:sap success but update failure, 00050: prCrdService error )
	 * @return RES_MSG ����޽���
	 */
	
	/*
	public Map<String, String> giftCardActvPtclTrms(Map<String, Object> param){
		
		coopCoCd = (String)param.get("COOPCO_CD");
		
		//�ش� ������� ����/��� ���� ������ ��ȸ 
		List<Map<String, Object>> giftCardActvPtclList = getGiftCardActvPtclList(coopCoCd,param);
		
		if(giftCardActvPtclList.size() == 0){
			resultMap.put("RES_CD", "00010");
			resultMap.put("RES_MSG", "no data");
			return resultMap;		//��ȸ��� ����
		}
				
		//RFC �Լ��� �� ���� ���̺� ����
		if("7030".equals(coopCoCd)){			//�����
			functionName = "ZFI_MAEILDO_MEMBERSHIP_SALES";	//SAP �Լ���
			tableName = "I_ZPOST0160";						//Ÿ�� ���̺��
		}else if("7010".equals(coopCoCd)){		//��������
			functionName = "ZFI_TMEMBERSHIP_SALES_INFO";	
			tableName = "I_ZPOST1010";						
		}
		
		try {
			//���� ������Ƽ ����
			abapName = setConnectProperties(coopCoCd);
			
			//SAP Ŀ�ؼ� �ν��Ͻ� ������
			JCoDestination destination = JCoDestinationManager.getDestination(abapName);
			
			//��������Ȯ��
			logger.info("jco Attributes : "+destination.getAttributes());
			
			//SAP Ŀ�ؼ����κ��� �ʿ��Լ� �ν��Ͻ� ȣ��
			JCoFunction function = destination.getRepository().getFunction(functionName);
			
			if(function == null){
				throw new RuntimeException(functionName+" not found in SAP.");
			}		
			
			//�ش��Լ��� ���̺� �Ķ���� �������� : ���̺��� input/output ��� ��밡��. �� �ڵ�� �Է¿����� ���̺� �Ķ���� ���
			JCoTable codes = function.getTableParameterList().getTable(tableName);
			
			//���۵����� ����
			codes = setActvPtclSapParam(codes, giftCardActvPtclList, coopCoCd);
			
			//SAP �Լ� ����ȣ��
			function.execute(destination);
			
			//ó����� �Ķ���� ��������
			JCoParameterList output = function.getExportParameterList();
			
			//�޽��� ����: S ����, E ����, W ���, I ����, A �ߴ�
			eReturnType = (String) output.getStructure("E_RETURN").getValue("TYPE");
			eReturnMessage = (String) output.getStructure("E_RETURN").getValue("MESSAGE");
			
			logger.info("eReturnType : "+eReturnType);				
			logger.info("eReturnMessage : "+eReturnMessage);
			
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("RES_CD", "00020");
			resultMap.put("RES_MSG", "sap transmission failure");
			return resultMap;		//SAP ���� ����
		}
		
		//�ش� ����� �������� ���� �� ���ۿ��� update
		if("S".equals(eReturnType)){
			try{
				resCd = updateGiftCardActvPtcl(coopCoCd, param);
				resMsg = "success";
			} catch(Exception e) {
				e.printStackTrace();
				resCd = "00040" ;		//00040 : SAP ���� ����, �������� ������Ʈ ����
				resMsg = "sap success but update failure";
			}
		}else{
			resCd = "00030" ; //sap ���۽���(eReturn:error)
			resMsg = "sap process failure";
		}
		
		resultMap.put("RES_CD", resCd);
		resultMap.put("RES_MSG", resMsg);
		
		return resultMap;	
	}
	
	*/
	
	/** 
	 * ����Ʈī�� ���� �Ҹ� ���� �ش� ����翡 ����
	 * @param REQ_DT ��������(YYYYMMDD)	 
	 * @param COOPCO_CD ����� �ڵ� 	  
	 * @param CALL_USER 1:BATCH, 2:ADMIN
	 * @return RES_CD �����ڵ� (00000:success, 00010:no data, 00020:sap transmission failure, 00030:sap process failure, 00040:sap success but update failure, 00050: prCrdService error )
	 * @return RES_MSG ����޽���
	 */
	
	/*
	public Map<String, String> giftCardActvXtnctPtclTrms(Map<String, Object> param){
		
		coopCoCd = (String)param.get("COOPCO_CD");
		
		//�ش� ������� ����/��� ���� ������ ��ȸ 
		List<Map<String, Object>> giftCardActvXtnctPtclList = getGiftCardActvXtnctPtclList(coopCoCd,param);
		
		if(giftCardActvXtnctPtclList.size() == 0){
			resultMap.put("RES_CD", "00010");
			resultMap.put("RES_MSG", "no data");
			return resultMap;		//��ȸ��� ����
		}
		
		//RFC �Լ��� �� ���� ���̺� ����
		if("7010".equals(coopCoCd)){		//��������
			functionName = "ZFI_TMEMBERSHIP_EXTINCTION_INFO";	
			tableName = "I_ZPOST1050";						
		}
				
		try {
			//���� ������Ƽ ����
			abapName = setConnectProperties(coopCoCd);
			
			//SAP Ŀ�ؼ� �ν��Ͻ� ������
			JCoDestination destination = JCoDestinationManager.getDestination(abapName);
			
			//��������Ȯ��
			logger.info("jco Attributes : "+destination.getAttributes());
			
			//SAP Ŀ�ؼ����κ��� �ʿ��Լ� �ν��Ͻ� ȣ��
			JCoFunction function = destination.getRepository().getFunction(functionName);
			
			if(function == null){
				throw new RuntimeException(functionName+" not found in SAP.");
			}		
			
			//�ش��Լ��� ���̺� �Ķ���� �������� : ���̺��� input/output ��� ��밡��. �� �ڵ�� �Է¿����� ���̺� �Ķ���� ���
			JCoTable codes = function.getTableParameterList().getTable(tableName);
			
			//���۵����� ����
			codes = setActvXtnctPtclSapParam(codes, giftCardActvXtnctPtclList, coopCoCd);
			
			//SAP �Լ� ����ȣ��
			function.execute(destination);
			
			//ó����� �Ķ���� ��������
			JCoParameterList output = function.getExportParameterList();
			
			//�޽��� ����: S ����, E ����, W ���, I ����, A �ߴ�
			eReturnType = (String) output.getStructure("E_RETURN").getValue("TYPE");
			eReturnMessage = (String) output.getStructure("E_RETURN").getValue("MESSAGE");
			
			logger.info("eReturnType : "+eReturnType);				
			logger.info("eReturnMessage : "+eReturnMessage);
			
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("RES_CD", "00020");
			resultMap.put("RES_MSG", "sap transmission failure");
			return resultMap;		//SAP ���� ����
		}
		
		//�ش� ����� �������� ���� �� ���ۿ��� update
		if("S".equals(eReturnType)){
			try{
				resCd = updateGiftCardActvXtnctPtcl(coopCoCd, param);
				resMsg = "success";
			} catch(Exception e) {
				e.printStackTrace();
				resCd = "00040" ;		//00040 : SAP ���� ����, �������� ������Ʈ ����
				resMsg = "sap success but update failure";
			}
		}else{
			resCd = "00030" ; //sap ���۽���(eReturn:error)
			resMsg = "sap process failure";
		}
		
		resultMap.put("RES_CD", resCd);
		resultMap.put("RES_MSG", resMsg);
		
		return resultMap;	
	}
*/

	/** 
	 * ����Ʈī�� ��� ����  �ش� ����翡 ���� API
	 * @param REQ_DT �������� (��ġ ���� -1�� ,YYYYMMDD)	 
	 * @param COOPCO_CD ����� �ڵ� 			
	 * @param CALL_USER 1:BATCH, 2:ADMIN		
	 * @return RES_CD �����ڵ� (00000:success, 00010:no data, 00020:sap transmission failure, 00030:sap process failure, 00040:sap success but update failure, 00050: prCrdService error )
	 * @return RES_MSG ����޽���
	 */
	/*
	public Map<String, String> giftCardUsePtclTrms(Map<String, Object> param) {
		coopCoCd = (String)param.get("COOPCO_CD");
		
		//�ش� ������� ��� ���� ������ ��ȸ 
		List<Map<String, Object>> giftCardUsePtclList = getGiftCardUsePtclList(coopCoCd,param);
		
		if(giftCardUsePtclList.size() == 0){
			resultMap.put("RES_CD", "00010");
			resultMap.put("RES_MSG", "no data");
			return resultMap;		//��ȸ��� ����
		}
		
		//RFC �Լ��� �� ���� ���̺� ����
		if("7010".equals(coopCoCd)){		//��������
			functionName = "ZFI_TMEMBERSHIP_USING_INFO";
			tableName = "I_ZPOST1040";
		}
		
		try {
			//���� ������Ƽ ����
			abapName = setConnectProperties(coopCoCd);
			
			//SAP Ŀ�ؼ� �ν��Ͻ� ������
			JCoDestination destination = JCoDestinationManager.getDestination(abapName);
			
			//��������Ȯ��
			logger.info("jco Attributes : "+destination.getAttributes());
			
			//SAP Ŀ�ؼ����κ��� �ʿ��Լ� �ν��Ͻ� ȣ��
			JCoFunction function = destination.getRepository().getFunction(functionName);
			
			if(function == null){
				throw new RuntimeException(functionName+" not found in SAP.");
			}		
			
			//�ش��Լ��� ���̺� �Ķ���� �������� : ���̺��� input/output ��� ��밡��. �� �ڵ�� �Է¿����� ���̺� �Ķ���� ���
			JCoTable codes = function.getTableParameterList().getTable(tableName);
			
			//���۵����� ����
			codes = setUsePtclSapParam(codes, giftCardUsePtclList, coopCoCd);
			
			//SAP �Լ� ����ȣ��
			function.execute(destination);
			
			//ó����� �Ķ���� ��������
			JCoParameterList output = function.getExportParameterList();
			
			//�޽��� ����: S ����, E ����, W ���, I ����, A �ߴ�
			eReturnType = (String) output.getStructure("E_RETURN").getValue("TYPE");
			eReturnMessage = (String) output.getStructure("E_RETURN").getValue("MESSAGE");
			
			logger.info("eReturnType : "+eReturnType);				
			logger.info("eReturnMessage : "+eReturnMessage);
			
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("RES_CD", "00020");
			resultMap.put("RES_MSG", "sap transmission failure");
			return resultMap;		//SAP ���� ����
		}
		
		//�ش� ����� �������� ���� �� ���ۿ��� update
		if("S".equals(eReturnType)){
			try{
				resCd = updateGiftCardUsePtcl(coopCoCd, param);
				resMsg = "success";
			} catch(Exception e) {
				e.printStackTrace();
				resCd = "00040" ;		//00040 : SAP ���� ����, �������� ������Ʈ ����
				resMsg = "sap success but update failure";
			}
		}else{
			resCd = "00030" ; //sap ���۽���(eReturn:error)
			resMsg = "sap process failure";
		}
		
		resultMap.put("RES_CD", resCd);
		resultMap.put("RES_MSG", resMsg);
		
		return resultMap;	
	}
	*/

	/** 
	 * ����Ʈī�� ȯ�� ó�� ��� ���� API
	 * @param REQ_DT �������� (��ġ ���� -1�� ,YYYYMMDD)	 
	 * @param COOPCO_CD ����� �ڵ� 			
	 * @param CALL_USER 1:BATCH, 2:ADMIN		
	 * @return RES_CD �����ڵ� (00000:success, 00010:no data, 00020:sap transmission failure, 00030:sap process failure, 00040:sap success but update failure, 00050: prCrdService error )
	 * @return RES_MSG ����޽���
	 */
	/*
	public Map<String, String> giftCardRefdReqPtclRecvT(Map<String, Object> param) {
		coopCoCd = (String)param.get("COOPCO_CD");
		
		//����Ʈī�� ȯ�� ������ ����Ʈ ��ȸ 
		param.put("CRD_ST", "92");			//ī�� ����[00:���, 10:����, 90:�нǽŰ�, 91:����, 92:ȯ��, 99:���]
		param.put("REFD_ST", "03");			//ȯ�� ����[00:����,01:ȯ�ҽ�û,02:ȯ�ҿϷ�, 03:ȯ��������,99:ȯ�ҿ���]
		param.put("REFD_ST_AFTER", "02");	//ȯ�ҽ�û ���� �� ��ȯ�� ���°� -> 02:ȯ�ҿϷ�
		
		List<Map<String, Object>> giftCardRefdReqPtclRecvList = getGiftCardRefdReqPtclRecvList(coopCoCd,param);
		
		if(giftCardRefdReqPtclRecvList.size() == 0){
			resultMap.put("RES_CD", "00010");
			resultMap.put("RES_MSG", "no data");
			return resultMap;		//��ȸ��� ����
		}
		
		//RFC �Լ��� �� ���� ���̺� ����
		if("7010".equals(coopCoCd)){		//��������
			functionName = "ZFI_TMEMBERSHIP_RETURN";	
			tableName = "T_ZPOST1030";
		}
		
		try {
			//���� ������Ƽ ����
			abapName = setConnectProperties(coopCoCd);
			
			//SAP Ŀ�ؼ� �ν��Ͻ� ������
			JCoDestination destination = JCoDestinationManager.getDestination(abapName);
			
			//��������Ȯ��
			logger.info("jco Attributes : "+destination.getAttributes());
			
			//SAP Ŀ�ؼ����κ��� �ʿ��Լ� �ν��Ͻ� ȣ��
			JCoFunction function = destination.getRepository().getFunction(functionName);					
			
			if(function == null){
				throw new RuntimeException(functionName+" not found in SAP.");
			}		
			
			//�ش��Լ��� ���̺� �Ķ���� �������� : ���̺��� input/output ��� ��밡��. �� �ڵ�� �Է¿����� ���̺� �Ķ���� ���
			JCoTable codes = function.getTableParameterList().getTable(tableName);
			
			//���۵����� ����
			codes = setRefdReqPtclRecvSapParam(codes, giftCardRefdReqPtclRecvList, coopCoCd);
			
			//SAP �Լ� ����ȣ��
			function.execute(destination);
			
			//ó����� �Ķ���� ��������
			JCoParameterList output = function.getExportParameterList();
						
			param = setReturnList(param, codes);
			
			//�޽��� ����: S ����, E ����, W ���, I ����, A �ߴ�
			eReturnType = (String) output.getStructure("E_RETURN").getValue("TYPE");
			eReturnMessage = (String) output.getStructure("E_RETURN").getValue("MESSAGE");
			
			logger.info("eReturnType : "+eReturnType);				
			logger.info("eReturnMessage : "+eReturnMessage);
			
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("RES_CD", "00020");
			resultMap.put("RES_MSG", "sap transmission failure");
			return resultMap;		//SAP ���� ����
		}
				
		
		//ȯ�� ó�� ��� ��ȸ ���� �� ������ update
		if("S".equals(eReturnType)){
			try {
				resCd = updateGiftCardRefdReqPtclRecv(coopCoCd, param);
				resMsg = "success";
			} catch (Exception e) {				
				e.printStackTrace();
				TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();	//���� �ѹ�ó��
				resCd = "00040" ;		//00040 : SAP ���� ����, �������� ������Ʈ ����
				resMsg = "sap success but update failure";
			}
		}else{
			resCd = "00030" ; //sap ���۽���(eReturn:error)
			resMsg = "sap process failure";
		}
		
		resultMap.put("RES_CD", resCd);
		resultMap.put("RES_MSG", resMsg);
		
		return resultMap;	
	}

*/
	
	/** 
	 * ����Ʈī��ȯ�� ��û ����  �ش� ����翡 ���� API
	 * @param REQ_DT �������� (��ġ ���� -1�� ,YYYYMMDD)	 
	 * @param COOPCO_CD ����� �ڵ� 			
	 * @param CALL_USER 1:BATCH, 2:ADMIN		
	 * @return RES_CD �����ڵ� (00000:success, 00010:no data, 00020:sap transmission failure, 00030:sap process failure, 00040:sap success but update failure, 00050: prCrdService error )
	 * @return RES_MSG ����޽���
	 */
	/*
	public Map<String, String> giftCardRefdReqPtclTrmsT(Map<String, Object> param) {
		coopCoCd = (String)param.get("COOPCO_CD");
		
		//����Ʈī�� ȯ�� ��û ����Ʈ ��ȸ 
		param.put("CRD_ST", "92");			//ī�� ����[00:���, 10:����, 90:�нǽŰ�, 91:����, 92:ȯ��, 99:���]
		param.put("REFD_ST", "01");			//ȯ�� ����[00:����,01:ȯ�ҽ�û,02:ȯ�ҿϷ�, 03:ȯ��������,99:ȯ�ҿ���]
		param.put("REFD_ST_AFTER", "03");	//ȯ�ҽ�û ���� �� ��ȯ�� ���°� -> 03:ȯ��������
		
		List<Map<String, Object>> giftCardRefdReqPtclList = getGiftCardRefdReqPtclList(coopCoCd,param);
		
		if(giftCardRefdReqPtclList.size() == 0){
			resultMap.put("RES_CD", "00010");
			resultMap.put("RES_MSG", "no data");
			return resultMap;		//��ȸ��� ����
		}
		
		//RFC �Լ��� �� ���� ���̺� ����
		if("7010".equals(coopCoCd)){		//��������
			functionName = "ZFI_TMEMBERSHIP_SALES_INFO";	
			tableName = "I_ZPOST1010";
		}
		
		try {
			//���� ������Ƽ ����
			abapName = setConnectProperties(coopCoCd);
			
			//SAP Ŀ�ؼ� �ν��Ͻ� ������
			JCoDestination destination = JCoDestinationManager.getDestination(abapName);
			
			//��������Ȯ��
			logger.info("jco Attributes : "+destination.getAttributes());
			
			//SAP Ŀ�ؼ����κ��� �ʿ��Լ� �ν��Ͻ� ȣ��
			JCoFunction function = destination.getRepository().getFunction(functionName);
			
			if(function == null){
				throw new RuntimeException(functionName+" not found in SAP.");
			}		
			
			//�ش��Լ��� ���̺� �Ķ���� �������� : ���̺��� input/output ��� ��밡��. �� �ڵ�� �Է¿����� ���̺� �Ķ���� ���
			JCoTable codes = function.getTableParameterList().getTable(tableName);
			
			//���۵����� ����
			codes = setRefdReqPtclSapParam(codes, giftCardRefdReqPtclList, coopCoCd);
			
			//SAP �Լ� ����ȣ��
			function.execute(destination);
			
			//ó����� �Ķ���� ��������
			JCoParameterList output = function.getExportParameterList();
			
			//�޽��� ����: S ����, E ����, W ���, I ����, A �ߴ�
			eReturnType = (String) output.getStructure("E_RETURN").getValue("TYPE");
			eReturnMessage = (String) output.getStructure("E_RETURN").getValue("MESSAGE");
			
			logger.info("eReturnType : "+eReturnType);				
			logger.info("eReturnMessage : "+eReturnMessage);
			
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("RES_CD", "00020");
			resultMap.put("RES_MSG", "sap transmission failure");
			return resultMap;		//SAP ���� ����
		}
		
		//ȯ�� ��û ���� ���� ���� �� ���ۿ��� update
		if("S".equals(eReturnType)){
			try {
				resCd = updateGiftCardRefdReqPtcl(coopCoCd, param, giftCardRefdReqPtclList);
				resMsg = "success";
			} catch (Exception e) {				
				e.printStackTrace();
				TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();	//���� �ѹ�ó��
				resCd = "00040" ;		//00040 : SAP ���� ����, �������� ������Ʈ ����
				resMsg = "sap success but update failure";
			}
		}else{
			resCd = "00030" ; //sap ���۽���(eReturn:error)
			resMsg = "sap process failure";
		}
		
		resultMap.put("RES_CD", resCd);
		resultMap.put("RES_MSG", resMsg);
		
		return resultMap;	
	}
*/

	/** 
	 * ����Ʈī�� ������ ���� �ش� ����翡 ����
	 * @param REQ_DT ������ ���� ��ȸ ������ (��ġ ���� -1�� ,YYYYMMDD)	
	 * @param COOPCO_CD ����� �ڵ� 			
	 * @param CALL_USER 1:BATCH, 2:ADMIN		
	 * @return RES_CD �����ڵ� (00000:success, 00010:no data, 00020:sap transmission failure, 00030:sap process failure, 00040:sap success but update failure, 00050: prCrdService error )
	 * @return RES_MSG ����޽���
	 */
	/*
	public Map<String, String> giftCardStmpTaxTrms(Map<String, Object> param) {
		coopCoCd = (String)param.get("COOPCO_CD");
		
		//�ش� ������� ������ ���� ������ ��ȸ 
		List<Map<String, Object>> giftCardStmpTaxList = getGiftCardStmpTaxList(coopCoCd,param);
		
		if(giftCardStmpTaxList.size() == 0){
			resultMap.put("RES_CD", "00010");
			resultMap.put("RES_MSG", "no data");
			return resultMap;		//��ȸ��� ����
		}		
		
		//RFC �Լ��� �� ���� ���̺� ����
		if("7010".equals(coopCoCd)){		//��������
			functionName = "ZFI_TMEMBERSHIP_STAMP_INFO";	//�������� SAP �Լ���
			tableName = "I_ZPOST1020";						//�������� Ÿ�� ���̺��
		}
		
		try {
			//���� ������Ƽ ����
			abapName = setConnectProperties(coopCoCd);
			
			//SAP Ŀ�ؼ� �ν��Ͻ� ������
			JCoDestination destination = JCoDestinationManager.getDestination(abapName);
			
			//��������Ȯ��
			logger.info("jco Attributes : "+destination.getAttributes());
			
			//SAP Ŀ�ؼ����κ��� �ʿ��Լ� �ν��Ͻ� ȣ��
			JCoFunction function = destination.getRepository().getFunction(functionName);
			
			if(function == null){
				throw new RuntimeException(functionName+" not found in SAP.");
			}		
			
			//�ش��Լ��� ���̺� �Ķ���� �������� : ���̺��� input/output ��� ��밡��. �� �ڵ�� �Է¿����� ���̺� �Ķ���� ���
			JCoTable codes = function.getTableParameterList().getTable(tableName);
			
			//���۵����� ����
			codes = setStmpTaxSapParam(codes, giftCardStmpTaxList, coopCoCd);
			
			//SAP �Լ� ����ȣ��
			function.execute(destination);
			
			//ó����� �Ķ���� ��������
			JCoParameterList output = function.getExportParameterList();
			
			//�޽��� ����: S ����, E ����, W ���, I ����, A �ߴ�
			eReturnType = (String) output.getStructure("E_RETURN").getValue("TYPE");
			eReturnMessage = (String) output.getStructure("E_RETURN").getValue("MESSAGE");
			
			logger.info("eReturnType : "+eReturnType);				
			logger.info("eReturnMessage : "+eReturnMessage);
			
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("RES_CD", "00020");
			resultMap.put("RES_MSG", "sap transmission failure");
			return resultMap;		//SAP ���� ����
		}
			
		
		//�ش� ����� �������� ���� �� ���ۿ��� update
		if("S".equals(eReturnType)){
			try{
				resCd = updateGiftCardStmpTax(coopCoCd, param);
				resMsg = "success";
			} catch(Exception e) {
				e.printStackTrace();
				resCd = "00040" ;		//00040 : SAP ���� ����, �������� ������Ʈ ����
				resMsg = "sap success but update failure";
			}
		}else{
			resCd = "00030" ; //sap ���۽���(eReturn:error)
			resMsg = "sap process failure";
		}
		
		resultMap.put("RES_CD", resCd);
		resultMap.put("RES_MSG", resMsg);
		
		return resultMap;
	}
	 */
	


	/**
	 * �ش� ������� ����Ʈī�� ����/��� ���� ������ ��ȸ
	 * @param coopCoCd 
	 * @param param - REQ_DT 
	 * @return giftCardActvPtclList
	 */
	/*
	private List<Map<String, Object>> getGiftCardActvPtclList(String coopCoCd, Map<String, Object> param) {
		
		List<Map<String, Object>> giftCardActvPtclList = new ArrayList<Map<String, Object>>();
				
		//����翡 ���� ��ȸ
		if("7030".equals(coopCoCd)){			//�����
			giftCardActvPtclList = prCrdMapper.selectGiftCardActvPtcl7030(param);	
		}else if("7010".equals(coopCoCd)){		//��������
			giftCardActvPtclList = prCrdMapper.selectGiftCardActvPtcl7010(param);	
		}
				
		return giftCardActvPtclList;
	}
	*/
	
	/**
	 * �ش� ������� ����Ʈī�� ���� �Ҹ� ���� ������ ��ȸ
	 * @param coopCoCd 
	 * @param param - REQ_DT 
	 * @return giftCardActvPtclList
	 */
	/*
	private List<Map<String, Object>> getGiftCardActvXtnctPtclList(String coopCoCd, Map<String, Object> param) {
		
		List<Map<String, Object>> giftCardActvXtnctPtclList = new ArrayList<Map<String, Object>>();
		
		//����翡 ���� ��ȸ
		if("7010".equals(coopCoCd)){		//��������
			//CALL_USER�� ���� REQ_DT ����, ADMIN������YYYYMM00 �� ���·� ���´�.
			if("2".equals(param.get("CALL_USER"))){
				param.put("REQ_DT", ((String)param.get("REQ_DT")).substring(0,6));
			}
			giftCardActvXtnctPtclList = prCrdMapper.selectGiftCardActvXtnctPtcl7010(param);	
		}
		
		return giftCardActvXtnctPtclList;
	}
	*/
	
	/**
	 * �ش� ������� ����Ʈī�� ��� ���� ������ ��ȸ
	 * @param coopCoCd 
	 * @param param - REQ_DT 
	 * @return giftCardUsePtclList
	 */
	/*
	private List<Map<String, Object>> getGiftCardUsePtclList(String coopCoCd, Map<String, Object> param) {
		
		List<Map<String, Object>> giftCardUsePtclList = new ArrayList<Map<String, Object>>();
		
		//����翡 ���� ��ȸ
		if("7010".equals(coopCoCd)){		//��������
			giftCardUsePtclList = prCrdMapper.selectGiftCardUsePtcl7010(param);	
		}
				
		return giftCardUsePtclList;
	}
	*/
	
	
	/**
	 * ����Ʈī�� ȯ�� ������ ������ ��ȸ
	 * @param coopCoCd 
	 * @param param - REQ_DT 
	 * @param param - CRD_ST ī�� ����[92:ȯ��]
	 * @param param - REFD_ST ȯ�� ����[03:ȯ��������]
	 * @return getGiftCardRefdReqPtclList
	 */
	/*
	private List<Map<String, Object>> getGiftCardRefdReqPtclRecvList(String coopCoCd, Map<String, Object> param) {
		List<Map<String, Object>> giftCardRefdReqPtclRecvList = new ArrayList<Map<String, Object>>();
		
		//����翡 ���� ��ȸ
		if("7010".equals(coopCoCd)){		//��������
			giftCardRefdReqPtclRecvList = prCrdMapper.selectGiftCardRefdReqPtclRecv7010(param);	
		}
				
		return giftCardRefdReqPtclRecvList;
	}
	*/
	
	/**
	 * ����Ʈī�� ȯ�� ��û ���� ������ ��ȸ
	 * @param coopCoCd 
	 * @param param - REQ_DT 
	 * @param param - CRD_ST ī�� ����[92:ȯ��]
	 * @param param - REFD_ST ȯ�� ����[01:ȯ�ҽ�û]
	 * @return getGiftCardRefdReqPtclList
	 */
	/*
	private List<Map<String, Object>> getGiftCardRefdReqPtclList(String coopCoCd, Map<String, Object> param) {
		List<Map<String, Object>> giftCardRefdReqPtclList = new ArrayList<Map<String, Object>>();
		
		//����翡 ���� ��ȸ
		if("7010".equals(coopCoCd)){		//��������
			giftCardRefdReqPtclList = prCrdMapper.selectGiftCardRefdReqPtcl7010(param);	
		}
		
		return giftCardRefdReqPtclList;
	}
	*/
	
	/**
	 * �ش� ������� ����Ʈī�� ������ ���� ������ ��ȸ
	 * @param coopCoCd 
	 * @param param - REQ_DT ������ ���� ��ȸ ������ (��ġ ���� -1�� ,YYYYMMDD)	
	 * @param param - CALL_USER 1:BATCH, 2:ADMIN	 
	 * @return giftCardStmpTaxList
	 */
	/*
	private List<Map<String, Object>> getGiftCardStmpTaxList(String coopCoCd, Map<String, Object> param) {
		
		List<Map<String, Object>> giftCardStmpTaxList = new ArrayList<Map<String, Object>>();
		
		//����翡 ���� ��ȸ
		if("7010".equals(coopCoCd)){		//��������
			//CALL_USER�� ���� REQ_DT ����, ADMIN������YYYYMM00 �� ���·� ���´�.
			if("2".equals(param.get("CALL_USER"))){
				param.put("REQ_DT", ((String)param.get("REQ_DT")).substring(0,6));
			}
			giftCardStmpTaxList = prCrdMapper.selectGiftCardStmpTax7010(param);	
		}
				
		return giftCardStmpTaxList;
	}
	*/

	/**
	 * ����Ʈī�� ����/������� ������ ����
	 * @param JCoTable codes 
	 * @param giftCardActvPtclList 
	 * @param coopCoCd 
	 * @return JCoTable codes
	 */
	/*
	private JCoTable setActvPtclSapParam(JCoTable codes, List<Map<String, Object>> giftCardActvPtclList, String coopCoCd) {
		
		setCurDate();	//����ð� ����
				
		if("7030".equals(coopCoCd)){			//�����
			//�������鼭 ���̺� �Ķ���Ϳ� ������ ����
			for(Map<String, Object> eachMap : giftCardActvPtclList){
				codes.appendRow();
				
				codes.setValue("BUKRS"			, "8000"						);	//ȸ���ڵ�, I/F �ƴ�'8000' ���� ����
				codes.setValue("ZSYS_GUBUN"		, "5"							);	//�ý��� ����,  1: ���� �����POS , 5:���ո���� �ý���
				codes.setValue("ZGUBUN"			, eachMap.get("SETL_DV")		);	//��������, 1: Membership ����, 2.  Membership ���, 3:  Membership ȯ��
				codes.setValue("BLDAT"			, eachMap.get("SETL_DT")		);	//��������, POS �ϸ��� ���� (���ո���� �ϸ��� ����)
				codes.setValue("ZSERIAL_NO"		, eachMap.get("IDX")			);	//����,  �ش��� �ŷ��� ������ȣ
				codes.setValue("KUNNR"			, eachMap.get("STOR_CD")		);	//����� �����ڵ�=(SAP���ڵ�), �ŷ��߻� ����� �����ڵ�
				codes.setValue("ZCASH_AMT"		, eachMap.get("CASH_AMT_SUM")	);	//���ݰ���ݾ�, ���� �ŷ� �߻� �ݾ�
				codes.setValue("ZCARD_AMT"		, eachMap.get("CRD_AMT_SUM")	);	//ī�����ݾ�, ī�� �ŷ� �߻� �ݾ�
//				codes.setValue("ZWEB_AMT"		, eachMap.get("")				);	//WEB ����ݾ�, WEB �ŷ� �߻��ݾ�(LG U+)
//				codes.setValue("ZDC_CASH_AMT"	, eachMap.get("")				);	//���� �Ǹ������ (���ݰŷ�)
//				codes.setValue("ZUP_CASH_AMT"	, eachMap.get("")				);	//���� �Ǹ������ (���ݰŷ�)
//				codes.setValue("ZDC_CARD_AMT"	, eachMap.get("")				);	//���� �Ǹ������ (ī��ŷ�)
//				codes.setValue("ZUP_CARD_AMT"	, eachMap.get("")				);	//���� �Ǹ������ (ī��ŷ�)
//				codes.setValue("KOSTL"			, eachMap.get("")				);	//�����ڽ�Ʈ����
//				codes.setValue("BANKL"			, eachMap.get("")				);	//�����ڵ�, ȯ���� ��쿡�� �ش� �����ڵ� 3�ڸ�
//				codes.setValue("BANKN"			, eachMap.get("")				);	//���¹�ȣ, ȯ���� ��쿡�� �ۼ�
//				codes.setValue("KOINH"			, eachMap.get("")				);	//�����ָ�, ȯ���� ��쿡�� �ۼ�
//				codes.setValue("ZPERSONAL_NO"	, eachMap.get("")				);	//ȸ����ȣ, ȯ���� ��쿡�� �ۼ�
//				codes.setValue("ZCARD_NO"		, eachMap.get("")				);	//ī���ȣ, ȯ���� ��쿡�� �ۼ�
				codes.setValue("IFDAT"			, trmsYmd						);	//��������
				codes.setValue("IFZET"			, trmsHms						);	//���۽ð�
				codes.setValue("IFNAM"			, "MAEILDO"						);	//������, ����ID
			}
		}else if("7010".equals(coopCoCd)){		//��������
			//�������鼭 ���̺� �Ķ���Ϳ� ������ ����
			for(Map<String, Object> eachMap : giftCardActvPtclList){
				//MaeilDo ���� ������ ���� �ý��� ���� ����
				
				String zsysGubun = "5";
				if("A".equals((String) eachMap.get("SETL_DV")) || "B".equals((String) eachMap.get("SETL_DV"))) zsysGubun="6";
				// �̺�Ʈ ������ ����Ʈī�� �߰� | 2018. 7. 6. | jhPark
				if("D".equals((String) eachMap.get("SETL_DV")) || "E".equals((String) eachMap.get("SETL_DV"))) zsysGubun="7";
				// 2018_106_��������ī�� | 2018. 7. 18. | jhPark
				if("G".equals((String) eachMap.get("SETL_DV")) || "H".equals((String) eachMap.get("SETL_DV"))) zsysGubun="8";
				
				
				String zGubun = "1";
				if("A".equals((String) eachMap.get("SETL_DV"))) zGubun = "1";
				else if("B".equals((String) eachMap.get("SETL_DV"))) zGubun = "2";
				
				// �̺�Ʈ ������ ����Ʈī�� �߰� | 2018. 7. 6. | jhPark
				else if("D".equals((String) eachMap.get("SETL_DV"))) zGubun = "1";
				else if("E".equals((String) eachMap.get("SETL_DV"))) zGubun = "2";
				
				// 2018_106_��������ī�� | 2018. 7. 18. | jhPark
				else if("G".equals((String) eachMap.get("SETL_DV"))) zGubun = "1";
				else if("H".equals((String) eachMap.get("SETL_DV"))) zGubun = "2";
				else zGubun = (String) eachMap.get("SETL_DV");
				
				codes.appendRow();
				
				codes.setValue("BUKRS"			, "1000"							);	//�������� ȸ���ڵ� '1000' ���� ����
				codes.setValue("INT_BUKRS"		, eachMap.get("COOPCO_CD")			);	//MaeilDO ���� ����ϴ� ȸ�����ڵ� (7000 MaeilDO, 7030 ����� ��)
				codes.setValue("ZSYS_GUBUN"		, zsysGubun							);	//�ý��� ����,  1: ���� �����POS , 5:���ո���� �ý���, 6:���ΰ����ո����, 7:�̺�Ʈ���������Ʈī��
				codes.setValue("ZGUBUN"			, zGubun							);	//��������, 1: Membership ����, 2.  Membership ���, 3:  Membership ȯ��
				codes.setValue("BLDAT"			, eachMap.get("SETL_DT")			);	//MaeilDO �ϸ��� ���� (���ո���� �ϸ��� ����)
				codes.setValue("ZSERIAL_NO"		, eachMap.get("IDX")				);	//����,  �ش��� �ŷ��� ������ȣ
//				codes.setValue("KUNNR"			, ""		);							//�ŷ��߻� ����� �����ڵ� --> �������� I/F �� 
				codes.setValue("ZCASH_AMT"		, eachMap.get("CASH_ACTV_AMT_SUM")	);	//���ݰ���ݾ�, ���� �ŷ� �߻� �ݾ�
				codes.setValue("ZCARD_AMT"		, eachMap.get("CRD_ACTV_AMT_SUM")	);	//ī�����ݾ�, ī�� �ŷ� �߻� �ݾ�
				codes.setValue("ZPAY_GUBUN"		, eachMap.get("MBLE_DV")			);	//����� �ŷ�����Ű (����� �ŷ� ���� Ű [10:LG U+, 20:PAYCO, 30:����(���θ�� ī��), 40:KGMob])
				codes.setValue("ZWEB_AMT"		, eachMap.get("MBLE_AMT")			);	//WEB ����ݾ�, ����� �ݾ� (�Աݵ� �ݾ�)
				codes.setValue("ZWEB_Q_AMT"		, eachMap.get("MBLE_CPN_AMT")		);	//WEB ����ݾ� (���� �ݾ�)
				codes.setValue("ZWEB_P_AMT"		, eachMap.get("MBLE_PINT_AMT")		);	//WEB ����ݾ� (����Ʈ �ݾ�)
				codes.setValue("ZWEB_E_AMT"		, eachMap.get("MBLE_ETC_AMT")		);	//WEB ����ݾ� (��Ÿ �ݾ�)
//				codes.setValue("KOSTL"			, eachMap.get("")					);	//�����ڽ�Ʈ���� --> �������� I/F ��
//				codes.setValue("ZDC_CASH_AMT"	, eachMap.get("")					);	//���� �Ǹ������ (���ݰŷ�)
//				codes.setValue("ZUP_CASH_AMT"	, eachMap.get("")					);	//���� �Ǹ������ (���ݰŷ�)
//				codes.setValue("ZDC_CARD_AMT"	, eachMap.get("")					);	//���� �Ǹ������ (ī��ŷ�)
//				codes.setValue("ZUP_CARD_AMT"	, eachMap.get("")					);	//���� �Ǹ������ (ī��ŷ�)
//				codes.setValue("BANKL"			, eachMap.get("")					);	//�����ڵ�, ȯ���� ��쿡�� �ش� (�����ڵ� 3�ڸ�)
//				codes.setValue("BANKN"			, eachMap.get("")					);	//���¹�ȣ, ȯ���� ��쿡�� �ۼ� (���� ������ȣ)
//				codes.setValue("KOINH"			, eachMap.get("")					);	//�����ָ�, ȯ���� ��쿡�� �ۼ� (���� �����ڸ�)
//				codes.setValue("ZPERSONAL_NO"	, eachMap.get("")					);	//ȸ����ȣ, ȯ���� ��쿡�� �ۼ�
//				codes.setValue("ZCARD_NO"		, eachMap.get("")					);	//ī���ȣ, ȯ���� ��쿡�� �ۼ�
				codes.setValue("IFDAT"			, trmsYmd							);			//��������
				codes.setValue("IFZET"			, trmsHms							);			//���۽ð�
				codes.setValue("IFNAM"			, "MAEILDO"							);		//������, ����ID
			}
		}
		
		return codes;
	}
*/	
	
	/**
	 * ����Ʈī�� ���� �Ҹ� ������ ����
	 * @param JCoTable codes 
	 * @param giftCardActvXtnctPtclList 
	 * @param coopCoCd 
	 * @return JCoTable codes
	 */
	/*
	private JCoTable setActvXtnctPtclSapParam(JCoTable codes, List<Map<String, Object>> giftCardActvXtnctPtclList, String coopCoCd) {
		
		setCurDate();	//����ð� ����

		if("7010".equals(coopCoCd)){		//��������
			//�������鼭 ���̺� �Ķ���Ϳ� ������ ����
			for(Map<String, Object> eachMap : giftCardActvXtnctPtclList){
				codes.appendRow();
				
				String zsysGubun = "5";
				if("C".equals((String) eachMap.get("SETL_DV"))) zsysGubun="6";
				// �̺�Ʈ ������ ����Ʈī�� �߰� | 2018. 7. 6. | jhPark
				else if("F".equals((String) eachMap.get("SETL_DV"))) zsysGubun="7";
				// 2018_106_��������ī�� | 2018. 7. 18. | jhPark
				else if("I".equals((String) eachMap.get("SETL_DV"))) zsysGubun="8";
				
				codes.setValue("BUKRS"			, "1000"							);	//�������� ȸ���ڵ� '1000' ���� ����
				codes.setValue("INT_BUKRS"		, eachMap.get("COOPCO_CD")			);	//MaeilDO ���� ����ϴ� ȸ�����ڵ� (7000 MaeilDO, 7030 ����� ��)
				codes.setValue("ZSYS_GUBUN"		, zsysGubun							);	//�ý��� ����,  1: ���� �����POS , 6:���ո���� �ý���( ����������ī��) , 7:�̺�Ʈ������ ����Ʈī��
				codes.setValue("BLDAT"			, eachMap.get("XTNCT_DT")			);	//MaeilDO �ϸ��� ���� (���ո���� �ϸ��� ����)
				codes.setValue("ZSERIAL_NO"		, eachMap.get("IDX")				);	//����,  �ش��� �ŷ��� ������ȣ
//				codes.setValue("KUNNR"			, ""		);							//�ŷ��߻� ����� �����ڵ� --> �������� I/F �� 
				codes.setValue("ZEXTINCT_AMT"	, eachMap.get("XTNCT_AMT")			);	//�����Ҹ�ݾ�
//				codes.setValue("KOSTL"			, eachMap.get("")					);	//�����ڽ�Ʈ���� --> �������� I/F ��
				codes.setValue("IFDAT"			, trmsYmd							);	//��������
				codes.setValue("IFZET"			, trmsHms							);	//���۽ð�
				codes.setValue("IFNAM"			, "MAEILDO"							);	//������, ����ID
			}
		}
		
		return codes;
	}
*/

	/**
	 * ����Ʈī�� ��� ���� ������ ����
	 * @param JCoTable codes 
	 * @param giftCardActvPtclList 
	 * @param coopCoCd 
	 * @return JCoTable codes
	 */
	/*
	private JCoTable setUsePtclSapParam(JCoTable codes, List<Map<String, Object>> giftCardUsePtclList, String coopCoCd) {
		
		setCurDate();	//����ð� ����
		
		if("7010".equals(coopCoCd)){			//��������
			//�������鼭 ���̺� �Ķ���Ϳ� ������ ����
			for(Map<String, Object> eachMap : giftCardUsePtclList){
				codes.appendRow();
				
				codes.setValue("BUKRS"			, "1000"							);	//�������� ȸ���ڵ� '1000' ���� ����
				codes.setValue("INT_BUKRS"		, eachMap.get("COOPCO_CD")			);	//MaeilDO ���� ����ϴ� ȸ�����ڵ� (7000 MaeilDO, 7030 ����� ��)
				codes.setValue("ZSYS_GUBUN"		, "5"								);	//�ý��� ����,  1: ���� �����POS , 5:���ո���� �ý���
				codes.setValue("ZGUBUN2"		, eachMap.get("SALE_DV")			);	//��������, 1: Membership ���, 2.  Membership ������
				codes.setValue("BLDAT"			, eachMap.get("USE_DT")				);	//MaeilDO �ϸ��� ���� (���ո���� �ϸ��� ����)
				codes.setValue("ZSERIAL_NO"		, eachMap.get("IDX")				);	//����,  �ش��� �ŷ��� ������ȣ
//				codes.setValue("KUNNR"			, ""		);							//�ŷ��߻� ����� �����ڵ� --> �������� I/F �� 
				codes.setValue("ZUSING_AMT"		, eachMap.get("USE_AMT")			);	//���/��� �ݾ�
//				codes.setValue("KOSTL"			, eachMap.get("")					);	//�����ڽ�Ʈ���� --> �������� I/F ��
				codes.setValue("IFDAT"			, trmsYmd							);	//��������
				codes.setValue("IFZET"			, trmsHms							);	//���۽ð�
				codes.setValue("IFNAM"			, "MAEILDO"							);	//������, ����ID
			}
		}
		
		return codes;
	}
	*/
	
	/**
	 * ����Ʈī�� ȯ�� ������ ������ ����
	 * @param JCoTable codes 
	 * @param giftCardRefdReqPtclList 
	 * @param coopCoCd 
	 * @return JCoTable codes
	 */
	/*
	private JCoTable setRefdReqPtclRecvSapParam(JCoTable codes, List<Map<String, Object>> giftCardRefdReqPtclRecvList, String coopCoCd) {

		setCurDate();	//����ð� ����
		
		if("7010".equals(coopCoCd)){			//��������
			//�������鼭 ���̺� �Ķ���Ϳ� ������ ����
			for(Map<String, Object> eachMap : giftCardRefdReqPtclRecvList){
				codes.appendRow();
				
				codes.setValue("BUKRS"			, "1000"							);	//�������� ȸ���ڵ� '1000' ���� ����
				codes.setValue("INT_BUKRS"		, "7000"							);	//MaeilDO ���� ����ϴ� ȸ�����ڵ� (7000 MaeilDO, 7030 ����� ��) -> ȯ�� �� MaeilDO ���
				codes.setValue("ZSYS_GUBUN"		, "5"								);	//�ý��� ����,  1: ���� �����POS , 5:���ո���� �ý���
				codes.setValue("ZGUBUN"			, "3"								);	//��������, 1: Membership ����, 2.  Membership ���, 3:  Membership ȯ��
				codes.setValue("BLDAT"			, eachMap.get("REQ_DTM")			);	//ȯ�ҿ�û��, MaeilDO �ϸ��� ���� (���ո���� �ϸ��� ����) -> ȯ�� �� �� ȯ�ҿ�û���� 8�ڸ��� ��ȯ�Ͽ� ����
				codes.setValue("ZSERIAL_NO"		, eachMap.get("IDX")				);	//����,  �ش��� �ŷ��� ������ȣ
				codes.setValue("ZCARD_NO"		, eachMap.get("CRD_ID")				);	//ī���ȣ
				codes.setValue("ZPERSONAL_NO"	, eachMap.get("UNFY_MMB_NO")		);	//����ȸ����ȣ
				codes.setValue("IFDAT"			, trmsYmd							);	//��������
				codes.setValue("IFZET"			, trmsHms							);	//���۽ð�
				codes.setValue("IFNAM"			, "MAEILDO"							);	//������, ����ID
			}
		}
		
		return codes;
	}
	*/
	
	
	/** 
	 * ����Ʈī�� ȯ�� ó�� ����(JCoTable)�� ����Ʈ ���·� ��ȯ�Ͽ� param�� �߰�
	 * @param param
	 * @param JCoTable codes	
	 * @return param
	 */
	/*
	private Map<String, Object> setReturnList(Map<String, Object> param, JCoTable codes) {		
		List<Map<String, Object>> returnListTempHist = new ArrayList<Map<String, Object>>();	//ȯ�� ó�� ����� ���� �̷¿� ����Ʈ 
		List<Map<String, Object>> returnListTempSuccess = new ArrayList<Map<String, Object>>();	//ȯ�� ���� ����� ���� �̷¿� �ӽ� ����ƴ
		List<Map<String, Object>> returnListTempFailuer = new ArrayList<Map<String, Object>>();	//ȯ�� ���� ����� ���� �̷¿� �ӽ� ����Ʈ 
		
		//sap���� ������ �����͸� ����Ʈ ���·� ��ȯ
		for(int i=0 ; i<codes.getNumRows() ; i++){
			Map<String, Object> eachRowData = new HashMap<String, Object>();
			codes.setRow(i);
			
			eachRowData.put("REQ_DTM",			(codes.getString("BLDAT")).replaceAll("-", ""));		//ȯ�ҿ�û��
			eachRowData.put("IDX", 				codes.getString("ZSERIAL_NO"));		//����,  �ش��� �ŷ��� ������ȣ
			eachRowData.put("CRD_ID",			codes.getString("ZCARD_NO"));		//ī���ȣ
			eachRowData.put("UNFY_MMB_NO",		codes.getString("ZPERSONAL_NO"));	//����ȸ����ȣ
			eachRowData.put("NM_INQ_SCSS_YN",	codes.getString("ZFLAG1"));			//������ȸ pos ���ۿ���, Y:����, N:����, NULL:������
			eachRowData.put("REFD_SCSS_YN",		codes.getString("ZFLAG2"));			//������� pos ���ۿ���, Y:����, NULL:������
			eachRowData.put("MSG_TYP",			codes.getString("MSGTY"));			//�޽��� ����(S:����, E:����), ��ȸ��û�� �����Ͱ� sap���� ���� ��� E�� ���ϵ�
			eachRowData.put("MSG_CTT",			codes.getString("MSGTX"));			//�޽��� �ؽ�Ʈ
			eachRowData.put("STATS",			codes.getString("STATS"));			//������ȸ����ڵ�
			eachRowData.put("TEXT",				codes.getString("TEXT"));			//������ȸ��������
			
			returnListTempHist.add(eachRowData);
			
			//������ȸ �� ��������� �Ϸ�� ���(ȯ�Ҽ���)���� ī�帶���Ϳ� ������Ʈ
			if("Y".equals(codes.getString("ZFLAG1")) && "Y".equals(codes.getString("ZFLAG2"))){
				returnListTempSuccess.add(eachRowData);				
			//ȯ�ҽ��� �� ��ó���� ���� ����Ʈ  
			}else if("E".equals(codes.getString("MSGTY"))){		//��ȸ��û�� �����Ͱ� sap���� ���� ���
				eachRowData.put("REFD_RTN_CD", "99");
				eachRowData.put("REFD_RTN_MSG", "no data at sap system.");
				returnListTempFailuer.add(eachRowData);
			}else if("N".equals(codes.getString("ZFLAG1"))){	//������ȸ�� ������ ���
				eachRowData.put("REFD_RTN_CD", "01");
//				eachRowData.put("REFD_RTN_MSG", "user info(account) search failure in sap");
				eachRowData.put("REFD_RTN_MSG", codes.getString("TEXT"));
				returnListTempFailuer.add(eachRowData);
			}
											
		};
		
		param.put("PARAM_LIST_HIST", returnListTempHist);
		param.put("PARAM_LIST_SUCCESS", returnListTempSuccess);
		param.put("PARAM_LIST_FAILURE", returnListTempFailuer);
		
		return param;
	}
	*/
	
	/**
	 * ����Ʈī�� ȯ�ҽ�û���� ������ ����
	 * @param JCoTable codes 
	 * @param giftCardRefdReqPtclList 
	 * @param coopCoCd 
	 * @return JCoTable codes
	 */
	/*
	private JCoTable setRefdReqPtclSapParam(JCoTable codes, List<Map<String, Object>> giftCardRefdReqPtclList, String coopCoCd) {
		
		setCurDate();	//����ð� ����
		
		if("7010".equals(coopCoCd)){			//��������
			//�������鼭 ���̺� �Ķ���Ϳ� ������ ����
			for(Map<String, Object> eachMap : giftCardRefdReqPtclList){
				
				logger.info("=========================����Ʈī�� ȯ�� ��û ���� ������ ���� ���� [s]===============================");
				logger.info("BLDAT  : " + eachMap.get("REQ_DT")	);
				logger.info("ZSERIAL_NO  : " + eachMap.get("IDX")	);
				logger.info("ZCASH_AMT  : " + eachMap.get("CASH_ACTV_AMT_SUM")	);
				logger.info("ZCARD_AMT  : " + eachMap.get("CRD_ACTV_AMT_SUM")	);
				logger.info("BANKL  : " + eachMap.get("BNK_CD")	);
				logger.info("BANKN  : " + eachMap.get("ACCT_NO")	);
				logger.info("KOINH  : " + eachMap.get("OWAC_NM")	);
				logger.info("ZPERSONAL_NO  : " + eachMap.get("UNFY_MMB_NO")	);
				logger.info("ZCARD_NO  : " + eachMap.get("CRD_ID")	);
				logger.info("ZCARD_NO  : " + eachMap.get("CRD_ID")	);
				logger.info("IFDAT  : " + trmsYmd);
				logger.info("IFZET  : " + trmsHms);
				logger.info("=========================����Ʈī�� ȯ�� ��û ���� ������ ���� ���� [e]===============================");
				
				codes.appendRow();
				
				codes.setValue("BUKRS"			, "1000"							);	//�������� ȸ���ڵ� '1000' ���� ����
				codes.setValue("INT_BUKRS"		, "7000"							);	//MaeilDO ���� ����ϴ� ȸ�����ڵ� (7000 MaeilDO, 7030 ����� ��) -> ȯ�� �� MaeilDO ���
				codes.setValue("ZSYS_GUBUN"		, "5"								);	//�ý��� ����,  1: ���� �����POS , 5:���ո���� �ý���
				codes.setValue("ZGUBUN"			, "3"								);	//��������, 1: Membership ����, 2.  Membership ���, 3:  Membership ȯ��
				codes.setValue("BLDAT"			, eachMap.get("REQ_DT")				);	//ȯ�ҿ�û��, MaeilDO �ϸ��� ���� (���ո���� �ϸ��� ����) -> ȯ�� �� �� ȯ�ҿ�û���� 8�ڸ��� ��ȯ�Ͽ� ����
				codes.setValue("ZSERIAL_NO"		, eachMap.get("IDX")				);	//����,  �ش��� �ŷ��� ������ȣ
//				codes.setValue("KUNNR"			, ""		);							//�ŷ��߻� ����� �����ڵ� --> �������� I/F �� 
				codes.setValue("ZCASH_AMT"		, eachMap.get("CASH_ACTV_AMT_SUM")	);	//���ݰ���ݾ�, ���� �ŷ� �߻� �ݾ�
				codes.setValue("ZCARD_AMT"		, eachMap.get("CRD_ACTV_AMT_SUM")	);	//ī�����ݾ�, ī�� �ŷ� �߻� �ݾ�
//				codes.setValue("ZPAY_GUBUN"		, eachMap.get("MBLE_DV")			);	//����� �ŷ�����Ű (10 : LG U+,  20 : PAYCO)
				codes.setValue("ZWEB_AMT"		, eachMap.get("REFD_AMT_POSI")		);	//WEB ����ݾ�, ����� �ݾ� (�Աݵ� �ݾ�) -> ȯ�� �� ȯ�ұݾ����� ���
//				codes.setValue("ZWEB_Q_AMT"		, eachMap.get("MBLE_CPN_AMT")		);	//WEB ����ݾ� (���� �ݾ�)
//				codes.setValue("ZWEB_P_AMT"		, eachMap.get("MBLE_PINT_AMT")		);	//WEB ����ݾ� (����Ʈ �ݾ�)
//				codes.setValue("ZWEB_E_AMT"		, eachMap.get("MBLE_ETC_AMT")		);	//WEB ����ݾ� (��Ÿ �ݾ�)
//				codes.setValue("KOSTL"			, eachMap.get("")					);	//�����ڽ�Ʈ���� --> �������� I/F ��
//				codes.setValue("ZDC_CASH_AMT"	, eachMap.get("")					);	//���� �Ǹ������ (���ݰŷ�)
//				codes.setValue("ZUP_CASH_AMT"	, eachMap.get("")					);	//���� �Ǹ������ (���ݰŷ�)
//				codes.setValue("ZDC_CARD_AMT"	, eachMap.get("")					);	//���� �Ǹ������ (ī��ŷ�)
//				codes.setValue("ZUP_CARD_AMT"	, eachMap.get("")					);	//���� �Ǹ������ (ī��ŷ�)
				codes.setValue("BANKL"			, eachMap.get("BNK_CD")				);	//�����ڵ�, ȯ���� ��쿡�� �ش� (�����ڵ� 3�ڸ�)
				codes.setValue("BANKN"			, eachMap.get("ACCT_NO")			);	//���¹�ȣ, ȯ���� ��쿡�� �ۼ� (���� ������ȣ)
				codes.setValue("KOINH"			, eachMap.get("OWAC_NM")			);	//�����ָ�, ȯ���� ��쿡�� �ۼ� (���� �����ڸ�)
				codes.setValue("ZPERSONAL_NO"	, eachMap.get("UNFY_MMB_NO")		);	//ȸ����ȣ, ȯ���� ��쿡�� �ۼ� (����ȸ����ȣ)
				codes.setValue("ZCARD_NO"		, eachMap.get("CRD_ID")				);	//ī���ȣ, ȯ���� ��쿡�� �ۼ�
				codes.setValue("IFDAT"			, trmsYmd							);			//��������
				codes.setValue("IFZET"			, trmsHms							);			//���۽ð�
				codes.setValue("IFNAM"			, "MAEILDO"							);		//������, ����ID
			}
		}
		
		return codes;
	}
	*/
	
	/**
	 * ����Ʈī�� ������ ������ ����
	 * @param JCoTable codes 
	 * @param giftCardActvPtclList 
	 * @param coopCoCd 
	 * @return JCoTable codes
	 */
	/*
	private JCoTable setStmpTaxSapParam(JCoTable codes, List<Map<String, Object>> giftCardStmpTaxList,	String coopCoCd) {
		
		setCurDate();	//����ð� ����
		
		if("7010".equals(coopCoCd)){			//��������
			//�������鼭 ���̺� �Ķ���Ϳ� ������ ����
			for(Map<String, Object> eachMap : giftCardStmpTaxList){
				// MaeilDo ���� ������ ���� �ý��� ���� ����
				String zsysGubun = "";
				if("1".equals((String) eachMap.get("SETL_DV")) || "2".equals((String) eachMap.get("SETL_DV"))) zsysGubun="5";
				else if("A".equals((String) eachMap.get("SETL_DV")) || "B".equals((String) eachMap.get("SETL_DV"))) zsysGubun="6";
				// �̺�Ʈ ������ ����Ʈ ī�� �߰� | 2018. 7. 6. | jhPark
				else if("D".equals((String) eachMap.get("SETL_DV")) || "E".equals((String) eachMap.get("SETL_DV"))) zsysGubun="7";
				// 2018_106_��������ī��: | 2018. 7. 18. | jhPark
				else if("G".equals((String) eachMap.get("SETL_DV")) || "H".equals((String) eachMap.get("SETL_DV"))) zsysGubun="8";
				
				codes.appendRow();
				
				codes.setValue("BUKRS"			, "1000"						);	//�������� ȸ���ڵ� '1000' ���� ����
				codes.setValue("INT_BUKRS"		, eachMap.get("COOPCO_CD")		);	//MaeilDO ���� ����ϴ� ȸ�����ڵ� (7000 MaeilDO, 7030 ����� ��)
				codes.setValue("ZSYS_GUBUN"		, zsysGubun						);	//�ý��� ����,  1: ���� �����POS , 5:���ո���� �ý���, 6:���ΰ����ո����
				codes.setValue("ZSTAMP_GUBUN"	, eachMap.get("TYP_DV")			);	//��������, 1: ������ �߻�, 2: ������ ���
				codes.setValue("BLDAT"			, eachMap.get("SETL_DT")		);	//MaeilDO �ϸ��� ���� (���ո���� �ϸ��� ����)
				codes.setValue("ZSERIAL_NO"		, eachMap.get("IDX")			);	//����,  �ش��� �ŷ��� ������ȣ
//				codes.setValue("KUNNR"			, ""							);	//�ŷ��߻� ����� �����ڵ� --> �������� I/F ��
				codes.setValue("ZSTAMP_QTY1"	, eachMap.get("STMP_TAX_QNT_1")	);	//1������ ����
				codes.setValue("ZSTAMP_AMT1"	, eachMap.get("STMP_TAX_AMT_1")	);	//1������ �ݾ�, ���� * 50
				codes.setValue("ZSTAMP_QTY2"	, eachMap.get("STMP_TAX_QNT_2")	);	//5������ ����
				codes.setValue("ZSTAMP_AMT2"	, eachMap.get("STMP_TAX_AMT_2")	);	//5������ �ݾ�, ���� * 200
				codes.setValue("ZSTAMP_QTY3"	, eachMap.get("STMP_TAX_QNT_3")	);	//10������ ����
				codes.setValue("ZSTAMP_AMT3"	, eachMap.get("STMP_TAX_AMT_3")	);	//10������ �ݾ�, ���� * 400
				codes.setValue("ZSTAMP_QTY4"	, eachMap.get("STMP_TAX_QNT_4")	);	//10�����ʰ��� ����
				codes.setValue("ZSTAMP_AMT4"	, eachMap.get("STMP_TAX_AMT_4")	);	//10�����ʰ��� �ݾ�, ���� * 800
				codes.setValue("IFDAT"			, trmsYmd						);	//��������
				codes.setValue("IFZET"			, trmsHms						);	//���۽ð�
				codes.setValue("IFNAM"			, "MAEILDO"						);	//������, ����ID
				
				logger.debug("--------------parameter-----------");
				logger.debug("BUKRS=1000,INT_BUKRS=" + eachMap.get("COOPCO_CD") 
							+",ZSYS_GUBUN="	+ zsysGubun
							+",ZSTAMP_GUBUN=" + eachMap.get("TYP_DV")
							+",BLDAT=" + eachMap.get("SETL_DT")
							+",ZSERIAL_NO=" + eachMap.get("IDX")
//							+",KUNNR"			+ ""							);
							+",ZSTAMP_QTY1="+ eachMap.get("STMP_TAX_QNT_1")
							+",ZSTAMP_AMT1="+ eachMap.get("STMP_TAX_AMT_1")
							+",ZSTAMP_QTY2="+ eachMap.get("STMP_TAX_QNT_2")
							+",ZSTAMP_AMT2="+ eachMap.get("STMP_TAX_AMT_2")
							+",ZSTAMP_QTY3="+ eachMap.get("STMP_TAX_QNT_3")
							+",ZSTAMP_AMT3="+ eachMap.get("STMP_TAX_AMT_3")
							+",ZSTAMP_QTY4="+ eachMap.get("STMP_TAX_QNT_4")
							+",ZSTAMP_AMT4="+ eachMap.get("STMP_TAX_AMT_4")
							+",IFDAT=" + trmsYmd
							+",IFZET=" + trmsHms
							+",IFNAM=" + "MAEILDO");
				
			}
		}
		
		return codes;
	}
*/
	
	/**
	 * �ش� ������� ����Ʈī�� ����/��� ���� ���� ���� ������Ʈ
	 * @param coopCoCd 
	 * @param param - REQ_DT
	 * @return resCd
	 */
	/*
	private String updateGiftCardActvPtcl(String coopCoCd, Map<String, Object> param) {
		
		//����ID ����		
		callUser = (String) param.get("CALL_USER");
		userId = setUserId(callUser);
		
		param.put("UPDR_ID", userId);
		
		if("7030".equals(coopCoCd)){			//�����
			prCrdMapper.updateGiftCardActvPtcl7030(param);	
		}else if("7010".equals(coopCoCd)){		//��������
			prCrdMapper.updateGiftCardActvPtcl7010(param);	
		}				

		return "00000"; //�����ڵ�, ���ۼ���
	}
	*/
	
	/**
	 * �ش� ������� ����Ʈī�� ���� �Ҹ� ���� ���� ���� ������Ʈ
	 * @param coopCoCd 
	 * @param param - REQ_DT
	 * @return resCd
	 */
	/*
	private String updateGiftCardActvXtnctPtcl(String coopCoCd, Map<String, Object> param) {
		
		//����ID ����		
		callUser = (String) param.get("CALL_USER");
		userId = setUserId(callUser);
		
		param.put("UPDR_ID", userId);
		
		if("7010".equals(coopCoCd)){		//��������
			prCrdMapper.updateGiftCardActvXtnctPtcl7010(param);	
		}				
		
		return "00000"; //�����ڵ�, ���ۼ���
	}
	*/
	
	/**
	 * �ش� ������� ����Ʈī�� ��� ���� ���� ���� ������Ʈ
	 * @param coopCoCd 
	 * @param param - REQ_DT
	 * @return resCd
	 * 
	 */
	/*
	private String updateGiftCardUsePtcl(String coopCoCd, Map<String, Object> param) {
		
		//����ID ����		
		callUser = (String) param.get("CALL_USER");
		userId = setUserId(callUser);
		
		param.put("UPDR_ID", userId);
		
		if("7010".equals(coopCoCd)){		//��������
			prCrdMapper.updateGiftCardUsePtcl7010(param);	
		}				
		
		return "00000"; //�����ڵ�, ���ۼ���
	}
	*/
	
	/**
	 * ����Ʈī�� ȯ�� ��û ���� ���� ���� ������Ʈ
	 * @param coopCoCd 
	 * @param param - REQ_DT ȯ�ҿ�û��(YYYYMMDD)
	 * @param param - CRD_ST ī�� ����[92:ȯ��]
	 * @param param - REFD_ST ȯ�� ����[01:ȯ�ҽ�û]
	 * @param param - REFD_ST_AFTER ȯ�ҽ�û ���� �� ��ȯ�� ���°�[03:ȯ��������]
	 * @param giftCardRefdReqPtclList ȯ�ҽ�ûī�帮��Ʈ
	 * @return resCd
	 */
	/*
	private String updateGiftCardRefdReqPtcl(String coopCoCd, Map<String, Object> param, List<Map<String, Object>> giftCardRefdReqPtclList ){
		//����ID ����		
		callUser = (String) param.get("CALL_USER");
		userId = setUserId(callUser);
		
		param.put("UPDR_ID", userId);		
		param.put("PARAM_LIST", giftCardRefdReqPtclList);
		
		if("7010".equals(coopCoCd)){		//��������
			//1. ī�帶���� ������Ʈ
			prCrdMapper.updateGiftCardRefdReqPtclMaster(param);
			
			//2. ȯ�� ��û �̷� ����
			prCrdMapper.insertGiftCardRefdReqPtclHist(param);
		}				

		return "00000"; 	//�����ڵ�, ���ۼ���
	}
*/

	
	/**
	 * ���� �ð� ����
	 */
	private void setCurDate() {
		Date curDate = new Date();
		trmsYmd = ymdFormat.format(curDate);	//��������
		trmsHms = hmsFormat.format(curDate);	//���۽ð�
	}
	
	
	/**
	 * JCO ���� ������Ƽ ����
	 * @param COOPCO_CD ����� �ڵ� 	
	 */
	/*
	private String setConnectProperties(String coopCoCd) {
		
		Properties connectProperties = new Properties();
		abapName = "";
		
		if("7030".equals(coopCoCd)){			//����� 
			sapAshost	= JProperties.getString("msseed.erpSapJco.ashost");
			sapSysnr	= JProperties.getString("msseed.erpSapJco.sysnr");
			sapClient	= JProperties.getString("msseed.erpSapJco.client");
			sapUser		= JProperties.getString("msseed.erpSapJco.user");
			sapPasswd	= JProperties.getString("msseed.erpSapJco.passwd");
			sapLang		= JProperties.getString("msseed.erpSapJco.lang");
			abapName = "7030_SAP_CONN";
		}else if("7010".equals(coopCoCd)){		//��������
			sapAshost	= JProperties.getString("maeil.erpSapJco.ashost");
			sapSysnr	= JProperties.getString("maeil.erpSapJco.sysnr");
			sapClient	= JProperties.getString("maeil.erpSapJco.client");
			sapUser		= JProperties.getString("maeil.erpSapJco.user");
			sapPasswd	= JProperties.getString("maeil.erpSapJco.passwd");
			sapLang		= JProperties.getString("maeil.erpSapJco.lang");
			abapName = "7010_SAP_CONN";
		}
		
		connectProperties.setProperty(DestinationDataProvider.JCO_ASHOST, sapAshost);	//SAP ȣ��Ʈ ����
		connectProperties.setProperty(DestinationDataProvider.JCO_SYSNR,  sapSysnr);	//�ν��Ͻ���ȣ
		connectProperties.setProperty(DestinationDataProvider.JCO_CLIENT, sapClient);	//SAP Ŭ���̾�Ʈ
		connectProperties.setProperty(DestinationDataProvider.JCO_USER,   sapUser);		//SAP������
		connectProperties.setProperty(DestinationDataProvider.JCO_PASSWD, sapPasswd);	//SAP �н�����
		connectProperties.setProperty(DestinationDataProvider.JCO_LANG,   sapLang);		//���
		
		//������Ƽ�� �̿��Ͽ� ���������� ����. ����ǰ� �ִ� ����ý��� ��ο� ������.
		createDestinationDataFile(abapName, connectProperties);
		
		return abapName;
	}
*/

	/** 
	 * sap �������� ����
	 */
	static void createDestinationDataFile(String destinationName, Properties connectProperties)
	{
		File destCfg = new File(destinationName+".jcoDestination");

		if(!destCfg.exists()){
			try
			{
				FileOutputStream fos = new FileOutputStream(destCfg, false);		            
				connectProperties.store(fos, "for tests only !");
				fos.close();
			}
			catch (Exception e)
			{
				throw new RuntimeException("Unable to create the destination files", e);
			}
		}
	}

	
	
}
