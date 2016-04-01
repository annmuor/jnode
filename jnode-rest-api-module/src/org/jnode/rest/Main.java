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

package org.jnode.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import jnode.event.IEvent;
import jnode.logger.Logger;
import jnode.module.JnodeModule;
import jnode.module.JnodeModuleException;
import org.jnode.rest.core.BadJsonException;
import org.jnode.rest.core.StringUtils;
import org.jnode.rest.dto.Message;
import org.jnode.rest.dto.MessageId;
import org.jnode.rest.mapper.MessageIdMapper;
import org.jnode.rest.mapper.MessageMapper;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

public class Main extends JnodeModule {

	private static final Logger LOGGER = Logger.getLogger(Main.class);
	private static final int HTTP_BAD_REQUEST = 400;
	private static final int HTTP_INTERNAL_SERVER_ERROR = 500;

	public static void main(String[] args) throws JnodeModuleException {
		Main mainModule = new Main(Main.class.getResource("config-rest.properties").getPath());
		mainModule.start();
	}

	public Main(String configFile) throws JnodeModuleException {
		super(configFile);
	}

	public int getPort() throws JnodeModuleException {
		try
		{
			return Integer.parseInt(properties.getProperty("port", "4567"));
		}  catch(NumberFormatException e){
			throw new JnodeModuleException("bad port value", e);
		}

	}

	@Override
	public void start() {
		try {
			startInternal();
		} catch (Exception e) {
			LOGGER.l1("fail", e);
		}
	}

	private void startInternal() throws JnodeModuleException {
		Spark.setPort(getPort());
		Spark.get(new Route("/users/:id") {
            @Override
            public Object handle(Request request, Response response) {
                response.type("application/json");

                String id = request.params("id");

                Message m = new Message();
                m.setSubject(id);
                return new Gson().toJson(m);
            }
        });
		Spark.post(new Route("/echoarea") {
            @Override
            public Object handle(Request request, Response response) {
                response.type("text/plain");
                Message message;
				try {
					message = MessageMapper.fromJson(request.body());
				} catch (BadJsonException e) {
					response.status(HTTP_BAD_REQUEST);
                    return StringUtils.EMPTY;
                }
                final MessageId value = new MessageId();
                value.setValue(message.getSubject() + "1");
                try {
                    return MessageIdMapper.toJson(value);
                } catch (JsonProcessingException e) {
                    response.status(HTTP_INTERNAL_SERVER_ERROR);
                    return StringUtils.EMPTY;
                }
            }
        });

	}

	@Override
	public void handle(IEvent iEvent) {

	}
}
