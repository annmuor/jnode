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
package org.jnode.rest.dto;

public class Message {
    private String echoArea;
    private String subject;
    private String body;

    public String getEchoArea() {
        return echoArea;
    }

    public void setEchoArea(String echoArea) {
        this.echoArea = echoArea;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "Message{" +
                "echoArea='" + echoArea + '\'' +
                ", subject='" + subject + '\'' +
                ", body='" + body + '\'' +
                '}';
    }
}
