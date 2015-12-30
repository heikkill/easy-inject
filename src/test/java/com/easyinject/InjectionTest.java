package com.easyinject;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.easyinject.testclasses.Class1;
import com.easyinject.testclasses.Class2;
import com.easyinject.testclasses.Class3;
import com.easyinject.testclasses.Class6;
import com.easyinject.testclasses.EntryPointClass;
import com.easyinject.testclasses.EntryPointHost;
import com.easyinject.testclasses.Interface2;
import com.easyinject.testclasses.ProducerClass;
import com.easyinject.testclasses.StartupClass1;
import com.easyinject.testclasses.StartupClass2;
import com.easyinject.testclasses.StartupClass3;

public class InjectionTest {
	
	@Test(dataProvider = "successSettings")
	public void testSuccessfulInjections(EasyInjectSettings settings) throws Exception {
		EasyInject easyInject = new EasyInject(settings);
		
		// Check entry point
		EntryPointClass epc = (EntryPointClass) easyInject.getEntryPointInstane();
		Assert.assertEquals(epc.isInitCalled(), true);
		Assert.assertEquals(epc != null, true);
		Assert.assertEquals(epc.getClass2() != null, true);
		Assert.assertEquals(epc.getClass4() == null, true);
		Assert.assertEquals(epc.getInterface1() != null, true);
		Assert.assertEquals(epc.getProducedClass() != null, true);
		Assert.assertEquals(epc.getInterface1List() != null, true);
		Assert.assertEquals(epc.getInterface1List().size(), 2);
		
		// Check implementation found for Interface2 and added to Set
		Class1 class1 = epc.getClass1();
		Assert.assertEquals(class1.getInterface2Set().size(), 1);
		Interface2 i2 = class1.getInterface2Set().iterator().next();
		Assert.assertEquals(i2 instanceof Class3, true);
		Class3 class3 = (Class3) i2;
		
		// Check circular dependency between singletons
		Class6 class6 = class1.getClass6();
		Assert.assertEquals(class6 != null, true);
		Assert.assertEquals(class6.getClass1() == class1, true);
		
		// Check startup classes
		StartupClass1 startupClass1 = class6.getStartupClass1();
		Assert.assertEquals(startupClass1 != null, true);
		StartupClass2 startupClass2 = class6.getStartupClass2();
		Assert.assertEquals(startupClass2 != null, true);
		StartupClass3 startupClass3 = StartupClass3.getInstance();
		Assert.assertEquals(startupClass3 != null, true);
		Assert.assertEquals(StartupClass3.getInstanceCount(), settings.isEverythingSingleton() ? 1 : 2);
		Assert.assertEquals(startupClass1 == startupClass3.getStartupClass1(), true);
		Assert.assertEquals(startupClass2 == startupClass1.getStartupClass2(), settings.isEverythingSingleton());
		
		// Check instance equality
		Class2 class2 = (Class2) (epc.getInterface1List().get(0) instanceof Class2
				? epc.getInterface1List().get(0) : epc.getInterface1List().get(1));
		Assert.assertEquals(class2 == epc.getClass2(), settings.isEverythingSingleton());
		Assert.assertEquals(class1.getClass2() == epc.getClass2(), settings.isEverythingSingleton());
		Assert.assertEquals(class3.getProducedClass() == epc.getProducedClass(), settings.isEverythingSingleton());
		
		// Check @PostConstruct calls
		Assert.assertEquals(class2.isInitCalled(), true);
		Assert.assertEquals(startupClass1.isInitCalled(), true);
		
		// Check correct amount of producers & producer calls
		List<ProducerClass> producerClasses = ProducerClass.getInstances();
		if (settings.isEverythingSingleton()) {
			Assert.assertEquals(producerClasses.size(), 1);
			Assert.assertEquals(producerClasses.get(0).getProduceCalls(), 1);
		}
		else {
			Assert.assertEquals(producerClasses.size(), 3);
			for (ProducerClass pc : producerClasses) {
				Assert.assertEquals(pc.getProduceCalls(), 1);
			}
		}
		
		// Check injecting entry point class
		EntryPointHost entryPointHost = class6.getEntryPointHost();
		Assert.assertEquals(entryPointHost != null, true);
		Assert.assertEquals(entryPointHost.getEntryPointClass() == epc, true);
	}
	
	@DataProvider(name = "successSettings")
	public Object[][] getSuccessSettings() {
		return new Object[][] {
				{ new EasyInjectSettings(true, true, false, true, true) },
				{ new EasyInjectSettings(false, true, false, true, true) }
		};
	}
	
	@Test(expectedExceptions = EasyInjectException.class, dataProvider = "failureSettings")
	public void testUnsuccessfulInjections(EasyInjectSettings settings) throws Exception {
		new EasyInject(settings);
	}
	
	@DataProvider(name = "failureSettings")
	public Object[][] getFailureSettings() {
		return new Object[][] {
				{ new EasyInjectSettings(true, true, false, false, true) },
				{ new EasyInjectSettings(true, true, false, true, false) }
		};
	}
}
