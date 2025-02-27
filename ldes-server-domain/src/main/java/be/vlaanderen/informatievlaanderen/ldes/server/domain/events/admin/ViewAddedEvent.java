package be.vlaanderen.informatievlaanderen.ldes.server.domain.events.admin;

import be.vlaanderen.informatievlaanderen.ldes.server.domain.model.ViewName;
import be.vlaanderen.informatievlaanderen.ldes.server.domain.model.ViewSpecification;

public class ViewAddedEvent {
	private final ViewSpecification viewSpecification;

	public ViewAddedEvent(ViewSpecification viewSpecification) {
		this.viewSpecification = viewSpecification;
	}

	public ViewName getViewName() {
		return viewSpecification.getName();
	}

	public ViewSpecification getViewSpecification() {
		return viewSpecification;
	}
}
