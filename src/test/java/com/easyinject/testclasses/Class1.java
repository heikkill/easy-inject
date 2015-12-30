package com.easyinject.testclasses;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class Class1 {

	@Inject
	private Class2 class2;
	
	@Inject
	private Class6 class6;
	
	@Inject
	private Set<Interface2> interface2Set;

	public Class2 getClass2() {
		return class2;
	}
	
	public Class6 getClass6() {
		return class6;
	}

	public Set<Interface2> getInterface2Set() {
		return interface2Set;
	}
}
