package be.vlaanderen.informatievlaanderen.ldes.server.retention.memberreferences;

import be.vlaanderen.informatievlaanderen.ldes.server.retention.entities.MemberReferences;
import be.vlaanderen.informatievlaanderen.ldes.server.retention.memberproperties.entities.MemberPropertiesEntity;
import be.vlaanderen.informatievlaanderen.ldes.server.retention.memberreferences.entities.MemberReferencesEntity;
import be.vlaanderen.informatievlaanderen.ldes.server.retention.memberreferences.mapper.MemberReferencesEntityMapper;
import be.vlaanderen.informatievlaanderen.ldes.server.retention.repositories.MemberReferencesRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.stream.Stream;

public class MemberReferencesRepositoryImpl implements MemberReferencesRepository {
    private final MongoTemplate mongoTemplate;
    private final MemberReferencesEntityMapper memberReferencesEntityMapper;
    public static final String ID = "_id";
    public static final String VIEWS = "views";

    public MemberReferencesRepositoryImpl(MongoTemplate mongoTemplate, MemberReferencesEntityMapper memberReferencesEntityMapper) {
        this.mongoTemplate = mongoTemplate;
        this.memberReferencesEntityMapper = memberReferencesEntityMapper;
    }

    @Override
    public synchronized void addMemberToView(String memberId, String viewName) {
        Query query = new Query();
        query.addCriteria(Criteria.where(ID).is(memberId));
        Update update = new Update();
        update.push(VIEWS, viewName);
        mongoTemplate.upsert(query, update, MemberReferencesEntity.class);
    }

    @Override
    public Stream<MemberReferences> getMemberReferencesByViewName(String viewName){
        Query query = new Query();
        query.addCriteria(Criteria.where(VIEWS).is(viewName));
        return mongoTemplate.stream(query, MemberReferencesEntity.class)
                .map(memberReferencesEntityMapper::toMemberReferences);
    }
}
