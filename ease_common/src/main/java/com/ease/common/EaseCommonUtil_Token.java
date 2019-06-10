package com.ease.common;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import net.sf.ehcache.CacheManager;

/**
 * common java utility
 * v0.1
 * @author ease
 *
 *1. java UUID 를 사용하여 유일토큰 생성
 *2. EhCache, Memcached 등을 사용할 경우 생성된 token을 키로
 *   고객계정 key (id, 고객번호 등)를 셋팅한다. (유지기간, 리프레쉬 , 삭제타이밍 등 고려)
 *3. 해당 토큰을 통해 유효한 cach가 존재하는 경우, 로그인 인증이 되었다 라고 판단한다.
 *
 */
public class EaseCommonUtil_Token {
	
	@Autowired
	CacheManager cacheManager;
	
	//토큰 생성
	//우선 uuid만 만들어서 넘기고 변경사항에 대해 더 고민해야함
	public String createToken(){
		//하이픈(-)을 제외한 32자리
		return UUID.randomUUID().toString().replaceAll("-", "");
	}
	
	
}
