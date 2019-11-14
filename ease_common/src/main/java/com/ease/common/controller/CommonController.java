package com.ease.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CommonController {
	
	@RequestMapping("/test")
	public String test(Model model) {
		model.addAttribute("value", "55555");
		return "testMain";
	}
	
}
