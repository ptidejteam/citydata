package ca.concordia.encs.citydata.producers;

import java.util.ArrayList;
import java.util.List;

import ca.concordia.encs.citydata.core.utils.RequestOptions;
import com.google.firebase.database.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import ca.concordia.encs.citydata.core.implementations.AbstractProducer;
import ca.concordia.encs.citydata.core.contracts.IProducer;
import com.google.gson.JsonParser;
/***
 * This is the Producer class for Firebase, which fetches data from Firebase
 * @author Rushin Makwana
 * @since 7th Feb 2024
 */
public class FirebaseProducer extends AbstractProducer<JsonObject> implements IProducer<JsonObject> {

    private final String nodePath; // Path to the Firebase node to fetch data from

    public FirebaseProducer(String databaseURL, String nodePath, RequestOptions requestOptions) {
        this.nodePath = nodePath;
        // Any request options if needed

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
	}
	@Override
	public void fetch(){
		final List<JsonObject> jsonObjects = new ArrayList<>();

		final DatabaseReference ref = FirebaseDatabase.getInstance().getReference(nodePath);

		// Adding a ValueEventListener to fetch data
		ref.addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				// Retrieve JSON string from dataSnapshot
				final String result = dataSnapshot.getValue(String.class);

				if (result != null) {
					// Convert JSON string to object
					final JsonElement jsonElement = JsonParser.parseString(result);
					final JsonObject jsonObject = jsonElement.getAsJsonObject();
					jsonObjects.add(jsonObject);
					applyOperation();
					System.out.println("Data fetched from Firebase");
				} else {
					System.err.println("No data found at the specified node path.");
				}
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {
				System.err.println("Error fetching data: " + databaseError.getMessage());
			}
		});
	}
}
