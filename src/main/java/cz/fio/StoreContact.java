package cz.fio;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

		if (firstName == null || lastName == null || email == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().write("Missing parameters");
			return;
		}

		String csvRecord = firstName + "," + lastName + "," + email;

		File csvFile = new File(csvPath);
		List<String> contactList = new ArrayList<>();
		try {
			contactList = Files.readAllLines(csvFile.toPath());
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}

		if (!contactList.contains(csvRecord)) {
			try {
				Files.writeString(csvFile.toPath(), csvRecord, StandardOpenOption.APPEND);
			} catch (IOException e) {
				log.error(e.getMessage(), e);
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		} else {
			response.getWriter().write("Contact is already in the file");
		}

	}
}
