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