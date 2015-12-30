package com.easyinject.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.easyinject.EasyInject;

public class EasyInjectMain {
	
	private static Logger logger = LoggerFactory.getLogger(EasyInjectMain.class);

	public static void main(String[] args) {
		try {
			new EasyInject();
		} catch (Exception e) {
			logger.error("Unable to start EasyInject. Reason: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
