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

package org.jnode.rest.db;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import jnode.dto.Link;

import java.io.Serializable;
import java.util.Date;

@DatabaseTable(tableName = "restapi_user")
public class RestUser implements Serializable{
    public static final String GUESTLOGIN_FIELD = "guestlogin";
    public static final String LINK_ID_FIELD = "link_id";
    public static final String TOKEN_FIELD = "token";

    @DatabaseField(generatedId = true, columnName = "id")
    private Long id;
    @DatabaseField(columnName = TOKEN_FIELD, canBeNull = false, uniqueIndexName = "lresttoken_idx", width = 300)
    private String token;
    @DatabaseField(columnName = LINK_ID_FIELD, foreign = true, foreignAutoRefresh = true, uniqueIndexName = "lrestlink_idx")
    private Link link;
    @DatabaseField(columnName = "lastLogin", dataType = DataType.DATE_LONG)
    private Date lastLogin;
    @DatabaseField(dataType = DataType.ENUM_STRING, canBeNull = false, columnName = "type", defaultValue = "GUEST")
    private Type type;
    @DatabaseField(columnName = GUESTLOGIN_FIELD, uniqueIndexName = "lrestglogin_idx", width = 300)
    private String guestLogin;

    public String getGuestLogin() {
        return guestLogin;
    }

    public void setGuestLogin(String guestLogin) {
        this.guestLogin = guestLogin;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Link getLink() {
        return link;
    }

    public void setLink(Link link) {
        this.link = link;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "RestUser{" +
                "id=" + id +
                ", token='" + token + '\'' +
                ", link=" + link +
                ", lastLogin=" + lastLogin +
                ", type=" + type +
                ", guestLogin='" + guestLogin + '\'' +
                '}';
    }


    public enum Type {
        GUEST, USER, ADMIN
    }

}
