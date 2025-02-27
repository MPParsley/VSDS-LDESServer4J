package be.vlaanderen.informatievlaanderen.ldes.server.rest.eventstream;

import be.vlaanderen.informatievlaanderen.ldes.server.admin.spi.EventStreamResponse;
import be.vlaanderen.informatievlaanderen.ldes.server.admin.spi.EventStreamServiceSpi;
import be.vlaanderen.informatievlaanderen.ldes.server.rest.caching.CachingStrategy;
import be.vlaanderen.informatievlaanderen.ldes.server.rest.config.RestConfig;
import io.micrometer.observation.annotation.Observed;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.jena.rdf.model.Model;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpHeaders.*;

@Observed
@RestController
public class EventStreamController implements OpenApiEventStreamController {

	private final RestConfig restConfig;
	private final CachingStrategy cachingStrategy;
	private final EventStreamServiceSpi eventStreamService;

	public EventStreamController(RestConfig restConfig, CachingStrategy cachingStrategy,
			EventStreamServiceSpi eventStreamService) {
		this.restConfig = restConfig;
		this.cachingStrategy = cachingStrategy;
		this.eventStreamService = eventStreamService;
	}

	@GetMapping("/")
	public Model getDcat(@RequestHeader(value = HttpHeaders.ACCEPT, defaultValue = "text/turtle") String language, HttpServletResponse response) {
		setContentTypeHeader(language, response);
		return eventStreamService.getComposedDcat();
	}

	@Override
	@CrossOrigin(origins = "*", allowedHeaders = "")
	@GetMapping(value = "{collectionname}")
	public ResponseEntity<EventStreamResponse> retrieveLdesFragment(
			@RequestHeader(value = HttpHeaders.ACCEPT, defaultValue = "text/turtle") String language,
			HttpServletResponse response, @PathVariable("collectionname") String collectionName) {
		EventStreamResponse eventStream = eventStreamService.retrieveEventStream(collectionName);

		response.setHeader(CACHE_CONTROL, restConfig.generateImmutableCacheControl());
		response.setHeader(CONTENT_DISPOSITION, RestConfig.INLINE);
		setContentTypeHeader(language, response);

		return ResponseEntity
				.ok()
				.eTag(cachingStrategy.generateCacheIdentifier(eventStream.getCollection(), language))
				.body(eventStream);
	}

	private void setContentTypeHeader(String language, HttpServletResponse response) {
		if (language.equals(MediaType.ALL_VALUE) || language.contains(MediaType.TEXT_HTML_VALUE)) {
			response.setHeader(CONTENT_TYPE, RestConfig.TEXT_TURTLE);
		} else {
			response.setHeader(CONTENT_TYPE, language.split(",")[0]);
		}
	}
}
