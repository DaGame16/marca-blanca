package com.marcablanca.platform.omnicanal.application.port.out;

/**
 * Avisa en tiempo real que hay datos nuevos de Omnicanal (conversacion
 * archivada o analisis IA terminado) para que el frontend refresque el panel
 * sin esperar al polling. Un solo metodo a proposito: el frontend no
 * distingue el tipo de evento, solo invalida su cache y vuelve a pedir datos
 * (mismo criterio que useLiwaEvento() en la version anterior del panel).
 *
 * A que empresa le llega el aviso lo decide el adaptador (infrastructure),
 * leyendo ContextoEmpresaActual -- este puerto no conoce ese concepto (ver
 * ArquitecturaHexagonalTest: omnicanal.application no puede depender de
 * empresas).
 */
public interface NotificadorEventosOmnicanal {
    void notificarCambio();
}
