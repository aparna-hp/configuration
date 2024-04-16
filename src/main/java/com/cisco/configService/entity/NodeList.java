package com.cisco.configService.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;


@Data
public class NodeList {

    @Id
    Long id;

    private String nodeIp;

    private String nodeManagementIp;

    private AuthGroupRef authGroup;

    private SnmpGroupRef snmpGroup;

    public void addAuthGroupRef(AuthGroup authGroup) {
        this.authGroup = createAuthGroupRef(authGroup);
    }

    private AuthGroupRef createAuthGroupRef(AuthGroup authGroup) {
        AuthGroupRef authGroupRef = new AuthGroupRef();
        authGroupRef.setAuthGroupId(authGroup.getId());
        return authGroupRef;
    }

    public void addSnmpGroupRef(SnmpGroup snmpGroup) {
        this.snmpGroup = createSnmpGroupRef(snmpGroup);
    }

    private SnmpGroupRef createSnmpGroupRef(SnmpGroup snmpGroup) {
        SnmpGroupRef snmpGroupRef = new SnmpGroupRef();
        snmpGroupRef.setSnmpGroupId(snmpGroup.getId());
        return snmpGroupRef;
    }

}
