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

/**
 * Планировщик (жалкая замена cron :-) )
 *
 * @author Manjago
 */
@DatabaseTable(tableName = "schedule")
public class Schedule {
    private static final ConcurrentDateFormatAccess DATE_DAY_FORMAT = new ConcurrentDateFormatAccess("MMMM dd yyyy");
    private static final ConcurrentDateFormatAccess DATE_HOUR_FORMAT = new ConcurrentDateFormatAccess("MMMM dd yyyy HH");
    @DatabaseField(columnName = "id", generatedId = true)
    private Long id;
    @DatabaseField(dataType = DataType.ENUM_STRING, canBeNull = false, columnName = "type", defaultValue = "DAILY")
    private Type type;
    @DatabaseField(columnName = "details", defaultValue = "0")
    private Integer details;
    @DatabaseField(columnName = "jscript_id", foreign = true, canBeNull = false, uniqueIndexName = "lsched_idx")
    private Jscript jscript;
    @DatabaseField(columnName = "lastRunDate", canBeNull = true, dataType = DataType.DATE)
    private Date lastRunDate;

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

    public boolean isNeedExec(Calendar calendar) {

        if (calendar == null || getType() == null || getDetails() == null) {
            return false;
        }

        switch (getType()) {
            case HOURLY:
                if (isSameHour(getLastRunDate(), new Date())) {
                    return false;
                }
                break;
            default:
                if (isSameDay(getLastRunDate(), new Date())) {
                    return false;
                }
                break;
        }

        switch (getType()) {
            case HOURLY:
                return true;
            case DAILY:
                return checkDetails(calendar.get(Calendar.HOUR_OF_DAY));
            case ANNUALLY:
                return checkDetails(calendar.get(Calendar.DAY_OF_YEAR));
            case MONTHLY:
                return checkDetails(calendar.get(Calendar.DAY_OF_MONTH));
            case WEEKLY:
                return checkDetails(calendar.get(Calendar.DAY_OF_WEEK));

            default:
                return false;
        }
    }

    private boolean checkDetails(int fromCalendar) {
        return getDetails() != null && getDetails().equals(fromCalendar);
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Integer getDetails() {
        return details;
    }

    public void setDetails(Integer details) {
        this.details = details;
    }

    public Jscript getJscript() {
        return jscript;
    }

    public void setJscript(Jscript jscript) {
        this.jscript = jscript;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Schedule{");
        sb.append("id=").append(id);
        sb.append(", type=").append(type);
        sb.append(", details=").append(details);
        sb.append(", jscript=").append(jscript);
        sb.append(", lastRunDate=").append(lastRunDate);
        sb.append('}');
        return sb.toString();
    }

    public static enum Type {
        HOURLY, DAILY, WEEKLY, MONTHLY, ANNUALLY
    }
}
