package com.monitoring.api_monitoring_starter.Application.Service;


import com.monitoring.api_monitoring_starter.Application.DTO.ApplicationLogDTO;
import com.monitoring.api_monitoring_starter.Application.Logging.MemoryLogAppender;


import ch.qos.logback.classic.spi.ILoggingEvent;

import org.springframework.stereotype.Service;


import java.util.List;



@Service
public class ApplicationLogService {



    public List<ApplicationLogDTO> getLogs(){


        return MemoryLogAppender
                .getLogs()
                .stream()
                .map(this::convert)
                .toList();


    }



    private ApplicationLogDTO convert(
            ILoggingEvent event
    ){


        ApplicationLogDTO dto =
                new ApplicationLogDTO();



        dto.setTimestamp(
                new java.util.Date(
                        event.getTimeStamp()
                ).toString()
        );


        dto.setLevel(
                event.getLevel()
                        .toString()
        );


        dto.setLogger(
                event.getLoggerName()
        );



        dto.setThread(
                event.getThreadName()
        );



        dto.setMessage(
                event.getFormattedMessage()
        );



        if(event.getThrowableProxy()!=null){

            dto.setException(
                    event.getThrowableProxy()
                            .getMessage()
            );

        }


        return dto;


    }


}