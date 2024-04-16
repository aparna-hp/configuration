Insert into NETWORK (ID ,NAME, DRAFT, DRAFT_CONFIG)  values
( 1 ,'Network_1', 'false', '' );

Insert into COLLECTOR ( ID, NAME , PARAMS, SOURCE_COLLECTOR ,TYPE) values
(1, 'IGP_COLLECTOR', '{"igpConfigs":[{"igpIndex":1,"seedRouter":"10.225.120.62","igpProtocol":"isis", "advanced": {"loginConfig": {"encodedTelnetPassword": "cYECb4rNBy0H3Pf3JSlwTA==","telnetUserName": "cisco"}}}],"collectInterfaces":true}' , null, 'TOPO_IGP'),
(2, 'BGPLS_COLLECTOR', '{"primarySrPceAgent":1,"secondarySrPceAgent":2,"igpProtocol":"ISIS","extendedTopologyDiscovery":true}', null, 'TOPO_BGPLS_XTC');

Insert into AGENTS ( ID ,NAME ,PARAMS, TYPE) values
(1, 'SR_PCE_AGENT', '{"enabled":true,"xtcHostIP":"10.225.120.119","xtcRestPort":2020,"authenticationType":"BASIC","authGroup":"authGroup","batchSize":1000,"maxLspHistory":0,"keepAliveInterval":10,"connectionTimeoutInterval":60,"connectionRetryCount":3,"keepAliveThreshold":2,"netRecorderMode":"OFF","eventBufferingEnabled":true,"playbackEventsDelay":null,"eventsBufferTime":30,"poolSize":null,"topologyCollection":"COLLECTION_AND_SUBSCRIPTION","lspCollection":"COLLECTION_AND_SUBSCRIPTION"}', 'SR_PCE_AGENT' );

Insert into AUTH_GROUP (ID ,NAME,LOGIN_TYPE,USERNAME) values
(1, 'auth_group', 'TELNET', 'cisco' ),
(2, 'auth_group_2', 'TELNET', 'cisco' );

Insert into SNMP_GROUP (ID ,NAME ,SNMP_TYPE ,USERNAME ,RO_COMMUNITY ,SECURITY_LEVEL ,AUTHENTICATION_PROTOCOL,AUTHENTICATION_PASSWORD ,ENCRYPTION_PROTOCOL ,ENCRYPTION_PASSWORD) values
(1, 'snmp_group', 'SNMPv2c', null, 'cisco', null, null, null, null, null),
(2, 'snmp_group_2', 'SNMPv2c', null, 'cisco', null, null, null, null, null);

Insert into NODE_PROFILE (ID ,NAME, DEFAULT_AUTH_GROUP, DEFAULT_SNMP_GROUP, USE_NODE_LIST_AS_INCLUDE_FILTER, UPDATE_DATE ) values
(1, 'node_profile', 'auth_group', 'snmp_group', 'true', '2023-01-10');

Insert into NODE_LIST (ID ,NODE_IP ,NODE_MANAGEMENT_IP) VALUES
(1, '1.1.1.1', '10.10.10.10');

Insert into NODE_FILTER (ID ,TYPE ,CONDITION ,VALUE, ENABLED ) VALUES
(1, 'HOST_REGEX', 'INCLUDE', 'HOST%', 'TRUE');

Insert into IMPORT_HISTORY ( ID, TYPE, STATUS) values
(1, 'CP', 'SUCCESS');

Insert into NODE_PROFILE_REF (ID ,NODE_PROFILE_ID ,NETWORK ) VALUES
(1,1,1);

Insert into COLLECTOR_REF (ID ,COLLECTOR_ID ,NETWORK) VALUES
(1,1,1),
(2,2,1);

Insert into AGENT_REF (ID ,AGENT_ID ,COLLECTOR) VALUES
(1,1,2);

Insert into NODE_FILTER_REF (ID ,NODE_FILTER_ID ,NODE_PROFILE) VALUES
(1,1,1);

Insert into NODE_LIST_REF (ID ,NODE_LIST_ID ,NODE_PROFILE) values
(1,1,1);

Insert into AUTH_GROUP_REF (ID ,AUTH_GROUP_ID ,NODE_LIST) values
(1,1,1);

Insert into SNMP_GROUP_REF (ID ,SNMP_GROUP_ID ,NODE_LIST) values
(1,1,1);