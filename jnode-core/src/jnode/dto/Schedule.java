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

package jnode.dto;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import jnode.core.ConcurrentDateFormatAccess;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

/**
 * Планировщик (жалкая замена cron :-) )
 *
 * @author Manjago
 */
@DatabaseTable(tableName = "schedule")
public class Schedule {
    private static final ConcurrentDateFormatAccess DATE_DAY_FORMAT = new ConcurrentDateFormatAccess("MMMM dd yyyy");
    private static final ConcurrentDateFormatAccess DATE_HOUR_FORMAT = new ConcurrentDateFormatAccess("MMMM dd yyyy HH");
    private static final long DAYMSEC = 86400000L;
    @DatabaseField(columnName = "id", generatedId = true)
    private Long id;
    @DatabaseField(dataType = DataType.ENUM_STRING, canBeNull = false, columnName = "type", defaultValue = "DAILY")
    private Type type;
    @DatabaseField(columnName = "jscript_id", foreign = true, canBeNull = false, uniqueIndexName = "lsched_idx")
    private Jscript jscript;
    @DatabaseField(columnName = "lastRunDate", canBeNull = true, dataType = DataType.DATE)
    private Date lastRunDate;
    @DatabaseField(columnName = "nextRunDate", canBeNull = true, dataType = DataType.DATE)
    private Date nextRunDate;

    private static boolean isSameDay(Date date1, Date date2) {
        return !(date1 == null || date2 == null) && DATE_DAY_FORMAT.format(date1).equals(DATE_DAY_FORMAT.format(date2));
    }

    private static boolean isSameHour(Date date1, Date date2) {
        return !(date1 == null || date2 == null) && DATE_HOUR_FORMAT.format(date1).equals(DATE_HOUR_FORMAT.format(date2));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getLastRunDate() {
        return lastRunDate;
    }

    public void setLastRunDate(Date lastRunDate) {
        this.lastRunDate = lastRunDate;
    }

    public Date queryNextRunDate() {
        return queryNextRunDate(getLastRunDate(), getType());
    }

     static Date queryNextRunDate(Date lastRunDate, Schedule.Type type) {

        Objects.requireNonNull(lastRunDate);
        Objects.requireNonNull(type);

        switch (type) {
            case HOURLY:
                return new Date(lastRunDate.getTime() + 1000L * 60 * 60);
            case DAILY:
                return new Date(lastRunDate.getTime() + DAYMSEC);
            case ANNUALLY:
                return new Date(lastRunDate.getTime() + DAYMSEC * 366);
            case MONTHLY:
                return new Date(lastRunDate.getTime() + DAYMSEC * 30);
            case WEEKLY:
                return new Date(lastRunDate.getTime() + DAYMSEC * 7);
            default:
                return null;
        }

    }

    public boolean isNeedExec(Calendar calendar) {

        if (calendar == null || getType() == null) {
            return false;
        }

        Date actualLastRunDate = getLastRunDate() != null ? getLastRunDate() : new Date(new Date().getTime() - DAYMSEC * 365 * 10);

        Date actualNextRunDate = getNextRunDate() != null ? getNextRunDate() : queryNextRunDate(actualLastRunDate, getType());

        if (actualNextRunDate == null) {
            return false;
        }

        Date now = calendar.getTime();

        return now.compareTo(actualNextRunDate) > 0;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Jscript getJscript() {
        return jscript;
    }

    public void setJscript(Jscript jscript) {
        this.jscript = jscript;
    }

    public Date getNextRunDate() {
        return nextRunDate;
    }

    public void setNextRunDate(Date nextRunDate) {
        this.nextRunDate = nextRunDate;
    }

    @Override
    public String toString() {
        return "Schedule{" +
                "id=" + id +
                ", type=" + type +
                ", jscript=" + jscript +
                ", lastRunDate=" + lastRunDate +
                ", nextRunDate=" + nextRunDate +
                '}';
    }

    public enum Type {
        HOURLY, DAILY, WEEKLY, MONTHLY, ANNUALLY
    }
}
