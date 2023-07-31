package be.vlaanderen.informatievlaanderen.ldes.server.retention.memberreferences.mapper;

import be.vlaanderen.informatievlaanderen.ldes.server.retention.entities.MemberReferences;
import be.vlaanderen.informatievlaanderen.ldes.server.retention.memberreferences.entities.MemberReferencesEntity;
import org.springframework.stereotype.Component;

@Component
public class MemberReferencesEntityMapper {

	public MemberReferences toMemberReferences(MemberReferencesEntity memberReferencesEntity) {
		return new MemberReferences(memberReferencesEntity.getId(), memberReferencesEntity.getViews());
	}
}
