/*
 * Licensed to the jNode FTN Platform Develpoment Team (jNode Team)
 * under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for 
 * additional information regarding copyright ownership.  
 * The jNode Team licenses this file to you under the 
 * Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package jnode.jscript;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.script.*;

import jnode.dto.Jscript;
import jnode.dto.Schedule;
import jnode.dto.ScriptHelper;
import jnode.logger.Logger;
import jnode.main.MainHandler;
import jnode.orm.ORMManager;

/**
 * Запускатель пользовательских скриптов по расписанию
 * 
 * @author Manjago
 * 
 */
public class JscriptExecutor implements Runnable {
	private static final String JSCRIPT_ENABLE = "jscript.enable";
	private static final long MILLISEC_IN_HOUR = 3600000L;
	private static final Logger logger = Logger
			.getLogger(JscriptExecutor.class);

	public JscriptExecutor() {
		Date showDate = getNextLaunchDate();
		Date now = new Date();
		long initialDelay = showDate.getTime() - now.getTime();
		if (initialDelay < 0) {
			initialDelay = 0;
		}

		logger.l3("First jscriptExecutor will run at " + showDate
				+ " and every 1h after");
		new ScheduledThreadPoolExecutor(1).scheduleAtFixedRate(this,
				initialDelay, MILLISEC_IN_HOUR, TimeUnit.MILLISECONDS);
	}

	private static Date getNextLaunchDate() {
		Calendar calendar = Calendar.getInstance(Locale.US);
		calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR));
		calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY));
		calendar.set(Calendar.MINUTE, 1);
		calendar.set(Calendar.SECOND, 0);
		return new Date(calendar.getTime().getTime() + MILLISEC_IN_HOUR);
	}

	private static Bindings createBindings() {
		Bindings bindings = new SimpleBindings();
		for (ScriptHelper scriptHelper : ORMManager.get(ScriptHelper.class)
				.getAll()) {
			loadHelper(bindings, scriptHelper);
		}
		return bindings;
	}

	@SuppressWarnings("unchecked")
	private static void loadHelper(Bindings bindings, ScriptHelper scriptHelper) {
		Class<? super IJscriptHelper> clazz;
		String scriptHelperClassName = scriptHelper.getClassName();
		try {
			clazz = (Class<? super IJscriptHelper>) Class
					.forName(scriptHelperClassName);
			IJscriptHelper object = (IJscriptHelper) clazz.newInstance();
			bindings.put(scriptHelper.getId(), object);
			logger.l4("ScriptHelper " + object.toString() + " ("
					+ scriptHelper.getId() + ") loaded");
		} catch (ClassNotFoundException e) {
			logger.l2("Helper " + scriptHelperClassName + " not found");
		} catch (InstantiationException e) {
			logger.l2("Helper " + scriptHelperClassName
					+ " can't been initialized");
		} catch (IllegalAccessException e) {
			logger.l2("Helper " + scriptHelperClassName
					+ " can't been initialized (2)");
		} catch (ClassCastException e) {
			logger.l2("Helper " + scriptHelperClassName
					+ " is not IJscriptHelper");
		}
	}

	@Override
	public void run() {
		if (!MainHandler.getCurrentInstance().getBooleanProperty(
				JSCRIPT_ENABLE, true)) {
			return;
		}

		Calendar now = Calendar.getInstance(Locale.US);
		List<Schedule> items = ORMManager.get(Schedule.class).getAll();
		logger.l5(MessageFormat.format("{0} items in queue", items.size()));
		if (items.size() == 0) {
			return;
		}
		tryRunItemsByDate(now, items);

	}

	private static void tryRunItemsByDate(Calendar now, List<Schedule> items) {
		final ScriptEngine engine = createScriptEngine();
		final Bindings bindings = createBindings();
		for (Schedule item : items) {
			if (item.isNeedExec(now)) {
				try {
					executeScript(engine, item, bindings);
				} catch (ScriptException e) {
					logger.l2(MessageFormat.format("fail script {0} execution",
							item.getJscript().getId()), e);
				} catch (Exception e) {
					logger.l2(MessageFormat.format(
							"unexpected fail script {0} execution", item
									.getJscript().getId()), e);
				}
				logger.l5(MessageFormat.format("executed script {0}", item
						.getJscript().getId()));
			}
		}
	}

	private static void executeScript(ScriptEngine engine, Schedule item,
			Bindings bindings) throws ScriptException {
		if (item.getJscript() != null && item.getJscript().getId() != null) {

			String content = ORMManager.get(Jscript.class)
					.getById(item.getJscript().getId()).getContent();
			if (content != null) {
				logger.l5(MessageFormat.format("execute script {0}", content));
				engine.eval(content, bindings);
				// выполнились? и иксипшена не произошло? ну вот это счастье!
				Schedule modItem = ORMManager.get(Schedule.class).getById(
						item.getId());
				modItem.setLastRunDate(new Date());
				ORMManager.get(Schedule.class).update(modItem);
			}

		}
	}

	/**
	 * Выполнить скрипт с идентификатором id
	 * 
	 * @param id
	 *            идентификатор скрипта
	 * @return отчет об ошибках
	 */
	public static String executeScript(Long id) {

		if (!MainHandler.getCurrentInstance().getBooleanProperty(
				JSCRIPT_ENABLE, true)) {
			return "Script execution disabled";
		}

		try {
			Jscript jscript = ORMManager.get(Jscript.class).getById(id);
			if (jscript == null) {
				return MessageFormat.format("Script with id {0} not found", id);
			}
			String content = jscript.getContent();
			if (content == null) {
				return MessageFormat.format(
						"Null content of script with id {0}", id);
			}

            internalExecuteScript(content);

		} catch (Exception ex) {
			logger.l4(
					MessageFormat.format("fail execute script with id {0}", id),
					ex);
			return "fail execute script: " + ex.getMessage();
		}

		return null;
	}

    /**
     * Выполнить скрипт
     *
     * @param content
     *            собстна скрипт
     * @return отчет об ошибках
     */
    public static String executeScript(String content) {

        if (!MainHandler.getCurrentInstance().getBooleanProperty(
                JSCRIPT_ENABLE, true)) {
            return "Script execution disabled";
        }

        return execScript(content);
    }

    static String execScript(String content) {
        try {
            if (content == null) {
                return "Null content of script";
            }

            internalExecuteScript(content);

        } catch (Exception ex) {
            logger.l4(
                    "fail execute script",
                    ex);
            return "fail execute script: " + ex.getMessage();
        }

        return null;
    }

    private static void internalExecuteScript(String content) throws ScriptException {
        final ScriptEngine engine = createScriptEngine();
        final Bindings bindings = createBindings();
        logger.l5(MessageFormat
                .format("custom execute script {0}", content));
        engine.eval(content, bindings);
    }

    private static ScriptEngine createScriptEngine() {
		return new ScriptEngineManager().getEngineByName("javascript");
	}
}
