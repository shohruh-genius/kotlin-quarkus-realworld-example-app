package io.realworld.domain.article

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName
import io.quarkus.runtime.annotations.RegisterForReflection
import io.realworld.domain.profile.ProfileResponse
import io.realworld.domain.tag.TagsResponse
import java.time.Instant

@JsonRootName("article")
@RegisterForReflection
data class ArticleResponse(
    @JsonProperty("slug")
    val slug: String,
    @JsonProperty("title")
    val title: String,
    @JsonProperty("description")
    val description: String,
    @JsonProperty("body")
    val body: String,
    @JsonProperty("tagList")
    val tagList: TagsResponse,
    // FIXME: pattern is failing to pass tests "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    @JsonProperty("createdAt")
    @JsonFormat(shape = STRING)
    val createdAt: Instant,
    // FIXME: pattern is failing to pass tests "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
    @JsonProperty("updatedAt")
    @JsonFormat(shape = STRING)
    val updatedAt: Instant,
    @JsonProperty("favorite")
    val favorite: Boolean,
    @JsonProperty("favoritesCount")
    val favoritesCount: Long,
    @JsonProperty("author")
    val author: ProfileResponse
) {
    companion object {
        @JvmStatic
        fun build(
            article: Article,
            favoritesCount: Long = 0,
            isFavorite: Boolean = false,
            isFollowing: Boolean = false
        ): ArticleResponse =
            ArticleResponse(
                slug = article.slug.toString(),
                title = article.title,
                description = article.description,
                body = article.body,
                tagList = TagsResponse.build(article.tagList),
                createdAt = article.createdAt,
                updatedAt = article.updatedAt,
                favorite = isFavorite,
                favoritesCount = favoritesCount,
                author = ProfileResponse.build(article.author, isFollowing)
            )
    }
}
