package com.kh.flokrGroupware.search.model.service;

import java.util.List;
import java.util.Map;

public interface TaskSearchService {
	
	List<Map<String, Object>> searchTasks(String keyword, boolean forceOriginal);
	
	List<String> getSuggestions(String prefix);

}
