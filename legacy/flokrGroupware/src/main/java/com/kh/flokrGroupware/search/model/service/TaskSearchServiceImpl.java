package com.kh.flokrGroupware.search.model.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaskSearchServiceImpl implements TaskSearchService {
	
	@Autowired
    private RestHighLevelClient client;

	@Override
	public List<Map<String, Object>> searchTasks(String keyword, boolean forceOriginal) {
	    List<Map<String, Object>> resultList = new ArrayList<>();
        String searchKeyword = keyword; // 실제로 검색에 사용할 키워드
        
	    // 1. 오타 수정 처리 (forceOriginal이 false일 때만)
	    if (!forceOriginal) {
	        SearchRequest suggestRequest = new SearchRequest("flokr_task_index");
	        SearchSourceBuilder suggestSourceBuilder = new SearchSourceBuilder();
	        
	        // suggest 쿼리 구성
	        SuggestBuilder suggestBuilder = new SuggestBuilder();
	        CompletionSuggestionBuilder completionBuilder = SuggestBuilders
	            .completionSuggestion("suggest")
	            .prefix(keyword, Fuzziness.TWO)
	            .skipDuplicates(true)
	            .size(1);
	        suggestBuilder.addSuggestion("corrected", completionBuilder);
	        suggestSourceBuilder.suggest(suggestBuilder);
	        suggestRequest.source(suggestSourceBuilder);
	        
	        try {
	            SearchResponse suggestResponse = client.search(suggestRequest, RequestOptions.DEFAULT);
	            Suggest suggest = suggestResponse.getSuggest();
	            
	            if (suggest != null) {
	                CompletionSuggestion correction = suggest.getSuggestion("corrected");
	                
	                if (correction != null && !correction.getOptions().isEmpty()) {
	                    String corrected = correction.getOptions().get(0).getText().string();
	                    
	                    // 원본 키워드와 수정된 키워드가 다를 때만 수정 정보 추가
	                    if (!corrected.equals(keyword)) {
	                        Map<String, Object> correctionInfo = new HashMap<>();
	                        correctionInfo.put("_corrected", corrected);
	                        resultList.add(correctionInfo); // 결과 목록 맨 앞에 추가
	                        
	                        // 교정된 키워드로 검색 진행
	                        searchKeyword = corrected;
	                    }
	                }
	            }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }

	    // 2. 검색 쿼리 구성 (항상 실행)
	    SearchRequest searchRequest = new SearchRequest("flokr_task_index");
	    SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
	    
	    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
	    boolQuery.should(QueryBuilders.matchQuery("taskTitle", searchKeyword).boost(3.0f));
	    boolQuery.should(QueryBuilders.matchQuery("taskContent", searchKeyword).boost(1.5f));
	    boolQuery.should(QueryBuilders.matchQuery("category", searchKeyword).boost(1.0f));
	    boolQuery.minimumShouldMatch(1);
	    
	    sourceBuilder.query(boolQuery);
	    sourceBuilder.sort(SortBuilders.scoreSort().order(SortOrder.DESC));
	    searchRequest.source(sourceBuilder);

	    // 3. 실제 검색 실행
	    try {
	        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
	        for (SearchHit hit : searchResponse.getHits()) {
	            resultList.add(hit.getSourceAsMap());
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    }

	    return resultList;
	}

	@Override
	public List<String> getSuggestions(String prefix) {
		List<String> suggestions = new ArrayList<>();

	    SearchRequest searchRequest = new SearchRequest("flokr_task_index");
	    SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

	    SuggestBuilder suggestBuilder = new SuggestBuilder();
	    CompletionSuggestionBuilder completionSuggestionBuilder =
	        SuggestBuilders.completionSuggestion("suggest")
	                       .prefix(prefix)
	                       .skipDuplicates(true)
	                       .size(10);  // 결과 개수 조절 가능

	    suggestBuilder.addSuggestion("task-suggest", completionSuggestionBuilder);
	    sourceBuilder.suggest(suggestBuilder);
	    searchRequest.source(sourceBuilder);

	    try {
	        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
	        Suggest suggest = response.getSuggest();
	        CompletionSuggestion taskSuggest = suggest.getSuggestion("task-suggest");

	        for (CompletionSuggestion.Entry.Option option : taskSuggest.getOptions()) {
	            suggestions.add(option.getText().string());
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    }

	    return suggestions;
	}
}