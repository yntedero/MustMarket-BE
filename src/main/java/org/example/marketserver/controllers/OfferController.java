package org.example.marketserver.controllers;

import java.util.Base64;

import org.example.marketserver.dtos.OfferDTO;
import org.example.marketserver.security.core.CustomAuthentication;
import org.example.marketserver.services.OfferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/offers")
public class OfferController {
    private final OfferService offerService;
    @Autowired
    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    @PostMapping
    ResponseEntity<Map<String, String>> createOffer(@RequestBody OfferDTO offerDTO) {
        if (offerDTO.getTitle() == null || offerDTO.getTitle().isEmpty() ||
                offerDTO.getDescription() == null || offerDTO.getDescription().isEmpty() ||
                offerDTO.getCityId() == null ||
                offerDTO.getCategoryId() == null ||
                offerDTO.getFile() == null || offerDTO.getFile().isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "All fields must be filled");
            return ResponseEntity.badRequest().body(response);
        }
        CustomAuthentication customAuth = (CustomAuthentication) SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) customAuth.getPrincipal();
        String userEmail = customAuth.getEmail();
        offerDTO.setUserId(userId);
        offerDTO.setUserEmail(userEmail);
        OfferDTO savedOfferDTO = offerService.createOffer(offerDTO);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Offer created with id: " + savedOfferDTO.getId());

        return ResponseEntity.ok(response);
    }
    @GetMapping
    public ResponseEntity<List<OfferDTO>> getAllOffers(@RequestParam(required = false) Long cityId,
                                                       @RequestParam(required = false) Long categoryId) {
        List<OfferDTO> filteredOffers = offerService.getAllOffers(cityId, categoryId);
        return ResponseEntity.ok(filteredOffers);
    }


    @GetMapping("/{id}")
    public ResponseEntity<OfferDTO> getOfferById(@PathVariable Long id) {
        OfferDTO offer = offerService.getOfferById(id);
        if (offer != null) {
            return ResponseEntity.ok(offer);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("@offerService.getOfferById(#id) == authentication.principal")
    public ResponseEntity<Void> deleteOffer(@PathVariable Long id) {
        offerService.deleteOffer(id);
        return ResponseEntity.noContent().build();
    }
}
