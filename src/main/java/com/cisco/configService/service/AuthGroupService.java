package com.cisco.configService.service;

import com.cisco.collectionService.utils.StringUtil;
import com.cisco.configService.entity.AuthGroup;
import com.cisco.configService.exception.CustomException;
import com.cisco.configService.model.preConfig.AuthGroupData;
import com.cisco.configService.repository.AuthGroupRepository;
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
public class AuthGroupService {

    @Autowired
    AuthGroupRepository authGroupRepository;

    @Autowired
    CryptoService cryptoService;

    public void addAuthGroup(AuthGroupData authGroupData, boolean... override) {

        List<AuthGroup> authGroupByName = authGroupRepository.findByName(authGroupData.getName());
        if(!authGroupByName.isEmpty()) {
            if(override == null || override.length == 0) {
                throw new CustomException(HttpStatus.BAD_REQUEST, "Invalid Auth Group Name.",
                        "Auth group with name " + authGroupData.getName() + " already exists !");
            } else {
                //Use the existing ID associated with name if override is true
                log.info("Using the existing ID {} associated with the name {}", authGroupByName.get(0).getId(),
                        authGroupData.getName());
                authGroupData.setId(authGroupByName.get(0).getId());
                //Invoke update instead of add for existing configurations.
                updateAuthGroup(authGroupData);
            }
        } else {
            //Ignore ID
            authGroupData.setId(null);
        }

        AuthGroup authGroup = new AuthGroup();
        authGroupData.setUpdateDate(LocalDate.now());
        BeanUtils.copyProperties(authGroupData, authGroup);

        encryptCredentials(authGroupData.getPassword(), authGroupData.getConfirmPassword(), authGroup);
        authGroupRepository.save(authGroup);
        authGroupData.setId(authGroup.getId());
    }

    public Optional<AuthGroupData> getAuthGroup(Long id) {
        Optional<AuthGroup> optionalAuthGroup = authGroupRepository.findById(id);
        if (optionalAuthGroup.isEmpty()) {
            return Optional.empty();
        }

        AuthGroup authGroup = optionalAuthGroup.get();
        AuthGroupData authGroupData = new AuthGroupData();
        log.debug("Auth Group  " + authGroup);

        BeanUtils.copyProperties(authGroup, authGroupData);
        decryptCredentials(authGroup.getPassword(), authGroup.getConfirmPassword(),authGroupData);
        log.debug("Auth Group data " + authGroupData);
        return Optional.of(authGroupData);
    }

    public Optional<AuthGroupData> getAuthGroupByName(String name) {
        List<AuthGroup> authGroupList = authGroupRepository.findByName(name);
        if (authGroupList.isEmpty()) {
            log.debug("There is no auth group by name " + name);
            return Optional.empty();
        } else if (authGroupList.size() > 1) {
            log.debug("Found more than 1 auth group with the same name. "
                    + Arrays.toString(authGroupList.toArray()));
        }

        AuthGroupData authGroupData = new AuthGroupData();
        BeanUtils.copyProperties(authGroupList.get(0), authGroupData);
        decryptCredentials(authGroupList.get(0).getPassword(), authGroupList.get(0).getConfirmPassword(),authGroupData);

        log.debug("Set AuthGroup mismatch to false.");

        return Optional.of(authGroupData);
    }

    public List<AuthGroupData> getAllAuthGroups() {
        Iterable<AuthGroup> authGroupList = authGroupRepository.findAll();
        List<AuthGroupData> authGroupDataList = new ArrayList<>();

        for(AuthGroup authGroup : authGroupList) {
            AuthGroupData authGroupData = new AuthGroupData();
            BeanUtils.copyProperties(authGroup, authGroupData);
            decryptCredentials(authGroup.getPassword(), authGroup.getConfirmPassword(),authGroupData);
            authGroupDataList.add(authGroupData);
        }
        log.info("No. of auth groups " + authGroupDataList.size());
        return authGroupDataList;
    }

    public Optional<Long> updateAuthGroup(AuthGroupData authGroupData) {

        Optional<AuthGroup> optionalAuthGroup = authGroupRepository.findById(authGroupData.getId());
        if (optionalAuthGroup.isEmpty()) {
            return Optional.empty();
        }

        AuthGroup authGroup = optionalAuthGroup.get();
        authGroupData.setName(authGroup.getName());
        authGroupData.setUpdateDate(LocalDate.now());
        BeanUtils.copyProperties(authGroupData, authGroup);
        encryptCredentials(authGroupData.getPassword(), authGroupData.getConfirmPassword(),authGroup);
        authGroupRepository.save(authGroup);
        log.debug("Auth group Update successful " + authGroupData);

        return Optional.of(authGroup.getId());
    }

    public Optional<Long> deleteAuthGroup(long id) {
        Optional<AuthGroup> optionalAuthGroupData = authGroupRepository.findById(id);
        if (optionalAuthGroupData.isEmpty()) {
            return Optional.empty();
        }

        try {
            authGroupRepository.delete(optionalAuthGroupData.get());
        } catch (DbActionExecutionException e) {
            log.error("Error deleting the auth group " ,e);
            throw new CustomException(HttpStatus.CONFLICT ,
                    "Auth group delete error.Cannot delete the auth group since it is associated with the node profile.");
        }

        return Optional.of(id);
    }

    //Determine if authgroup is already added or not
    public Optional<AuthGroup> isAuthGroupExist(String authGroupName) {
        log.debug("Verifying the authGroupName " + authGroupName);
        if (null == authGroupName || authGroupName.isEmpty()) {
            log.debug("Auth group name is not provided. Set AuthGroup mismatch to true.");
            return Optional.empty();
        } else {
            List<AuthGroup> authGroupList = authGroupRepository.findByName(authGroupName);
            if (authGroupList.isEmpty()) {
                log.debug("Auth group name is not present. Set AuthGroup mismatch to true.");
                return Optional.empty();
            } else if (authGroupList.size() > 1) {
                log.debug("Found more than 1 snmp group with the same name. "
                        + Arrays.toString(authGroupList.toArray()));
            }
            log.debug("Set AuthGroup mismatch to false.");
            return Optional.of(authGroupList.get(0));
        }
    }

    public Optional<String> getAuthGroupNameById(Long authGroupId) {

        log.debug("Get the Auth Group name associated with Auth group Id :" + authGroupId);

            Optional<AuthGroup> optionalAuthGroup = authGroupRepository.findById(authGroupId);
            if (optionalAuthGroup.isEmpty()) {
                log.debug("Auth group is not added for the NodeList.");
                return Optional.empty();
            } else {
                log.debug("Auth group name is found. Set AuthGroup mismatch to false.");
                return Optional.of(optionalAuthGroup.get().getName());
            }
    }

    public void deleteAll(){
        authGroupRepository.deleteAll();
    }

    private void encryptCredentials(String password, String confirmPassword, AuthGroup authGroup) {
        byte[] encrypted;
        if(null != password) {
            encrypted = cryptoService.aesEncrypt(password);
            if(null != encrypted) {
                authGroup.setPassword(encrypted);
            }
        }

        if(!StringUtil.isEmpty(confirmPassword)) {
            encrypted = cryptoService.aesEncrypt(confirmPassword);
            if( null != encrypted) {
                authGroup.setConfirmPassword(encrypted);
            }
        }
    }

    private void decryptCredentials(byte[] password, byte[] confirmPassword, AuthGroupData authGroup) {
        byte[] decrypted;

        if (null != password && password.length > 0) {
            decrypted = cryptoService.aesDecrypt(password);
            if (null != decrypted) {
                authGroup.setPassword(new String(decrypted));
            }
        }

        if (null != confirmPassword && confirmPassword.length > 0) {
            decrypted = cryptoService.aesDecrypt(confirmPassword);
            if(null != decrypted) {
                authGroup.setConfirmPassword(new String(decrypted));
            }
        }

        log.info("Updated auth group data " + authGroup);
    }

    public List<Long> getNodeProfileByAuthGroup(Long authGroupId){
        return authGroupRepository.findNodeProfileByAuthGroup(authGroupId);
    }
}
