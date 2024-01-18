package io.realworld.domain.article

import io.quarkus.runtime.annotations.RegisterForReflection
import io.realworld.domain.comment.Comment
import io.realworld.domain.tag.Tag
import io.realworld.domain.user.User
import io.realworld.infrastructure.database.Tables.ARTICLE_TABLE
import io.realworld.infrastructure.database.Tables.TAG_RELATIONSHIP
import io.realworld.utils.ValidationMessages.Companion.TITLE_MUST_NOT_BE_BLANK
import org.hibernate.annotations.GenericGenerator
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction.CASCADE
import java.time.Instant
import java.time.Instant.now
import java.util.*
import java.util.UUID.randomUUID
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PastOrPresent
import jakarta.validation.constraints.Size

@Entity(name = ARTICLE_TABLE)
@RegisterForReflection
open class Article(
    @Id
    @Column(columnDefinition = "uuid")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    open var slug: UUID = randomUUID(),
    @field:Size(min = 5, max = 127)
    @field:NotBlank(message = TITLE_MUST_NOT_BE_BLANK)
    open var title: String = "",
    @field:Size(min = 0, max = 255)
    open var description: String = "",
    @field:Size(min = 0, max = 4095)
    open var body: String = "",
    @field:Size(min = 0, max = 5)
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = TAG_RELATIONSHIP)
    open var tagList: MutableList<Tag> = mutableListOf(),
    @field:PastOrPresent
    open var createdAt: Instant = now(),
    @field:PastOrPresent
    open var updatedAt: Instant = now(),
    @ManyToOne
    open var author: User = User(),
    @OneToMany(cascade = [CascadeType.REMOVE], mappedBy = "article", orphanRemoval = true)
    @OnDelete(action = CASCADE)
    open var comments: MutableList<Comment> = mutableListOf()
) {
    override fun toString(): String =
        "Article($slug, $title, ${description.take(20)}, ${body.take(20)}, $createdAt, $updatedAt, ${author.username})"

    final override fun hashCode(): Int = slug.hashCode()

    final override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Article) return false
        if (slug != other.slug) return false
        return true
    }
}
