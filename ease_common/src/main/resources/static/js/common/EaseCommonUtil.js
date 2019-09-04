/**
 * common javaScript utility
 * v0.1
 * @need jQuery v1.7 over
 * @author ease
 *
 */

/**
 * get label
 * @param labels, id
 * @returns
 */
function get_label(labels, id){
    var el = null;
    var text = '';
    for(i=0; i<labels.length; i++) {
        if(id == labels[i].htmlFor) {
            el = labels[i];
            break;
        }
    }

    if(el != null) {
        text = el.innerHTML;
    }
    return text;
}


/**
 * check input data with dataset
 * checktype : nullCheck (null check)
 *             etc
 * @param formId
 * @returns
 * @exam <input name="TRSC_TYP_CD" type="text" data-checktype="nullCheck" />
 */
function validationForm(formId){
	
	var trueFalse = true;
	var $targetForm = $('#'+formId);
	//there must be label tag what have same ID with target input 
	var labels = document.getElementsByTagName('label');
	
	$targetForm.find(':input').each(function() {
		if(this.dataset.checktype == "nullCheck"){
			checkData = this.value;
			if(checkData == null || checkData == ""){
				var label = get_label(labels, this.id);				    					
				cmmOpenAlert(label+"는 필수값 입니다.", this.focus());		
				trueFalse = false;
				return false;
			}
		}
  	    
	});
	
	if(trueFalse == false){
		return false;
	}
	
	return true;		
};


/**
 * 조회기간 셀렉트박스 셋팅
 * 고정된 span에 셀렉트박스를 셋팅하고, 선택 시 조회 시작일 / 마지막일을 변경한다.
 * @param selectBoxType 1: 기존 사용하던 baseterm, 2: 추가된 패턴
 * @param strDateId
 * @param endDateId
 * @returns
 */
function setDateTermSelectBox(selectBoxType, strDateId, endDateId){
	var selectBoxHtml = "";
	var optionJsonData = getDateTermSelectBoxData(selectBoxType);
	
	selectBoxHtml += "<select id='dateTermSelectBox' name='dateTermSelectBox' class='searchDb' onchange='changeDateTermSelectBox(\""+strDateId+"\",\""+endDateId+"\")'>";
	selectBoxHtml += "<option value=''>"+convertLanguage('기간선택')+"</option>";
	
	for (i=0 ; i < optionJsonData.length ; i++){
		selectBoxHtml += "<option value='" + optionJsonData[i].ID + "'>" + optionJsonData[i].Name + "</option>";
	}
		
	selectBoxHtml += "</select>";
	
	$("#dateTermSelectBoxSpan").html(selectBoxHtml);
}


/**
 * 조회기간 셀렉트박스 이벤트
 * @param strDateId
 * @param endDateId
 * @returns
 */
function changeDateTermSelectBox(strDateId, endDateId){
	var termType = $("#dateTermSelectBox").val();
	if(termType){
		var selectedTermDate = getTermDate(termType);		
		$("#"+strDateId).datepicker("setDate",selectedTermDate.strDate);
		$("#"+endDateId).datepicker("setDate",selectedTermDate.endDate);
	}
}


/**
 * selectBoxType에 따른 데이터 셋팅
 * @param selectBoxType
 * @returns
 */
function getDateTermSelectBoxData(selectBoxType){
	var dateTermSelectBoxData = "";
	
	if(selectBoxType == '2'){
		dateTermSelectBoxData = [
			{"ID": 0, "Name": convertLanguage("오늘")},
			{"ID": 7, "Name": convertLanguage("어제~내일")},
			{"ID": 5, "Name": convertLanguage("이번주")},
			{"ID": 8, "Name": convertLanguage("지난한주")},
			{"ID": 6, "Name": convertLanguage("지난주")},
			{"ID": 1, "Name": convertLanguage("이번달")},
			{"ID": 9, "Name": convertLanguage("이전한달")},
			{"ID": 2, "Name": convertLanguage("지난달")},
			{"ID": 3, "Name": convertLanguage("이번분기")},
			{"ID": 4, "Name": convertLanguage("지난분기")}
		];
	}else{	//기존 사용하던 baseterm
		dateTermSelectBoxData = [
			{"ID": 0, "Name": convertLanguage("오늘")},
			{"ID": 1, "Name": convertLanguage("이번달")},
			{"ID": 2, "Name": convertLanguage("지난달")},
			{"ID": 3, "Name": convertLanguage("이번분기")},
			{"ID": 4, "Name": convertLanguage("지난분기")},
			{"ID": 5, "Name": convertLanguage("이번주")},
			{"ID": 6, "Name": convertLanguage("지난주")},
			{"ID": 7, "Name": convertLanguage("어제~내일")}
		];
	}
	
	return dateTermSelectBoxData;
}


/**
 *  조회기간 타입별 날짜 셋팅
 * @param termType
 * @returns resultTermDate(strDate, endDate)
 */
function getTermDate(termType){
	var resultTermDate = {strDate : "", endDate : "" };
	var resultTermDateTemp = "";
	var today = new Date();
//	today = new Date(2016, 5-1, 28);		//for test
//	today = new Date(1981, 11-1, 24);		//for test
	var strDate = "";
	var endDate = "";
	var quarter = "";
	
	if(termType == '0'){		//오늘
		strDate = today;
		endDate = today;
	}else if(termType == '1'){	//이번달
		strDate = new Date(today.getFullYear(), today.getMonth(), 1);		//현재 달의 1일
		endDate = new Date(today.getFullYear(), today.getMonth()+1, 0);		//현재 달의 마지막날
	}else if(termType == '2'){	//지난달
		strDate = new Date(today.getFullYear(), today.getMonth()-1, 1);		//지난 달의 1일
		endDate = new Date(today.getFullYear(), today.getMonth(), 0);		//지난 달의 마지막날
	}else if(termType == '3'){	//이번분기		
		resultTermDateTemp = getQuarterTerm(today);
		strDate = resultTermDateTemp.strDate;
		endDate = resultTermDateTemp.endDate;
	}else if(termType == '4'){	//지난분기
		var tempDate = new Date(today.getFullYear(), today.getMonth()-3, today.getDate());  //3달 전
		resultTermDateTemp = getQuarterTerm(tempDate);
		strDate = resultTermDateTemp.strDate;
		endDate = resultTermDateTemp.endDate;
	}else if(termType == '5'){	//이번주		
		resultTermDateTemp = getWeekTerm(today);		
		strDate = resultTermDateTemp.strDate;
		endDate = resultTermDateTemp.endDate;
	}else if(termType == '6'){	//지난주
		var lastWeek = new Date(Date.parse(today) - 7 * 1000 * 60 * 60 * 24);
		resultTermDateTemp = getWeekTerm(lastWeek);
		strDate = resultTermDateTemp.strDate;
		endDate = resultTermDateTemp.endDate;
	}else if(termType == '7'){	//어제~내일
		strDate = new Date(Date.parse(today) - 1000 * 60 * 60 * 24);
		endDate = new Date(Date.parse(today) + 1000 * 60 * 60 * 24);
	}else if(termType == '8'){	//이전한주
		strDate = new Date(Date.parse(today) - 7 * 1000 * 60 * 60 * 24);
		endDate = today;
	}else if(termType == '9'){	//이전한달
		strDate = new Date(today.getFullYear(), today.getMonth()-1,  today.getDate());	//1달전
		endDate = today;
		
	}
	
	resultTermDate.strDate = strDate;
	resultTermDate.endDate = endDate;
	
	return resultTermDate;
}


/**
 * get week date
 * @param today
 * @returns resultTermDate(strDate, endDate)
 */
function getWeekTerm(today){
	var resultTermDate = {strDate : "", endDate : "" };
	var nowDayOfWeek = today.getDay(); 
	strDate = new Date(today.getFullYear(), today.getMonth(), today.getDate() - nowDayOfWeek); 
	endDate = new Date(today.getFullYear(), today.getMonth(), today.getDate() + (6 - nowDayOfWeek));
	resultTermDate.strDate = strDate;
	resultTermDate.endDate = endDate;
	
	return resultTermDate;
}


/**
 * get quater date
 * @param today
 * @returns resultTermDate(strDate, endDate)
 */
function getQuarterTerm(today){
	var resultTermDate = {strDate : "", endDate : "" };
	var quarter = Math.ceil( (today.getMonth()+1) / 3 );
	var startMonth = 0;
	var endMonth = 0;
	
	switch(quarter){
		case 1 : startMonth = 0; endMonth = 3; break;  
		case 2 : startMonth = 3; endMonth = 6; break;
		case 3 : startMonth = 6; endMonth = 9; break;
		case 4 : startMonth = 9; endMonth = 12; break;
	}
	
	strDate = new Date(today.getFullYear(), startMonth, 1); 
	endDate = new Date(today.getFullYear(), endMonth, 0);
	resultTermDate.strDate = strDate;
	resultTermDate.endDate = endDate;
	
	return resultTermDate;
}

/**
 * hover event
 * @param targetEl
 * @returns
 */
function checkHoverEvent(targetEl){
	if(targetEl.value == "55555"){
		var $preTargetEl = $(targetEl).prev();
		$preTargetEl.css({"color" : "#315ac7", "text-decoration" : "underline", "cursor" : "pointer"});
		
		$preTargetEl.hover(function(e){
			viewPopup("on", e, "popupDiv");
		}, function(e){
			viewPopup("off", e, "popupDiv");
		});
		
	}else{
		console.log("NO");
	}
}


/**
 * hover popup
 * @param status
 * @param e
 * @param popupDiv : 
 * @returns
 */
function viewPopup(status, e, popupDiv){
	if(status == "on"){		
		var divTop = e.clientY - 50;	//상단 좌표 위치 안맞을시 e.pageY 
		var divLeft = e.clientX;		//좌측 좌표 위치 안맞을시 e.pageX 
		var serial = $(this).attr("serial"); 
		var idx = $(this).attr("idx");		
		var text = '<div style="padding : 1ex">';
			text += '<span style="cursor:pointer;font-size:1.0em"> popup 완료 </span>';
			text += '</div>';
			
		$('#'+popupDiv).empty().append(text); 
		$('#'+popupDiv).css({ "top": divTop ,"left": divLeft , "position": "absolute", "background-color": "#cddbff", "border": "1px solid #a5b8ec"}).show();		
	}else if(status == "off"){
		$('#'+popupDiv).hide();
	}
}





