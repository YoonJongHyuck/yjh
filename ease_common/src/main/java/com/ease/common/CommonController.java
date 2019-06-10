package com.ease.common;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CommonController {
	
	@RequestMapping("/test")
	public String test() {
		return "hello ease.";
	}
	

}
