package io.realworld.domain.tag

import io.realworld.infrastructure.web.Routes.TAGS_PATH
import io.realworld.utils.ValidationMessages.Companion.REQUEST_BODY_MUST_NOT_BE_NULL
import jakarta.transaction.Transactional
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType.APPLICATION_JSON
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.Response.Status.CREATED
import jakarta.ws.rs.core.Response.Status.OK
import jakarta.ws.rs.core.Response.created
import jakarta.ws.rs.core.Response.ok
import jakarta.ws.rs.core.UriBuilder.fromResource

@Path(TAGS_PATH)
class TagResource(
    private val repository: TagRepository
) {
    @GET
    @Produces(APPLICATION_JSON)
    fun list(): Response =
        repository.listAll().run {
            ok(TagsResponse.build(this)).status(OK).build()
        }

    @POST
    @Transactional
    @Consumes(APPLICATION_JSON)
    fun create(
        @Valid
        @NotNull(message = REQUEST_BODY_MUST_NOT_BE_NULL)
        newTag: Tag
    ): Response =
        repository.persist(newTag).run {
            created(fromResource(TagResource::class.java).path("/" + newTag.name).build())
                .status(CREATED).build()
        }
}
