package com.example.soundCloud_BE.zingMp3.Dto;

import lombok.Data;
import java.util.List;

@Data
public class AcSuggestions {
    private String tracking;
    private List<AcSuggestionsItem> items;
    
    @Data
    public static class AcSuggestionsItem {
        private List<Keyword> keywords;
        private List<SongData> suggestions;
    }
    
    @Data
    public static class Keyword {
        private int type;
        private String keyword;
        private int suggestType;
    }
}
