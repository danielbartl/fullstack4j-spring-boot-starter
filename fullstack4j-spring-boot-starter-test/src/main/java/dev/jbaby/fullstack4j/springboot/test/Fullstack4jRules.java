/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.jbaby.fullstack4j.springboot.test;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.enofex.taikai.Taikai;
import com.enofex.taikai.TaikaiRule;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaConstructorCall;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.properties.CanBeAnnotated;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import org.apache.wicket.Component;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.repository.Repository;
import org.springframework.modulith.events.ApplicationModuleListener;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.assignableTo;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.SimpleConditionEvent.violated;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;

/**
 * The rules every fullstack4j application follows, checked with
 * <a href="https://github.com/enofex/taikai">Taikai</a>.
 * <p>
 * Spring Modulith's {@code ApplicationModules.verify()} keeps slices out of each other's
 * {@code internal} packages and free of cycles. These rules check what it leaves alone: how slices
 * are named, what a slice's public package holds, where documents live, how events travel, what
 * Wicket components hold, and that no test is switched off. Run both:
 * </p>
 * <pre class="code">
 * class ArchitectureTests {
 *
 *     &#064;Test
 *     void slicesStayOutOfEachOthersInternals() {
 *         ApplicationModules.of(ShopApplication.class).verify();
 *     }
 *
 *     &#064;Test
 *     void followsTheFullstack4jRules() {
 *         Fullstack4jRules.builder(ShopApplication.class)
 *             .build()
 *             .checkAll();
 *     }
 * }
 * </pre>
 * <p>
 * A slice is a direct subpackage of the application's package, as in Spring Modulith. The rules:
 * </p>
 * <ul>
 * <li>A slice is named after a business capability, as one lowercase word, and below it there is
 * only its {@code internal} package.</li>
 * <li>A slice's base package holds only its public API: Wicket components, interfaces, records and
 * enums.</li>
 * <li>MongoDB documents live in a slice's {@code internal} package.</li>
 * <li>Wicket components and a slice's base package use no repositories, no Spring Data and no
 * MongoDB driver.</li>
 * <li>An {@code @ApplicationModuleListener} listens to a record, never to a document.</li>
 * <li>Only the slice that owns an event creates it.</li>
 * <li>Wicket components hold no documents, and no records that aren't {@link Serializable}.</li>
 * <li>Wicket components carry no Spring annotations, which only take effect on Spring beans.</li>
 * <li>No class is named {@code *Service}, {@code *Impl} or {@code *Dto}.</li>
 * <li>No field is injected with {@code @Autowired}, and no method calls its own
 * {@code @Transactional}, {@code @Async} or listener methods, which would bypass Spring's proxy.</li>
 * <li>No test is {@code @Disabled}.</li>
 * </ul>
 * <p>
 * Each violation names the class, the fullstack4j rules also say how to fix it, and
 * {@link Taikai#checkAll()} reports them all in one run. The builder is an ordinary {@link Taikai.Builder}: add rules of your own to it,
 * or exclude classes from every rule with {@link Taikai.Builder#excludeClasses(String...)}. The
 * fullstack4j rules pass on an application that has nothing to check yet, such as a freshly
 * generated one. Leave {@link Taikai.Builder#failOnEmpty(boolean) failOnEmpty} off, since some of
 * Taikai's own rules would then fail there.
 * </p>
 *
 * @author Daniel Bartl
 */
public final class Fullstack4jRules {

	private static final Set<String> TECHNICAL_NAMES = Set.of("api", "common", "config", "core", "domain",
			"dto", "entity", "infrastructure", "model", "persistence", "repository", "service", "services",
			"shared", "ui", "util", "utils", "view", "web");

	private static final List<String> SPRING_BEAN_ANNOTATIONS = List.of(
			"org.springframework.stereotype.Component",
			"org.springframework.transaction.annotation.Transactional",
			"jakarta.transaction.Transactional",
			"org.springframework.scheduling.annotation.Async",
			"org.springframework.scheduling.annotation.Scheduled",
			"org.springframework.context.event.EventListener",
			"org.springframework.cache.annotation.Cacheable",
			"org.springframework.cache.annotation.CachePut",
			"org.springframework.cache.annotation.CacheEvict");

	private Fullstack4jRules() {
	}

	/**
	 * Returns a Taikai builder that checks the fullstack4j rules on the application's package and
	 * everything below it, test classes excluded except for the rule on {@code @Disabled}.
	 * @param applicationClass the {@code @SpringBootApplication} class, whose package holds the
	 * slices
	 * @return a builder holding the rules, ready to {@link Taikai.Builder#build() build} or to extend
	 */
	public static Taikai.Builder builder(Class<?> applicationClass) {
		Objects.requireNonNull(applicationClass, "applicationClass");
		String base = applicationClass.getPackageName();
		if (base.isEmpty()) {
			throw new IllegalArgumentException(applicationClass.getName() + " is in the default package");
		}
		return Taikai.builder()
			.namespace(base)
			.java((java) -> java
				.naming((naming) -> naming
					.classesShouldNotMatch(".*(Service|Impl|Dto)")))
			.spring((spring) -> spring
				.noAutowiredFields()
				.noSelfInvocationOfProxiedMethods())
			.test((test) -> test
				.junit((junit) -> junit
					.classesShouldNotBeAnnotatedWithDisabled()
					.methodsShouldNotBeAnnotatedWithDisabled()))
			.addRules(rules(base).stream().map(TaikaiRule::of).toList());
	}

	static List<ArchRule> rules(String base) {
		return List.of(slicesAreBusinessCapabilities(base), slicesShowOnlyTheirApi(base),
				documentsStayInternal(base), dataStaysBehindTheFacade(base), listenersHearRecords(),
				onlyTheOwningSliceCreatesItsEvents(base), componentsHoldNoDocuments(),
				componentsAreNotSpringBeans());
	}

	static ArchRule slicesAreBusinessCapabilities(String base) {
		return classes()
			.should(new ArchCondition<JavaClass>("be in the root package, in a slice or in its internal package") {
				@Override
				public void check(JavaClass type, ConditionEvents events) {
					String packageName = type.getPackageName();
					if (!packageName.startsWith(base + ".")) {
						return;
					}
					String[] segments = packageName.substring(base.length() + 1).split("\\.");
					String slice = segments[0];
					if (!slice.matches("[a-z][a-z0-9]*") || TECHNICAL_NAMES.contains(slice)) {
						events.add(violated(type, type.getName() + " is in slice " + slice
								+ ", which isn't named after a business capability"));
					}
					else if (segments.length > 1 && !segments[1].equals("internal")) {
						events.add(violated(type, type.getName() + " is in " + slice + "." + segments[1]
								+ ", but below a slice there is only " + slice + ".internal"));
					}
				}
			})
			.because("a slice is one business capability, named as one lowercase word such as ordering or "
					+ "billing, and everything but its public API goes in <slice>.internal")
			.allowEmptyShould(true);
	}

	static ArchRule slicesShowOnlyTheirApi(String base) {
		return classes()
			.that(areInASliceBasePackage(base))
			.and().areTopLevelClasses()
			.and().doNotHaveSimpleName("package-info")
			.should(new ArchCondition<JavaClass>("be Wicket components, interfaces, records or enums") {
				@Override
				public void check(JavaClass type, ConditionEvents events) {
					if (!(type.isAssignableTo(Component.class) || type.isInterface() || type.isEnum()
							|| type.isAssignableTo(Record.class))) {
						events.add(violated(type, type.getName() + " is in the base package of "
								+ sliceOf(type, base) + ", but is no Wicket component, interface, record or enum"));
					}
				}
			})
			.because("a slice's base package is its public API: pages and other components, events, and "
					+ "interfaces with the records and enums they use; move everything else to <slice>.internal")
			.allowEmptyShould(true);
	}

	static ArchRule documentsStayInternal(String base) {
		return classes()
			.that().areAnnotatedWith(Document.class)
			.should().resideInAPackage(base + ".*.internal..")
			.because("a document never leaves its slice; give other slices a record or an event instead")
			.allowEmptyShould(true);
	}

	static ArchRule dataStaysBehindTheFacade(String base) {
		return noClasses()
			.that(areInASliceBasePackage(base).or(assignableTo(Component.class)))
			.should().dependOnClassesThat(assignableTo(Repository.class)
				.or(resideInAnyPackage("org.springframework.data..", "com.mongodb..", "org.bson.."))
				.as("are repositories or belong to Spring Data or the MongoDB driver"))
			.because("pages and a slice's public API reach data only through the slice's facade in "
					+ "<slice>.internal")
			.allowEmptyShould(true);
	}

	static ArchRule listenersHearRecords() {
		return methods()
			.that(areListeners())
			.should(new ArchCondition<JavaMethod>("listen to a record that isn't a document") {
				@Override
				public void check(JavaMethod listener, ConditionEvents events) {
					JavaClass event = eventOf(listener);
					if (event == null) {
						return;
					}
					if (!event.isAssignableTo(Record.class)) {
						events.add(violated(listener, listener.getFullName() + " listens to " + event.getName()
								+ ", which isn't a record"));
					}
					else if (event.isAnnotatedWith(Document.class)) {
						events.add(violated(listener, listener.getFullName() + " listens to " + event.getName()
								+ ", which is a document"));
					}
				}
			})
			.because("an event is an immutable record that its slice publishes; documents never travel")
			.allowEmptyShould(true);
	}

	static ArchRule onlyTheOwningSliceCreatesItsEvents(String base) {
		return classes()
			.should(new ArchCondition<JavaClass>("create only their own slice's events") {

				private final Set<String> heard = new HashSet<>();

				@Override
				public void init(Collection<JavaClass> allClasses) {
					this.heard.clear();
					for (JavaClass type : allClasses) {
						for (JavaMethod method : type.getMethods()) {
							JavaClass event = areListeners().test(method) ? eventOf(method) : null;
							if (event != null) {
								this.heard.add(event.getName());
							}
						}
					}
				}

				@Override
				public void check(JavaClass type, ConditionEvents events) {
					for (JavaConstructorCall call : type.getConstructorCallsFromSelf()) {
						JavaClass event = call.getTargetOwner();
						if (this.heard.contains(event.getName()) && !sliceOf(event, base).equals(sliceOf(type, base))) {
							events.add(violated(call, call.getOrigin().getFullName() + " creates " + event.getName()
									+ ", an event of " + owner(event, base)));
						}
					}
				}
			})
			.because("only the slice that owns an event publishes it; to make another slice act, call an "
					+ "interface it offers")
			.allowEmptyShould(true);
	}

	static ArchRule componentsHoldNoDocuments() {
		return noFields()
			.that().areDeclaredInClassesThat().areAssignableTo(Component.class)
			.and().areNotStatic()
			.should().haveRawType(DescribedPredicate.describe("a document or a record that isn't Serializable",
					(type) -> type.isAnnotatedWith(Document.class)
							|| (type.isAssignableTo(Record.class) && !type.isAssignableTo(Serializable.class))))
			.because("Wicket stores components serialised between requests; keep an id or a "
					+ "LoadableDetachableModel, and map another slice's records to a small Serializable record")
			.allowEmptyShould(true);
	}

	static ArchRule componentsAreNotSpringBeans() {
		return classes()
			.that().areAssignableTo(Component.class)
			.should(new ArchCondition<JavaClass>("carry no Spring annotations") {
				@Override
				public void check(JavaClass component, ConditionEvents events) {
					for (String annotation : SPRING_BEAN_ANNOTATIONS) {
						if (carries(component, annotation)) {
							events.add(violated(component, component.getName() + " is annotated with @"
									+ simpleName(annotation)));
						}
						for (JavaMethod method : component.getMethods()) {
							if (carries(method, annotation)) {
								events.add(violated(method, method.getFullName() + " is annotated with @"
										+ simpleName(annotation)));
							}
						}
					}
				}
			})
			.because("Wicket creates components itself, so Spring never proxies them; inject the facade with "
					+ "@SpringBean and put transactions, events and async work on it")
			.allowEmptyShould(true);
	}

	private static DescribedPredicate<JavaClass> areInASliceBasePackage(String base) {
		return DescribedPredicate.describe("are in a slice's base package", (type) -> {
			String packageName = type.getPackageName();
			return packageName.startsWith(base + ".") && packageName.indexOf('.', base.length() + 1) < 0;
		});
	}

	private static DescribedPredicate<JavaMethod> areListeners() {
		return DescribedPredicate.describe("are annotated with @ApplicationModuleListener",
				(method) -> carries(method, ApplicationModuleListener.class.getName()));
	}

	private static JavaClass eventOf(JavaMethod listener) {
		List<JavaClass> parameters = listener.getRawParameterTypes();
		return parameters.isEmpty() ? null : parameters.get(0);
	}

	private static String sliceOf(JavaClass type, String base) {
		String packageName = type.getPackageName();
		if (!packageName.startsWith(base + ".")) {
			return "";
		}
		String rest = packageName.substring(base.length() + 1);
		int dot = rest.indexOf('.');
		return (dot < 0) ? rest : rest.substring(0, dot);
	}

	private static String owner(JavaClass event, String base) {
		String slice = sliceOf(event, base);
		return slice.isEmpty() ? "the root package" : slice;
	}

	private static boolean carries(CanBeAnnotated element, String annotation) {
		return element.isAnnotatedWith(annotation) || element.isMetaAnnotatedWith(annotation);
	}

	private static String simpleName(String annotation) {
		return annotation.substring(annotation.lastIndexOf('.') + 1);
	}
}
