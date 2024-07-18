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
		setupRequestParameters(null);
		handleRequestAndFlush();
		assertTrue(stringWriter.toString().contains("Missing parameters"));
		verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
	}

	@Test
	void duplicateContact() throws IOException {
		setupRequestParameters("John");
		handleRequestAndFlush();
		handleRequestAndFlush();
		verify(response).setStatus(HttpServletResponse.SC_CONFLICT);
		assertTrue(stringWriter.toString().contains("Contact is already in the file"));
	}

	private void setupRequestParameters(String firstName) {
		when(request.getParameter("firstName")).thenReturn(firstName);
		when(request.getParameter("lastName")).thenReturn("Doe");
		when(request.getParameter("email")).thenReturn("johndoe@email.com");
	}

	private void handleRequestAndFlush() throws IOException {
		storeContact.doGet(request, response);
		writer.flush();
	}
}
