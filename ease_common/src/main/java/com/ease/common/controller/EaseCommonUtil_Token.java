package com.ease.common.controller;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

//import net.sf.ehcache.CacheManager;

/**
 * common java utility
 * v0.1
 * @author ease
 */
public class EaseCommonUtil_Token {
	
//	@Autowired
//	CacheManager cacheManager;
	
	//토큰 생성
	//우선 uuid만 만들어서 넘기고 변경사항에 대해 더 고민해야함
	public String createToken(){
		//하이픈(-)을 제외한 32자리
		return UUID.randomUUID().toString().replaceAll("-", "");
	}
	
	
}
