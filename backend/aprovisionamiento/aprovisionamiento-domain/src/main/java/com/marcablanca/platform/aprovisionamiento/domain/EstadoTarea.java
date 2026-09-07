package com.marcablanca.platform.aprovisionamiento.domain;

/** Estado de alto nivel de una tarea de aprovisionamiento. Coincide con ck_aprovisionamiento_tareas_estado. */
public enum EstadoTarea {
    PENDIENTE,
    EN_PROGRESO,
    COMPLETADO,
    ERROR
}