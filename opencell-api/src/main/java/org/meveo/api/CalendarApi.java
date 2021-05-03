/*
 * (C) Copyright 2015-2020 Opencell SAS (https://opencellsoft.com/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * THERE IS NO WARRANTY FOR THE PROGRAM, TO THE EXTENT PERMITTED BY APPLICABLE LAW. EXCEPT WHEN
 * OTHERWISE STATED IN WRITING THE COPYRIGHT HOLDERS AND/OR OTHER PARTIES PROVIDE THE PROGRAM "AS
 * IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE ENTIRE RISK AS TO
 * THE QUALITY AND PERFORMANCE OF THE PROGRAM IS WITH YOU. SHOULD THE PROGRAM PROVE DEFECTIVE,
 * YOU ASSUME THE COST OF ALL NECESSARY SERVICING, REPAIR OR CORRECTION.
 *
 * For more information on the GNU Affero General Public License, please consult
 * <https://www.gnu.org/licenses/agpl-3.0.en.html>.
 */

package org.meveo.api;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.dto.*;
import org.meveo.api.dto.response.ListCalendarResponse;
import org.meveo.api.dto.response.PagingAndFiltering;
import org.meveo.api.exception.*;
import org.meveo.api.restful.util.GenericPagingAndFilteringUtils;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.catalog.*;
import org.meveo.model.catalog.CalendarJoin.CalendarJoinTypeEnum;
import org.meveo.service.catalog.impl.CalendarBankingService;
import org.meveo.service.catalog.impl.CalendarService;
import org.meveo.service.catalog.impl.DayInYearService;
import org.meveo.service.catalog.impl.HourInDayService;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Edward P. Legaspi
 * @author hznibar
 **/
@Stateless
public class CalendarApi extends BaseApi {

    @Inject
    private CalendarService calendarService;
    
    @Inject
    private CalendarBankingService calendarBankingService;

    @Inject
    private DayInYearService dayInYearService;

    @Inject
    private HourInDayService hourInDayService;
    
    /** The INVALID_WEEKEND_PERIOD message. */
    private static final String INVALID_WEEKEND_PERIOD = "Invalid weekend period! Possible values are from 1 to 7";

    private static final String INVALID_HOLIDAY_PERIOD = "Invalid holiday period! Possible values are from 101 to 1231";

    public void create(CalendarDto postData) throws MeveoApiException, BusinessException {

        if (StringUtils.isBlank(postData.getCode())) {
            missingParameters.add("code");
        }
        if (StringUtils.isBlank(postData.getCalendarType())) {
            missingParameters.add("calendarType");
        }

        handleMissingParametersAndValidate(postData);

        if (calendarService.findByCode(postData.getCode()) != null) {
            throw new EntityAlreadyExistsException(Calendar.class, postData.getCode());
        }

        if (postData.getCalendarType() == CalendarTypeEnum.YEARLY) {

            CalendarYearly calendar = new CalendarYearly();
            calendar.setCode(postData.getCode());
            calendar.setDescription(postData.getDescription());
            if (postData.getDays() != null && postData.getDays().size() > 0) {
                List<DayInYear> days = new ArrayList<DayInYear>();
                for (DayInYearDto d : postData.getDays()) {
                    DayInYear dayInYear = dayInYearService.findByMonthAndDay(d.getMonth(), d.getDay());                   
                    if (dayInYear != null) {
                        days.add(dayInYear);
                    }
                }

                calendar.setDays(days);
            }

            calendarService.create(calendar);

        } else if (postData.getCalendarType() == CalendarTypeEnum.DAILY) {

            CalendarDaily calendar = new CalendarDaily();
            calendar.setCode(postData.getCode());
            calendar.setDescription(postData.getDescription());

            if (postData.getHours() != null && postData.getHours().size() > 0) {
                List<HourInDay> hours = new ArrayList<HourInDay>();
                for (HourInDayDto d : postData.getHours()) {
                    HourInDay hourInDay = hourInDayService.findByHourAndMin(d.getHour(), d.getMin());
                    if (hourInDay == null) {
                        hourInDay = new HourInDay(d.getHour(), d.getMin());
                    }
                    hours.add(hourInDay);
                }

                calendar.setHours(hours);
            }

            calendarService.create(calendar);

        } else if (postData.getCalendarType() == CalendarTypeEnum.PERIOD) {

            if (StringUtils.isBlank(postData.getPeriodUnit())) {
                missingParameters.add("periodUnit");
                handleMissingParameters();
            }

            CalendarPeriod calendar = new CalendarPeriod();
            calendar.setCode(postData.getCode());
            calendar.setDescription(postData.getDescription());
            calendar.setPeriodLength(postData.getPeriodLength());
            calendar.setNbPeriods(postData.getNbPeriods());
            calendar.setPeriodUnit(postData.getPeriodUnit().getUnitValue());

            calendarService.create(calendar);

        } else if (postData.getCalendarType() == CalendarTypeEnum.INTERVAL) {

            CalendarInterval calendar = new CalendarInterval();
            calendar.setCode(postData.getCode());
            calendar.setDescription(postData.getDescription());
            calendar.setIntervalType(postData.getIntervalType());

            if (postData.getIntervals() != null && postData.getIntervals().size() > 0) {
                List<CalendarDateInterval> intervals = new ArrayList<CalendarDateInterval>();
                for (CalendarDateIntervalDto interval : postData.getIntervals()) {
                    intervals.add(new CalendarDateInterval(calendar, interval.getIntervalBegin(), interval.getIntervalEnd()));
                }

                calendar.setIntervals(intervals);
            }

            calendarService.create(calendar);

        } else if (postData.getCalendarType().isJoin()) {

            if (StringUtils.isBlank(postData.getJoinCalendar1Code())) {
                missingParameters.add("joinCalendar1Code");
            }
            if (StringUtils.isBlank(postData.getJoinCalendar2Code())) {
                missingParameters.add("joinCalendar2Code");
            }

            handleMissingParameters();

            Calendar cal1 = calendarService.findByCode(postData.getJoinCalendar1Code());
            Calendar cal2 = calendarService.findByCode(postData.getJoinCalendar2Code());

            if (cal1 == null) {
                throw new InvalidParameterException("joinCalendar1Code", postData.getJoinCalendar1Code());
            }
            if (cal2 == null) {
                throw new InvalidParameterException("joinCalendar2Code", postData.getJoinCalendar2Code());
            }

            CalendarJoin calendar = new CalendarJoin();
            calendar.setCode(postData.getCode());
            calendar.setDescription(postData.getDescription());
            calendar.setJoinType(CalendarJoinTypeEnum.valueOf(postData.getCalendarType().name())); // Join type is expressed as Calendar type in DTO
            calendar.setJoinCalendar1(cal1);
            calendar.setJoinCalendar2(cal2);

            calendarService.create(calendar);
        } else if (postData.getCalendarType() == CalendarTypeEnum.BANKING) { 

            CalendarBanking calendar = new CalendarBanking();
            calendar.setCode(postData.getCode());
            calendar.setDescription(postData.getDescription());
            calendar.setStartDate(postData.getStartDate());
            calendar.setEndDate(postData.getEndDate());
            if(!isWeekendPeriodValid(postData.getWeekendBegin()) || !isWeekendPeriodValid(postData.getWeekendEnd())) {
                throw new BusinessApiException(INVALID_WEEKEND_PERIOD);
            }
            calendar.setWeekendBegin(postData.getWeekendBegin());
            calendar.setWeekendEnd(postData.getWeekendEnd());

            if (postData.getHolidays() != null && postData.getHolidays().size() > 0) {
                List<CalendarHoliday> holidays = new ArrayList<CalendarHoliday>();
                for (CalendarHolidayDto holiday : postData.getHolidays()) {
                    if(!isHolidayPeriodValid(holiday.getHolidayBegin()) || !isHolidayPeriodValid(holiday.getHolidayEnd())) {
                        throw new BusinessApiException(INVALID_HOLIDAY_PERIOD);
                    }
                    holidays.add(new CalendarHoliday(calendar, holiday.getHolidayBegin(), holiday.getHolidayEnd()));
                }

                calendar.setHolidays(holidays);
            }

            calendarService.create(calendar);

        
        } else {
            throw new BusinessApiException("invalid calendar type, possible values YEARLY, DAILY, PERIOD, INTERVAL, JOIN, BANKING");
        }

    }

    public void update(CalendarDto postData) throws MeveoApiException, BusinessException {

        if (StringUtils.isBlank(postData.getCode())) {
            missingParameters.add("code");
        }
        if (StringUtils.isBlank(postData.getCalendarType())) {
            missingParameters.add("calendarType");
        }

        handleMissingParametersAndValidate(postData);

        Calendar calendar = calendarService.findByCode(postData.getCode());
        if (calendar == null) {
            throw new EntityDoesNotExistsException(Calendar.class, postData.getCode());
        }
        calendar.setCode(StringUtils.isBlank(postData.getUpdatedCode()) ? postData.getCode() : postData.getUpdatedCode());
        calendar.setDescription(postData.getDescription());

        if (calendar instanceof CalendarYearly) {
            if (postData.getDays() != null && postData.getDays().size() > 0) {
                List<DayInYear> days = new ArrayList<DayInYear>();
                for (DayInYearDto d : postData.getDays()) {
                    DayInYear dayInYear = dayInYearService.findByMonthAndDay(d.getMonth(), d.getDay());
                    if (dayInYear != null) {
                        days.add(dayInYear);
                    }
                }

                ((CalendarYearly) calendar).setDays(days);
            }

        } else if (calendar instanceof CalendarDaily) {
            if (postData.getHours() != null && postData.getHours().size() > 0) {
                List<HourInDay> hours = new ArrayList<HourInDay>();
                for (HourInDayDto d : postData.getHours()) {
                    HourInDay hourInDay = hourInDayService.findByHourAndMin(d.getHour(), d.getMin());
                    if (hourInDay == null) {
                        hourInDay = new HourInDay(d.getHour(), d.getMin());
                    }
                    hours.add(hourInDay);
                }

                ((CalendarDaily) calendar).setHours(hours);
            }

        } else if (calendar instanceof CalendarPeriod) {

            ((CalendarPeriod) calendar).setPeriodLength(postData.getPeriodLength());
            ((CalendarPeriod) calendar).setNbPeriods(postData.getNbPeriods());
            if (!StringUtils.isBlank(postData.getPeriodUnit())) {
                ((CalendarPeriod) calendar).setPeriodUnit(postData.getPeriodUnit().getUnitValue());
            }

        } else if (calendar instanceof CalendarInterval) {

            CalendarInterval calendarInterval = (CalendarInterval) calendar;
            calendarInterval.setIntervalType(postData.getIntervalType());

            calendarInterval.getIntervals().clear();

            if (postData.getIntervals() != null && postData.getIntervals().size() > 0) {
                for (CalendarDateIntervalDto interval : postData.getIntervals()) {
                    calendarInterval.getIntervals().add(new CalendarDateInterval(calendarInterval, interval.getIntervalBegin(), interval.getIntervalEnd()));
                }
            }

        } else if (calendar instanceof CalendarJoin) {

            if (StringUtils.isBlank(postData.getJoinCalendar1Code())) {
                missingParameters.add("joinCalendar1Code");
            }
            if (StringUtils.isBlank(postData.getJoinCalendar2Code())) {
                missingParameters.add("joinCalendar2Code");
            }

            handleMissingParameters();

            Calendar cal1 = calendarService.findByCode(postData.getJoinCalendar1Code());
            Calendar cal2 = calendarService.findByCode(postData.getJoinCalendar2Code());

            if (cal1 == null) {
                throw new InvalidParameterException("joinCalendar1Code", postData.getJoinCalendar1Code());
            }
            if (cal2 == null) {
                throw new InvalidParameterException("joinCalendar2Code", postData.getJoinCalendar2Code());
            }

            CalendarJoin calendarJoin = (CalendarJoin) calendar;
            calendarJoin.setJoinType(CalendarJoinTypeEnum.valueOf(postData.getCalendarType().name()));// Join type is expressed as Calendar type in DTO
            calendarJoin.setJoinCalendar1(cal1);
            calendarJoin.setJoinCalendar2(cal2);
        } else if (calendar instanceof CalendarBanking) {
            
            CalendarBanking calendarBanking = (CalendarBanking) calendar;
            calendarBanking.setStartDate(postData.getStartDate());
            calendarBanking.setEndDate(postData.getEndDate());
            if(!isWeekendPeriodValid(postData.getWeekendBegin()) || !isWeekendPeriodValid(postData.getWeekendEnd())) {
                throw new BusinessApiException(INVALID_WEEKEND_PERIOD);
            }
            calendarBanking.setWeekendBegin(postData.getWeekendBegin());
            calendarBanking.setWeekendEnd(postData.getWeekendEnd());
            calendarBanking.getHolidays().clear();

            if (postData.getHolidays() != null && postData.getHolidays().size() > 0) {
                for (CalendarHolidayDto holiday : postData.getHolidays()) {
                    if(!isHolidayPeriodValid(holiday.getHolidayBegin()) || !isHolidayPeriodValid(holiday.getHolidayEnd())) {
                        throw new BusinessApiException(INVALID_HOLIDAY_PERIOD);
                    }
                    calendarBanking.getHolidays().add(new CalendarHoliday(calendarBanking, holiday.getHolidayBegin(), holiday.getHolidayEnd()));
                }
            }
        }

        calendarService.update(calendar);
    }

    public CalendarDto find(String calendarCode) throws MeveoApiException {
        CalendarDto result = new CalendarDto();

        if (!StringUtils.isBlank(calendarCode)) {
            Calendar calendar = calendarService.findByCode(calendarCode);
            if (calendar == null) {
                throw new EntityDoesNotExistsException(Calendar.class, calendarCode);
            }

            result = new CalendarDto(calendar);
        } else {
            if (StringUtils.isBlank(calendarCode)) {
                missingParameters.add("calendarCode");
            }

            handleMissingParameters();
        }

        return result;
    }
    
    /**
     * Gets the banking date status.
     *
     * @param date the date to check
     * @return the banking date status : is working date if the date to check is not a holiday or weekend
     * @throws MeveoApiException the meveo api exception
     */
    public BankingDateStatusDto getBankingDateStatus(Date date) throws MeveoApiException {
        Boolean isWorkingDate = true;
        if (date != null) {
            isWorkingDate = calendarBankingService.isBankWorkingDate(date);
        } else {
            missingParameters.add("date");
            handleMissingParameters();
        }

        return new BankingDateStatusDto(date,isWorkingDate);
    }

    public List<CalendarDto> list() throws MeveoApiException {
        List<CalendarDto> result = new ArrayList<CalendarDto>();
        for (Calendar calendar : calendarService.list()) {
            result.add(new CalendarDto(calendar));
        }
        return result;
    }

    public ListCalendarResponse list(PagingAndFiltering pagingAndFiltering) {
        ListCalendarResponse result = new ListCalendarResponse();
        result.setPaging( pagingAndFiltering );

        List<Calendar> calendars = calendarService.list( GenericPagingAndFilteringUtils.getInstance().getPaginationConfiguration() );
        if (calendars != null) {
            for (Calendar calendar : calendars) {
                result.getCalendars().getCalendar().add(new CalendarDto(calendar));
            }
        }

        return result;
    }

    public void remove(String calendarCode) throws MeveoApiException, BusinessException {
        if (!StringUtils.isBlank(calendarCode)) {
            Calendar calendar = calendarService.findByCode(calendarCode);
            if (calendar == null) {
                throw new EntityDoesNotExistsException(Calendar.class, calendarCode);
            }

            calendarService.remove(calendar);
        } else {
            if (StringUtils.isBlank(calendarCode)) {
                missingParameters.add("calendarCode");
            }

            handleMissingParameters();
        }
    }

    public void createOrUpdate(CalendarDto postData) throws MeveoApiException, BusinessException {
        Calendar calendar = calendarService.findByCode(postData.getCode());
        if (calendar == null) {
            // create
            create(postData);
        } else {
            // update
            update(postData);
        }
    }
    
    /**
     * Checks if is holiday period valid.
     *
     * @param holidayMonthDay the holiday month day
     * @return true, if the monthDay is between 101 and 1231
     */
    private boolean isHolidayPeriodValid(Integer holidayMonthDay) {
        return holidayMonthDay != null && holidayMonthDay >= 101 && holidayMonthDay <= 1231;
    }
    
    
    /**
     * Checks if is weekend period valid.
     *
     * @param weekendDay the weekend day
     * @return true, if the weekend day is between 1 and 7.
     */
    private boolean isWeekendPeriodValid(Integer weekendDay) {
        return weekendDay != null && weekendDay >=1 && weekendDay <= 7;
    }

}
