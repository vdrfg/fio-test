package cz.fio;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoreContact extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String csvPath = System.getProperty("java.io.tmpdir") + File.separator + "contacts.csv";
	private static final Logger log = LoggerFactory.getLogger(StoreContact.class);

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String firstName = request.getParameter("firstName");
		String lastName = request.getParameter("lastName");
		String email = request.getParameter("email");

		// responding with bad request if any of params are missing
		if (firstName == null || lastName == null || email == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().write("Missing parameters");
			return;
		}

		// protecting against injection attacks
		firstName = StringEscapeUtils.escapeHtml4(firstName);
		lastName = StringEscapeUtils.escapeHtml4(lastName);
		email = StringEscapeUtils.escapeHtml4(email);

		// email validation
		String emailRegEx = "^[A-Za-z0-9+_.-]+@(.+)$";
		Pattern pattern = Pattern.compile(emailRegEx);
		Matcher matcher = pattern.matcher(email);

		if (!matcher.matches()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().write("Invalid email");
			return;
		}

		// forming a contact for future comparison with existing contacts
		String csvContact = firstName + "," + lastName + "," + email + System.lineSeparator();

		File csvFile = new File(csvPath);

		// preventing multi-thread access to the file
		synchronized (StoreContact.class) {
			try {
				// creating csv file if it doesn't exist yet
				if (!csvFile.exists()) {
					csvFile.createNewFile();
				}

				// reading file to get existing contacts
				Set<String> contactSet = new HashSet<>(Files.readAllLines(csvFile.toPath(), StandardCharsets.ISO_8859_1));

				if (!contactSet.contains(csvContact.trim())) {
					// saving a contact if it doesn't exist yet and responding with 200
					Files.writeString(csvFile.toPath(), csvContact, StandardOpenOption.APPEND);
					response.setStatus(HttpServletResponse.SC_OK);
				} else {
					// responding with 409 if entered contact is duplicate
					response.setStatus(HttpServletResponse.SC_CONFLICT);
					response.getWriter().write("Contact is already in the file");
				}

			} catch (IOException e) {
				// logging error to find out the exact cause of failure
				// this could be expanded to handle specific errors
				// user could then get more appropriate and specific response
				log.error("File operation failed", e);
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().write("Internal server error");
			}
		}
	}
}
