package be.vlaanderen.informatievlaanderen.ldes.server.retention.services;

import be.vlaanderen.informatievlaanderen.ldes.server.domain.events.fragmentation.MemberAllocatedEvent;
import be.vlaanderen.informatievlaanderen.ldes.server.retention.repositories.MemberPropertiesRepository;
import be.vlaanderen.informatievlaanderen.ldes.server.retention.repositories.MemberReferencesRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AllocatedMemberHandler {
	private final MemberReferencesRepository memberReferencesRepository;

	public AllocatedMemberHandler(MemberReferencesRepository memberReferencesRepository) {
		this.memberReferencesRepository = memberReferencesRepository;
	}

	@EventListener
	public void handleMemberAllocatedEvent(MemberAllocatedEvent event) {
		memberReferencesRepository.addMemberToView(event.memberId(), event.viewName().asString());
	}

}
