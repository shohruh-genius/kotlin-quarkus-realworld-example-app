package io.realworld.domain.user

import io.quarkus.runtime.annotations.RegisterForReflection
import io.realworld.domain.article.Article
import io.realworld.domain.comment.Comment
import io.realworld.infrastructure.database.Tables.FOLLOW_RELATIONSHIP
import io.realworld.infrastructure.database.Tables.USER_TABLE
import io.realworld.utils.Patterns.Companion.ALPHANUMERICAL
import io.realworld.utils.ValidationMessages.Companion.EMAIL_MUST_BE_NOT_BLANK
import io.realworld.utils.ValidationMessages.Companion.PASSWORD_MUST_BE_NOT_BLANK
import io.realworld.utils.ValidationMessages.Companion.USERNAME_MUST_MATCH_PATTERN
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction.CASCADE
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.OneToMany
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

@Entity(name = USER_TABLE)
@RegisterForReflection
open class User(
    @Id
    @field:Pattern(regexp = ALPHANUMERICAL, message = USERNAME_MUST_MATCH_PATTERN)
    open var username: String = "",
    @field:Email
    @field:NotBlank(message = EMAIL_MUST_BE_NOT_BLANK)
    @Column(unique = true)
    open var email: String = "",
    @field:NotBlank(message = PASSWORD_MUST_BE_NOT_BLANK)
    open var password: String = "",
    @field:Size(min = 0, max = 255)
    open var bio: String = "",
    @field:Size(min = 0, max = 2097152) // max = 1920 x 1080-pixel resolution
    open var image: String = "",
    @ManyToMany
    @JoinTable(
        name = FOLLOW_RELATIONSHIP,
        joinColumns = [JoinColumn(name = "userId", referencedColumnName = "username")],
        inverseJoinColumns = [JoinColumn(name = "followingId", referencedColumnName = "username")]
    )
    open var follows: MutableList<User> = mutableListOf(),
    @OneToMany(cascade = [CascadeType.REMOVE], mappedBy = "author", orphanRemoval = true)
    @OnDelete(action = CASCADE)
    open var articles: MutableList<Article> = mutableListOf(),
    @OneToMany(cascade = [CascadeType.REMOVE], mappedBy = "author", orphanRemoval = true)
    @OnDelete(action = CASCADE)
    open var comments: MutableList<Comment> = mutableListOf()
) {
    override fun toString(): String = "User($username, $email, ${bio.take(20)}..., $image)"

    final override fun hashCode(): Int = username.hashCode()

    final override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false
        if (username != other.username) return false
        return true
    }
}
