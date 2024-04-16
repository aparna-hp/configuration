package com.cisco.configService;

import com.cisco.configService.exception.CustomException;
import com.cisco.configService.maintanance.MaintenanceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.time.Instant;
import java.time.ZoneId;

@Component
@Slf4j
public class ConfigServiceInterceptor implements HandlerInterceptor {

    @Autowired
    MaintenanceService maintenanceService;

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        log.info("PreHandle interceptor invoked." + "[" + request.getMethod()
                + "]" + request.getRequestURI() );
        log.debug("Maintenance mode value : " + maintenanceService.isInMaintenanceMode());
        if(!request.getMethod().equals(HttpMethod.GET.toString()) && maintenanceService.isInMaintenanceMode()) {
            log.error("Service is in Maintenance mode. {} operation is not allowed.", request.getMethod());
            throw new CustomException(HttpStatus.SERVICE_UNAVAILABLE, "Service is in Maintenance mode.", "Operation not allowed in Maintenance mode.");
        }

        return true;
    }

    @Override
    public void postHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            ModelAndView modelAndView) throws Exception {

        Instant instant = Instant.now();
        ZoneId zoneId = ZoneId.of("GMT");
        String dateTime = instant.atZone(zoneId).toString();
        log.debug("[postHandle] Adding timezone " + dateTime);

        response.setDateHeader(dateTime, 2L);
        response.setHeader("Location", request.getRequestURI());
    }

}
