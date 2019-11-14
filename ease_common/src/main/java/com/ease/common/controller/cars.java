package com.ease.common.controller;

import org.apache.camel.json.simple.JsonArray;
import org.apache.camel.json.simple.JsonObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class cars {	
	@RequestMapping(value="/cars/brand/{brand}",method=RequestMethod.GET)
	public @ResponseBody ResponseEntity<Object> getBrandList(@PathVariable String brand, @RequestParam(value="id", required=false, defaultValue="nothing") String id) {
		HttpStatus resultHttpStatus = HttpStatus.OK;
		JsonArray resultArray = new JsonArray();
		JsonObject resultData1 = new JsonObject();
		JsonObject resultData2 = new JsonObject();
		if(brand.equals("volvo")) {			
			resultData1.put("brand", "volvo");
			resultData1.put("kind", "s60");
			resultData1.put("score", "90");
			resultData2.put("brand", "volvo");
			resultData2.put("kind", "s90");
			resultData2.put("score", "80");
		}else if(brand.equals("hundai")) {
			resultData1.put("brand", "hundai");
			resultData1.put("kind", "i30");			
			resultData1.put("score", "85");
			resultData2.put("brand", "hundai");
			resultData2.put("kind", "i40");
			resultData2.put("score", "85");
		}
		
		resultArray.add(resultData1);
		resultArray.add(resultData2);
		
		System.out.println("id : "+id);
		ResponseEntity<Object> resultEntity = new ResponseEntity<Object>(resultArray, resultHttpStatus);
		
		return resultEntity;		
	}
		
	@RequestMapping(value="/cars/brand/{brand}/name/{name}",method=RequestMethod.GET)
	public @ResponseBody ResponseEntity<Object> getbrandCar(@PathVariable String brand, @PathVariable String name, @RequestParam(value="id", required=false, defaultValue="nothing") String id) {
		HttpStatus resultHttpStatus = HttpStatus.OK;
		JsonArray resultArray = new JsonArray();
		JsonObject resultData1 = new JsonObject();
		if(brand.equals("volvo")) {
			if(name.equals("s60")) {
				resultData1.put("brand", "volvo");
				resultData1.put("kind", "s60r가나아");
				resultData1.put("score", "90");
			}else if(name.equals("s90")) {
				resultData1.put("brand", "volvo");
				resultData1.put("kind", "s90");
				resultData1.put("score", "80");
			}			
		}else if(brand.equals("hundai")) {
			if(name.equals("i30")) {
				resultData1.put("brand", "hundai");
				resultData1.put("kind", "i30");			
				resultData1.put("score", "85");
			}else if(name.equals("i40")) {
				resultData1.put("brand", "hundai");
				resultData1.put("kind", "i40");
				resultData1.put("score", "85");
			}	
		}
		
		resultArray.add(resultData1);
		
		System.out.println("id : "+id);
		ResponseEntity<Object> resultEntity = new ResponseEntity<Object>(resultArray, resultHttpStatus);
		
		return resultEntity;		
	}
	
	
	@RequestMapping(value="/cars",method=RequestMethod.POST)
	public JsonArray insertCars() {
		JsonArray resultArray = new JsonArray();
		JsonObject resultData1 = new JsonObject();
		resultData1.put("resultCode", "00");
		resultData1.put("resultMessage", "SUCCESS");
		
		resultArray.add(resultData1);
		return resultArray;
	}

}
