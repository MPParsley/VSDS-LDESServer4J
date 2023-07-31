package be.vlaanderen.informatievlaanderen.ldes.server.retention.services;

import be.vlaanderen.informatievlaanderen.ldes.server.domain.events.fragmentation.MemberAllocatedEvent;
import be.vlaanderen.informatievlaanderen.ldes.server.domain.viewcreation.valueobjects.ViewName;
import be.vlaanderen.informatievlaanderen.ldes.server.retention.repositories.MemberPropertiesRepository;
import be.vlaanderen.informatievlaanderen.ldes.server.retention.repositories.MemberReferencesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AllocatedMemberHandlerTest {
	private final static String ID = "id";
	private final static ViewName VIEW = new ViewName("collection", "view");
	private final static MemberAllocatedEvent event = new MemberAllocatedEvent(ID, VIEW);
	private AllocatedMemberHandler handler;
	private MemberReferencesRepository memberReferencesRepository;

	@BeforeEach
	void setUp() {
		memberReferencesRepository = mock(MemberReferencesRepository.class);
		handler = new AllocatedMemberHandler(memberReferencesRepository);
	}

	@Test
	void allocateMember() {
		handler.handleMemberAllocatedEvent(event);

		verify(memberReferencesRepository).addMemberToView(ID, VIEW.asString());
	}

}