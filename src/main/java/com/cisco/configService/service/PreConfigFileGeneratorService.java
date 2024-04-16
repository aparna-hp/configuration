package com.cisco.configService.service;


import com.cisco.collectionService.utils.StringUtil;
import com.cisco.configService.AppPropertiesReader;
import com.cisco.configService.enums.SnmpSecurityLevel;
import com.cisco.configService.model.DeviceCredentials;
import com.cisco.configService.model.preConfig.AuthGroupData;
import com.cisco.configService.model.preConfig.NodeListData;
import com.cisco.configService.model.preConfig.NodeProfileData;
import com.cisco.configService.model.preConfig.SnmpGroupData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.cisco.configService.enums.SnmpSecurityLevel.valueOf;

@Slf4j
@Service
public class PreConfigFileGeneratorService {

    @Autowired
    AuthGroupService authGroupService;

    @Autowired
    SnmpGroupService snmpGroupService;

    @Autowired
    CryptoService cryptoService;

    static final String NEW_LINE = "\n", TAB = "\t",
            MANAGE_IP_FILE_NAME_PREFIX = "_ip_manage.txt",
            AUTH_FILE_NAME_PREFIX = "_auth_file.txt",
            NET_ACESS_NAME_PREFIX = "_network_access.txt",
            NETWORK_ACCESS_FILE_NAME = "network-access.txt";

    static String FILE_SEPERATOR = "/";

    private static final String mp = "cariden";

    @Autowired
    AppPropertiesReader conf;

    @Autowired
    FileGatewayService fileGatewayService;

    /**
     * Generates IPManage file for given node-profile.
     *
     * @param nodeProfileData node profile details
     */
    public boolean generateManageIpFile(NodeProfileData nodeProfileData) {
        try {

            StringBuilder mangaeIpContent = new StringBuilder("<SystemToManagement>")
                    .append(NEW_LINE)
                    .append("IPAddress")
                    .append(TAB)
                    .append("IPManage")
                    .append(NEW_LINE);

            //Store the node ip address to management Ip address mapping.
            for (NodeListData nodeList : nodeProfileData.getNodeLists()) {
                String ipManage = nodeList.getNodeManagementIp();
                if (ipManage == null) {
                    continue;
                }
                mangaeIpContent.append(nodeList.getNodeIp())
                        .append(TAB)
                        .append(ipManage)
                        .append(NEW_LINE);
            }

            // table
            mangaeIpContent.append("<SystemToManagementDefaultRule>").append(NEW_LINE)
                    .append("Rule").append(NEW_LINE)
                    .append("IdentityTransformer").append(NEW_LINE);

            log.debug("ManagementIP content " + mangaeIpContent);

            //Generate the file name.
            String fileName = nodeProfileData.getId() + MANAGE_IP_FILE_NAME_PREFIX;

            log.info("Uploading the management Ip file with name {} " + fileName);
            return uploadStringToMinio(String.valueOf(mangaeIpContent).getBytes(), fileName);

        } catch (Exception e) {
            log.error("Error storing the management Ip file in minio.", e);
        }
        return false;
    }

    /**
     * Generates Auth file for given node-profile.
     *
     * @param nodeProfileData node profile details
     * Returns the path where the auth file got uploaded.
     */
    public boolean generateAuthFile(NodeProfileData nodeProfileData) {
            String authText = getAuthFileContents(nodeProfileData);
            byte[] encryptedAuth = cryptoService.aesEncrypt(authText);

            if (null != encryptedAuth) {
                //Generate the file name.
                String fileName = nodeProfileData.getId()  + AUTH_FILE_NAME_PREFIX ;
                return uploadStringToMinio(encryptedAuth, fileName);
            }

        return false;
    }

    public String getAuthFileContents(NodeProfileData nodeProfileData) {
        StringBuilder authText = new StringBuilder();
        try {
            Map<String, DeviceCredentials> ipToCredentials = populateIpToCredentials(nodeProfileData);
            DeviceCredentials defaultDeviceCredentials = fetchDeviceCredentials(".*", nodeProfileData.getDefaultAuthGroup(),
                    nodeProfileData.getDefaultSnmpGroup());

            authText.append("#").append(NEW_LINE);
            authText.append("# This Network Authorization File is generated automatically\n");
            authText.append("#").append(NEW_LINE).append(NEW_LINE);
            authText.append("<MasterPassword>").append(NEW_LINE);
            authText.append("Password").append(NEW_LINE);
            authText.append(mp);
            authText.append(NEW_LINE);
            authText.append("<CommunityTable>").append(NEW_LINE);
            authText.append("IPRegExp\tCommunity").append(NEW_LINE);

            //Loop through the ip manage to device credentials map
            // to populate the ip manage & community nmae
            for (String ip : ipToCredentials.keySet()) {
                if (ipToCredentials.get(ip).isSnmpV3()) {
                    // no point in putting the community if we 're just going to use V3
                    continue;
                }
                String userName = ipToCredentials.get(ip).getSnmpCommunityName();
                if (StringUtil.isEmpty(userName)) {
                    continue;
                }
                authText.append(ip);
                authText.append(TAB);
                authText.append(userName);
                authText.append(NEW_LINE);

                authText.append(ipToCredentials.get(ip).getNodeIp());
                authText.append(TAB);
                authText.append(userName);
                authText.append(NEW_LINE);
            }

            if(null != nodeProfileData.getDefaultSnmpGroup() &&
                    ! StringUtil.isEmpty(defaultDeviceCredentials.getSnmpCommunityName())) {
                log.debug("Inserting default snmp community name " + defaultDeviceCredentials.getSnmpCommunityName());
                authText.append(defaultDeviceCredentials.getNodeIp());
                authText.append(TAB);
                authText.append(defaultDeviceCredentials.getSnmpCommunityName());
                authText.append(NEW_LINE);
            }

            authText.append("<UserTable>").append(NEW_LINE);
            authText.append("IPRegExp\tLoginType\tUsername\tPassword\tEnablePassword").append(NEW_LINE);
            for (String ip : ipToCredentials.keySet()) {
                DeviceCredentials creds = ipToCredentials.get(ip);
                if (StringUtil.isEmpty(creds.getRemoteName())) {
                    // user name is empty so lets continue
                    continue;
                }
                authText.append(ip);
                authText.append(TAB);
                authText.append(creds.getRemoteLoginType());
                authText.append(TAB);
                authText.append(creds.getRemoteName());
                authText.append(TAB);
                authText.append(creds.getRemotePassword());
                authText.append(TAB);
                authText.append(creds.getRemoteEnablePassword());
                authText.append(NEW_LINE);
            }

            if(null != nodeProfileData.getDefaultAuthGroup() &&
                    !StringUtil.isEmpty(defaultDeviceCredentials.getRemoteName())) {
                authText.append(defaultDeviceCredentials.getNodeIp());
                authText.append(TAB);
                authText.append(defaultDeviceCredentials.getRemoteLoginType());
                authText.append(TAB);
                authText.append(defaultDeviceCredentials.getRemoteName());
                authText.append(TAB);
                authText.append(defaultDeviceCredentials.getRemotePassword());
                authText.append(TAB);
                authText.append(defaultDeviceCredentials.getRemoteEnablePassword());
                authText.append(NEW_LINE);
            }

            authText.append("<SNMPv3ProfileTable>").append(NEW_LINE);
            authText.append("ProfileName\tSecurityLevel\tUsername\tAuthProtocol"
                    + "\tAuthPassword\tEncryptionProtocol\tEncryptionPassword\tContextName").append(NEW_LINE);

            for (String ip : ipToCredentials.keySet()) {
                log.debug("Populating SNMPv3 for ip " + ip);
                if (StringUtil.isEmpty(ip)) {
                    continue;
                }
                DeviceCredentials cred = ipToCredentials.get(ip);
                log.debug("Cred for ip " + cred + cred.getSnmpRemoteName());
                if (StringUtil.isEmpty(cred.getSnmpRemoteName())) {
                    continue;
                }

                authText.append(cred.getSnmpGroupName());
                authText.append(TAB).append(secLvl(cred.getSnmpSecurityLevel()));
                authText.append(TAB).append(cred.getSnmpRemoteName());
                authText.append(TAB).append(StringUtil.nullToEmpty(cred.getSnmpAuthProtocol()).toUpperCase());
                authText.append(TAB).append(StringUtil.nullToEmpty(cred.getSnmpAuthPassword()));
                authText.append(TAB).append(StringUtil.nullToEmpty(cred.getSnmpEncProtocol()).toUpperCase());
                authText.append(TAB).append(StringUtil.nullToEmpty(cred.getSnmpEncPassword()));
                authText.append(TAB);
                authText.append(NEW_LINE);
            }

            authText.append("<SNMPv3MappingTable>\n");
            authText.append("IPRegEx\tProfileName\n");

            for (String ip : ipToCredentials.keySet()) {
                DeviceCredentials cred = ipToCredentials.get(ip);
                if (StringUtil.isEmpty(cred.getSnmpRemoteName())) {
                    continue;
                }
                authText.append(ip);
                authText.append(TAB).append(cred.getSnmpGroupName());
                authText.append(NEW_LINE);
            }
        } catch (Exception e) {
            log.error("Error storing the auth file file to minio.", e);
        }
        log.info("Auth file contents " + authText);
        return authText.toString();
    }

    private Map<String, DeviceCredentials> populateIpToCredentials(NodeProfileData nodeProfileData) {
        Map<String, DeviceCredentials> ipToCredentials = new HashMap<>();
        for (NodeListData nodeListData : nodeProfileData.getNodeLists()) {
            ipToCredentials.putIfAbsent(nodeListData.getNodeManagementIp(),
                    fetchDeviceCredentials(nodeListData.getNodeIp(),
                            nodeListData.getAuthGroupName(), nodeListData.getSnmpGroupName()));
        }
        return ipToCredentials;
    }

    /**
     * Store Net Access file in the shared File System
     */
    public boolean generateNetAccess(long networkProfileId) {
        String fileName = networkProfileId + NET_ACESS_NAME_PREFIX;
        InputStream networkAccessFile = null;
        try {
            networkAccessFile = fileGatewayService.downloadFile(null, NETWORK_ACCESS_FILE_NAME);
            if (null == networkAccessFile) {
                ClassPathResource classPathResource = new ClassPathResource(conf.getNetworkAccessPath());
                networkAccessFile = classPathResource.getInputStream();
            }
            return fileGatewayService.saveFileStream(networkAccessFile,
                    conf.getNetworkProfileDirectory(), fileName);
        } catch (Exception e) {
            log.error("Error uploading the network access file", e);
        } finally {
            Optional.ofNullable(networkAccessFile).ifPresent(file -> {
                try {
                    file.close();
                } catch (IOException e) {
                    log.error("Error closing the input stream associated with network access", e);
                }
            });
        }
        return false;
    }

    private String secLvl(String snmpSecurityLevel) {
        if(StringUtil.isEmpty(snmpSecurityLevel)) {
            log.debug("Empty snmpSecurityLevel ");
            return "";
        }
        SnmpSecurityLevel securityLevel = valueOf(snmpSecurityLevel);
        switch (securityLevel) {
            case AUTH_NOPRIV:
                return "authNoPriv";
            case AUTH_PRIV:
                return "authPriv";
            case NOAUTH_NOPRIV:
                return "noAuthNoPriv";
            default:
                log.warn("Unrecognized snmpSecurityLevel " + snmpSecurityLevel);
                return "";
        }
    }

    public DeviceCredentials fetchDeviceCredentials(final String nodeIp, final String authGroupName, final String snmpGroupName) {
        log.debug("Fetching the device credentials for auth group {} and snmp group {}",
                authGroupName, snmpGroupName);
        final DeviceCredentials creds = new DeviceCredentials();
        creds.setNodeIp(nodeIp);
        if (!StringUtil.isEmpty(authGroupName)) {
            Optional<AuthGroupData> authGroupDataOptional = authGroupService.getAuthGroupByName(authGroupName);
            if (authGroupDataOptional.isPresent()) {
                AuthGroupData authGroupData = authGroupDataOptional.get();
                creds.setRemoteLoginType(null == authGroupData.getLoginType()?
                        AuthGroupData.LoginType.TELNET.name() : authGroupData.getLoginType().name());
                creds.setRemoteName(authGroupData.getUsername());
                creds.setRemotePassword(authGroupData.getPassword());
                creds.setRemoteEnablePassword(authGroupData.getConfirmPassword());
            }
        }

        if (!StringUtil.isEmpty(snmpGroupName)) {
            Optional<SnmpGroupData> snmpGroupDataOptional = snmpGroupService.getSnmpGroupByName(snmpGroupName);
            if (snmpGroupDataOptional.isPresent()) {
                SnmpGroupData snmpGroupData = snmpGroupDataOptional.get();
                creds.setSnmpGroupName(snmpGroupData.getName());
                SnmpGroupData.SnmpType snmpType = SnmpGroupData.SnmpType.SNMPv2c;
                if (null != snmpGroupData.getSnmpType()) {
                    snmpType = snmpGroupData.getSnmpType();
                }
                creds.setSnmpType(snmpType.name());
                if (snmpType.equals(SnmpGroupData.SnmpType.SNMPv2c)) {
                    creds.setSnmpCommunityName(snmpGroupData.getRoCommunity());
                } else {
                    //CSCwi76247: Do not set the snmp username to remote name field.
                    //creds.setRemoteName(snmpGroupData.getUsername());
                    creds.setSnmpRemoteName(snmpGroupData.getUsername());
                    creds.setSnmpAuthPassword(snmpGroupData.getEncryptionPassword());


                    if (null != snmpGroupData.getSecurityLevel()) {
                        creds.setSnmpSecurityLevel(snmpGroupData.getSecurityLevel().name());
                    }
                    if (null != snmpGroupData.getAuthenticationProtocol()) {
                        creds.setSnmpAuthProtocol(snmpGroupData.getAuthenticationProtocol().name());
                    }
                    creds.setSnmpAuthPassword(snmpGroupData.getAuthenticationPassword());
                    if (null != snmpGroupData.getEncryptionProtocol()) {
                        creds.setSnmpEncProtocol(snmpGroupData.getEncryptionProtocol().name());
                        creds.setSnmpEncPassword(snmpGroupData.getEncryptionPassword());
                    }
                }
            }
        }
        return creds;
    }

    private boolean uploadStringToMinio(byte[] data, String fileName) {
        try (InputStream stream = new ByteArrayInputStream(data)) {

            //save the file to minio
            return fileGatewayService.saveFileStream(stream,
                    conf.getNetworkProfileDirectory(), fileName);
        } catch (Exception e) {
            log.error("Error saving the string input stream ", e);
        }
        return false;
    }
}
