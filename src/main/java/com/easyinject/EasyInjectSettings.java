package com.easyinject;

public class EasyInjectSettings {
	
	private boolean everythingSingleton = false;
	private boolean producerSingleton = true;
	private boolean allowMultipleProducers = true;
	private boolean allowMultipleImplementationsOfInjectedInterface = true;
	private boolean allowNullInjections = false;
	
	public EasyInjectSettings() {
	}
	
	public EasyInjectSettings(boolean everythingSingleton,
			boolean producerSingleton, boolean allowMultipleProducers,
			boolean allowMultipleImplementationsOfInjectedInterface,
			boolean allowNullInjections) {
		this.everythingSingleton = everythingSingleton;
		this.producerSingleton = producerSingleton;
		this.allowMultipleProducers = allowMultipleProducers;
		this.allowMultipleImplementationsOfInjectedInterface = allowMultipleImplementationsOfInjectedInterface;
		this.allowNullInjections = allowNullInjections;
	}

	public boolean isEverythingSingleton() {
		return everythingSingleton;
	}

	public boolean isProducerSingleton() {
		return producerSingleton;
	}

	public boolean isAllowMultipleProducers() {
		return allowMultipleProducers;
	}

	public boolean isAllowMultipleImplementationsOfInjectedInterface() {
		return allowMultipleImplementationsOfInjectedInterface;
	}

	public boolean isAllowNullInjections() {
		return allowNullInjections;
	}
}
