package architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * The Clean Architecture rules, checked on every build. A rule that fails names each class and
 * the dependency that broke it.
 *
 * <p>Only production code is checked; tests may use anything.
 */
@AnalyzeClasses(
    packages = {"entity", "usecase", "interfaceadapter", "dataaccess", "view", "app"},
    importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

  /** Entities are plain Java: they know nothing about use cases, storage, the web or the UI. */
  @ArchTest
  static final ArchRule entities_onlyDependOnJava =
      classes()
          .that()
          .resideInAPackage("entity..")
          .should()
          .onlyDependOnClassesThat()
          .resideInAnyPackage("entity..", "java..");

  /** Use cases work with entities and the interfaces they own, never with the outer layers. */
  @ArchTest
  static final ArchRule useCases_onlyDependOnEntitiesAndJava =
      classes()
          .that()
          .resideInAPackage("usecase..")
          .should()
          .onlyDependOnClassesThat()
          .resideInAnyPackage("usecase..", "entity..", "java..");

  /**
   * The two inner layers don't touch the UI, the network, JSON, the database or files, even
   * though those packages start with {@code java.} or come with the build.
   */
  @ArchTest
  static final ArchRule innerLayers_dontUseUiNetworkJsonDatabaseOrFiles =
      noClasses()
          .that()
          .resideInAnyPackage("entity..", "usecase..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(
              "javax.swing..", "java.awt..", "okhttp3..", "org.json..", "java.sql..", "java.io..");

  /**
   * Screens reach data only through controllers and use cases. Allowed to be empty until the
   * first screen is written.
   */
  @ArchTest
  static final ArchRule views_dontUseDataAccess =
      noClasses()
          .that()
          .resideInAPackage("view..")
          .should()
          .dependOnClassesThat()
          .resideInAPackage("dataaccess..")
          .allowEmptyShould(true);
}
