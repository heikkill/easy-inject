package com.easyinject.testclasses;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import com.easyinject.EasyInjectAnnotations.EntryPoint;
import com.easyinject.testclasses.Class2.Class4;

public class EntryPointClass {

	@Inject
	private List<Interface1> interface1List;
	
	@Inject
	private Class1 class1;
	
	@Inject
	private Class2 class2;
	
	@Inject
	private Class4 class4;
	
	@Inject
	private Interface1 interface1;
	
	@Inject
	@Named("p")
	private ProducedClass producedClass;
	
	private boolean initCalled = false;
	
	@EntryPoint
	private void init() {
		initCalled = true;
	}
	
	public Class1 getClass1() {
		return class1;
	}

	public Class2 getClass2() {
		return class2;
	}

	public Interface1 getInterface1() {
		return interface1;
	}

	public List<Interface1> getInterface1List() {
		return interface1List;
	}

	public boolean isInitCalled() {
		return initCalled;
	}

	public Class4 getClass4() {
		return class4;
	}

	public ProducedClass getProducedClass() {
		return producedClass;
	}
}
