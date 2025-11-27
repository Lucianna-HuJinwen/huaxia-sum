package com.liam.term.elasticsearch;


import com.liam.term.domain.es.TermDictES;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TermDictRepository extends ElasticsearchRepository<TermDictES, Long> {

    @Query("""
            {
              "bool": {
                "must": [
                  {
                    "bool": {
                      "should": [
                        { "match": { "sourceTerm": "?0" } },
                        { "match": { "targetTerm": "?0" } }
                      ]
                    }
                  },
                  { "term": { "glossaryId": "?1" } }
                ]
              }
            }
            """)
    Page<TermDictES> findByKeywordAndGlossaryId(String keyword, Long glossaryId, Pageable pageable);

    @Query("""
            {
              "bool": {
                "should": [
                  { "match": { "sourceTerm": "?0" } },
                  { "match": { "targetTerm": "?0" } }
                ]
              }
            }
            """)
    Page<TermDictES> findByKeyword(String keyword, Pageable pageable);

    Page<TermDictES> findByGlossaryId(Long glossaryId, Pageable pageable);

    long countByGlossaryId(Long glossaryId);
} 