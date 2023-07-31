package be.vlaanderen.informatievlaanderen.ldes.server.retention.services.retentionpolicy.execution;

import be.vlaanderen.informatievlaanderen.ldes.server.retention.entities.MemberProperties;
import be.vlaanderen.informatievlaanderen.ldes.server.retention.entities.MemberReferences;
import be.vlaanderen.informatievlaanderen.ldes.server.retention.repositories.MemberPropertiesRepository;
import be.vlaanderen.informatievlaanderen.ldes.server.retention.repositories.MemberReferencesRepository;
import be.vlaanderen.informatievlaanderen.ldes.server.retention.repositories.RetentionPolicyCollection;
import be.vlaanderen.informatievlaanderen.ldes.server.retention.services.retentionpolicy.definition.RetentionPolicy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class RetentionService {

	private final MemberPropertiesRepository memberPropertiesRepository;
	private final MemberReferencesRepository memberReferencesRepository;
	private final MemberRemover memberRemover;
	private final RetentionPolicyCollection retentionPolicyCollection;

	public RetentionService(MemberPropertiesRepository memberPropertiesRepository, MemberReferencesRepository memberReferencesRepository, MemberRemover memberRemover,
							RetentionPolicyCollection retentionPolicyCollection) {
		this.memberPropertiesRepository = memberPropertiesRepository;
		this.memberReferencesRepository = memberReferencesRepository;
		this.memberRemover = memberRemover;
		this.retentionPolicyCollection = retentionPolicyCollection;
	}

	@Scheduled(fixedDelay = 10000)
	public void executeRetentionPolicies() {
		retentionPolicyCollection
				.getRetentionPolicyMap()
				.entrySet()
				.stream()
				.filter(this::viewHasRetentionPolicies)
				.forEach(viewWithRetentionPolicies -> removeMembersFromViewThatMatchRetentionPolicies(
						viewWithRetentionPolicies.getKey(), viewWithRetentionPolicies.getValue()));
	}

	private boolean viewHasRetentionPolicies(Map.Entry<String, List<RetentionPolicy>> entry) {
		return !entry.getValue().isEmpty();
	}

	private void removeMembersFromViewThatMatchRetentionPolicies(String viewName,
			List<RetentionPolicy> retentionPoliciesOfView) {
		memberReferencesRepository.getMemberReferencesByViewName(viewName)
				.map(memberReferences -> memberPropertiesRepository
						.retrieve(memberReferences.getMemberId())
						.map(memberProperties -> createRetentionCandidate(memberReferences, memberProperties)))
				.flatMap(Optional::stream)
				.filter(retentionCandidate -> memberMatchesAllRetentionPoliciesOfView(retentionPoliciesOfView, viewName,
						retentionCandidate))
				.forEach(retentionCandidate -> memberRemover.removeMemberFromView(retentionCandidate, viewName));
	}

	private RetentionCandidate createRetentionCandidate(MemberReferences memberReferences, MemberProperties memberProperties) {
		return new RetentionCandidate(memberProperties, memberReferences);
	}

	private boolean memberMatchesAllRetentionPoliciesOfView(List<RetentionPolicy> retentionPolicies,
															String viewName, RetentionCandidate retentionCandidate) {
		return retentionPolicies
				.stream()
				.allMatch(retentionPolicy -> retentionPolicy.matchesPolicyOfView(retentionCandidate.getMemberProperties(), viewName));
	}

}