package com.microservices.demo.elastic.query.service.api;

import com.microservices.demo.elastic.query.client.service.ElasticQueryClient;
import com.microservices.demo.elastic.query.service.model.SocialEventQueryResponseModel;
import com.microservices.demo.elastic.query.service.transformer.ElasticToResponseModelTransformer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/events", produces = "application/json")
@Tag(name = "Social Events API", description = "REST API for querying social events from Elasticsearch")
@Validated
public class ElasticQueryController {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticQueryController.class);

    private final ElasticQueryClient elasticQueryClient;
    private final ElasticToResponseModelTransformer transformer;

    public ElasticQueryController(ElasticQueryClient queryClient,
                                   ElasticToResponseModelTransformer elasticToResponseModelTransformer) {
        this.elasticQueryClient = queryClient;
        this.transformer = elasticToResponseModelTransformer;
    }

    @Operation(summary = "Get event by ID", description = "Retrieve a single social event by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event found",
                    content = @Content(schema = @Schema(implementation = SocialEventQueryResponseModel.class))),
            @ApiResponse(responseCode = "404", description = "Event not found", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<SocialEventQueryResponseModel> getEventById(
            @Parameter(description = "Event ID", required = true)
            @PathVariable @NotBlank String id) {
        LOG.info("Received request to get event by id: {}", id);
        var indexModel = elasticQueryClient.getIndexModelById(id);
        return ResponseEntity.ok(transformer.getResponseModel(indexModel));
    }

    @Operation(summary = "Get all events", description = "Retrieve all social events with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Events retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping
    public ResponseEntity<Page<SocialEventQueryResponseModel>> getAllEvents(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        LOG.info("Received request to get all events. Page: {}, Size: {}",
                pageable.getPageNumber(), pageable.getPageSize());
        var indexModelsPage = elasticQueryClient.getAllIndexModels(pageable);
        var responseModels = transformer.getResponseModels(indexModelsPage.getContent());
        return ResponseEntity.ok(new PageImpl<>(responseModels, pageable, indexModelsPage.getTotalElements()));
    }

    @Operation(summary = "Search events by text", description = "Full-text search for social events containing the specified text")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Invalid search text", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/search")
    public ResponseEntity<Page<SocialEventQueryResponseModel>> searchEventsByText(
            @Parameter(description = "Search text", required = true)
            @RequestParam @NotBlank String text,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        LOG.info("Received request to search events by text: '{}'. Page: {}, Size: {}",
                text, pageable.getPageNumber(), pageable.getPageSize());
        var indexModelsPage = elasticQueryClient.getIndexModelByText(text, pageable);
        var responseModels = transformer.getResponseModels(indexModelsPage.getContent());
        return ResponseEntity.ok(new PageImpl<>(responseModels, pageable, indexModelsPage.getTotalElements()));
    }

    @Operation(summary = "Get events by user ID", description = "Retrieve all social events created by a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Events retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user ID", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SocialEventQueryResponseModel>> getEventsByUserId(
            @Parameter(description = "User ID", required = true)
            @PathVariable @NotNull Long userId) {
        LOG.info("Received request to get events by userId: {}", userId);
        var indexModels = elasticQueryClient.getIndexModelByUserId(userId);
        return ResponseEntity.ok(transformer.getResponseModels(indexModels));
    }
}
