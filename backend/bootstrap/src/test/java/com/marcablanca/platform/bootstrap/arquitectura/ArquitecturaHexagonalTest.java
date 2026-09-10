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
        ArchRule regla = noClasses()
                .that().resideInAPackage("..omnicanal..")
                .should().dependOnClassesThat().resideInAPackage("..usuarios..");

        regla.check(clases);
    }

    @Test
    void omnicanal_dominio_y_aplicacion_no_dependen_de_otros_contextos() {
        // El nucleo de omnicanal solo habla con el exterior por sus puertos. Los
        // unicos acoplamientos permitidos -- ContextoEmpresaActual (empresas) y el
        // gate del modulo (modulos-empresa) -- son adaptadores en infrastructure,
        // nunca aca.
        ArchRule regla = noClasses()
                .that().resideInAnyPackage(
                        "..omnicanal.domain..", "..omnicanal.application..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..empresas..", "..modulosempresa..", "..identidadvisual..",
                        "..usuarios..", "..autenticacion..", "..aprovisionamiento..",
                        "..consola..", "..correo..");

        regla.check(clases);
    }

    @Test
    void aprovisionamiento_dominio_y_aplicacion_no_dependen_de_otros_contextos() {
        // El nucleo del contexto de aprovisionamiento solo habla con el exterior a
        // traves de sus puertos. El unico acoplamiento permitido hacia modulos-empresa
        // es el adaptador ACL, y vive en infrastructure (no aca).
        ArchRule regla = noClasses()
                .that().resideInAnyPackage(
                        "..aprovisionamiento.domain..", "..aprovisionamiento.application..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..empresas..", "..modulosempresa..", "..identidadvisual..",
                        "..usuarios..", "..autenticacion..", "..omnicanal..");

        regla.check(clases);
    }

    @Test
    void consola_dominio_y_aplicacion_no_dependen_de_otros_contextos() {
        // La consola de operacion solo habla con el exterior por sus puertos. El
        // acoplamiento hacia aprovisionamiento (re-aprovisionar, etc.) sera un
        // adaptador ACL en infrastructure, nunca aca.
        ArchRule regla = noClasses()
                .that().resideInAnyPackage(
                        "..consola.domain..", "..consola.application..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..empresas..", "..modulosempresa..", "..identidadvisual..",
                        "..usuarios..", "..autenticacion..", "..aprovisionamiento..",
                        "..omnicanal..", "..correo..")
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
