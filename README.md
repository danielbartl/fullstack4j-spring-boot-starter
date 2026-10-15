# fullstack4j Spring Boot Starter

**[Project page](https://danielbartl.github.io/fullstack4j-spring-boot-starter/)**

One dependency for a [fullstack4j](https://fullstack4j.dev) application: **Apache Wicket** with the
[Wicket Spring Boot Starter](https://github.com/danielbartl/wicket-spring-boot-starter),
[**Wicket Oat**](https://github.com/danielbartl/wicket-oat), **Spring Modulith** and **MongoDB**, at
versions that work together on **Spring Boot 4.1**. A test starter next to it brings `@WicketTest`,
Spring Modulith's test support, Testcontainers, and a MongoDB that Spring Modulith can actually run on.

---

## Motivation

A fullstack4j application builds every feature as one Spring Modulith module holding its Wicket pages,
its use cases, its MongoDB documents and its events. Putting that stack together by hand takes about a
dozen dependencies, two `dependencyManagement` entries and two settings, and two of those steps fail
in ways that are hard to trace back:

- **Wicket Oat brings an older Wicket.** Without an override, `wicket-extensions` resolves to the
  version Oat was built with, next to a newer `wicket-core`: a mix of Wicket versions that nobody
  has tested together.
- **Spring Modulith needs more from MongoDB than Spring Boot's defaults give.** Its event publication
  registry stores UUIDs, which the MongoDB driver refuses to encode until a UUID representation is set.
  And it runs every `@ApplicationModuleListener` in a transaction, which MongoDB only supports on a
  replica set: a single node, which is what a plain Testcontainers MongoDB is, fails the first event.

These starters settle all of that once. They hold no logic of their own beyond one default property
and one test configuration, so everything past that point is ordinary Wicket, Spring Modulith and
Spring Data MongoDB.

The [Wicket Spring Boot Starter](https://github.com/danielbartl/wicket-spring-boot-starter) stays
what it is: the smallest thing that runs Wicket on Spring Boot, for any application. This starter is
the opinionated bundle on top of it, for the fullstack4j stack in particular.

---

## Dependency Configuration

Add the starter, and the test starter in test scope, at the same version (check
[Maven Central](https://central.sonatype.com/artifact/dev.jbaby/fullstack4j-spring-boot-starter) for
the latest release):

```xml
<dependency>
    <groupId>dev.jbaby</groupId>
    <artifactId>fullstack4j-spring-boot-starter</artifactId>
    <version>0.1.0</version>
</dependency>

<dependency>
    <groupId>dev.jbaby</groupId>
    <artifactId>fullstack4j-spring-boot-starter-test</artifactId>
    <version>0.1.0</version>
    <scope>test</scope>
</dependency>
```

That is the whole build. No `spring-modulith-bom` import, no `spring-modulith.version` property, and
no Wicket entry in `dependencyManagement`.

### What they bring

| Starter | Brings |
|---|---|
| `fullstack4j-spring-boot-starter` | `wicket-spring-boot-starter` (Wicket, `@SpringBean`, Spring MVC, embedded Tomcat), `wicket-oat-core`, `spring-boot-starter-data-mongodb`, `spring-modulith-starter-core`, `spring-modulith-starter-mongodb` |
| `fullstack4j-spring-boot-starter-test` | `wicket-spring-boot-starter-test` (`WicketTester`, `@WicketTest`, Spring Boot's test starter), `spring-boot-starter-data-mongodb-test`, `spring-modulith-starter-test` (ArchUnit included), `spring-boot-testcontainers`, `testcontainers-junit-jupiter`, `testcontainers-mongodb`, and [`MongoDbReplicaSetConfiguration`](#mongodb-in-tests-and-while-developing) |

The test starter brings exactly what start.fullstack4j.dev adds to a project with Wicket, Spring Data
MongoDB, Testcontainers and Spring Modulith selected.

### One Wicket version

The starter declares `wicket-core` and `wicket-extensions` itself, although the Wicket starter and
Wicket Oat already bring them. Maven picks the declaration nearest to your project, so every Wicket
artifact resolves to the Wicket version of the Wicket starter rather than to the older one Oat was
built with. If you add another Wicket module yourself (`wicket-devutils`, a WicketStuff library),
give it that same version; the [Compatibility Matrix](#compatibility-matrix) lists it.

---

## The UUID Default

The starter sets

```properties
spring.mongodb.representation.uuid=standard
```

as a default with the lowest precedence of all property sources. A value of your own, in
`application.properties`, as the environment variable `SPRING_MONGODB_REPRESENTATION_UUID` or on the
command line, always wins over it. Without any value, publishing the first event fails with "The
uuidRepresentation has not been specified, so the UUID cannot be encoded".

---

## MongoDB in Tests and While Developing

`MongoDbReplicaSetConfiguration` starts `mongo:8` in Docker as a replica set and connects the
application to it through `@ServiceConnection`. Import it into tests:

```java
@WicketTest
@Import(MongoDbReplicaSetConfiguration.class)
class OrdersPageTests {
    // ...
}
```

and into the application started from the test classpath while developing:

```java
public class TestShopApplication {

    public static void main(String[] args) {
        SpringApplication.from(ShopApplication::main)
            .with(MongoDbReplicaSetConfiguration.class)
            .run(args);
    }
}
```

```bash
./mvnw spring-boot:test-run      # http://localhost:8080, on a throwaway MongoDB
```

Docker has to be running. Spring caches test contexts, so all tests sharing a context share one
container.

### Moving a generated project over

A project from [start.fullstack4j.dev](https://start.fullstack4j.dev) with `wicket`, `data-mongodb`,
`testcontainers` and `modulith` selected moves over in three steps:

1. Replace all its dependencies with the two above, and remove the `spring-modulith-bom` import and the
   `spring-modulith.version` property.
2. Delete the generated `TestcontainersConfiguration`: it starts a single MongoDB node, on which
   Spring Modulith's listeners fail.
3. Import `MongoDbReplicaSetConfiguration` wherever `TestcontainersConfiguration` was imported, in
   `TestShopApplication` and in the generated test.

---

## Compatibility Matrix

| Starter | Spring Boot | Spring Modulith | Apache Wicket | Wicket Spring Boot Starter | Wicket Oat | Java |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **`0.1.x`** (Current) | `4.1.x` | `2.1.x` | `10.11.x` | `0.2.x` | `0.3.x` | 17 to 25 |

Each Spring Modulith line belongs to one Spring Boot line (2.0 to Boot 4.0, 2.1 to Boot 4.1), so each
starter release supports one Spring Boot line. Java 26 and later are not supported yet: Wicket 10.11
can't create its `@SpringBean` proxies there. The [Wicket Spring Boot
Starter](https://github.com/danielbartl/wicket-spring-boot-starter#compatibility-matrix) explains why,
and this starter moves on when it does.

Compatibility is checked by the integration test under `fullstack4j-spring-boot-starter-test/src/it`:
a project shaped like one generated by Spring Initializr, inheriting from `spring-boot-starter-parent`
and depending on nothing but the two starters. It renders a page built from Wicket Oat components with
`@WicketTest`, sends an event from one Spring Modulith module to another through MongoDB, runs
`ApplicationModules.verify()`, and fails the build if two Wicket artifacts resolve to different
versions. To check another Spring Boot 4.1 patch release, run from the root:

```bash
mvn verify -Dspring-boot.version=4.1.0
```

---

## Development Console (optional)

[BootUI](https://www.julien-dubois.com/boot-ui/) is a local developer console for Spring Boot: health,
log tail, exceptions, HTTP exchanges, startup timing, beans and JVM views, plus a local MCP server
that lets a coding agent look into the running application. It is not part of the starters, because
it is a development tool, and Spring Boot's build plugin would package it into your production jar.
Its database panels are built for JDBC databases and don't cover MongoDB.

In a fullstack4j project it fits best in **test scope**: `spring-boot:test-run` starts the
application from the test classpath, so the console is there while you develop and can never reach
the jar you deploy.

```xml
<dependency>
    <groupId>com.julien-dubois.bootui</groupId>
    <artifactId>bootui-spring-boot-starter</artifactId>
    <version>2.1.0</version>
    <scope>test</scope>
</dependency>
```

BootUI switches itself on in the `dev` and `local` profiles, so activate one in `TestShopApplication`:

```java
SpringApplication.from(ShopApplication::main)
    .with(MongoDbReplicaSetConfiguration.class)
    .withAdditionalProfiles("dev")
    .run(args);
```

`./mvnw spring-boot:test-run` then serves the console at http://localhost:8080/bootui, next to your
Wicket pages: Wicket passes the requests it doesn't handle on to Spring MVC, where BootUI lives. Your
tests stay unaffected, since they run without the `dev` profile. BootUI also switches itself on
whenever `spring-boot-devtools` is on the classpath; if you add DevTools, set
`bootui.enabled=false` for your tests.

---

## What Is Not Included

- **Docker Compose.** fullstack4j starts its development database with Testcontainers instead. A
  starter can't ship the `compose.yaml` your project would need, Spring Boot's generated one starts a
  single MongoDB node, and `spring-boot-docker-compose` is meant to be an optional, development-only
  dependency of your own.
- **Spring Boot DevTools**, for the same reason. It works with the Wicket starter out of the box if you
  add it.
- **Architecture and traceability tests.** The [fullstack4j
  skill](https://github.com/danielbartl/fullstack4j-skill) ships `ArchitectureTests` and
  `SpecTraceabilityTests` to copy into a project.

---

## Building and Releasing

`mvn verify` builds both starters, runs their tests and the integration test. Build with JDK 17 to 25,
with Docker running for the MongoDB tests.

Releases go to Maven Central through the Central Portal, from the Release workflow. Pushing a tag
releases the version it names, while `main` stays on the next `-SNAPSHOT`:

1. Update the version in the README's dependency snippets and compatibility matrix, and in the
   project page (`docs/index.html`), then commit.
2. Tag and push:
   ```bash
   git tag vX.Y.Z
   git push origin vX.Y.Z
   ```
3. Once the release is on Maven Central, move `main` to the next development version:
   ```bash
   mvn versions:set -DnewVersion=X.Y+1.0-SNAPSHOT -DgenerateBackupPoms=false
   ```

The workflow runs the full build, attaches sources and Javadoc, signs everything with GPG, uploads it
and creates a GitHub Release. It needs the repository secrets `MAVEN_CENTRAL_USERNAME` and
`MAVEN_CENTRAL_PASSWORD` (a Central Portal user token), and `GPG_PRIVATE_KEY` and `GPG_PASSPHRASE`. To
review a release in the Central Portal before it goes public, set the repository variable
`CENTRAL_AUTO_PUBLISH` to `false`.

When the Wicket Spring Boot Starter moves to a new Wicket version, move `wicket.version` in the parent
pom with it: Dependabot leaves Wicket alone for that reason.

---

Apache Wicket, Wicket and Apache are trademarks of The Apache Software Foundation. This project is
not affiliated with or endorsed by the ASF.
