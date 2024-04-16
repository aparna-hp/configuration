package com.cisco.configService.controller;

import com.cisco.configService.model.preConfig.AuthGroupData;
import com.cisco.configService.service.AuthGroupService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CORSTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    AuthGroupService authGroupService;

    @Test
    public void test_corsFilterBean() throws Exception {
        AuthGroupData authGroup = new AuthGroupData();
        authGroup.setName("authGroupCORSTest");
        authGroup.setLoginType(AuthGroupData.LoginType.TELNET);
        authGroup.setUsername("username");
        authGroup.setPassword("pwsd");
        authGroup.setConfirmPassword("pwsd");

        Mockito.doReturn(List.of(authGroup)).when(authGroupService).getAllAuthGroups();
        Mockito.doReturn(Optional.empty()).when(authGroupService).getAuthGroup(2L);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/auth-groups"))
                .andExpect(status().isOk())
                .andExpect(header().string("Vary", "Origin"))
                .andDo(print())
                .andReturn();

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/auth-groups/id/2"))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Vary", "Origin"))
                .andDo(print())
                .andReturn();
    }
}
