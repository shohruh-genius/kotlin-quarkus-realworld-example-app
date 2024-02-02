package io.realworld.domain.article

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import io.quarkus.panache.common.Page.of
import io.quarkus.panache.common.Parameters
import io.quarkus.panache.common.Sort
import io.realworld.infrastructure.database.Tables.ARTICLE_TABLE
import io.realworld.infrastructure.database.Tables.FOLLOW_RELATIONSHIP
import io.realworld.utils.QueryBuilder
import io.realworld.utils.QueryBuilder.JOIN
import io.realworld.utils.QueryBuilder.SELECT
import io.realworld.utils.QueryBuilder.WHERE
import java.util.*
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class ArticleRepository : PanacheRepositoryBase<Article, UUID> {
    fun findBy(
        limit: Int = 20,
        offset: Int = 0,
        tags: List<String> = listOf(),
        authors: List<String> = listOf(),
        favorites: List<String> = listOf()
    ): List<Article> =
        with(hashMapOf<String, Any>()) {
            find(
                query =
                    QueryBuilder()
                        .add(SELECT("articles from $ARTICLE_TABLE as articles"))
                        .addIf(
                            tags.isNotEmpty(),
                            JOIN("articles.tagList as tags"),
                            WHERE("upper(tags.name) in (:tags)")
                        ) { this["tags"] = tags.toUpperCase() }
                        .addIf(
                            authors.isNotEmpty(),
                            JOIN("articles.author as authors"),
                            WHERE("upper(authors.username) in (:authors)")
                        ) { this["authors"] = authors.toUpperCase() }
                        .addIf(
                            favorites.isNotEmpty(),
                            JOIN("articles.favoriteBy as favorites"),
                            WHERE("upper(favorites.username) in (:favorites)")
                        ) { this["favorites"] = favorites.toUpperCase() }
                        .build(),
                sort =
                    Sort
                        .descending("createdAt")
                        .and("updatedAt")
                        .descending(),
                params = this
            )
                .page(of(offset, limit))
                .list()
        }

    fun findByTheAuthorsAUserFollows(
        limit: Int = 20,
        offset: Int = 0,
        userId: String
    ): List<Article> =
        run {
            val usernames =
                QueryBuilder().add(
                    SELECT("follows.id.followingId from $FOLLOW_RELATIONSHIP follows"),
                    WHERE("follows.id.userId = :loggedInUserId")
                )
            find(
                query =
                    QueryBuilder().add(
                        SELECT("articles from $ARTICLE_TABLE as articles"),
                        JOIN("articles.author as authors"),
                        WHERE("authors.username in ( $usernames )")
                    ).build(),
                sort =
                    Sort
                        .descending("createdAt")
                        .and("updatedAt")
                        .descending(),
                params = Parameters.with("loggedInUserId", userId)
            )
                .page(of(offset, limit))
                .list()
        }

    fun exists(subjectedArticleId: UUID): Boolean =
        count(
            query = "id.slug = :subjectedArticleId",
            params = Parameters.with("subjectedArticleId", subjectedArticleId)
        ) > 0

    fun exists(
        subjectedArticleId: UUID,
        withAuthorId: String
    ): Boolean =
        count(
            query = "id.slug = :subjectedArticleId AND author.username = :withAuthorId",
            params = Parameters.with("subjectedArticleId", subjectedArticleId).and("withAuthorId", withAuthorId)
        ) > 0

    private fun List<String>.toUpperCase(): List<String> = this.map(String::toUpperCase)
}
