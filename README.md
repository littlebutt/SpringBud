# SpringBud 

![](https://img.shields.io/github/languages/top/littlebutt/SpringBud) ![](https://img.shields.io/github/license/littlebutt/SpringBud) ![](https://img.shields.io/github/repo-size/littlebutt/SpringBud)

## Introduction

This is a nano version of famous Java Framework SpringBoot. The purpose of the project is to learn the architecture and programming thought of SpringBoot.

## Implementation

Currently, it implements IOC, DI, AOP, and MVC. The MVC module is deprecated due to the collision between ASM and Tomcat container. More effort need to put into the project.

### IOC Module

The IOC (Inversion of Control) module is implemented in `org.springbud.core`. 
The general ideal is traversing every package and initializing every `class` as a bean with package name.The container will collect beans and provide methods to access these beans. 
The default value of fields is `null`. To fill these field [See DI Module](#1). The package name can be package name or .jar file.

#### Annotations
    
Annotations include `@Component`, `@Controller`, `@Repository`, and `@Service`:
    
| Annotation | Parameters | Description |
|------------| -----------|-------------|
|`@Component`|            | Common bean |
|`@Controller`|            | Controller bean|
|`@Repository`|            | Repository bean|
|`@Service`  |            | Service bean|

#### Usages

The classes with these annotations will be put into the bean container and initialized when the container is ready. 

### DI Module <span id="1"></span>

The DI (Dependencies Injection) module is implemented in `org.springbud.inject`.
It will traverse every field in evry class and initialize those fields with `@Autowired` annotation. 
The field will be initialized with given class in `value`. Unlike Spring Framwork, current framework can only support field injection.

#### Annotations

Annotations include `@Autowired`:

| Annotation | Parameters | Description |
|------------| -----------|-------------|
|`@Autowired`|`Value`   | Bean to be injected when initialization|

#### Usages

The fields with the annotation will be injected with given class.

### AOP Module

The AOP (Aspect Oriented Programming) module is implemented in `org.springbud.aop`.
It uses AspectJ as a Proxy API to weave the aspect functions into normal classes.

#### Annotations

Annotations include `@Aspect`, and `@Order`:

| Annotation | Parameters | Description |
|------------| -----------|-------------|
|`@Aspect`  | `pointcut` | The aspect defining classes|
|`@Order`  | `value` | The priority of the aspect|

#### Usages

The aspect defining classes must extend `DefaultAspect` and all parameters in annotations are required:

```java
@Aspect(pointcut = "execution(* com.littlebutt.controller..*.*(..))")
@Order(1)
public class Logger extends DefaultAspect {
    @Override
    public void before(Class<?> target, Method method, Object[] args) throws Throwable {
        System.out.println("[AOP info] Before: Controller Object " + target + ", Method " + method);
    }
}
```

### MVC Module (deprecated)

The MVC module use tomcat to host a service for MVC implementation. It implements dafault render, json render, and view render to present the data.
It uses job chain to process different requests and HttpServlet to forward request.

#### Annotation

Annotations include `@RequestMapping`, `@RequestParam`, and `@ResponseBody`:

| Annotation | Parameters | Description |
|------------| -----------|-------------|
|`@RequestMapping`| `value`, `method` | The path for each method call|
|`@RequestParam` | `value`, `isRequired` | The path matching parameters |
|`@ResponseBody`|         | The return value |

#### Usages

To use this module, a tomcat server must be deployed. The classes with these annotations will be explained as controllers. In addition, the package name will also be set.

### ORM Model

The ORM module is implemented in `org.springbud.orm`. It uses reflection to create tables (default) and insert data into tables. Currently, the query function does not support because of the constructor issue.

#### Annotation

Annotations include `@DatabaseConfigurer`, `@Table`, `@Id`, and `@Column`:

| Annotation | Parameters | Description |
|------------| -----------|-------------|
|`@DatabaseConfigurer` | `host`, `port`, `database`, `username`, `password` | The configuration class for the connection of the database |
|`@Table`| `name` | The table in the database |
|`@Id` | `type`, `name` | The Id of a record in the table |
|`@Column` | `type`, `name` | The column of a record in the table |

#### Usages

To use ORM in the framework, a configurer class and an entity are necessary. In the configurer class:

```java
import org.springbud.orm.annotations.DatabaseConfigurer;

@DatabaseConfigurer(password = "root" /* Some other configuration*/)
class DatabaseConfig{
}
```

In an entity class:

```java
import org.springbud.core.annotation.Component;
import org.springbud.orm.annotations.Id;import org.springbud.orm.annotations.Table;

@Table("table1")
@Component
class Entity {
    @Id(type = "INTEGER", name = "id")
    private int id;
    // ...
}
```

When inserting entity:

```java
class Test{
    public static void main(String[] args){
      new DatabaseProcessor().insert(new Entity(1));
    }
}
```

## TODO

- Reconstruct the MVC module
- The query function in ORM module

## Reference
 
[the course on imooc.com](https://coding.imooc.com/class/420.html)