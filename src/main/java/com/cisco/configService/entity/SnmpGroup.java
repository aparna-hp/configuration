package com.cisco.configService.entity;

import com.cisco.configService.enums.SnmpSecurityLevel;
import com.cisco.configService.model.preConfig.SnmpGroupData;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.UniqueElements;
import org.springframework.data.annotation.Id;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class SnmpGroup {

    @Id
    Long id;

    @UniqueElements
    private String name;

    private SnmpGroupData.SnmpType snmpType;

    private SnmpSecurityLevel securityLevel;

    private String username;

    private SnmpGroupData.AuthenticationProtocol authenticationProtocol;

    private byte[] authenticationPassword;

    private SnmpGroupData.EncryptionProtocol encryptionProtocol;

    private byte[] encryptionPassword;

    private String roCommunity;

    private LocalDate updateDate;

}
