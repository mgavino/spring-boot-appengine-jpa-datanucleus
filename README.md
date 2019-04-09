# Spring Boot + Restful + App Engine + Objectify

This is a simple sample of a Restful API build with Spring Boot, prepare to be deploy on App Engine, using Objectify as data access API (specifically designed for the Google Cloud Datastore).

## Spring Boot

Firstly, we need to setup our pom.xml to use Spring Boot.
Adding Spring Boot Parent:
```
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.1.3.RELEASE</version>
</parent>
```
And the initial dependencies and plugin:
```
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>
    </plugins>
</build>
```

Now, we'll configure our _main_ class:
```
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

We can add a simple _@Controller_ to check that it works.
```
@RestController
public class HomeController {
    @RequestMapping("/")
    public String grettings() {
        return "Greetings!";
    }
}
```

And checking it running the server
```
$ mvn spring-boot:run
```

## App Engine

Having this, we continue configuring our application to be runnable on App Engine

Firstly, let's exclude _spring-boot-starter-tomcat_, and add _javax.servlet-api_ dependency as provided. This is because App Engine uses Jetty while Spring Boot uses Tomcat. Our dependencies on _pom.xml_ has to be like this:
```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <exclusions>
        <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-tomcat</artifactId>
        </exclusion>
    </exclusions>
</dependency>

<dependency>
    <groupId>javax.servlet</groupId>
    <artifactId>javax.servlet-api</artifactId>
    <version>3.1.0</version>
    <scope>provided</scope>
</dependency>
```

And add Maven App Engine plugin (default port is 8080, so I have changed it not to having conflicts with other web applications)
```
<plugin>
    <groupId>com.google.appengine</groupId>
    <artifactId>appengine-maven-plugin</artifactId>
    <version>1.9.71</version>
    <configuration>
        <port>9090</port>
    </configuration>
</plugin>
```

Finally, we need to create the App Engine configuration file, _appengine_web.xml_ under WEB-INF directory:
```
<appengine-web-app xmlns="http://appengine.google.com/ns/1.0">
    <threadsafe>true</threadsafe>
    <runtime>java8</runtime>
</appengine-web-app>
```

Let´s go to check it (we have to have the same behivor as with _spring-boot:run_, but now, under App Engine):
```
$ mvn appengine:run
```

## Objectify

To begin with, we need to add Objectify dependency on our _pom.xml_:
```
<dependency>
    <groupId>com.googlecode.objectify</groupId>
    <artifactId>objectify</artifactId>
    <version>6.0.3</version>
</dependency>
```

And create our entity objects, using _@Entity_ annotation from Objectify:
```
@Entity
public class ExampleEntity extends IdentifyEntity {
    
    private String title;
    private String description;

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

}
```

I have also created an abstract class _IdentifyEntity_, which will be use in each _@Entity_:
```
public abstract class IdentifyEntity {

    @Id
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
```

At this point, I would like to separate 2 ways to config Objectify

#### Simple configuration

As we need to init _ObjectifyService_ and register our entities before doing any operation, we can add in our _main_ class (_Application.java_) the next method:
```
@PostConstruct
private void initObjectify() throws Exception {
    ObjectifyService.init();
    ObjectifyService.register(ExampleEntity.class);
}
```

It is needed, also, to begin the _transaction_ before using Objectify. For this, there is a Filter which init the transaction for each http request. In Spring Boot, we can configurate it on this way:
```
@Configuration
public class ObjectifyConfig {
  @Bean
  public FilterRegistrationBean<ObjectifyFilter> objectifyFilterRegistration() {
    final FilterRegistrationBean<ObjectifyFilter> registration = new FilterRegistrationBean<>();
    registration.setFilter(new ObjectifyFilter());
    registration.addUrlPatterns("/*");
    registration.setOrder(1);
    return registration;
}
```

#### My configuration

As I didn't like to much the idea of having the transaction at the HTTP Request level (I prefer using @Service component for that), and much less having to register each @Entity manually, I did the next steps:

I have created a custom Objectify configuration where Objectify is initialized and each entity is registered, but instead of doing it manually, I use the class _ClassPathScanningCandidateComponentProvider_ from Spring to looking for all @Entity objects in my project:
```
@Configuration
public class ObjectifyConfig {
    @PostConstruct
    private void init() throws Exception {
        ObjectifyService.init();
        
        // register entities
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));

        Set<BeanDefinition> entityBeanDefinitions = scanner.findCandidateComponents("com.mgavino.restful_appengine_objectify");

        entityBeanDefinitions.stream()
                .map( bean -> {
                    try {
                        return Class.forName(bean.getBeanClassName());
                    } catch (Exception exception) {
                        return null;
                    }
                } )
                .filter( entityClass -> entityClass != null )
                .forEach( entityClass -> ObjectifyService.register(entityClass) );

    }
}
```

On the other hand, insted of using _ObjectifyFilter_ to begin the transaction, I have used AOP to do it at the Service layer.
Firstly, let's add aspect dependencies on our _pom.xml_:
```
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
```

Then, let's create our Aspect configuration:
```
@Aspect
@Configuration
public class ObjectifyAspect {

    @Around("anyMethod() && serviceClasses()")
    public Object transaction(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result;
        try (Closeable closeable = ObjectifyService.begin()) {
            result = joinPoint.proceed();
        }
        return result;
    }

    @Pointcut("@within(org.springframework.stereotype.Service)")
    public void serviceClasses() {}

    @Pointcut("execution(* *(..))")
    public void anyMethod() {}

}
```

At last, we have to enable the Aspect configuration in our Application, adding _@EnableAspectJAutoProxy_ to our application _main_ class
```
@SpringBootApplication
@EnableAspectJAutoProxy
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

## Service
For this sample, I have just created a Generic Service with CRUD operations. In each method, you have to inform the _@Entity_ classType, in which the operation will be executed:
```
@Service
public class GenericService {

    public <T extends IdentifyEntity> List<T> findAll(Class<T> classType) {
        return ObjectifyService.ofy().load().type(classType).list();
    }

    public <T extends IdentifyEntity> T find(Long id, Class<T> classType) {
        return ObjectifyService.ofy().load().type(classType).id(id).now();
    }

    public <T extends IdentifyEntity> T save(T entity) {
        ObjectifyService.ofy().save().entity(entity).now();
        return entity;
    }

    public <T extends IdentifyEntity> void delete(Long id, Class<T> classType) {
        ObjectifyService.ofy().delete().type(classType).id(id).now();
    }

    public <T extends IdentifyEntity> void deleteAll(Class<T> classType) {
        ObjectifyService.ofy().delete().entities( findAll(classType) );
    }

}
```

## Restful
At this point, we are left with just one task, the Restful layer. Simply, we have to create a _@Controller_ with our Restful API, calling to the given _@Service_ method in each case:

```
@RestController
@RequestMapping("/example")
public class ExampleController {

    @Autowired
    private GenericService service;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<ExampleEntity>> getAll() {

        List<ExampleEntity> entities = service.findAll(ExampleEntity.class);
        if (entities == null || entities.isEmpty()) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<List<ExampleEntity>>(entities, HttpStatus.OK);

    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<ExampleEntity> get(@PathVariable("id") long id) {

        ExampleEntity entity = service.find(id, ExampleEntity.class);
        if (entity == null) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<ExampleEntity>(entity, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<ExampleEntity> post(@RequestBody ExampleEntity entity, UriComponentsBuilder ucBuilder) {

        if (entity.getId() != null) {
            entity.setId(null);
        }

        entity = service.save(entity);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ucBuilder.path("/example/{id}").buildAndExpand(entity.getId()).toUri());
        return new ResponseEntity<ExampleEntity>(entity, headers, HttpStatus.CREATED);

    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity<ExampleEntity> put(@PathVariable("id") Long id, @RequestBody ExampleEntity entity) {

        ExampleEntity foundEntity = service.find(id, ExampleEntity.class);
        if (foundEntity == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        entity.setId(id);
        service.save(entity);

        return new ResponseEntity<ExampleEntity>(entity, HttpStatus.OK);

    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PATCH)
    public ResponseEntity<ExampleEntity> patch(@PathVariable("id") Long id, @RequestBody ExampleEntity entity) {

        ExampleEntity foundEntity = service.find(id, ExampleEntity.class);
        if (foundEntity == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (entity.getId() != null) {
            entity.setId(null);
        }

        Utils.merge( entity, foundEntity );

        service.save(foundEntity);

        return new ResponseEntity<ExampleEntity>(entity, HttpStatus.OK);

    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<ExampleEntity> delete(@PathVariable("id") Long id) {

        ExampleEntity entity = service.find(id, ExampleEntity.class);
        if (entity == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        service.delete(id, ExampleEntity.class);
        return new ResponseEntity<ExampleEntity>(HttpStatus.NO_CONTENT);
    }

}
```

And try it:
```
$ mvn appengine:run
```
