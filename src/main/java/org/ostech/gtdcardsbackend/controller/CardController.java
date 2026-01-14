package org.ostech.gtdcardsbackend.controller;

import jakarta.validation.Valid;
import org.ostech.gtdcardsbackend.dto.ApiResponseDTO;
import org.ostech.gtdcardsbackend.dto.CardRequestDTO;
import org.ostech.gtdcardsbackend.dto.CardResponseDTO;
import org.ostech.gtdcardsbackend.service.CardService;
import org.ostech.gtdcardsbackend.util.APIConstants;
import org.ostech.gtdcardsbackend.util.MessageConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(APIConstants.CARD_ENDPOINT)
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<CardResponseDTO>> createCard(
        @Valid @RequestBody CardRequestDTO cardRequestDTO) {
        CardResponseDTO cardResponseDTO = cardService.createCard(cardRequestDTO);
        ApiResponseDTO<CardResponseDTO> response = ApiResponseDTO.success(
            cardResponseDTO,
            MessageConstants.CARD_CREATED_SUCCESS
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
