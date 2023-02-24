package tfip.nus.iss.day28workshop.repo;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.MongoExpression;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationExpression;

import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import static tfip.nus.iss.day28workshop.Constants.*;

import java.util.List;

@Repository
public class GameRepo {

    @Autowired
    private MongoTemplate template;

    public List<Document> findGameById(Integer gId) {
        Criteria c = Criteria.where(FIELD_GID).is(gId);
        MatchOperation match = Aggregation.match(c);

        ProjectionOperation project = Aggregation.project()
                .andInclude(FIELD_GID, FIELD_NAME, FIELD_YEAR,
                        FIELD_RANK)
                .and(AggregationExpression.from(MongoExpression.create("""
                        $avg: "$reviews.rating"
                        """))).as("average")
                .andInclude(FIELD_RATED, FIELD_URL, FIELD_IMAGE)
                .andInclude(FIELD_REVIEWS)
                .andExclude(FIELD_UNDERSCORE_ID);
        LookupOperation lookup = Aggregation.lookup(COLLECTION_COMMENT, FIELD_GID, FIELD_GID, FIELD_REVIEWS);

        Aggregation pipeline = Aggregation.newAggregation(match, lookup, project);
        return template.aggregate(pipeline, COLLECTION_GAME, Document.class).getMappedResults();
    }

    public List<Document> findCommentById(String cid) {
        Criteria c = Criteria.where(FIELD_CID).is(cid);

        MatchOperation m = Aggregation.match(c);

        ProjectionOperation p = Aggregation.project()
                .andInclude(FIELD_GID, FIELD_CID, FIELD_USER, FIELD_RATING, FIELD_CTEXT)
                .andExclude(FIELD_UNDERSCORE_ID);
        Aggregation pipeline = Aggregation.newAggregation(m, p);
        return template.aggregate(pipeline, COLLECTION_COMMENT, Document.class).getMappedResults();
    }

    public List<Document> listCommentByRating(Boolean high) {
        Criteria c = Criteria.where(FIELD_YEAR).is(1999);

        MatchOperation match = Aggregation.match(c);

        LookupOperation lookup = Aggregation.lookup(COLLECTION_COMMENT, FIELD_GID, FIELD_GID, FIELD_REVIEWS);

        UnwindOperation unwind = Aggregation.unwind(FIELD_REVIEWS);

        SortOperation sort;

        if (high) {
            sort = Aggregation.sort(Sort.Direction.DESC, FIELD_AVERAGE);
        } else {
            sort = Aggregation.sort(Sort.Direction.ASC, FIELD_AVERAGE);
        }

        GroupOperation group = Aggregation.group(FIELD_GID)
                .first(FIELD_NAME).as(FIELD_NAME)
                .first(FIELD_AVERAGE).as("rating")
                .first("reviews.user").as("user")
                .first("reviews.c_text").as("comment")
                .first("reviews.c_id").as("review_id");

        Aggregation pipeline = Aggregation.newAggregation(match, lookup,
                unwind, sort, group);

        return template.aggregate(pipeline, COLLECTION_GAME, Document.class).getMappedResults();
    }
}
