# Getting Started with Weaver-Rest

It's really easy to get a REST web server running with weaver-rest.

With weaver-rest, you don't build your software and then deploy it into a container to run it - your application *is* the server.  Deploying your application is super easy - just build a jar and run it.

## Adding Weaver-Rest to your Project

### Using Maven
Just add the weaver-rest package to your project's POM file:

```xml
<dependency>
  <groupId>com.zoomulus</groupId>
  <artifactId>weaver-rest</artifactId>
  <version>0.0.1</version>
</dependency>
```
Substitute the latest version (or your preferred version) into the version tag of course.

### Using Something Else
I'm not doing packaged versions for now (e.g JAR files), but you can certainly get the source and build it yourself if you have something against Maven:

```git clone https://github.com/zoomulus/weaver/rest.git```


## Creating the Server
Get your server running in just three easy steps.

### Define a ServerConnector
A ```ServerConnector``` describes a particular connection - for example, the port the server should listen on, where to find the resources, etc.

"Resources" here are classes that implement your endpoints.  We'll cover those in just a bit.

It is really easy to add resources to a ```ServerConnector```.  Add a single class as a resource by calling the ```withResource()``` method on the builder and passing in the class instance, e.g.
```java
	ServerConnector connector = RestServerConnector.builder()
		.withPort(8080)
		.withResource(MyTestResource.class)
		.build();
```
You can add as many resources as you want, like this:
```java
	ServerConnector connector = RestServerConnector.builder()
		.withPort(8080)
		.withResource(ATestResource.class)
		.withResource(AnotherTestResource.class)
		.withResource(LastTestResource.class)
		.build();
```
Or you can add all the classes in a particular package by specifying the package name:
```java
	ServerConnector connector = RestServerConnector.builder()
		.withPort(8080)
		.withResources("com.sample.testserver.resources")
		.build();
```

### Create a RestServer
Once you have a ```ServerConnector```, you use it to create a ```RestServer``` instance:
```java
	RestServer server = new RestServer(connector);
```

### Run the server
Now you just invoke the ```RestServer``` instance's ```start()``` method:
```java
	server.start();
```
That's it.  The server handles threading, shutdown events, etc. for you.

## Adding Resources
At this point you have a server that will start up, open the port you specified, and listen for connections on that port - but any request to that server is going to return 404.  To make your server complete, you need to add resource classes.  A resource class is just a basic Java class with certain annotations that tell Weaver-REST how to use the class as a REST resource.

Suppose you add a new class named ```MyResource```:
```java
@Path("/")
public class MyResource
{
    @GET
    @Path("get")
    public String hello()
    {
        return "Hello!";
    }
}
```
Weaver-REST generally tries to adhere to the JAX-RS 2.0 specification for resource classes.  In this example, that means the following:

* The class is decorated with the ```@Path``` annotation which defines the base path for all resources in the class.  Every endpoint in the class will build from this base path.
	* *IMPORTANT* - Your resource class *MUST* be decorated with the ```@Path``` annotation for Weaver-REST to recognize it as a resource.  The resource scanner will ignore it otherwise.
* The ```hello()``` method in this example is what we refer to as an *endpoint*.  Endpoints are identified by a path and an HTTP method.  The ```@Path``` annotation on method builds on the path provided for the resource class, so this endpoint's full path is "/get".  The ```@GET``` annotation defines the method for this endpoint as HTTP GET.

That's it.  Weaver-REST will automatically interpret the return value as a successful response and build a valid HTTP response with it, which gets sent back to the requesting client.

All that's left is to add your resource to your ```ServerConnector```:
```java
	ServerConnector connector = RestServerConnector.builder()
		.withPort(8080)
		.withResource(MyResource.class)
		.build();
```

Then re-run your server and try requesting your resource at http://localhost:8080/get.  You should get back a friendly "Hello!"