package be.vlaanderen.informatievlaanderen.ldes.server.domain.ldesfragment.services;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.awaitility.Durations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

class FragmentationQueueMediatorImplTest {

	private FragmentationQueueMediator fragmentationQueueMediator;

	private final FragmentationExecutor fragmentationExecutor = mock(FragmentationExecutor.class);

	@BeforeEach
	void setUp() {
		fragmentationQueueMediator = new FragmentationQueueMediatorImpl(new SimpleMeterRegistry(),
				fragmentationExecutor);
	}

	@Test
	@DisplayName("Adding a member to the queue")
	void when_MemberIsAddedForFragmentation_AThreadIsStartedWhichCallsTheFragmentationService() {
		fragmentationQueueMediator.addLdesMember("someMember");

		await()
				.pollDelay(Durations.ONE_MILLISECOND)
				.atMost(Durations.ONE_HUNDRED_MILLISECONDS)
				.until(fragmentationQueueMediator::queueIsEmtpy);

		verify(fragmentationExecutor, times(1)).executeFragmentation("someMember");
	}

}