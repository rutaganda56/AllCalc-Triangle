package com.equations.equations_api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class EquationsApiApplicationTests {
	@Autowired
	private WebApplicationContext applicationContext;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
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
}
