package dev.sara.micos_color_code.stats;

import java.util.List;

public record RankingResponseDTO(List<RankingPlayerDTO> top3, RankingPlayerDTO currentUser) {}
