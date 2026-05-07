package com.mongodb.javabasic;
import java.util.Arrays;

import org.bson.Document;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.TransactionOptions;
import com.mongodb.WriteConcern;
import com.mongodb.client.ChangeStreamIterable;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.TransactionBody;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.changestream.ChangeStreamDocument;

public class ChangeStreamExample {
	public static void main(String[] args) {
		// Connection String to MongoDB
		String uri = "mongodb://admin:admin@localhost:27017,localhost:27018,localhost:27019/test?authSource=admin"; // Replace with your MongoDB URI
		String databaseName = "test"; // Replace with your database name
		String collectionName = "tickets";
		String resumeTokensCollectionName = "resumeTokens";
		final String changeStreamId = "example";

		// Create a MongoDB client
		try (MongoClient mongoClient = MongoClients.create(MongoClientSettings.builder()
				.applyConnectionString(new ConnectionString(uri))
				.build())) {

			// Connect to the database
			MongoDatabase database = mongoClient.getDatabase(databaseName);

			// Access the collection
			MongoCollection<Document> collection = database.getCollection(collectionName);
			MongoCollection<Document> resumeTokensCollection = database.getCollection(resumeTokensCollectionName);

			int totalNoOfInstances = 2;
			int currentInstanceIndex = 0;
			// Open a change stream on the collection
			System.out.println("Listening for changes...");
			ChangeStreamIterable<Document> changeStream = collection.watch(Arrays.asList(new Document("$match",
					new Document("$expr",
							new Document("$eq", Arrays.asList(new Document("$abs",
									new Document("$mod", Arrays.asList(
											new Document("$toHashedIndexKey", "$documentKey._id"),
											totalNoOfInstances))),
									currentInstanceIndex))))));

			Document resumeTokenDoc = resumeTokensCollection.find(Filters.eq("_id", changeStreamId)).first();
			if (resumeTokenDoc != null) {
				changeStream = changeStream
						.resumeAfter(resumeTokenDoc.get("resumeToken", Document.class).toBsonDocument());
			}

			// Iterate over the changes and print them
			try (MongoCursor<ChangeStreamDocument<Document>> cursor = changeStream.iterator()) {
				while (cursor.hasNext()) {
					ChangeStreamDocument<Document> change = cursor.next();
					System.out.println(change);

					// Start a client session
					ClientSession clientSession = mongoClient.startSession();

					// Define the transaction options (optional)
					TransactionOptions txnOptions = TransactionOptions.builder()
							.readPreference(ReadPreference.primary())
							.readConcern(ReadConcern.MAJORITY)
							.writeConcern(WriteConcern.MAJORITY)
							.build();

					// Execute the transaction using withTransaction()
					try {
						// The driver manages start, commit, and abort/retry
						clientSession.withTransaction(new TransactionBody<Void>() {
							@Override
							public Void execute() {
								Document doc = collection.aggregate(clientSession, Arrays.asList(new Document("$match",
										new Document("eventId", change.getFullDocument().getObjectId("eventId"))),
										new Document("$group",
												new Document("_id", "$eventId")
														.append("attendeesCount",
																new Document("$sum", 1))
														.append("ticketSales",
																new Document("$sum", "$totalAmount")))
								)).first();
								database.getCollection("events").updateOne(
										Filters.eq("_id", doc.getObjectId("_id")),
										Updates.combine(
												Updates.set("attendeesCount", doc.getInteger("attendeesCount")),
												Updates.set("ticketSales", doc.getInteger("ticketSales"))));
								resumeTokensCollection.updateOne(
										Filters.eq("_id", changeStreamId),
										Updates.set("resumeToken", change.getResumeToken()),
										new UpdateOptions().upsert(true));
								return null;
							}
						}, txnOptions);
						System.out.println("Transaction committed successfully.");
					} catch (Exception e) {
						System.err.println("Transaction failed: " + e.getMessage());
					} finally {
						clientSession.close();
					}
				}
			}
		}
	}
}
