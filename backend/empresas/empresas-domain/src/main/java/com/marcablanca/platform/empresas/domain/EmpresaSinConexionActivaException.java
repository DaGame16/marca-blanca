package com.marcablanca.platform.empresas.domain;

public class EmpresaSinConexionActivaException extends RuntimeException {

    public EmpresaSinConexionActivaException(String identificadorEmpresa) {
        super("No hay una conexion activa para la empresa: " + identificadorEmpresa);
    }
}
