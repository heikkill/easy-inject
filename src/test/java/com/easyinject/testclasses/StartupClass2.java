package com.easyinject.testclasses;

import javax.inject.Inject;

import com.easyinject.EasyInjectAnnotations.Startup;

@Startup
public class StartupClass2 {

	@Inject
	private Class2 class2;

	public Class2 getClass2() {
		return class2;
	}
}
