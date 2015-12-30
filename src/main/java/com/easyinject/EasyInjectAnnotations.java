package com.easyinject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

public interface EasyInjectAnnotations {

	@Qualifier
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE})
	public @interface EntryPoint {
	}
	
	@Qualifier
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE})
	public @interface Exclude {
	}
	
	@Qualifier
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE})
	public @interface Startup {
	}
}
