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

import jnode.dto.Jscript;
import jnode.dto.Schedule;
import jnode.dto.ScriptHelper;
import jnode.logger.Logger;
import jnode.main.MainHandler;
import jnode.orm.ORMManager;

import javax.script.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Запускатель пользовательских скриптов по расписанию
 * 
 * @author Manjago
 * 
 */
public class JscriptExecutor implements Runnable {
    private static final String JSCRIPT_ENABLE = "jscript.enable";
    private static final String JSCRIPT_ENGINE = "jscript.engine";
	private static final long MILLISEC_IN_HOUR = 3600000L;
	private static final Logger logger = Logger
			.getLogger(JscriptExecutor.class);

	public JscriptExecutor() {
		Date now = new Date();
		long initialDelay = 60000L;
        Date showDate = new Date(now.getTime() + initialDelay);

		logger.l3("First jscriptExecutor will run at " + showDate
				+ " and every 1h after");
		new ScheduledThreadPoolExecutor(1).scheduleAtFixedRate(this,
				initialDelay, MILLISEC_IN_HOUR, TimeUnit.MILLISECONDS);
	}

	public static Bindings createBindings() {
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
			logger.l2("Helper " + scriptHelperClassName + " not found", e);
		} catch (InstantiationException | IllegalAccessException e) {
			logger.l2("Helper " + scriptHelperClassName
					+ " can't been initialized", e);
		} catch (ClassCastException e) {
			logger.l2("Helper " + scriptHelperClassName
					+ " is not IJscriptHelper", e);
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
		logger.l5(MessageFormat.format("{0} pretenders", items.size()));
		if (items.isEmpty()) {
			return;
		}

        List<Schedule> needExec = queryNeedExec(now, items);
        logger.l5(MessageFormat.format("{0} items in queue", needExec.size()));

        if (!needExec.isEmpty()) {
            tryRunItems(needExec);
        }

	}

    private List<Schedule> queryNeedExec(Calendar now, List<Schedule> items) {
        List<Schedule> needExec = new ArrayList<>();
        for (Schedule item : items) {
            if (item.isNeedExec(now)){
                needExec.add(item);
            }
        }
        return needExec;
    }

    private static void tryRunItems(List<Schedule> items) {
		final ScriptEngine engine = createScriptEngine();
		final Bindings bindings = createBindings();
		for (Schedule item : items) {
                logger.l5(MessageFormat.format("need process {0}", item));
				try {
					executeScript(engine, item, bindings);
				} catch (ScriptException e) {
					logger.l2(MessageFormat.format("fail script {0} execution",
							item.getJscript().getId()), e);
					continue;
				} catch (Exception e) {
					logger.l2(MessageFormat.format(
							"unexpected fail script {0} execution", item
									.getJscript().getId()), e);
                    continue;
				}
				logger.l5(MessageFormat.format("executed script {0}", item
						.getJscript().getId()));
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
                final Date lastRunDate = new Date();
                modItem.setLastRunDate(lastRunDate);
                final Date nextRunDate = modItem.queryNextRunDate();
                modItem.setNextRunDate(nextRunDate);
                logger.l5(MessageFormat.format("script {0} executed SUCCESS, last run date {1}, next run date {2}", content, lastRunDate, nextRunDate));
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

    public static String executeScript(String content, Bindings appendBindings, boolean force) {

        if (!force && !MainHandler.getCurrentInstance().getBooleanProperty(
                JSCRIPT_ENABLE, true)) {
            return "Script execution disabled";
        }

        Bindings bindings = force ? new SimpleBindings() : createBindings();
        if (appendBindings != null){
            bindings.putAll(appendBindings);
        }
        return execScript(content, bindings);
    }

    static String execScript(String content, Bindings bindings) {
        try {
            if (content == null) {
                return "Null content of script";
            }

            internalExecuteScript(content, bindings);

        } catch (Exception ex) {
            logger.l4(
                    "fail execute script",
                    ex);
            return "fail execute script: " + ex.getMessage();
        }

        return null;
    }

    private static void internalExecuteScript(String content) throws ScriptException {
        internalExecuteScript(content, null);
    }

    private static void internalExecuteScript(String content, Bindings bindings) throws ScriptException {
        final ScriptEngine engine = createScriptEngine();
        final Bindings binds = bindings != null ? bindings : createBindings();
        logger.l5(MessageFormat
                .format("custom execute script {0}", content));
        engine.eval(content, binds);
    }

    private static ScriptEngine createScriptEngine() {
        return new ScriptEngineManager().getEngineByName(
                MainHandler.getCurrentInstance() != null ?
                        MainHandler.getCurrentInstance().getProperty(JSCRIPT_ENGINE, "javascript")
                        : "javascript"
        );
    }
}
