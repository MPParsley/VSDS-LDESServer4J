package be.vlaanderen.informatievlaanderen.ldes.server.retention.repositories;

import be.vlaanderen.informatievlaanderen.ldes.server.retention.entities.MemberProperties;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface MemberPropertiesRepository {

	void saveMemberPropertiesWithoutViews(MemberProperties memberProperties);

	void save(MemberProperties memberProperties);

	Optional<MemberProperties> retrieve(String id);

	List<MemberProperties> getMemberPropertiesOfVersionAndView(String versionOf, String viewName);

	Stream<MemberProperties> getMemberPropertiesWithViewReference(String viewName);

	void removeViewReference(String id, String viewName);

	void removeMemberPropertiesOfCollection(String collectionName);

	void deleteById(String id);

	Stream<MemberProperties> getMemberPropertiesByIds(List<String> membersOfView);
}
