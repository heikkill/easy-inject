package com.easyinject.testclasses;

import javax.annotation.PostConstruct;



public class Class2 implements Interface1 {

	public class Class4 {
		
	}
	
	private boolean initCalled = false;
	
	@PostConstruct
	private void init() {
		initCalled = true;
	}

	public boolean isInitCalled() {
		return initCalled;
	}
}
