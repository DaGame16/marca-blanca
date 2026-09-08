package com.marcablanca.platform.omnicanal.infrastructure.web;

import java.util.HashMap;

/**
 * Se recibe como Map libre (no un DTO estricto) a proposito -- LIWA manda
 * campos variables segun el flujo (user_id, chat_history_details_large,
 * contact_name, ads, etc.) y el payload completo se persiste igual en
 * datos_crudos para auditoria, sin importar que campos exactos traiga.
 */
public class OmnicanalWebhookRequest extends HashMap<String, Object> {
}
