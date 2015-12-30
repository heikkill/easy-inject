package com.easyinject.testclasses;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class Class6 {

	@Inject
	private Class1 class1;
	
	@Inject
	private StartupClass1 startupClass1;
	
	@Inject
	private StartupClass2 startupClass2;
	
	@Inject
	private EntryPointHost entryPointHost;

	public Class1 getClass1() {
		return class1;
	}

	public EntryPointHost getEntryPointHost() {
		return entryPointHost;
	}

	public StartupClass1 getStartupClass1() {
		return startupClass1;
	}

	public StartupClass2 getStartupClass2() {
		return startupClass2;
	}
}
