package com.cisco.configService.repository;

import com.cisco.configService.entity.Network;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface NetworkRepository extends CrudRepository<Network, Long> {

    Optional<Network> findByName(String name);
}
