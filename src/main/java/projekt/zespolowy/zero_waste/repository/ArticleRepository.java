package projekt.zespolowy.zero_waste.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import projekt.zespolowy.zero_waste.entity.EducationalEntities.Article;
import projekt.zespolowy.zero_waste.entity.EducationalEntities.ArticleCategory;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    Page<Article> findByArticleCategory(ArticleCategory category, Pageable pageable);
    Page<Article> findByTitleContainingIgnoreCase(String title, Pageable pageable);


}
