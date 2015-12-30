package com.easyinject.testclasses;

import javax.inject.Inject;

public class EntryPointHost {

	@Inject
	private EntryPointClass entryPointClass;
	
	@Inject
	private Class2 class2;

	public EntryPointClass getEntryPointClass() {
		return entryPointClass;
	}

	public Class2 getClass2() {
		return class2;
	}
}
