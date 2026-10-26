# fullstack4j Spring Boot Starter

**[Project page](https://danielbartl.github.io/fullstack4j-spring-boot-starter/)**

One dependency for a [fullstack4j](https://fullstack4j.dev) application: **Apache Wicket** with the
[Wicket Spring Boot Starter](https://github.com/danielbartl/wicket-spring-boot-starter),
[**Wicket Oat**](https://github.com/danielbartl/wicket-oat), **Spring Modulith** and **MongoDB**, at
versions that work together on **Spring Boot 4.1**. A test starter next to it brings `@WicketTest`
with [wicket-testkit](https://github.com/danielbartl/wicket-testkit), Spring Modulith's test support,
Testcontainers, a MongoDB that Spring Modulith can actually run on, and
the rules that keep a fullstack4j application in shape as it grows.

---

## Motivation

A fullstack4j application builds every feature as one Spring Modulith module holding its Wicket pages,
its use cases, its MongoDB documents and its events. Putting that stack together by hand takes about a
dozen dependencies, two `dependencyManagement` entries and two settings, and two of those steps fail
in ways that are hard to trace back:

- **Wicket Oat can bring an older Wicket.** Without an override, `wicket-extensions` resolves to the
  version Oat was built with, which is older than `wicket-core` whenever Wicket moves on first: a mix
  of Wicket versions that nobody has tested together.
- **Spring Modulith needs more from MongoDB than Spring Boot's defaults give.** Its event publication
  registry stores UUIDs, which the MongoDB driver refuses to encode until a UUID representation is set.
  And it runs every `@ApplicationModuleListener` in a transaction, which MongoDB only supports on a
  replica set: a single node, which is what a plain Testcontainers MongoDB is, fails the first event.

These starters settle all of that once. They hold little of their own: one default property, one test
configuration, and the architecture rules every fullstack4j application follows. Everything past that
point is ordinary Wicket, Spring Modulith and Spring Data MongoDB.

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
    <version>0.7.0</version>
</dependency>

<dependency>
    <groupId>dev.jbaby</groupId>
    <artifactId>fullstack4j-spring-boot-starter-test</artifactId>
    <version>0.7.0</version>
    <scope>test</scope>
</dependency>
```

That is the whole build. No `spring-modulith-bom` import, no `spring-modulith.version` property, and
no Wicket entry in `dependencyManagement`.

### What they bring

| Starter | Brings |
|---|---|
| `fullstack4j-spring-boot-starter` | `wicket-spring-boot-starter` (Wicket, `@SpringBean`, Spring MVC, embedded Tomcat), `wicket-oat-core`, `spring-boot-starter-data-mongodb`, `spring-modulith-starter-core`, `spring-modulith-starter-mongodb` |
| `fullstack4j-spring-boot-starter-test` | `wicket-spring-boot-starter-test` (`WicketTester`, `@WicketTest`, [wicket-testkit](https://github.com/danielbartl/wicket-testkit), Spring Boot's test starter), `spring-boot-starter-data-mongodb-test`, `spring-modulith-starter-test` (ArchUnit included), `spring-boot-testcontainers`, `testcontainers-junit-jupiter`, `testcontainers-mongodb`, [Taikai](https://github.com/enofex/taikai), [`MongoDbReplicaSetConfiguration`](#mongodb-in-tests-and-while-developing) and [`Fullstack4jRules`](#the-fullstack4j-rules) |

The test starter brings what start.fullstack4j.dev adds to a project with Wicket, Spring Data MongoDB,
Testcontainers and Spring Modulith selected, plus Taikai for the fullstack4j rules.

### One Wicket version

The starter declares `wicket-core` and `wicket-extensions` itself, although the Wicket starter and
Wicket Oat already bring them. Maven picks the declaration nearest to your project, so every Wicket
artifact resolves to the Wicket version of the Wicket starter rather than to the one Oat was built
with, which can be older. If you add another Wicket module yourself (`wicket-devutils`, a WicketStuff library),
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

`MongoDbReplicaSetConfiguration` starts `mongo:8.0` in Docker as a replica set and connects the
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

### Another MongoDB image

*Since 0.4.0.* The container runs `mongo:8.0`, the MongoDB line most production databases run
(0.3.x and earlier run `mongo:8`, which follows MongoDB's newest 8.x release). To run another image,
set `fullstack4j.test.mongodb.image`, in `src/test/resources/application.properties` for the
project or as an environment variable for one machine:

```bash
export FULLSTACK4J_TEST_MONGODB_IMAGE=mongo:7.0
```

### MongoDB doesn't start: "Linux kernel versions 6.19 and newer has a known incompatibility"

Every test that needs MongoDB fails with `Container startup failed for image mongo:8.0`, and the
container's log says:

```
MongoDB cannot start: Linux kernel versions 6.19 and newer has a known incompatibility with this
version of MongoDB.
```

MongoDB 8 refuses to start on Linux kernels 6.19 to 7.0.13, which break the memory allocator it
uses ([SERVER-125742](https://jira.mongodb.org/browse/SERVER-125742)). The kernel that runs Docker
decides, so `uname -r` there tells you. Docker Desktop brings its own kernel.

- **Update the kernel** to 7.0.14 or later, which fixes the allocator. Nothing else changes.
- Until then, **run MongoDB 7.0** on that machine (starter 0.4.0 and later):
  `FULLSTACK4J_TEST_MONGODB_IMAGE=mongo:7.0`. It
  uses an older allocator and runs on those kernels; Spring Modulith's events work on it as well.
- **On GitHub Actions**, `ubuntu-latest` has such a kernel since image 20261018 (7.0.0). Use
  `runs-on: ubuntu-22.04` (kernel 6.8), or set the variable for the job.

Don't switch to MongoDB 9.0 or 8.2 for this: they use the same allocator without the check, so
they start, but can crash later.

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

## The fullstack4j Rules

*Since 0.3.0.* Spring Modulith's `ApplicationModules.verify()` keeps slices out of each other's
`internal` packages and free of cycles. `Fullstack4jRules` checks what it leaves alone, with
[Taikai](https://github.com/enofex/taikai) and ArchUnit. Run both:

```java
class ArchitectureTests {

    @Test
    void slicesStayOutOfEachOthersInternals() {
        ApplicationModules.of(ShopApplication.class).verify();
    }

    @Test
    void followsTheFullstack4jRules() {
        Fullstack4jRules.builder(ShopApplication.class)
            .build()
            .checkAll();
    }
}
```

A slice is a direct subpackage of the application's package, as in Spring Modulith:

| Rule | Why |
|---|---|
| A slice is named after a business capability, as one lowercase word, and below it there is only `<slice>.internal` | `ordering` and `billing`, not `web` or `shared`: one slice is one feature, from page to database |
| A slice's base package holds only Wicket components, interfaces, records and enums | It is the slice's public API. Facades, documents and repositories go in `internal` |
| MongoDB documents live in `<slice>.internal` | Documents never leave their slice; other slices get records and events |
| Wicket components and a slice's base package use no repositories, no Spring Data and no MongoDB driver | Data goes through the slice's facade |
| An `@ApplicationModuleListener` listens to a record, never to a document | Events are immutable records |
| Only the slice that owns an event creates it | A slice that wants another to act calls an interface the other offers |
| Wicket components hold no documents, and no records that aren't `Serializable` | Wicket serialises components between requests. The rule also finds documents captured by anonymous components |
| Wicket components carry no Spring annotations such as `@Component`, `@Transactional`, `@Async` or `@EventListener` | Spring never proxies a component, so the annotations do nothing |
| No class is named `*Service`, `*Impl` or `*Dto` | A facade is named after what it holds (`Orders`), a record after what it is |
| No `@Autowired` fields, and no bean calls its own `@Transactional`, `@Async` or listener methods | Constructor injection; a call to itself bypasses Spring's proxy |
| No test is `@Disabled` | A switched-off test still names its requirement, so the spec would look tested |

Each violation names the class, the fullstack4j rules also say how to fix it, and `checkAll()` reports
every violation in one run. Test classes are left out, except from the rule on `@Disabled`. The rules
pass on a project that has no slices yet, such as a freshly generated one. Leave Taikai's
`failOnEmpty` off: on such a project, some of Taikai's own rules would fail with it.

The builder is an ordinary Taikai builder. Add rules of your own to it, or exclude classes from every
rule:

```java
Fullstack4jRules.builder(ShopApplication.class)
    .excludeClasses("com.acme.shop.legacy..")
    .java(java -> java.noUsageOf(java.util.Date.class))
    .build()
    .checkAll();
```

Taikai brings ArchUnit 1.5.1, and Maven resolves ArchUnit to that version instead of Spring Modulith's
1.4.2, in your application's runtime classpath too. The integration tests run Spring Modulith on it.

---

## Compatibility Matrix

| Starter | Spring Boot | Spring Modulith | Apache Wicket | Wicket Spring Boot Starter | Wicket Oat | Taikai | Java |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **`0.7.x`** (Current) | `4.1.x` | `2.1.x` | `10.11.x` | `0.3.x` | `0.6.x` | `2.0.x` | 17 to 25 |
| `0.6.x` | `4.1.x` | `2.1.x` | `10.11.x` | `0.2.x` | `0.6.x` | `2.0.x` | 17 to 25 |
| `0.5.x` | `4.1.x` | `2.1.x` | `10.11.x` | `0.2.x` | `0.5.x` | `2.0.x` | 17 to 25 |
| `0.4.x` | `4.1.x` | `2.1.x` | `10.11.x` | `0.2.x` | `0.4.x` | `2.0.x` | 17 to 25 |
| `0.3.x` | `4.1.x` | `2.1.x` | `10.11.x` | `0.2.x` | `0.4.x` | `2.0.x` | 17 to 25 |
| `0.2.x` | `4.1.x` | `2.1.x` | `10.11.x` | `0.2.x` | `0.4.x` | – | 17 to 25 |
| `0.1.x` | `4.1.x` | `2.1.x` | `10.11.x` | `0.2.x` | `0.3.x` | – | 17 to 25 |

Each Spring Modulith line belongs to one Spring Boot line (2.0 to Boot 4.0, 2.1 to Boot 4.1), so each
starter release supports one Spring Boot line. Java 26 and later are not supported yet: Wicket 10.11
can't create its `@SpringBean` proxies there. The [Wicket Spring Boot
Starter](https://github.com/danielbartl/wicket-spring-boot-starter#compatibility-matrix) explains why,
and this starter moves on when it does.

Compatibility is checked by the integration test under `fullstack4j-spring-boot-starter-test/src/it`:
a project shaped like one generated by Spring Initializr, inheriting from `spring-boot-starter-parent`
and depending on nothing but the two starters. It renders a page built from Wicket Oat components with
`@WicketTest`, sends an event from one Spring Modulith module to another through MongoDB, runs
`ApplicationModules.verify()` and the fullstack4j rules, and fails the build if two Wicket artifacts
resolve to different versions. Two smaller projects next to it test the rules themselves: one breaks
each rule once and has to fail with exactly those violations, and one is a freshly generated project
that has to pass. To check another Spring Boot 4.1 patch release, run from the root:

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
- **Spec traceability tests.** The [fullstack4j skill](https://github.com/danielbartl/fullstack4j-skill)
  ships `SpecTraceabilityTests`, which matches the requirements in each slice's spec to their tests.
  The spec format belongs to the skill, so the test does too.

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
