package com.easyinject.testclasses;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;

public class ProducerClass {
	
	private static List<ProducerClass> instances = new ArrayList<ProducerClass>();
	
	private int produceCalls = 0;
	
	public ProducerClass() {
		instances.add(this);
	}

	@Named("p")
	private ProducedClass produce() {
		produceCalls++;
		return new ProducedClass();
	}

	public int getProduceCalls() {
		return produceCalls;
	}
	
	public static List<ProducerClass> getInstances() {
		return instances;
	}
}
