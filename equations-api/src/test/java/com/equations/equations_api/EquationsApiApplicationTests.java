package com.equations.equations_api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
class EquationsApiApplicationTests {
	@Autowired
	private WebApplicationContext applicationContext;

	@Autowired
	private ObjectMapper objectMapper;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = webAppContextSetup(applicationContext)
				.apply(springSecurity())
				.build();
	}

	@Test
	void contextLoads() {
	}

	@Test
	void corsAllowsConfiguredLocalWebOrigin() throws Exception {
		mockMvc.perform(options("/api/v1/calculations")
						.header("Origin", "http://localhost:3000")
						.header("Access-Control-Request-Method", "POST"))
				.andExpect(status().isOk())
				.andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
	}

	@Test
	void rejectsFieldsOutsideTheSixSwingCoordinates() throws Exception {
		mockMvc.perform(post("/api/v1/calculations")
						.contentType("application/json")
						.content("""
								{
								  "x1": 0,
								  "y1": 0,
								  "x2": 3,
								  "y2": 0,
								  "x3": 0,
								  "y3": 4,
								  "sideA": 3
								}
								"""))
				.andExpect(status().isBadRequest());
	}

	@Test
	void rejectsFractionalCoordinatesLikeTheIntegerSwingInputs() throws Exception {
		mockMvc.perform(post("/api/v1/calculations")
						.contentType("application/json")
						.content("""
								{
								  "x1": 0.5,
								  "y1": 0,
								  "x2": 3,
								  "y2": 0,
								  "x3": 0,
								  "y3": 4
								}
								"""))
				.andExpect(status().isBadRequest());
	}

	@Test
	void historyRequiresAuthentication() throws Exception {
		mockMvc.perform(get("/api/v1/history"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void historyIsScopedToTheAuthenticatedUserAndCanBeDeleted() throws Exception {
		UUID owner = UUID.randomUUID();
		UUID otherUser = UUID.randomUUID();
		String savedId = mockMvc.perform(post("/api/v1/history")
						.with(jwt().jwt(token -> token.subject(owner.toString())))
						.contentType("application/json")
						.content("""
								{
								  "x1": -3,
								  "y1": 2,
								  "x2": 1,
								  "y2": 5,
								  "x3": -5,
								  "y3": 7
								}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.title").value("Triangle (-3, 2), (1, 5), (-5, 7)"))
				.andExpect(jsonPath("$.calculation.area").value(13.0))
				.andReturn()
				.getResponse()
				.getContentAsString();

		String id = objectMapper
				.readTree(savedId)
				.get("id")
				.asText();

		mockMvc.perform(get("/api/v1/history")
						.with(jwt().jwt(token -> token.subject(owner.toString()))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(id))
				.andExpect(jsonPath("$[0].calculation.area").value(13.0));

		mockMvc.perform(get("/api/v1/history")
						.with(jwt().jwt(token -> token.subject(otherUser.toString()))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isEmpty());

		mockMvc.perform(delete("/api/v1/history/{id}", id)
						.with(jwt().jwt(token -> token.subject(otherUser.toString()))))
				.andExpect(status().isNotFound());

		mockMvc.perform(delete("/api/v1/history/{id}", id)
						.with(jwt().jwt(token -> token.subject(owner.toString()))))
				.andExpect(status().isNoContent());
	}
}
