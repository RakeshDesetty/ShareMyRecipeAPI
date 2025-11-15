package com.example.recipeapi.dto;

import java.util.List;

public class RecipeDtos {
    public static class CreateRequest {
        public String title;
        public String summary;
        public List<String> ingredients;
        public List<String> steps;
        public List<String> labels;
        public boolean publish = false;
    }
}
