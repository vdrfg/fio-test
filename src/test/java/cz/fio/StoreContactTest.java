package cz.fio;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

class StoreContactTest {
	private HttpServletRequest request;
	private HttpServletResponse response;
	private StringWriter stringWriter;
	private PrintWriter writer;
	private StoreContact storeContact;

	@BeforeEach
	void setup() throws IOException {
		request = Mockito.mock(HttpServletRequest.class);
		response = Mockito.mock(HttpServletResponse.class);
		stringWriter = new StringWriter();
		writer = new PrintWriter(stringWriter);
		when(response.getWriter()).thenReturn(writer);
		storeContact = new StoreContact();
	}

	@Test
	void missingParams() throws IOException {
		setupRequestParameters(null, "Doe", "johndoe@email.com");
		handleRequestAndFlush();
		assertTrue(stringWriter.toString().contains("Missing parameters"));
		verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
	}

	@Test
	void duplicateContact() throws IOException {
		setupRequestParameters("John", "Doe", "johndoe@email.com");
		handleRequestAndFlush();
		verify(response).setStatus(HttpServletResponse.SC_CONFLICT);
		assertTrue(stringWriter.toString().contains("Contact is already in the file"));
	}

	@Test
	void invalidEmail() throws IOException {
		setupRequestParameters("John", "Doe", "johndoeemail.com");
		handleRequestAndFlush();
		verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
		assertTrue(stringWriter.toString().contains("Invalid email"));
	}

	// param setup - more useful on bigger scale (more tests for specific params etc)
	private void setupRequestParameters(String firstName, String lastName, String email) {
		when(request.getParameter("firstName")).thenReturn(firstName);
		when(request.getParameter("lastName")).thenReturn(lastName);
		when(request.getParameter("email")).thenReturn(email);
	}

	private void handleRequestAndFlush() throws IOException {
		storeContact.doGet(request, response);
		writer.flush();
	}
}
