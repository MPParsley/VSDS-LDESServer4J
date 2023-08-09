package be.vlaanderen.informatievlaanderen.ldes.server.fetchrest.treenode.services;

import be.vlaanderen.informatievlaanderen.ldes.server.domain.converter.PrefixAdder;
import be.vlaanderen.informatievlaanderen.ldes.server.domain.events.ShaclChangedEvent;
import be.vlaanderen.informatievlaanderen.ldes.server.domain.events.ShaclDeletedEvent;
import be.vlaanderen.informatievlaanderen.ldes.server.domain.eventstream.entities.EventStream;
import be.vlaanderen.informatievlaanderen.ldes.server.domain.eventstream.valueobjects.EventStreamCreatedEvent;
import be.vlaanderen.informatievlaanderen.ldes.server.domain.eventstream.valueobjects.EventStreamDeletedEvent;
import be.vlaanderen.informatievlaanderen.ldes.server.domain.fetching.EventStreamInfoResponse;
import be.vlaanderen.informatievlaanderen.ldes.server.domain.shacl.entities.ShaclShape;
import be.vlaanderen.informatievlaanderen.ldes.server.domain.view.service.DcatViewService;
import be.vlaanderen.informatievlaanderen.ldes.server.domain.viewcreation.valueobjects.ViewName;
import be.vlaanderen.informatievlaanderen.ldes.server.fetchapplication.entities.TreeNodeDto;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static be.vlaanderen.informatievlaanderen.ldes.server.domain.constants.RdfConstants.*;
import static be.vlaanderen.informatievlaanderen.ldes.server.domain.constants.ServerConstants.HOST_NAME_KEY;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;
import static org.apache.jena.rdf.model.ResourceFactory.createStatement;

@Component
public class TreeNodeConverterImpl implements TreeNodeConverter {

	private final PrefixAdder prefixAdder;
	private String hostName;

	private final HashMap<String, EventStream> eventStreams = new HashMap<>();
	private final HashMap<String, ShaclShape> shaclShapes = new HashMap<>();
	private final DcatViewService dcatViewService;

	public TreeNodeConverterImpl(PrefixAdder prefixAdder, @Value(HOST_NAME_KEY) String hostName,
			DcatViewService dcatViewService) {
		this.prefixAdder = prefixAdder;
		this.hostName = hostName;
		this.dcatViewService = dcatViewService;
	}

	@Override
	public Model toModel(final TreeNodeDto treeNodeDto) {
		Model model = ModelFactory.createDefaultModel()
				.add(addTreeNodeStatements(treeNodeDto, treeNodeDto.getCollectionName()));
		return prefixAdder.addPrefixesToModel(model);
	}

	private List<Statement> addTreeNodeStatements(TreeNodeDto treeNodeDto, String collectionName) {
		EventStream eventStream = eventStreams.get(collectionName);
		ShaclShape shaclShape = shaclShapes.get(collectionName);
		List<Statement> statements = new ArrayList<>(treeNodeDto.getTreeNode().getModel().listStatements().toList());
		addLdesCollectionStatements(statements, treeNodeDto.isView(), treeNodeDto.getFragmentId(), eventStream,
				shaclShape);

		return statements;
	}

	private void addLdesCollectionStatements(List<Statement> statements, boolean isView, String currentFragmentId,
			EventStream eventStream, ShaclShape shaclShape) {
		String baseUrl = hostName + "/" + eventStream.getCollection();
		Resource collection = createResource(baseUrl);

		if (isView) {
			EventStreamInfoResponse eventStreamInfoResponse = new EventStreamInfoResponse(
					baseUrl,
					eventStream.getTimestampPath(),
					eventStream.getVersionOfPath(),
					null,
					Collections.singletonList(currentFragmentId));
			statements.addAll(eventStreamInfoResponse.convertToStatements());
			statements.addAll(shaclShape.getModel().listStatements().toList());
			addDcatStatements(statements, currentFragmentId, eventStream.getCollection());
		} else {
			statements.add(createStatement(createResource(currentFragmentId), IS_PART_OF_PROPERTY, collection));
		}
	}

	private void addDcatStatements(List<Statement> statements, String currentFragmentId, String collection) {
		ViewName viewName = ViewName.fromString(currentFragmentId.substring(currentFragmentId.indexOf(collection)));
		dcatViewService.findByViewName(viewName)
				.ifPresent(dcatView -> statements.addAll(dcatView.getStatementsWithBase(hostName)));

	}

	@EventListener
	public void handleEventStreamInitEvent(EventStreamCreatedEvent event) {
		eventStreams.put(event.eventStream().getCollection(), event.eventStream());
	}

	@EventListener
	public void handleShaclInitEvent(ShaclChangedEvent event) {
		shaclShapes.put(event.getShacl().getCollection(), event.getShacl());
	}

	@EventListener
	public void handleEventStreamDeletedEvent(EventStreamDeletedEvent event) {
		eventStreams.remove(event.collectionName());
	}

	@EventListener
	public void handleShaclDeletedEvent(ShaclDeletedEvent event) {
		shaclShapes.remove(event.collectionName());
	}
}
