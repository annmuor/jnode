package jnode;

import org.apache.xmlrpc.XmlRpcException;

/**
 * Команды для работы со скриптами
 * @author Kirill Temnenkov (ktemnenkov@intervale.ru)
 */
public interface Scripter {
    /**
     * Выполнить скрипт с идентификаторором id. В случае успешного выполнения возвращается пустая строка,
     * в случае ошибки - возвращается строка с описанием ошибки
     * @param id идентификатор скрипта. Передается как строка (издержки XML-RPC), на самом деле Long
     * @return пустая строка в случае успешного выполнения, описание ошибки в случае ошибки
     * @throws XmlRpcException
     */
    String run(String id) throws XmlRpcException;
}
