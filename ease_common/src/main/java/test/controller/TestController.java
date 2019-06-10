package test.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class TestController {
	
	@RequestMapping("/testMain")
	public String index(Model model) {
		model.addAttribute("name", "ease");
		System.out.print("test");
		return "hi, stranger";		
	}

}
