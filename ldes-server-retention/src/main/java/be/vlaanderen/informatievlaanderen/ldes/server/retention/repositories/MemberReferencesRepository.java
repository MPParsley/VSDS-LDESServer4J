package be.vlaanderen.informatievlaanderen.ldes.server.retention.repositories;

import be.vlaanderen.informatievlaanderen.ldes.server.retention.entities.MemberReferences;

import java.util.stream.Stream;

public interface MemberReferencesRepository {

    void addMemberToView(String memberId, String viewName);

    Stream<MemberReferences> getMemberReferencesByViewName(String viewName);
}
