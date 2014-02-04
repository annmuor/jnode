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

package jnode.install.support;

import jnode.dto.Echoarea;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * @author kreon
 */
@DatabaseTable(tableName = "dupes")
public class Dupe_1_4 {
    @DatabaseField(columnName = "msgid", index = true)
    private String msgid;
    @DatabaseField(columnName = "echoarea_id", foreign = true)
    private Echoarea echoarea;

    public String getMsgid() {
        return msgid;
    }

    public void setMsgid(String msgid) {
        this.msgid = msgid;
    }

    public Echoarea getEchoarea() {
        return echoarea;
    }

    public void setEchoarea(Echoarea echoarea) {
        this.echoarea = echoarea;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Dupe{");
        sb.append("msgid='").append(msgid).append('\'');
        sb.append(", echoarea=").append(echoarea);
        sb.append('}');
        return sb.toString();
    }
}
