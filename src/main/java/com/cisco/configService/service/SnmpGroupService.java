package com.cisco.configService.service;

import com.cisco.configService.entity.SnmpGroup;
import com.cisco.configService.exception.CustomException;
import com.cisco.configService.model.preConfig.SnmpGroupData;
import com.cisco.configService.repository.SnmpGroupRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.relational.core.conversion.DbActionExecutionException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class SnmpGroupService {

    @Autowired
    SnmpGroupRepository snmpGroupRepository;

    @Autowired
    CryptoService cryptoService;

    public void addSnmpGroup(SnmpGroupData snmpGroupData, boolean... override) {
        SnmpGroup snmpGroup = new SnmpGroup();
        List<SnmpGroup> snmpGroupByName = snmpGroupRepository.findByName(snmpGroupData.getName());
        if(!snmpGroupByName.isEmpty()) {
            if(override == null || override.length == 0) {
                throw new CustomException(HttpStatus.BAD_REQUEST, "Snmp group with name " + snmpGroupData.getName() + " already exists !");
            }else {
                //Use the existing ID associated with name if override is true
                log.info("Using the existing ID {} associated with the name {}", snmpGroupByName.get(0).getId(),
                        snmpGroupData.getName());
                snmpGroupData.setId(snmpGroupByName.get(0).getId());
                //Invoke update instead of add for existing configurations.
                updateSnmpGroup(snmpGroupData);
                return;
            }
        } else {
            //Ignore Id
            snmpGroupData.setId(null);
        }

        snmpGroupData.setUpdateDate(LocalDate.now());

        BeanUtils.copyProperties(snmpGroupData, snmpGroup);
        encryptCredentials(snmpGroupData.getAuthenticationPassword(), snmpGroupData.getEncryptionPassword(), snmpGroup);

        snmpGroupRepository.save(snmpGroup);

        snmpGroupData.setId(snmpGroup.getId());
    }

    public Optional<SnmpGroupData> getSnmpGroup(Long id) {
        Optional<SnmpGroup> optionalSnmpGroup = snmpGroupRepository.findById(id);
        if (optionalSnmpGroup.isEmpty()) {
            return Optional.empty();
        }

        SnmpGroup snmpGroup = optionalSnmpGroup.get();
        SnmpGroupData snmpGroupData = new SnmpGroupData();

        BeanUtils.copyProperties(snmpGroup, snmpGroupData);
        decryptCredentials(snmpGroup.getAuthenticationPassword(), snmpGroup.getEncryptionPassword(), snmpGroupData);
        return Optional.of(snmpGroupData);
    }

    public List<SnmpGroupData> getAllSnmpGroups() {
        Iterable<SnmpGroup> snmpGroupList = snmpGroupRepository.findAll();
        List<SnmpGroupData> snmpGroupDataList = new ArrayList<>();

        for (SnmpGroup snmpGroup : snmpGroupList) {
            SnmpGroupData snmpGroupData = new SnmpGroupData();
            BeanUtils.copyProperties(snmpGroup, snmpGroupData);
            decryptCredentials(snmpGroup.getAuthenticationPassword(), snmpGroup.getEncryptionPassword(), snmpGroupData);
            snmpGroupDataList.add(snmpGroupData);
        }

        log.info("No. of snmp groups " + snmpGroupDataList.size());
        return snmpGroupDataList;
    }

    public Optional<Long> updateSnmpGroup(SnmpGroupData snmpGroupData) {
        Optional<SnmpGroup> optionalSnmpGroup = snmpGroupRepository.findById(snmpGroupData.getId());
        if (optionalSnmpGroup.isEmpty()) {
            return Optional.empty();
        }

        SnmpGroup snmpGroup = optionalSnmpGroup.get();
        //Name cannot be updated.
        snmpGroupData.setName(snmpGroup.getName());
        snmpGroupData.setUpdateDate(LocalDate.now());
        BeanUtils.copyProperties(snmpGroupData, snmpGroup);
        encryptCredentials(snmpGroupData.getAuthenticationPassword(), snmpGroupData.getEncryptionPassword(), snmpGroup);

        snmpGroupRepository.save(snmpGroup);

        return Optional.of(snmpGroup.getId());
    }

    public Optional<Long> deleteSnmpGroup(Long id) {

        Optional<SnmpGroupData> optionalSnmpGroupData = getSnmpGroup(id);
        if (optionalSnmpGroupData.isEmpty()) {
            return Optional.empty();
        }

        SnmpGroup snmpGroup = new SnmpGroup();

        BeanUtils.copyProperties(optionalSnmpGroupData.get(), snmpGroup);
        try {
            snmpGroupRepository.delete(snmpGroup);
        } catch ( DbActionExecutionException e) {
            log.error("Error deleting snmp group ",e);
            throw new CustomException(HttpStatus.CONFLICT,
                    "Cannot delete the snmp group since it is associated with the node profile.");
        }

        return Optional.of(id);
    }

    public Optional<SnmpGroupData> getSnmpGroupByName(String name) {
        List<SnmpGroup> snmpGroup = snmpGroupRepository.findByName(name);
        if (snmpGroup.isEmpty()) {
            log.debug("There is no snmp group by name " + name);
            return Optional.empty();
        }
        log.debug("Found snmp group by name " + name);

        if(snmpGroup.size() > 1) {
            log.debug("Found more than 1 snmp group with the same name. "
                    + Arrays.toString(snmpGroup.toArray()));
        }
        SnmpGroupData snmpGroupData = new SnmpGroupData();

        BeanUtils.copyProperties(snmpGroup.get(0), snmpGroupData);
        decryptCredentials(snmpGroup.get(0).getAuthenticationPassword(), snmpGroup.get(0).getEncryptionPassword(), snmpGroupData);

        log.debug("Set Snmp Group mismatch to false.");
        return Optional.of(snmpGroupData);
    }

    public Optional<SnmpGroup> isSnmpGroupExists(String snmpGroupName) {
        log.debug("Verifying the snmpGroupName " + snmpGroupName);
        if (null == snmpGroupName || snmpGroupName.isEmpty()) {
            log.debug("Snmp group name is not provided. Set Snmp mismatch to true.");
            return Optional.empty();
        } else {
            List<SnmpGroup> snmpGroupList = snmpGroupRepository.findByName(snmpGroupName);
            if (snmpGroupList.isEmpty()) {
                log.debug("Snmp group name is not present. Set Snmp mismatch to true.");
                return Optional.empty();
            } else if(snmpGroupList.size() > 1) {
                log.debug("Found more than 1 snmp group with the same name. "
                        + Arrays.toString(snmpGroupList.toArray()));
            }
            log.info("Set Snmp Group mismatch to false.");
            return Optional.of(snmpGroupList.get(0));
        }
    }

    public Optional<String> getSnmpGroupNameById(Long snmpGroupId) {
        log.debug("Get the Snmp Group name associated with Node List Id " + snmpGroupId);

        Optional<SnmpGroup> optionalSnmpGroup = snmpGroupRepository.findById(snmpGroupId);
        if (optionalSnmpGroup.isEmpty()) {
            log.debug("Snmp group is not added for the NodeList.");
            return Optional.empty();
        } else {
            log.debug("Snmp group name is found. Set Snmp mismatch to false.");
            return Optional.of(optionalSnmpGroup.get().getName());
        }
    }

    public void deleteAll(){
        snmpGroupRepository.deleteAll();
    }

    private void encryptCredentials(String authenticationPassword, String encryptionPassword, SnmpGroup snmpGroup) {
        byte[] encrypted;
        if(null != authenticationPassword) {
            encrypted = cryptoService.aesEncrypt(authenticationPassword);
            if(null != encrypted) {
                snmpGroup.setAuthenticationPassword(encrypted);
            }
        }

        if(null != encryptionPassword) {
            encrypted = cryptoService.aesEncrypt(encryptionPassword);
            if( null != encrypted) {
                snmpGroup.setEncryptionPassword(encrypted);
            }
        }
    }

    private void decryptCredentials(byte[] authenticationPassword, byte[] encryptionPassword, SnmpGroupData snmpGroupData) {
        byte[] decrypted;

        if (null != authenticationPassword && authenticationPassword.length > 0) {
            decrypted = cryptoService.aesDecrypt(authenticationPassword);
            if (null != decrypted) {
                snmpGroupData.setAuthenticationPassword(new String(decrypted));
            }
        }

        if (null != encryptionPassword && encryptionPassword.length > 0) {
            decrypted = cryptoService.aesDecrypt(encryptionPassword);
            if(null != decrypted) {
                snmpGroupData.setEncryptionPassword(new String(decrypted));
            }
        }

        log.debug("Updated snmp group data " + snmpGroupData);
    }

    public List<Long> getNodeProfileBySnmpGroup(Long snmpGroupId){
        return snmpGroupRepository.findNodeProfileBySnmpGroup(snmpGroupId);
    }
}
