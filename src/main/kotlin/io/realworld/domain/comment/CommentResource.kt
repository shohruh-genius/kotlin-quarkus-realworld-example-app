package io.realworld.domain.comment

import io.realworld.domain.exception.InvalidAuthorException
import io.realworld.infrastructure.security.Role.ADMIN
import io.realworld.infrastructure.security.Role.USER
import io.realworld.infrastructure.web.Routes.ARTICLES_PATH
import io.realworld.utils.ValidationMessages.Companion.REQUEST_BODY_MUST_NOT_BE_NULL
import java.util.*
import jakarta.annotation.security.PermitAll
import jakarta.annotation.security.RolesAllowed
import jakarta.transaction.Transactional
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType.APPLICATION_JSON
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.Response.Status.CREATED
import jakarta.ws.rs.core.Response.Status.OK
import jakarta.ws.rs.core.Response.ok
import jakarta.ws.rs.core.SecurityContext
import jakarta.ws.rs.core.UriBuilder.fromResource

@Path(ARTICLES_PATH)
class CommentResource(
    private val service: CommentService
) {
    @GET
    @Path("/{slug}/comments")
    @Produces(APPLICATION_JSON)
    @PermitAll
    fun list(
        @PathParam("slug") slug: UUID,
        @Context securityContext: SecurityContext
    ): Response = ok(service.list(slug, securityContext.userPrincipal.name)).status(OK).build()

    @POST
    @Path("/{slug}/comments")
    @Transactional
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @RolesAllowed(USER)
    fun create(
        @Valid @NotNull(message = REQUEST_BODY_MUST_NOT_BE_NULL) newCommentRequest: CommentCreateRequest,
        @PathParam("slug") slug: UUID,
        @Context securityContext: SecurityContext
    ): Response =
        service.create(newCommentRequest, slug, securityContext.userPrincipal.name).run {
            ok(this).location(fromResource(CommentResource::class.java).path("/$slug/comments/$id").build()).status(CREATED).build()
        }

    @DELETE
    @Path("/{slug}/comments/{id}")
    @Transactional
    @Produces(APPLICATION_JSON)
    @RolesAllowed(USER, ADMIN)
    fun delete(
        @PathParam("slug") slug: UUID,
        @PathParam("id") id: Long,
        @Context securityContext: SecurityContext
    ): Response =
        securityContext.run {
            if (service.isCommentAuthor(id, userPrincipal.name) || isUserInRole(ADMIN)) {
                ok(service.delete(id)).build()
            } else {
                throw InvalidAuthorException()
            }
        }
}
