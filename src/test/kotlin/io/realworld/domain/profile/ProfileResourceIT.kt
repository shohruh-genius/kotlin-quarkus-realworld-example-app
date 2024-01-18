package io.realworld.domain.profile

import io.quarkus.test.common.http.TestHTTPEndpoint
import io.quarkus.test.junit.QuarkusTest
import io.quarkus.test.junit.mockito.InjectMock
import io.quarkus.test.security.TestSecurity
import io.realworld.infrastructure.security.Role.USER
import io.realworld.support.factory.ProfileFactory
import io.realworld.support.factory.UserFactory
import io.restassured.RestAssured.given
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.`is`
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import jakarta.ws.rs.core.MediaType.APPLICATION_JSON
import jakarta.ws.rs.core.Response.Status.OK

@QuarkusTest
@TestHTTPEndpoint(ProfileResource::class)
internal class ProfileResourceIT {
    @InjectMock
    lateinit var service: ProfileService

    @Test
    @TestSecurity(user = "loggedInUser", roles = [USER])
    fun `Given an existing user, when profile is requested, then correct details should be returned`() {
        val loggedInUser = UserFactory.create(username = "loggedInUser")
        val existingUser = UserFactory.create()

        `when`(service.findProfile(existingUser.username, loggedInUser.username))
            .thenReturn(ProfileFactory.create(existingUser))

        given()
            .accept(APPLICATION_JSON)
            .get(existingUser.username)
            .then()
            .body("size()", equalTo(1))
            .body("profile.size()", `is`(4))
            .body("profile.username", equalTo(existingUser.username))
            .body("profile.bio", equalTo(existingUser.bio))
            .body("profile.image", equalTo(existingUser.image))
            .body("profile.following", `is`(false))
            .contentType(APPLICATION_JSON)
            .statusCode(OK.statusCode)

        verify(service).findProfile(existingUser.username, loggedInUser.username)
    }

    @Test
    @TestSecurity(user = "anonymous", roles = [])
    fun `Given an existing user, when a non-authenticated user requests a profile, then they should still be able to access the profile`() {
        val existingUser = UserFactory.create()

        // FIXME: is there a way to re-write this test without having to pass an "anonymous" parameter or TestSecurity?
        `when`(service.findProfile(existingUser.username, "anonymous"))
            .thenReturn(ProfileFactory.create(existingUser))

        given()
            .accept(APPLICATION_JSON)
            .get(existingUser.username)
            .then()
            .body("size()", equalTo(1))
            .body("profile.size()", `is`(4))
            .body("profile.username", equalTo(existingUser.username))
            .body("profile.bio", equalTo(existingUser.bio))
            .body("profile.image", equalTo(existingUser.image))
            .body("profile.following", `is`(false))
            .contentType(APPLICATION_JSON)
            .statusCode(OK.statusCode)

        verify(service).findProfile(existingUser.username, "anonymous")
    }

    @Test
    @TestSecurity(user = "loggedInUser", roles = [USER])
    fun `Given an existing user, when logged-in user follows existing user, then the follow relationship should be persisted`() {
        val loggedInUser = UserFactory.create(username = "loggedInUser")
        val existingUser = UserFactory.create()

        `when`(service.findProfile(existingUser.username, loggedInUser.username))
            .thenReturn(ProfileFactory.create(existingUser, following = true))

        given()
            .accept(APPLICATION_JSON)
            .post("${existingUser.username}/follow")
            .then()
            .body("size()", equalTo(1))
            .body("profile.size()", `is`(4))
            .body("profile.username", equalTo(existingUser.username))
            .body("profile.bio", equalTo(existingUser.bio))
            .body("profile.image", equalTo(existingUser.image))
            .body("profile.following", `is`(true))
            .contentType(APPLICATION_JSON)
            .statusCode(OK.statusCode)

        verify(service).follow(existingUser.username, loggedInUser.username)
        verify(service).findProfile(existingUser.username, loggedInUser.username)
    }

    @Test
    @TestSecurity(user = "loggedInUser", roles = [USER])
    fun `Given an existing user, when logged-in user unfollows an existing user, then the follow relationship should be deleted`() {
        val loggedInUser = UserFactory.create(username = "loggedInUser")
        val existingUser = UserFactory.create()

        `when`(service.findProfile(existingUser.username, loggedInUser.username))
            .thenReturn(ProfileFactory.create(existingUser))

        given()
            .accept(APPLICATION_JSON)
            .delete("${existingUser.username}/follow")
            .then()
            .body("size()", equalTo(1))
            .body("profile.size()", `is`(4))
            .body("profile.username", equalTo(existingUser.username))
            .body("profile.bio", equalTo(existingUser.bio))
            .body("profile.image", equalTo(existingUser.image))
            .body("profile.following", `is`(false))
            .contentType(APPLICATION_JSON)
            .statusCode(OK.statusCode)

        verify(service).unfollow(existingUser.username, loggedInUser.username)
        verify(service).findProfile(existingUser.username, loggedInUser.username)
    }
}
