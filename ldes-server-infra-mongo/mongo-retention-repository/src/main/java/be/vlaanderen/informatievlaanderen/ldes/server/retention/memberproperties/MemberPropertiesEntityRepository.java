package be.vlaanderen.informatievlaanderen.ldes.server.retention.memberproperties;

import be.vlaanderen.informatievlaanderen.ldes.server.retention.memberproperties.entities.MemberPropertiesEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MemberPropertiesEntityRepository extends MongoRepository<MemberPropertiesEntity, String> {

}
