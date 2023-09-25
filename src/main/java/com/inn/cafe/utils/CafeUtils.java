package com.inn.cafe.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;


public class CafeUtils {

	private CafeUtils() {
		
	}
	
	public static ResponseEntity<String> getResponseEntity(String responseMessage, HttpStatus httpStatus){
		return new ResponseEntity<String>("{\"message\":\""+responseMessage+"\"}",httpStatus);
	}
	
	public static String getUUID() {
		Date date = new Date();
		long time = date.getTime();
		return "BILL-" + time;
	}
	
	public static JSONArray getJsonArrayFromString(String data) throws JSONException {
		JSONArray jsonArray = new JSONArray(data);
		return jsonArray;
	}
	
	 public static Map<String, Object> getMapFromJson(String data) {
	        if (StringUtils.isNotEmpty(data)) {
	            Type type = new TypeToken<Map<String, Object>>() {}.getType();
	            return new Gson().fromJson(data, type);
	        }
	        return new HashMap<>();
	    }
	 
	 public static Boolean isFileExist(String path) {
		 try {
			 File file = new File(path);
			 return (file != null && file.exists()) ? Boolean.TRUE : Boolean.FALSE;
		 } catch(Exception e) {
			 e.printStackTrace();
		 }
		 return false;
	 }
	
//	public static Map<String,Object> getMapFromJson(String data){
//		if(!StringUtils.isNotBlank(data))
//			return new Gson().fromJson(data, new TypeToken<Map<String, Object>>(){
//			}.getType());
//		return new HashMap<>();
//	}
}
