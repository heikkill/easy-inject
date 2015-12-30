package com.easyinject.testclasses;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.easyinject.EasyInjectAnnotations.Startup;

@Startup
@Singleton
public class StartupClass1 {

	@Inject
	private Class2 class2;
	
	@Inject
	private StartupClass2 startupClass2;
	
	private boolean initCalled = false;
	
	@PostConstruct
	private void init() {
		initCalled = true;
	}

	public Class2 getClass2() {
		return class2;
	}

	public StartupClass2 getStartupClass2() {
		return startupClass2;
	}

	public boolean isInitCalled() {
		return initCalled;
	}
}
