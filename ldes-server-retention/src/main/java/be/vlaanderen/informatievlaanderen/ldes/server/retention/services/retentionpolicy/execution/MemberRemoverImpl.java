package be.vlaanderen.informatievlaanderen.ldes.server.retention.services.retentionpolicy.execution;

import be.vlaanderen.informatievlaanderen.ldes.server.domain.events.retention.MemberDeletedEvent;
import be.vlaanderen.informatievlaanderen.ldes.server.domain.events.retention.MemberUnallocatedEvent;
import be.vlaanderen.informatievlaanderen.ldes.server.domain.viewcreation.valueobjects.ViewName;
import be.vlaanderen.informatievlaanderen.ldes.server.retention.repositories.MemberPropertiesRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class MemberRemoverImpl implements MemberRemover {

	private final MemberPropertiesRepository memberPropertiesRepository;
	private final ApplicationEventPublisher applicationEventPublisher;

	public MemberRemoverImpl(MemberPropertiesRepository memberPropertiesRepository,
			ApplicationEventPublisher applicationEventPublisher) {
		this.memberPropertiesRepository = memberPropertiesRepository;
		this.applicationEventPublisher = applicationEventPublisher;
	}

	@Override
	public void removeMemberFromView(RetentionCandidate retentionCandidate, String viewName) {
		retentionCandidate.deleteViewReference(viewName);
		memberPropertiesRepository.removeViewReference(retentionCandidate.getId(), viewName);
		applicationEventPublisher
				.publishEvent(new MemberUnallocatedEvent(retentionCandidate.getId(), ViewName.fromString(viewName)));
		if (retentionCandidate.hasNoViewReferences()) {
			memberPropertiesRepository.deleteById(retentionCandidate.getId());
			applicationEventPublisher.publishEvent(
					new MemberDeletedEvent(retentionCandidate.getId()));
		}
	}
}
