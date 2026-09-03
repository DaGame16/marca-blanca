package com.marcablanca.platform.bootstrap.arquitectura;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Verifica que el codigo respete la arquitectura hexagonal acordada:
 *  - domain no depende de nada (ni de application/infrastructure, ni de
 *    frameworks como Spring o Jakarta).
 *  - application no depende de infrastructure.
 *  - los bounded contexts "usuarios" y "omnicanal" estan aislados entre si.
 *
 * Corre como parte de "mvn test" en el modulo bootstrap, porque es el unico
 * modulo que tiene en su classpath el codigo de todos los demas.
 */
class ArquitecturaHexagonalTest {

    private static final String BASE_PACKAGE = "com.marcablanca.platform";

    private static JavaClasses clases;

    @BeforeAll
    static void importarClases() {
        clases = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(BASE_PACKAGE);
    }

    @Test
    void el_dominio_no_depende_de_application_ni_infrastructure() {
        ArchRule regla = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage("..application..", "..infrastructure..");

        regla.check(clases);
    }

    @Test
    void el_dominio_no_depende_de_frameworks() {
        ArchRule regla = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("org.springframework..", "jakarta..", "javax..");

        regla.check(clases);
    }

    @Test
    void application_no_depende_de_infrastructure() {
        ArchRule regla = noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure..");

        regla.check(clases);
    }

    @Test
    void usuarios_no_depende_de_omnicanal() {
        ArchRule regla = noClasses()
                .that().resideInAPackage("..usuarios..")
                .should().dependOnClassesThat().resideInAPackage("..omnicanal..");

        regla.check(clases);
    }

    @Test
    void omnicanal_no_depende_de_usuarios() {
        // allowEmptyShould: hoy "omnicanal" todavia no tiene clases (modulos
        // vacios, en construccion). La regla queda lista para cuando se
        // empiece a escribir codigo ahi, sin fallar mientras tanto por
        // "0 clases evaluadas".
        ArchRule regla = noClasses()
                .that().resideInAPackage("..omnicanal..")
                .should().dependOnClassesThat().resideInAPackage("..usuarios..")
                .allowEmptyShould(true);

        regla.check(clases);
    }

    @Test
    void las_excepciones_de_dominio_terminan_en_exception() {
        ArchRule regla = classes()
                .that().resideInAPackage("..domain..")
                .and().areAssignableTo(RuntimeException.class)
                .should().haveSimpleNameEndingWith("Exception");

        regla.check(clases);
    }
}
