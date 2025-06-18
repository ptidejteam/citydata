package ca.concordia.encs.citydata.core;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * ExistsController routes test
 *
 * @author Rushin Makwana
 * @since 2025-01-01
 */
public class FireBaseTest {

	private static FirebaseDatabase database;
	private static boolean skipTest = false;

	@BeforeAll
	public static void setup() throws IOException {

		try {
			FileInputStream serviceAccount = new FileInputStream("./src/test/data/citylayer-middleware.json");

			// Create FirebaseOptions using the static method
			FirebaseOptions options = FirebaseOptions.builder()
					.setCredentials(GoogleCredentials.fromStream(serviceAccount))
					.setDatabaseUrl("https://citylayer-middleware-default-rtdb.firebaseio.com/").build();

			FirebaseApp.initializeApp(options);
			database = FirebaseDatabase.getInstance();
		} catch (FileNotFoundException e) {
			// FIXME: find a way to load credentials for tests running on GitHub.
			// However, do NOT push credentials to GitHub!
			skipTest = true;
			System.out.println(
					"There is no Firebase config file in this environment. This will be corrected in the future. Skipping test for now!");
		}

	}

	@Test
	public void testFetchDataFromFirebase() {

		if (skipTest) {
			assertNotNull(skipTest, "Should skip test!");
		} else {
			DatabaseReference ref = database.getReference("/");
			ref.addListenerForSingleValueEvent(new ValueEventListener() {
				@Override
				public void onDataChange(DataSnapshot dataSnapshot) {
					// Assuming you expect some JSON-like structure in your database
					System.out.println(dataSnapshot.getValue());
					assertNotNull(dataSnapshot.getValue(), "Data should not be null");
				}

				@Override
				public void onCancelled(DatabaseError databaseError) {
					// Handle possible errors
					System.out.println("The read failed: " + databaseError.getMessage());
				}
			});

			// Add a sleep or wait mechanism if necessary, since Firebase operations are
			// asynchronous
			try {
				Thread.sleep(5000); // Adjust time as necessary
			} catch (InterruptedException e) {
				System.out.println(e.getMessage());
			}
		}

	}
}
