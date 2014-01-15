package jnode;

import org.apache.xmlrpc.XmlRpcException;

/**
 * Команды для работы с echomail
 * @author Kirill Temnenkov (ktemnenkov@intervale.ru)
 */
public interface EchomailTools {
    /**
     * Написать сообщение в эхоарию
     * @param areaname имя эхоарии
     * @param subject сабжект сообщения
     * @param text тело сообщения
     * @return пустая строка в случае успешного выполнения, описание ошибки в случае ошибки
     * @throws XmlRpcException
     */
    String writeEchomail(String areaname, String subject, String text) throws XmlRpcException;
    /**
     * Написать сообщение в эхоарию
     * @param areaname имя эхоарии
     * @param subject сабжект сообщения
     * @param text тело сообщения
     * @param fromName от кого
     * @param fromName кому
     * @return пустая строка в случае успешного выполнения, описание ошибки в случае ошибки
     * @throws XmlRpcException
     */
    String writeEchomail(String areaname, String subject, String text, String fromName, String toName) throws XmlRpcException;
}
