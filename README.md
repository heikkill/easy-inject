# easy-inject
Dependency injection for standalone Java applications

### Supported annotations
- **@Inject** : inject a field or a Collection of all instantiable subtypes of an interface
- **@Named** : map a field with its producer
- **@Singleton** : define a singleton class
- **@PostConstruct** : define method execution after injections are done
- **@Startup** : define a class to instantiate after injections are done
- **@EntryPoint** : define where to start injections from and a method to call when injections are done
- **@Exclude** : exclude class from all injections

### Usage
1. Add maven dependency
2. Annotate a method with ```@EntryPoint```. Injections will be started from the declaring class.
3. Annotate fields, methods & classes using annotations listed above
3. Call ```EasyInject()``` from somewhere or use the provided utility main class ```com.easyinject.util.EasyInjectMain```

### Example
```java
public class Main {
    public static void main(String[] args) throws Exception {
        new EasyInject();
    }
}

public class Zoo {
    @Inject
    private Bear bear;
    
    @Inject
    private List<Animal> animals;
    
    @EntryPoint
    private void init() {
        bear.doBearStuff();
        for (Animal animal : animals) {
            animal.doAnimalStuff();
        }
    }
}

public interface Animal {
    public void doAnimalStuff();
}

public class Bear implements Animal {
    public void doAnimalStuff() {
        System.out.println("DOING ANIMAL STUFF");
    }
    
    public void doBearStuff() {
        System.out.println("DOING BEAR STUFF");
    }
}

public class Chicken implements Animal {
	@Inject
	@Named("needanegg")
	private Egg egg;
	
	public void doAnimalStuff() {
        System.out.println("my egg is a " + egg.name);
	}
}

@Singleton
public class EggProducer {
	@Named("needanegg")
	public Egg produceEgg() {
	    return new Egg("produced egg");
	}
	
	@PostConstruct
	private void init() {
	    System.out.println("EggProducer is up");
	}
}

public class Egg {
    public String name;
    
    public Egg(String name) {
        this.name = name;
    }
}
```
#####Output:
```
EggProducer is up
DOING BEAR STUFF
DOING ANIMAL STUFF
my egg is a produced egg
```

### Settings
- **everythingSingleton** : force all classes used in injections to be singletons
- **producerSingleton** : force classes containing producer methods to be singletons
- **allowMultipleProducers** : when multiple possible producer methods are found, either throw an exception or just select any
- **allowMultipleImplementationsOfInjectedInterface** : when multiple possible implementations for an injected interface are found, either throw an exception or just select any
- **allowNullInjections** : whether to throw an exception if some field can not be injected

### Overriding default settings
Either provide settings to EasyInject constructor:
```java
new EasyInject(new EasyInjectSettings(true, false, false, false, false));
```

..or simply implement a class inherited from ```EasyInjectSettings``` and place it on the classpath. It will be scanned and hopefully found by EasyInject.
