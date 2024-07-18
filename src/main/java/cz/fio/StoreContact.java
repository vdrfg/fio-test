package cz.fio;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;

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

		response.setContentType("text/plain");

		if (firstName == null || lastName == null || email == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().write("Missing parameters");
			return;
		}

		String csvRecord = firstName + "," + lastName + "," + email + System.lineSeparator();

		File csvFile = new File(csvPath);

		synchronized (StoreContact.class) {
			try {
				if (!csvFile.exists()) {
					csvFile.createNewFile();
				}
				Set<String> contactSet = new HashSet<>(Files.readAllLines(csvFile.toPath(), StandardCharsets.ISO_8859_1));

				if (!contactSet.contains(csvRecord.trim())) {
					Files.writeString(csvFile.toPath(), csvRecord, StandardOpenOption.APPEND);
					response.setStatus(HttpServletResponse.SC_OK);
				} else {
					response.setStatus(HttpServletResponse.SC_CONFLICT);
					response.getWriter().write("Contact is already in the file");
				}
			} catch (IOException e) {
				log.error("File operation failed", e);
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().write("Internal server error");
			}
		}
	}
}
