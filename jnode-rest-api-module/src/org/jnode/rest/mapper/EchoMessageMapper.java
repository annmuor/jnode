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
package org.jnode.rest.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jnode.rest.core.BadJsonException;
import org.jnode.rest.core.StringUtils;
import org.jnode.rest.dto.EchoMessage;

import java.io.IOException;

public final class EchoMessageMapper {
    private EchoMessageMapper() {
    }

    public static EchoMessage fromJson(String payload) throws BadJsonException {
        if (StringUtils.isEmpty(payload)) {
            throw new BadJsonException("empty body");
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            EchoMessage echoMessage = mapper.readValue(payload, EchoMessage.class);
            if (!echoMessage.isValid()) {
                throw new BadJsonException("invalid echoMessage");
            }
            return echoMessage;
        } catch (IOException e) {
            throw new BadJsonException("json process exception", e);
        }
    }
}
