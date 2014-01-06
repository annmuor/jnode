package jnode.extenal;

import jnode.jscript.JscriptExecutor;

/**
 * API для вызовов снаружи
 * @author Kirill Temnenkov (kirill@temnenkov.com)
 */
public final class Api {
    private Api() {
    }

    /**
     * Выполнить скрипт с идентификаторором id. В случае успешного выполнения возвращается null,
     * в случае ошибки - возвращается строка с описанием ошибки
     * @param id идентификатор скрипта
     * @return null в случае успешного выполнения, описание ошибки в случае ошибки
     */
    public static String executeScript(long id){
        return JscriptExecutor.executeScript(id);
    }
}
