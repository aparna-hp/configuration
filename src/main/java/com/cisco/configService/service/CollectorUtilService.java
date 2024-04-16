package com.cisco.configService.service;

import com.cisco.collectionService.utils.StringUtil;
import com.cisco.configService.entity.Collector;
import com.cisco.configService.entity.Network;
import com.cisco.configService.exception.CustomException;
import com.cisco.configService.repository.CollectorRepository;
import com.cisco.configService.repository.NetworkRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class CollectorUtilService {

    @Autowired
    CollectorRepository collectorRepository;

    @Autowired
    NetworkRepository networkRepository;

    /*
    This method returns the network ID associated with the name.
    */
    public Long validateNetworkNameAndGetId(String networkName) {
        log.info("Validate the network name{} ", networkName);
        if (StringUtil.isEmpty(networkName)) {
            log.error("No network name provided.");
            throw new CustomException(HttpStatus.BAD_REQUEST, "Please specify the network name.");
        }
        Optional<Network> networkOptional = networkRepository.findByName(networkName);
        if (networkOptional.isEmpty()) {
            log.error("There is no network associated with name " + networkName);
            throw new CustomException(HttpStatus.BAD_REQUEST, "Please specify a valid network associated with the scheduler.");
        }

        return networkOptional.get().getId();
    }

    /*
    This method returns the network ID associated with the name.
    */
    public Collector validateCollectorName(String collectorName) {
        log.info("Validate the collector name {} ", collectorName);
        if (StringUtil.isEmpty(collectorName)) {
            log.error("No collector name provided.");
            throw new CustomException(HttpStatus.BAD_REQUEST, "Please specify the collector name.");
        }

        List<Collector> collectorList = collectorRepository.findByName(collectorName);
        if (null == collectorList || collectorList.size() == 0) {
            log.error("There is no collector associated with name " + collectorName);
            throw new CustomException(HttpStatus.BAD_REQUEST, "Please specify a valid collector name.");
        }

        return collectorList.get(0);
    }
}
