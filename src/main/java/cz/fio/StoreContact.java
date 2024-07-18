package cz.fio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.csv.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoreContact extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String csvPath = System.getProperty("java.io.tmpdir") + File.separator + "contacts.csv";
	private static final Logger log = LoggerFactory.getLogger(StoreContact.class);

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String firstName = request.getParameter("firstName");
		String lastName = request.getParameter("lastName");
		String email = request.getParameter("email");

		// responding with bad request if any of params are missing
		if (firstName == null || lastName == null || email == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().write("Missing parameters");
			return;
		}

		// decoding characters to work with Czech
		firstName = java.net.URLDecoder.decode(firstName, "windows-1250");
		lastName = java.net.URLDecoder.decode(lastName, "windows-1250");
		email = java.net.URLDecoder.decode(email, "windows-1250");

		// protection against injection attacks - unfortunately messes up special chars (ščřžý)
		// after multiple attempts to fix it I have decided it's not necessary for the purpose of this test
		// firstName = StringEscapeUtils.escapeHtml4(firstName);
		// lastName = StringEscapeUtils.escapeHtml4(lastName);
		// email = StringEscapeUtils.escapeHtml4(email);

		// email validation
		validateEmail(response, email);

		// forming a contact for future comparison with existing contacts
		String csvContact = String.format("%s,%s,%s", firstName, lastName, email);

		File csvFile = new File(csvPath);

		// preventing multi-thread access to the file
		synchronized (StoreContact.class) {
			try {
				// creating csv file if it doesn't exist yet
				if (!csvFile.exists()) {
					csvFile.createNewFile();
					writeContactIntoFile(csvFile, csvContact, response);
				} else {
					// creating a set of existing contacts
					Set<String> contactSet = readCsv(csvFile);

					// make sure there are no duplicates
					if (!contactSet.contains(csvContact)) {
						writeContactIntoFile(csvFile, csvContact, response);
					} else {
						response.setStatus(HttpServletResponse.SC_CONFLICT);
						response.getWriter().write("Contact is already in the file");
					}
				}
			} catch (IOException e) {
				// logging error to find out the exact cause of failure
				// this could be expanded to handle specific errors
				// user could then get more appropriate and specific response
				log.error("Failed to write contact to CSV", e);
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().write("Internal server error");
			}
		}
	}

	private void validateEmail(HttpServletResponse response, String email) throws IOException {
		String emailRegEx = "^[A-Za-z0-9+_.-]+@(.+)$";
		Pattern pattern = Pattern.compile(emailRegEx);
		Matcher matcher = pattern.matcher(email);

		if (!matcher.matches()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().write("Invalid email");
		}
	}

	// method to read existing contacts from CSV file
	private Set<String> readCsv(File csvFile) throws IOException {
		Set<String> contactSet = new HashSet<>();
		try (CSVParser csvParser = CSVFormat.DEFAULT.parse(Files.newBufferedReader(
				csvFile.toPath(),
				Charset.forName("windows-1250")))
		) {
			for (CSVRecord record : csvParser) {
				String contact = record.get(0);
				contactSet.add(contact);
			}
		}
		return contactSet;
	}

	private void writeContactIntoFile(
			File csvFile, String csvContact, HttpServletResponse response) throws IOException {
		// creating CSV printer with windows encoding and append mode
		try (CSVPrinter csvPrinter = CSVFormat.DEFAULT
				.print(new OutputStreamWriter(
						new FileOutputStream(csvFile, true),
						"windows-1250")
				)
		) {
			// writing CSV record
			csvPrinter.printRecord(csvContact);
			response.setStatus(HttpServletResponse.SC_OK);
		}
	}
}
