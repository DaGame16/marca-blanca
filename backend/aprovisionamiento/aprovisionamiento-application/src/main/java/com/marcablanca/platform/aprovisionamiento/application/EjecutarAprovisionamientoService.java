package com.marcablanca.platform.aprovisionamiento.application;

import com.marcablanca.platform.aprovisionamiento.application.port.in.EjecutarAprovisionamiento;
import com.marcablanca.platform.aprovisionamiento.application.port.out.PasosDeAprovisionamiento;
import com.marcablanca.platform.aprovisionamiento.application.port.out.RepositorioEmpresas;
import com.marcablanca.platform.aprovisionamiento.application.port.out.RepositorioTareasDeAprovisionamiento;
import com.marcablanca.platform.aprovisionamiento.domain.Empresa;
import com.marcablanca.platform.aprovisionamiento.domain.EmpresaRegistrada;
import com.marcablanca.platform.aprovisionamiento.domain.PasoDeAprovisionamiento;
import com.marcablanca.platform.aprovisionamiento.domain.TareaDeAprovisionamiento;

import java.util.List;
import java.util.Objects;

/**
 * Capa 2. Ejecuta el pipeline paso a paso, guardando el checkpoint despues de
 * cada uno. En un reintento arranca desde el ultimo paso completado, no desde cero.
 * Cada paso de PasosDeAprovisionamiento es idempotente, asi que re-ejecutar el
 * ultimo paso (si se cayo justo despues de hacerlo pero antes de guardar) es seguro.
 */
public class EjecutarAprovisionamientoService implements EjecutarAprovisionamiento {

    private final RepositorioEmpresas repositorioEmpresas;
    private final RepositorioTareasDeAprovisionamiento repositorioTareas;
    private final PasosDeAprovisionamiento pasos;

    public EjecutarAprovisionamientoService(RepositorioEmpresas repositorioEmpresas,
                                            RepositorioTareasDeAprovisionamiento repositorioTareas,
                                            PasosDeAprovisionamiento pasos) {
        this.repositorioEmpresas = repositorioEmpresas;
        this.repositorioTareas = repositorioTareas;
        this.pasos = pasos;
    }

    @Override
    public void ejecutar(EmpresaRegistrada evento) {
        Empresa empresa = repositorioEmpresas.buscarPorId(evento.empresaId())
                .orElseThrow(() -> new IllegalStateException(
                        "No existe la empresa " + evento.empresaId() + " que el evento dice registrar."));

        TareaDeAprovisionamiento tarea = repositorioTareas.buscarPorEmpresa(evento.empresaId())
                .orElseGet(() -> TareaDeAprovisionamiento.iniciar(
                        evento.empresaId(), empresa.getIdentificador().nombreBaseDeDatos()));

        if (tarea.agotoReintentos()) {
            return; // quedo en ERROR terminal: no se reintenta en automatico, requiere intervencion
        }

        List<PasoEjecutable> plan = List.of(
                new PasoEjecutable(PasoDeAprovisionamiento.BASE_CREADA,
                        () -> pasos.crearBaseDeDatos(empresa)),
                new PasoEjecutable(PasoDeAprovisionamiento.SEMILLA_APLICADA,
                        () -> pasos.aplicarSemilla(empresa)),
                new PasoEjecutable(PasoDeAprovisionamiento.ROLES_CREADOS,
                        () -> pasos.crearRolesDeTenant(empresa)),
                new PasoEjecutable(PasoDeAprovisionamiento.CONEXION_REGISTRADA,
                        () -> pasos.registrarConexion(empresa)),
                new PasoEjecutable(PasoDeAprovisionamiento.VERSION_REGISTRADA,
                        () -> pasos.registrarVersionDeEsquema(empresa)),
                new PasoEjecutable(PasoDeAprovisionamiento.MODULOS_POBLADOS,
                        () -> pasos.poblarModulos(empresa, evento.modulosSolicitados())),
                new PasoEjecutable(PasoDeAprovisionamiento.EMPRESA_ACTIVADA,
                        () -> { empresa.activar(); repositorioEmpresas.guardar(empresa); })
        );

        try {
            // El indice de arranque sale del ordinal del ultimo paso completado:
            // NO_INICIADO(0) -> plan[0]; BASE_CREADA(1) -> plan[1]; etc.
            for (int i = tarea.getPaso().ordinal(); i < plan.size(); i++) {
                PasoEjecutable paso = plan.get(i);
                paso.accion().run();
                tarea.avanzarA(paso.marca());
                repositorioTareas.guardar(tarea);
            }
        } catch (RuntimeException e) {
            tarea.registrarFallo(e.getMessage());
            repositorioTareas.guardar(tarea);
            throw e; // que el sondeador lo vea y marque el evento del outbox
        }
    }

    private record PasoEjecutable(PasoDeAprovisionamiento marca, Runnable accion) {
        private PasoEjecutable {
            Objects.requireNonNull(marca);
            Objects.requireNonNull(accion);
        }
    }
}