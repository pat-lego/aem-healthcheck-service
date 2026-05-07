# AEMaaCS Readiness

`aem-readiness` is an OSGi bundle that exposes lightweight readiness endpoints
for Adobe Experience Manager as a Cloud Service style deployments.

The bundle provides a small HTTP surface and a pair of provider contracts that
other OSGi components can implement to report whether the system and important
content paths are ready to serve traffic.

## Purpose

The purpose of this project is to separate the readiness endpoint from the
application-specific readiness logic.

The bundle owns the servlet registration, URL structure, JSON response handling,
and OSGi service contracts. Implementing bundles own the actual readiness
decision, such as checking required services, validating application startup
state, or confirming that important content paths are available.

This gives the platform a stable readiness URL while still allowing each
implementation to define what "ready" means for its own application.

## What This Bundle Provides

The project provides:

- An OSGi Http Whiteboard context mounted under `/adobe/system/readiness`.
- A core readiness servlet mounted under `/core/*`.
- A content readiness servlet mounted under `/content/*`.
- A `SystemReadyProvider` service contract for system-level readiness checks.
- A `ContentReadyProvider` service contract for content-level readiness checks.
- A `Content` model that can be serialized to JSON for content readiness output.
- Custom checked exceptions for system and content readiness failures.
- A `ComponentResolver` helper for checking whether OSGi services are present.

The resulting endpoint paths are:

```text
/adobe/system/readiness/core/*
/adobe/system/readiness/content/*
```

## High-Level Flow

The readiness flow is intentionally small:

1. A request reaches one of the readiness servlet endpoints.
2. The servlet delegates the readiness decision to a registered provider
   service.
3. The provider performs application-specific checks.
4. The servlet translates the provider result into an HTTP response.

The servlet layer does not contain application-specific readiness rules. It is
the transport and integration layer between AEM, OSGi, and the provider services.

## Core Readiness

Core readiness is handled by `ReadinessCoreServlet`.

The servlet depends on a `SystemReadyProvider` implementation. That provider is
responsible for deciding whether the application is ready at a system level.

The provider contract is:

```java
boolean isReady(ComponentResolver componentResolver) throws SystemReadyException;

String isReadyFailed();

String isReadySucceeded();
```

`isReady(...)` performs the actual readiness check. It receives a
`ComponentResolver`, which can be used to check whether required OSGi services
are currently registered.

If the provider returns `true`, the servlet responds with HTTP 200 and writes the
success message returned by `isReadySucceeded()`.

If the provider returns `false` or throws `SystemReadyException`, the servlet
responds with HTTP 500.

## Content Readiness

Content readiness is handled by `ReadinessContentServlet`.

The servlet depends on a `ContentReadyProvider` implementation. That provider is
responsible for returning the content paths that were checked and the HTTP status
observed for each path.

The provider contract is:

```java
List<Content> getContent() throws ContentReadyException;
```

The servlet serializes the returned list with Gson and writes it as JSON.

The content model is intentionally small:

```java
public class Content {
    String contentPath;
    int responseCode;
}
```

An example JSON response would look like:

```json
[
  {
    "contentPath": "/content/site/us/en",
    "responseCode": 200
  },
  {
    "contentPath": "/content/site/ca/en",
    "responseCode": 200
  }
]
```

## Provider Responsibilities

Provider implementations should keep readiness checks fast and predictable.

Good readiness checks usually answer questions like:

- Are required OSGi services registered?
- Has the application completed initialization?
- Can required configuration be resolved?
- Are critical content paths present and reachable?
- Is the application able to serve traffic without returning startup errors?

Readiness checks should avoid expensive work, long network calls, or operations
that can put additional pressure on an already unhealthy instance.

## OSGi Design

This project uses OSGi Declarative Services and the OSGi Http Whiteboard model.

The context is registered with:

```java
@HttpWhiteboardContext(
    name = "com.adobe.skyline.readiness",
    path = "/adobe/system/readiness"
)
```

The servlets select that context with:

```java
@HttpWhiteboardContextSelect(
    "(osgi.http.whiteboard.context.name=com.adobe.skyline.readiness)"
)
```

This keeps the readiness servlets grouped under the same context path and avoids
hard-coding full endpoint URLs inside each servlet.

## Exported API Packages

The Maven build uses `bnd-maven-plugin` to generate the OSGi manifest and export
the API packages other bundles may need to compile against:

```text
com.adobe.skyline.readiness.services
com.adobe.skyline.readiness.model
com.adobe.skyline.readiness.exceptions
com.adobe.skyline.readiness.utils
```

These exports allow another bundle to implement `SystemReadyProvider` or
`ContentReadyProvider` without depending on servlet implementation classes.

Servlet packages are implementation details and should not be treated as public
API unless another bundle truly needs to import them.

## Runtime Notes

The bundle is intended to run inside an AEM-compatible OSGi runtime. It relies on
runtime-provided OSGi, servlet, and framework services.

The project currently compiles with:

```xml
<maven.compiler.release>21</maven.compiler.release>
```

That means the generated bundle can require the OSGi execution environment
`JavaSE-21`. The AEM runtime must be running on a compatible Java version, or
the bundle will fail to resolve with an `osgi.ee` requirement error.

If the target runtime is Java 11 or Java 17, lower the Maven compiler release to
match the runtime before building the bundle.

## Dependencies

The project uses:

- OSGi APIs for Declarative Services, framework access, and Http Whiteboard.
- Servlet API for the readiness servlets.
- Gson 2.9.0 for JSON serialization.
- Lombok for model boilerplate generation.
- JUnit 5 for tests.
- bnd for OSGi manifest generation and package exports.

In an AEM deployment, dependencies that are already provided by the runtime
should not be embedded into the bundle unless there is a specific reason to do
so. This is especially important for OSGi APIs, servlet APIs, and libraries
already exported by AEM.

## Project Structure

```text
src/main/java/com/adobe/skyline/readiness
├── exceptions
│   ├── ContentReadyException.java
│   └── SystemReadyException.java
├── model
│   └── Content.java
├── services
│   ├── ContentReadyProvider.java
│   └── SystemReadyProvider.java
├── servlets
│   ├── ReadinessContentServlet.java
│   ├── ReadinessContext.java
│   └── ReadinessCoreServlet.java
└── utils
    └── ComponentResolver.java
```

## Building

Build the bundle with Maven:

```bash
mvn clean package
```

The generated bundle is written to:

```text
target/aem-readiness-1.0-SNAPSHOT.jar
```

The bnd-generated manifest is included by the Maven jar plugin so the final JAR
contains the OSGi metadata required by the runtime.

## Intended Usage

This bundle should be installed into AEM alongside an implementation bundle that
registers one or both provider services.

At minimum, an implementation bundle can provide:

```java
@Component(service = SystemReadyProvider.class)
public class MySystemReadyProvider implements SystemReadyProvider {
    @Override
    public boolean isReady(ComponentResolver componentResolver)
            throws SystemReadyException {
        return componentResolver.isPresent("com.example.RequiredService");
    }

    @Override
    public String isReadyFailed() {
        return "{\"ready\":false}";
    }

    @Override
    public String isReadySucceeded() {
        return "{\"ready\":true}";
    }
}
```

For content checks, an implementation can provide:

```java
@Component(service = ContentReadyProvider.class)
public class MyContentReadyProvider implements ContentReadyProvider {
    @Override
    public List<Content> getContent() throws ContentReadyException {
        return List.of();
    }
}
```

The readiness bundle then exposes those provider results through the registered
HTTP endpoints.

## Summary

`aem-readiness` is a small AEM OSGi readiness bundle. It gives AEM a consistent
readiness endpoint structure and gives application code a clean provider API for
reporting system and content health.

The important idea is that the servlet bundle should stay generic. The business
meaning of readiness should live in provider implementations that can evolve per
project, environment, or application.
