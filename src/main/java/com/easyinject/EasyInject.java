package com.easyinject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.easyinject.EasyInjectAnnotations.EntryPoint;
import com.easyinject.EasyInjectAnnotations.Exclude;
import com.easyinject.EasyInjectAnnotations.Startup;
import com.easyinject.util.EasyInjectUtils;

public class EasyInject {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private EasyInjectSettings settings;
	private Reflections reflections = new Reflections("", new MethodAnnotationsScanner(),
			new SubTypesScanner(), new FieldAnnotationsScanner(), new TypeAnnotationsScanner());
	private Map<Class<?>, Object> singletonInstances = new HashMap<Class<?>, Object>();
	private List<Object> instancesWithPostConstruct = new ArrayList<Object>();
	private List<Object> instancesWithNullSingletonFields = new ArrayList<Object>();
	private Method entryPointMethod = null;
	private Object entryPointInstance = null;
	private Class<?> entryPointClass = null;
	
	public EasyInject() throws Exception {
		init(findSettings());
	}

	public EasyInject(EasyInjectSettings settings) throws Exception {
		init(settings);
	}
	
	private void init(EasyInjectSettings settings) throws Exception {
		this.settings = settings;
		acquireEntryPoint();
		instantiateStartups();
		handleObjectsWithNullSingletonFields();
		invokePostContructs();
		EasyInjectUtils.invokeMethod(entryPointInstance, entryPointMethod);
	}
	
	private void invokePostContructs() throws Exception {
		for (Object obj : instancesWithPostConstruct) {
			for (Method method : obj.getClass().getDeclaredMethods()) {
				if (!method.isAnnotationPresent(PostConstruct.class)) {
					continue;
				}
				
				EasyInjectUtils.invokeMethod(obj, method);
			}
		}
		instancesWithPostConstruct.clear();
		instancesWithPostConstruct = null;
	}

	private void instantiateStartups() throws Exception {
		for (Class<?> clazz : reflections.getTypesAnnotatedWith(Startup.class)) {
			if (isSingletonClass(clazz) && singletonInstances.containsKey(clazz)) {
				continue;
			}
			
			getInstance(clazz);
		}
	}

	private void acquireEntryPoint() throws Exception {
		entryPointMethod = findEntryPointMethod();
		entryPointClass = entryPointMethod.getDeclaringClass();
		entryPointInstance = createEntryPointInstance(entryPointClass);
	}

	private void handleObjectsWithNullSingletonFields() throws Exception {
		for (Object obj : instancesWithNullSingletonFields) {
			for (Field field : obj.getClass().getDeclaredFields()) {
				if (!field.isAnnotationPresent(Inject.class) || !isSingletonClass(field.getType())
						|| EasyInjectUtils.getFieldValue(obj, field) != null) {
					continue;
				}
				
				Object singleton = singletonInstances.get(field.getType());
				EasyInjectUtils.setFieldValue(obj, field, singleton);
			}
		}
		
		instancesWithNullSingletonFields.clear();
		instancesWithNullSingletonFields = null;
	}

	private Object createEntryPointInstance(Class<?> clazz) throws Exception {
		List<Class<?>> forbidden = new ArrayList<Class<?>>();
		forbidden.add(entryPointClass);
		return createInstance(entryPointMethod.getDeclaringClass(), forbidden, false, true);
	}

	@SuppressWarnings("unchecked")
	public <E> E getExistingSingletonInstance(Class<E> clazz) {
		return (E) singletonInstances.get(clazz);
	}
	
	private EasyInjectSettings findSettings() throws Exception {
		Set<Class<? extends EasyInjectSettings>> set = getSubTypes(EasyInjectSettings.class);
		if (set.isEmpty()) {
			return new EasyInjectSettings();
		}
		else if (set.size() > 1) {
			throw new EasyInjectException("Found multiple candidates for EasyInjectSettings: "
					+ EasyInjectUtils.toString(set));
		}
		else {
			return set.iterator().next().newInstance();
		}
	}

	private Method findEntryPointMethod() throws EasyInjectException {
		Set<Method> set = reflections.getMethodsAnnotatedWith(EntryPoint.class);
		if (set.size() != 1) {
			throw new EasyInjectException("Exactly 1 EntryPoint required. Found " + set.size() + " EntryPoints");
		}
		return set.iterator().next();
	}
	
	private <E> Set<Class<? extends E> > getSubTypes(Class<E> clazz) {
		Set<Class<? extends E>> set = reflections.getSubTypesOf(clazz);
		
		Iterator<Class<? extends E>> iter = set.iterator();
		while (iter.hasNext()) {
			if (!canInstantiateClass(iter.next())) {
				iter.remove();
			}
		}
		return set;
	}
	
	private boolean canInstantiateClass(Class<?> clazz) {
		return canInstantiateClass(clazz, false);
	}
	
	private boolean canInstantiateClass(Class<?> clazz, boolean allowEntryPoint) {
		return !clazz.isInterface() && !clazz.isAnonymousClass()
				&& !clazz.isLocalClass() && !clazz.isMemberClass()
				&& !clazz.isSynthetic()
				&& !clazz.isAnnotationPresent(Exclude.class)
				&& !Modifier.isAbstract(clazz.getModifiers())
				&& !Modifier.isStatic(clazz.getModifiers())
				&& (allowEntryPoint || !clazz.equals(entryPointClass));
	}
	
	private <E, D extends Collection<E>> D getSubTypeInstances(Class<E> clazz,
			Class<D> collectionClazz, List<Class<?>> forbiddenClasses) throws Exception {
		return getSubTypeInstances(clazz, collectionClazz, forbiddenClasses, null);
	}
	
	private <E, D extends Collection<E>> D getSubTypeInstances(Class<E> clazz,
			Class<D> collectionClazz, List<Class<?>> forbiddenClasses,
			Integer maxAmount) throws Exception {
		Set<Class<? extends E>> set = getSubTypes(clazz);
		D collection = getCollectionInstance(collectionClazz);
		for (Class<? extends E> c : set) {
			logger.trace("Found " + c.getSimpleName() + " which extends " + clazz.getSimpleName());
			addInstanceToCollection(c, collection, forbiddenClasses);
			if (maxAmount != null && collection.size() >= maxAmount) {
				break;
			}
		}
		return collection;
	}
	
	private <E> void addInstanceToCollection(Class<? extends E> clazz,
			Collection<E> collection, List<Class<?>> forbiddenClasses) throws Exception {
		E instance = getInstance(clazz, forbiddenClasses);
		if (instance != null) {
			collection.add(instance);
		}
	}
	
	@SuppressWarnings("unchecked")
	private <E, D extends Collection<E>> D getCollectionInstance(Class<D> clazz)
			throws EasyInjectException {
		if (clazz.isAssignableFrom(ArrayList.class)) {
			return (D) new ArrayList<E>();
		}
		else if (clazz.isAssignableFrom(HashSet.class)) {
			return (D) new HashSet<E>();
		}
		else if (clazz.isAssignableFrom(LinkedList.class)) {
			return (D) new LinkedList<E>();
		}
		
		throw new EasyInjectException("No implementation found for " + clazz.getSimpleName());
	}
	private <E> E getInstance(Class<E> clazz) throws Exception {
		return getInstance(clazz, settings.isEverythingSingleton());
	}
	
	private <E> E getInstance(Class<E> clazz, boolean forceSingleton) throws Exception {
		List<Class<?>> forbidden = new ArrayList<Class<?>>();
		forbidden.add(clazz);
		return getInstance(clazz, forbidden, forceSingleton);
	}
	
	private <E> E getInstance(Class<E> clazz, List<Class<?>> forbiddenInjectionClasses) throws Exception {
		return getInstance(clazz, forbiddenInjectionClasses, settings.isEverythingSingleton(),
				settings.isAllowMultipleImplementationsOfInjectedInterface(),
				settings.isAllowNullInjections());
	}
	
	private <E> E getInstance(Class<E> clazz, List<Class<?>> forbiddenInjectionClasses, boolean forceSingleton) throws Exception {
		return getInstance(clazz, forbiddenInjectionClasses, forceSingleton,
				settings.isAllowMultipleImplementationsOfInjectedInterface(),
				settings.isAllowNullInjections());
	}

	@SuppressWarnings("unchecked")
	private <E> E getInstance(Class<E> clazz, List<Class<?>> forbiddenInjectionClasses,
			boolean forceSingleton, boolean allowMultipleImpls,
			boolean allowNullInjections) throws Exception {
		if (clazz.isInterface()) {
			return getImplementingClass(clazz, forbiddenInjectionClasses, forceSingleton, allowMultipleImpls);
		}
		
		if (isSingletonClass(clazz) && singletonInstances.containsKey(clazz)) {
			return (E) singletonInstances.get(clazz);
		}
		
		return createInstance(clazz, forbiddenInjectionClasses, allowNullInjections);
	}
	
	private <E> E createInstance(Class<E> clazz, List<Class<?>> forbiddenInjectionClasses,
			boolean allowNullInjections) throws Exception {
		return createInstance(clazz, forbiddenInjectionClasses, allowNullInjections, false);
	}
	
	private <E> E createInstance(Class<E> clazz, List<Class<?>> forbiddenInjectionClasses,
			boolean allowNullInjections, boolean allowEntryPoint) throws Exception {
		if (!canInstantiateClass(clazz, allowEntryPoint)) {
			if (allowNullInjections) {
				return null;
			}
			throw new EasyInjectException("Unable to instantiate " + clazz.getSimpleName());
		}
		
		logger.trace("Setting injected fields of new " + clazz.getSimpleName());
		E instance = clazz.newInstance();
		setInjectedFields(instance, forbiddenInjectionClasses);
		
		if (hasAnnotatedMethod(instance.getClass(), PostConstruct.class)) {
			instancesWithPostConstruct.add(instance);
		}
		if (isSingletonClass(instance.getClass())) {
			singletonInstances.put(instance.getClass(), instance);
		}
		
		logger.trace("Instantiated new instance of " + clazz.getSimpleName());
		return instance;
	}
	
	private boolean hasAnnotatedMethod(Class<?> clazz, Class<? extends Annotation> annotation) {
		for (Method m : clazz.getDeclaredMethods()) {
			if (m.isAnnotationPresent(annotation)) {
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	private <E, D extends E> D getImplementingClass(Class<E> clazz, List<Class<?>> forbiddenInjectionClasses,
			boolean forceSingleton, boolean allowMultipleImpls) throws Exception {
		
		Set<Class<? extends E>> set = getSubTypes(clazz);
		if (!allowMultipleImpls && set.size() > 1) {
			throw new EasyInjectException("Found " + set.size()
					+ " implementations for " + clazz.getSimpleName());
		}
		else if (set.isEmpty()) {
			throw new EasyInjectException("No implementation found for "
					+ clazz.getSimpleName());
		}
		
		Class<D> implClazz = (Class<D>) set.iterator().next();
		return getInstance(implClazz, forbiddenInjectionClasses);
	}
	
	@SuppressWarnings("unchecked")
	private <E> Object getInstance(Field field, List<Class<?>> forbiddenClasses) throws Exception {
		if (field.isAnnotationPresent(Named.class)) {
			return produce(field.getType(), field.getAnnotation(Named.class).value());
		}
		else if (Collection.class.isAssignableFrom(field.getType())) {
			Class<Collection<E>> collClazz = (Class<Collection<E>>) field.getType();
			Class<E> parametrizedType = (Class<E>) EasyInjectUtils.getParametrizedType(field);
			return getSubTypeInstances(parametrizedType, collClazz, forbiddenClasses);
		}
		else {
			return getInstance(field.getType(), forbiddenClasses);
		}
	}
	
	@SuppressWarnings("unchecked")
	private <E> E produce(Class<E> clazz, String producerName) throws Exception {
		logger.trace("Finding producer method annotated with @Named(\"" + producerName 
				+ "\") to produce new " + clazz.getSimpleName());
		
		if (isSingletonClass(clazz) && singletonInstances.containsKey(clazz)) {
			return (E) singletonInstances.get(clazz);
		}
		
		Method producerMethod = getMethodNamed(clazz, producerName,
				settings.isAllowMultipleProducers());
		Object producerInstance = getProducerInstance(clazz, producerMethod);
		if (producerInstance == null) {
			return null;
		}
		
		logger.trace("Found procuder " + producerInstance.getClass().getSimpleName()
				+ " to produce " + clazz.getSimpleName());
		
		E instance = (E) EasyInjectUtils.invokeMethod(producerInstance, producerMethod);
		if (isSingletonClass(instance.getClass())) {
			singletonInstances.put(instance.getClass(), instance);
		}
		return instance;
	}
	
	private Object getProducerInstance(Class<?> clazz, Method producerMethod) throws Exception {
		Class<?> producerClass = producerMethod.getDeclaringClass();
		boolean forceSingleton = settings.isProducerSingleton() || settings.isEverythingSingleton();
		return getInstance(producerClass, forceSingleton);
	}

	private Method getMethodNamed(Class<?> returnType, String named,
			boolean allowMultiple) throws EasyInjectException {
		Set<Method> methods = getMethodsNamed(named, returnType);
		if (methods.size() == 1) {
			return methods.iterator().next();
		}
		else if (!allowMultiple && methods.size() > 1) {
			throw new EasyInjectException("Found " + methods.size()
					+ " Methods annotated with Named(\"" + named + "\") returning "
					+ returnType.getSimpleName());
		}
		
		throw new EasyInjectException("Unable to find Method returning " + returnType.getSimpleName()
				+  "annotated with Named(\"" + named + "\")");
	}
	
	private Set<Method> getMethodsNamed(String named, Class<?> returnType) {
		Set<Method> namedMethods = new HashSet<Method>();
		for (Method m : reflections.getMethodsAnnotatedWith(Named.class)) {
			if (named.equals(m.getAnnotation(Named.class).value())
					&& m.getReturnType().equals(returnType)) {
				namedMethods.add(m);
			}
		}
		return namedMethods;
	}

	private <E> E setInjectedFields(E target, List<Class<?>> forbiddenClasses) throws Exception {
		List<Class<?>> forbiddenCopy = new ArrayList<Class<?>>();
		forbiddenCopy.addAll(forbiddenClasses);
		if (!forbiddenCopy.contains(target.getClass())) {
			forbiddenCopy.add(target.getClass());
		}
		
		for (Field field : target.getClass().getDeclaredFields()) {
			if (!field.isAnnotationPresent(Inject.class)) {
				continue;
			}
			
			if (field.getType() == target.getClass()) {
				throw new EasyInjectException("Injecting self in class " + target.getClass());
			}
			else if (forbiddenCopy.contains(field.getType())) {
				if (isSingletonClass(field.getType())) {
					if (!instancesWithNullSingletonFields.contains(target)) {
						instancesWithNullSingletonFields.add(target);
					}
					continue;
				}
				
				throw new EasyInjectException("Circular dependency detected: " + target.getClass()
						+ " should not inject " + field.getType() + " " + field.getName());
			}
			
			Object value = getInstance(field, forbiddenCopy);
			EasyInjectUtils.setFieldValue(target, field, value);
		}
		return target;
	}
	
	private boolean isSingletonClass(Class<?> clazz) {
		return settings.isEverythingSingleton() || clazz.isAnnotationPresent(Singleton.class)
				|| clazz == entryPointClass;
	}

	public Method getEntryPointMethod() {
		return entryPointMethod;
	}
	
	public Object getEntryPointInstane() {
		return entryPointInstance;
	}
}
