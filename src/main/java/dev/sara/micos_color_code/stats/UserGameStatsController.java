package dev.sara.micos_color_code.stats;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/users/play/{gameId}/ranking")
public class UserGameStatsController {
    private final UserGameStatsService userGameStatsService;

    public UserGameStatsController(UserGameStatsService userGameStatsService) {
        this.userGameStatsService = userGameStatsService;
    }
    
    @GetMapping
    public ResponseEntity<RankingResponseDTO> getRanking(
            @PathVariable Long gameId,
            Authentication authentication) {
        
        Long userId = getUserIdFromAuthentication(authentication);
        RankingResponseDTO response = userGameStatsService.getRanking(gameId, userId);
        return ResponseEntity.ok(response);
    }
    
    /*private Long getUserIdFromAuthentication(Authentication authentication) {
        // Tu lógica de autenticación aquí
        return 1L; // placeholder
    }*/
}
