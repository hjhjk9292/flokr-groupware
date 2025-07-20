package com.kh.flokrGroupware.search.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.kh.flokrGroupware.search.model.service.TaskSearchService;

@Controller
public class TaskSearchController {
	
	@Autowired
    private TaskSearchService taskSearchService;
	
	@GetMapping("/searchTasks")
	@ResponseBody
	public List<Map<String, Object>> search(@RequestParam String keyword, 
	                                      @RequestParam(required = false, defaultValue = "false") boolean forceOriginal) {
	    return taskSearchService.searchTasks(keyword, forceOriginal);
	}
	
	@GetMapping("/autocomplete")
	@ResponseBody
	public List<String> autocomplete(@RequestParam String prefix) {
	    return taskSearchService.getSuggestions(prefix);
	}


}
