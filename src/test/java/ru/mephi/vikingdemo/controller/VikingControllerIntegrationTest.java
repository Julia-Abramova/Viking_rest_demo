package ru.mephi.vikingdemo.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;
import ru.mephi.vikingdemo.model.BeardStyle;
import ru.mephi.vikingdemo.model.EquipmentItem;
import ru.mephi.vikingdemo.model.HairColor;
import ru.mephi.vikingdemo.model.Viking;
import ru.mephi.vikingdemo.repository.VikingRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        properties = {
                "spring.datasource.url=jdbc:h2:mem:viking-controller-test;DB_CLOSE_DELAY=-1",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.sql.init.mode=always"
        }
)
class VikingControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VikingRepository vikingRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        vikingRepository.deleteAll();
    }

    @Test
    void createsSpecificViking() throws Exception {
        Viking request = new Viking(
                null,
                "Bjorn",
                31,
                184,
                HairColor.Blond,
                BeardStyle.BRAIDED,
                List.of(new EquipmentItem("Iron Axe", "Rare"))
        );

        String responseBody = mockMvc.perform(post("/api/vikings")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Viking response = objectMapper.readValue(responseBody, Viking.class);
        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo("Bjorn");
        assertThat(response.equipment()).hasSize(1);
    }

    @Test
    void deletesVikingById() throws Exception {
        Viking created = createViking("Ragnar");

        mockMvc.perform(delete("/api/vikings/{id}", created.id()))
                .andExpect(status().isNoContent());

        String responseBody = mockMvc.perform(get("/api/vikings"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(objectMapper.readValue(responseBody, Viking[].class)).isEmpty();
    }

    @Test
    void returnsNotFoundWhenDeletingMissingViking() throws Exception {
        mockMvc.perform(delete("/api/vikings/{id}", 999))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatesVikingById() throws Exception {
        Viking created = createViking("Ivar");
        Viking update = new Viking(
                null,
                "Ivar the Boneless",
                28,
                176,
                HairColor.Brown,
                BeardStyle.SHORT,
                List.of(
                        new EquipmentItem("Sword", "Legendary"),
                        new EquipmentItem("Shield", "Rare")
                )
        );

        String responseBody = mockMvc.perform(put("/api/vikings/{id}", created.id())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Viking response = objectMapper.readValue(responseBody, Viking.class);
        assertThat(response.id()).isEqualTo(created.id());
        assertThat(response.name()).isEqualTo("Ivar the Boneless");
        assertThat(response.age()).isEqualTo(28);
        assertThat(response.equipment())
                .extracting(EquipmentItem::name)
                .containsExactly("Sword", "Shield");
    }

    @Test
    void returnsNotFoundWhenUpdatingMissingViking() throws Exception {
        Viking update = new Viking(
                null,
                "Lagertha",
                35,
                172,
                HairColor.Blond,
                BeardStyle.CLEAN_SHAVEN,
                List.of()
        );

        mockMvc.perform(put("/api/vikings/{id}", 999)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isNotFound());
    }

    private Viking createViking(String name) throws Exception {
        Viking request = new Viking(
                null,
                name,
                30,
                180,
                HairColor.Black,
                BeardStyle.LONG,
                List.of(new EquipmentItem("Axe", "Common"))
        );

        String responseBody = mockMvc.perform(post("/api/vikings")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readValue(responseBody, Viking.class);
    }
}
