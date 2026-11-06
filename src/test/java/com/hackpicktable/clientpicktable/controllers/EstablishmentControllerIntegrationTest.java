package com.hackpicktable.clientpicktable.controllers;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import com.hackpicktable.clientpicktable.dtos.EstablishmentDTO;
import com.hackpicktable.clientpicktable.impl.RefreshEstablishmentInDBImpl;

@WebMvcTest(EstablishmentController.class)
class EstablishmentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RefreshEstablishmentInDBImpl refreshEstablishmentInDB;

    @Test
    void getEstablishmentRefreshesAndReturnsEstablishment() throws Exception {
        String establishmentName = "zao-cafe";
        UUID establishmentId = UUID.fromString("6e10b641-2212-4efc-8ed7-c890d54f3f90");
        EstablishmentDTO establishment = new EstablishmentDTO();
        establishment.setId(establishmentId);
        establishment.setName("Zao Cafe");
        establishment.setSlug(establishmentName);

        given(refreshEstablishmentInDB.refreshTablesInDB(establishmentName)).willReturn(establishment);

        mockMvc.perform(get("/establishment/{establishmentName}", establishmentName))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(establishmentId.toString()))
                .andExpect(jsonPath("$.name").value("Zao Cafe"))
                .andExpect(jsonPath("$.slug").value(establishmentName));

        verify(refreshEstablishmentInDB).refreshTablesInDB(establishmentName);
    }
}
