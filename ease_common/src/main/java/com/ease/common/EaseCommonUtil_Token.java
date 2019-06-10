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
 *1. java UUID �� ����Ͽ� ������ū ����
 *2. EhCache, Memcached ���� ����� ��� ������ token�� Ű��
 *   ������ key (id, ����ȣ ��)�� �����Ѵ�. (�����Ⱓ, �������� , ����Ÿ�̹� �� ���)
 *3. �ش� ��ū�� ���� ��ȿ�� cach�� �����ϴ� ���, �α��� ������ �Ǿ��� ��� �Ǵ��Ѵ�.
 *
 */
public class EaseCommonUtil_Token {
	
	@Autowired
	CacheManager cacheManager;
	
	//��ū ����
	//�켱 uuid�� ���� �ѱ�� ������׿� ���� �� ����ؾ���
	public String createToken(){
		//������(-)�� ������ 32�ڸ�
		return UUID.randomUUID().toString().replaceAll("-", "");
	}
	
	
}
