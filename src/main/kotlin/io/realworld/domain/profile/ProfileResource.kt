package io.realworld.domain.profile

import io.realworld.infrastructure.security.Role.USER
import io.realworld.infrastructure.web.Routes.PROFILES_PATH
import jakarta.annotation.security.PermitAll
import jakarta.annotation.security.RolesAllowed
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType.APPLICATION_JSON
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.Response.Status.OK
import jakarta.ws.rs.core.Response.ok
import jakarta.ws.rs.core.SecurityContext

@Path(PROFILES_PATH)
class ProfileResource(
    private val service: ProfileService
) {
    @GET
    @Path("/{username}")
    @Produces(APPLICATION_JSON)
    @PermitAll
    fun getProfile(
        @Context securityContext: SecurityContext,
        @PathParam("username") username: String
    ): Response = ok(service.findProfile(username, securityContext.userPrincipal.name)).status(OK).build()

    @POST
    @Path("/{username}/follow")
    @Produces(APPLICATION_JSON)
    @RolesAllowed(USER)
    fun follow(
        @Context securityContext: SecurityContext,
        @PathParam("username") username: String
    ): Response =
        service.follow(username, securityContext.userPrincipal.name).run {
            ok(service.findProfile(username, securityContext.userPrincipal.name)).status(OK).build()
        }

    @DELETE
    @Path("/{username}/follow")
    @Produces(APPLICATION_JSON)
    @RolesAllowed(USER)
    fun unfollow(
        @Context securityContext: SecurityContext,
        @PathParam("username") username: String
    ): Response =
        service.unfollow(username, securityContext.userPrincipal.name).run {
            ok(service.findProfile(username, securityContext.userPrincipal.name)).status(OK).build()
        }
}
