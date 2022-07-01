
package osgi.common.util;

import java.util.Arrays;

import org.bson.BsonDocument;
import org.bson.BsonDocumentWrapper;
import org.bson.Document;
import org.bson.codecs.BsonCodecProvider;
import org.bson.codecs.BsonValueCodecProvider;
import org.bson.codecs.DocumentCodecProvider;
import org.bson.codecs.IterableCodecProvider;
import org.bson.codecs.MapCodecProvider;
import org.bson.codecs.ValueCodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.jsr310.Jsr310CodecProvider;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.mongodb.ConnectionString;
import com.mongodb.DBObjectCodecProvider;
import com.mongodb.DBRefCodecProvider;
import com.mongodb.DocumentToDBRefTransformer;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.codecs.GridFSFileCodecProvider;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.geojson.codecs.GeoJsonCodecProvider;

/**
 * The Class DataSourceUtil.
 * @author zhangchangchun
 * @Date 2022年5月31日
 */
public class DataSourceUtil {

    private static final MongoClient mongoClient;

    static {
        /** 
         * @see https://www.mongodb.com/docs/drivers/java/sync/current/fundamentals/connection/connection-options/#std-label-connection-options
        */
        String connectionString = "mongodb://root:admin@localhost:27017/?connectTimeoutMS=30000&maxPoolSize=500";
        mongoClient = MongoClients.create(MongoClientSettings.builder()
                .codecRegistry(CodecRegistries.fromProviders(Arrays.asList(new ValueCodecProvider(),
                        new BsonValueCodecProvider(), new DBRefCodecProvider(), new DBObjectCodecProvider(),
                        new DocumentCodecProvider(new DocumentToDBRefTransformer()),
                        new IterableCodecProvider(new DocumentToDBRefTransformer()),
                        new MapCodecProvider(new DocumentToDBRefTransformer()), new GeoJsonCodecProvider(),
                        new GridFSFileCodecProvider(), new Jsr310CodecProvider(), new BsonCodecProvider(),
                        PojoCodecProvider.builder().automatic(true).build())))
                .applyConnectionString(new ConnectionString(connectionString)).build());
    }

    public static MongoClient getMongoClient() {
        return mongoClient;
    }

    public static void closeMongoClient() {
        mongoClient.close();
    }

    public static void main(String[] args) {
        MongoDatabase db = getMongoClient().getDatabase("test");
        db.getCollection("user").insertOne(Document
                .parse("{name:\"tutu\",age:12,score:78,modified:new Date(\"Mon Mar 2022 02 13: 57: 49 CST\")}"));
        System.out.println(db.getCollection("user").countDocuments());
        for (Document doc : db.getCollection("user").find()) {
            System.out.println(doc);
        }
        db.getCollection("user", BsonDocument.class).updateOne(Filters.gt("score", 60),
                BsonDocumentWrapper.parse("{$set:{modified:\"$$NOW\"}}"));
        System.out.println(db.getCollection("user").find().first());
        db.getCollection("user", BsonDocument.class).deleteMany(BsonDocumentWrapper.parse("{name:\"tutu\"}"));
        closeMongoClient();
    }
}
