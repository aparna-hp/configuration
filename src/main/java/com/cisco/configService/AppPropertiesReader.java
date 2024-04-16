package com.cisco.configService;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class AppPropertiesReader {

    @Value("${app.workflow.baseUrl}")
    private String workflowBaseUrl;

    @Value("${app.workflow.jobs}")
    private String workflowJobUrl;

    @Value("${app.workflow.network.status}")
    private String workflowNetworkStatus;

    @Value("${app.workflow.job.status}")
    private String workflowJobStatus;

    @Value("${app.workflow.taskHistory}")
    private String workflowTaskHistory;

    @Value("${app.workflow.pause}")
    private String workflowPauseJob;

    @Value("${app.workflow.resume}")
    private String workflowResumeJob;

    @Value("${app.workflow.execute}")
    private String workflowExecuteJob;

    @Value("${app.workflow.abort}")
    private String workflowAbortJob;

    @Value("${app.workflow.network.resync}")
    private String workflowResyncJob;

    @Value("${app.workflow.network.delete}")
    private String workflowDeleteSchedulersOfNetwork;

    @Value("${app.workflow.collector.delete}")
    private String workflowDeleteCollectorTasks;

    @Value("${app.workflow.job.stats}")
    private String workflowJobStats;

    @Value("${app.workflow.agent.job}")
    private String workflowAgentJob;

    @Value("${app.srpce.baseUrl}")
    private String srpceAgentBaseUrl;

    @Value("${app.srpce.status}")
    private String srpceAgentStatus;

    @Value("${app.netflow.baseUrl}")
    private String netflowBaseUrl;

    @Value("${app.netflow.status}")
    private String netflowStatus;

    @Value("${app.traffic.poller.baseUrl}")
    private String trafficPollerBaseUrl;

    @Value("${app.traffic.poller.status}")
    private String trafficPollerStatus;

    @Value("${app.srpce.all.status}")
    private String srpceAllAgentStatus;

    @Value("${app.srpce.bgpls.stop}")
    private String srpceStopBgpls;

    @Value("${app.srpce.pcep.stop}")
    private String srpceStopPcep;

    @Value("${app.aggregator.baseUrl}")
    private String aggregatorBaseUrl;

    @Value("${app.aggregator.script.properties.validator}")
    private String aggregatorScriptPropertiesValidator;

    @Value("${app.aggregator.script.properties}")
    private String aggregatorScriptProperties;

    @Value("${app.aggregator.config.get}")
    private String aggregatorConfigGet;

    @Value("${app.aggregator.config.update}")
    private String aggregatorConfigUpdate;

    @Value("${app.aggregator.config.reset}")
    private String aggregatorConfigReset;

    @Value("${app.aggregator.aging}")
    private String aggregatorAging;

    @Value("${mount.directory}")
    private String mountDirectory;

    @Value("${mount.network.directory}")
    private String networkDirectory;

    @Value("${mount.network.profile.directory}")
    private String networkProfileDirectory;

    @Value("${mount.user.upload.directory}")
    private String userUploadDirectory;

    @Value("${mount.data.directory}")
    private String dataDirectory;

    @Value("${mount.agents.directory}")
    private String agentDirectory;

    @Value("${mount.agents.srpce.directory}")
    private String srpceAgentDirectory;

    @Value("${mount.network.folder.suffix}")
    private String networkFolderSuffix;

    @Value("${resource.network.profile.path}")
    private String networkAccessPath;

}
