/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.rest.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.mapper.PetMapper;
import org.springframework.samples.petclinic.mapper.VisitMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.advice.ExceptionControllerAdvice;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.PetDto;
import org.springframework.samples.petclinic.rest.dto.PetTypeDto;
import org.springframework.samples.petclinic.rest.dto.VisitDto;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.samples.petclinic.service.clinicService.ApplicationTestConfig;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;


/**
 * Test class for {@link OwnerRestController}
 *
 * @author Vitaliy Fedoriv
 */
@SpringBootTest
@ContextConfiguration(classes = ApplicationTestConfig.class)
@WebAppConfiguration
class OwnerRestControllerTests {

    @Autowired
    private OwnerRestController ownerRestController;

    @Autowired
    private OwnerMapper ownerMapper;

    @Autowired
    private PetMapper petMapper;

    @Autowired
    private VisitMapper visitMapper;

    @MockitoBean
    private ClinicService clinicService;

    private MockMvc mockMvc;

    private List<OwnerDto> owners;

    private List<PetDto> pets;

    private List<VisitDto> visits;

    @BeforeEach
    void initOwners() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(ownerRestController)
            .setControllerAdvice(new ExceptionControllerAdvice())
            .build();
        owners = new ArrayList<>();

        OwnerDto ownerWithPet = new OwnerDto();
        owners.add(ownerWithPet.id(1).firstName("George").lastName("Franklin").address("110 W. Liberty St.").city("Madison").telephone("6085551023").addPetsItem(getTestPetWithIdAndName(1, "Rosy")));
        OwnerDto owner = new OwnerDto();
        owners.add(owner.id(2).firstName("Betty").lastName("Davis").address("638 Cardinal Ave.").city("Sun Prairie").telephone("6085551749"));
        owner = new OwnerDto();
        owners.add(owner.id(3).firstName("Eduardo").lastName("Rodriquez").address("2693 Commerce St.").city("McFarland").telephone("6085558763"));
        owner = new OwnerDto();
        owners.add(owner.id(4).firstName("Harold").lastName("Davis").address("563 Friendly St.").city("Windsor").telephone("6085553198"));

        PetTypeDto petType = new PetTypeDto();
        petType.id(2)
            .name("dog");

        pets = new ArrayList<>();
        PetDto pet = new PetDto();
        pets.add(pet.id(3)
            .name("Rosy")
            .birthDate(LocalDate.now())
            .type(petType));

        pet = new PetDto();
        pets.add(pet.id(4)
            .name("Jewel")
            .birthDate(LocalDate.now())
            .type(petType));

        visits = new ArrayList<>();
        VisitDto visit = new VisitDto();
        visit.setId(2);
        visit.setPetId(pet.getId());
        visit.setDate(LocalDate.now());
        visit.setDescription("rabies shot");
        visits.add(visit);

        visit = new VisitDto();
        visit.setId(3);
        visit.setPetId(pet.getId());
        visit.setDate(LocalDate.now());
        visit.setDescription("neutered");
        visits.add(visit);
    }

    private PetDto getTestPetWithIdAndName(final int id, final String name) {
        PetTypeDto petType = new PetTypeDto();
        PetDto pet = new PetDto();
        pet.id(id).name(name).birthDate(LocalDate.now()).type(petType.id(2).name("dog")).addVisitsItem(getTestVisitForPet(1));
        return pet;
    }

    private VisitDto getTestVisitForPet(final int id) {
        VisitDto visit = new VisitDto();
        return visit.id(id).date(LocalDate.now()).description("test" + id);
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @DisplayName("Id 1인 유저를 생성하면, get으로 불러왔을 때 Id 1인 유저를 반환할 것이다.")
    void testGetOwnerSuccess() throws Exception {
        given(this.clinicService.findOwnerById(1)).willReturn(ownerMapper.toOwner(owners.get(0)));
        this.mockMvc.perform(get("/api/owners/1")
                .accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.firstName").value("George"));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @DisplayName("Id 2인 유저를 생성하지 않으면, get으로 불러왔을 때 isNotFound를 발생시킨다.")
    void testGetOwnerNotFound() throws Exception {
        given(this.clinicService.findOwnerById(2)).willReturn(null);
        this.mockMvc.perform(get("/api/owners/2")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @DisplayName("ownerList를 성공적으로 반환하는지 검사한다.")
    void testGetOwnersListSuccess() throws Exception {
        owners.remove(0);
        owners.remove(1);
        given(this.clinicService.findOwnerByLastName("Davis")).willReturn(ownerMapper.toOwners(owners));
        this.mockMvc.perform(get("/api/owners?lastName=Davis")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.[0].id").value(2))
            .andExpect(jsonPath("$.[0].firstName").value("Betty"))
            .andExpect(jsonPath("$.[1].id").value(4))
            .andExpect(jsonPath("$.[1].firstName").value("Harold"));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @DisplayName("존재하지 않는 유저를 찾으려고 할 때 isNotFound를 발생시킨다.")
    void testGetOwnersListNotFound() throws Exception {
        owners.clear();
        given(this.clinicService.findOwnerByLastName("0")).willReturn(ownerMapper.toOwners(owners));
        this.mockMvc.perform(get("/api/owners?lastName=0")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @DisplayName("모든 owner를 잘 반환하는지 검사한다.")
    void testGetAllOwnersSuccess() throws Exception {
        owners.remove(0);
        owners.remove(1);
        given(this.clinicService.findAllOwners()).willReturn(ownerMapper.toOwners(owners));
        this.mockMvc.perform(get("/api/owners")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.[0].id").value(2))
            .andExpect(jsonPath("$.[0].firstName").value("Betty"))
            .andExpect(jsonPath("$.[1].id").value(4))
            .andExpect(jsonPath("$.[1].firstName").value("Harold"));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @DisplayName("빈 owners list에 대해 isNotFound를 발생시킨다.")
    void testGetAllOwnersNotFound() throws Exception {
        owners.clear();
        given(this.clinicService.findAllOwners()).willReturn(ownerMapper.toOwners(owners));
        this.mockMvc.perform(get("/api/owners")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @DisplayName("정상적으로 newOwnerDto를 생성하면, isCreated 상태가 된다.")
    void testCreateOwnerSuccess() throws Exception {
        OwnerDto newOwnerDto = owners.get(0);
        newOwnerDto.setId(null);
        ObjectMapper mapper = new ObjectMapper();
        String newOwnerAsJSON = mapper.writeValueAsString(newOwnerDto);
        this.mockMvc.perform(post("/api/owners")
                .content(newOwnerAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @DisplayName("newOwnderDto를 생성한 후, id와 firstname을 null으로 설정하면 BadRequest 상태가 된다.")
    void testCreateOwnerError() throws Exception {
        OwnerDto newOwnerDto = owners.get(0);
        newOwnerDto.setId(null);
        newOwnerDto.setFirstName(null);
        ObjectMapper mapper = new ObjectMapper();
        String newOwnerAsJSON = mapper.writeValueAsString(newOwnerDto);
        this.mockMvc.perform(post("/api/owners")
                .content(newOwnerAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @DisplayName("owner 정보를 업데이트(id, FirstNamme, LastName, Address, City, Telephone)하면 owner의 정보가 정상적으로 업데이트된다.")
    void testUpdateOwnerSuccess() throws Exception {
        given(this.clinicService.findOwnerById(1)).willReturn(ownerMapper.toOwner(owners.get(0)));
        int ownerId = owners.get(0).getId();
        OwnerDto updatedOwnerDto = new OwnerDto();
        // body.id = ownerId which is used in url path
        updatedOwnerDto.setId(ownerId);
        updatedOwnerDto.setFirstName("GeorgeI");
        updatedOwnerDto.setLastName("Franklin");
        updatedOwnerDto.setAddress("110 W. Liberty St.");
        updatedOwnerDto.setCity("Madison");
        updatedOwnerDto.setTelephone("6085551023");
        ObjectMapper mapper = new ObjectMapper();
        String newOwnerAsJSON = mapper.writeValueAsString(updatedOwnerDto);
        this.mockMvc.perform(put("/api/owners/" + ownerId)
                .content(newOwnerAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().contentType("application/json"))
            .andExpect(status().isNoContent());

        this.mockMvc.perform(get("/api/owners/" + ownerId)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(ownerId))
            .andExpect(jsonPath("$.firstName").value("GeorgeI"));

    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @DisplayName("owner 정보를 업데이트 할 때 id를 명시해주지 않아도 업데이트된 정보가 잘 반영된다.")
    void testUpdateOwnerSuccessNoBodyId() throws Exception {
        given(this.clinicService.findOwnerById(1)).willReturn(ownerMapper.toOwner(owners.get(0)));
        int ownerId = owners.get(0).getId();
        OwnerDto updatedOwnerDto = new OwnerDto();
//        updatedOwnerDto.setId(ownerId);
        updatedOwnerDto.setFirstName("GeorgeI");
        updatedOwnerDto.setLastName("Franklin");
        updatedOwnerDto.setAddress("110 W. Liberty St.");
        updatedOwnerDto.setCity("Madison");

        updatedOwnerDto.setTelephone("6085551023");
        ObjectMapper mapper = new ObjectMapper();
        String newOwnerAsJSON = mapper.writeValueAsString(updatedOwnerDto);
        this.mockMvc.perform(put("/api/owners/" + ownerId)
                .content(newOwnerAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().contentType("application/json"))
            .andExpect(status().isNoContent());

        this.mockMvc.perform(get("/api/owners/" + ownerId)
                .accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(ownerId))
            .andExpect(jsonPath("$.firstName").value("GeorgeI"));

    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @DisplayName("owner 정보를 업데이트 할 때, FirstName을 null으로 설정하면 BadRequest 상태가 된다.")
    void testUpdateOwnerError() throws Exception {
        OwnerDto newOwnerDto = owners.get(0);
        newOwnerDto.setFirstName("");
        ObjectMapper mapper = new ObjectMapper();
        String newOwnerAsJSON = mapper.writeValueAsString(newOwnerDto);
        this.mockMvc.perform(put("/api/owners/1")
                .content(newOwnerAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @DisplayName("id=1인 owner를 삭제하면, id=1로 접속했을 때 isNoContent 상태가 된다.")
    void testDeleteOwnerSuccess() throws Exception {
        OwnerDto newOwnerDto = owners.get(0);
        ObjectMapper mapper = new ObjectMapper();
        String newOwnerAsJSON = mapper.writeValueAsString(newOwnerDto);
        final Owner owner = ownerMapper.toOwner(owners.get(0));
        given(this.clinicService.findOwnerById(1)).willReturn(owner);
        this.mockMvc.perform(delete("/api/owners/1")
                .content(newOwnerAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @DisplayName("존재하지 않는 owner(id=999)를 삭제하려 했을 때 isNotFound 상태가 된다.")
    void testDeleteOwnerError() throws Exception {
        OwnerDto newOwnerDto = owners.get(0);
        ObjectMapper mapper = new ObjectMapper();
        String newOwnerAsJSON = mapper.writeValueAsString(newOwnerDto);
        given(this.clinicService.findOwnerById(999)).willReturn(null);
        this.mockMvc.perform(delete("/api/owners/999")
                .content(newOwnerAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @DisplayName("pet의 id를 정상적으로(id=999) 설정하면, 등록이 정상적으로 진행되어 isCreated 상태가 된다.")
    void testCreatePetSuccess() throws Exception {
        PetDto newPet = pets.get(0);
        newPet.setId(999);
        ObjectMapper mapper =  JsonMapper.builder()
            .defaultDateFormat(new SimpleDateFormat("dd/MM/yyyy"))
            .build();
        String newPetAsJSON = mapper.writeValueAsString(newPet);
        System.err.println("--> newPetAsJSON=" + newPetAsJSON); // TODO: 뭐임? 
        this.mockMvc.perform(post("/api/owners/1/pets")
                .content(newPetAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @DisplayName("pet의 id와 name을 null로 설정하면, 등록이 진행되지 않고 isBadRequest 상태가 된다.")
    void testCreatePetError() throws Exception {
        PetDto newPet = pets.get(0);
        newPet.setId(null);
        newPet.setName(null);
        ObjectMapper mapper =  JsonMapper.builder()
            .defaultDateFormat(new SimpleDateFormat("dd/MM/yyyy"))
            .build();
        String newPetAsJSON = mapper.writeValueAsString(newPet);
        this.mockMvc.perform(post("/api/owners/1/pets")
                .content(newPetAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest()).andDo(MockMvcResultHandlers.print());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @DisplayName("pet의 id를 정상적으로(id=999) 설정한 후, 해당 TODO로 방문하면 isCreated 상태가 된다.")
    void testCreateVisitSuccess() throws Exception {
        VisitDto newVisit = visits.get(0);
        newVisit.setId(999);
        ObjectMapper mapper = new ObjectMapper();
        String newVisitAsJSON = mapper.writeValueAsString(visitMapper.toVisit(newVisit));
        System.out.println("newVisitAsJSON " + newVisitAsJSON);
        this.mockMvc.perform(post("/api/owners/1/pets/1/visits")
                .content(newVisitAsJSON).accept(MediaType.APPLICATION_JSON_VALUE).contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @DisplayName("owner와 pet을 정상적으로 설정하면, id=2인 owner의 pet 중, id=1인 pet의 정보를 반환한다.")
    void testGetOwnerPetSuccess() throws Exception {
        var owner = ownerMapper.toOwner(owners.get(0));
        given(this.clinicService.findOwnerById(2)).willReturn(owner);
        var pet = petMapper.toPet(pets.get(0));
        pet.setOwner(owner);
        given(this.clinicService.findPetById(1)).willReturn(pet);
        this.mockMvc.perform(get("/api/owners/2/pets/1")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"));
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @DisplayName("owner가 비어있을 때, owner를 통해 pet의 정보를 확인하면 isNotFound를 반환한다.")
    void testGetOwnersPetsWithOwnerNotFound() throws Exception {
        owners.clear();
        given(this.clinicService.findAllOwners()).willReturn(ownerMapper.toOwners(owners));
        this.mockMvc.perform(get("/api/owners/1/pets/1")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @DisplayName("정상적으로 정의된 owner에 대해 연결된 pet이 없을 때, 해당 pet의 정보를 확인하려고 하면 isNotFound를 반환한다.")
    void testGetOwnersPetsWithPetNotFound() throws Exception {
        var owner1 = ownerMapper.toOwner(owners.get(0));
        given(this.clinicService.findOwnerById(1)).willReturn(owner1);
        this.mockMvc.perform(get("/api/owners/1/pets/2")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }


    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @DisplayName("정상적으로 정의되고 연결된 owner, pet에 대해, 해당 pet의 정보를 확인하려고 하면 엥?") // TODO: 확인필요. 왜 isNoContent를 기대하는지? 
    void testUpdateOwnersPetSuccess() throws Exception {
        int ownerId = owners.get(0).getId();
        int petId = pets.get(0).getId();
        given(this.clinicService.findOwnerById(ownerId)).willReturn(ownerMapper.toOwner(owners.get(0)));
        given(this.clinicService.findPetById(petId)).willReturn(petMapper.toPet(pets.get(0)));
        PetDto updatedPetDto = pets.get(0);
        updatedPetDto.setName("Rex");
        updatedPetDto.setBirthDate(LocalDate.of(2020, 1, 15));
        ObjectMapper mapper =  JsonMapper.builder()
            .defaultDateFormat(new SimpleDateFormat("dd/MM/yyyy"))
            .build();
        String updatedPetAsJSON = mapper.writeValueAsString(updatedPetDto);
        this.mockMvc.perform(put("/api/owners/" + ownerId + "/pets/" + petId)
                .content(updatedPetAsJSON)
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isNoContent());
    }

    // TODO: DisplayName 마저 달기
    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    @DisplayName("")
    void testUpdateOwnersPetOwnerNotFound() throws Exception {
        int ownerId = 0;
        int petId = pets.get(0).getId();
        given(this.clinicService.findOwnerById(ownerId)).willReturn(null);
        PetDto petDto = pets.get(0);
        petDto.setName("Thor");
        ObjectMapper mapper =  JsonMapper.builder()
            .defaultDateFormat(new SimpleDateFormat("dd/MM/yyyy"))
            .build();
        String updatedPetAsJSON = mapper.writeValueAsString(petDto);
        this.mockMvc.perform(put("/api/owners/" + ownerId + "/pets/" + petId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatedPetAsJSON))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "OWNER_ADMIN")
    void testUpdateOwnersPetPetNotFound() throws Exception {
        int ownerId = owners.get(0).getId();
        int petId = 0;
        given(this.clinicService.findOwnerById(ownerId)).willReturn(ownerMapper.toOwner(owners.get(0)));
        given(this.clinicService.findPetById(petId)).willReturn(null);
        PetDto petDto = pets.get(0);
        petDto.setName("Ghost");
        petDto.setBirthDate(LocalDate.of(2020, 1, 1));
        ObjectMapper mapper =  JsonMapper.builder()
            .defaultDateFormat(new SimpleDateFormat("dd/MM/yyyy"))
            .build();
        String updatedPetAsJSON = mapper.writeValueAsString(petDto);
        this.mockMvc.perform(put("/api/owners/" + ownerId + "/pets/" + petId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updatedPetAsJSON))
            .andExpect(status().isNotFound());
    }

}
