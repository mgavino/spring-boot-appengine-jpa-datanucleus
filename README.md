# Spring Boot + Restful + App Engine + Objectify

This is a simple sample of a Restful API build with Spring Boot, prepare to be deploy on App Engine, using Objectify as data access API (specifically designed for the Google Cloud Datastore).

## Spring Boot

Firsly, we need to setup our pom.xml to use Spring Boot.
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

Now, we are going to configure our _main_ class:
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

Firsly, let's exclude _spring-boot-starter-tomcat_, and add _javax.servlet-api_ dependency as provided. This is because App Engine uses Jetty while Spring Boot uses Tomcat. Our dependencies on _pom.xml_ has to be like this:
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

And add Maven App Engine plugin (default port is 8080, so I have changed it to not having conflicts with other web applications)
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

Finally, we need to create the app engine configuration file, _appengine_web.xml_ under WEB-INF directory:
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
    
    @Id
    private Long id;
    
    private String title;
    private String description;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

At this point, let's go to separate 2 ways to config Objectify

### Simple configuration

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

### My configuration

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

On the other hand, insted of using _ObjectifyFilter_ to begin the transaction, I have used AOP to do it at the Service level.
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
