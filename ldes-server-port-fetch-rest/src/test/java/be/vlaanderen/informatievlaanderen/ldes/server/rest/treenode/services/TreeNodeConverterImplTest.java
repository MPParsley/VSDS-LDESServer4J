package be.vlaanderen.informatievlaanderen.ldes.server.rest.treenode.services;

import be.vlaanderen.informatievlaanderen.ldes.server.domain.view.entity.DcatView;
import be.vlaanderen.informatievlaanderen.ldes.server.domain.viewcreation.valueobjects.ViewName;
import be.vlaanderen.informatievlaanderen.ldes.server.fetchapplication.valueobjects.TreeNodeDto;
import be.vlaanderen.informatievlaanderen.ldes.server.fetchdomain.valueobjects.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.riot.RDFParserBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static be.vlaanderen.informatievlaanderen.ldes.server.domain.constants.RdfConstants.*;
import static org.apache.jena.rdf.model.ResourceFactory.createResource;

class TreeNodeConverterImplTest {

	private static final String HOST_NAME = "http://localhost:8080";
	private static final String COLLECTION_NAME = "mobility-hindrances";
	private static final String PREFIX = HOST_NAME + "/" + COLLECTION_NAME;
	private static final String VIEW_NAME = "view";

	@Test
	void when_TreeNodeHasNoMembersAndIsAView_ModelHasTreeNodeAndLdesStatements() {
		ViewName viewName = new ViewName(COLLECTION_NAME, VIEW_NAME);
		Model dcat = RDFParser.source("eventstream/streams/dcat-view-valid.ttl").lang(Lang.TURTLE).build().toModel();
		DcatView dcatView = DcatView.from(viewName, dcat);
		String eventStreamIdentifier = PREFIX;
		String treeNodeIdentifier = PREFIX + "/" + VIEW_NAME;
		Model shacl = RDFParser.source("eventstream/streams/example-shape.ttl").lang(Lang.TURTLE).build().toModel();
		List<Statement> dcatStatements = dcatView.getStatementsWithBase(HOST_NAME);
		EventStreamProperties eventStreamProperties = new EventStreamProperties(eventStreamIdentifier,
				"http://www.w3.org/ns/prov#generatedAtTime", "http://purl.org/dc/terms/isVersionOf");
		EventStreamInfo eventStreamInfo = new EventStreamInfo(
				treeNodeIdentifier, eventStreamIdentifier,
				shacl, true, dcatStatements, eventStreamProperties);
		TreeNodeInfo treeNodeInfo = new TreeNodeInfo(treeNodeIdentifier, List.of());

		TreeMemberList treeMemberList = new TreeMemberList(eventStreamIdentifier, List.of());
		TreeNodeDto treeNodeDto = new TreeNodeDto(
				new TreeNode(eventStreamInfo, treeNodeInfo,
						treeMemberList).getModel(),
				treeNodeIdentifier, List.of(), List.of(), false);

		Model model = treeNodeDto.getModel();

		Assertions.assertEquals(20, getNumberOfStatements(model));
		verifyTreeNodeStatement(model);
		verifyLdesStatements(model);
	}

	@Test
	void when_TreeNodeHasNoMembersAndIsNotAView_ModelHasTreeNodeAndPartOfStatements() {
		String eventStreamIdentifier = PREFIX;
		String treeNodeIdentifier = PREFIX + "/" + VIEW_NAME;
		EventStreamProperties eventStreamProperties = new EventStreamProperties(eventStreamIdentifier,
				"http://www.w3.org/ns/prov#generatedAtTime", "http://purl.org/dc/terms/isVersionOf");
		EventStreamInfo eventStreamInfo = new EventStreamInfo(treeNodeIdentifier, eventStreamIdentifier,
				ModelFactory.createDefaultModel(), false, List.of(), eventStreamProperties);
		TreeNodeInfo treeNodeInfo = new TreeNodeInfo(treeNodeIdentifier, List.of());
		TreeMemberList treeMemberList = new TreeMemberList(eventStreamIdentifier, List.of());
		TreeNodeDto treeNodeDto = new TreeNodeDto(
				new TreeNode(eventStreamInfo, treeNodeInfo,
						treeMemberList).getModel(),
				treeNodeIdentifier, List.of(), List.of(), false);
		Model model = treeNodeDto.getModel();

		Assertions.assertEquals(2, getNumberOfStatements(model));
		verifyTreeNodeStatement(model);
		verifyIsPartOfStatement(model);
	}

	@Test
	void when_TreeNodeHasMembersAndARelations_ModelHasMultipleStatements() {
		Model ldesMemberModel = RDFParserBuilder.create().fromString("""
				<http://localhost:8080/mobility-hindrances> <https://w3id.org/tree#member>
				<https://private-api.gipod.beta-vlaanderen.be/api/v1/mobility-hindrances/10228622/165>
				.""").lang(Lang.NQUADS).toModel();
		String treeMemberIdentifier = "https://private-api.gipod.beta-vlaanderen.be/api/v1/mobility-hindrances/10228622/165";
		String eventStreamIdentifier = PREFIX;
		String treeNodeIdentifier = PREFIX + "/" + VIEW_NAME;
		EventStreamProperties eventStreamProperties = new EventStreamProperties(eventStreamIdentifier,
				"http://www.w3.org/ns/prov#generatedAtTime", "http://purl.org/dc/terms/isVersionOf");
		EventStreamInfo eventStreamInfo = new EventStreamInfo(treeNodeIdentifier, eventStreamIdentifier,
				ModelFactory.createDefaultModel(), false, List.of(), eventStreamProperties);
		TreeMember treeMember = new TreeMember(
				treeMemberIdentifier, ldesMemberModel);
		TreeNodeInfo treeNodeInfo = new TreeNodeInfo(treeNodeIdentifier,
				List.of(new TreeRelation("path", "http://localhost:8080/mobility-hindrances/node", "value",
						"http://www.w3.org/2001/XMLSchema#dateTime", "relation")));
		TreeMemberList treeMemberList = new TreeMemberList(eventStreamIdentifier, List.of(treeMember));
		TreeNode treeNode = new TreeNode(eventStreamInfo, treeNodeInfo, treeMemberList);
		TreeNodeDto treeNodeDto = new TreeNodeDto(treeNode.getModel(), treeNodeIdentifier, List.of(),
				List.of(treeMemberIdentifier), false);

		Model model = treeNodeDto.getModel();

		Assertions.assertEquals(9, getNumberOfStatements(model));
		verifyTreeNodeStatement(model);
		verifyIsPartOfStatement(model);
		Resource relationObject = model.listStatements(null, TREE_RELATION,
				(Resource) null).nextStatement().getObject()
				.asResource();
		verifyRelationStatements(model, relationObject);
		verifyMemberStatements(model);
	}

	private void verifyLdesStatements(Model model) {
		String id = HOST_NAME + "/" + COLLECTION_NAME;

		Assertions.assertEquals(
				"[" + id + ", http://www.w3.org/1999/02/22-rdf-syntax-ns#type, https://w3id.org/ldes#EventStream]",
				model.listStatements(createResource(id), RDF_SYNTAX_TYPE, (Resource) null).nextStatement().toString());
		Assertions.assertEquals(
				"[" + id + ", https://w3id.org/ldes#timestampPath, http://www.w3.org/ns/prov#generatedAtTime]",
				model.listStatements(createResource(id), LDES_TIMESTAMP_PATH, (Resource) null).nextStatement()
						.toString());
		Assertions.assertEquals(
				"[" + id + ", https://w3id.org/ldes#versionOfPath, http://purl.org/dc/terms/isVersionOf]",
				model.listStatements(createResource(id), LDES_VERSION_OF, (Resource) null).nextStatement().toString());

		verifyIsViewOfStatement(model);
		verifyShaclStatements(model);
		verifyDcatStatements(model);
	}

	private void verifyShaclStatements(Model model) {
		Resource shapeResource = createResource("http://localhost:8080/collectionName1/shape");
		Assertions.assertEquals(3, model.listStatements(shapeResource, null, (RDFNode) null).toList().size());
	}

	private void verifyDcatStatements(Model model) {
		Resource shapeResource = createResource("http://localhost:8080/mobility-hindrances/view/description");
		Assertions.assertEquals(6, model.listStatements(shapeResource, null, (RDFNode) null).toList().size());
	}

	private void verifyRelationStatements(Model model, Resource relationObject) {
		Assertions.assertEquals(
				String.format(
						"[" + HOST_NAME + "/" + COLLECTION_NAME + "/" + VIEW_NAME
								+ ", https://w3id.org/tree#relation, %s]",
						relationObject),
				model.listStatements(createResource(HOST_NAME + "/" + COLLECTION_NAME + "/" + VIEW_NAME), TREE_RELATION,
						(Resource) null)
						.nextStatement().toString());
		Assertions.assertEquals(
				String.format("[%s, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, relation]", relationObject),
				model.listStatements(relationObject, RDF_SYNTAX_TYPE, (Resource) null).nextStatement().toString());
		Assertions.assertEquals(String.format("[%s, https://w3id.org/tree#path, path]", relationObject),
				model.listStatements(relationObject, TREE_PATH, (Resource) null).nextStatement().toString());
		Assertions.assertEquals(
				String.format("[%s, https://w3id.org/tree#node, http://localhost:8080/mobility-hindrances/node]",
						relationObject),
				model.listStatements(relationObject, TREE_NODE, (Resource) null).nextStatement().toString());
		Assertions.assertEquals(
				String.format("[%s, https://w3id.org/tree#value, \"value\"^^http://www.w3.org/2001/XMLSchema#dateTime]",
						relationObject),
				model.listStatements(relationObject, TREE_VALUE, (Resource) null).nextStatement().toString());
	}

	private void verifyMemberStatements(Model model) {
		Assertions.assertEquals(
				"[http://localhost:8080/mobility-hindrances, https://w3id.org/tree#member, https://private-api.gipod.beta-vlaanderen.be/api/v1/mobility-hindrances/10228622/165]",
				model.listStatements(null, TREE_MEMBER, (Resource) null).nextStatement().toString());
	}

	private int getNumberOfStatements(Model model) {
		AtomicInteger statementCounter = new AtomicInteger();
		model.listStatements().forEach((statement) -> statementCounter.getAndIncrement());
		return statementCounter.get();
	}

	private void verifyTreeNodeStatement(Model model) {
		Assertions.assertEquals(
				"[" + HOST_NAME + "/" + COLLECTION_NAME + "/" + VIEW_NAME
						+ ", http://www.w3.org/1999/02/22-rdf-syntax-ns#type, https://w3id.org/tree#Node]",
				model.listStatements(null, RDF_SYNTAX_TYPE, createResource(TREE_NODE_RESOURCE)).nextStatement()
						.toString());
	}

	private void verifyIsViewOfStatement(Model model) {
		Assertions.assertEquals(
				"[" + HOST_NAME + "/" + COLLECTION_NAME
						+ ", https://w3id.org/tree#view, " + HOST_NAME + "/" + COLLECTION_NAME + "/" + VIEW_NAME + "]",
				model.listStatements(null, TREE_VIEW, (Resource) null).nextStatement()
						.toString());
	}

	private void verifyIsPartOfStatement(Model model) {
		Assertions.assertEquals(
				"[" + HOST_NAME + "/" + COLLECTION_NAME + "/" + VIEW_NAME
						+ ", http://purl.org/dc/terms/isPartOf, " + HOST_NAME + "/" + COLLECTION_NAME + "]",
				model.listStatements(null, IS_PART_OF_PROPERTY, (Resource) null).nextStatement()
						.toString());
	}

}
