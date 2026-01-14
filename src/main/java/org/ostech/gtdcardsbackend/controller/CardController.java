package org.ostech.gtdcardsbackend.controller;

import jakarta.validation.Valid;
import org.ostech.gtdcardsbackend.dto.ApiResponseDTO;
import org.ostech.gtdcardsbackend.dto.CardRequestDTO;
import org.ostech.gtdcardsbackend.dto.CardResponseDTO;
import org.ostech.gtdcardsbackend.dto.CardUpdateDTO;
import org.ostech.gtdcardsbackend.service.CardService;
import org.ostech.gtdcardsbackend.util.APIConstants;
import org.ostech.gtdcardsbackend.util.MessageConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PutMapping(APIConstants.CARD_UPDATE_ENDPOINT)
    public ResponseEntity<ApiResponseDTO<CardResponseDTO>> updateCard(
        @PathVariable Long id,
        @Valid @RequestBody CardUpdateDTO cardUpdateDTO) {
        CardResponseDTO cardResponseDTO = cardService.updateCard(id, cardUpdateDTO);
        ApiResponseDTO<CardResponseDTO> response = ApiResponseDTO.success(
            cardResponseDTO,
            MessageConstants.CARD_UPDATED_SUCCESS
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
