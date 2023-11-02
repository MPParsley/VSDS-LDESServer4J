package be.vlaanderen.informatievlaanderen.ldes.server.admin.domain.dcatserver.services;

import be.vlaanderen.informatievlaanderen.ldes.server.admin.domain.dcat.dcatdataset.entities.DcatDataset;
import be.vlaanderen.informatievlaanderen.ldes.server.admin.domain.dcat.dcatdataset.services.DcatDatasetService;
import be.vlaanderen.informatievlaanderen.ldes.server.admin.domain.dcat.dcatserver.entities.DcatServer;
import be.vlaanderen.informatievlaanderen.ldes.server.admin.domain.dcat.dcatserver.exceptions.DcatAlreadyConfiguredException;
import be.vlaanderen.informatievlaanderen.ldes.server.admin.domain.dcat.dcatserver.exceptions.MissingDcatServerException;
import be.vlaanderen.informatievlaanderen.ldes.server.admin.domain.dcat.dcatserver.repositories.DcatServerRepository;
import be.vlaanderen.informatievlaanderen.ldes.server.admin.domain.dcat.dcatserver.services.DcatServerService;
import be.vlaanderen.informatievlaanderen.ldes.server.admin.domain.dcat.dcatserver.services.DcatServerServiceImpl;
import be.vlaanderen.informatievlaanderen.ldes.server.admin.domain.validation.DcatShaclValidator;
import be.vlaanderen.informatievlaanderen.ldes.server.admin.domain.view.service.DcatViewService;
import be.vlaanderen.informatievlaanderen.ldes.server.domain.constants.RdfConstants;
import be.vlaanderen.informatievlaanderen.ldes.server.domain.model.DcatView;
import be.vlaanderen.informatievlaanderen.ldes.server.domain.model.ViewName;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static be.vlaanderen.informatievlaanderen.ldes.server.domain.constants.RdfConstants.RDF_SYNTAX_TYPE;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DcatServerServiceImplTest {
	private static final String ID = "2a896d35-8c72-4723-83b3-add9b1be96aa";
	private Model dcat;
	private DcatServer serverDcat;
	private DcatServerService service;
	@Mock
	private DcatServerRepository repository;
	@Mock
	private DcatViewService dcatViewService;
	@Mock
	private DcatShaclValidator dcatShaclValidator;
	@Mock
	private DcatDatasetService dcatDatasetService;

	@BeforeEach
	void setUp() {
		service = new DcatServerServiceImpl(repository, dcatViewService, dcatDatasetService, "http://localhost.dev",
				dcatShaclValidator);
	}

	@Nested
	class CreateDcat {
		@BeforeEach
		void setUp() {
			dcat = RDFParser.source("dcat/valid-server-dcat.ttl").lang(Lang.TURTLE).toModel();
			serverDcat = new DcatServer(ID, dcat);
		}

		@Test
		void when_ServerHasNoDcat_and_CreateDcat_then_ReturnIt() {
			when(repository.getServerDcat()).thenReturn(List.of());
			when(repository.saveServerDcat(any())).thenReturn(serverDcat);

			final DcatServer createdDcat =  service.createDcatServer(dcat);
			createdDcat.getDcat().listStatements(null, RdfConstants.DC_IDENTIFIER, ID);

			assertThat(createdDcat).isEqualTo(serverDcat);
			InOrder inOrder = Mockito.inOrder(repository);
			inOrder.verify(repository).getServerDcat();
			inOrder.verify(repository).saveServerDcat(argThat(dcatServer -> dcatServer.getDcat().listObjectsOfProperty(RdfConstants.DC_IDENTIFIER).hasNext()));
			inOrder.verifyNoMoreInteractions();
		}

		@Test
		void when_ServerAlreadyHasDcatConfigured_then_ThrowException() {
			when(repository.getServerDcat()).thenReturn(List.of(serverDcat));

			assertThatThrownBy(() -> service.createDcatServer(dcat))
					.isInstanceOf(DcatAlreadyConfiguredException.class)
					.hasMessage("The server can contain only one dcat configuration and there already has been configured one with id %s", ID);
			verify(repository).getServerDcat();
			verifyNoMoreInteractions(repository);
		}

		@Test
		void when_ServerHasNoDcat_and_CreateDcatWithEmptyModel_then_ThrowException() {
			when(repository.getServerDcat()).thenReturn(List.of());
			Model emptyDcat = ModelFactory.createDefaultModel();

			assertThatThrownBy(() -> service.createDcatServer(emptyDcat))
					.isInstanceOf(IllegalArgumentException.class)
					.hasMessage("Provided dcat must have a statement with a %s predicate", RDF_SYNTAX_TYPE.toString())
					.hasCauseInstanceOf(NoSuchElementException.class);
			verify(repository).getServerDcat();
			verifyNoMoreInteractions(repository);
		}
	}

	@Nested
	class UpdateDcat {
		@BeforeEach
		void setUp() {
			dcat = RDFParser.source("dcat/server-dcat-with-id.ttl").lang(Lang.TURTLE).toModel();
			serverDcat = new DcatServer(ID, dcat);
		}

		@Test
		void when_UpdateExistingDcat_then_ReturnDcat() {
			when(repository.getServerDcatById(ID)).thenReturn(Optional.of(serverDcat));
			when(repository.saveServerDcat(serverDcat)).thenReturn(serverDcat);

			DcatServer updatedDcatServer = service.updateDcatServer(ID, dcat);

			assertThat(updatedDcatServer).isEqualTo(serverDcat);
			InOrder inOrder = inOrder(repository);
			inOrder.verify(repository).getServerDcatById(ID);
			inOrder.verify(repository).saveServerDcat(DcatServerServiceImplTest.this.serverDcat);
			inOrder.verifyNoMoreInteractions();
		}

		@Test
		void when_UpdateNonExistingDcat_then_ThrowException() {
			when(repository.getServerDcatById(ID)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> service.updateDcatServer(ID, dcat))
					.isInstanceOf(MissingDcatServerException.class)
					.hasMessage("No dcat is configured on the server with id %s", ID);
			verify(repository).getServerDcatById(ID);
			verifyNoMoreInteractions(repository);
		}
	}

	@Test
	void when_DeleteExistingDcat_then_ReturnVoid() {
		assertThatNoException().isThrownBy(() -> service.deleteDcatServer(ID));
		verify(repository).deleteServerDcat(ID);
		verifyNoMoreInteractions(repository);
	}

	@Nested
	class GetComposedDcat {

		private final static String COLLECTION_PARCELS = "parcels";
		private final static String COLLECTION_BUILDINGS = "buildings";
		private final static String VIEW_BY_PAGE = "by-page";
		private final static String VIEW_BY_TIME = "by-time";

		@Test
		void should_ReturnEmptyModel_when_NoServerDcatPresent() {
			when(repository.findSingleDcatServer()).thenReturn(Optional.empty());

			Model result = service.getComposedDcat();

			assertThat(result).matches(Model::isEmpty);
			verify(dcatShaclValidator).validate(any());
		}

		@Test
		void should_CombineServerAndDataset_when_TheseArePresent_and_ViewsAreNot() {
			when(repository.findSingleDcatServer()).thenReturn(createServer());
			when(dcatDatasetService.findAll()).thenReturn(createDatasets());
			when(dcatViewService.findAll()).thenReturn(List.of());

			Model result = service.getComposedDcat();

			String path = "dcat/dcat-combined-without-views.ttl";
			Model expectedResult = RDFParser.source(path).lang(Lang.TURTLE).build().toModel();
			assertThat(result).matches(expectedResult::isIsomorphicWith);
		}

		@Test
		void should_CombineServerDatasetAndViews_when_AllArePresent() {
			when(repository.findSingleDcatServer()).thenReturn(createServer());
			when(dcatDatasetService.findAll()).thenReturn(createDatasets());
			when(dcatViewService.findAll()).thenReturn(createViews());

			Model result = service.getComposedDcat();

			String path = "dcat/dcat-combined-all.ttl";
			Model expectedResult = RDFParser.source(path).lang(Lang.TURTLE).build().toModel();
			assertThat(result).matches(expectedResult::isIsomorphicWith);
		}

		private Optional<DcatServer> createServer() {
			Model server = RDFParser.source("dcat/server.ttl").lang(Lang.TURTLE).build().toModel();
			return Optional.of(new DcatServer("id1", server));
		}

		private List<DcatView> createViews() {
			final List<DcatView> views = new ArrayList<>();
			views.addAll(createDcatBuildingViews());
			views.addAll(createDcatParcelViews());
			return views;
		}

		private List<DcatDataset> createDatasets() {
			Model parcels = RDFParser.source("dcat/parcels.ttl").lang(Lang.TURTLE).build().toModel();
			DcatDataset parcelDataset = new DcatDataset(COLLECTION_PARCELS, parcels);

			Model buildings = RDFParser.source("dcat/buildings.ttl").lang(Lang.TURTLE).build().toModel();
			DcatDataset buildingDataset = new DcatDataset(COLLECTION_BUILDINGS, buildings);
			return List.of(parcelDataset, buildingDataset);
		}

		private List<DcatView> createDcatParcelViews() {
			Model byPage = RDFParser.source("dcat/view-by-page.ttl").lang(Lang.TURTLE).build().toModel();
			Model byTime = RDFParser.source("dcat/view-by-geospatial.ttl").lang(Lang.TURTLE).build().toModel();
			return List.of(
					DcatView.from(new ViewName(COLLECTION_PARCELS, VIEW_BY_PAGE), byPage),
					DcatView.from(new ViewName(COLLECTION_PARCELS, VIEW_BY_TIME), byTime));
		}

		private List<DcatView> createDcatBuildingViews() {
			Model byPage = RDFParser.source("dcat/view-by-page.ttl").lang(Lang.TURTLE).build().toModel();
			return List.of(DcatView.from(new ViewName(COLLECTION_BUILDINGS, VIEW_BY_PAGE), byPage));
		}

	}

}
