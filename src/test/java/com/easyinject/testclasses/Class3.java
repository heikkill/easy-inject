package com.easyinject.testclasses;

import javax.inject.Inject;
import javax.inject.Named;

public class Class3 implements Interface2 {

	@Inject
	@Named("p")
	private ProducedClass producedClass;
	
	public ProducedClass getProducedClass() {
		return producedClass;
	}
}
