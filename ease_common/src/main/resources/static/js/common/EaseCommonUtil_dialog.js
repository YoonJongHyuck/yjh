/**
 * common javaScript utility
 * v0.1
 * @author ease
 *
 */


﻿define([ "jquery"], function($){


/**
 * 문자열의 UTF8 byte 길이를 반환한다.
 * @return 문자열의 UTF8 byte 길이
 */
window.isNull = function(str) {
	if ( str == null || str == "" ) {
		return true;
	}else{
		return false;
	}
};

/**
 * 필수 입력항목 미 입력시 해당 항목으로 포커스 이동
 * @return
 */
window.cmnExecCallBack = function(param) {
	if ( $.type(param) === 'object' ) {
		param.focus();
	}
};
    
});
/**
 * 공통 alert창
 * @param  text   화면에 보여줄 text
 **/
function cmmOpenAlert(text,widthSize)
{
	var widthSizeReal = 400;
	if(!isNull(widthSize)){
		widthSizeReal = widthSize;
	}
	
	$('#cmmOpenAlertDialog').remove();
	var divStr =  '<div class="pop_contents">'
	+ 	'<div id="cmmOpenAlertDialog" style="display: none;" title="알림">'
	+ 		'<table>'
	+ 			'<colgroup>'
	+ 				'<col width="*">'
	+ 			'</colgroup>'
	+ 			'<tr>'
	+ 				'<th style="text-align: left;">'
	+ 				text
	+ 				'</th>'
	+ 			'</tr>'
	+ 		'</table>'
	+ 		'<span id="input_cmmOpenAlertDialog"/>'
	+ '</div>'
	+ '</div>';// cust_info_connection template.
	$(divStr).appendTo('body');
	$("#cmmOpenAlertDialog").dialog({
		resizable: false,
		width:widthSizeReal,
		modal: true,
		create: function( event, ui ) {
			$(this).parents('.ui-dialog').addClass('dialogBorder');
			$(this).siblings('.ui-dialog-titlebar').attr("style","background: #029add;border:#029add");
			$(this).siblings('.ui-dialog-titlebar').find('.ui-dialog-title').attr("style","color: #ffffff;font-family: NanumGothicB;font-size: 14px;");
			$(this).siblings('.ui-dialog-buttonpane').attr("style","text-align: left;border-width: 1px 0 0 0;background-image: none;margin-top: .5em;padding:0;");
			$(this).siblings('.ui-dialog-buttonpane').find('.ui-dialog-buttonset').attr("style","float:none;text-align:center;");
			$(this).siblings('.ui-dialog-buttonpane').find('.ui-dialog-buttonset').find('.ui-button-text').attr("class","btn_control")
			.append("<a href='javascript:;' id='cmmOpenAlertButton' style='display: inline-block;padding: 2px 10px 3px 10px;color: #ffffff;font-size: 12px;font-family: NanumGothicB;font-weight: bold;word-spacing: -1px;'>확인</a>");
		},
		buttons: {
			'' : function() {
				$(this).dialog("close");
			}
		}
	});
	setTimeout( "$('#cmmOpenAlertButton').focus()", 100 );
//	$("#cmmOpenAlertButton").focus();
//	$("#input_cmmOpenAlertDialog").focus();
//	$("#cmmOpenAlertDialog").trigger("click");
};

/**
 * 공통 confirm창
 *
 * @param    text:메세지, callback:콜백함수명, 파라메터, widthSize
 **/
function cmmOpenConfirm(text,callback,param,widthSize)
{
	var widthSizeReal = 400;
	if(!isNull(widthSize)){
		widthSizeReal = widthSize;
	}
	$('#cmmOpenConfirmDialog').remove();
	var divStr =  '<div id="cmmOpenConfirmDialog" style="display: none;" title="알림">'
		+ 		'<table>'
		+ 			'<colgroup>'
		+ 				'<col width="*">'
		+ 			'</colgroup>'
		+ 			'<tr>'
		+ 				'<th style="text-align: left;">'
		+ 				text
		+ 				'</th>'
		+ 			'</tr>'
		+ 		'</table>'
		+ '</div>';// cust_info_connection template.
	$(divStr).appendTo('body');
	$("#cmmOpenConfirmDialog").dialog({
		resizable: false,
		width:widthSizeReal,
		modal: true,
		create: function( event, ui ) {
			$(this).parents('.ui-dialog').addClass('dialogBorder');
			$(this).siblings('.ui-dialog-titlebar').attr("style","background: #029add;border:#029add");
			$(this).siblings('.ui-dialog-titlebar').find('.ui-dialog-title').attr("style","color: #ffffff;font-family: NanumGothicB;font-size: 14px;");
			$(this).siblings('.ui-dialog-buttonpane').attr("style","text-align: left;border-width: 1px 0 0 0;background-image: none;margin-top: .5em;padding:0;");
			$(this).siblings('.ui-dialog-buttonpane').find('.ui-dialog-buttonset').attr("style","float:none;text-align:center;");
			$(this).siblings('.ui-dialog-buttonpane').find('.ui-dialog-buttonset').find('.ui-button-text').attr("class","btn_control");
			$(this).siblings('.ui-dialog-buttonpane').find('.ui-dialog-buttonset').find('.btn_control').each(function(index, element){
				var str;
				if(index==0){
					str = '확인';
				}else if(index==1){
					str = '취소';
				}
				$(this).append("<a href='javascript:;' style='display: inline-block;padding: 2px 10px 3px 10px;color: #ffffff;font-size: 12px;font-family: NanumGothicB;font-weight: bold;word-spacing: -1px;'>"+str+"</a>");
			});
		},
		buttons: {
			'' : function(event) {
				$(this).dialog("close");
				callback(param);
				
			}
			,' ' : function(event) {
				$(this).dialog("close");
			}
		}
	});
//	$("#cmmOpenConfirmDialog").focus();
//	$("#cmmOpenConfirmDialog").trigger("click");
};

/**
 * 공통 alert창(리턴)
 * @param    text:메세지, callbackFn:콜백함수명, data:데이터, widthSize:가로 사이즈
 **/
function cmmOpenAlertReturn(text,callbackFn,data,widthSize)
{
	var widthSizeReal = 400;
	if(!isNull(widthSize)){
		widthSizeReal = widthSize;
	}
	
	$('#cmmOpenAlertDialog').remove();
	var divStr =  '<div class="pop_contents">'
		+ 	'<div id="cmmOpenAlertDialog" style="display: none;" title="알림">'
		+ 		'<table>'
		+ 			'<colgroup>'
		+ 				'<col width="*">'
		+ 			'</colgroup>'
		+ 			'<tr>'
		+ 				'<th style="text-align: left;">'
		+ 				text
		+ 				'</th>'
		+ 			'</tr>'
		+ 		'</table>'
		+ 		'<span id="input_cmmOpenAlertDialog"/>'
		+ '</div>'
		+ '</div>';// cust_info_connection template.
	$(divStr).appendTo('body');
	$("#cmmOpenAlertDialog").dialog({
		resizable: false,
		width:widthSizeReal,
		modal: true,
		create: function( event, ui ) {
			$(this).parents('.ui-dialog').addClass('dialogBorder');
			$(this).siblings('.ui-dialog-titlebar').attr("style","background: #029add;border:#029add");
			$(this).siblings('.ui-dialog-titlebar').find('.ui-dialog-title').attr("style","color: #ffffff;font-family: NanumGothicB;font-size: 14px;");
			$(this).siblings('.ui-dialog-buttonpane').attr("style","text-align: left;border-width: 1px 0 0 0;background-image: none;margin-top: .5em;padding:0;");
			$(this).siblings('.ui-dialog-buttonpane').find('.ui-dialog-buttonset').attr("style","float:none;text-align:center;");
			$(this).siblings('.ui-dialog-buttonpane').find('.ui-dialog-buttonset').find('.ui-button-text').attr("class","btn_control")
			.append("<a href='javascript:;' id='cmmOpenAlertButton' style='display: inline-block;padding: 2px 10px 3px 10px;color: #ffffff;font-size: 12px;font-family: NanumGothicB;font-weight: bold;word-spacing: -1px;'>확인</a>");
		},
		buttons: {
			'' : function() {
				$(this).dialog("close");
				callbackFn(data);
			}
		}
	});
	setTimeout( "$('#cmmOpenAlertButton').focus()", 100 );
//	$("#cmmOpenAlertButton").focus();
};

/**
 * 공통 confirm창(리턴함수)
 *
 * @param    text:메세지, callback:콜백함수명, 파라메터, widthSize
 **/
function cmmOpenConfirmReturn(text,callback,param,widthSize)
{
	var widthSizeReal = 400;
	if(!isNull(widthSize)){
		widthSizeReal = widthSize;
	}
	$('#cmmOpenConfirmDialog').remove();
	var divStr =  '<div id="cmmOpenConfirmDialog" style="display: none;" title="알림">'
		+ 		'<table>'
		+ 			'<colgroup>'
		+ 				'<col width="*">'
		+ 			'</colgroup>'
		+ 			'<tr>'
		+ 				'<th style="text-align: left;">'
		+ 				text
		+ 				'</th>'
		+ 			'</tr>'
		+ 		'</table>'
		+ '</div>';// cust_info_connection template.
	$(divStr).appendTo('body');
	$("#cmmOpenConfirmDialog").dialog({
		resizable: false,
		width:widthSizeReal,
		modal: true,
		create: function( event, ui ) {
			$(this).parents('.ui-dialog').addClass('dialogBorder');
			$(this).siblings('.ui-dialog-titlebar').attr("style","background: #029add;border:#029add");
			$(this).siblings('.ui-dialog-titlebar').find('.ui-dialog-title').attr("style","color: #ffffff;font-family: NanumGothicB;font-size: 14px;");
			$(this).siblings('.ui-dialog-buttonpane').attr("style","text-align: left;border-width: 1px 0 0 0;background-image: none;margin-top: .5em;padding:0;");
			$(this).siblings('.ui-dialog-buttonpane').find('.ui-dialog-buttonset').attr("style","float:none;text-align:center;");
			$(this).siblings('.ui-dialog-buttonpane').find('.ui-dialog-buttonset').find('.ui-button-text').attr("class","btn_control");
			$(this).siblings('.ui-dialog-buttonpane').find('.ui-dialog-buttonset').find('.btn_control').each(function(index, element){
				var str;
				if(index==0){
					str = '확인';
				}else if(index==1){
					str = '취소';
				}
				$(this).append("<a href='javascript:;' style='display: inline-block;padding: 2px 10px 3px 10px;color: #ffffff;font-size: 12px;font-family: NanumGothicB;font-weight: bold;word-spacing: -1px;'>"+str+"</a>");
			});
		},
		buttons: {
			'' : function(event) {
				$(this).dialog("close");
				callback(param,true);
				
			}
		,' ' : function(event) {
			$(this).dialog("close");
			callback(param,false);
		}
		}
	});
//	$("#cmmOpenConfirmDialog").focus();
//	$("#cmmOpenConfirmDialog").trigger("click");
};



/**
 * 공통 confirm창(text입력창)
 * 추가 | 20170522 | yjh
 * @param    text:메세지, callback:콜백함수명, 파라메터
 **/
function cmmOpenConfirmWithText(text,callback,param)
{
	var widthSizeReal = 400;

	$('#cmmOpenConfirmDialog').remove();
	var divStr =  '<div id="cmmOpenConfirmDialog"  class="pop_wrap_in">'
		+		'<div class="subtitle-box" style="margin:0;">'
		+			'<h2 class="subtitle">'
		+			text
		+			'</h2>'
		+		'</div>'	
//		+ 		'<table class="tbl_Form" summary="사유">'
//		+ 			'<colgroup><col width="100px"/><col width="*"/></colgroup>'
//		+			'<tr>'		
//		+				'<th scope="row"><label for="">상세내용</label></th>'
//		+				'<td>' 
		+					'<textarea id="cmmOpenConfirmDialogInput" rows="10" class="size-98p" placeholder="(필수)변경 사유를 입력하세요." autofocus required/>'
//		+				'</td>'
//		+			'</tr>'
//		+ 		'</table>'
		+ '</div>';// cust_info_connection template.
	$(divStr).appendTo('body');
	$("#cmmOpenConfirmDialog").dialog({
		resizable: false,
		width:widthSizeReal,
		modal: true,
		create: function( event, ui ) {
			$(this).parents('.ui-dialog').addClass('dialogBorder');
			$(this).siblings('.ui-dialog-titlebar').attr("style","background: #029add;border:#029add");
			$(this).siblings('.ui-dialog-titlebar').find('.ui-dialog-title').attr("style","color: #ffffff;font-family: NanumGothicB;font-size: 14px;");
			$(this).siblings('.ui-dialog-buttonpane').attr("style","text-align: left;border-width: 1px 0 0 0;background-image: none;margin-top: .5em;padding:0;");
			$(this).siblings('.ui-dialog-buttonpane').find('.ui-dialog-buttonset').attr("style","float:none;text-align:center;");
			$(this).siblings('.ui-dialog-buttonpane').find('.ui-dialog-buttonset').find('.ui-button-text').attr("class","btn_control");
			$(this).siblings('.ui-dialog-buttonpane').find('.ui-dialog-buttonset').find('.btn_control').each(function(index, element){
				var str;
				if(index==0){
					str = '확인';
				}else if(index==1){
					str = '취소';
				}
				$(this).append("<a href='javascript:;' style='display: inline-block;padding: 2px 10px 3px 10px;color: #ffffff;font-size: 12px;font-family: NanumGothicB;font-weight: bold;word-spacing: -1px;'>"+str+"</a>");
			});
		},
		buttons: {
			'' : function(event) {
				inputText = $("#cmmOpenConfirmDialogInput").val();
				if(inputText == ""){
					cmmOpenAlert("변경 사유를 입력하세요.");
				}else{
					$(this).dialog("close");
					callback(param,inputText,true);
				}
			}
			,' ' : function(event) {
				$(this).dialog("close");
			}
		}
	});
//	$("#cmmOpenConfirmDialog").focus();
//	$("#cmmOpenConfirmDialog").trigger("click");
};



/**
 * 공통 confirm창(환불 등록)
 * @param    text:메세지, bankInfo:selectBox에 담을 은행 정보, callback:콜백함수명, param:파라메터
 **/
function cmmOpenConfirmForRefund(text, bankInfo, callback, param)
{
	var widthSizeReal = 400;
	
	var selectStr = '<option value="">은행선택</option>';
	
	for(index in bankInfo){
		selectStr += '<option value="' + bankInfo[index].BNK_CD + '">'
				   + bankInfo[index].BNK_NM
				   + '</option>';
	}
	
	$('#cmmOpenConfirmDialog').remove();
	var divStr =  '<div id="cmmOpenConfirmDialog"  class="pop_wrap_in">'
		+		'<div class="subtitle-box" style="margin:0;">'
		+			'<h2 class="subtitle">'
		+			text
		+			'</h2>'
		+		'</div>'
		+		'<div style="margin-top: 4px;">'
		+			'<select id="selectBank">'
		+				selectStr
		+			'</select>'
		+			'<input type="text" id="inputNm" value="" placeholder="예금주명을 입력하세요."/>'
		+		'</div>'
		+		'<div style="margin-top: 4px;">'
		+			'<input type="text" id="inputAcc" placeholder="\'-\' 없이 계좌번호를 입력하세요."/>'
		+		'</div>'
		+		'<div style="margin: 4px 0px 4px 0px;">'
		+			'<span>'
		+				'※ 본인명의 계좌로만 가능하니, 꼭!! 확인하세요.'
		+			'</span>'
		+		'</div>'
		+		'<textarea id="cmmOpenConfirmDialogInput" rows="10" class="size-98p" placeholder="(필수)변경 사유를 입력하세요." style="margin-top: 2px;" autofocus required/>'
		+ '</div>';// cust_info_connection template.
	$(divStr).appendTo('body');
	$("#cmmOpenConfirmDialog").dialog({
		resizable: false,
		width:widthSizeReal,
		modal: true,
		create: function( event, ui ) {
			$(this).parents('.ui-dialog').addClass('dialogBorder');
			$(this).siblings('.ui-dialog-titlebar').attr("style","background: #029add;border:#029add");
			$(this).siblings('.ui-dialog-titlebar').find('.ui-dialog-title').attr("style","color: #ffffff;font-family: NanumGothicB;font-size: 14px;");
			$(this).siblings('.ui-dialog-buttonpane').attr("style","text-align: left;border-width: 1px 0 0 0;background-image: none;margin-top: .5em;padding:0;");
			$(this).siblings('.ui-dialog-buttonpane').find('.ui-dialog-buttonset').attr("style","float:none;text-align:center;");
			$(this).siblings('.ui-dialog-buttonpane').find('.ui-dialog-buttonset').find('.ui-button-text').attr("class","btn_control");
			$(this).siblings('.ui-dialog-buttonpane').find('.ui-dialog-buttonset').find('.btn_control').each(function(index, element){
				var str;
				if(index==0){
					str = '확인';
				}else if(index==1){
					str = '취소';
				}
				$(this).append("<a href='javascript:;' style='display: inline-block;padding: 2px 10px 3px 10px;color: #ffffff;font-size: 12px;font-family: NanumGothicB;font-weight: bold;word-spacing: -1px;'>"+str+"</a>");
			});
			
			$(this).find('#selectBank').css('padding', '1px 3px 2px 3px');
			$(this).find('#selectBank').css('border', 'solid 1px #cccccc');
			$(this).find('#selectBank').css('font-size', '12px');
			$(this).find('#selectBank').css('border-radius', '2px');
			$(this).find('#selectBank').css('width', '80px');
			$(this).find('#selectBank').css('margin-right', '10px');
			
			$(this).find('#inputNm').css('padding', '1px 3px 2px 3px');
			$(this).find('#inputNm').css('border', 'solid 1px #cccccc');
			$(this).find('#inputNm').css('font-size', '12px');
			$(this).find('#inputNm').css('width', '150px');
			
			$(this).find('#inputAcc').css('padding', '1px 3px 2px 3px');
			$(this).find('#inputAcc').css('border', 'solid 1px #cccccc');
			$(this).find('#inputAcc').css('font-size', '12px');
			$(this).find('#inputAcc').css('width', '300px');
		},
		buttons: {
			'' : function(event) {
				inputText = $("#cmmOpenConfirmDialogInput").val();
				if($('#selectBank').val() == ''){
					cmmOpenAlert("은행을 선택하세요.");
				}else if($('#inputNm').val() == ''){
					cmmOpenAlert("예금주명을 입력하세요.");
				}else if($('#inputAcc').val() == ''){
					cmmOpenAlert("계좌번호를 입력하세요.");
				}else if(inputText == ""){
					cmmOpenAlert("변경 사유를 입력하세요.");
				}else{
					param.BNK_CD = $('#selectBank').val();
					param.ACCT_NO = $('#inputAcc').val();
					param.OWAC_NM = $('#inputNm').val();
					
					$(this).dialog("close");
					callback(param,inputText,true);
				}
			}
		,' ' : function(event) {
			$(this).dialog("close");
		}
		}
	});
	
//	$("#cmmOpenConfirmDialog").focus();
//	$("#cmmOpenConfirmDialog").trigger("click");
};
