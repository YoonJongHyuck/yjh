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
 * SAP - 정산데이터 SAP 전송 관련 API
 * 
 * sapjco3.dll 또는 sapjco3.so 파일을 SAP 시스템 관리자에게 지원받아야 한다.
 * 
 * sapjco3 로컬 설정 방법 : lib에 있는 sapjco3.dll 파일을 System32에 복사한다.
 * 또는 톰캣 환경변수 에 -Djava.library.path="C:\yjh\workspace\maeil-backoffice\WebContent\WEB-INF\lib 와 같이 dll파일이 있는 위치를 셋팅한다.
 * 리눅스 서버의 경우, sapjco3.so(64bit)를 라이브러리에 복사한 후 LD_LIBRARY_PATH 로 잡아준다. ( ex: LD_LIBRARY_PATH=/apps/stage/MAEIL_ADMIN/lib/ )
 */

public class EaseCommonUtil_SAP {
	
	


//	@Autowired
//	private PRCRDMapper prCrdMapper;
	
	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	//JCO 커넥션 정보
	String sapAshost = "";	
	String sapSysnr	 = "";
	String sapClient = "";
	String sapUser	 = "";
	String sapPasswd = "";
	String sapLang	 = "";
	
	String coopCoCd  = "";	
	String abapName	 = "";			//연결파일명으로 사용됨
	String eReturnType	 	= "";	//SAP전송결과 TYPE
	String eReturnMessage	= "";	//SAP전송결과 MESSAGE	
	String functionName	 	= "";	//SAP 함수명칭
	String tableName		= "";	//SAP table 명칭
	String userId 	 = "";	
	String callUser  = "";			//호출시스템, 1:BATCH, 2:BACKOFFICE
	String resCd	 = "";			//응답코드 (00000:성공, 기타) 	
	String resMsg	 = "";
	
	SimpleDateFormat ymdFormat = new SimpleDateFormat("yyyyMMdd");
	SimpleDateFormat hmsFormat = new SimpleDateFormat("HHmmss");
	String trmsYmd = "";	//전송일자
	String trmsHms = "";	//전송시간
	
	Map<String, String> resultMap = new HashMap<String, String>();	//응답코드, 메시지를 담을 맵
	Map<String, String> dateMap = new HashMap<String, String>();	//전송일자 및 시간을 담을 맵
			
	/** 
	 * 기프트카드 충전/충전취소 내역 해당 관계사에 전송
	 * @param REQ_DT 집계일자(YYYYMMDD)	 
	 * @param COOPCO_CD 관계사 코드 	  
	 * @param CALL_USER 1:BATCH, 2:ADMIN
	 * @return RES_CD 응답코드 (00000:success, 00010:no data, 00020:sap transmission failure, 00030:sap process failure, 00040:sap success but update failure, 00050: prCrdService error )
	 * @return RES_MSG 응답메시지
	 */
	
	/*
	public Map<String, String> giftCardActvPtclTrms(Map<String, Object> param){
		
		coopCoCd = (String)param.get("COOPCO_CD");
		
		//해당 관계사의 충전/취소 내역 데이터 조회 
		List<Map<String, Object>> giftCardActvPtclList = getGiftCardActvPtclList(coopCoCd,param);
		
		if(giftCardActvPtclList.size() == 0){
			resultMap.put("RES_CD", "00010");
			resultMap.put("RES_MSG", "no data");
			return resultMap;		//조회결과 없음
		}
				
		//RFC 함수명 및 참조 테이블 셋팅
		if("7030".equals(coopCoCd)){			//엠즈씨드
			functionName = "ZFI_MAEILDO_MEMBERSHIP_SALES";	//SAP 함수명
			tableName = "I_ZPOST0160";						//타겟 테이블명
		}else if("7010".equals(coopCoCd)){		//매일유업
			functionName = "ZFI_TMEMBERSHIP_SALES_INFO";	
			tableName = "I_ZPOST1010";						
		}
		
		try {
			//연결 프로퍼티 생성
			abapName = setConnectProperties(coopCoCd);
			
			//SAP 커넥션 인스턴스 얻어오기
			JCoDestination destination = JCoDestinationManager.getDestination(abapName);
			
			//연결정보확인
			logger.info("jco Attributes : "+destination.getAttributes());
			
			//SAP 커넥션으로부터 필요함수 인스턴스 호출
			JCoFunction function = destination.getRepository().getFunction(functionName);
			
			if(function == null){
				throw new RuntimeException(functionName+" not found in SAP.");
			}		
			
			//해당함수의 테이블 파라미터 가져오기 : 테이블은 input/output 모두 사용가능. 본 코드는 입력용으로 테이블 파라미터 사용
			JCoTable codes = function.getTableParameterList().getTable(tableName);
			
			//전송데이터 셋팅
			codes = setActvPtclSapParam(codes, giftCardActvPtclList, coopCoCd);
			
			//SAP 함수 실행호출
			function.execute(destination);
			
			//처리결과 파라미터 가져오기
			JCoParameterList output = function.getExportParameterList();
			
			//메시지 유형: S 성공, E 오류, W 경고, I 정보, A 중단
			eReturnType = (String) output.getStructure("E_RETURN").getValue("TYPE");
			eReturnMessage = (String) output.getStructure("E_RETURN").getValue("MESSAGE");
			
			logger.info("eReturnType : "+eReturnType);				
			logger.info("eReturnMessage : "+eReturnMessage);
			
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("RES_CD", "00020");
			resultMap.put("RES_MSG", "sap transmission failure");
			return resultMap;		//SAP 전송 실패
		}
		
		//해당 관계사 내역전송 성공 시 전송여부 update
		if("S".equals(eReturnType)){
			try{
				resCd = updateGiftCardActvPtcl(coopCoCd, param);
				resMsg = "success";
			} catch(Exception e) {
				e.printStackTrace();
				resCd = "00040" ;		//00040 : SAP 전송 성공, 성공여부 업데이트 실패
				resMsg = "sap success but update failure";
			}
		}else{
			resCd = "00030" ; //sap 전송실패(eReturn:error)
			resMsg = "sap process failure";
		}
		
		resultMap.put("RES_CD", resCd);
		resultMap.put("RES_MSG", resMsg);
		
		return resultMap;	
	}
	
	*/
	
	/** 
	 * 기프트카드 충전 소멸 내역 해당 관계사에 전송
	 * @param REQ_DT 집계일자(YYYYMMDD)	 
	 * @param COOPCO_CD 관계사 코드 	  
	 * @param CALL_USER 1:BATCH, 2:ADMIN
	 * @return RES_CD 응답코드 (00000:success, 00010:no data, 00020:sap transmission failure, 00030:sap process failure, 00040:sap success but update failure, 00050: prCrdService error )
	 * @return RES_MSG 응답메시지
	 */
	
	/*
	public Map<String, String> giftCardActvXtnctPtclTrms(Map<String, Object> param){
		
		coopCoCd = (String)param.get("COOPCO_CD");
		
		//해당 관계사의 충전/취소 내역 데이터 조회 
		List<Map<String, Object>> giftCardActvXtnctPtclList = getGiftCardActvXtnctPtclList(coopCoCd,param);
		
		if(giftCardActvXtnctPtclList.size() == 0){
			resultMap.put("RES_CD", "00010");
			resultMap.put("RES_MSG", "no data");
			return resultMap;		//조회결과 없음
		}
		
		//RFC 함수명 및 참조 테이블 셋팅
		if("7010".equals(coopCoCd)){		//매일유업
			functionName = "ZFI_TMEMBERSHIP_EXTINCTION_INFO";	
			tableName = "I_ZPOST1050";						
		}
				
		try {
			//연결 프로퍼티 생성
			abapName = setConnectProperties(coopCoCd);
			
			//SAP 커넥션 인스턴스 얻어오기
			JCoDestination destination = JCoDestinationManager.getDestination(abapName);
			
			//연결정보확인
			logger.info("jco Attributes : "+destination.getAttributes());
			
			//SAP 커넥션으로부터 필요함수 인스턴스 호출
			JCoFunction function = destination.getRepository().getFunction(functionName);
			
			if(function == null){
				throw new RuntimeException(functionName+" not found in SAP.");
			}		
			
			//해당함수의 테이블 파라미터 가져오기 : 테이블은 input/output 모두 사용가능. 본 코드는 입력용으로 테이블 파라미터 사용
			JCoTable codes = function.getTableParameterList().getTable(tableName);
			
			//전송데이터 셋팅
			codes = setActvXtnctPtclSapParam(codes, giftCardActvXtnctPtclList, coopCoCd);
			
			//SAP 함수 실행호출
			function.execute(destination);
			
			//처리결과 파라미터 가져오기
			JCoParameterList output = function.getExportParameterList();
			
			//메시지 유형: S 성공, E 오류, W 경고, I 정보, A 중단
			eReturnType = (String) output.getStructure("E_RETURN").getValue("TYPE");
			eReturnMessage = (String) output.getStructure("E_RETURN").getValue("MESSAGE");
			
			logger.info("eReturnType : "+eReturnType);				
			logger.info("eReturnMessage : "+eReturnMessage);
			
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("RES_CD", "00020");
			resultMap.put("RES_MSG", "sap transmission failure");
			return resultMap;		//SAP 전송 실패
		}
		
		//해당 관계사 내역전송 성공 시 전송여부 update
		if("S".equals(eReturnType)){
			try{
				resCd = updateGiftCardActvXtnctPtcl(coopCoCd, param);
				resMsg = "success";
			} catch(Exception e) {
				e.printStackTrace();
				resCd = "00040" ;		//00040 : SAP 전송 성공, 성공여부 업데이트 실패
				resMsg = "sap success but update failure";
			}
		}else{
			resCd = "00030" ; //sap 전송실패(eReturn:error)
			resMsg = "sap process failure";
		}
		
		resultMap.put("RES_CD", resCd);
		resultMap.put("RES_MSG", resMsg);
		
		return resultMap;	
	}
*/

	/** 
	 * 기프트카드 사용 내역  해당 관계사에 전송 API
	 * @param REQ_DT 집계일자 (배치 실행 -1일 ,YYYYMMDD)	 
	 * @param COOPCO_CD 관계사 코드 			
	 * @param CALL_USER 1:BATCH, 2:ADMIN		
	 * @return RES_CD 응답코드 (00000:success, 00010:no data, 00020:sap transmission failure, 00030:sap process failure, 00040:sap success but update failure, 00050: prCrdService error )
	 * @return RES_MSG 응답메시지
	 */
	/*
	public Map<String, String> giftCardUsePtclTrms(Map<String, Object> param) {
		coopCoCd = (String)param.get("COOPCO_CD");
		
		//해당 관계사의 사용 내역 데이터 조회 
		List<Map<String, Object>> giftCardUsePtclList = getGiftCardUsePtclList(coopCoCd,param);
		
		if(giftCardUsePtclList.size() == 0){
			resultMap.put("RES_CD", "00010");
			resultMap.put("RES_MSG", "no data");
			return resultMap;		//조회결과 없음
		}
		
		//RFC 함수명 및 참조 테이블 셋팅
		if("7010".equals(coopCoCd)){		//매일유업
			functionName = "ZFI_TMEMBERSHIP_USING_INFO";
			tableName = "I_ZPOST1040";
		}
		
		try {
			//연결 프로퍼티 생성
			abapName = setConnectProperties(coopCoCd);
			
			//SAP 커넥션 인스턴스 얻어오기
			JCoDestination destination = JCoDestinationManager.getDestination(abapName);
			
			//연결정보확인
			logger.info("jco Attributes : "+destination.getAttributes());
			
			//SAP 커넥션으로부터 필요함수 인스턴스 호출
			JCoFunction function = destination.getRepository().getFunction(functionName);
			
			if(function == null){
				throw new RuntimeException(functionName+" not found in SAP.");
			}		
			
			//해당함수의 테이블 파라미터 가져오기 : 테이블은 input/output 모두 사용가능. 본 코드는 입력용으로 테이블 파라미터 사용
			JCoTable codes = function.getTableParameterList().getTable(tableName);
			
			//전송데이터 셋팅
			codes = setUsePtclSapParam(codes, giftCardUsePtclList, coopCoCd);
			
			//SAP 함수 실행호출
			function.execute(destination);
			
			//처리결과 파라미터 가져오기
			JCoParameterList output = function.getExportParameterList();
			
			//메시지 유형: S 성공, E 오류, W 경고, I 정보, A 중단
			eReturnType = (String) output.getStructure("E_RETURN").getValue("TYPE");
			eReturnMessage = (String) output.getStructure("E_RETURN").getValue("MESSAGE");
			
			logger.info("eReturnType : "+eReturnType);				
			logger.info("eReturnMessage : "+eReturnMessage);
			
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("RES_CD", "00020");
			resultMap.put("RES_MSG", "sap transmission failure");
			return resultMap;		//SAP 전송 실패
		}
		
		//해당 관계사 내역전송 성공 시 전송여부 update
		if("S".equals(eReturnType)){
			try{
				resCd = updateGiftCardUsePtcl(coopCoCd, param);
				resMsg = "success";
			} catch(Exception e) {
				e.printStackTrace();
				resCd = "00040" ;		//00040 : SAP 전송 성공, 성공여부 업데이트 실패
				resMsg = "sap success but update failure";
			}
		}else{
			resCd = "00030" ; //sap 전송실패(eReturn:error)
			resMsg = "sap process failure";
		}
		
		resultMap.put("RES_CD", resCd);
		resultMap.put("RES_MSG", resMsg);
		
		return resultMap;	
	}
	*/

	/** 
	 * 기프트카드 환불 처리 결과 수신 API
	 * @param REQ_DT 집계일자 (배치 실행 -1일 ,YYYYMMDD)	 
	 * @param COOPCO_CD 관계사 코드 			
	 * @param CALL_USER 1:BATCH, 2:ADMIN		
	 * @return RES_CD 응답코드 (00000:success, 00010:no data, 00020:sap transmission failure, 00030:sap process failure, 00040:sap success but update failure, 00050: prCrdService error )
	 * @return RES_MSG 응답메시지
	 */
	/*
	public Map<String, String> giftCardRefdReqPtclRecvT(Map<String, Object> param) {
		coopCoCd = (String)param.get("COOPCO_CD");
		
		//기프트카드 환불 진행중 리스트 조회 
		param.put("CRD_ST", "92");			//카드 상태[00:대기, 10:정상, 90:분실신고, 91:해지, 92:환불, 99:폐기]
		param.put("REFD_ST", "03");			//환불 상태[00:정상,01:환불신청,02:환불완료, 03:환불진행중,99:환불오류]
		param.put("REFD_ST_AFTER", "02");	//환불신청 성공 시 변환할 상태값 -> 02:환불완료
		
		List<Map<String, Object>> giftCardRefdReqPtclRecvList = getGiftCardRefdReqPtclRecvList(coopCoCd,param);
		
		if(giftCardRefdReqPtclRecvList.size() == 0){
			resultMap.put("RES_CD", "00010");
			resultMap.put("RES_MSG", "no data");
			return resultMap;		//조회결과 없음
		}
		
		//RFC 함수명 및 참조 테이블 셋팅
		if("7010".equals(coopCoCd)){		//매일유업
			functionName = "ZFI_TMEMBERSHIP_RETURN";	
			tableName = "T_ZPOST1030";
		}
		
		try {
			//연결 프로퍼티 생성
			abapName = setConnectProperties(coopCoCd);
			
			//SAP 커넥션 인스턴스 얻어오기
			JCoDestination destination = JCoDestinationManager.getDestination(abapName);
			
			//연결정보확인
			logger.info("jco Attributes : "+destination.getAttributes());
			
			//SAP 커넥션으로부터 필요함수 인스턴스 호출
			JCoFunction function = destination.getRepository().getFunction(functionName);					
			
			if(function == null){
				throw new RuntimeException(functionName+" not found in SAP.");
			}		
			
			//해당함수의 테이블 파라미터 가져오기 : 테이블은 input/output 모두 사용가능. 본 코드는 입력용으로 테이블 파라미터 사용
			JCoTable codes = function.getTableParameterList().getTable(tableName);
			
			//전송데이터 셋팅
			codes = setRefdReqPtclRecvSapParam(codes, giftCardRefdReqPtclRecvList, coopCoCd);
			
			//SAP 함수 실행호출
			function.execute(destination);
			
			//처리결과 파라미터 가져오기
			JCoParameterList output = function.getExportParameterList();
						
			param = setReturnList(param, codes);
			
			//메시지 유형: S 성공, E 오류, W 경고, I 정보, A 중단
			eReturnType = (String) output.getStructure("E_RETURN").getValue("TYPE");
			eReturnMessage = (String) output.getStructure("E_RETURN").getValue("MESSAGE");
			
			logger.info("eReturnType : "+eReturnType);				
			logger.info("eReturnMessage : "+eReturnMessage);
			
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("RES_CD", "00020");
			resultMap.put("RES_MSG", "sap transmission failure");
			return resultMap;		//SAP 전송 실패
		}
				
		
		//환불 처리 결과 조회 성공 시 데이터 update
		if("S".equals(eReturnType)){
			try {
				resCd = updateGiftCardRefdReqPtclRecv(coopCoCd, param);
				resMsg = "success";
			} catch (Exception e) {				
				e.printStackTrace();
				TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();	//강제 롤백처리
				resCd = "00040" ;		//00040 : SAP 전송 성공, 성공여부 업데이트 실패
				resMsg = "sap success but update failure";
			}
		}else{
			resCd = "00030" ; //sap 전송실패(eReturn:error)
			resMsg = "sap process failure";
		}
		
		resultMap.put("RES_CD", resCd);
		resultMap.put("RES_MSG", resMsg);
		
		return resultMap;	
	}

*/
	
	/** 
	 * 기프트카드환불 신청 내역  해당 관계사에 전송 API
	 * @param REQ_DT 집계일자 (배치 실행 -1일 ,YYYYMMDD)	 
	 * @param COOPCO_CD 관계사 코드 			
	 * @param CALL_USER 1:BATCH, 2:ADMIN		
	 * @return RES_CD 응답코드 (00000:success, 00010:no data, 00020:sap transmission failure, 00030:sap process failure, 00040:sap success but update failure, 00050: prCrdService error )
	 * @return RES_MSG 응답메시지
	 */
	/*
	public Map<String, String> giftCardRefdReqPtclTrmsT(Map<String, Object> param) {
		coopCoCd = (String)param.get("COOPCO_CD");
		
		//기프트카드 환불 신청 리스트 조회 
		param.put("CRD_ST", "92");			//카드 상태[00:대기, 10:정상, 90:분실신고, 91:해지, 92:환불, 99:폐기]
		param.put("REFD_ST", "01");			//환불 상태[00:정상,01:환불신청,02:환불완료, 03:환불진행중,99:환불오류]
		param.put("REFD_ST_AFTER", "03");	//환불신청 성공 시 변환할 상태값 -> 03:환불진행중
		
		List<Map<String, Object>> giftCardRefdReqPtclList = getGiftCardRefdReqPtclList(coopCoCd,param);
		
		if(giftCardRefdReqPtclList.size() == 0){
			resultMap.put("RES_CD", "00010");
			resultMap.put("RES_MSG", "no data");
			return resultMap;		//조회결과 없음
		}
		
		//RFC 함수명 및 참조 테이블 셋팅
		if("7010".equals(coopCoCd)){		//매일유업
			functionName = "ZFI_TMEMBERSHIP_SALES_INFO";	
			tableName = "I_ZPOST1010";
		}
		
		try {
			//연결 프로퍼티 생성
			abapName = setConnectProperties(coopCoCd);
			
			//SAP 커넥션 인스턴스 얻어오기
			JCoDestination destination = JCoDestinationManager.getDestination(abapName);
			
			//연결정보확인
			logger.info("jco Attributes : "+destination.getAttributes());
			
			//SAP 커넥션으로부터 필요함수 인스턴스 호출
			JCoFunction function = destination.getRepository().getFunction(functionName);
			
			if(function == null){
				throw new RuntimeException(functionName+" not found in SAP.");
			}		
			
			//해당함수의 테이블 파라미터 가져오기 : 테이블은 input/output 모두 사용가능. 본 코드는 입력용으로 테이블 파라미터 사용
			JCoTable codes = function.getTableParameterList().getTable(tableName);
			
			//전송데이터 셋팅
			codes = setRefdReqPtclSapParam(codes, giftCardRefdReqPtclList, coopCoCd);
			
			//SAP 함수 실행호출
			function.execute(destination);
			
			//처리결과 파라미터 가져오기
			JCoParameterList output = function.getExportParameterList();
			
			//메시지 유형: S 성공, E 오류, W 경고, I 정보, A 중단
			eReturnType = (String) output.getStructure("E_RETURN").getValue("TYPE");
			eReturnMessage = (String) output.getStructure("E_RETURN").getValue("MESSAGE");
			
			logger.info("eReturnType : "+eReturnType);				
			logger.info("eReturnMessage : "+eReturnMessage);
			
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("RES_CD", "00020");
			resultMap.put("RES_MSG", "sap transmission failure");
			return resultMap;		//SAP 전송 실패
		}
		
		//환불 신청 내역 전송 성공 시 전송여부 update
		if("S".equals(eReturnType)){
			try {
				resCd = updateGiftCardRefdReqPtcl(coopCoCd, param, giftCardRefdReqPtclList);
				resMsg = "success";
			} catch (Exception e) {				
				e.printStackTrace();
				TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();	//강제 롤백처리
				resCd = "00040" ;		//00040 : SAP 전송 성공, 성공여부 업데이트 실패
				resMsg = "sap success but update failure";
			}
		}else{
			resCd = "00030" ; //sap 전송실패(eReturn:error)
			resMsg = "sap process failure";
		}
		
		resultMap.put("RES_CD", resCd);
		resultMap.put("RES_MSG", resMsg);
		
		return resultMap;	
	}
*/

	/** 
	 * 기프트카드 인지세 내역 해당 관계사에 전송
	 * @param REQ_DT 인지세 집계 조회 기준일 (배치 실행 -1일 ,YYYYMMDD)	
	 * @param COOPCO_CD 관계사 코드 			
	 * @param CALL_USER 1:BATCH, 2:ADMIN		
	 * @return RES_CD 응답코드 (00000:success, 00010:no data, 00020:sap transmission failure, 00030:sap process failure, 00040:sap success but update failure, 00050: prCrdService error )
	 * @return RES_MSG 응답메시지
	 */
	/*
	public Map<String, String> giftCardStmpTaxTrms(Map<String, Object> param) {
		coopCoCd = (String)param.get("COOPCO_CD");
		
		//해당 관계사의 인지세 내역 데이터 조회 
		List<Map<String, Object>> giftCardStmpTaxList = getGiftCardStmpTaxList(coopCoCd,param);
		
		if(giftCardStmpTaxList.size() == 0){
			resultMap.put("RES_CD", "00010");
			resultMap.put("RES_MSG", "no data");
			return resultMap;		//조회결과 없음
		}		
		
		//RFC 함수명 및 참조 테이블 셋팅
		if("7010".equals(coopCoCd)){		//매일유업
			functionName = "ZFI_TMEMBERSHIP_STAMP_INFO";	//매일유업 SAP 함수명
			tableName = "I_ZPOST1020";						//매일유업 타겟 테이블명
		}
		
		try {
			//연결 프로퍼티 생성
			abapName = setConnectProperties(coopCoCd);
			
			//SAP 커넥션 인스턴스 얻어오기
			JCoDestination destination = JCoDestinationManager.getDestination(abapName);
			
			//연결정보확인
			logger.info("jco Attributes : "+destination.getAttributes());
			
			//SAP 커넥션으로부터 필요함수 인스턴스 호출
			JCoFunction function = destination.getRepository().getFunction(functionName);
			
			if(function == null){
				throw new RuntimeException(functionName+" not found in SAP.");
			}		
			
			//해당함수의 테이블 파라미터 가져오기 : 테이블은 input/output 모두 사용가능. 본 코드는 입력용으로 테이블 파라미터 사용
			JCoTable codes = function.getTableParameterList().getTable(tableName);
			
			//전송데이터 셋팅
			codes = setStmpTaxSapParam(codes, giftCardStmpTaxList, coopCoCd);
			
			//SAP 함수 실행호출
			function.execute(destination);
			
			//처리결과 파라미터 가져오기
			JCoParameterList output = function.getExportParameterList();
			
			//메시지 유형: S 성공, E 오류, W 경고, I 정보, A 중단
			eReturnType = (String) output.getStructure("E_RETURN").getValue("TYPE");
			eReturnMessage = (String) output.getStructure("E_RETURN").getValue("MESSAGE");
			
			logger.info("eReturnType : "+eReturnType);				
			logger.info("eReturnMessage : "+eReturnMessage);
			
		} catch (Exception e) {
			e.printStackTrace();
			resultMap.put("RES_CD", "00020");
			resultMap.put("RES_MSG", "sap transmission failure");
			return resultMap;		//SAP 전송 실패
		}
			
		
		//해당 관계사 내역전송 성공 시 전송여부 update
		if("S".equals(eReturnType)){
			try{
				resCd = updateGiftCardStmpTax(coopCoCd, param);
				resMsg = "success";
			} catch(Exception e) {
				e.printStackTrace();
				resCd = "00040" ;		//00040 : SAP 전송 성공, 성공여부 업데이트 실패
				resMsg = "sap success but update failure";
			}
		}else{
			resCd = "00030" ; //sap 전송실패(eReturn:error)
			resMsg = "sap process failure";
		}
		
		resultMap.put("RES_CD", resCd);
		resultMap.put("RES_MSG", resMsg);
		
		return resultMap;
	}
	 */
	


	/**
	 * 해당 관계사의 기프트카드 충전/취소 내역 데이터 조회
	 * @param coopCoCd 
	 * @param param - REQ_DT 
	 * @return giftCardActvPtclList
	 */
	/*
	private List<Map<String, Object>> getGiftCardActvPtclList(String coopCoCd, Map<String, Object> param) {
		
		List<Map<String, Object>> giftCardActvPtclList = new ArrayList<Map<String, Object>>();
				
		//관계사에 따라 조회
		if("7030".equals(coopCoCd)){			//엠즈씨드
			giftCardActvPtclList = prCrdMapper.selectGiftCardActvPtcl7030(param);	
		}else if("7010".equals(coopCoCd)){		//매일유업
			giftCardActvPtclList = prCrdMapper.selectGiftCardActvPtcl7010(param);	
		}
				
		return giftCardActvPtclList;
	}
	*/
	
	/**
	 * 해당 관계사의 기프트카드 충전 소멸 내역 데이터 조회
	 * @param coopCoCd 
	 * @param param - REQ_DT 
	 * @return giftCardActvPtclList
	 */
	/*
	private List<Map<String, Object>> getGiftCardActvXtnctPtclList(String coopCoCd, Map<String, Object> param) {
		
		List<Map<String, Object>> giftCardActvXtnctPtclList = new ArrayList<Map<String, Object>>();
		
		//관계사에 따라 조회
		if("7010".equals(coopCoCd)){		//매일유업
			//CALL_USER에 따라 REQ_DT 변형, ADMIN에서는YYYYMM00 의 형태로 들어온다.
			if("2".equals(param.get("CALL_USER"))){
				param.put("REQ_DT", ((String)param.get("REQ_DT")).substring(0,6));
			}
			giftCardActvXtnctPtclList = prCrdMapper.selectGiftCardActvXtnctPtcl7010(param);	
		}
		
		return giftCardActvXtnctPtclList;
	}
	*/
	
	/**
	 * 해당 관계사의 기프트카드 사용 내역 데이터 조회
	 * @param coopCoCd 
	 * @param param - REQ_DT 
	 * @return giftCardUsePtclList
	 */
	/*
	private List<Map<String, Object>> getGiftCardUsePtclList(String coopCoCd, Map<String, Object> param) {
		
		List<Map<String, Object>> giftCardUsePtclList = new ArrayList<Map<String, Object>>();
		
		//관계사에 따라 조회
		if("7010".equals(coopCoCd)){		//매일유업
			giftCardUsePtclList = prCrdMapper.selectGiftCardUsePtcl7010(param);	
		}
				
		return giftCardUsePtclList;
	}
	*/
	
	
	/**
	 * 기프트카드 환불 진행중 데이터 조회
	 * @param coopCoCd 
	 * @param param - REQ_DT 
	 * @param param - CRD_ST 카드 상태[92:환불]
	 * @param param - REFD_ST 환불 상태[03:환불진행중]
	 * @return getGiftCardRefdReqPtclList
	 */
	/*
	private List<Map<String, Object>> getGiftCardRefdReqPtclRecvList(String coopCoCd, Map<String, Object> param) {
		List<Map<String, Object>> giftCardRefdReqPtclRecvList = new ArrayList<Map<String, Object>>();
		
		//관계사에 따라 조회
		if("7010".equals(coopCoCd)){		//매일유업
			giftCardRefdReqPtclRecvList = prCrdMapper.selectGiftCardRefdReqPtclRecv7010(param);	
		}
				
		return giftCardRefdReqPtclRecvList;
	}
	*/
	
	/**
	 * 기프트카드 환불 신청 내역 데이터 조회
	 * @param coopCoCd 
	 * @param param - REQ_DT 
	 * @param param - CRD_ST 카드 상태[92:환불]
	 * @param param - REFD_ST 환불 상태[01:환불신청]
	 * @return getGiftCardRefdReqPtclList
	 */
	/*
	private List<Map<String, Object>> getGiftCardRefdReqPtclList(String coopCoCd, Map<String, Object> param) {
		List<Map<String, Object>> giftCardRefdReqPtclList = new ArrayList<Map<String, Object>>();
		
		//관계사에 따라 조회
		if("7010".equals(coopCoCd)){		//매일유업
			giftCardRefdReqPtclList = prCrdMapper.selectGiftCardRefdReqPtcl7010(param);	
		}
		
		return giftCardRefdReqPtclList;
	}
	*/
	
	/**
	 * 해당 관계사의 기프트카드 인지세 내역 데이터 조회
	 * @param coopCoCd 
	 * @param param - REQ_DT 인지세 집계 조회 기준일 (배치 실행 -1일 ,YYYYMMDD)	
	 * @param param - CALL_USER 1:BATCH, 2:ADMIN	 
	 * @return giftCardStmpTaxList
	 */
	/*
	private List<Map<String, Object>> getGiftCardStmpTaxList(String coopCoCd, Map<String, Object> param) {
		
		List<Map<String, Object>> giftCardStmpTaxList = new ArrayList<Map<String, Object>>();
		
		//관계사에 따라 조회
		if("7010".equals(coopCoCd)){		//매일유업
			//CALL_USER에 따라 REQ_DT 변형, ADMIN에서는YYYYMM00 의 형태로 들어온다.
			if("2".equals(param.get("CALL_USER"))){
				param.put("REQ_DT", ((String)param.get("REQ_DT")).substring(0,6));
			}
			giftCardStmpTaxList = prCrdMapper.selectGiftCardStmpTax7010(param);	
		}
				
		return giftCardStmpTaxList;
	}
	*/

	/**
	 * 기프트카드 충전/충전취소 데이터 셋팅
	 * @param JCoTable codes 
	 * @param giftCardActvPtclList 
	 * @param coopCoCd 
	 * @return JCoTable codes
	 */
	/*
	private JCoTable setActvPtclSapParam(JCoTable codes, List<Map<String, Object>> giftCardActvPtclList, String coopCoCd) {
		
		setCurDate();	//현재시간 셋팅
				
		if("7030".equals(coopCoCd)){			//엠즈씨드
			//루프돌면서 테이블 파라미터에 데이터 세팅
			for(Map<String, Object> eachMap : giftCardActvPtclList){
				codes.appendRow();
				
				codes.setValue("BUKRS"			, "8000"						);	//회사코드, I/F 아님'8000' 으로 고정
				codes.setValue("ZSYS_GUBUN"		, "5"							);	//시스템 구분,  1: 기존 엠즈씨드POS , 5:통합멤버쉽 시스템
				codes.setValue("ZGUBUN"			, eachMap.get("SETL_DV")		);	//업무구분, 1: Membership 충전, 2.  Membership 취소, 3:  Membership 환불
				codes.setValue("BLDAT"			, eachMap.get("SETL_DT")		);	//마감일자, POS 일마감 일자 (통합멤버쉽 일마감 일자)
				codes.setValue("ZSERIAL_NO"		, eachMap.get("IDX")			);	//순번,  해당일 거래의 순차번호
				codes.setValue("KUNNR"			, eachMap.get("STOR_CD")		);	//엠즈씨드 매장코드=(SAP고객코드), 거래발생 엠즈씨드 매장코드
				codes.setValue("ZCASH_AMT"		, eachMap.get("CASH_AMT_SUM")	);	//현금결재금액, 현금 거래 발생 금액
				codes.setValue("ZCARD_AMT"		, eachMap.get("CRD_AMT_SUM")	);	//카드결재금액, 카드 거래 발생 금액
//				codes.setValue("ZWEB_AMT"		, eachMap.get("")				);	//WEB 결재금액, WEB 거래 발생금액(LG U+)
//				codes.setValue("ZDC_CASH_AMT"	, eachMap.get("")				);	//할인 판매장려금 (현금거래)
//				codes.setValue("ZUP_CASH_AMT"	, eachMap.get("")				);	//할증 판매장려금 (현금거래)
//				codes.setValue("ZDC_CARD_AMT"	, eachMap.get("")				);	//할인 판매장려금 (카드거래)
//				codes.setValue("ZUP_CARD_AMT"	, eachMap.get("")				);	//할증 판매장려금 (카드거래)
//				codes.setValue("KOSTL"			, eachMap.get("")				);	//매장코스트센터
//				codes.setValue("BANKL"			, eachMap.get("")				);	//은행코드, 환불의 경우에만 해당 은행코드 3자리
//				codes.setValue("BANKN"			, eachMap.get("")				);	//계좌번호, 환불의 경우에만 작성
//				codes.setValue("KOINH"			, eachMap.get("")				);	//예금주명, 환불의 경우에만 작성
//				codes.setValue("ZPERSONAL_NO"	, eachMap.get("")				);	//회원번호, 환불의 경우에만 작성
//				codes.setValue("ZCARD_NO"		, eachMap.get("")				);	//카드번호, 환불의 경우에만 작성
				codes.setValue("IFDAT"			, trmsYmd						);	//전송일자
				codes.setValue("IFZET"			, trmsHms						);	//전송시간
				codes.setValue("IFNAM"			, "MAEILDO"						);	//전송자, 전송ID
			}
		}else if("7010".equals(coopCoCd)){		//매일유업
			//루프돌면서 테이블 파라미터에 데이터 세팅
			for(Map<String, Object> eachMap : giftCardActvPtclList){
				//MaeilDo 집계 데이터 기준 시스템 구분 세팅
				
				String zsysGubun = "5";
				if("A".equals((String) eachMap.get("SETL_DV")) || "B".equals((String) eachMap.get("SETL_DV"))) zsysGubun="6";
				// 이벤트 증정용 기프트카드 추가 | 2018. 7. 6. | jhPark
				if("D".equals((String) eachMap.get("SETL_DV")) || "E".equals((String) eachMap.get("SETL_DV"))) zsysGubun="7";
				// 2018_106_교원제휴카드 | 2018. 7. 18. | jhPark
				if("G".equals((String) eachMap.get("SETL_DV")) || "H".equals((String) eachMap.get("SETL_DV"))) zsysGubun="8";
				
				
				String zGubun = "1";
				if("A".equals((String) eachMap.get("SETL_DV"))) zGubun = "1";
				else if("B".equals((String) eachMap.get("SETL_DV"))) zGubun = "2";
				
				// 이벤트 증정용 기프트카드 추가 | 2018. 7. 6. | jhPark
				else if("D".equals((String) eachMap.get("SETL_DV"))) zGubun = "1";
				else if("E".equals((String) eachMap.get("SETL_DV"))) zGubun = "2";
				
				// 2018_106_교원제휴카드 | 2018. 7. 18. | jhPark
				else if("G".equals((String) eachMap.get("SETL_DV"))) zGubun = "1";
				else if("H".equals((String) eachMap.get("SETL_DV"))) zGubun = "2";
				else zGubun = (String) eachMap.get("SETL_DV");
				
				codes.appendRow();
				
				codes.setValue("BUKRS"			, "1000"							);	//매일유업 회사코드 '1000' 으로 고정
				codes.setValue("INT_BUKRS"		, eachMap.get("COOPCO_CD")			);	//MaeilDO 에서 사용하는 회원사코드 (7000 MaeilDO, 7030 엠즈씨드 등)
				codes.setValue("ZSYS_GUBUN"		, zsysGubun							);	//시스템 구분,  1: 기존 엠즈씨드POS , 5:통합멤버쉽 시스템, 6:내부고객통합멤버쉽, 7:이벤트증정용기프트카드
				codes.setValue("ZGUBUN"			, zGubun							);	//업무구분, 1: Membership 충전, 2.  Membership 취소, 3:  Membership 환불
				codes.setValue("BLDAT"			, eachMap.get("SETL_DT")			);	//MaeilDO 일마감 일자 (통합멤버쉽 일마감 일자)
				codes.setValue("ZSERIAL_NO"		, eachMap.get("IDX")				);	//순번,  해당일 거래의 순차번호
//				codes.setValue("KUNNR"			, ""		);							//거래발생 엠즈씨드 매장코드 --> 공란으로 I/F 함 
				codes.setValue("ZCASH_AMT"		, eachMap.get("CASH_ACTV_AMT_SUM")	);	//현금결재금액, 현금 거래 발생 금액
				codes.setValue("ZCARD_AMT"		, eachMap.get("CRD_ACTV_AMT_SUM")	);	//카드결재금액, 카드 거래 발생 금액
				codes.setValue("ZPAY_GUBUN"		, eachMap.get("MBLE_DV")			);	//모바일 거래구분키 (모바일 거래 구분 키 [10:LG U+, 20:PAYCO, 30:현금(프로모션 카드), 40:KGMob])
				codes.setValue("ZWEB_AMT"		, eachMap.get("MBLE_AMT")			);	//WEB 결재금액, 모바일 금액 (입금될 금액)
				codes.setValue("ZWEB_Q_AMT"		, eachMap.get("MBLE_CPN_AMT")		);	//WEB 결재금액 (쿠폰 금액)
				codes.setValue("ZWEB_P_AMT"		, eachMap.get("MBLE_PINT_AMT")		);	//WEB 결재금액 (포인트 금액)
				codes.setValue("ZWEB_E_AMT"		, eachMap.get("MBLE_ETC_AMT")		);	//WEB 결재금액 (기타 금액)
//				codes.setValue("KOSTL"			, eachMap.get("")					);	//매장코스트센터 --> 공란으로 I/F 함
//				codes.setValue("ZDC_CASH_AMT"	, eachMap.get("")					);	//할인 판매장려금 (현금거래)
//				codes.setValue("ZUP_CASH_AMT"	, eachMap.get("")					);	//할증 판매장려금 (현금거래)
//				codes.setValue("ZDC_CARD_AMT"	, eachMap.get("")					);	//할인 판매장려금 (카드거래)
//				codes.setValue("ZUP_CARD_AMT"	, eachMap.get("")					);	//할증 판매장려금 (카드거래)
//				codes.setValue("BANKL"			, eachMap.get("")					);	//은행코드, 환불의 경우에만 해당 (은행코드 3자리)
//				codes.setValue("BANKN"			, eachMap.get("")					);	//계좌번호, 환불의 경우에만 작성 (은행 계정번호)
//				codes.setValue("KOINH"			, eachMap.get("")					);	//예금주명, 환불의 경우에만 작성 (계정 보유자명)
//				codes.setValue("ZPERSONAL_NO"	, eachMap.get("")					);	//회원번호, 환불의 경우에만 작성
//				codes.setValue("ZCARD_NO"		, eachMap.get("")					);	//카드번호, 환불의 경우에만 작성
				codes.setValue("IFDAT"			, trmsYmd							);			//전송일자
				codes.setValue("IFZET"			, trmsHms							);			//전송시간
				codes.setValue("IFNAM"			, "MAEILDO"							);		//전송자, 전송ID
			}
		}
		
		return codes;
	}
*/	
	
	/**
	 * 기프트카드 충전 소멸 데이터 셋팅
	 * @param JCoTable codes 
	 * @param giftCardActvXtnctPtclList 
	 * @param coopCoCd 
	 * @return JCoTable codes
	 */
	/*
	private JCoTable setActvXtnctPtclSapParam(JCoTable codes, List<Map<String, Object>> giftCardActvXtnctPtclList, String coopCoCd) {
		
		setCurDate();	//현재시간 셋팅

		if("7010".equals(coopCoCd)){		//매일유업
			//루프돌면서 테이블 파라미터에 데이터 세팅
			for(Map<String, Object> eachMap : giftCardActvXtnctPtclList){
				codes.appendRow();
				
				String zsysGubun = "5";
				if("C".equals((String) eachMap.get("SETL_DV"))) zsysGubun="6";
				// 이벤트 증정용 기프트카드 추가 | 2018. 7. 6. | jhPark
				else if("F".equals((String) eachMap.get("SETL_DV"))) zsysGubun="7";
				// 2018_106_교원제휴카드 | 2018. 7. 18. | jhPark
				else if("I".equals((String) eachMap.get("SETL_DV"))) zsysGubun="8";
				
				codes.setValue("BUKRS"			, "1000"							);	//매일유업 회사코드 '1000' 으로 고정
				codes.setValue("INT_BUKRS"		, eachMap.get("COOPCO_CD")			);	//MaeilDO 에서 사용하는 회원사코드 (7000 MaeilDO, 7030 엠즈씨드 등)
				codes.setValue("ZSYS_GUBUN"		, zsysGubun							);	//시스템 구분,  1: 기존 엠즈씨드POS , 6:통합멤버쉽 시스템( 임직원복지카드) , 7:이벤트증정용 기프트카드
				codes.setValue("BLDAT"			, eachMap.get("XTNCT_DT")			);	//MaeilDO 일마감 일자 (통합멤버쉽 일마감 일자)
				codes.setValue("ZSERIAL_NO"		, eachMap.get("IDX")				);	//순번,  해당일 거래의 순차번호
//				codes.setValue("KUNNR"			, ""		);							//거래발생 엠즈씨드 매장코드 --> 공란으로 I/F 함 
				codes.setValue("ZEXTINCT_AMT"	, eachMap.get("XTNCT_AMT")			);	//충전소멸금액
//				codes.setValue("KOSTL"			, eachMap.get("")					);	//매장코스트센터 --> 공란으로 I/F 함
				codes.setValue("IFDAT"			, trmsYmd							);	//전송일자
				codes.setValue("IFZET"			, trmsHms							);	//전송시간
				codes.setValue("IFNAM"			, "MAEILDO"							);	//전송자, 전송ID
			}
		}
		
		return codes;
	}
*/

	/**
	 * 기프트카드 사용 내역 데이터 셋팅
	 * @param JCoTable codes 
	 * @param giftCardActvPtclList 
	 * @param coopCoCd 
	 * @return JCoTable codes
	 */
	/*
	private JCoTable setUsePtclSapParam(JCoTable codes, List<Map<String, Object>> giftCardUsePtclList, String coopCoCd) {
		
		setCurDate();	//현재시간 셋팅
		
		if("7010".equals(coopCoCd)){			//매일유업
			//루프돌면서 테이블 파라미터에 데이터 세팅
			for(Map<String, Object> eachMap : giftCardUsePtclList){
				codes.appendRow();
				
				codes.setValue("BUKRS"			, "1000"							);	//매일유업 회사코드 '1000' 으로 고정
				codes.setValue("INT_BUKRS"		, eachMap.get("COOPCO_CD")			);	//MaeilDO 에서 사용하는 회원사코드 (7000 MaeilDO, 7030 엠즈씨드 등)
				codes.setValue("ZSYS_GUBUN"		, "5"								);	//시스템 구분,  1: 기존 엠즈씨드POS , 5:통합멤버쉽 시스템
				codes.setValue("ZGUBUN2"		, eachMap.get("SALE_DV")			);	//업무구분, 1: Membership 사용, 2.  Membership 사용취소
				codes.setValue("BLDAT"			, eachMap.get("USE_DT")				);	//MaeilDO 일마감 일자 (통합멤버쉽 일마감 일자)
				codes.setValue("ZSERIAL_NO"		, eachMap.get("IDX")				);	//순번,  해당일 거래의 순차번호
//				codes.setValue("KUNNR"			, ""		);							//거래발생 엠즈씨드 매장코드 --> 공란으로 I/F 함 
				codes.setValue("ZUSING_AMT"		, eachMap.get("USE_AMT")			);	//사용/취소 금액
//				codes.setValue("KOSTL"			, eachMap.get("")					);	//매장코스트센터 --> 공란으로 I/F 함
				codes.setValue("IFDAT"			, trmsYmd							);	//전송일자
				codes.setValue("IFZET"			, trmsHms							);	//전송시간
				codes.setValue("IFNAM"			, "MAEILDO"							);	//전송자, 전송ID
			}
		}
		
		return codes;
	}
	*/
	
	/**
	 * 기프트카드 환불 진행중 데이터 셋팅
	 * @param JCoTable codes 
	 * @param giftCardRefdReqPtclList 
	 * @param coopCoCd 
	 * @return JCoTable codes
	 */
	/*
	private JCoTable setRefdReqPtclRecvSapParam(JCoTable codes, List<Map<String, Object>> giftCardRefdReqPtclRecvList, String coopCoCd) {

		setCurDate();	//현재시간 셋팅
		
		if("7010".equals(coopCoCd)){			//매일유업
			//루프돌면서 테이블 파라미터에 데이터 세팅
			for(Map<String, Object> eachMap : giftCardRefdReqPtclRecvList){
				codes.appendRow();
				
				codes.setValue("BUKRS"			, "1000"							);	//매일유업 회사코드 '1000' 으로 고정
				codes.setValue("INT_BUKRS"		, "7000"							);	//MaeilDO 에서 사용하는 회원사코드 (7000 MaeilDO, 7030 엠즈씨드 등) -> 환불 시 MaeilDO 사용
				codes.setValue("ZSYS_GUBUN"		, "5"								);	//시스템 구분,  1: 기존 엠즈씨드POS , 5:통합멤버쉽 시스템
				codes.setValue("ZGUBUN"			, "3"								);	//업무구분, 1: Membership 충전, 2.  Membership 취소, 3:  Membership 환불
				codes.setValue("BLDAT"			, eachMap.get("REQ_DTM")			);	//환불요청일, MaeilDO 일마감 일자 (통합멤버쉽 일마감 일자) -> 환불 시 고객 환불요청일을 8자리로 변환하여 전송
				codes.setValue("ZSERIAL_NO"		, eachMap.get("IDX")				);	//순번,  해당일 거래의 순차번호
				codes.setValue("ZCARD_NO"		, eachMap.get("CRD_ID")				);	//카드번호
				codes.setValue("ZPERSONAL_NO"	, eachMap.get("UNFY_MMB_NO")		);	//통합회원번호
				codes.setValue("IFDAT"			, trmsYmd							);	//전송일자
				codes.setValue("IFZET"			, trmsHms							);	//전송시간
				codes.setValue("IFNAM"			, "MAEILDO"							);	//전송자, 전송ID
			}
		}
		
		return codes;
	}
	*/
	
	
	/** 
	 * 기프트카드 환불 처리 내역(JCoTable)을 리스트 형태로 변환하여 param에 추가
	 * @param param
	 * @param JCoTable codes	
	 * @return param
	 */
	/*
	private Map<String, Object> setReturnList(Map<String, Object> param, JCoTable codes) {		
		List<Map<String, Object>> returnListTempHist = new ArrayList<Map<String, Object>>();	//환불 처리 결과를 담을 이력용 리스트 
		List<Map<String, Object>> returnListTempSuccess = new ArrayList<Map<String, Object>>();	//환불 성공 결과를 담을 이력용 임시 리스틈
		List<Map<String, Object>> returnListTempFailuer = new ArrayList<Map<String, Object>>();	//환불 실패 결과를 담을 이력용 임시 리스트 
		
		//sap에서 수신한 데이터를 리스트 형태로 변환
		for(int i=0 ; i<codes.getNumRows() ; i++){
			Map<String, Object> eachRowData = new HashMap<String, Object>();
			codes.setRow(i);
			
			eachRowData.put("REQ_DTM",			(codes.getString("BLDAT")).replaceAll("-", ""));		//환불요청일
			eachRowData.put("IDX", 				codes.getString("ZSERIAL_NO"));		//순번,  해당일 거래의 순차번호
			eachRowData.put("CRD_ID",			codes.getString("ZCARD_NO"));		//카드번호
			eachRowData.put("UNFY_MMB_NO",		codes.getString("ZPERSONAL_NO"));	//통합회원번호
			eachRowData.put("NM_INQ_SCSS_YN",	codes.getString("ZFLAG1"));			//성명조회 pos 전송여부, Y:성공, N:실패, NULL:진행중
			eachRowData.put("REFD_SCSS_YN",		codes.getString("ZFLAG2"));			//대금지급 pos 전송여부, Y:성공, NULL:진행중
			eachRowData.put("MSG_TYP",			codes.getString("MSGTY"));			//메시지 유형(S:성공, E:에러), 조회요청한 데이터가 sap측에 없는 경우 E로 리턴됨
			eachRowData.put("MSG_CTT",			codes.getString("MSGTX"));			//메시지 텍스트
			eachRowData.put("STATS",			codes.getString("STATS"));			//성명조회결과코드
			eachRowData.put("TEXT",				codes.getString("TEXT"));			//성명조회오류사유
			
			returnListTempHist.add(eachRowData);
			
			//성명조회 및 대금지급이 완료된 경우(환불성공)에만 카드마스터에 업데이트
			if("Y".equals(codes.getString("ZFLAG1")) && "Y".equals(codes.getString("ZFLAG2"))){
				returnListTempSuccess.add(eachRowData);				
			//환불실패 시 후처리를 위핸 리스트  
			}else if("E".equals(codes.getString("MSGTY"))){		//조회요청한 데이터가 sap측에 없는 경우
				eachRowData.put("REFD_RTN_CD", "99");
				eachRowData.put("REFD_RTN_MSG", "no data at sap system.");
				returnListTempFailuer.add(eachRowData);
			}else if("N".equals(codes.getString("ZFLAG1"))){	//성명조회에 실패한 경우
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
	 * 기프트카드 환불신청내역 데이터 셋팅
	 * @param JCoTable codes 
	 * @param giftCardRefdReqPtclList 
	 * @param coopCoCd 
	 * @return JCoTable codes
	 */
	/*
	private JCoTable setRefdReqPtclSapParam(JCoTable codes, List<Map<String, Object>> giftCardRefdReqPtclList, String coopCoCd) {
		
		setCurDate();	//현재시간 셋팅
		
		if("7010".equals(coopCoCd)){			//매일유업
			//루프돌면서 테이블 파라미터에 데이터 세팅
			for(Map<String, Object> eachMap : giftCardRefdReqPtclList){
				
				logger.info("=========================기프트카드 환불 신청 내역 데이터 셋팅 정보 [s]===============================");
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
				logger.info("=========================기프트카드 환불 신청 내역 데이터 셋팅 정보 [e]===============================");
				
				codes.appendRow();
				
				codes.setValue("BUKRS"			, "1000"							);	//매일유업 회사코드 '1000' 으로 고정
				codes.setValue("INT_BUKRS"		, "7000"							);	//MaeilDO 에서 사용하는 회원사코드 (7000 MaeilDO, 7030 엠즈씨드 등) -> 환불 시 MaeilDO 사용
				codes.setValue("ZSYS_GUBUN"		, "5"								);	//시스템 구분,  1: 기존 엠즈씨드POS , 5:통합멤버쉽 시스템
				codes.setValue("ZGUBUN"			, "3"								);	//업무구분, 1: Membership 충전, 2.  Membership 취소, 3:  Membership 환불
				codes.setValue("BLDAT"			, eachMap.get("REQ_DT")				);	//환불요청일, MaeilDO 일마감 일자 (통합멤버쉽 일마감 일자) -> 환불 시 고객 환불요청일을 8자리로 변환하여 전송
				codes.setValue("ZSERIAL_NO"		, eachMap.get("IDX")				);	//순번,  해당일 거래의 순차번호
//				codes.setValue("KUNNR"			, ""		);							//거래발생 엠즈씨드 매장코드 --> 공란으로 I/F 함 
				codes.setValue("ZCASH_AMT"		, eachMap.get("CASH_ACTV_AMT_SUM")	);	//현금결재금액, 현금 거래 발생 금액
				codes.setValue("ZCARD_AMT"		, eachMap.get("CRD_ACTV_AMT_SUM")	);	//카드결재금액, 카드 거래 발생 금액
//				codes.setValue("ZPAY_GUBUN"		, eachMap.get("MBLE_DV")			);	//모바일 거래구분키 (10 : LG U+,  20 : PAYCO)
				codes.setValue("ZWEB_AMT"		, eachMap.get("REFD_AMT_POSI")		);	//WEB 결재금액, 모바일 금액 (입금될 금액) -> 환불 시 환불금액으로 사용
//				codes.setValue("ZWEB_Q_AMT"		, eachMap.get("MBLE_CPN_AMT")		);	//WEB 결재금액 (쿠폰 금액)
//				codes.setValue("ZWEB_P_AMT"		, eachMap.get("MBLE_PINT_AMT")		);	//WEB 결재금액 (포인트 금액)
//				codes.setValue("ZWEB_E_AMT"		, eachMap.get("MBLE_ETC_AMT")		);	//WEB 결재금액 (기타 금액)
//				codes.setValue("KOSTL"			, eachMap.get("")					);	//매장코스트센터 --> 공란으로 I/F 함
//				codes.setValue("ZDC_CASH_AMT"	, eachMap.get("")					);	//할인 판매장려금 (현금거래)
//				codes.setValue("ZUP_CASH_AMT"	, eachMap.get("")					);	//할증 판매장려금 (현금거래)
//				codes.setValue("ZDC_CARD_AMT"	, eachMap.get("")					);	//할인 판매장려금 (카드거래)
//				codes.setValue("ZUP_CARD_AMT"	, eachMap.get("")					);	//할증 판매장려금 (카드거래)
				codes.setValue("BANKL"			, eachMap.get("BNK_CD")				);	//은행코드, 환불의 경우에만 해당 (은행코드 3자리)
				codes.setValue("BANKN"			, eachMap.get("ACCT_NO")			);	//계좌번호, 환불의 경우에만 작성 (은행 계정번호)
				codes.setValue("KOINH"			, eachMap.get("OWAC_NM")			);	//예금주명, 환불의 경우에만 작성 (계정 보유자명)
				codes.setValue("ZPERSONAL_NO"	, eachMap.get("UNFY_MMB_NO")		);	//회원번호, 환불의 경우에만 작성 (통합회원번호)
				codes.setValue("ZCARD_NO"		, eachMap.get("CRD_ID")				);	//카드번호, 환불의 경우에만 작성
				codes.setValue("IFDAT"			, trmsYmd							);			//전송일자
				codes.setValue("IFZET"			, trmsHms							);			//전송시간
				codes.setValue("IFNAM"			, "MAEILDO"							);		//전송자, 전송ID
			}
		}
		
		return codes;
	}
	*/
	
	/**
	 * 기프트카드 인지세 데이터 셋팅
	 * @param JCoTable codes 
	 * @param giftCardActvPtclList 
	 * @param coopCoCd 
	 * @return JCoTable codes
	 */
	/*
	private JCoTable setStmpTaxSapParam(JCoTable codes, List<Map<String, Object>> giftCardStmpTaxList,	String coopCoCd) {
		
		setCurDate();	//현재시간 셋팅
		
		if("7010".equals(coopCoCd)){			//매일유업
			//루프돌면서 테이블 파라미터에 데이터 세팅
			for(Map<String, Object> eachMap : giftCardStmpTaxList){
				// MaeilDo 집계 데이터 기준 시스템 구분 세팅
				String zsysGubun = "";
				if("1".equals((String) eachMap.get("SETL_DV")) || "2".equals((String) eachMap.get("SETL_DV"))) zsysGubun="5";
				else if("A".equals((String) eachMap.get("SETL_DV")) || "B".equals((String) eachMap.get("SETL_DV"))) zsysGubun="6";
				// 이벤트 증정용 기프트 카드 추가 | 2018. 7. 6. | jhPark
				else if("D".equals((String) eachMap.get("SETL_DV")) || "E".equals((String) eachMap.get("SETL_DV"))) zsysGubun="7";
				// 2018_106_교원제휴카드: | 2018. 7. 18. | jhPark
				else if("G".equals((String) eachMap.get("SETL_DV")) || "H".equals((String) eachMap.get("SETL_DV"))) zsysGubun="8";
				
				codes.appendRow();
				
				codes.setValue("BUKRS"			, "1000"						);	//매일유업 회사코드 '1000' 으로 고정
				codes.setValue("INT_BUKRS"		, eachMap.get("COOPCO_CD")		);	//MaeilDO 에서 사용하는 회원사코드 (7000 MaeilDO, 7030 엠즈씨드 등)
				codes.setValue("ZSYS_GUBUN"		, zsysGubun						);	//시스템 구분,  1: 기존 엠즈씨드POS , 5:통합멤버쉽 시스템, 6:내부고객통합멤버쉽
				codes.setValue("ZSTAMP_GUBUN"	, eachMap.get("TYP_DV")			);	//업무구분, 1: 인지세 발생, 2: 인지세 취소
				codes.setValue("BLDAT"			, eachMap.get("SETL_DT")		);	//MaeilDO 일마감 일자 (통합멤버쉽 일마감 일자)
				codes.setValue("ZSERIAL_NO"		, eachMap.get("IDX")			);	//순번,  해당일 거래의 순차번호
//				codes.setValue("KUNNR"			, ""							);	//거래발생 엠즈씨드 매장코드 --> 공란으로 I/F 함
				codes.setValue("ZSTAMP_QTY1"	, eachMap.get("STMP_TAX_QNT_1")	);	//1만원권 수량
				codes.setValue("ZSTAMP_AMT1"	, eachMap.get("STMP_TAX_AMT_1")	);	//1만원권 금액, 수량 * 50
				codes.setValue("ZSTAMP_QTY2"	, eachMap.get("STMP_TAX_QNT_2")	);	//5만원권 수량
				codes.setValue("ZSTAMP_AMT2"	, eachMap.get("STMP_TAX_AMT_2")	);	//5만원권 금액, 수량 * 200
				codes.setValue("ZSTAMP_QTY3"	, eachMap.get("STMP_TAX_QNT_3")	);	//10만원권 수량
				codes.setValue("ZSTAMP_AMT3"	, eachMap.get("STMP_TAX_AMT_3")	);	//10만원권 금액, 수량 * 400
				codes.setValue("ZSTAMP_QTY4"	, eachMap.get("STMP_TAX_QNT_4")	);	//10만원초과권 수량
				codes.setValue("ZSTAMP_AMT4"	, eachMap.get("STMP_TAX_AMT_4")	);	//10만원초과권 금액, 수량 * 800
				codes.setValue("IFDAT"			, trmsYmd						);	//전송일자
				codes.setValue("IFZET"			, trmsHms						);	//전송시간
				codes.setValue("IFNAM"			, "MAEILDO"						);	//전송자, 전송ID
				
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
	 * 해당 관계사의 기프트카드 충전/취소 내역 전송 여부 업데이트
	 * @param coopCoCd 
	 * @param param - REQ_DT
	 * @return resCd
	 */
	/*
	private String updateGiftCardActvPtcl(String coopCoCd, Map<String, Object> param) {
		
		//유저ID 셋팅		
		callUser = (String) param.get("CALL_USER");
		userId = setUserId(callUser);
		
		param.put("UPDR_ID", userId);
		
		if("7030".equals(coopCoCd)){			//엠즈씨드
			prCrdMapper.updateGiftCardActvPtcl7030(param);	
		}else if("7010".equals(coopCoCd)){		//매일유업
			prCrdMapper.updateGiftCardActvPtcl7010(param);	
		}				

		return "00000"; //응답코드, 전송성공
	}
	*/
	
	/**
	 * 해당 관계사의 기프트카드 충전 소멸 내역 전송 여부 업데이트
	 * @param coopCoCd 
	 * @param param - REQ_DT
	 * @return resCd
	 */
	/*
	private String updateGiftCardActvXtnctPtcl(String coopCoCd, Map<String, Object> param) {
		
		//유저ID 셋팅		
		callUser = (String) param.get("CALL_USER");
		userId = setUserId(callUser);
		
		param.put("UPDR_ID", userId);
		
		if("7010".equals(coopCoCd)){		//매일유업
			prCrdMapper.updateGiftCardActvXtnctPtcl7010(param);	
		}				
		
		return "00000"; //응답코드, 전송성공
	}
	*/
	
	/**
	 * 해당 관계사의 기프트카드 사용 내역 전송 여부 업데이트
	 * @param coopCoCd 
	 * @param param - REQ_DT
	 * @return resCd
	 * 
	 */
	/*
	private String updateGiftCardUsePtcl(String coopCoCd, Map<String, Object> param) {
		
		//유저ID 셋팅		
		callUser = (String) param.get("CALL_USER");
		userId = setUserId(callUser);
		
		param.put("UPDR_ID", userId);
		
		if("7010".equals(coopCoCd)){		//매일유업
			prCrdMapper.updateGiftCardUsePtcl7010(param);	
		}				
		
		return "00000"; //응답코드, 전송성공
	}
	*/
	
	/**
	 * 기프트카드 환불 신청 내역 전송 여부 업데이트
	 * @param coopCoCd 
	 * @param param - REQ_DT 환불요청일(YYYYMMDD)
	 * @param param - CRD_ST 카드 상태[92:환불]
	 * @param param - REFD_ST 환불 상태[01:환불신청]
	 * @param param - REFD_ST_AFTER 환불신청 성공 시 변환할 상태값[03:환불진행중]
	 * @param giftCardRefdReqPtclList 환불신청카드리스트
	 * @return resCd
	 */
	/*
	private String updateGiftCardRefdReqPtcl(String coopCoCd, Map<String, Object> param, List<Map<String, Object>> giftCardRefdReqPtclList ){
		//유저ID 셋팅		
		callUser = (String) param.get("CALL_USER");
		userId = setUserId(callUser);
		
		param.put("UPDR_ID", userId);		
		param.put("PARAM_LIST", giftCardRefdReqPtclList);
		
		if("7010".equals(coopCoCd)){		//매일유업
			//1. 카드마스터 업데이트
			prCrdMapper.updateGiftCardRefdReqPtclMaster(param);
			
			//2. 환불 요청 이력 적재
			prCrdMapper.insertGiftCardRefdReqPtclHist(param);
		}				

		return "00000"; 	//응답코드, 전송성공
	}
*/

	
	/**
	 * 현재 시간 셋팅
	 */
	private void setCurDate() {
		Date curDate = new Date();
		trmsYmd = ymdFormat.format(curDate);	//전송일자
		trmsHms = hmsFormat.format(curDate);	//전송시간
	}
	
	
	/**
	 * JCO 연결 프로퍼티 생성
	 * @param COOPCO_CD 관계사 코드 	
	 */
	/*
	private String setConnectProperties(String coopCoCd) {
		
		Properties connectProperties = new Properties();
		abapName = "";
		
		if("7030".equals(coopCoCd)){			//엠즈씨즈 
			sapAshost	= JProperties.getString("msseed.erpSapJco.ashost");
			sapSysnr	= JProperties.getString("msseed.erpSapJco.sysnr");
			sapClient	= JProperties.getString("msseed.erpSapJco.client");
			sapUser		= JProperties.getString("msseed.erpSapJco.user");
			sapPasswd	= JProperties.getString("msseed.erpSapJco.passwd");
			sapLang		= JProperties.getString("msseed.erpSapJco.lang");
			abapName = "7030_SAP_CONN";
		}else if("7010".equals(coopCoCd)){		//매일유업
			sapAshost	= JProperties.getString("maeil.erpSapJco.ashost");
			sapSysnr	= JProperties.getString("maeil.erpSapJco.sysnr");
			sapClient	= JProperties.getString("maeil.erpSapJco.client");
			sapUser		= JProperties.getString("maeil.erpSapJco.user");
			sapPasswd	= JProperties.getString("maeil.erpSapJco.passwd");
			sapLang		= JProperties.getString("maeil.erpSapJco.lang");
			abapName = "7010_SAP_CONN";
		}
		
		connectProperties.setProperty(DestinationDataProvider.JCO_ASHOST, sapAshost);	//SAP 호스트 정보
		connectProperties.setProperty(DestinationDataProvider.JCO_SYSNR,  sapSysnr);	//인스턴스번호
		connectProperties.setProperty(DestinationDataProvider.JCO_CLIENT, sapClient);	//SAP 클라이언트
		connectProperties.setProperty(DestinationDataProvider.JCO_USER,   sapUser);		//SAP유저명
		connectProperties.setProperty(DestinationDataProvider.JCO_PASSWD, sapPasswd);	//SAP 패스워드
		connectProperties.setProperty(DestinationDataProvider.JCO_LANG,   sapLang);		//언어
		
		//프로퍼티를 이용하여 연결파일을 생성. 실행되고 있는 응용시스템 경로에 생성됨.
		createDestinationDataFile(abapName, connectProperties);
		
		return abapName;
	}
*/

	/** 
	 * sap 연결파일 생성
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
