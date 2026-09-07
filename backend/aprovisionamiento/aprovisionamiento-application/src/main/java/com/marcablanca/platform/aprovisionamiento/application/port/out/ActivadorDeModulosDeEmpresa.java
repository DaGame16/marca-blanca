package com.marcablanca.platform.aprovisionamiento.application.port.out;

import java.util.UUID;

/**
 * Puerto de salida hacia el contexto de modulos-empresa. El pipeline pide "activa
 * este modulo para esta empresa" sin saber como se persiste eso -- la implementacion
 * (un adaptador Anti-Corruption Layer en infraestructura) delega en el puerto de
 * entrada publico de modulos-empresa, sin importar su modelo interno.
 */
public interface ActivadorDeModulosDeEmpresa {
    void activar(UUID empresaId, String codigoModulo);
}
