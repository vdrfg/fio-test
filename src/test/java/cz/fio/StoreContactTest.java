
package cz.fio;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

class StoreContactTest {

	@Test
	void testDoGet_missingParams() throws IOException {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

		Mockito.when(request.getParameter("firstName")).thenReturn(null);
		Mockito.when(request.getParameter("lastName")).thenReturn("last");
		Mockito.when(request.getParameter("email")).thenReturn("email@test.com");

		StringWriter stringWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(stringWriter);
		Mockito.when(response.getWriter()).thenReturn(writer);

		StoreContact storeContact = new StoreContact();
		storeContact.doGet(request, response);

		writer.flush();
		assertTrue(stringWriter.toString().contains("Missing parameters"));
	}

	@Test
	void testDoGet_allParams() throws IOException {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
		Mockito.when(request.getParameter("firstName")).thenReturn("first");
		Mockito.when(request.getParameter("lastName")).thenReturn("last");
		Mockito.when(request.getParameter("email")).thenReturn("email@test.com");

		StringWriter stringWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(stringWriter);
		Mockito.when(response.getWriter()).thenReturn(writer);

		StoreContact storeContact = new StoreContact();
		storeContact.doGet(request, response);

		writer.flush();
		assertTrue(stringWriter.toString().isEmpty());
	}

	@Test
	void testDoGet_duplicateContact() throws IOException {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
		Mockito.when(request.getParameter("firstName")).thenReturn("firstDupl");
		Mockito.when(request.getParameter("lastName")).thenReturn("lastDupl");
		Mockito.when(request.getParameter("email")).thenReturn("emailDupl@test.com");

		StringWriter stringWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(stringWriter);
		Mockito.when(response.getWriter()).thenReturn(writer);

		StoreContact storeContact = new StoreContact();
		storeContact.doGet(request, response);
		storeContact.doGet(request, response);

		writer.flush();
		assertTrue(stringWriter.toString().contains("Contact is already in the file"));
	}
}
