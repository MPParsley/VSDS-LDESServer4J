package be.vlaanderen.informatievlaanderen.ldes.server.domain.tree.member.services;

import be.vlaanderen.informatievlaanderen.ldes.server.domain.converter.RdfModelConverter;
import be.vlaanderen.informatievlaanderen.ldes.server.domain.events.ingest.MemberIngestedEvent;
import be.vlaanderen.informatievlaanderen.ldes.server.domain.eventstream.valueobjects.EventStreamDeletedEvent;
import be.vlaanderen.informatievlaanderen.ldes.server.domain.ldesfragment.services.FragmentationMediator;
import be.vlaanderen.informatievlaanderen.ldes.server.domain.tree.member.entities.Member;
import be.vlaanderen.informatievlaanderen.ldes.server.domain.tree.member.repository.MemberRepository;
import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.util.ResourceUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

class MemberIngestServiceImplTest {

	private final MemberRepository memberRepository = mock(MemberRepository.class);

	private final LegacyMemberConverter legacyMemberConverter = mock(LegacyMemberConverter.class);

	private final FragmentationMediator fragmentationMediator = mock(FragmentationMediator.class);
	private MemberIngestServiceImpl memberIngestService;

	@BeforeEach
	void setUp() {
		memberIngestService = new MemberIngestServiceImpl(legacyMemberConverter, memberRepository,
				fragmentationMediator);
	}

	@Test
	@DisplayName("Adding Member when there is a member with the same id that            already exists")
	void when_TheMemberAlreadyExists_thenMemberIsReturned() throws IOException {
		String ldesMemberString = FileUtils.readFileToString(ResourceUtils.getFile("classpath:example-ldes-member.nq"),
				StandardCharsets.UTF_8);
		String memberId = "https://private-api.gipod.beta-vlaanderen.be/api/v1/mobility-hindrances/10810464/1";
		String collectionName = "collectionName";
		Model model = RdfModelConverter.fromString(ldesMemberString, Lang.NQUADS);
		Member member = new Member(memberId, collectionName, 0L, null, null, model, List.of());
		when(memberRepository.memberExists(memberId)).thenReturn(true);
		when(legacyMemberConverter.toMember(collectionName, model)).thenReturn(member);

		memberIngestService.addMember(new MemberIngestedEvent(model, memberId, collectionName));

		InOrder inOrder = inOrder(memberRepository, fragmentationMediator);
		inOrder.verify(memberRepository,
				times(1)).memberExists(memberId);
		inOrder.verifyNoMoreInteractions();
		verifyNoInteractions(fragmentationMediator);
		verifyNoMoreInteractions(memberRepository);
	}

	@Test
	@DisplayName("Adding Member when there is no existing member with the same            id")
	void when_TheMemberDoesNotAlreadyExists_thenMemberIsStored() throws IOException {
		String ldesMemberString = FileUtils.readFileToString(ResourceUtils.getFile("classpath:example-ldes-member.nq"),
				StandardCharsets.UTF_8);
		String memberId = "https://private-api.gipod.beta-vlaanderen.be/api/v1/mobility-hindrances/10810464/1";
		String collectionName = "collectionName";
		Model model = RdfModelConverter.fromString(ldesMemberString, Lang.NQUADS);
		Member member = new Member(memberId, collectionName, 0L, null, null, model, List.of());
		when(memberRepository.memberExists(memberId)).thenReturn(false);
		when(legacyMemberConverter.toMember(collectionName, model)).thenReturn(member);

		memberIngestService.addMember(new MemberIngestedEvent(model, memberId, collectionName));

		InOrder inOrder = inOrder(memberRepository, fragmentationMediator);
		inOrder.verify(memberRepository, times(1)).memberExists(memberId);
		inOrder.verify(memberRepository, times(1)).saveLdesMember(member);
		inOrder.verify(fragmentationMediator, times(1)).addMemberToFragment(member);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	void when_EventStreamIsDeleted_then_DeleteAllMembers() throws IOException {
		final String collection = "collectionName";
		final String memberId = "https://private-api.gipod.beta-vlaanderen.be/api/v1/mobility-hindrances/10810464/1";
		final String ldesMemberString = FileUtils.readFileToString(
				ResourceUtils.getFile("classpath:example-ldes-member.nq"),
				StandardCharsets.UTF_8);
		final Member member = new Member(
				memberId, collection, 0L, null, null, RdfModelConverter.fromString(ldesMemberString, Lang.NQUADS),
				List.of());
		when(memberRepository.getMemberStreamOfCollection(collection)).thenReturn(Stream.of(member));
		((MemberIngestServiceImpl) memberIngestService)
				.handleEventStreamDeletedEvent(new EventStreamDeletedEvent(collection));
		verify(memberRepository).deleteMembersByCollection(collection);
		verifyNoMoreInteractions(memberRepository);
	}
}
