package com.easyinject.testclasses;

import javax.inject.Inject;

import com.easyinject.EasyInjectAnnotations.Startup;

@Startup
public class StartupClass3 {
	
	private static int instanceCount = 0;
	private static StartupClass3 instance = null;

	@Inject
	private StartupClass1 startupClass1;
	
	public StartupClass3() {
		instanceCount++;
		instance = this;
	}

	public StartupClass1 getStartupClass1() {
		return startupClass1;
	}

	public static int getInstanceCount() {
		return instanceCount;
	}

	public static StartupClass3 getInstance() {
		return instance;
	}
}
